/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler.os;

import com.onevizion.integration.scheduler.RunningIntegration;
import org.zeroturnaround.exec.ProcessExecutor;

import java.util.List;

public interface OsCommands {
    String SCRIPTS_FOLDER = "scripts";

    void createUser(String directoryName, String userName);

    void deleteUser(String directoryName, String userName);

    void deleteUnnecessaryUsers(List<RunningIntegration> integrations);

    void putReadmeFile();

    void grantPrivsOnDirectory(String directoryName, String userName);

    void grantPrivsOnDirectoryToCurrentUser(String directoryName);

    ProcessExecutor configureExecutorToRunUnderUser(String command, String userName);

    void prepareForOsCommandsExecution();
}
