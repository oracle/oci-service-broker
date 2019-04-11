/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapters.adb.adw;

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
 * ADWServiceOCIClient provides implementation of various operation for aTP
 * Service. This class abstracts the OCI Service SDK/API related details.
 */
public class ADWServiceOCIClient implements AutonomousDatabaseOCIClient {

    private static final Logger LOGGER = getLogger(ADWServiceOCIClient.class);

    private DatabaseClient ociDBClient;

    private String compartmentId;

    ADWServiceOCIClient(AuthenticationDetailsProvider authProvider, String compartmentId) {
        this(authProvider, compartmentId, Region.fromRegionId(System.getProperty(Constants.REGION_ID)));
    }

    private ADWServiceOCIClient(AuthenticationDetailsProvider authProvider, String compartmentId, Region regionId) {
        this.compartmentId = compartmentId;
        String regionId1 = regionId.getRegionId();
        ociDBClient = new DatabaseClient(authProvider);
        ociDBClient.setRegion(regionId1);
    }

    /**
     * Create an ADW instance. The instance will be provisioned asynchronously
     *
     * @param displayName display name(mostly for console)
     * @param dbName      name of the database.
     * @param cpuCount    number of cpu cores required.
     * @param StorageSize required storage size for DB in Terabytes.
     * @param tags        freeform tags.
     * @param password    password to be set for the DB admin user.
     * @return AutonomousDataWarehouse
     */
    public AutonomousDatabaseInstance create(String displayName, String dbName, int cpuCount, int StorageSize,
                                             Map<String, String> tags, Map<String, Map<String, Object>> definedTags,
                                             String password, boolean licenseIncluded) {
        CreateAutonomousDataWarehouseDetails request = CreateAutonomousDataWarehouseDetails.builder()
                .adminPassword(password)
                .compartmentId(compartmentId)
                .cpuCoreCount(cpuCount)
                .dataStorageSizeInTBs(StorageSize)
                .dbName(dbName)
                .displayName(displayName)
                .freeformTags(tags)
                .definedTags(definedTags)
                .licenseModel(licenseIncluded ? CreateAutonomousDataWarehouseDetails
                        .LicenseModel.LicenseIncluded : CreateAutonomousDataWarehouseDetails.LicenseModel
                        .BringYourOwnLicense)
                .build();
        CreateAutonomousDataWarehouseResponse response = ociDBClient.createAutonomousDataWarehouse
                (CreateAutonomousDataWarehouseRequest.builder().createAutonomousDataWarehouseDetails(request).build());

        return buildADBInstance(response.getAutonomousDataWarehouse());
    }

