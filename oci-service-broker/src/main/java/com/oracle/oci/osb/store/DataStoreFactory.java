/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.store;

import com.oracle.oci.osb.util.Constants;
import com.oracle.oci.osb.util.Errors;

public class DataStoreFactory {

    private static DataStore dataStore = createDataStore();


    /**
     * Returns the DataStore instance.
     *
     * @return DataStore
     */
    public static DataStore getDataStore() {
        return dataStore;
    }

    /**
     * Creates a new instance of DataStore.
     *
     * @return DataStore
     */
    public static DataStore createDataStore() {
        String storeType = System.getProperty(Constants.STORE_TYPE, Constants.OBJECT_STORE_TYPE);
        switch (storeType) {
            case Constants.MEMORY_TYPE:
                return new MemoryStore();
            case Constants.OBJECT_STORE_TYPE:
                return new ObjectStorageStore();
            case Constants.ETCD_TYPE:
                return new EtcdStore();
            default:
                throw Errors.invalidStoreTypeError();
        }

    }
}
