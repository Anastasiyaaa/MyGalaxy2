/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import com.onevizion.ExceptionHandlingJacksonObjectMapper;
import com.onevizion.dao.admin.ParamSystemDao;
import com.onevizion.dao.integration.IntegrationDao;
import com.onevizion.integration.scheduler.exception.IntegrationFilesProviderException;
import com.onevizion.integration.scheduler.exception.IntegrationRunPreparationException;
import com.onevizion.integration.scheduler.os.OsCommands;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

@Service
public class IntegrationRunHelper {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExceptionHandlingJacksonObjectMapper mapper = new ExceptionHandlingJacksonObjectMapper();
    private final String ovUrl;

    private final OsCommands osCommands;
    private final IntegrationFilesProvider integrationFilesProvider;
    private final IntegrationDao integrationDao;
    private final IntegrationRunStatusHandler integrationRunStatusHandler;

    @Autowired
    public IntegrationRunHelper(OsCommands osCommands, IntegrationFilesProvider integrationFilesProvider, IntegrationDao integrationDao,
                                IntegrationRunStatusHandler integrationRunStatusHandler, ParamSystemDao paramSystemDao) {
        this.osCommands = osCommands;
        this.integrationFilesProvider = integrationFilesProvider;
        this.integrationDao = integrationDao;
        this.integrationRunStatusHandler = integrationRunStatusHandler;

        ovUrl = paramSystemDao.findByName("DomainName").getValue();
    }

    public void prepareForIntegrationRun(RunningIntegration integration) {
        logger.info("Creating user");
        osCommands.createUser(integration.getWorkingDirectory().getFileName().toString(), integration.getOsUserName());

        logger.info("Creating Integration working directory");
        createWorkingDirectory(integration);

        logger.info("Granting privs on integration files for Owner");
        osCommands.grantPrivsOnDirectoryToCurrentUser(integration.getWorkingDirectory().getFileName().toString());

        logger.info("Loading or updating integration files to working directory");
        if (isDirectoryEmpty(integration.getWorkingDirectory())) {
            integrationFilesProvider.loadIntegrationFiles(integration, integration.getWorkingDirectory());
        } else {
            try {
                integrationFilesProvider.updateIntegrationFiles(integration, integration.getWorkingDirectory());

            } catch (IntegrationFilesProviderException e) {
                logger.error("Cannot update integration files", e);
                integrationRunStatusHandler.createWarningLog(integration, e.getMessage(), ExceptionUtils.getStackTrace(e));

                logger.info("Cleaning integration working directory");
                integrationFilesProvider.cleanIntegrationDirectory(integration.getWorkingDirectory());

                logger.info("Reloading integration files to working directory");
                integrationFilesProvider.loadIntegrationFiles(integration, integration.getWorkingDirectory());
            }
        }

        logger.info("Copying Integration Settings file to working directory");
        copySettingsFileToWorkingDirectory(integration);

        logger.info("Copying Integration process_id file to working directory");
        copyProcessIdFileToIntegrationWorkingDirectory(integration);

        logger.info("Copying Integration ihub_parameters.json file to working directory");
        copyIntegrationParamsToIntegrationWorkingDirectory(integration);

        logger.info("Granting privs on integration files for [{}] Integration User", integration.getIntegrationName());
        osCommands.grantPrivsOnDirectory(integration.getWorkingDirectory().getFileName().toString(),
                integration.getOsUserName());

        if (isDirectoryEmpty(integration.getWorkingDirectory())) {
            throw new IntegrationRunPreparationException("Can't run Integration without files");
        }
    }

    private void createWorkingDirectory(RunningIntegration integration) {
        if (Files.notExists(integration.getWorkingDirectory())) {
            try {
                Files.createDirectory(integration.getWorkingDirectory());
            } catch (IOException e) {
                throw new IntegrationRunPreparationException("Can't create Integration working directory", e);
            }
        }
    }

    private void copyProcessIdFileToIntegrationWorkingDirectory(RunningIntegration integration) {
        try (InputStream dataStream = IOUtils.toInputStream(integration.getProcessId().toString())) {
            FileUtils.copyInputStreamToFile(dataStream, integration.getProcessIdFilePath());
        } catch (IOException e) {
            throw new IntegrationRunPreparationException("Can't create ihub_process_id file", e);
        }
    }

    private void copyIntegrationParamsToIntegrationWorkingDirectory(RunningIntegration integration) {
        IntegrationParametersJson integrationParametersJson = new IntegrationParametersJson(integration, ovUrl);

        try (InputStream dataStream = IOUtils.toInputStream(mapper.writeValueAsString(integrationParametersJson))) {
            FileUtils.copyInputStreamToFile(dataStream, integration.getParametersFilePath());
        } catch (IOException e) {
            throw new IntegrationRunPreparationException("Can't create ihub_parameters.json file", e);
        }
    }

    private void copySettingsFileToWorkingDirectory(RunningIntegration integration) {
        try (InputStream dataStream = integrationDao.readSettingsFile(integration.getIntegrationId())) {
            if (dataStream != null) {
                Optional<String> settingsFileName = getSettingsFileNameFromIntegrationPropertiesFile(integration);
                if (settingsFileName.isPresent()) {
                    FileUtils.copyInputStreamToFile(dataStream,
                            new File(integration.getWorkingDirectory().toFile().getAbsolutePath() + File.separator
                                    + settingsFileName.get()));
                } else {
                    FileUtils.copyInputStreamToFile(dataStream, integration.getSettingsFilePath());
                }
            }
        } catch (IOException e) {
            throw new IntegrationRunPreparationException("Can't load integration settings file", e);
        }
    }

    private static boolean isDirectoryEmpty(Path directory) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            return !directoryStream.iterator().hasNext();
        } catch (IOException e) {
            throw new IntegrationRunPreparationException("Can't access integration working directory", e);
        }
    }

    private Optional<String> getSettingsFileNameFromIntegrationPropertiesFile(RunningIntegration integration) {
        Optional<String> optionalSettingsFileName = Optional.empty();
        try (InputStream integrationPropertiesFile = FileUtils.openInputStream(integration.getIntegrationPropertiesFilePath())) {
            Properties properties = new Properties();
            try {
                properties.load(integrationPropertiesFile);
                optionalSettingsFileName = Optional.ofNullable(properties.getProperty(RunningIntegration.SETTINGS_FILE_NAME_PROPERTY));
            } catch (IOException | IllegalArgumentException e) {
                logger.warn("Could not load properties from .integration file", e);
                integrationRunStatusHandler.createWarningLog(integration, "Could not load properties from .integration file",
                        ExceptionUtils.getStackTrace(e));
            }
        } catch (IOException e) {
            logger.warn(".integration file not found ", e);
            integrationRunStatusHandler.createWarningLog(integration, ".integration file not found ",
                    ExceptionUtils.getStackTrace(e));
        }
        return optionalSettingsFileName;
    }
}
