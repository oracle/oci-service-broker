/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.store;

/**
 * DataStore provides interface for the Services to store and retreive
 * service and binding related metadata.
 */
public interface DataStore {

    /**
     * Store service related metadata for an Service instance.
     *
     * @param instanceId unique id of the service instance.
     * @param svcData    service metadata
     */
    void storeServiceData(String instanceId, ServiceData svcData);

    /**
     * Fetch service metadata for an Service Instance.
     *
     * @param instanceId unique id of the service instance.
     * @return service metadata
     */
    ServiceData getServiceData(String instanceId);

    /**
     * Store binding related metadata for an Service instance.
     *
     * @param bindingId   unique binding id .
     * @param bindingData binding metadata.
     */
    void storeBinding(String bindingId, BindingData bindingData);

    /**
     * Fetch binding metadata for an Service Instance.
     *
     * @param bindingId unique binding id.
     * @return bindingData
     */
    BindingData getBindingData(String bindingId);

    /**
     * Remove service metadata related to an Service Instance.
     *
     * @param instanceId unique id of the service instance.
     */
    void removeServiceData(String instanceId);

    /**
     * Remove binding metadata related to an Service Instance.
     *
     * @param bindingId
     */
    void removeBindingData(String bindingId);

    /**
     * Checks if the store is healthy.
     *
     * @return true if the store is healthy, false otherwise
     */
    boolean isStoreHealthy();
}
