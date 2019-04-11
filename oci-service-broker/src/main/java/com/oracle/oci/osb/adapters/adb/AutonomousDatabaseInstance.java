/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapters.adb;

import java.util.Map;

/**
 * AutonomousDatabaseInstance represents Autonomous Database Instance (ATP or ADW).
 */
public class AutonomousDatabaseInstance {
    private String id;
    private TYPE type;
    private String displayName;
    private int cpuCoreCount;
    private int storageSizeInGBs;
    private String dbName;
    private AutonomousDatabaseAdapter.LicenseModel licenseModel;
    private Map<String, String> freeformTags;
    private LifecycleState lifecycleState;

    public AutonomousDatabaseAdapter.LicenseModel getLicenseModel() {
        return licenseModel;
    }

    public enum TYPE {
        ATP, ADW;
    }


    public AutonomousDatabaseInstance(String id, TYPE type, String displayName, int cpuCoreCount, int
            storageSizeInGBs, String dbName, AutonomousDatabaseAdapter.LicenseModel licenseModel, Map<String, String> freeformTags, LifecycleState lifecycleState) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.cpuCoreCount = cpuCoreCount;
        this.storageSizeInGBs = storageSizeInGBs;
        this.dbName = dbName;
        this.licenseModel = licenseModel;
        this.freeformTags = freeformTags;
        this.lifecycleState = lifecycleState;
    }

    public static LifecycleState lifecycleState(String lifecycleStateStr) {
        for (LifecycleState lstate : LifecycleState.values()) {
            if (lifecycleStateStr.equalsIgnoreCase(lstate.getValue())) {
                return lstate;
            }
        }
        return LifecycleState.UnknownEnumValue;
    }

    public String getId() {
        return id;
    }

    public TYPE getType() {
        return type;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCpuCoreCount() {
        return cpuCoreCount;
    }

    public int getStorageSizeInGBs() {
        return storageSizeInGBs;
    }

    public LifecycleState getLifecycleState() {
        return lifecycleState;
    }

    public Map<String, String> getFreeformTags() {
        return freeformTags;
    }

    /**
     * LifecycleState of the Autonomous Database Instance
     */
    public enum LifecycleState {
        Provisioning("PROVISIONING"),
        Available("AVAILABLE"),
        Stopping("STOPPING"),
        Stopped("STOPPED"),
        Starting("STARTING"),
        Terminating("TERMINATING"),
        Terminated("TERMINATED"),
        Unavailable("UNAVAILABLE"),
        RestoreInProgress("RESTORE_IN_PROGRESS"),
        BackupInProgress("BACKUP_IN_PROGRESS"),
        ScaleInProgress("SCALE_IN_PROGRESS"),
        AvailableNeedsAttention("AVAILABLE_NEEDS_ATTENTION"),
        UnknownEnumValue(null);

        private final String value;

        LifecycleState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
