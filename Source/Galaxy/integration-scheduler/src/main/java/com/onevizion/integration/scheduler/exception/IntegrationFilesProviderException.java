/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler.exception;

import com.onevizion.exception.UnexpectedException;

public class IntegrationFilesProviderException extends UnexpectedException {
    public IntegrationFilesProviderException() { }

    public IntegrationFilesProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegrationFilesProviderException(String message) {
        super(message);
    }

    public IntegrationFilesProviderException(Throwable cause) {
        super(cause);
    }

    public IntegrationFilesProviderException(String message, Throwable cause, Object... params) {
        super(message, cause, params);
    }

    public IntegrationFilesProviderException(String message, Object... params) {
        super(message, params);
    }
}
