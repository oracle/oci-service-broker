/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapters.adb.atp;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.model.*;
import com.oracle.bmc.database.requests.*;
import com.oracle.bmc.database.responses.*;
import com.oracle.oci.osb.adapters.adb.ADBUtils;
import com.oracle.oci.osb.adapters.adb.AutonomousDatabaseAdapter;
import com.oracle.oci.osb.adapters.adb.AutonomousDatabaseInstance;
import com.oracle.oci.osb.adapters.adb.AutonomousDatabaseOCIClient;
import com.oracle.oci.osb.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.oci.osb.util.Utils.*;
/**
 * ATPServiceOCIClient provides implementation of various operation for aTP
 * Service. This class abstracts the OCI Service SDK/API related details.
 */
public class ATPServiceOCIClient implements AutonomousDatabaseOCIClient {

    private static final Logger LOGGER = getLogger(ATPServiceOCIClient.class);

    private DatabaseClient ociDBClient;

    private String compartmentId;


    ATPServiceOCIClient(AuthenticationDetailsProvider authProvider, String compartmentId) {
        this(authProvider, compartmentId, Region.fromRegionId(System.getProperty(Constants.REGION_ID)));
    }

    private ATPServiceOCIClient(AuthenticationDetailsProvider authProvider, String compartmentId, Region regionId) {
        this.compartmentId = compartmentId;
        ociDBClient = new DatabaseClient(authProvider);
        ociDBClient.setRegion(regionId);
    }

    /**
     * Create an ATP instance. The instance will be provisioned asynchronously
     *
     * @param displayName display name(mostly for console)
     * @param dbName      name of the database.
     * @param cpuCount    number of cpu cores required.
     * @param StorageSize required storage size for DB in Terabytes.
     * @param tags        freeform tags.
     * @param password    password to be set for the DB admin user.
     * @return AutonomousDatabase
     */
    public AutonomousDatabaseInstance create(String displayName, String dbName, int cpuCount, int StorageSize,
                                             Map<String, String> tags, Map<String, Map<String, Object>> definedTags,
                                             String password, boolean licenseIncluded) {
        CreateAutonomousDatabaseDetails request = CreateAutonomousDatabaseDetails.builder()
                .adminPassword(password)
                .compartmentId(compartmentId)
                .cpuCoreCount(cpuCount)
                .dataStorageSizeInTBs(StorageSize)
                .dbName(dbName)
                .displayName(displayName)
                .freeformTags(tags)
                .definedTags(definedTags)
                .licenseModel(licenseIncluded ? CreateAutonomousDatabaseDetails
                        .LicenseModel.LicenseIncluded : CreateAutonomousDatabaseDetails.LicenseModel
                        .BringYourOwnLicense)
                .build();
        CreateAutonomousDatabaseResponse response = ociDBClient.createAutonomousDatabase
                (CreateAutonomousDatabaseRequest.builder().createAutonomousDatabaseDetails(request).build());

        return buildADBInstance(response.getAutonomousDatabase());
    }

