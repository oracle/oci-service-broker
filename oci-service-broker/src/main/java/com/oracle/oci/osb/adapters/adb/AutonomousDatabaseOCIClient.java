/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapters.adb;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AutonomousDatabaseOCIClient extends AutoCloseable {
    AutonomousDatabaseInstance create(String displayName, String dbName, int cpuCount, int StorageSize, Map<String,
            String> tags, Map<String, Map<String, Object>> definedTags, String password, boolean licenseIncluded);

    AutonomousDatabaseInstance update(String instanceOCID, String displayName, int cpuCount, int StorageSize,
                                      Map<String, String> tags, Map<String, Map<String, Object>> definedTags);

    AutonomousDatabaseInstance get(String instanceOCID);

    void delete(String instanceOCID);

    void close();

    AutonomousDatabaseInstance changePassword(String password);

    Map<String, String> getCredentials(String atpId, String dbName, String wPassword) throws IOException;

    List<AutonomousDatabaseInstance> listInstances(String compartmentId);

    List<AutonomousDatabaseInstance> listInstances(String compartmentId, String displayName);
}
