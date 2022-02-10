/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import org.springframework.stereotype.Component;

@Component
public class ConnectionProperties {
    private final ThreadLocal<Long> threadLocalProgramId = new ThreadLocal<>();

    public Long getProgramId() {
        return threadLocalProgramId.get();
    }

    public void setProgramId(Long programId) {
        threadLocalProgramId.set(programId);
    }
}
