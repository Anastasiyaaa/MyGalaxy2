/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import com.onevizion.vo.integration.Integration;
import com.onevizion.vo.integration.IntegrationLogLevel;
import com.onevizion.vo.integration.IntegrationVo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;

public class RunningIntegration implements Integration {
    private static final String USER_NAME_PREFIX = "u_";
    private static final String IHUB_PROCESS_ID_FILE_NAME = "ihub_process_id";
    private static final String IHUB_PARAMETERS_FILE_NAME = "ihub_parameters.json";
    private static final String INTEGRATION_PROPERTIES_FILE_NAME = ".integration";
    public static final String SETTINGS_FILE_NAME_PROPERTY = "settings_file_name";

    private final IntegrationVo integration;
    private Long processId;
    private Long integrationRunId;

    public RunningIntegration(IntegrationVo integration) {
        this.integration = integration;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setIntegrationRunId(Long integrationRunId) {
        this.integrationRunId = integrationRunId;
    }

    public Long getIntegrationRunId() {
        return integrationRunId;
    }

    public Path getWorkingDirectory() {
        return Paths.get(integration.getIntegrationId().toString()).toAbsolutePath();
    }

    public File getSettingsFilePath() {
        return new File(getWorkingDirectory().toFile().getAbsolutePath() + File.separator + integration.getSettingsFileName());
    }

    public File getProcessIdFilePath() {
        return new File(getWorkingDirectory().toFile().getAbsolutePath() + File.separator + IHUB_PROCESS_ID_FILE_NAME);
    }

    public File getParametersFilePath() {
        return new File(getWorkingDirectory().toFile().getAbsolutePath() + File.separator + IHUB_PARAMETERS_FILE_NAME);
    }

    public File getIntegrationPropertiesFilePath() {
        return new File(getWorkingDirectory().toFile().getAbsolutePath() + File.separator + INTEGRATION_PROPERTIES_FILE_NAME);
    }

    public String getOsUserName() {
        return USER_NAME_PREFIX + integration.getIntegrationId();
    }

    @Override
    public IntegrationLogLevel getLogLevel() {
        return integration.getLogLevel();
    }

    @Override
    public Long getIntegrationId() {
        return integration.getIntegrationId();
    }

    @Override
    public String getIntegrationName() {
        return integration.getIntegrationName();
    }

    @Override
    public boolean isEnabled() {
        return integration.isEnabled();
    }

    @Override
    public Date getLastRun() {
        return integration.getLastRun();
    }

    @Override
    public String getDescription() {
        return integration.getDescription();
    }

    @Override
    public boolean isReadFromStdout() {
        return integration.isReadFromStdout();
    }

    @Override
    public String getVersion() {
        return integration.getVersion();
    }

    @Override
    public String getSchedule() {
        return integration.getSchedule();
    }

    @Override
    public String getGitRepoUrl() {
        return integration.getGitRepoUrl();
    }

    @Override
    public Long getProgramId() {
        return integration.getProgramId();
    }

    @Override
    public String getSettingsFileName() {
        return integration.getSettingsFileName();
    }

    @Override
    public String getCommand() {
        return integration.getCommand();
    }

    @Override
    public String getGitRepoLogin() {
        return integration.getGitRepoLogin();
    }

    @Override
    public String getGitRepoPassword() {
        return integration.getGitRepoPassword();
    }

    @Override
    public Date getNextRun(){
        return integration.getNextRun();
    }

    @Override
    public Long getBplCompLogId() {
        return integration.getBplCompLogId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunningIntegration that = (RunningIntegration) o;
        return Objects.equals(integration, that.integration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(integration.getIntegrationId());
    }

    public void copyIntegration(Integration integration) {
        this.integration.copyIntegration(integration);
    }

    public boolean isChangedIntegration(Integration integration) {
        return this.integration.isChangedIntegration(integration);
    }
}
