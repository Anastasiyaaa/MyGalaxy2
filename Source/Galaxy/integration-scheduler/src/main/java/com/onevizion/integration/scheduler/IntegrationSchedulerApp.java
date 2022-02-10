/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import com.onevizion.BootstrapDbApp;
import com.onevizion.integration.scheduler.os.OsCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class IntegrationSchedulerApp extends BootstrapDbApp {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationSchedulerApp.class);
    private static final int EXIT_CODE_ERROR = 1;

    private static final String BEANS_XML_PATH = "com/onevizion/integration/scheduler/beans.xml";

    public static void main(String[] args) {
        try {
            IntegrationSchedulerApp app = new IntegrationSchedulerApp();

            ApplicationContext ctx = app.getAppContext(BEANS_XML_PATH, args);

            OsCommands osCommands = ctx.getBean(OsCommands.class);
            osCommands.prepareForOsCommandsExecution();

            IntegrationScheduler scheduler = ctx.getBean(IntegrationScheduler.class);
            scheduler.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            System.exit(EXIT_CODE_ERROR);
        }
    }
}
