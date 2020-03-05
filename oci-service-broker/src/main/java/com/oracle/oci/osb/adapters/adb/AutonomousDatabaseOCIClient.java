/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapters.adb;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.model.*;
import com.oracle.bmc.database.requests.*;
import com.oracle.bmc.database.responses.*;
import com.oracle.oci.osb.util.Constants;
import com.oracle.oci.osb.util.RequestUtil;
import com.oracle.oci.osb.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.oci.osb.util.Utils.debugLog;
import static com.oracle.oci.osb.util.Utils.getLogger;

public class AutonomousDatabaseOCIClient implements AutoCloseable{

    private static final Logger LOGGER = getLogger(AutonomousDatabaseOCIClient.class);

    private DatabaseClient ociDBClient;

    private String compartmentId;

    AutonomousDatabaseOCIClient(AuthenticationDetailsProvider authProvider, String compartmentId) {
        this(authProvider, compartmentId, Region.fromRegionId(System.getProperty(Constants.REGION_ID)));
    }

    private AutonomousDatabaseOCIClient(AuthenticationDetailsProvider authProvider, String compartmentId, Region regionId) {
        this.compartmentId = compartmentId;
        ociDBClient = new DatabaseClient(authProvider);
        ociDBClient.setRegion(regionId);
    }

    /**
     * Create an AD instance. The instance will be provisioned asynchronously
     *
     * @param displayName display name(mostly for console)
     * @param dbName      name of the database.
     * @param type        DBWorkload Type of the Database. OLTP or DW.
     * @param cpuCount    number of cpu cores required.
     * @param StorageSize required storage size for DB in Terabytes.
     * @param tags        freeform tags.
     * @param password    password to be set for the DB admin user.
     * @return AutonomousDatabase
     */
    public AutonomousDatabaseInstance create(String displayName, String dbName, CreateAutonomousDatabaseBase.DbWorkload type,
                                             int cpuCount, int StorageSize, Map<String, String> tags,
                                             Map<String, Map<String, Object>> definedTags, String password,
                                             boolean licenseIncluded, boolean autoScalingEnabled) {
        CreateAutonomousDatabaseDetails request = CreateAutonomousDatabaseDetails.builder()
                .adminPassword(password)
                .compartmentId(compartmentId)
                .cpuCoreCount(cpuCount)
                .dbWorkload(type)
                .dataStorageSizeInTBs(StorageSize)
                .dbName(dbName)
                .displayName(displayName)
                .freeformTags(tags)
                .definedTags(definedTags)
                .licenseModel(licenseIncluded ? CreateAutonomousDatabaseDetails
                        .LicenseModel.LicenseIncluded : CreateAutonomousDatabaseDetails.LicenseModel
                        .BringYourOwnLicense)
                .isAutoScalingEnabled(autoScalingEnabled)
                .build();
        CreateAutonomousDatabaseResponse response = ociDBClient.createAutonomousDatabase
                (CreateAutonomousDatabaseRequest.builder().createAutonomousDatabaseDetails(request).build());

        return buildADInstance(response.getAutonomousDatabase());
    }

