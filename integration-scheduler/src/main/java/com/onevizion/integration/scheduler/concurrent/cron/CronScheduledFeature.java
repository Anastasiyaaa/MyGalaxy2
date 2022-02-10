/*
 * Copyright 2003-2020 OneVizion, Inc. All rights reserved.
 */

package com.onevizion.integration.scheduler.concurrent.cron;

import java.util.concurrent.*;

public class CronScheduledFeature<V> implements ScheduledFuture<V> {
    private ScheduledFuture<V> reallyExecutedFeature;
    private Future<?> scheduledFeature;

    @Override
    public long getDelay(TimeUnit unit) {
        return reallyExecutedFeature.getDelay(unit);
    }

    @Override
    public int compareTo(Delayed delayed) {
        return reallyExecutedFeature.compareTo(delayed);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        scheduledFeature.cancel(true);
        return reallyExecutedFeature.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return reallyExecutedFeature.isCancelled();
    }

    @Override
    public boolean isDone() {
        return reallyExecutedFeature.isCancelled();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return reallyExecutedFeature.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return reallyExecutedFeature.get(timeout, unit);
    }

    void setReallyExecutedFeature(ScheduledFuture<V> reallyExecutedFeature) {
        this.reallyExecutedFeature = reallyExecutedFeature;
    }

    ScheduledFuture<V> getReallyExecutedFeature() {
        return reallyExecutedFeature;
    }

    void setScheduledFeature(Future<?> scheduledFeature) {
        this.scheduledFeature = scheduledFeature;
    }
}
