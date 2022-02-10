/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler.concurrent.cron;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.*;

public class CronScheduledThreadPoolExecutor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ScheduledExecutorService scheduledExecutor;

    public CronScheduledThreadPoolExecutor(int corePoolSize) {
        scheduledExecutor = Executors.newScheduledThreadPool(corePoolSize);
    }

    public CronScheduledFeature<?> schedule(Runnable task, String cronExpression) {
        Objects.requireNonNull(task);
        Assert.hasText(cronExpression, () -> "cronExpression can't be empty");

        CronExpression expression;
        try {
            expression = new CronExpression(cronExpression);
        } catch (ParseException e) {
            throw new IntegrationSchedulerConcurrentException("[{}] is invalid cron expression", e, cronExpression);
        }

        CronScheduledFeature<Void> feature = new CronScheduledFeature<>();
        Runnable scheduledTask = new SchedulingTask(expression, task, feature);
        feature.setScheduledFeature(scheduledExecutor.submit(scheduledTask));

        return feature;
    }

    private class SchedulingTask implements Runnable {
        private final CronExpression cronExpression;
        private final Runnable task;
        private CronScheduledFeature<Void> feature;

        private SchedulingTask(CronExpression cronExpression, Runnable task, CronScheduledFeature<Void> feature) {
            this.cronExpression = cronExpression;
            this.task = task;
            this.feature = feature;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                Date now = new Date();
                Date time = cronExpression.getNextValidTimeAfter(now);
                while (time != null) {
                    ScheduledFuture<?> reallyExecutedFeature;

                    long delayMillis = time.getTime() - now.getTime();
                    reallyExecutedFeature = scheduledExecutor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
                    feature.setReallyExecutedFeature((ScheduledFuture<Void>) reallyExecutedFeature);

                    reallyExecutedFeature.get();

                    now = new Date();
                    time = cronExpression.getNextValidTimeAfter(now);
                }

            } catch (RejectedExecutionException e) {
                //TODO: correctly handle exceptions and send to email
                logger.error("Executor was already shutdown when schedule() is called", e);
            } catch (CancellationException e) {
                logger.error("Scheduled, but not yet executed tasks are canceled during shutdown", e);
            } catch (InterruptedException e) {
                logger.error("Executing tasks are interrupted during shutdownNow()", e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                logger.error("Executing tasks exception occurs", e);
            }
        }
    }
}
