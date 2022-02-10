/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler.os;

import com.google.common.collect.Lists;
import com.onevizion.integration.scheduler.RunningIntegration;
import com.onevizion.integration.scheduler.exception.IntegrationRunPreparationException;
import com.onevizion.integration.scheduler.exception.ScriptExecutionException;
import com.onevizion.vo.integration.IntegrationVo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.onevizion.TextFileStaticPopulator.from;

public class LinuxDependentCommands implements OsCommands {
    private final static String COMMANDS_EXECUTOR = "CommandsExecutor.sh";

    private static final String CREATE_USER = "CreateUser.sh";
    private static final String GRANT_PRIVS = "GrantPrivs.sh";
    private static final String GRANT_PRIVS_TO_CURRENT_USER = "GrantPrivsToOwner.sh";
    private static final String DELETE_USER = "DeleteUser.sh";

    private static final String README = from("com/onevizion/integration/scheduler/Readme.txt");

    private static final String README_FILE_NAME = "Readme.txt";

    private static final List<String> SCRIPTS_TO_COPY = Arrays.asList(CREATE_USER, GRANT_PRIVS,
            GRANT_PRIVS_TO_CURRENT_USER, DELETE_USER, COMMANDS_EXECUTOR);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void createUser(String directoryName, String userName) {
        logger.info("Check existing user login for [{}] Integration", userName);

        try {
            executeScriptFile(getScriptFile(CREATE_USER), directoryName, userName);
        } catch (ScriptExecutionException e) {
            throw new IntegrationRunPreparationException("Can't create user [{}]", e, userName);
        }
    }

    @Override
    public void deleteUser(String directoryName, String userName) {
        try {
            executeScriptFile(getScriptFile(DELETE_USER), directoryName, userName);
        } catch (ScriptExecutionException e) {
            throw new IntegrationRunPreparationException("Can't delete user [{}]", e, userName);
        }
    }

    @Override
    public void deleteUnnecessaryUsers(List<RunningIntegration> integrations) {

        File[] files = Paths.get(".").toAbsolutePath().toFile().listFiles();
        if (files != null && files.length > 0) {

            List<RunningIntegration> deletedIntegrations = Lists.newArrayList(files)
                    .stream()
                    .filter(File::isDirectory)
                    .map(File::getName)
                    .filter(name -> name.matches("\\d+"))
                    .map(name -> {
                        IntegrationVo integrationVo = new IntegrationVo();
                        integrationVo.setIntegrationId(Long.valueOf(name));
                        return new RunningIntegration(integrationVo);
                    })
                    .collect(Collectors.toList());

            deletedIntegrations.removeAll(integrations);

            for (RunningIntegration runningIntegration : deletedIntegrations) {
                deleteUser(runningIntegration.getWorkingDirectory().getFileName().toString(), runningIntegration.getOsUserName());
            }
        }
    }

    @Override
    public void putReadmeFile() {
        File readmeFile = new File(Paths.get(".").toFile().getAbsolutePath() + File.separator + README_FILE_NAME);
        try (InputStream dataStream = IOUtils.toInputStream(README)) {
            FileUtils.copyInputStreamToFile(dataStream, readmeFile);
        } catch (IOException e) {
            throw new IntegrationRunPreparationException("Can't create readme file", e);
        }
    }

    @Override
    public void grantPrivsOnDirectory(String directoryName, String userName) {
        try {
            executeScriptFile(getScriptFile(GRANT_PRIVS), directoryName, userName);
        } catch (ScriptExecutionException e) {
            throw new IntegrationRunPreparationException("Can't grant privs to [{}]", e, userName);
        }
    }

    @Override
    public void grantPrivsOnDirectoryToCurrentUser(String directoryName) {
        try {
            executeScriptFile(getScriptFile(GRANT_PRIVS_TO_CURRENT_USER), directoryName);
        } catch (ScriptExecutionException e) {
            throw new IntegrationRunPreparationException("Can't grant privs on [{}] to current user", e, directoryName);
        }
    }

    @Override
    public ProcessExecutor configureExecutorToRunUnderUser(String command, String user) {
        List<String> commandParts = new ArrayList<>();
        commandParts.add(getScriptFile(COMMANDS_EXECUTOR).getAbsolutePath());
        commandParts.add( user);
        commandParts.addAll(Arrays.asList( command.split(" ")));
        return new ProcessExecutor(commandParts);
    }

    @Override
    public void prepareForOsCommandsExecution() {
        File scriptDirectory = new File(SCRIPTS_FOLDER);
        if (scriptDirectory.exists()) {
            try {
                FileUtils.deleteDirectory(scriptDirectory);
            } catch (IOException e) {
                throw new IntegrationRunPreparationException("Can't clear scripts directory", e);
            }
        }
        try {
            if (!scriptDirectory.mkdir()) {
                throw new IntegrationRunPreparationException("Can't create scripts directory");
            }
        } catch (SecurityException e) {
            throw new IntegrationRunPreparationException("Can't create scripts directory", e);
        }

        for (String script : SCRIPTS_TO_COPY) {
            File file = new File(SCRIPTS_FOLDER + File.separator + script);
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(script);

            try {
                FileUtils.copyInputStreamToFile(inputStream, file);
            } catch (IOException e) {
                throw new IntegrationRunPreparationException("Can't copy script [{}] to directory", e, script);
            }

            if (!file.setExecutable(true, true)) {
                throw new IntegrationRunPreparationException("Can't set execute permission to [{}] script file",
                        script);
            }
        }

    }

    private File getScriptFile(String scriptFileName) {
        File file = new File(SCRIPTS_FOLDER + File.separator + scriptFileName);
        if (!file.exists()) {
            throw new IntegrationRunPreparationException("Script file [{}] is missed", scriptFileName);
        }
        return file;
    }

    private void executeScriptFile(File file, String... params) {
        ArrayList<String> command = new ArrayList<>();
        command.add(file.getAbsolutePath());
        command.addAll(Arrays.asList(params));

        try {
            new ProcessExecutor(command).readOutput(true).exitValue(0).execute();
        } catch (IOException | TimeoutException e) {
            logger.error("Can't execute [{}] script file", e, file.getAbsolutePath());
            throw new ScriptExecutionException(e);
        } catch (InterruptedException e) {
            logger.error("Can't execute [{}] script file", e, file.getAbsolutePath());
            Thread.currentThread().interrupt();
            throw new ScriptExecutionException(e);
        } catch (InvalidExitValueException e) {
            throw new ScriptExecutionException(e.getResult().outputUTF8(), e);
        }
    }
}
