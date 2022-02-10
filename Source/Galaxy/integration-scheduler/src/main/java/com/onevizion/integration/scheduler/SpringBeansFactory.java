/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import com.onevizion.integration.scheduler.os.LinuxDependentCommands;
import com.onevizion.integration.scheduler.os.OsCommands;
import com.onevizion.integration.scheduler.os.TestingEnvironmentCommands;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;

@Configuration
public class SpringBeansFactory {

    @Bean
    @Scope("prototype")
    public IntegrationExecutorTask createIntegrationExecutorTask(RunningIntegration integration) {
        return new IntegrationExecutorTask(integration);
    }

    @Bean
    public OsCommands createOsCommands() {
        if (IS_OS_LINUX) {
            return new LinuxDependentCommands();
        } else {
            return new TestingEnvironmentCommands();
        }
    }
}
