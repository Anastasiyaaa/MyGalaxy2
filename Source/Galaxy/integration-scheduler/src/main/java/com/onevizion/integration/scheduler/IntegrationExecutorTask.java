/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import com.onevizion.dao.pkg.PkgRuleatorDao;
import com.onevizion.facade.integration.scheduler.IntegrationSchedulerFacade;
import com.onevizion.integration.scheduler.os.OsCommands;
import com.onevizion.vo.rule.RuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.StringJoiner;
import java.util.concurrent.TimeoutException;

public class IntegrationExecutorTask implements Runnable {

    @Autowired
    private IntegrationRunStatusHandler integrationRunStatusHandler;

    @Autowired
    private IntegrationRunHelper integrationRunHelper;

    @Autowired
    private OsCommands osCommands;

    @Autowired
    private ConnectionProperties connectionProperties;

    @Autowired
    private IntegrationSchedulerFacade integrationSchedulerFacade;

    @Autowired
    private PkgRuleatorDao pkgRuleatorDao;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RunningIntegration runningIntegration;

    public IntegrationExecutorTask(RunningIntegration integration) {
        runningIntegration = integration;
    }

    public void run() {
        connectionProperties.setProgramId(runningIntegration.getProgramId());
        integrationRunStatusHandler.onIntegrationStart(runningIntegration);

        try {
            pkgRuleatorDao.executeTrigger(RuleType.BEFORE_INTEGRATION_START.getId(), runningIntegration.getIntegrationId(),
                    runningIntegration.getIntegrationRunId(), null, null, null);
            executeIntegration(runningIntegration);
        } catch (Exception e) {
            integrationRunStatusHandler.onIntegrationError(runningIntegration, e);
        }

        try {
            integrationSchedulerFacade.updateNextRun(runningIntegration);
        } catch (Exception e) {
            logger.error("Cannot update Next Run", e);
        }

        try {
            pkgRuleatorDao.executeTrigger(RuleType.AFTER_INTEGRATION_COMPLETE.getId(), runningIntegration.getIntegrationId(),
                    runningIntegration.getIntegrationRunId(), null, null, null);
        } catch (Exception e) {
            logger.error("Rule execution failed", e);
        }

    }

    private void executeIntegration(RunningIntegration runningIntegration) throws IOException {
        integrationRunHelper.prepareForIntegrationRun(runningIntegration);

        ProcessExecutor processExecutor = osCommands.configureExecutorToRunUnderUser(runningIntegration.getCommand(),
                runningIntegration.getOsUserName());
        StringJoiner errorMessage = new StringJoiner("\n");
        processExecutor.directory(runningIntegration.getWorkingDirectory().toFile())
                       .destroyOnExit()
                       .redirectOutput(new LogOutputStream() {
                           @Override
                           protected void processLine(String line) {
                               integrationRunStatusHandler.createLogsWithStdout(runningIntegration, line);
                           }
                       })
                       .redirectError(new LogOutputStream() {
                           @Override
                           protected void processLine(String line) {
                               errorMessage.add(line);
                               integrationRunStatusHandler.createLogsWithStderr(runningIntegration, line);
                           }
                       })
                       .exitValue(0);

        try {
            logger.info("Executing [{}] integration", runningIntegration.getIntegrationName());
            Instant start = Instant.now();
            processExecutor.execute();
            Long runtime = Duration.between(start, Instant.now()).toMillis() / 1000L;
            logger.info("Integration [{}] executed successfully", runningIntegration.getIntegrationName());
            integrationRunStatusHandler.onIntegrationFinish(runningIntegration.getProcessId(), runtime);
        } catch (TimeoutException | InvalidExitValueException e) {
            logger.error("Integration [{}] executed with errors", e, runningIntegration.getIntegrationName());
            integrationRunStatusHandler.onIntegrationError(runningIntegration, errorMessage.toString(), errorMessage.toString());
        } catch (InterruptedException e) {
            logger.error("Integration [{}] executed with errors", e, runningIntegration.getIntegrationName());
            Thread.currentThread().interrupt();
            integrationRunStatusHandler.onIntegrationError(runningIntegration, errorMessage.toString(), errorMessage.toString());
        }
    }
}