    /**
     * Update an AD Instance. Update will be done asynchronously. If the param
     * values are empty or if they already match the existing value then update
     * is skipped for those values. If none of the params have any change then
     * this simply returns without doing any update.
     *
     * @param adOCID     OCID of the AD instance to be updated.
     * @param displayName new display name.
     * @param cpuCount    new number of CPU core.
     * @param StorageSize new DB storage size in Terabytes.
     * @param tags        new freeform tags.
     * @param definedTags new defined tags.
     * @param licenseModelStr New License Type for the existing DB Instance.
     * @param autoScalingEnabled flag to enable autoscaling
     * @return AutonomousDatabase
     */
    public AutonomousDatabaseInstance update(String adOCID, String displayName, String password, int cpuCount, int StorageSize,
                                             Map<String, String> tags, Map<String, Map<String, Object>> definedTags,
                                             String licenseModelStr, boolean autoScalingEnabled) {
        AutonomousDatabase ad = getADInstance(adOCID);
        UpdateAutonomousDatabaseDetails.Builder reqBuilder = UpdateAutonomousDatabaseDetails.builder();

        //Check if update required
        boolean updateRequired = false;
        if (!Utils.isNullOrEmptyString(displayName) && !ad.getDisplayName().equals(displayName)) {
            reqBuilder = reqBuilder.displayName(displayName);
            debugLog(LOGGER, "DisplayName to be updated.from:%s;to:%s", Level.FINE, ad.getDisplayName(), displayName);
            updateRequired = true;
        }
        if (!Utils.isNullOrEmptyString(password)) {
            reqBuilder = reqBuilder.adminPassword(password);
            debugLog(LOGGER, "Admin password to be updated.", Level.FINE);
            updateRequired = true;
        }
        if (cpuCount > 0 && ad.getCpuCoreCount() != cpuCount) {
            reqBuilder = reqBuilder.cpuCoreCount(cpuCount);
            debugLog(LOGGER, "CpuCoreCount to be updated.from:%s;to:%s", Level.FINE, ad.getCpuCoreCount()
                    .toString(), Integer.toString(cpuCount));
            updateRequired = true;
        }
        if (StorageSize > 0 && ad.getDataStorageSizeInTBs() != StorageSize) {
            reqBuilder = reqBuilder.dataStorageSizeInTBs(StorageSize);
            debugLog(LOGGER, "StorageSize to be updated.from:%s;to:%s", Level.FINE, ad.getDataStorageSizeInTBs()
                    .toString(), Integer.toString(StorageSize));
            updateRequired = true;
        }
        if (tags != null && tags.entrySet().size() > 0 && !tags.equals(ad.getFreeformTags())) {
            reqBuilder = reqBuilder.freeformTags(tags);
            debugLog(LOGGER, "tags to be updated.from:%s;to:%s", Level.FINE, ad.getFreeformTags(), tags);
            updateRequired = true;
        }
        if (definedTags != null && definedTags.entrySet().size() > 0 && !definedTags.equals(ad.getDefinedTags())) {
            reqBuilder = reqBuilder.definedTags(definedTags);
            debugLog(LOGGER, "Defined tags to be updated.from:%s;to:%s", Level.FINE, ad.getDefinedTags(), definedTags);
            updateRequired = true;
        }
        if (licenseModelStr != null) {
            AutonomousDatabaseAdapter.LicenseModel licenseModel = AutonomousDatabaseAdapter.LicenseModel.valueOf(licenseModelStr.toUpperCase());
            UpdateAutonomousDatabaseDetails.LicenseModel updateADDLicenseModel = (licenseModel == AutonomousDatabaseAdapter.LicenseModel.NEW) ? UpdateAutonomousDatabaseDetails
                    .LicenseModel.LicenseIncluded : UpdateAutonomousDatabaseDetails.LicenseModel.BringYourOwnLicense;
            if(getADLicenseType(updateADDLicenseModel) != ad.getLicenseModel()) {
                reqBuilder = reqBuilder.licenseModel(updateADDLicenseModel);
                debugLog(LOGGER, "License Model to be updated.from:%s;to:%s", Level.FINE, ad.getLicenseModel().getValue(), licenseModelStr);
                updateRequired = true;
            }
        }
        if (ad.getIsAutoScalingEnabled().booleanValue() != autoScalingEnabled) {
            reqBuilder = reqBuilder.isAutoScalingEnabled(autoScalingEnabled);
            debugLog(LOGGER, "AutoScaling Enabled to be updated.from:%s;to:%s", Level.FINE, ad.getIsAutoScalingEnabled(), autoScalingEnabled);
            updateRequired = true;
        }

        if (!updateRequired) {
            throw new AutonomousDatabaseAdapter.UpdateNotRequiredException();
        }

        UpdateAutonomousDatabaseDetails request = reqBuilder.build();
        UpdateAutonomousDatabaseResponse response = ociDBClient.updateAutonomousDatabase
                (UpdateAutonomousDatabaseRequest.builder().autonomousDatabaseId(adOCID)
                        .updateAutonomousDatabaseDetails(request).build());
        return buildADInstance(response.getAutonomousDatabase());
    }

    /**
     * Get details of an AD instance.
     * @param adOCID OCID of the AD instance to be updated.
     * @return AutonomousDatabase
     */
    public AutonomousDatabaseInstance get(String adOCID) {
        return buildADInstance(getADInstance(adOCID));
    }

    /**
     * Delete an AD instance. The operation will be asynchronously.
     *
     * @param adOCID OCID of the AD instance to be deleted.
     */
    public void delete(String adOCID) {
        DeleteAutonomousDatabaseRequest request = DeleteAutonomousDatabaseRequest.builder().autonomousDatabaseId
                (adOCID).build();
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
        return buildADInstance(response.getAutonomousDatabase());
    }

