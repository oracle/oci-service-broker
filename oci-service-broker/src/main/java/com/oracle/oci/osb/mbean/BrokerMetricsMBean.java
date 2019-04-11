/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.mbean;

/**
 * MBean interface for exposing OCI OSB specific metrics.
 */
public interface BrokerMetricsMBean {

    /**
     * @return the total number of requests
     */
    long getRequestCount();

    /**
     * @return the total number of failed requests
     */
    long getFailedRequestCount();

    /**
     * @return the total number of provision requests
     */
    long getProvisionRequestCount();

    /**
     * @return the total number of failed provision requests
     */
    long getFailedProvisionRequestCount();

    /**
     * @return the total number of binding requests
     */
    long getBindingRequestCount();

    /**
     * @return the total number of failed binding requests
     */
    long getFailedBindingRequestCount();

    /**
     * @return the total number of deprovision requests
     */
    long getDeprovisionRequestCount();

    /**
     * @return the total number of failed deprovision requests
     */
    long getFailedDeprovisionRequestCount();

    /**
     * @return the total number of unbind requests
     */
    long getUnbindRequestCount();

    /**
     * @return the total number of failed unbind requests
     */
    long getFailedUnbindRequestCount();

    /**
     * @return the total number of update requests
     */
    long getUpdateRequestCount();

    /**
     * @return the total number of failed update requests
     */
    long getFailedUpdateRequestCount();

    /**
     * @return the total number of GetInstance requests
     */
    long getGetInstanceRequestCount();

    /**
     * @return the total number of failed GetInstance requests
     */
    long getFailedGetInstanceRequestCount();

    /**
     * @return the total number of GetBinding requests
     */
    long getGetBindingRequestCount();

    /**
     * @return the total number of failed GetBinding requests
     */
    long getFailedGetBindingRequestCount();

    /**
     * @return the total number of last operation requests for instance specific operations
     */
    long getLastOperationInstanceRequestCount();

    /**
     * @return the total number of failed last operation requests for instance specific operations
     */
    long getFailedLastOperationInstanceRequestCount();

    /**
     * @return the total number of last operation requests for binding specific operations
     */
    long getLastOperationBindingRequestCount();

    /**
     * @return the total number of failed last operation requests for binding specific operations
     */
    long getFailedLastOperationBindingRequestCount();

}
