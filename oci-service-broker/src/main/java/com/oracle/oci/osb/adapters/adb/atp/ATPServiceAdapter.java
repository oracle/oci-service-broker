/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapters.adb.atp;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.oci.osb.adapters.adb.AutonomousDatabaseAdapter;
import com.oracle.oci.osb.adapters.adb.AutonomousDatabaseInstance;
import com.oracle.oci.osb.adapters.adb.AutonomousDatabaseOCIClient;
import com.oracle.oci.osb.util.Constants;

/**
 * ATPServiceAdapter provides implementation to provision and manage
 * Autonomous Transaction Processor Service instance.
 */
public class ATPServiceAdapter extends AutonomousDatabaseAdapter {

    @Override
    protected String getInstanceTypeString() {
        return AutonomousDatabaseAdapter.DBWorkloadType.ATP.name();

    }

    @Override
    protected String getCatalogFileName() {
        return Constants.ATP_CATALOG_JSON;
    }
}