    /**
     * Update an ADW Instance. Update will be done asynchronously. If the param
     * values are empty or if they already match the existing value then update
     * is skipped for those values. If none of the params have any change then
     * this simply returns without doing any update.
     *
     * @param adwOCID     OCID of the ADW instance to be updated.
     * @param displayName new display name.
     * @param cpuCount    new number of CPU core.
     * @param StorageSize new DB storage size in Terabytes.
     * @param tags        new freeform tags.
     * @return AutonomousDataWarehouse
     */
    public AutonomousDatabaseInstance update(String adwOCID, String displayName, int cpuCount, int StorageSize,
                                             Map<String, String> tags, Map<String, Map<String, Object>> definedTags) {
        AutonomousDataWarehouse adw = getADWInstance(adwOCID);
        UpdateAutonomousDataWarehouseDetails.Builder reqBuilder = UpdateAutonomousDataWarehouseDetails.builder();

        //Check if update required
        boolean updateRequired = false;
        if (!isNullOrEmptyString(displayName) && !adw.getDisplayName().equals(displayName)) {
            reqBuilder = reqBuilder.displayName(displayName);
            debugLog(LOGGER, "DisplayName to be updated.from:%s;to:%s", Level.FINE, adw.getDisplayName(), displayName);
            updateRequired = true;
        }
        if (cpuCount > 0 && adw.getCpuCoreCount() != cpuCount) {
            reqBuilder = reqBuilder.cpuCoreCount(cpuCount);
            debugLog(LOGGER, "CpuCoreCount to be updated.from:%s;to:%s", Level.FINE, adw.getCpuCoreCount()
                    .toString(), Integer.toString(cpuCount));
            updateRequired = true;
        }
        if (StorageSize > 0 && adw.getDataStorageSizeInTBs() != StorageSize) {
            reqBuilder = reqBuilder.dataStorageSizeInTBs(StorageSize);
            debugLog(LOGGER, "StorageSize to be updated.from:%s;to:%s", Level.FINE, adw.getDataStorageSizeInTBs()
                    .toString(), Integer.toString(StorageSize));
            updateRequired = true;
        }
        if (tags != null && tags.entrySet().size() > 0 && !tags.equals(adw.getFreeformTags())) {
            reqBuilder = reqBuilder.freeformTags(tags);
            debugLog(LOGGER, "tags to be updated.from:%s;to:%s", Level.FINE, adw.getFreeformTags(), tags);
            updateRequired = true;
        }
        if (definedTags != null && definedTags.entrySet().size() > 0 && !definedTags.equals(adw.getDefinedTags())) {
            reqBuilder = reqBuilder.definedTags(definedTags);
            debugLog(LOGGER, "Defined tags to be updated.from:%s;to:%s", Level.FINE, adw.getDefinedTags(), definedTags);
            updateRequired = true;
        }

        if (!updateRequired) {
            throw new AutonomousDatabaseAdapter.UpdateNotRequiredException();
        }

        UpdateAutonomousDataWarehouseDetails request = reqBuilder.build();
        UpdateAutonomousDataWarehouseResponse response = ociDBClient.updateAutonomousDataWarehouse
                (UpdateAutonomousDataWarehouseRequest.builder().autonomousDataWarehouseId(adwOCID)
                        .updateAutonomousDataWarehouseDetails(request).build());

        return buildADBInstance(response.getAutonomousDataWarehouse());
    }

    /**
     * Get details of an ADW instance.
     *
     * @param adwOCID OCID of the ADW instance to be updated.
     * @return AutonomousDataWarehouse
     */
    public AutonomousDatabaseInstance get(String adwOCID) {
        return buildADBInstance(getADWInstance(adwOCID));
    }

    private AutonomousDataWarehouse getADWInstance(String adwOCID) {
        GetAutonomousDataWarehouseRequest request = GetAutonomousDataWarehouseRequest.builder()
                .autonomousDataWarehouseId(adwOCID).build();
        GetAutonomousDataWarehouseResponse response = ociDBClient.getAutonomousDataWarehouse(request);
        return response.getAutonomousDataWarehouse();
    }

    /**
     * Delete an ADW instance. The operation will be asynchronously.
     *
     * @param adwOCID OCID of the ADW instance to be deleted.
     */
    public void delete(String adwOCID) {
        DeleteAutonomousDataWarehouseRequest request = DeleteAutonomousDataWarehouseRequest.builder()
                .autonomousDataWarehouseId(adwOCID).build();
        ociDBClient.deleteAutonomousDataWarehouse(request);
    }

    /**
     * Update DB ADMIN password.
     *
     * @param password new DB ADMIN password.
     * @return AutonomousDataWarehouse
     */
    public AutonomousDatabaseInstance changePassword(String password) {
        UpdateAutonomousDataWarehouseDetails request = UpdateAutonomousDataWarehouseDetails.builder().adminPassword
                (password).build();
        UpdateAutonomousDataWarehouseResponse response = ociDBClient.updateAutonomousDataWarehouse
                (UpdateAutonomousDataWarehouseRequest.builder().updateAutonomousDataWarehouseDetails(request).build());
        return buildADBInstance(response.getAutonomousDataWarehouse());
    }

    /**
     * Download the credential/configuration files for connecting to an ADW
     * instance. The files are base64 encoded and converted as strings.
     *
     * @param adwId     OCID of the ADW instance.
     * @param dbName    name of the database.
     * @param wPassword password to set for the Oracle wallet that is
     *                  created for this request.
     * @return Map with filename/attribute name as keys and filename/attribute
     * base64 encoded contents as values.
     * @throws IOException if downloading credential zip file fails.
     */
    public Map<String, String> getCredentials(String adwId, String dbName, String wPassword) throws IOException {
        GenerateAutonomousDataWarehouseWalletDetails adwWalletDetails = GenerateAutonomousDataWarehouseWalletDetails
                .builder().password(wPassword).build();
        GenerateAutonomousDataWarehouseWalletResponse adwWalletResponse = ociDBClient
                .generateAutonomousDataWarehouseWallet(GenerateAutonomousDataWarehouseWalletRequest.builder()
                        .generateAutonomousDataWarehouseWalletDetails(adwWalletDetails)
                        .autonomousDataWarehouseId(adwId)
                        .build());
        return ADBUtils.generateCredentialsMap(dbName, adwWalletResponse.getInputStream());
    }


