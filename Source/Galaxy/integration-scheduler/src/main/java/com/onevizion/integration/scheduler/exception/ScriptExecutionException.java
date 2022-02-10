/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler.exception;

import com.onevizion.exception.UnexpectedException;

public class ScriptExecutionException extends UnexpectedException {
    public ScriptExecutionException() { }

    public ScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptExecutionException(String message) {
        super(message);
    }

    public ScriptExecutionException(Throwable cause) {
        super(cause);
    }

    public ScriptExecutionException(String message, Throwable cause, Object... params) {
        super(message, cause, params);
    }

    public ScriptExecutionException(String message, Object... params) {
        super(message, params);
    }
}
