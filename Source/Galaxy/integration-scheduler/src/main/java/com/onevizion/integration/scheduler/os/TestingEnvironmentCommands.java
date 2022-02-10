/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler.os;

import com.onevizion.integration.scheduler.RunningIntegration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;

import java.util.List;

public class TestingEnvironmentCommands implements OsCommands {
    private static final String NOT_SUPPORTED_OS_WARNING = "Current OS doesn't supported by integration scheduler. Use it only for development and testing purposes.";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void createUser(String directoryName, String userName) {
        logger.warn(NOT_SUPPORTED_OS_WARNING);
    }

    @Override
    public void deleteUser(String directoryName, String userName) {
        logger.warn(NOT_SUPPORTED_OS_WARNING);
    }

    @Override
    public void deleteUnnecessaryUsers(List<RunningIntegration> integrations) {
        logger.warn(NOT_SUPPORTED_OS_WARNING);
    }

    @Override
    public void putReadmeFile() {
        logger.warn(NOT_SUPPORTED_OS_WARNING);
    }

    @Override
    public void grantPrivsOnDirectory(String directoryName, String userName) {
        logger.warn(NOT_SUPPORTED_OS_WARNING);

    }

    @Override
    public void grantPrivsOnDirectoryToCurrentUser(String directoryName) {
        logger.warn(NOT_SUPPORTED_OS_WARNING);
    }

    @Override
    public ProcessExecutor configureExecutorToRunUnderUser(String command, String userName) {
        logger.warn(NOT_SUPPORTED_OS_WARNING);
        return new ProcessExecutor().commandSplit(command);
    }

    @Override
    public void prepareForOsCommandsExecution() {
        logger.warn(NOT_SUPPORTED_OS_WARNING);
    }

}