    /**
     * Fetch the list of all ADW instances in a given OCI Compartment.
     *
     * @param compartmentId OCID of the Compartment.
     * @return List of ADW instance details in the compartment.
     */
    public List<AutonomousDatabaseInstance> listInstances(String compartmentId) {
        return listInstances(compartmentId, "");
    }

    /**
     * Fetch the list of all ADW with a specific display name in a given OCI
     * compartment.
     *
     * @param compartmentId OCID of the Compartment.
     * @param displayName   display name to filter the ADW instances.
     * @return List of ADW instance details.
     */
    public List<AutonomousDatabaseInstance> listInstances(String compartmentId, String displayName) {
        ListAutonomousDataWarehousesRequest.Builder reqBuilder = ListAutonomousDataWarehousesRequest.builder()
                .compartmentId(compartmentId);
        if (displayName != null && !displayName.isEmpty()) {
            reqBuilder.displayName(displayName);
        }
        ListAutonomousDataWarehousesRequest request = reqBuilder.build();
        ListAutonomousDataWarehousesResponse response = ociDBClient.listAutonomousDataWarehouses(request);

        List<AutonomousDatabaseInstance> autonomousDatabaseInstanceList = new ArrayList<>();
        response.getItems().forEach((adwSummary) -> autonomousDatabaseInstanceList.add(buildADBInstance(adwSummary)));

        return autonomousDatabaseInstanceList;
    }

    @Override
    public void close() {
        ociDBClient.close();
    }

    private AutonomousDatabaseInstance buildADBInstance(AutonomousDataWarehouseSummary summary) {
        return new AutonomousDatabaseInstance(summary.getId(),
                AutonomousDatabaseInstance.TYPE.ADW,
                summary.getDisplayName(),
                summary.getCpuCoreCount(),
                summary.getDataStorageSizeInTBs(),
                summary.getDbName(),
                getADBLicenseType(summary.getLicenseModel()),
                summary.getFreeformTags(),
                AutonomousDatabaseInstance.lifecycleState(summary.getLifecycleState().getValue()));
    }

    private AutonomousDatabaseInstance buildADBInstance(AutonomousDataWarehouse adwInstance) {
        return new AutonomousDatabaseInstance(adwInstance.getId(),
                AutonomousDatabaseInstance.TYPE.ADW,
                adwInstance.getDisplayName(),
                adwInstance.getCpuCoreCount(),
                adwInstance.getDataStorageSizeInTBs(),
                adwInstance.getDbName(),
                getADBLicenseType(adwInstance.getLicenseModel()),
                adwInstance.getFreeformTags(),
                AutonomousDatabaseInstance.lifecycleState(adwInstance.getLifecycleState().getValue()));
    }

    private AutonomousDatabaseAdapter.LicenseModel getADBLicenseType(AutonomousDataWarehouseSummary.LicenseModel
                                                                            sdkLicenseModel){
        switch (sdkLicenseModel) {
            case BringYourOwnLicense: return AutonomousDatabaseAdapter.LicenseModel.BYOL;
            case LicenseIncluded: return AutonomousDatabaseAdapter.LicenseModel.NEW;
            default: return AutonomousDatabaseAdapter.LicenseModel.UNKNOWN;
        }
    }

    private AutonomousDatabaseAdapter.LicenseModel getADBLicenseType(AutonomousDataWarehouse.LicenseModel
                                                                            adwLicenseModel){
        switch (adwLicenseModel) {
            case BringYourOwnLicense: return AutonomousDatabaseAdapter.LicenseModel.BYOL;
            case LicenseIncluded: return AutonomousDatabaseAdapter.LicenseModel.NEW;
            default: return AutonomousDatabaseAdapter.LicenseModel.UNKNOWN;
        }
    }
}
