/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class ServiceInstanceProvisionRequest {

    private @Valid
    String serviceId = null;

    private @Valid
    String planId = null;

    private @Valid
    Context context = null;

    private @Valid
    String organizationGuid = null;

    private @Valid
    String spaceGuid = null;

    private @Valid
    java.lang.Object parameters = null;

    /**
     **/
    public ServiceInstanceProvisionRequest serviceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    @JsonProperty("service_id")
    @NotNull
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     **/
    public ServiceInstanceProvisionRequest planId(String planId) {
        this.planId = planId;
        return this;
    }

    @JsonProperty("plan_id")
    @NotNull
    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    /**
     **/
    public ServiceInstanceProvisionRequest context(Context context) {
        this.context = context;
        return this;
    }

    @JsonProperty("context")
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     **/
    public ServiceInstanceProvisionRequest organizationGuid(String organizationGuid) {
        this.organizationGuid = organizationGuid;
        return this;
    }

    @JsonProperty("organization_guid")
    @NotNull
    public String getOrganizationGuid() {
        return organizationGuid;
    }

    public void setOrganizationGuid(String organizationGuid) {
        this.organizationGuid = organizationGuid;
    }

    /**
     **/
    public ServiceInstanceProvisionRequest spaceGuid(String spaceGuid) {
        this.spaceGuid = spaceGuid;
        return this;
    }

    @JsonProperty("space_guid")
    @NotNull
    public String getSpaceGuid() {
        return spaceGuid;
    }

    public void setSpaceGuid(String spaceGuid) {
        this.spaceGuid = spaceGuid;
    }

    /**
     **/
    public ServiceInstanceProvisionRequest parameters(java.lang.Object parameters) {
        this.parameters = parameters;
        return this;
    }

    @JsonProperty("parameters")
    public java.lang.Object getParameters() {
        return parameters;
    }

    public void setParameters(java.lang.Object parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceInstanceProvisionRequest serviceInstanceProvisionRequest = (ServiceInstanceProvisionRequest) o;
        return Objects.equals(serviceId, serviceInstanceProvisionRequest.serviceId) && Objects.equals(planId,
                serviceInstanceProvisionRequest.planId) && Objects.equals(context, serviceInstanceProvisionRequest
                .context) && Objects.equals(organizationGuid, serviceInstanceProvisionRequest.organizationGuid) &&
                Objects.equals(spaceGuid, serviceInstanceProvisionRequest.spaceGuid) && Objects.equals(parameters,
                serviceInstanceProvisionRequest.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, planId, context, organizationGuid, spaceGuid, parameters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceInstanceProvisionRequest {\n");

        sb.append("    serviceId: ").append(toIndentedString(serviceId)).append("\n");
        sb.append("    planId: ").append(toIndentedString(planId)).append("\n");
        sb.append("    context: ").append(toIndentedString(context)).append("\n");
        sb.append("    organizationGuid: ").append(toIndentedString(organizationGuid)).append("\n");
        sb.append("    spaceGuid: ").append(toIndentedString(spaceGuid)).append("\n");
        sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

