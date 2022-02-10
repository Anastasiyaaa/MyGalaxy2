/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler.exception;

import com.onevizion.exception.UnexpectedException;

public class IntegrationRunPreparationException extends UnexpectedException {
    public IntegrationRunPreparationException() { }

    public IntegrationRunPreparationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegrationRunPreparationException(String message) {
        super(message);
    }

    public IntegrationRunPreparationException(Throwable cause) {
        super(cause);
    }

    public IntegrationRunPreparationException(String message, Throwable cause, Object... params) {
        super(message, cause, params);
    }

    public IntegrationRunPreparationException(String message, Object... params) {
        super(message, params);
    }
}
