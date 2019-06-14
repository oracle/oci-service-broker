/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapters.objectstorage;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.bmc.Region;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.*;
import com.oracle.bmc.objectstorage.requests.*;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import com.oracle.bmc.objectstorage.responses.ListPreauthenticatedRequestsResponse;
import com.oracle.oci.osb.adapter.ServiceAdapter;
import com.oracle.oci.osb.model.*;
import com.oracle.oci.osb.ociclient.SystemPropsAuthProvider;
import com.oracle.oci.osb.store.BindingData;
import com.oracle.oci.osb.store.ServiceData;
import com.oracle.oci.osb.util.Constants;
import com.oracle.oci.osb.util.Errors;
import com.oracle.oci.osb.util.RequestUtil;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ObjectStorageServiceAdapter provides implementation to provision and manage
 * Object storage buckets.
 */
public class ObjectStorageServiceAdapter implements ServiceAdapter {
    public static final String NAME = "name";
    public static final String COMPARTMENT_ID = "compartmentId";
    public static final String NAMESPACE = "namespace";
    public static final String STORAGE_TIER = "storageTier";
    public static final String PUBLIC_ACCESS_TYPE = "publicAccessType";
    public static final String BUCKET_NAME = "bucketName";
    public static final String GENERATE_PRE_AUTH = "generatePreAuth";
    public static final String PRE_AUTH_ID = "preAuthId";
    public static final String EXPIRY_TIME = "expiryTime";
    public static final String PRE_AUTH_ACCESS_URI = "preAuthAccessUri";

    private final ObjectStorageClient objectStorageClient;

    private Catalog catalog = null;

    public ObjectStorageServiceAdapter() {
        super();
        objectStorageClient = new ObjectStorageClient(
                new SystemPropsAuthProvider().getAuthProvider());
        objectStorageClient
                .setRegion(Region.fromRegionId(System.getProperty(Constants.REGION_ID)));
    }

    @Override
    public Catalog getCatalog() throws IOException {
        if (catalog == null) {
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            InputStream stream = ObjectStorageServiceAdapter.class.getClassLoader()
                    .getResourceAsStream("objectstore-catalog.json");
            catalog = mapper.readValue(stream, Catalog.class);
        }
        return catalog;
    }

    @Override
    public ServiceInstanceStatus getOciServiceInstanceStatus(String instanceId, ServiceInstanceProvisionRequest body)
            throws BmcException {
        try {
            Map mapParameters = RequestUtil.validateParamsExists(body.getParameters());
            String bucketName = RequestUtil.getNonEmptyStringParameter(mapParameters, NAME);
            String namespace = RequestUtil.getNonEmptyStringParameter(mapParameters, NAMESPACE);
            boolean isProvisioningRequired = RequestUtil.getBooleanParameterDefaultValueTrue(mapParameters, Constants.PROVISIONING, false);

            Bucket bucket = objectStorageClient
                    .getBucket(GetBucketRequest.builder().bucketName(bucketName)
                            .namespaceName(namespace).build()).getBucket();

            if(!isProvisioningRequired) { // it's for just binding request where instance already provisioned so just verifying bucket
                if (bucket != null) {
                    return ServiceInstanceStatus.EXISTS;
                } else {
                    return ServiceInstanceStatus.DOESNOTEXIST;
                }
            } else {
                Map<String, String> tags = bucket.getFreeformTags();
                if (tags == null ||
                        !instanceId.equals(tags.get(Constants.OSB_INSTANCE_ID_LABEL)) || !(sameInstance(mapParameters, bucket))) {
                    return ServiceInstanceStatus.CONFLICT;
                } else {
                    return ServiceInstanceStatus.EXISTS;
                }
            }
        } catch (BmcException e) {
            if (e.getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
                return ServiceInstanceStatus.DOESNOTEXIST;
            } else {
                throw e;
            }
        }
    }

