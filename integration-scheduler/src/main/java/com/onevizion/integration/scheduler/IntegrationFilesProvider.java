/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import com.onevizion.integration.scheduler.exception.IntegrationFilesProviderException;
import com.onevizion.vo.integration.Integration;

import java.nio.file.Path;

public interface IntegrationFilesProvider {

    void loadIntegrationFiles(Integration integration, Path directory) throws IntegrationFilesProviderException;

    void updateIntegrationFiles(Integration integration, Path directory) throws IntegrationFilesProviderException;

    void cleanIntegrationDirectory(Path directory) throws IntegrationFilesProviderException;
}