    /**
     * Update an ATP Instance. Update will be done asynchronously. If the param
     * values are empty or if they already match the existing value then update
     * is skipped for those values. If none of the params have any change then
     * this simply returns without doing any update.
     *
     * @param atpOCID     OCID of the ATP instance to be updated.
     * @param displayName new display name.
     * @param cpuCount    new number of CPU core.
     * @param StorageSize new DB storage size in Terabytes.
     * @param tags        new freeform tags.
     * @return AutonomousDatabase
     */
    public AutonomousDatabaseInstance update(String atpOCID, String displayName, int cpuCount, int StorageSize,
                                             Map<String, String> tags, Map<String, Map<String, Object>> definedTags) {
        AutonomousDatabase atp = getATPInstance(atpOCID);
        UpdateAutonomousDatabaseDetails.Builder reqBuilder = UpdateAutonomousDatabaseDetails.builder();

        //Check if update required
        boolean updateRequired = false;
        if (!isNullOrEmptyString(displayName) && !atp.getDisplayName().equals(displayName)) {
            reqBuilder = reqBuilder.displayName(displayName);
            debugLog(LOGGER, "DisplayName to be updated.from:%s;to:%s", Level.FINE, atp.getDisplayName(), displayName);
            updateRequired = true;
        }
        if (cpuCount > 0 && atp.getCpuCoreCount() != cpuCount) {
            reqBuilder = reqBuilder.cpuCoreCount(cpuCount);
            debugLog(LOGGER, "CpuCoreCount to be updated.from:%s;to:%s", Level.FINE, atp.getCpuCoreCount()
                    .toString(), Integer.toString(cpuCount));
            updateRequired = true;
        }
        if (StorageSize > 0 && atp.getDataStorageSizeInTBs() != StorageSize) {
            reqBuilder = reqBuilder.dataStorageSizeInTBs(StorageSize);
            debugLog(LOGGER, "StorageSize to be updated.from:%s;to:%s", Level.FINE, atp.getDataStorageSizeInTBs()
                    .toString(), Integer.toString(StorageSize));
            updateRequired = true;
        }
        if (tags != null && tags.entrySet().size() > 0 && !tags.equals(atp.getFreeformTags())) {
            reqBuilder = reqBuilder.freeformTags(tags);
            debugLog(LOGGER, "tags to be updated.from:%s;to:%s", Level.FINE, atp.getFreeformTags(), tags);
            updateRequired = true;
        }
        if (definedTags != null && definedTags.entrySet().size() > 0 && !definedTags.equals(atp.getDefinedTags())) {
            reqBuilder = reqBuilder.definedTags(definedTags);
            debugLog(LOGGER, "Defined tags to be updated.from:%s;to:%s", Level.FINE, atp.getDefinedTags(), definedTags);
            updateRequired = true;
        }
        if (!updateRequired) {
            throw new AutonomousDatabaseAdapter.UpdateNotRequiredException();
        }

        UpdateAutonomousDatabaseDetails request = reqBuilder.build();
        UpdateAutonomousDatabaseResponse response = ociDBClient.updateAutonomousDatabase
                (UpdateAutonomousDatabaseRequest.builder().autonomousDatabaseId(atpOCID)
                        .updateAutonomousDatabaseDetails(request).build());
        return buildADBInstance(response.getAutonomousDatabase());
    }

    /**
     * Get details of an ATP instance.
     * @param atpOCID OCID of the ATP instance to be updated.
     * @return AutonomousDatabase
     */
    public AutonomousDatabaseInstance get(String atpOCID) {
        return buildADBInstance(getATPInstance(atpOCID));
    }

    /**
     * Delete an ATP instance. The operation will be asynchronously.
     *
     * @param atpOCID OCID of the ATP instance to be deleted.
     */
    public void delete(String atpOCID) {
        DeleteAutonomousDatabaseRequest request = DeleteAutonomousDatabaseRequest.builder().autonomousDatabaseId
                (atpOCID).build();
        ociDBClient.deleteAutonomousDatabase(request);
    }

    /**
     * Update DB ADMIN password.
     *
     * @param password new DB ADMIN password.
     * @return AutonomousDatabase
     */
    public AutonomousDatabaseInstance changePassword(String password) {
        UpdateAutonomousDatabaseDetails request = UpdateAutonomousDatabaseDetails.builder().adminPassword(password)
                .build();
        UpdateAutonomousDatabaseResponse response = ociDBClient.updateAutonomousDatabase
                (UpdateAutonomousDatabaseRequest.builder().updateAutonomousDatabaseDetails(request).build());
        return buildADBInstance(response.getAutonomousDatabase());
    }

    /**
     * Download the credential/configuration files for connecting to an ATP
     * instance. The files are base64 encoded and converted as strings.
     *
     * @param atpId     OCID of the ATP instance.
     * @param dbName    name of the database.
     * @param wPassword password to set for the Oracle wallet that is
     *                  created for this request.
     * @return Map with filename/attribute name as keys and filename/attribute
     * base64 encoded contents as values.
     * @throws IOException if downloading credential zip file fails.
     */
    public Map<String, String> getCredentials(String atpId, String dbName, String wPassword) throws IOException {
        GenerateAutonomousDatabaseWalletDetails atpWalletDetails = GenerateAutonomousDatabaseWalletDetails.builder()
                .password(wPassword).build();
        GenerateAutonomousDatabaseWalletResponse atpWalletResponse = ociDBClient.generateAutonomousDatabaseWallet(
                GenerateAutonomousDatabaseWalletRequest.builder()
                        .generateAutonomousDatabaseWalletDetails(atpWalletDetails)
                        .autonomousDatabaseId(atpId)
                        .build());
        return ADBUtils.generateCredentialsMap(dbName, atpWalletResponse.getInputStream());
    }

