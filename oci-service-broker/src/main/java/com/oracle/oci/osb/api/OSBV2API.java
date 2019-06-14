/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.bmc.model.BmcException;
import com.oracle.oci.osb.adapter.ServiceAdapter;
import com.oracle.oci.osb.model.*;
import com.oracle.oci.osb.rest.OSBAPI;
import com.oracle.oci.osb.store.BindingData;
import com.oracle.oci.osb.store.DataStore;
import com.oracle.oci.osb.store.DataStoreFactory;
import com.oracle.oci.osb.store.ServiceData;
import com.oracle.oci.osb.util.*;
import org.glassfish.jersey.SslConfigurator;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.oracle.oci.osb.util.Utils.*;

@Path("/v2")
@Singleton
@OSBAPI
public class OSBV2API {

    private static final Logger LOGGER = getLogger(OSBV2API.class);

    private static final Map<String, String> commonTags = new HashMap<>();

    private enum RequestType {
        PROVISION("Provision"),
        UPDATE("Update"),
        DELETE("Delete"),
        GET("Get"),
        BIND("Bind"),
        GET_BINDING("GetBinding"),
        DELETE_BINDING("DeleteBinding");
        private String type;

        RequestType(String type) {
            this.type = type;
        }

        String getType() {
            return type;
        }
    }

