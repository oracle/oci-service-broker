/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapters.adb.adw;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.oci.osb.adapters.adb.AutonomousDatabaseAdapter;
import com.oracle.oci.osb.adapters.adb.AutonomousDatabaseInstance;
import com.oracle.oci.osb.adapters.adb.AutonomousDatabaseOCIClient;
import com.oracle.oci.osb.util.Constants;

/**
 * ADWServiceAdapter provides implementation to provision and manage
 * Data Warehouse Service instance.
 */
public class ADWServiceAdapter extends AutonomousDatabaseAdapter {

    @Override
    protected AutonomousDatabaseOCIClient getOCIClient(AuthenticationDetailsProvider authProvider, String
            compartmentId) {
        return new ADWServiceOCIClient(authProvider, compartmentId);
    }

    @Override
    protected String getInstanceTypeString() {
        return AutonomousDatabaseInstance.TYPE.ADW.name();
    }

    @Override
    protected String getCatalogFileName() {
        return Constants.ADW_CATALOG_JSON;
    }
}
