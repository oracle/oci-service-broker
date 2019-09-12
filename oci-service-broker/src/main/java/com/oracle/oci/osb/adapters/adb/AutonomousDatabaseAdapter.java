/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapters.adb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.bmc.model.BmcException;
import com.oracle.oci.osb.adapter.ServiceAdapter;
import com.oracle.oci.osb.model.*;
import com.oracle.oci.osb.ociclient.SystemPropsAuthProvider;
import com.oracle.oci.osb.store.BindingData;
import com.oracle.oci.osb.store.ServiceData;
import com.oracle.oci.osb.util.Constants;
import com.oracle.oci.osb.util.Errors;
import com.oracle.oci.osb.util.RequestUtil;
import com.oracle.oci.osb.util.Utils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.oci.osb.util.Utils.debugLog;
import static com.oracle.oci.osb.util.Utils.getLogger;
import static java.net.HttpURLConnection.*;
/**
 * AutonomousDatabaseAdapter provides implementation to provision and manage
 * instances of OCI services that are based on Oracle Autonomous Database.
 */
public abstract class AutonomousDatabaseAdapter implements ServiceAdapter {

    private static final Logger LOGGER = getLogger(AutonomousDatabaseAdapter.class);

    public static final String REQ_PARAM_COMPARTMENT_ID = "compartmentId";

    public static final String REQ_PARAM_LICENSE_MODEL = "licenseType";

    public static final String REQ_PARAM_NAME = "name";

    public static final String REQ_PARAM_DB_NAME = "dbName";

    public static final String REQ_PARAM_CPU_COUNT = "cpuCount";

    public static final String REQ_PARAM_STORAGE_SIZE_TB = "storageSizeTBs";

    public static final String REQ_PARAM_PASSWORD = "password";

    private static final String REQ_PARAM_TAGS = "freeFormTags";

    public static final String BINDING_PARAM_WALLET_PASSWORD = "walletPassword";

    public static final String BINDING_RES_PARAM_USER_NAME = "user_name";

    /**
     * Available License Type options.
     * NEW - Include a new Cloud License with the service.
     * BYOL - Bring Your Own License.
     */
    public enum LicenseModel {
        BYOL, NEW, UNKNOWN
    }

    /**
     * Available Autonomous Database Workload options.
     * OLTP - Autonomous Transaction Processing Database.
     * DW   - Autonomous Data Warehouse Database.
     */
    public enum DBWorkloadType {
        ATP, ADW
    }

    private static final String DEFAULT_DB_USER_NAME = "ADMIN";

    private final String ADB_INSTANCE_ID = getInstanceTypeString() + "-Id";

    private AuthenticationDetailsProvider provider;

    public AutonomousDatabaseAdapter() {
        provider = new SystemPropsAuthProvider().getAuthProvider();
    }

    protected abstract String getInstanceTypeString();

    protected abstract String getCatalogFileName();

