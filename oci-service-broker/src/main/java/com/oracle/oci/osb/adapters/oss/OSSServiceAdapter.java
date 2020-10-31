/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapters.oss;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.oci.osb.adapter.ServiceAdapter;
import com.oracle.oci.osb.model.*;
import com.oracle.oci.osb.model.Error;
import com.oracle.oci.osb.ociclient.SystemPropsAuthProvider;
import com.oracle.oci.osb.store.BindingData;
import com.oracle.oci.osb.store.ServiceData;
import com.oracle.oci.osb.util.BrokerHttpException;
import com.oracle.oci.osb.util.Constants;
import com.oracle.oci.osb.util.Errors;
import com.oracle.oci.osb.util.RequestUtil;
import com.oracle.bmc.streaming.StreamAdminClient;
import com.oracle.bmc.streaming.model.CreateStreamDetails;
import com.oracle.bmc.streaming.model.Stream;
import com.oracle.bmc.streaming.model.StreamSummary;
import com.oracle.bmc.streaming.model.UpdateStreamDetails;
import com.oracle.bmc.streaming.requests.*;
import com.oracle.bmc.streaming.responses.CreateStreamResponse;
import com.oracle.bmc.streaming.responses.ListStreamsResponse;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static com.oracle.oci.osb.util.Utils.getLogger;

/**
 * OSSServiceAdapter provides implementation to provision and manage
 * Oracle Streaming Service instances.
 */
public class OSSServiceAdapter implements ServiceAdapter {

    private static final Logger LOGGER = getLogger(OSSServiceAdapter.class);

    public static final String PARTITIONS = "partitions";
    private static final String STREAM_ID = "streamId";
    private static final String MESSAGE_ENDPOINT = "messageEndpoint";

    private StreamAdminClient streamAdminClient;

    private Catalog catalog = null;


    public OSSServiceAdapter() {
        super();
        streamAdminClient = StreamAdminClient.builder().build(new SystemPropsAuthProvider().getAuthProvider());
        streamAdminClient.setRegion(System.getProperty(Constants.REGION_ID));
    }

