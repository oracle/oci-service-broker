/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.store;

import java.util.HashMap;

/**
 * BindingData class holds the Binding related metedata for all Services.
 */
public class BindingData {

    private String instanceId;

    private String serviceId;

    private String planId;

    private String bindingId;

    // Used for storing service specific attributes.
    private HashMap<String, String> metadata = new HashMap<>();


    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    /**
     * Add service specific metadata
     *
     * @param key
     * @param value
     */
    public void putMetadata(String key, String value) {
        metadata.put(key, value);
    }

    /**
     * Fetch a metadata value.
     *
     * @param key
     * @return metadata value.
     */
    public String getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * Remove a metadata value.
     *
     * @param key
     * @return removed metadata value.
     */
    public String removeMetadata(String key) {
        return metadata.remove(key);
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }
}