    @Override
    public Catalog getCatalog() throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream stream = AutonomousDatabaseAdapter.class.getClassLoader().getResourceAsStream(getCatalogFileName());
        return mapper.readValue(stream, Catalog.class);
    }

    @Override
    public ServiceInstanceStatus getOciServiceInstanceStatus(String instanceId, ServiceInstanceProvisionRequest body) {
        Map reqParams = RequestUtil.validateParamsExists(body.getParameters());
        boolean isProvisioningRequired =
                RequestUtil.getBooleanParameterDefaultValueTrue(reqParams, Constants.PROVISIONING, false);

        if (isProvisioningRequired) {
            return getOciServiceInstanceStatusForProvisioning(instanceId, reqParams);
        } else {
            return getOciServiceInstanceStatusForBinding(reqParams);
        }
    }

    private ServiceInstanceStatus getOciServiceInstanceStatusForProvisioning(String instanceId, Map reqParams) {

        String compartmentId = RequestUtil.getNonEmptyStringParameter(reqParams, REQ_PARAM_COMPARTMENT_ID);
        String name = RequestUtil.getNonEmptyStringParameter(reqParams, REQ_PARAM_NAME);

        try (AutonomousDatabaseOCIClient adbServiceClient = new AutonomousDatabaseOCIClient(provider, compartmentId)) {
            AutonomousDatabaseInstance autonomousDatabaseInstance = instanceExists(adbServiceClient
                    .listInstances(compartmentId, name), instanceId);
            if (autonomousDatabaseInstance == null) {
                return ServiceInstanceStatus.DOESNOTEXIST;
            } else {
                if (isSameInstance(autonomousDatabaseInstance, reqParams)) {
                    return ServiceInstanceStatus.EXISTS;
                } else {
                    return ServiceInstanceStatus.CONFLICT;
                }
            }
        }
    }

    private ServiceInstanceStatus getOciServiceInstanceStatusForBinding(Map reqParams) {

        String ocId = RequestUtil.getNonEmptyStringParameter(reqParams, Constants.OCID);
        if(!RequestUtil.isValidOCID(ocId)) {
            throw Errors.invalidParameter(Constants.OCID);
        }
        String compartmentId = RequestUtil.getNonEmptyStringParameter(reqParams, REQ_PARAM_COMPARTMENT_ID);

        try (AutonomousDatabaseOCIClient adbServiceClient = new AutonomousDatabaseOCIClient(provider, compartmentId)) {
            AutonomousDatabaseInstance autonomousDatabaseInstance = adbServiceClient.get(ocId);
            if (autonomousDatabaseInstance == null) {
                return ServiceInstanceStatus.DOESNOTEXIST;
            } else {
                return ServiceInstanceStatus.EXISTS;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ServiceInstanceProvision provisionExistingServiceInstance(String instanceId,
                                                                     ServiceInstanceProvisionRequest body) {
        ServiceInstanceProvision response = new ServiceInstanceProvision();
        String serviceId = body.getServiceId();
        String planId = body.getPlanId();

        Map reqParams = RequestUtil.validateParamsExists(body.getParameters());
        boolean isProvisioningRequired =
                RequestUtil.getBooleanParameterDefaultValueTrue(reqParams, Constants.PROVISIONING, false);
        String compartmentId = RequestUtil.getNonEmptyStringParameter(reqParams, REQ_PARAM_COMPARTMENT_ID);

        try (AutonomousDatabaseOCIClient adbServiceClient = new AutonomousDatabaseOCIClient(provider, compartmentId)) {

            //Provision
            String name = RequestUtil.getNonEmptyStringParameter(reqParams, REQ_PARAM_NAME);

            ServiceData adbSvcData = new ServiceData();
            adbSvcData.setServiceId(serviceId);
            adbSvcData.setPlanId(planId);
            adbSvcData.setCompartmentId(compartmentId);
            adbSvcData.setProvisioning(isProvisioningRequired);

            //Check oci and provision if instance doesn't exists already
            if (isProvisioningRequired) {
                AutonomousDatabaseInstance autonomousDatabaseInstance = instanceExists(adbServiceClient
                        .listInstances(compartmentId, name), instanceId);

                if (autonomousDatabaseInstance != null) {
                    //instance already exists!
                    adbSvcData.setOcid(autonomousDatabaseInstance.getId());
                    adbSvcData.putMetadata(Constants.DB_WORKLOAD_TYPE, autonomousDatabaseInstance.getDbWorkloadType().toString());
                    response.setSvcData(adbSvcData);
                    if (autonomousDatabaseInstance.getLifecycleState() == AutonomousDatabaseInstance.LifecycleState.Available) {
                        response.setStatusCode(Response.Status.OK.getStatusCode());
                    } else {
                        response.setStatusCode(Response.Status.ACCEPTED.getStatusCode());
                    }
                }
            } else {
                String ocId = RequestUtil.getNonEmptyStringParameter(reqParams, Constants.OCID);
                adbSvcData.setOcid(ocId);
                adbSvcData.putMetadata(Constants.DB_WORKLOAD_TYPE, getInstanceTypeString());
                response.setSvcData(adbSvcData);
                response.setStatusCode(Response.Status.OK.getStatusCode());
            }
            return response;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Supports both Synchronous and Asynchronous Provisioning of an instance. Response contains an HTTP
     * ServiceInstanceStatus code that indicates the provisioning type. A status code of 200 indicates the
     * provisioning is completed and 202 indicates asynchronous provisioning.
     */
    @Override
    @SuppressWarnings("unchecked")
    public ServiceInstanceProvision provisionServiceInstance(String instanceId, ServiceInstanceProvisionRequest body,
                                                             Map<String, String> freeFormTags) {

        String serviceId = body.getServiceId();
        String planId = body.getPlanId();
        LOGGER.finest("Provision request invoked");
        ServiceInstanceProvision response = new ServiceInstanceProvision();
        Map reqParams = RequestUtil.validateParamsExists(body.getParameters());
        String compartmentId = RequestUtil.getNonEmptyStringParameter(reqParams, REQ_PARAM_COMPARTMENT_ID);

        try (AutonomousDatabaseOCIClient adbServiceClient = new AutonomousDatabaseOCIClient(provider, compartmentId)) {

            //Provision
            String name = RequestUtil.getNonEmptyStringParameter(reqParams, REQ_PARAM_NAME);
            String dbName = RequestUtil.getNonEmptyStringParameter(reqParams, REQ_PARAM_DB_NAME);
            Integer cpuCount = RequestUtil.getIntegerParameter(reqParams, REQ_PARAM_CPU_COUNT, true);
            Integer storageSize = RequestUtil.getIntegerParameter(reqParams, REQ_PARAM_STORAGE_SIZE_TB, true);
            String password = RequestUtil.getNonEmptyStringParameter(reqParams, REQ_PARAM_PASSWORD);
            String licenseModelStr = RequestUtil.getNonEmptyStringParameter(reqParams, REQ_PARAM_LICENSE_MODEL);
            boolean autoScalingEnabled = RequestUtil.getBooleanParameterDefaultValueFalse(reqParams,
                    Constants.AUTOSCALING_ENABLED, false);
            Map<String, Map<String, Object>> definedTags = RequestUtil.getMapMapObjectParameter(reqParams, Constants
                    .DEFINED_TAGS, false);
            if (!(LicenseModel.NEW.toString().equalsIgnoreCase(licenseModelStr) || LicenseModel.BYOL.toString()
                    .equalsIgnoreCase(licenseModelStr))) {
                LOGGER.severe("Invalid License Model : " + licenseModelStr);
                throw Errors.invalidParameter(REQ_PARAM_LICENSE_MODEL);
            }

            LicenseModel licenseModel = LicenseModel.valueOf(licenseModelStr.toUpperCase());
            boolean isLicenseIncluded = (licenseModel == LicenseModel.NEW);

            ServiceData adbSvcData = new ServiceData();
            adbSvcData.setServiceId(serviceId);
            adbSvcData.setPlanId(planId);
            adbSvcData.setCompartmentId(compartmentId);
            adbSvcData.setProvisioning(true);
            adbSvcData.putMetadata(Constants.DB_WORKLOAD_TYPE, getInstanceTypeString().toUpperCase());

            AutonomousDatabaseInstance autonomousDatabaseInstance = adbServiceClient.create(name, dbName,
                    getDBWorkload(getInstanceTypeString()), cpuCount, storageSize, freeFormTags, definedTags, password,
                    isLicenseIncluded, autoScalingEnabled);

            adbSvcData.setOcid(autonomousDatabaseInstance.getId());
            response.setStatusCode(HTTP_ACCEPTED);

            if (autonomousDatabaseInstance.getId() == null || "".equals(autonomousDatabaseInstance.getId().trim())) {
                LOGGER.severe("OCID not  found in the create response!!!");
            }
            response.setSvcData(adbSvcData);
        }

        return response;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Supports both Synchronous and Asynchronous update of an instance.
     * Response contains an HTTP ServiceInstanceStatus code that indicates the type. A status
     * code of 200 indicates that update is complete and 202 indicates
     * asynchronous update.
     */
    @Override
    public ServiceInstanceAsyncOperation updateServiceInstance(String instanceId, ServiceInstanceUpdateRequest body,
                                                               ServiceData svcData) {

        LOGGER.finest("Update request received.");
        ServiceInstanceAsyncOperation response = new ServiceInstanceAsyncOperation();
        String compartmentId = svcData.getCompartmentId();

        //Validate request
        if (body.getParameters() == null) {
            // no changes in parameters. Hence return success.
            LOGGER.fine("parameters in the request body is empty. Update not required");
            response.setStatusCode(HTTP_OK);
            return response;
        } else {
            if (!(body.getParameters() instanceof Map)) {
                LOGGER.severe("parameters value in the request body is not a Map!");
                throw Errors.invalidParameters();
            }
        }

        Map<String, Object> params = (Map<String, Object>) body.getParameters();

        String name = RequestUtil.getStringParameter(params, REQ_PARAM_NAME, false);
        Integer cpuCount = RequestUtil.getIntegerParameter(params, REQ_PARAM_CPU_COUNT, false);
        Integer storageSize = RequestUtil.getIntegerParameter(params, REQ_PARAM_STORAGE_SIZE_TB, false);
        Map<String, String> tags = RequestUtil.getMapStringParameter(params, REQ_PARAM_TAGS, false);
        String licenseModelStr = RequestUtil.getNonEmptyStringParameter(params, REQ_PARAM_LICENSE_MODEL);
        boolean autoScalingEnabled = RequestUtil.getBooleanParameterDefaultValueFalse(params, Constants.AUTOSCALING_ENABLED, false);

        if (licenseModelStr != null && (!(LicenseModel.NEW.toString().equalsIgnoreCase(licenseModelStr) || LicenseModel.BYOL.toString()
                .equalsIgnoreCase(licenseModelStr)))) {
            LOGGER.severe("Invalid License Model : " + licenseModelStr);
            throw Errors.invalidParameter(REQ_PARAM_LICENSE_MODEL);
        }

        if(!tags.isEmpty()) {
            tags.put(Constants.OSB_INSTANCE_ID_LABEL, instanceId);
        }

        Map<String, Map<String, Object>> definedTags = RequestUtil.getMapMapObjectParameter(params, Constants
                .DEFINED_TAGS, false);

        try (AutonomousDatabaseOCIClient adbServiceClient = new AutonomousDatabaseOCIClient(provider, compartmentId)) {
            adbServiceClient.update(svcData.getOcid(), name, cpuCount, storageSize, tags, definedTags, licenseModelStr, autoScalingEnabled);
            response.setStatusCode(HTTP_ACCEPTED);
        } catch(UpdateNotRequiredException ue) {
            response.setStatusCode(HTTP_OK);
        }

        if (response.getStatusCode() == HTTP_ACCEPTED) {
            response.setOperation(Constants.UPDATE_OPERATION);
        }

        return response;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Supports both Synchronous and Asynchronous delete of an instance.
     * Response contains an HTTP ServiceInstanceStatus code that indicates the type. A status
     * code of 200 indicates that delete is complete and 202 indicates
     * asynchronous delete.
     */
    @Override
    public AsyncOperation deleteServiceInstance(String instanceId, String serviceDefinitionId, String planId,
                                                ServiceData svcData) {
        debugLog(LOGGER, "Delete request received. instanceId: %s", Level.FINER, instanceId);
        AsyncOperation response = new AsyncOperation();

        //Check data store
        String compartmentId = svcData.getCompartmentId();

        try (AutonomousDatabaseOCIClient adbServiceClient = new AutonomousDatabaseOCIClient(provider, compartmentId)) {
            adbServiceClient.delete(svcData.getOcid());
            response.setStatusCode(HTTP_ACCEPTED);
        } catch (BmcException x) {
            if (x.getStatusCode() == HTTP_NOT_FOUND || (x.getStatusCode() == HTTP_CONFLICT && x.getMessage().contains
                    ("conflicting state of TERMINATED"))) {
                response.setStatusCode(HTTP_OK);
            } else if (x.getStatusCode() == HTTP_CONFLICT &&
                    x.getMessage().contains("conflicting state of TERMINATING")) {
                response.setStatusCode(HTTP_ACCEPTED);
            } else {
                LOGGER.log(Level.SEVERE, getInstanceTypeString() + " instance deletion failed", x);
                throw new RuntimeException(" instance deletion failed");
            }
        }

        if (response.getStatusCode() == HTTP_ACCEPTED) {
            response.setOperation(Constants.DELETE_OPERATION);
        }
        return response;
    }


    @Override
    public LastOperationResource getLastOperation(String instanceId, String serviceDefinitionId, String planId,
                                                  String operation, ServiceData svcData) {
        debugLog(LOGGER, "Get last operation request received. instanceId: %s;operation: %s", Level.FINER, instanceId,
                operation);

        LastOperationResource response = new LastOperationResource();
        String compartmentId = svcData.getCompartmentId();
        AutonomousDatabaseInstance autonomousDatabaseInstance;

        //Get instance details from OCI. If instance not found and it is delete operation then it is considered success.
        try (AutonomousDatabaseOCIClient adbServiceClient = new AutonomousDatabaseOCIClient(provider, compartmentId)) {
            autonomousDatabaseInstance = adbServiceClient.get(svcData.getOcid());
        } catch (Exception x) {
            if ( x instanceof  BmcException && ((BmcException)  x).getStatusCode() == HTTP_NOT_FOUND && Constants
                    .DELETE_OPERATION.equals(operation)) {
                debugLog(LOGGER, "ServiceData for instanceId %s removed from datastore ", Level.FINER, instanceId);
                response.setStatusCode(HTTP_GONE);
            } else {
                LOGGER.log(Level.WARNING, "Error in getting instance details for instancId " + instanceId, x);
                response.setState(LastOperationResource.StateEnum.IN_PROGRESS);
                response.setStatusCode(HTTP_OK);
            }
            return response;
        }

        debugLog(LOGGER, getInstanceTypeString() + " instance = %s", Level.FINER, autonomousDatabaseInstance);
        AutonomousDatabaseInstance.LifecycleState state = autonomousDatabaseInstance.getLifecycleState();
        response.setDescription(state.getValue());

        if (Constants.PROVISION_OPERATION.equals(operation) || Constants.UPDATE_OPERATION.equals(operation)) {

            if (state == AutonomousDatabaseInstance.LifecycleState.Available) {
                response.setState(LastOperationResource.StateEnum.SUCCEEDED);
            } else if (state == AutonomousDatabaseInstance.LifecycleState.AvailableNeedsAttention || state ==
                    AutonomousDatabaseInstance.LifecycleState.Terminated || state == AutonomousDatabaseInstance
                    .LifecycleState.Unavailable) {
                response.setState(LastOperationResource.StateEnum.FAILED);
            } else {
                response.setState(LastOperationResource.StateEnum.IN_PROGRESS);
            }

        } else if (Constants.DELETE_OPERATION.equals(operation)) {

            if (state == AutonomousDatabaseInstance.LifecycleState.Terminated) {
                response.setState(LastOperationResource.StateEnum.SUCCEEDED);
            } else if (state == AutonomousDatabaseInstance.LifecycleState.Available || state ==
                    AutonomousDatabaseInstance.LifecycleState.AvailableNeedsAttention) {
                response.setState(LastOperationResource.StateEnum.FAILED);
            } else {
                response.setState(LastOperationResource.StateEnum.IN_PROGRESS);
            }

        } else {

            response.setState(LastOperationResource.StateEnum.FAILED);
            response.setDescription("operation cannot be empty");
        }
        response.setStatusCode(HTTP_OK);
        return response;
    }

    @Override
    public ServiceInstanceResource getServiceInstance(ServiceData serviceData) {
        String instanceId = serviceData.getInstanceId();
        debugLog(LOGGER, "Get request received. instanceId: {}", Level.FINER, instanceId);

        Map<String, String> resParams = new HashMap<>();
        resParams.put(ADB_INSTANCE_ID, serviceData.getOcid());

        ServiceInstanceResource response = new ServiceInstanceResource().serviceId(serviceData.getServiceId()).planId
                (serviceData.getPlanId()).parameters(resParams);
        response.setStatusCode(HTTP_OK);
        return response;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Supports only synchronous mode. The credentials files are downloaded and
     * base64 decoded to convert to string and there by making it easier for
     * inclusion in a JSON response.
     */
    @Override
    public ServiceBinding bindToService(String instanceId, String bindingId, ServiceBindingRequest request,
                                        ServiceData svcData) {

        LOGGER.finest("Service bind request received.");

        ServiceBinding response = new ServiceBinding();
        Map reqParams = RequestUtil.validateParamsExists(request.getParameters());

        String walletPassword = RequestUtil.getNonEmptyStringParameter(reqParams, BINDING_PARAM_WALLET_PASSWORD);
        String compartmentId = svcData.getCompartmentId();

        AutonomousDatabaseInstance autonomousDatabaseInstance;
        try (AutonomousDatabaseOCIClient adbServiceClient = new AutonomousDatabaseOCIClient(provider, compartmentId)) {
            autonomousDatabaseInstance = adbServiceClient.get(svcData.getOcid());
            Map<String, String> creds = adbServiceClient.getCredentials(autonomousDatabaseInstance
                    .getId(), autonomousDatabaseInstance.getDbName(), walletPassword);
            creds.put(BINDING_RES_PARAM_USER_NAME, DEFAULT_DB_USER_NAME);
            response.setCredentials(creds);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        BindingData data = new BindingData();
        data.setBindingId(bindingId);
        data.setInstanceId(instanceId);
        data.setPlanId(request.getPlanId());
        data.setServiceId(request.getServiceId());
        response.setBindingData(data);
        response.setStatusCode(HTTP_OK);
        return response;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This operation is not supported!
     */
    @Override
    public ServiceBindingResource getServiceBinding(String bindingId, ServiceData svcData) {
        ServiceBindingResource response = new ServiceBindingResource();
        response.setStatusCode(HTTP_BAD_REQUEST);
        return response;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Service Binding is supported only in synchronous mode and hence this
     * operation is not supported!
     */
    @Override
    public LastOperationResource getLastBindingOperation(String instanceId, String bindingId, String
            serviceDefinitionId, String planId, ServiceData svcData, BindingData bindingData) {
        throw Errors.bindingSynchronousError();
    }

    @Override
    public LastOperationResource deleteServiceBinding(String instanceId, String bindingId, String
            serviceDefinitionId, String planId, ServiceData svcData, BindingData bindingData) {
        LastOperationResource response = new LastOperationResource().state(LastOperationResource.StateEnum.SUCCEEDED)
                .description("Completed");
        response.setStatusCode(HTTP_OK);
        return response;
    }

    private AutonomousDatabaseInstance instanceExists(List<AutonomousDatabaseInstance> instanceList, String instanceId) {
        if (instanceList != null) {
            for (AutonomousDatabaseInstance instance : instanceList) {
                Map<String, String> tags = instance.getFreeformTags();
                for (Map.Entry<String, String> e : tags.entrySet()) {
                    if (e.getKey().equals(Constants.OSB_INSTANCE_ID_LABEL) && e.getValue().equals(instanceId)) {
                        String clusterIdTagValue = tags.get(Constants.CLUSTER_ID_TAG);
                        String clusterId = System.getProperty(Constants.CLUSTER_ID_TAG);
                        if (Utils.isNullOrEmptyString(clusterIdTagValue) || Utils
                                .isNullOrEmptyString(clusterId) || clusterId.equals(clusterIdTagValue)) {
                            return instance;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isSameInstance(AutonomousDatabaseInstance instance, Map<String, Object> reqParams) {
        //The Parameters are not mentioned as mandatory here as we don't want to do validation in an utility method.
        String displayName = RequestUtil.getStringParameter(reqParams, REQ_PARAM_NAME, false);
        String dbName = RequestUtil.getStringParameter(reqParams, REQ_PARAM_DB_NAME, false);
        Integer cpuCount = RequestUtil.getIntegerParameter(reqParams, REQ_PARAM_CPU_COUNT, false);
        Integer storageSize = RequestUtil.getIntegerParameter(reqParams, REQ_PARAM_STORAGE_SIZE_TB, false);
        String licenseModel = RequestUtil.getStringParameter(reqParams, REQ_PARAM_LICENSE_MODEL, false);
        boolean isAutoScalingEnabled = RequestUtil.getBooleanParameterDefaultValueFalse(reqParams,
                                                                        Constants.AUTOSCALING_ENABLED, false);
        if (instance.getDisplayName().equals(displayName) && instance.getCpuCoreCount() == cpuCount &&
                instance.getStorageSizeInGBs() == storageSize && instance.getDbName().equals(dbName) && instance
                .getLicenseModel().toString().equalsIgnoreCase(licenseModel) && instance.isAutoScalingEnabled() == isAutoScalingEnabled) {
            return true;
        }
        return false;
    }

    public static class UpdateNotRequiredException extends RuntimeException {
    }

    private CreateAutonomousDatabaseBase.DbWorkload getDBWorkload(String dbWorkloadType) {
        if (dbWorkloadType.equalsIgnoreCase(DBWorkloadType.ATP.toString())) {
            return CreateAutonomousDatabaseBase.DbWorkload.Oltp;
        } else {
            return CreateAutonomousDatabaseBase.DbWorkload.Dw;
        }
    }
}