    //Build Common Tags that should be added by adapters for Service they provision.
    static {
        commonTags.putAll(System.getProperties().entrySet().stream()
                .filter(e -> ((String) e.getKey()).startsWith(Constants.SERVICE_TAG))
                .collect(Collectors.toMap(
                        e -> ((String) e.getKey()).substring(Constants.SERVICE_TAG.length()),
                        e -> (String) e.getValue())));
        commonTags.put(Constants.CREATED_BY, Constants.OCI_OSB_BROKER);

        String clusterId = commonTags.get(Constants.CLUSTER_ID_TAG);

        // only get the label from kubernetes if all the required parameters are present
        if (!commonTags.containsKey(Constants.CLUSTER_ID_TAG)) {

            String tokenFile = System.getProperty(Constants.KUBERNETES_BEARER_API_TOKEN_FILE);
            String apiHost = System.getenv(Constants.KUBERNETES_SERVICE_HOST);
            String port = System.getenv(Constants.KUBERNETES_SERVICE_PORT);
            String nodeName = System.getenv(Constants.NODE_NAME);
            String apiCaCert = System.getProperty(Constants.API_SERVER_CA_CERT);
            if (tokenFile != null && apiHost != null &&  port != null && nodeName != null && !Utils
                    .isNullOrEmptyString(apiCaCert) && Files.isReadable(Paths.get(apiCaCert))){
                try {
                    SslConfigurator sslConfig = SslConfigurator.newInstance();
                    sslConfig.trustStore(getTrustStoreWithApiCaCerts(apiCaCert));
                    SSLContext sslContext = sslConfig.createSSLContext();
                    Client client = ClientBuilder.newBuilder().sslContext(sslContext).build();

                    WebTarget webTarget
                            = client.target(String.format(Constants.NODE_API, apiHost, port, nodeName));
                    Invocation.Builder invocationBuilder =
                            webTarget.request(MediaType.APPLICATION_JSON);
                    String bearerToken = new String(Files.readAllBytes(
                            Paths.get(tokenFile)),
                            Constants.CHARSET_UTF8);
                    invocationBuilder.header(Constants.AUTHORIZATION_HEADER,
                            Constants.AUTHORIZATION_BEARER + " " + bearerToken);
                    Response response = invocationBuilder.get();
                    String entity = response.readEntity(String.class);
                    ObjectMapper objMapper = new ObjectMapper();
                    Map responseMap = objMapper.readValue(entity, Map.class);
                    String nodeDisplayName = ((String) ((Map) ((Map) responseMap.get(Constants.K8S_METADATA))
                            .get(Constants.K8S_LABELS))
                            .get(Constants.K8S_DISPLAYNAME));

                    if (nodeDisplayName.startsWith(Constants.OKE_PREFIX)) {
                        clusterId = nodeDisplayName.split("-")[1];
                        commonTags.put(Constants.CLUSTER_ID_TAG, clusterId);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Exception occurred while getting cluster id for OKE", e);
                }
            }
        }
        if(clusterId != null){
            System.setProperty(Constants.CLUSTER_ID_TAG, clusterId);
        }
    }

    private static KeyStore getTrustStoreWithApiCaCerts(String apiServerCaCert) throws Exception {
        BufferedInputStream caCertStream = new BufferedInputStream(new FileInputStream(apiServerCaCert));
        X509Certificate caCert = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(caCertStream);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("apicacert", caCert);
        return keyStore;
    }

    private Catalog catalog;

    private Map<String, ServiceAdapter> mapServiceToAdapters;

    private Map<String, ServiceAdapter> mapPlanToAdapters;

    private Map<String, Service> mapServices;

    private final DataStore dataStore;

    public OSBV2API() throws IOException {
        dataStore = DataStoreFactory.getDataStore();
        catalog = new Catalog();
        mapServiceToAdapters = new HashMap<>();
        mapPlanToAdapters = new HashMap<>();
        mapServices = new HashMap<>();
        ServiceLoader<ServiceAdapter> serviceAdapterLoader = ServiceLoader
                .load(ServiceAdapter.class);
        for (ServiceAdapter adapter : serviceAdapterLoader) {
            List<Service> services = adapter.getCatalog().getServices();
            for (Service service : services) {
                if (mapServiceToAdapters.containsKey(service.getId())) {
                    throw Errors.serviceWithSameId();
                }
                mapServiceToAdapters.put(service.getId(), adapter);
                mapServices.put(service.getId(), service);
                for (Plan plan : service.getPlans()) {
                    if (mapPlanToAdapters.containsKey(plan.getId())) {
                        throw Errors.planWithSameId();
                    }
                    mapPlanToAdapters.put(plan.getId(), adapter);
                }
            }
            catalog.getServices().addAll(services);
        }
        debugLog(LOGGER, "ServiceAdapter Map:  %s", Level.FINE, mapServiceToAdapters);
    }

    @GET
    @Path("/catalog")
    @Produces({"application/json"})
    public Response catalogGet(
            @HeaderParam("X-ServiceAdapter-API-Version") @NotNull String xBrokerAPIVersion) {
        return Response.ok().entity(catalog).build();
    }

    @PUT
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Produces({"application/json"})
    public Response serviceBindingBinding(
            @HeaderParam("X-ServiceAdapter-API-Version") @NotNull String xBrokerAPIVersion,
            @PathParam("instance_id") String instanceId,
            @PathParam("binding_id") String bindingId,
            @Valid ServiceBindingRequest body,
            @QueryParam("accepts_incomplete") Boolean acceptsIncomplete,
            @Context UriInfo uriInfo) {
        try {
            Service svc = mapServices.get(body.getServiceId());
            ServiceData svcData = dataStore.getServiceData(instanceId);

            Response opsResponse = executeAndReturnResponse(() -> {
                if (svcData == null) {
                    throw Errors.instanceDoesNotExistException();
                }

                // check if binding already exists to a different instance
                BindingData bindingData = dataStore.getBindingData(bindingId);

                String bodySvcId = body.getServiceId();
                String bodyPlanId = body.getPlanId();
                // if binding already exists but with different instance id or service or plan id
                // then 409 conflict needs to be returned. This is a bit different from other calls.
                if (bindingData != null && (!bindingData.getInstanceId().equals(instanceId)
                        || (bodySvcId != null && !bodySvcId.equals(bindingData.getServiceId()))
                        || (bodyPlanId != null && !bodyPlanId.equals(bindingData.getPlanId())))) {
                    throw Errors.bindingWithSameIdExists();
                }

                ServiceAdapter serviceAdapter = validateAndGetAdapter(body.getServiceId());
                validateServiceAndPlanId(bodyPlanId, svcData.getPlanId(), bodySvcId, svcData.getServiceId(), true);

                //If binding already existing(with same attributes) then we need to delete and recreate the binding
                // as we
                //don't store the binding and hence can not return the binding data without creating a new binding.
                //Note: Attribute check is already done and hence we are just checking if binding exists or not
                if (bindingData != null) {
                    serviceAdapter.deleteServiceBinding(bindingData.getInstanceId(), bindingData.getBindingId(),
                            bindingData.getServiceId(), bindingData.getPlanId(), svcData, bindingData);
                }

                // check if async provision is required
                Boolean asyncProvision = svc.isAsyncBinding();
                if (asyncProvision != null && asyncProvision && (acceptsIncomplete == null || !acceptsIncomplete)) {
                    throw Errors.asyncRequired();
                }

                // check if service is bindable
                Boolean isBindable = svc.isBindable();
                if (isBindable != null && !isBindable) {
                    throw Errors.unbindable();
                }

                ServiceBinding response = serviceAdapter.bindToService(instanceId, bindingId, body, svcData);

                bindingData = response.getBindingData();
                if (bindingData == null) {
                    throw Errors.bindingDataMissing();
                }

                dataStore.storeBinding(bindingId, bindingData);
                debugLog(LOGGER, "Service Binding response: %s", Level.FINER, response);
                return response;
            });

            auditLog(RequestType.BIND, bindingId, svc, opsResponse, svcData);
            return opsResponse;
        } catch (Exception e) {
            return logAndGetErrorResponse(e);
        }
    }

    @GET
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Produces({"application/json"})
    public Response serviceBindingGet(
            @HeaderParam("X-ServiceAdapter-API-Version") @NotNull String xBrokerAPIVersion,
            @PathParam("instance_id") String instanceId,
            @PathParam("binding_id") String bindingId,
            @Context UriInfo uriInfo) {
        try {
            ServiceData svcData = dataStore.getServiceData(instanceId);

            Response opsResponse = executeAndReturnResponse(() -> {
                if (svcData == null) {
                    throw Errors.instanceDoesNotExistException();
                }
                Service svc = mapServices.get(svcData.getServiceId());
                // check if async provision is required
                Boolean isRetrievable = svc.isBindingsRetrievable();
                if (isRetrievable != null && !isRetrievable) {
                    throw Errors.bindingNotRetrievable();
                }

                ServiceAdapter serviceAdapter = validateAndGetAdapter(svcData.getServiceId());
                return serviceAdapter.getServiceBinding(bindingId, svcData);
            });

            Service svc = (svcData != null) ? mapServices.get(svcData.getServiceId()) : null;
            auditLog(RequestType.GET_BINDING, bindingId, svc, opsResponse, svcData);
            return opsResponse;
        } catch (Exception e) {
            return logAndGetErrorResponse(e);
        }
    }


    @GET
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}/last_operation")
    @Produces({"application/json"})
    public Response serviceBindingLastOperationGet(
            @HeaderParam("X-ServiceAdapter-API-Version")
            @NotNull String xBrokerAPIVersion,
            @PathParam("instance_id") String instanceId,
            @PathParam("binding_id") String bindingId,
            @QueryParam("service_id") String serviceId,
            @QueryParam("plan_id") String planId,
            @QueryParam("operation") String operation) {

        return executeAndReturnResponse(() -> {

            //service_id is not a mandatory param
            if(serviceId != null && mapServices.get(serviceId) != null) {
                Service svc = mapServices.get(serviceId);
                if ( svc.isBindable() == null || !svc.isBindable()) {
                    throw Errors.unbindable();
                }

                if ( svc.isAsyncBinding() == null || !svc.isAsyncBinding()) {
                    throw Errors.bindingSynchronousError();
                }
            }

            if (operation == null || operation.trim().equals("")) {
                throw Errors.operationParameterNotProvided();
            }

            ServiceData svcData = dataStore.getServiceData(instanceId);
            if (svcData == null) {
                if (Constants.DELETE_OPERATION.equals(operation)) {
                    throw Errors.instanceDeletedError();
                } else {
                    throw Errors.instanceDoesNotExistException();
                }
            }

            ServiceAdapter serviceAdapter = validateAndGetAdapter(serviceId);
            validateServiceAndPlanId(planId, svcData.getPlanId(), serviceId, svcData.getServiceId(), false);

            Service svc = mapServices.get(serviceId);

            if ( svc.isBindable() == null || !svc.isBindable()) {
                throw Errors.unbindable();
            }

            if ( svc.isAsyncBinding() == null || !svc.isAsyncBinding()) {
                throw Errors.bindingSynchronousError();
            }

            BindingData bindingData = dataStore.getBindingData(bindingId);
            if (bindingData == null) {
                if (Constants.DELETE_OPERATION.equals(operation)) {
                    throw Errors.bindingDeletedError();
                } else {
                    throw Errors.bindingDoesNotExistError();
                }
            }

            if (serviceId != null && !serviceId.equals(svcData.getServiceId())) {
                throw Errors.invalidServiceError();
            }

            if (planId != null && !planId.equals(svcData.getPlanId())) {
                throw Errors.invalidPlanError();
            }


            LastOperationResource response = serviceAdapter
                    .getLastBindingOperation(instanceId, bindingId, serviceId, svcData.getPlanId(), svcData, bindingData);

            if (operation.equals(Constants.DELETE_OPERATION) &&
                    response.getState() == LastOperationResource.StateEnum.SUCCEEDED) {
                dataStore.removeBindingData(bindingId);
            }

            debugLog(LOGGER, "Service Binding last_operation response: %s", Level.FINE, response);
            return response;
        });
    }


