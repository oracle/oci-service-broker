/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapter;

import com.oracle.oci.osb.model.*;
import com.oracle.oci.osb.store.BindingData;
import com.oracle.oci.osb.store.ServiceData;

import java.io.IOException;
import java.util.Map;

/**
 * ServiceAdapter provides standard interface for the individual service adapter to
 * provide their implementation for provisioning and manging Service instances.
 */
public interface ServiceAdapter {

    enum ServiceInstanceStatus {EXISTS, DOESNOTEXIST, CONFLICT}

    /**
     * Fetch the Catalog containing the Service and Plan details.
     *
     * @return Catalog that contains services and plan details.
     * @throws IOException exception reading catalog file.
     */
    Catalog getCatalog() throws IOException;

    /**
     * Return the status of the service instance. The statuses are
     *     EXISTS - if the instance exists and can be returned
     *     DOESNOTEXIST - if the instance does not exist
     *     CONFLICT - If there is a conflicting service with similar parameters
     * @return the status of the service
     */
    ServiceInstanceStatus getOciServiceInstanceStatus(String instanceId, ServiceInstanceProvisionRequest body);

    /**
     * Provision an instances of a Service. During provisioning, if the service supports FreeFormTags, then
     * the {@code freeFormTags} should be used. {@code freeFormTags} includes the tags contained in the body along
     * with the mandatory tags. Hence a adapter <strong>should never</strong> read free form tags directly from the
     * body.
     *
     * @param instanceId unique ID representing an instance.
     * @param body       request body.
     * @param freeFormTags freeformTags that includes the mandatory tags.
     * @return ServiceInstanceProvision.
     */
    ServiceInstanceProvision provisionServiceInstance(String instanceId, ServiceInstanceProvisionRequest body,
                                                      Map<String, String> freeFormTags);

    /**
     * Provision an instances of a Service which already exist. If the provision is a no-op as the instance already
     * exists, return a valid response similar to an actual provision instance.
     *
     * @param instanceId unique ID representing an instance.
     * @param body       request body.
     * @return ServiceInstanceProvision.
     */
    ServiceInstanceProvision provisionExistingServiceInstance(String instanceId, ServiceInstanceProvisionRequest body);

    /**
     * Update an existing Service instance.
     *
     * @param instanceId unique ID representing an instance.
     * @param body       request body.
     * @return ServiceInstanceProvision.
     */
    ServiceInstanceAsyncOperation updateServiceInstance(String instanceId, ServiceInstanceUpdateRequest body,
                                                        ServiceData svcData);

    /**
     * Fetch the current status of the earlier async provision or delete request.
     *
     * @param instanceId          unique ID representing an instance.
     * @param serviceDefinitionId service details.
     * @param planId              planId used by the instance.
     * @param operation           operation name sent by the async request for
     *                            which this fucntion is getting invoked.
     * @return LastOperationResource. The response body will either contain
     * "in progress" "succeeded" or "failed" depending on the state of the request.
     */
    LastOperationResource getLastOperation(String instanceId, String serviceDefinitionId, String planId, String
            operation, ServiceData svcData);

    /**
     * Fetch details about a Service instance.
     *
     * @param svcData service Metadata.
     * @return ServiceInstanceResource.
     */
    ServiceInstanceResource getServiceInstance(ServiceData svcData);

    /**
     * Delete an Service Instance.
     *
     * @param instanceId          unique ID representing an instance.
     * @param serviceDefinitionId service details.
     * @param planId              planId used by the instance.
     * @return AsyncOperation.
     */
    AsyncOperation deleteServiceInstance(String instanceId, String serviceDefinitionId, String planId, ServiceData
            svcData);

    /**
     * Create Binding to fetch the credentials/configuration required for
     * consuming the Service instance.
     *
     * @param instanceId unique ID representing an instance.
     * @param bindingId  unique ID representing the binding for an instance.
     * @param request    request body.
     * @return ServiceBinding.
     */
    ServiceBinding bindToService(String instanceId, String bindingId, ServiceBindingRequest request, ServiceData
            svcData);

    /**
     * Fetch credentials/configuration required for consuming the Service instance.
     *
     * @param bindingId unique ID representing the binding for an instance.
     * @param svcData   service Metadata.
     * @return ServiceBindingResource.
     */
    ServiceBindingResource getServiceBinding(String bindingId, ServiceData svcData);

    /**
     * Fetch the current status of the earlier async Service Binding request.
     *
     * @param instanceId          unique ID representing an instance
     * @param bindingId           unique ID representing the binding of an Service
     *                            instance
     * @param serviceDefinitionId service details.
     * @param planId              planId used by the instance.
     * @return LastOperationResource. The response body will either contain
     * "in progress" "succeeded" or "failed" depending on the state of the request.
     */
    LastOperationResource getLastBindingOperation(String instanceId, String bindingId, String serviceDefinitionId,
                                                  String planId, ServiceData svcData, BindingData bindingData);

    /**
     * Delete the Service Binding of an Service Instance.
     *
     * @param instanceId          unique ID representing an instance.
     * @param bindingId           unique ID representing the binding of an Service
     *                            instance.
     * @param serviceDefinitionId service details.
     * @param planId              planId used by the instance.
     * @return LastOperationResource
     */
    LastOperationResource deleteServiceBinding(String instanceId, String bindingId, String serviceDefinitionId,
                                               String planId, ServiceData svcData, BindingData bindingData);
}