    /**
     * Download the credential/configuration files for connecting to an AD
     * instance. The files are base64 encoded and converted as strings.
     *
     * @param adID     OCID of the AD instance.
     * @param dbName    name of the database.
     * @param wPassword password to set for the Oracle wallet that is
     *                  created for this request.
     * @return Map with filename/attribute name as keys and filename/attribute
     * base64 encoded contents as values.
     * @throws IOException if downloading credential zip file fails.
     */
    public Map<String, String> getCredentials(String adID, String dbName, String wPassword) throws IOException {
        GenerateAutonomousDatabaseWalletDetails adbWalletDetails = GenerateAutonomousDatabaseWalletDetails.builder()
                .password(wPassword).build();
        GenerateAutonomousDatabaseWalletResponse adbWalletResponse = ociDBClient.generateAutonomousDatabaseWallet(
                GenerateAutonomousDatabaseWalletRequest.builder()
                        .generateAutonomousDatabaseWalletDetails(adbWalletDetails)
                        .autonomousDatabaseId(adID)
                        .build());
        return ADBUtils.generateCredentialsMap(dbName, adbWalletResponse.getInputStream());
    }

    /**
     * Fetch the list of all AD with a specific display name in a given OCI
     * compartment.
     *
     * @param compartmentId OCID of the Compartment.
     * @param displayName   display name to filter the AD instances.
     * @return List of ADB instance details.
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
        response.getItems().forEach((adwSummary) -> autonomousDatabaseInstanceList.add(buildADInstance(adwSummary)));

        return autonomousDatabaseInstanceList;
    }

    public void close() {
        ociDBClient.close();
    }

    private AutonomousDatabase getADInstance(String adOCID) {
        GetAutonomousDatabaseRequest request = GetAutonomousDatabaseRequest.builder().autonomousDatabaseId(adOCID)
                .build();
        GetAutonomousDatabaseResponse response = ociDBClient.getAutonomousDatabase(request);
        return response.getAutonomousDatabase();
    }

    private AutonomousDatabaseInstance buildADInstance(AutonomousDatabaseSummary summary) {
        if (summary == null) {
            return null;
        } else {
            return new AutonomousDatabaseInstance(summary.getId(),
                    getDBWorkloadType(summary.getDbWorkload().getValue()),
                    summary.getDisplayName(),
                    summary.getCpuCoreCount(),
                    summary.getDataStorageSizeInTBs(),
                    summary.getDbName(),
                    getADBLicenseType(summary.getLicenseModel()),
                    summary.getIsAutoScalingEnabled(),
                    summary.getFreeformTags(),
                    AutonomousDatabaseInstance.lifecycleState(summary.getLifecycleState().getValue()));
        }
    }

    private AutonomousDatabaseInstance buildADInstance(AutonomousDatabase adbInstance) {
        if (adbInstance == null) {
            return null;
        } else {
            return new AutonomousDatabaseInstance(adbInstance.getId(),
                    getDBWorkloadType(adbInstance.getDbWorkload().getValue()),
                    adbInstance.getDisplayName(),
                    adbInstance.getCpuCoreCount(),
                    adbInstance.getDataStorageSizeInTBs(),
                    adbInstance.getDbName(),
                    getADBLicenseType(adbInstance.getLicenseModel()),
                    adbInstance.getIsAutoScalingEnabled(),
                    adbInstance.getFreeformTags(),
                    AutonomousDatabaseInstance.lifecycleState(adbInstance.getLifecycleState().getValue()));
        }
    }

    private AutonomousDatabaseAdapter.DBWorkloadType getDBWorkloadType (String dbWorkloadType) {
        if (dbWorkloadType.equalsIgnoreCase(AutonomousDatabase.DbWorkload.Oltp.getValue())) {
            return AutonomousDatabaseAdapter.DBWorkloadType.ATP;
        } else {
            return AutonomousDatabaseAdapter.DBWorkloadType.ADW;
        }
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

    private AutonomousDatabase.LicenseModel getADLicenseType(UpdateAutonomousDatabaseDetails.LicenseModel upADDLicenseModel) {
        switch (upADDLicenseModel) {
            case BringYourOwnLicense: return AutonomousDatabase.LicenseModel.BringYourOwnLicense;
            case LicenseIncluded: return AutonomousDatabase.LicenseModel.LicenseIncluded;
            default: return AutonomousDatabase.LicenseModel.UnknownEnumValue;
        }
    }
}