    @DELETE
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Produces({"application/json"})
    public Response serviceBindingUnbinding(
            @HeaderParam("X-ServiceAdapter-API-Version") @NotNull String xBrokerAPIVersion,
            @PathParam("instance_id") String instanceId,
            @PathParam("binding_id") String bindingId,
            @QueryParam("service_id") @NotNull String serviceId,
            @QueryParam("plan_id") @NotNull String planId,
            @QueryParam("accepts_incomplete") Boolean acceptsIncomplete,
            @Context UriInfo uriInfo) {
        try {
            ServiceData svcData = dataStore.getServiceData(instanceId);
            BindingData bindingData = dataStore.getBindingData(bindingId);
            Response opsResponse = executeAndReturnResponse(() -> {
                if (bindingData == null) {
                    throw Errors.bindingDoesNotExistError();
                }

                if (svcData == null) {
                    throw Errors.instanceDoesNotExistException();
                }

                ServiceAdapter serviceAdapter = validateAndGetAdapter(serviceId);
                validateServiceAndPlanId(planId, svcData.getPlanId(), serviceId, svcData.getServiceId(), true);

                LastOperationResource response = serviceAdapter.deleteServiceBinding(instanceId, bindingId, serviceId,
                        planId, svcData, bindingData);

                if (response.getStatusCode() == Response.Status.OK.getStatusCode()) {
                    dataStore.removeBindingData(bindingId);
                }

                debugLog(LOGGER, "Service Unbinding response: %s", Level.FINE, response);
                return response;
            });
            auditLog(RequestType.DELETE_BINDING, bindingId, mapServices.get(serviceId), opsResponse, svcData);
            return opsResponse;
        } catch (Exception e) {
            return logAndGetErrorResponse(e);
        }
    }

