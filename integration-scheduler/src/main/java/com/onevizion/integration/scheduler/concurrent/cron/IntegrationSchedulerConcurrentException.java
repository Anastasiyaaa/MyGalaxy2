/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler.concurrent.cron;

import com.onevizion.exception.UnexpectedException;

public class IntegrationSchedulerConcurrentException extends UnexpectedException {

    public IntegrationSchedulerConcurrentException() { }

    public IntegrationSchedulerConcurrentException(String message, Object... params) {
        super(message, params);
    }

    public IntegrationSchedulerConcurrentException(String message, Throwable cause, Object... params) {
        super(message, cause, params);
    }

    public IntegrationSchedulerConcurrentException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegrationSchedulerConcurrentException(String message) {
        super(message);
    }

    public IntegrationSchedulerConcurrentException(Throwable cause) {
        super(cause);
    }
}
