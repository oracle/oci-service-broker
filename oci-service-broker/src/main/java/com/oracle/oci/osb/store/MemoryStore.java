/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.store;

import java.util.HashMap;

/**
 * MemoryStore is Memory backed implementation of the Datastore. The data is
 * not persisted anywhere and kept only in the memory.
 */
public class MemoryStore implements DataStore {

    private final HashMap<String, ServiceData> store = new HashMap<>();

    private final HashMap<String, BindingData> svcBindingStore = new HashMap<>();

    @Override
    public void storeServiceData(String instanceId, ServiceData svcData) {
        store.put(instanceId, svcData);
    }

    @Override
    public ServiceData getServiceData(String instanceId) {
        return store.get(instanceId);
    }

    @Override
    public void storeBinding(String bindingId, BindingData bindingData) {
        svcBindingStore.put(bindingId, bindingData);
    }

    @Override
    public BindingData getBindingData(String bindingId) {
        return svcBindingStore.get(bindingId);
    }

    @Override
    public void removeServiceData(String instanceId) {
        store.remove(instanceId);
    }

    @Override
    public void removeBindingData(String instanceId) {
        svcBindingStore.remove(instanceId);
    }

    @Override
    public boolean isStoreHealthy() {
        return true;
    }
}