    @DELETE
    @Path("/service_instances/{instance_id}")
    @Produces({"application/json"})
    public Response serviceInstanceDeprovision(
            @HeaderParam("X-ServiceAdapter-API-Version") @NotNull String xBrokerAPIVersion,
            @PathParam("instance_id") String instanceId,
            @QueryParam("service_id") @NotNull String serviceId,
            @QueryParam("plan_id") @NotNull String planId,
            @QueryParam("accepts_incomplete") Boolean acceptsIncomplete,
            @Context UriInfo uriInfo) {
        try {
            Service svc = mapServices.get(serviceId);
            ServiceData svcData = dataStore.getServiceData(instanceId);

            Response opsResponse = executeAndReturnResponse(() -> {
                ServiceAdapter serviceAdapter = validateAndGetAdapter(serviceId);

                if (svcData == null) {
                    throw new BrokerHttpException(Response.Status.GONE.getStatusCode(),
                            "The service instance does not exist", "InstanceDoesNotExist");
                }

                AsyncOperation response = null;
                Boolean asyncDelete = svc.isAsyncDelete();
                if (asyncDelete != null && asyncDelete && (acceptsIncomplete == null || !acceptsIncomplete)) {
                    throw Errors.asyncRequired();
                }

                validateServiceAndPlanId(planId, svcData.getPlanId(), serviceId, svcData.getServiceId(), true);

                Boolean isProvisioning = svcData.getProvisioning();
                if(!isProvisioning) {
                    dataStore.removeServiceData(instanceId);
                    response = new AsyncOperation();
                    response.setStatusCode(Response.Status.OK.getStatusCode());
                    response.setOperation(Constants.DELETE_OPERATION);
                    debugLog(LOGGER, "Actual Service Instance is not deleted as it's binding only request.: %s", Level.FINE, response);
                }
                else {
                    response = serviceAdapter.deleteServiceInstance(instanceId, serviceId, planId, svcData);
                    if (response.getStatusCode() == Response.Status.OK.getStatusCode() ||
                            response.getStatusCode() == Response.Status.GONE.getStatusCode()) {
                        dataStore.removeServiceData(instanceId);
                    }
                    debugLog(LOGGER, "Service Delete response: %s", Level.FINE, response);
                }
                return response;

            });
            auditLog(RequestType.DELETE, instanceId, svc, opsResponse, svcData);
            return opsResponse;
        } catch (Exception e) {
            return logAndGetErrorResponse(e);
        }
    }

