/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.mbean;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Concrete implementation of {@link BrokerMetricsMBean}.
 */
public class BrokerMetrics implements BrokerMetricsMBean {

    /**
     * Count of total number of requests.
     */
    private AtomicLong requestCount = new AtomicLong();

    /**
     * Count of total number of failed requests.
     */
    private AtomicLong failedRequestCount = new AtomicLong();

    /**
     * Count of total number of provision requests.
     */
    private AtomicLong provisionRequestcount = new AtomicLong();

    /**
     * Count of total number of failed provision requests.
     */
    private AtomicLong failedProvisionRequestcount = new AtomicLong();

    /**
     * Count of total number of binding requests.
     */
    private AtomicLong bindingRequestcount = new AtomicLong();

    /**
     * Count of total number of failed binding requests.
     */
    private AtomicLong failedBindingRequestcount = new AtomicLong();

    /**
     * Count of total number of deprovision requests.
     */
    private AtomicLong deprovisionRequestcount = new AtomicLong();

    /**
     * Count of total number of failed deprovision requests.
     */
    private AtomicLong failedDeprovisionRequestcount = new AtomicLong();

    /**
     * Count of total number of unbind requests.
     */
    private AtomicLong unbindRequestcount = new AtomicLong();

    /**
     * Count of total number of failed unbind requests.
     */
    private AtomicLong failedUnbindRequestcount = new AtomicLong();

    /**
     * Count of total number of last operation(instance level) requests.
     */
    private AtomicLong lastOperationInstanceRequestcount = new AtomicLong();

    /**
     * Count of total number of failed last operation(instance level) requests.
     */
    private AtomicLong failedlastOperationInstanceRequestcount = new AtomicLong();

    /**
     * Count of total number of update requests.
     */
    private AtomicLong updateRequestcount = new AtomicLong();

    /**
     * Count of total number of failed update requests.
     */
    private AtomicLong failedUpdateRequestcount = new AtomicLong();

    /**
     * Count of total number of GetInstance requests.
     */
    private AtomicLong getInstanceRequestcount = new AtomicLong();

    /**
     * Count of total number of failed GetInstance requests.
     */
    private AtomicLong failedGetInstanceRequestcount = new AtomicLong();

    /**
     * Count of total number of GetBinding requests.
     */
    private AtomicLong getBindingRequestcount = new AtomicLong();

    /**
     * Count of total number of failed GetBinding requests.
     */
    private AtomicLong failedGetBindingRequestcount = new AtomicLong();

    /**
     * Count of total number of last operation(binding level) requests.
     */
    private AtomicLong lastOperationBindingRequestcount = new AtomicLong();

    /**
     * Count of total number of failed last operation(binding level) requests.
     */
    private AtomicLong failedlastOperationBindingRequestcount = new AtomicLong();

    @Override
    public long getRequestCount() {
        return requestCount.get();
    }

    @Override
    public long getFailedRequestCount() {
        return failedRequestCount.get();
    }

    @Override
    public long getProvisionRequestCount() {
        return provisionRequestcount.get();
    }

    @Override
    public long getFailedProvisionRequestCount() {
        return failedProvisionRequestcount.get();
    }

    @Override
    public long getBindingRequestCount() {
        return bindingRequestcount.get();
    }

    @Override
    public long getFailedBindingRequestCount() {
        return failedBindingRequestcount.get();
    }

    @Override
    public long getDeprovisionRequestCount() {
        return deprovisionRequestcount.get();
    }

    @Override
    public long getFailedDeprovisionRequestCount() {
        return failedDeprovisionRequestcount.get();
    }

    @Override
    public long getUnbindRequestCount() {
        return unbindRequestcount.get();
    }

    @Override
    public long getFailedUnbindRequestCount() {
        return failedUnbindRequestcount.get();
    }

    @Override
    public long getLastOperationInstanceRequestCount() {
        return lastOperationInstanceRequestcount.get();
    }

    @Override
    public long getFailedLastOperationInstanceRequestCount() {
        return failedlastOperationInstanceRequestcount.get();
    }

    @Override
    public long getLastOperationBindingRequestCount() {
        return lastOperationBindingRequestcount.get();
    }

    @Override
    public long getFailedLastOperationBindingRequestCount() {
        return failedlastOperationBindingRequestcount.get();
    }

    @Override
    public long getUpdateRequestCount() {
        return updateRequestcount.get();
    }

    @Override
    public long getFailedUpdateRequestCount() {
        return failedUpdateRequestcount.get();
    }

    @Override
    public long getGetInstanceRequestCount() {
        return getInstanceRequestcount.get();
    }

    @Override
    public long getFailedGetInstanceRequestCount() {
        return failedGetInstanceRequestcount.get();
    }

    @Override
    public long getGetBindingRequestCount() {
        return getBindingRequestcount.get();
    }

    @Override
    public long getFailedGetBindingRequestCount() {
        return failedGetBindingRequestcount.get();
    }

    public void incrementServiceBindingRequestCount() {
        bindingRequestcount.incrementAndGet();
    }

    public void incrementFailedServiceBindingRequestCount() {
        failedBindingRequestcount.incrementAndGet();
    }

    public void incrementProvisionRequestCount() {
        provisionRequestcount.incrementAndGet();
    }

    public void incrementFailedProvisionRequestCount() {
        failedProvisionRequestcount.incrementAndGet();
    }

    public void incrementUnBindRequestCount() {
        unbindRequestcount.incrementAndGet();
    }

    public void incrementFailedUnBindRequestCount() {
        failedUnbindRequestcount.incrementAndGet();
    }

    public void incrementDeprovisionRequestCount() {
        deprovisionRequestcount.incrementAndGet();
    }

    public void incrementFailedDeprovisionRequestCount() {
        failedDeprovisionRequestcount.incrementAndGet();
    }

    public void incrementUpdateRequestCount() {
        updateRequestcount.incrementAndGet();
    }

    public void incrementFailedUpdateRequestCount() {
        failedUpdateRequestcount.incrementAndGet();
    }

    public void incrementServiceBindingLastOperationCount() {
        lastOperationBindingRequestcount.incrementAndGet();
    }

    public void incrementFailedServiceBindingLastOperationCount() {
        failedlastOperationBindingRequestcount.incrementAndGet();
    }

    public void incrementServiceLastOperationCount() {
        lastOperationInstanceRequestcount.incrementAndGet();
    }

    public void incrementFailedServiceLastOperationCount() {
        failedlastOperationInstanceRequestcount.incrementAndGet();
    }

    public void incrementServiceBindingGetOperationCount() {
        getBindingRequestcount.incrementAndGet();
    }

    public void incrementFailedServiceBindinGetOperationCount() {
        failedGetBindingRequestcount.incrementAndGet();
    }

    public void incrementInstanceGetOperationCount() {
        getInstanceRequestcount.incrementAndGet();
    }

    public void incrementFailedInstanceGetOperationCount() {
        failedGetInstanceRequestcount.incrementAndGet();
    }

    public void incrementTotalRequestCount() {
        requestCount.incrementAndGet();
    }

    public void incrementFailedTotalRequestCount() {
        failedRequestCount.incrementAndGet();
    }
}
