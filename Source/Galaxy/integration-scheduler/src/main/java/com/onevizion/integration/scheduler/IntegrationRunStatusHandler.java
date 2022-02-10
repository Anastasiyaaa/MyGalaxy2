/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import com.onevizion.dao.integration.IntegrationDao;
import com.onevizion.facade.integration.scheduler.IntegrationLogFacade;
import com.onevizion.facade.integration.scheduler.IntegrationRunFacade;
import com.onevizion.vo.integration.Integration;
import com.onevizion.vo.integration.IntegrationRun;
import com.onevizion.vo.process.ProcessStatus;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.onevizion.vo.integration.IntegrationLogLevel.*;

@Component
public class IntegrationRunStatusHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IntegrationRunFacade integrationRunFacade;

    @Autowired
    private IntegrationLogFacade integrationLogFacade;

    @Autowired
    private IntegrationDao integrationDao;

    public void onIntegrationStart(RunningIntegration integration) {
        logger.info("[{}] Checking other Integration Runs before starting a new one", integration.getIntegrationName());
        try {
            integrationRunFacade.interruptRunningIntegrationRuns(integration.getIntegrationId());
        } catch (Exception e) {
            logger.error("Can't interrupt previous Integration Runs", e);
        }

        logger.info("[{}] Integration Run has been started", integration.getIntegrationName());
        try {
            IntegrationRun integrationRun = integrationRunFacade.createIntegrationRun(integration, ProcessStatus.RUNNING);
            integration.setProcessId(integrationRun.getProcessId());
            integration.setIntegrationRunId(integrationRun.getIntegrationRunId());
        } catch (Exception e) {
            logger.error("Can't create Integration Run", e);
        }
    }

    public void onIntegrationFinish(Long processId, Long runtime) {
        integrationRunFacade.updateIntegrationRun(processId, ProcessStatus.EXECUTED_WITHOUT_ERRORS, runtime);
    }

    public void onIntegrationError(RunningIntegration integration, Exception e) {
        onIntegrationError(integration, ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e));
    }

    public void onIntegrationError(RunningIntegration integration, String errorMsg, String fullErrorMsg) {
        try {
            integrationRunFacade.updateIntegrationRunWithError(integration.getProcessId(), errorMsg, fullErrorMsg);
        } catch (Exception e) {
            logger.error("Can't update Integration Run", e);
        }
    }

    public void createLogsWithStdout(RunningIntegration integration, String line) {
        if (integration.isReadFromStdout() && integrationLogFacade.isSupportedLogLevel(integration.getLogLevel(), INFO)) {
            integrationLogFacade.createIntegrationLogsFromStdout(integration.getProcessId(), line);
        }
    }

    public void onIntegrationSchedulingError(Integration integration, Exception schedulingException) {
        logger.error("Can't schedule Integration [{}]", schedulingException, integration.getIntegrationName());
        try {
            integrationRunFacade.createIntegrationRunWithError(integration, ExceptionUtils.getMessage(schedulingException),
                    ExceptionUtils.getStackTrace(schedulingException));
        } catch (Exception e) {
            logger.error("Can't create Integration Run", e);
        }
    }

    public void createLogsWithStderr(RunningIntegration integration, String line) {
        if (integration.isReadFromStdout() && integrationLogFacade.isSupportedLogLevel(integration.getLogLevel(), ERROR)) {
            integrationLogFacade.createIntegrationLogsFromStderr(integration.getProcessId(), line);
        }
    }

    public void createWarningLog(RunningIntegration integration, String msg, String fullMsg) {
        if (integrationLogFacade.isSupportedLogLevel(integration.getLogLevel(), WARNING)) {
            integrationLogFacade.createIntegrationLog(integration.getProcessId(), msg, fullMsg, WARNING);
        }
    }
}