    @Override
    public ServiceInstanceProvision provisionExistingServiceInstance(String instanceId, ServiceInstanceProvisionRequest body) {
        ServiceInstanceProvision response = new ServiceInstanceProvision();
        Map mapParameters = (Map) body.getParameters();

        String bucketName = RequestUtil.getNonEmptyStringParameter(mapParameters, NAME);
        String compartmentId = RequestUtil.getNonEmptyStringParameter(mapParameters, COMPARTMENT_ID);
        String namespace = RequestUtil.getNonEmptyStringParameter(mapParameters, NAMESPACE);
        boolean isProvisioningRequired = RequestUtil
                .getBooleanParameterDefaultValueTrue(mapParameters, Constants.PROVISIONING, false);

        response.setSvcData(getSvcData(instanceId, body, bucketName, compartmentId, namespace, isProvisioningRequired));
        response.setStatusCode(Response.Status.OK.getStatusCode());

        return response;
    }

    @Override
    public ServiceInstanceProvision provisionServiceInstance(String instanceId, ServiceInstanceProvisionRequest body,
                                                             Map<String, String> freeFormTags) {
        ServiceInstanceProvision response = new ServiceInstanceProvision();
        Map mapParameters = (Map) body.getParameters();

        String bucketName = RequestUtil
                .getStringParameter(mapParameters, NAME, true);
        String compartmentId = RequestUtil
                .getStringParameter(mapParameters, COMPARTMENT_ID, true);
        String namespace = RequestUtil
                .getStringParameter(mapParameters, NAMESPACE, true);
        CreateBucketDetails.StorageTier storageTier = body.getPlanId().equals("k1d643051-c407-4f3f-8527-82cee9ab45f6")
                ? CreateBucketDetails.StorageTier.Standard
                : CreateBucketDetails.StorageTier.Archive;

        Map<String, String> metadataMap = RequestUtil
                .getMapStringParameter(mapParameters, Constants.METADATA, false);
        Map<String, Map<String, Object>> definedTags = RequestUtil
                .getMapMapObjectParameter(mapParameters,
                        Constants.DEFINED_TAGS, false);
        String publicAccessType = RequestUtil
                .getStringParameter(mapParameters, PUBLIC_ACCESS_TYPE, false);

        CreateBucketDetails.Builder createBucketDetailsBuilder = CreateBucketDetails
                .builder()
                .name(bucketName).compartmentId(compartmentId)
                .metadata(metadataMap)
                .definedTags(definedTags)
                .freeformTags(freeFormTags)
                .storageTier(storageTier);

        if (publicAccessType != null) {
            createBucketDetailsBuilder.publicAccessType(
                    CreateBucketDetails.PublicAccessType.valueOf(publicAccessType));
        }

        objectStorageClient.createBucket(CreateBucketRequest.builder()
                .namespaceName(namespace)
                .createBucketDetails(createBucketDetailsBuilder.build()).build());

        response.setSvcData(getSvcData(instanceId, body, bucketName, compartmentId, namespace, true));

        response.setStatusCode(Response.Status.CREATED.getStatusCode());
        return response;
    }

    @Override
    public ServiceInstanceAsyncOperation updateServiceInstance(String instanceId,
                                                               ServiceInstanceUpdateRequest body, ServiceData svcData) {
        Map mapParameters = RequestUtil.validateParamsExists(body.getParameters());

        Map<String, String> freeFormTags = RequestUtil
                .getMapStringParameter(mapParameters, Constants.FREE_FORM_TAGS,
                        false);
        Map<String, String> metadataMap = RequestUtil
                .getMapStringParameter(mapParameters, Constants.METADATA,
                        false);
        Map<String, Map<String, Object>> definedTags = RequestUtil
                .getMapMapObjectParameter(mapParameters,
                        Constants.DEFINED_TAGS, false);
        String publicAccessType = RequestUtil
                .getStringParameter(mapParameters, PUBLIC_ACCESS_TYPE, false);

        UpdateBucketDetails.Builder details = UpdateBucketDetails.builder();

        details.definedTags(definedTags).freeformTags(freeFormTags)
                .metadata(metadataMap);

        if (publicAccessType != null) {
            details.publicAccessType(
                    UpdateBucketDetails.PublicAccessType.valueOf(publicAccessType));
        }

        objectStorageClient.updateBucket(UpdateBucketRequest.builder()
                .namespaceName(svcData.getMetadata(NAMESPACE))
                .bucketName(svcData.getMetadata(BUCKET_NAME))
                .updateBucketDetails(details.build()).build());

        ServiceInstanceAsyncOperation response = new ServiceInstanceAsyncOperation();
        response.setStatusCode(Response.Status.OK.getStatusCode());

        return response;
    }