    @GET
    @Path("/service_instances/{instance_id}")
    @Produces({"application/json"})
    public Response serviceInstanceGet(
            @HeaderParam("X-ServiceAdapter-API-Version") @NotNull String xBrokerAPIVersion,
            @PathParam("instance_id") String instanceId,
            @Context UriInfo uriInfo) {
        try {
            ServiceData svcData = dataStore.getServiceData(instanceId);

            Response opsResponse = executeAndReturnResponse(() -> {
                if (svcData == null) {
                    throw Errors.instanceDoesNotExistException();
                }
                Service svc = mapServices.get(svcData.getServiceId());
                // check if async provision is required
                Boolean isRetrievable = svc.isInstancesRetrievable();
                if (isRetrievable != null && !isRetrievable) {
                    throw Errors.bindingNotRetrievable();
                }

                ServiceAdapter serviceAdapter = validateAndGetAdapter(svcData.getServiceId());
                return serviceAdapter.getServiceInstance(svcData);
            });

            Service svc = (svcData != null) ? mapServices.get(svcData.getServiceId()) : null;
            auditLog(RequestType.GET, instanceId, svc, opsResponse, svcData);
            return opsResponse;
        } catch (Exception e) {
            return logAndGetErrorResponse(e);
        }
    }

    @GET
    @Path("/service_instances/{instance_id}/last_operation")
    @Produces({"application/json"})
    public Response serviceInstanceLastOperationGet(
            @HeaderParam("X-ServiceAdapter-API-Version") @NotNull String xBrokerAPIVersion,
            @PathParam("instance_id") String instanceId,
            @QueryParam("service_id") String serviceId,
            @QueryParam("plan_id") String planId,
            @QueryParam("operation") String operation) {
        return executeAndReturnResponse(() -> {
            //service_id is not a mandatory param
            if(serviceId != null && mapServices.get(serviceId) != null) {
                Service svc = mapServices.get(serviceId);
                if ( svc.isAsyncProvision() == null || !svc.isAsyncProvision()) {
                    throw Errors.provisionSynchronousError();
                }
            }

            if (operation == null || operation.trim().equals("")) {
                throw new BrokerHttpException(Response.Status.BAD_REQUEST.getStatusCode(),
                        "The operation parameter is not provided", "OperationNotProvided");
            }

            ServiceData svcData = dataStore.getServiceData(instanceId);
            if (svcData == null) {
                if (Constants.DELETE_OPERATION.equals(operation)) {
                    throw Errors.instanceDeletedError();
                } else {
                    throw Errors.instanceDoesNotExistException();
                }
            }

            validateServiceAndPlanId(planId, svcData.getPlanId(), serviceId, svcData.getServiceId(),false);

            ServiceAdapter serviceAdapter = validateAndGetAdapter(svcData.getServiceId());

            Service svc = mapServices.get(svcData.getServiceId());
            if (svc.isAsyncProvision() == null || !svc.isAsyncProvision()) {
                throw Errors.provisionSynchronousError();
            }

            LastOperationResource response = serviceAdapter
                    .getLastOperation(instanceId, svcData.getServiceId(), svcData.getPlanId(), operation, svcData);

            if (operation.equals(Constants.DELETE_OPERATION) &&
                    response.getState() == LastOperationResource.StateEnum.SUCCEEDED) {
                dataStore.removeServiceData(instanceId);
            }
            debugLog(LOGGER, "Service Get  last_operation response: %s", Level.FINE, response);
            return response;
        });
    }