    /**
     * Fetch the list of all ATP with a specific display name in a given OCI
     * compartment.
     *
     * @param compartmentId OCID of the Compartment.
     * @param displayName   display name to filter the ATP instances.
     * @return List of ATP instance details.
     */
    public List<AutonomousDatabaseInstance> listInstances(String compartmentId, String displayName) {
        ListAutonomousDatabasesRequest.Builder reqBuilder = ListAutonomousDatabasesRequest.builder().compartmentId
                (compartmentId);
        if (displayName != null && !displayName.isEmpty()) {
            reqBuilder.displayName(displayName);
        }
        ListAutonomousDatabasesRequest request = reqBuilder.build();
        ListAutonomousDatabasesResponse response = ociDBClient.listAutonomousDatabases(request);
        List<AutonomousDatabaseInstance> autonomousDatabaseInstanceList = new ArrayList<>();
        response.getItems().forEach((adwSummary) -> autonomousDatabaseInstanceList.add(buildADBInstance(adwSummary)));

        return autonomousDatabaseInstanceList;
    }

    @Override
    public void close() {
        ociDBClient.close();
    }

    private AutonomousDatabase getATPInstance(String adwOCID) {
        GetAutonomousDatabaseRequest request = GetAutonomousDatabaseRequest.builder().autonomousDatabaseId(adwOCID)
                .build();
        GetAutonomousDatabaseResponse response = ociDBClient.getAutonomousDatabase(request);
        return response.getAutonomousDatabase();
    }

    private AutonomousDatabaseInstance buildADBInstance(AutonomousDatabaseSummary summary) {
        return new AutonomousDatabaseInstance(summary.getId(),
                AutonomousDatabaseInstance.TYPE.ATP,
                summary.getDisplayName(),
                summary.getCpuCoreCount(),
                summary.getDataStorageSizeInTBs(),
                summary.getDbName(),
                getADBLicenseType(summary.getLicenseModel()),
                summary.getFreeformTags(),
                AutonomousDatabaseInstance.lifecycleState(summary.getLifecycleState().getValue()));
    }

    private AutonomousDatabaseInstance buildADBInstance(AutonomousDatabase atpInstance) {
        return new AutonomousDatabaseInstance(atpInstance.getId(),
                AutonomousDatabaseInstance.TYPE.ATP,
                atpInstance.getDisplayName(),
                atpInstance.getCpuCoreCount(),
                atpInstance.getDataStorageSizeInTBs(),
                atpInstance.getDbName(),
                getADBLicenseType(atpInstance.getLicenseModel()),
                atpInstance.getFreeformTags(),
                AutonomousDatabaseInstance.lifecycleState(atpInstance.getLifecycleState().getValue()));
    }

    private AutonomousDatabaseAdapter.LicenseModel getADBLicenseType(AutonomousDatabaseSummary.LicenseModel
                                                                            sdkLicenseModel){
        switch (sdkLicenseModel) {
            case BringYourOwnLicense: return AutonomousDatabaseAdapter.LicenseModel.BYOL;
            case LicenseIncluded: return AutonomousDatabaseAdapter.LicenseModel.NEW;
            default: return AutonomousDatabaseAdapter.LicenseModel.UNKNOWN;
        }
    }

    private AutonomousDatabaseAdapter.LicenseModel getADBLicenseType(AutonomousDatabase.LicenseModel
                                                                            adwLicenseModel){
        switch (adwLicenseModel) {
            case BringYourOwnLicense: return AutonomousDatabaseAdapter.LicenseModel.BYOL;
            case LicenseIncluded: return AutonomousDatabaseAdapter.LicenseModel.NEW;
            default: return AutonomousDatabaseAdapter.LicenseModel.UNKNOWN;
        }
    }
}