    @Override
    public LastOperationResource getLastOperation(String instanceId,
                                                  String serviceDefinitionId, String planId, String operation, ServiceData svcData) {
        throw Errors.provisionSynchronousError();
    }

    @Override
    public ServiceInstanceResource getServiceInstance(ServiceData svcData) {
        ServiceInstanceResource response = new ServiceInstanceResource();
        response.setServiceId(svcData.getServiceId());
        Map<String, String> metadata = new HashMap<>();

        metadata.put(BUCKET_NAME, svcData.getMetadata(BUCKET_NAME));
        metadata.put(NAMESPACE, svcData.getMetadata(NAMESPACE));
        metadata.put(Constants.COMPARTMENT_ID, svcData.getCompartmentId());

        response.setStatusCode(Response.Status.OK.getStatusCode());
        response.setParameters(metadata);
        return response;
    }

    @Override
    public AsyncOperation deleteServiceInstance(String instanceId,
                                                String serviceDefinitionId, String planId, ServiceData svcData) {
        AsyncOperation response = new AsyncOperation();
        objectStorageClient.deleteBucket(DeleteBucketRequest.builder()
                .bucketName(svcData.getMetadata(BUCKET_NAME))
                .namespaceName(svcData.getMetadata(NAMESPACE)).build());
        response.setStatusCode(Response.Status.OK.getStatusCode());

        return response;
    }