    @Override
    public Catalog getCatalog() throws IOException {
        if (catalog == null) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            InputStream stream = OSSServiceAdapter.class.getClassLoader().getResourceAsStream("oss-catalog.json");
            catalog = mapper.readValue(stream, Catalog.class);
        }
        return catalog;
    }

    private ServiceInstanceStatus getOciServiceInstanceStatusForBinding(Map mapParameters ) {
        String ocID = RequestUtil.getNonEmptyStringParameter(mapParameters, Constants.OCID);
        Stream stream = null;
        try {
            stream = streamAdminClient
                    .getStream(GetStreamRequest.builder().streamId(ocID).build())
                    .getStream();
        } catch (Exception e) {
            return ServiceInstanceStatus.DOESNOTEXIST;
        }


        if (stream!=null && (stream.getLifecycleState() == Stream.LifecycleState.Active ||
                stream.getLifecycleState() == Stream.LifecycleState.Creating)) {
            return ServiceInstanceStatus.EXISTS;
        } else {
            return ServiceInstanceStatus.DOESNOTEXIST;
        }
    }

    private ServiceInstanceStatus getOciServiceInstanceStatusForProvisioning(String instanceId, Map mapParameters ) {
        String streamName = RequestUtil.getNonEmptyStringParameter(mapParameters, Constants.NAME);
        String compartmentId = RequestUtil.getNonEmptyStringParameter(mapParameters, Constants.COMPARTMENT_ID);
        ListStreamsResponse listResponse = streamAdminClient.listStreams(ListStreamsRequest.builder()
                .compartmentId(compartmentId).build());
        Optional<StreamSummary> summary = listResponse.getItems().stream()
                .filter(item -> item.getName().equals(streamName) && item.getLifecycleState() != StreamSummary.LifecycleState.Deleted).findFirst();
        if (summary.isPresent()) {
            StreamSummary stream = summary.get();
            Map<String, String> freeFormTagsExisting = stream.getFreeformTags();
            if (freeFormTagsExisting == null ||
                    !instanceId.equals(freeFormTagsExisting.get(Constants.OSB_INSTANCE_ID_LABEL)) || !sameInstance(stream, mapParameters)) {
                return ServiceInstanceStatus.CONFLICT;
            } else if (stream.getLifecycleState() == StreamSummary.LifecycleState.Active ||
                    stream.getLifecycleState() == StreamSummary.LifecycleState.Creating) {
                return ServiceInstanceStatus.EXISTS;
            }
        }
        return ServiceInstanceStatus.DOESNOTEXIST;
    }

    @Override
    public ServiceInstanceStatus getOciServiceInstanceStatus(String instanceId, com.oracle.oci.osb.model.ServiceInstanceProvisionRequest body) {
        Map mapParameters = validateParamsExists(body.getParameters());
        boolean isProvisioningRequired = RequestUtil.getBooleanParameterDefaultValueTrue(mapParameters, Constants.PROVISIONING, false);
        if(!isProvisioningRequired) {
            return getOciServiceInstanceStatusForBinding(mapParameters);
        } else {
            return getOciServiceInstanceStatusForProvisioning(instanceId,mapParameters);
        }
    }

    @Override
    public ServiceInstanceProvision provisionExistingServiceInstance(String instanceId,
                                                                     ServiceInstanceProvisionRequest body) {
        Map mapParameters = validateParamsExists(body.getParameters());
        ServiceInstanceProvision response = new ServiceInstanceProvision();

        boolean isProvisioningRequired = RequestUtil
                .getBooleanParameterDefaultValueTrue(mapParameters, Constants.PROVISIONING, false);

        if(isProvisioningRequired) {
            String streamName = RequestUtil
                    .getNonEmptyStringParameter(mapParameters, Constants.NAME);
            String compartmentId = RequestUtil
                    .getNonEmptyStringParameter(mapParameters, Constants.COMPARTMENT_ID);

            ListStreamsResponse listResponse = streamAdminClient.listStreams(ListStreamsRequest.builder()
                    .compartmentId(compartmentId).build());
            Optional<StreamSummary> summary = listResponse.getItems().stream()
                    .filter(item -> item.getName().equals(streamName)).findFirst();

            StreamSummary stream = summary.get();
            int statusCode = stream.getLifecycleState() == StreamSummary.LifecycleState.Active
                    ? Response.Status.OK.getStatusCode()
                    : Response.Status.ACCEPTED.getStatusCode();
            response.setStatusCode(statusCode);
            response.setSvcData(getSvcData(instanceId, body, compartmentId, summary.get().getId(), isProvisioningRequired));
            return response;
        } else {
            String ocID = RequestUtil.getNonEmptyStringParameter(mapParameters, Constants.OCID);
            Stream stream = streamAdminClient
                    .getStream(GetStreamRequest.builder().streamId(ocID).build())
                    .getStream();
            int statusCode = stream.getLifecycleState() == Stream.LifecycleState.Active
                    ? Response.Status.OK.getStatusCode()
                    : Response.Status.ACCEPTED.getStatusCode();
            response.setStatusCode(statusCode);
            response.setSvcData(getSvcData(instanceId, body, stream.getCompartmentId(), ocID, isProvisioningRequired));
            return response;
        }
    }

    private Map validateParamsExists(Object params){
        if (params == null || !(params instanceof Map)) {
            throw Errors.missingParameters();
        }
        return (Map) params;
    }

    @Override
    public ServiceInstanceProvision provisionServiceInstance(String instanceId, ServiceInstanceProvisionRequest body,
                                                             Map<String, String> freeFormTags) {
        ServiceInstanceProvision response = new ServiceInstanceProvision();

        Map mapParameters = validateParamsExists(body.getParameters());

        String streamName = RequestUtil.getNonEmptyStringParameter(mapParameters, Constants.NAME);
        Integer partitions = RequestUtil.getIntegerParameter(mapParameters, PARTITIONS, true);
        String streampoolId = RequestUtil.getStringParameter(mapParameters, Constants.STREAM_POOL_ID, false);;
        String compartmentId = RequestUtil.getStringParameter(mapParameters, Constants.COMPARTMENT_ID, false);
        Map<String, Map<String, Object>> definedTags = RequestUtil.getMapMapObjectParameter(mapParameters,
                        Constants.DEFINED_TAGS, false);

        if(streampoolId == null && compartmentId == null) {
            LOGGER.info("Both CompartmentId and StreamPoolId cannot be null");
            throw Errors.missingParameters();
        }

        CreateStreamDetails.Builder createStreamBuilder = CreateStreamDetails.builder().name(streamName)
                .partitions(partitions).definedTags(definedTags).freeformTags(freeFormTags);

        if(streampoolId != null) {
            createStreamBuilder.streamPoolId(streampoolId);
        } else {
            createStreamBuilder.compartmentId(compartmentId);
        }

        CreateStreamResponse createResponse = streamAdminClient.createStream(CreateStreamRequest.builder()
                .createStreamDetails(createStreamBuilder.build()).build());

        response.setSvcData(getSvcData(instanceId, body, compartmentId, createResponse.getStream().getId(), true));

        response.setStatusCode(Response.Status.ACCEPTED.getStatusCode());
        return response;
    }

    @Override
    public ServiceInstanceAsyncOperation updateServiceInstance(String instanceId,
                                                               ServiceInstanceUpdateRequest body, ServiceData svcData) {

        Map mapParameters = validateParamsExists(body.getParameters());

        Map<String, String> freeFormTags = RequestUtil
                .getMapStringParameter(mapParameters, Constants.FREE_FORM_TAGS,
                        false);
        Map<String, Map<String, Object>> definedTags = RequestUtil
                .getMapMapObjectParameter(mapParameters,
                        Constants.DEFINED_TAGS, false);

        UpdateStreamDetails updateStreamDetails =
                UpdateStreamDetails.builder().definedTags(definedTags).freeformTags(freeFormTags).build();

        streamAdminClient.updateStream(UpdateStreamRequest.builder().updateStreamDetails(updateStreamDetails)
                .streamId(svcData.getOcid()).build());

        ServiceInstanceAsyncOperation response = new ServiceInstanceAsyncOperation();
        response.setStatusCode(Response.Status.OK.getStatusCode());
        return response;
    }

    @Override
    public LastOperationResource getLastOperation(String instanceId,
                                                  String serviceDefinitionId,
                                                  String planId,
                                                  String operation,
                                                  ServiceData svcData) {
        LastOperationResource response = new LastOperationResource();
        if (operation.equals(Constants.PROVISION_OPERATION)) {
            String streamOcid = svcData.getOcid();
            Stream stream = streamAdminClient
                    .getStream(GetStreamRequest.builder().streamId(streamOcid).build())
                    .getStream();
            switch (stream.getLifecycleState()) {
                case Active:
                    response.setState(LastOperationResource.StateEnum.SUCCEEDED);
                    break;
                case Failed:
                    response.setState(LastOperationResource.StateEnum.FAILED);
                    break;
                case Creating:
                    response.setState(LastOperationResource.StateEnum.IN_PROGRESS);
                    break;
                default:
                    response.setState(LastOperationResource.StateEnum.FAILED);

            }

        } else if (operation.equals(Constants.DELETE_OPERATION)) {
            String streamOcid = svcData.getOcid();
            Stream stream = streamAdminClient
                    .getStream(GetStreamRequest.builder().streamId(streamOcid).build())
                    .getStream();
            switch (stream.getLifecycleState()) {
                case Deleted:
                    response.setState(LastOperationResource.StateEnum.SUCCEEDED);
                    break;
                case Deleting:
                    response.setState(LastOperationResource.StateEnum.IN_PROGRESS);
                    break;
                default:
                    response.setState(LastOperationResource.StateEnum.FAILED);
            }

        }
        response.setStatusCode(Response.Status.OK.getStatusCode());
        return response;
    }

    @Override
    public ServiceInstanceResource getServiceInstance(ServiceData svcData) {
        Map<String, String> resParams = new HashMap<>();
        resParams.put(Constants.OCID, svcData.getOcid());

        ServiceInstanceResource response = new ServiceInstanceResource();
        response.setServiceId(svcData.getServiceId());
        response.setParameters(resParams);
        response.setStatusCode(Response.Status.OK.getStatusCode());
        return response;
    }

    @Override
    public AsyncOperation deleteServiceInstance(String instanceId,
                                                String serviceDefinitionId, String planId, ServiceData svcData) {
        String ocid = svcData.getOcid();
        DeleteStreamRequest deleteStreamRequest =
                DeleteStreamRequest.builder()
                        .streamId(ocid)
                        .build();
        streamAdminClient.deleteStream(deleteStreamRequest);
        AsyncOperation response = new AsyncOperation();
        response.setStatusCode(Response.Status.ACCEPTED.getStatusCode());
        response.setOperation(Constants.DELETE_OPERATION);
        return response;
    }

    @Override
    public ServiceBinding bindToService(String instanceId, String bindingId,
                                        ServiceBindingRequest request, ServiceData svcData) {
        ServiceBinding binding = new ServiceBinding();
        Map<String, String> credentials = new HashMap<>();
        credentials
                .put(STREAM_ID, svcData.getOcid());
        //Adding the message endpoint to credentails for each bind request
        getStreamMessageEndpoint(svcData.getOcid(), credentials);
        binding.setCredentials(credentials);
        binding.setStatusCode(Response.Status.CREATED.getStatusCode());

        BindingData bindingData = new BindingData();
        bindingData.setBindingId(bindingId);
        bindingData.setInstanceId(instanceId);
        bindingData.setPlanId(svcData.getPlanId());
        binding.setBindingData(bindingData);

        return binding;
    }

    @Override
    public ServiceBindingResource getServiceBinding(String bindingId,
                                                    ServiceData svcData) {
        ServiceBindingResource binding = new ServiceBindingResource();
        Map<String, String> credentials = new HashMap<>();
        credentials.put(STREAM_ID, svcData.getOcid());
        // Adding messageEndpoint
        getStreamMessageEndpoint(svcData.getOcid(), credentials);
        binding.setCredentials(credentials);
        binding.setStatusCode(Response.Status.OK.getStatusCode());
        return binding;
    }

    @Override
    public LastOperationResource getLastBindingOperation(String instanceId,
                                                         String bindingId,
                                                         String serviceDefinitionId,
                                                         String planId,
                                                         ServiceData svcData,
                                                         BindingData bindingData) {
        throw new BrokerHttpException(Response.Status.BAD_REQUEST.getStatusCode()
                , "Binding is a synchronous operation", "BindingSynchronous");
    }

    @Override
    public LastOperationResource deleteServiceBinding(String instanceId,
                                                      String bindingId,
                                                      String serviceDefinitionId,
                                                      String planId,
                                                      ServiceData svcDat,
                                                      BindingData bindingData) {
        LastOperationResource response = new LastOperationResource();
        response.setStatusCode(Response.Status.OK.getStatusCode());
        return response;
    }

    private ServiceData getSvcData(String instanceId,
                                   ServiceInstanceProvisionRequest body,
                                   String compartmentId,
                                   String ocid, boolean provisioningRequired) {
        ServiceData svcData = new ServiceData();
        svcData.setInstanceId(instanceId);
        svcData.setPlanId(body.getPlanId());
        svcData.setServiceId(body.getServiceId());
        svcData.setCompartmentId(compartmentId);
        svcData.setOcid(ocid);
        svcData.setProvisioning(provisioningRequired);
        return svcData;
    }


    private boolean sameInstance(StreamSummary stream, Map mapParameters) {
        Integer partitions = RequestUtil.getIntegerParameter(mapParameters, PARTITIONS, true);
        if (!stream.getPartitions().equals(partitions)) {
            return false;
        }
        return true;
    }

    private void getStreamMessageEndpoint(String ocid, Map<String, String> credMap) {
        Stream stream = streamAdminClient.getStream(GetStreamRequest.builder().streamId(ocid).build()).getStream();
        if (stream != null) {
            credMap.put(MESSAGE_ENDPOINT, stream.getMessagesEndpoint());
        }
    }
}