    @PUT
    @Path("/service_instances/{instance_id}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response serviceInstanceProvision(
            @HeaderParam("X-ServiceAdapter-API-Version") @NotNull
                    String xBrokerAPIVersion,
            @PathParam("instance_id")
                    String instanceId,
            @Valid ServiceInstanceProvisionRequest body,
            @QueryParam("accepts_incomplete")
                    Boolean acceptsIncomplete,
            @Context UriInfo uriInfo) {
        try {

            Service svc = (body != null) ? mapServices.get(body.getServiceId()) : null;

            Response opsResponse = executeAndReturnResponse(() -> {

               ServiceAdapter serviceAdapter = validateAndGetAdapter(body.getServiceId());

               boolean isProvisioningRequired = RequestUtil.getBooleanParameterDefaultValueTrue(
                        (Map<String, Object>) body.getParameters(), Constants.PROVISIONING, false);

                ServiceAdapter planBroker = mapPlanToAdapters.get(body.getPlanId());
                if (planBroker == null) throw Errors.planDoesNotExistError();

                //Check data store to see if instance already exists.
                ServiceData svcData = dataStore.getServiceData(instanceId);
                if (svcData != null) {
                    validateServiceAndPlanId(body.getPlanId(), svcData.getPlanId(), body.getServiceId(),
                            svcData.getServiceId(), true);
                }
                Boolean asyncProvision = svc.isAsyncProvision();
                if (asyncProvision != null && asyncProvision && (acceptsIncomplete == null || !acceptsIncomplete)) {
                    throw Errors.asyncRequired();
                }

                ServiceAdapter.ServiceInstanceStatus serviceInstanceStatus = serviceAdapter
                        .getOciServiceInstanceStatus(instanceId, body);

                if (serviceInstanceStatus == ServiceAdapter.ServiceInstanceStatus.CONFLICT) {
                    throw new BrokerHttpException(Response.Status.CONFLICT.getStatusCode(),
                            "A Conflicting service with same ID or parameters already exists", "ServiceExists");
                } else if (serviceInstanceStatus == ServiceAdapter.ServiceInstanceStatus.EXISTS) {
                    ServiceInstanceProvision response = serviceAdapter.provisionExistingServiceInstance(instanceId, body);
                    if (response.getStatusCode() == Response.Status.ACCEPTED.getStatusCode()) {
                        response.setOperation(Constants.PROVISION_OPERATION);
                    }
                    dataStore.storeServiceData(instanceId, response.getSvcData());
                    debugLog(LOGGER, "Service Provision of existing service response: %s", Level.FINE, response);
                    return response;
                } else if (!isProvisioningRequired && serviceInstanceStatus == ServiceAdapter.ServiceInstanceStatus.DOESNOTEXIST) {
                    // it's just for binding request.
                    // If instance does not exist and provisioning flag set false we need to throw error.
                    throw Errors.serviceDoesNotExistError();
                }

                Map<String, String> freeFormTags;
                if (body.getParameters() != null && body.getParameters() instanceof Map) {
                    Map mapParameters = (Map) body.getParameters();
                    freeFormTags = RequestUtil
                            .getMapStringParameter(mapParameters, Constants.FREE_FORM_TAGS, false);
                } else {
                    freeFormTags = new HashMap<>();
                }
                freeFormTags.put(Constants.OSB_INSTANCE_ID_LABEL, instanceId);
                freeFormTags.put(Constants.CREATED_ON_BEHALF, OriginatingIdentity.getUserName());
                freeFormTags.putAll(commonTags);

                ServiceInstanceProvision response = serviceAdapter.provisionServiceInstance(instanceId, body,
                        Collections
                        .unmodifiableMap(freeFormTags));
                svcData = response.getSvcData();
                if (svcData == null) {
                    throw Errors.svcDataMissing();
                }

                if (response.getStatusCode() == Response.Status.ACCEPTED.getStatusCode()) {
                    response.setOperation(Constants.PROVISION_OPERATION);
                }

                dataStore.storeServiceData(instanceId, svcData);
                debugLog(LOGGER, "Service Provision response: %s", Level.FINE, response);
                return response;
            });

            auditLog(RequestType.PROVISION, instanceId, svc, opsResponse, dataStore.getServiceData(instanceId));
            return opsResponse;
        } catch (Exception e) {
            return logAndGetErrorResponse(e);
        }
    }

