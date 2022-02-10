/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import com.onevizion.vo.integration.IntegrationLogLevel;

public class IntegrationParametersJson {

    private Long integrationId;
    private String integrationName;
    private boolean readFromStdout;
    private IntegrationLogLevel logLevel;
    private String ovUrl;
    private Long processId;

    public IntegrationParametersJson(RunningIntegration runningIntegration, String ovUrl) {
        this.integrationId = runningIntegration.getIntegrationId();
        this.integrationName = runningIntegration.getIntegrationName();
        this.readFromStdout = runningIntegration.isReadFromStdout();
        this.logLevel = runningIntegration.getLogLevel();
        this.ovUrl = ovUrl;
        this.processId = runningIntegration.getProcessId();
    }

    public Long getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(Long integrationId) {
        this.integrationId = integrationId;
    }

    public String getIntegrationName() {
        return integrationName;
    }

    public void setIntegrationName(String integrationName) {
        this.integrationName = integrationName;
    }

    public boolean isReadFromStdout() {
        return readFromStdout;
    }

    public void setReadFromStdout(boolean readFromStdout) {
        this.readFromStdout = readFromStdout;
    }

    public IntegrationLogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(IntegrationLogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getOvUrl() {
        return ovUrl;
    }

    public void setOvUrl(String ovUrl) {
        this.ovUrl = ovUrl;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }
}