    @Override
    public ServiceBinding bindToService(String instanceId, String bindingId,
                                        ServiceBindingRequest request, ServiceData svcData) {
        Object parameters = request.getParameters();

        if (parameters != null && !(parameters instanceof Map)) {
            throw Errors.invalidParameters();
        }

        Map mapParameters = (Map) parameters;
        String preAuthToken = null;
        String preAuthId = null;

        if (mapParameters != null && mapParameters.size() > 0) {
            CreatePreauthenticatedRequestDetails.Builder bldrPreAuth = CreatePreauthenticatedRequestDetails
                    .builder();
            String preAuthRequired = RequestUtil
                    .getStringParameter(mapParameters, GENERATE_PRE_AUTH,
                            false);
            if (preAuthRequired != null && Boolean.parseBoolean(preAuthRequired)) {

                ListPreauthenticatedRequestsRequest listPreAuth = ListPreauthenticatedRequestsRequest
                        .builder()
                        .bucketName(svcData.getMetadata(BUCKET_NAME))
                        .namespaceName(svcData.getMetadata(NAMESPACE)).build();

                ListPreauthenticatedRequestsResponse listResp =
                        objectStorageClient.listPreauthenticatedRequests(listPreAuth);

                Optional<PreauthenticatedRequestSummary> existingRequest =
                        listResp.getItems().stream()
                                .filter(item -> item.getName().equals(bindingId)).findFirst();

                if (existingRequest.isPresent()) {
                    // if it exist in opc and not in our store, it means the response could not be sent
                    // before storing in DB, so we need to delete the existing one first
                    objectStorageClient.deletePreauthenticatedRequest(
                            DeletePreauthenticatedRequestRequest.builder()
                                    .parId(existingRequest.get().getId())
                                    .bucketName(svcData.getMetadata(BUCKET_NAME))
                                    .namespaceName(svcData.getMetadata(NAMESPACE))
                                    .build());

                }

                String expiryTimeStr = RequestUtil
                        .getStringParameter(mapParameters, EXPIRY_TIME,
                                false);

                Date expiryTime = null;

                if (expiryTimeStr != null) {
                    try {
                        expiryTime = Date.from(OffsetDateTime.parse(expiryTimeStr, DateTimeFormatter.ISO_DATE_TIME).toInstant());
                    } catch (Exception e) {
                        expiryTime = null;
                    }
                }

                bldrPreAuth.name(bindingId).accessType(CreatePreauthenticatedRequestDetails.AccessType.AnyObjectWrite);
                if (expiryTime == null) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.YEAR, 20);
                    expiryTime = now.getTime();
                }

                bldrPreAuth.timeExpires(expiryTime);
                CreatePreauthenticatedRequestResponse preAuthResponse =
                        objectStorageClient.createPreauthenticatedRequest(
                                CreatePreauthenticatedRequestRequest.builder()
                                        .createPreauthenticatedRequestDetails(bldrPreAuth.build())
                                        .bucketName(svcData.getMetadata(BUCKET_NAME))
                                        .namespaceName(svcData.getMetadata(NAMESPACE))
                                        .build());

                PreauthenticatedRequest preAuth = preAuthResponse
                        .getPreauthenticatedRequest();
                preAuthToken = preAuth.getAccessUri();
                preAuthId = preAuth.getId();
            }
        }

        Map<String, String> credentials = new HashMap<>();
        ServiceBinding response = new ServiceBinding();
        response.setCredentials(credentials);
        response.setStatusCode(Response.Status.CREATED.getStatusCode());

        if (preAuthToken != null) {
            credentials.put(PRE_AUTH_ACCESS_URI, preAuthToken);
        }
        response.setBindingData(getBindingData(instanceId, bindingId, request, preAuthId));

        return response;
    }

    @Override
    public ServiceBindingResource getServiceBinding(String bindingId,
                                                    ServiceData svcData) {
        throw Errors.bindingNotFetchable();
    }

    @Override
    public LastOperationResource getLastBindingOperation(String instanceId,
                                                         String bindingId, String serviceDefinitionId, String planId, ServiceData svcData, BindingData bindingData) {
        throw Errors.bindingSynchronousError();
    }

    @Override
    public LastOperationResource deleteServiceBinding(String instanceId,
                                                      String bindingId, String serviceDefinitionId, String planId, ServiceData svcData, BindingData bindingData) {
        LastOperationResource response = new LastOperationResource();

        String preAuthId = bindingData.getMetadata(PRE_AUTH_ID);
        if (preAuthId != null) {
            objectStorageClient.deletePreauthenticatedRequest(
                    DeletePreauthenticatedRequestRequest.builder()
                            .parId(preAuthId)
                            .bucketName(svcData.getMetadata(BUCKET_NAME))
                            .namespaceName(svcData.getMetadata(NAMESPACE)).build());
        }
        response.setStatusCode(Response.Status.OK.getStatusCode());
        return response;
    }

    private BindingData getBindingData(String instanceId,
                                 String bindingId,
                                 ServiceBindingRequest request,
                                 String preAuthId) {
        BindingData bindingData = new BindingData();
        bindingData.setInstanceId(instanceId);
        bindingData.setBindingId(bindingId);
        bindingData.setPlanId(request.getPlanId());
        bindingData.setServiceId(request.getServiceId());
        bindingData.putMetadata(PRE_AUTH_ID, preAuthId);
        return bindingData;
    }

    private ServiceData getSvcData(String instanceId,
                             ServiceInstanceProvisionRequest body,
                             String bucketName,
                             String compartmentId,
                             String namespace, boolean provisioningRequired) {
        ServiceData svcData = new ServiceData();
        svcData.setInstanceId(instanceId);
        svcData.setPlanId(body.getPlanId());
        svcData.setServiceId(body.getServiceId());
        svcData.putMetadata(BUCKET_NAME, bucketName);
        svcData.putMetadata(NAMESPACE, namespace);
        svcData.setCompartmentId(compartmentId);
        svcData.setProvisioning(provisioningRequired);
        return svcData;
    }

    private boolean sameInstance(Map mapParameters, Bucket bucket) {
        String strPublicAccessType = RequestUtil
                .getStringParameter(mapParameters, PUBLIC_ACCESS_TYPE, false);
        Bucket.PublicAccessType accessType = strPublicAccessType == null
                ? Bucket.PublicAccessType.NoPublicAccess
                : Bucket.PublicAccessType.valueOf(strPublicAccessType);
        if(accessType != bucket.getPublicAccessType()) {
            return false;
        }
        return true;
    }
}