    @PATCH
    @Path("/service_instances/{instance_id}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response serviceInstanceUpdate(
            @HeaderParam("X-ServiceAdapter-API-Version") @NotNull String xBrokerAPIVersion,
            @PathParam("instance_id") String instanceId,
            @Valid ServiceInstanceUpdateRequest body,
            @QueryParam("accepts_incomplete") Boolean acceptsIncomplete,
            @Context UriInfo uriInfo) {
        try {
            ServiceData svcData = dataStore.getServiceData(instanceId);
            Service svc = mapServices.get(body.getServiceId());

            Response opsResponse = executeAndReturnResponse(() -> {
                String existingServiceId = body.getServiceId();
                String existingPlanId = body.getPlanId();

                ServiceInstancePreviousValues previousValues = body.getPreviousValues();
                if (previousValues != null) {
                    if (!isNullOrEmptyString(previousValues.getPlanId())) {
                        existingPlanId = previousValues.getPlanId();
                    }

                    if (!isNullOrEmptyString(previousValues.getServiceId())) {
                        existingServiceId = previousValues.getServiceId();
                    }
                }
                if (svcData == null) {
                    throw Errors.instanceDoesNotExistException();
                }

                // if there is an update, we should not check if the new plan matches exiting plan
                validateServiceAndPlanId(null, null, body.getServiceId(), svcData.getServiceId(), false);

                ServiceAdapter serviceAdapter = validateAndGetAdapter(body.getServiceId());
                Boolean asyncUpdate = svc.isAsyncUpdate();
                if (asyncUpdate != null && asyncUpdate && (acceptsIncomplete == null || !acceptsIncomplete)) {
                    throw Errors.asyncRequired();
                }
                debugLog(LOGGER, "Service Update request; ServiceId:%s PlanId: %s", Level.FINE, existingServiceId,
                        existingPlanId);

                if (!svcData.getProvisioning().booleanValue()) {
                    throw Errors.unSupportedOperation();
                }

                String newPlanId = body.getPlanId();
                if (newPlanId != null && mapPlanToAdapters.get(newPlanId) == null) {
                    throw Errors.planDoesNotExistError();
                }
                ServiceInstanceAsyncOperation response = serviceAdapter.updateServiceInstance(instanceId, body,
                        svcData);
                if (response.getSvcData() != null) {
                    dataStore.storeServiceData(instanceId, response.getSvcData());
                }
                debugLog(LOGGER, "Service Update response: %s", Level.FINE, response);
                return response;
            });

            auditLog(RequestType.UPDATE, instanceId, svc, opsResponse, svcData);
            return opsResponse;
        } catch (Exception e) {
            return logAndGetErrorResponse(e);
        }

    }

