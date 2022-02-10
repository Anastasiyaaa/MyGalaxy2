/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler;

import com.onevizion.dao.integration.IntegrationDao;
import com.onevizion.facade.integration.scheduler.IntegrationRunFacade;
import com.onevizion.facade.integration.scheduler.IntegrationSchedulerFacade;
import com.onevizion.integration.scheduler.concurrent.cron.CronScheduledFeature;
import com.onevizion.integration.scheduler.concurrent.cron.CronScheduledThreadPoolExecutor;
import com.onevizion.integration.scheduler.concurrent.cron.IntegrationSchedulerConcurrentException;
import com.onevizion.integration.scheduler.os.OsCommands;
import com.onevizion.vo.integration.Integration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class IntegrationScheduler {

    @Autowired
    private IntegrationDao integrationDao;

    @Autowired
    private IntegrationRunFacade integrationRunFacade;

    @Autowired
    private SpringBeansFactory springBeansFactory;

    @Autowired
    private IntegrationRunStatusHandler integrationRunStatusHandler;

    @Autowired
    private OsCommands osCommands;

    @Autowired
    private ConnectionProperties connectionProperties;

    @Autowired
    private IntegrationSchedulerFacade integrationSchedulerFacade;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CronScheduledThreadPoolExecutor cronScheduledExecutor = new CronScheduledThreadPoolExecutor(30);
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    private final Map<RunningIntegration, CronScheduledFeature<?>> integrationFeatures = new HashMap<>();

    public void start() {
        logger.info("integration-scheduler is started");
        connectionProperties.setProgramId(0L);
        osCommands.putReadmeFile();

        List<RunningIntegration> integrations = integrationDao.readAll()
                                                              .stream()
                                                              .map(RunningIntegration::new)
                                                              .collect(Collectors.toList());
        osCommands.deleteUnnecessaryUsers(integrations);
        scheduledExecutor.scheduleAtFixedRate(this::updateIntegrations, 0, 2, TimeUnit.MINUTES);
    }

    private void updateIntegrations() {
        try {
            updateIntegrationsInternal();
        } catch (Exception e) {
            logger.error("Error during running integrations update", e);
        }
    }

    private void updateIntegrationsInternal() {
        logger.info("updating list of integrations");

        connectionProperties.setProgramId(0L);

        Set<RunningIntegration> integrations = integrationDao.readAll()
                                                             .stream()
                                                             .map(RunningIntegration::new)
                                                             .collect(Collectors.toSet());
        logger.info("[{}] integrations are enabled", integrations.stream()
                                                                 .filter(RunningIntegration::isEnabled)
                                                                 .count());

        Set<RunningIntegration> integrationsToInterrupt = integrationDao.findIntegrationForInterrupting()
                                                                        .stream()
                                                                        .map(RunningIntegration::new)
                                                                        .collect(Collectors.toSet());

        Set<RunningIntegration> integrationsToSchedule = integrations.stream()
                                                                     .filter(isScheduledIntegration())
                                                                     .collect(Collectors.toSet());
        Set<RunningIntegration> integrationsToStop = integrations.stream()
                                                                 .filter(isStoppedIntegration())
                                                                 .collect(Collectors.toSet());
        Set<RunningIntegration> integrationsToReSchedule = integrations.stream()
                                                                       .filter(isRescheduledIntegration())
                                                                       .collect(Collectors.toSet());
        Set<RunningIntegration> integrationsToChange = integrations.stream()
                                                                   .filter(isChangedIntegration())
                                                                   .collect(Collectors.toSet());
        Set<RunningIntegration> integrationsToDelete = new HashSet<>(integrationFeatures.keySet());
        integrationsToDelete.removeAll(integrations);
        integrationsToStop.removeAll(integrationsToInterrupt);

        integrationsToStop.addAll(integrationsToReSchedule);
        integrationsToSchedule.addAll(integrationsToReSchedule);

        interrupt(integrationsToInterrupt, true);
        interrupt(integrationsToDelete, false);
        stop(integrationsToStop);
        schedule(integrationsToSchedule);
        change(integrationFeatures.keySet(), integrationsToChange);
        deleteUsers(integrationsToDelete);
    }

    private void schedule(Set<RunningIntegration> integrations) {
        for (RunningIntegration integration : integrations) {
            IntegrationExecutorTask integrationTask = springBeansFactory.createIntegrationExecutorTask(integration);
            try {
                CronScheduledFeature<?> feature = cronScheduledExecutor.schedule(integrationTask, integration.getSchedule());
                integrationFeatures.put(integration, feature);
                logger.info("[{}] Integration successfully scheduled", integration.getIntegrationName());
            } catch (IntegrationSchedulerConcurrentException | IllegalArgumentException | NullPointerException e) {
                integrationRunStatusHandler.onIntegrationSchedulingError(integration, e);
            }

            try {
                integrationSchedulerFacade.updateNextRun(integration);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void stop(Set<RunningIntegration> integrations) {
        for (RunningIntegration integration : integrations) {
            integrationFeatures.computeIfPresent(integration, (i, f) -> {
                f.cancel(false);
                return null;
            });
            logger.debug("[{}] Integration has stopped", integration.getIntegrationName());
        }
    }

    private void interrupt(Set<RunningIntegration> integrations, boolean disableIntegration) {
        for (RunningIntegration integration : integrations) {
            integrationFeatures.computeIfPresent(integration, (i, f) -> {
                f.cancel(true);
                return null;
            });

            if (disableIntegration) {
                integrationRunFacade.interruptInterruptingIntegrationRun(integration.getIntegrationId());
            }
            logger.debug("[{}] Integration has been interrupted", integration.getIntegrationName());
        }
    }

    private void change(Set<RunningIntegration> sIntegrations, Set<RunningIntegration> integrations) {
        for (Integration integration : integrations) {
            sIntegrations.forEach(si -> {
                if (si.equals(integration)) {
                    si.copyIntegration(integration);
                }
            });
            logger.debug("[{}] Integration has been changed", integration.getIntegrationName());
        }
    }

    private void deleteUsers(Set<RunningIntegration> integrations) {
        for (RunningIntegration integration : integrations) {
            osCommands.deleteUser(integration.getWorkingDirectory().getFileName().toString(), integration.getOsUserName());
        }
    }

    private Predicate<Integration> isScheduledIntegration() {
        return integration -> {
            for (Integration sIntegration : integrationFeatures.keySet()) {
                if (integration.equals(sIntegration)) {
                    return false;
                }
            }

            return integration.isEnabled();
        };
    }

    private Predicate<Integration> isStoppedIntegration() {
        return integration -> {
            if (!integration.isEnabled()) {
                for (Integration sIntegration : integrationFeatures.keySet()) {
                    if (integration.equals(sIntegration)) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    private Predicate<Integration> isRescheduledIntegration() {
        return integration -> {
            if (integration.isEnabled()) {
                for (Integration sIntegration : integrationFeatures.keySet()) {
                    if (integration.equals(sIntegration) && !sIntegration.getSchedule()
                                                                         .equals(integration.getSchedule())) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    private Predicate<RunningIntegration> isChangedIntegration() {
        return integration -> {
            if (integration.isEnabled()) {
                for (Integration sIntegration : integrationFeatures.keySet()) {
                    if (integration.equals(sIntegration) && integration.isChangedIntegration(sIntegration)) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

}