    private ServiceAdapter validateAndGetAdapter(String serviceId) {
        ServiceAdapter serviceAdapter = mapServiceToAdapters.get(serviceId);

        if (serviceAdapter == null) {
            throw Errors.serviceDoesNotExistError();
        }
        return serviceAdapter;
    }

    private void validateServiceAndPlanId(String planId, String existingPlanId, String serviceId,
                                String existingServiceId, boolean validateNonNullPlan) {
        if (existingServiceId != null && serviceId != null && !existingServiceId.equals(serviceId)) {
            throw Errors.invalidServiceError();
        }
        if (validateNonNullPlan && planId == null) {
            throw Errors.planNotProvidedError();
        }
        if (existingPlanId != null && planId != null && !existingPlanId.equals(planId)) {
            throw Errors.invalidPlanError();
        }
    }

    private Response executeAndReturnResponse(Callable<AbstractResponse> callable) {
        try {
            AbstractResponse abstractResponse = callable.call();
            return Response.status(abstractResponse.getStatusCode()).entity(abstractResponse).build();
        } catch (BrokerHttpException e) {
            LOGGER.log(Level.SEVERE, "Exception occurred while executing OSB Request", e);
            return Response.status(e.getResponse().getStatus())
                    .entity(new ErrorResponse(e.getErrorCode(), e.getMessage())).build();
        } catch (BmcException e) {
            LOGGER.log(Level.SEVERE,"Exception occurred while executing OSB Request", e);
            return Response.status(e.getStatusCode())
                    .entity(new ErrorResponse(e.getServiceCode(), e.getMessage())).build();
        } catch (Exception e) {
            return logAndGetErrorResponse(e);
        }
    }

    private Response logAndGetErrorResponse(Exception e) {
        LOGGER.log(Level.SEVERE, "Exception occurred while executing OSB Request", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .entity(new ErrorResponse("InternalServerError", e.getMessage())).build();
    }

    private void auditLog(RequestType requestType, String identifier, Service svc, Response
            response, ServiceData svcData) {
       try {
           String user = OriginatingIdentity.getUserName();
           StringBuilder sb = new StringBuilder();
           ///Audit Log format: <Timestmap>|<LogLevel>|<ClassName>|Audit|<RequestSource>|<On-Behalf-User>|<ServiceName>
           // <InstanceId/BindingId>|<RequestType>|<ServiceData>
           String serviceName = (svc != null) ? svc.getName() : "UNKNOWN";
           String responseCode = (response != null) ? Integer.toString(response.getStatus()) : "UNKNOWN";
           sb.append("Audit||")
                   .append(user).append("|")
                   .append(serviceName).append("|")
                   .append(identifier).append("|")
                   .append(requestType.getType()).append("|")
                   .append(responseCode).append("|");
           if (svcData != null) {
               sb.append(serializeToJson(svcData));
           }
           LOGGER.info(sb.toString());
       } catch(Exception e){
           //Make sure any exception in logging doesn't propagate.
           LOGGER.log(Level.SEVERE, "Error logging Audit message!", e);
       }
    }
}

