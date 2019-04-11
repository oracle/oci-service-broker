/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class ServiceBindingRequest {

    private @Valid
    Context context = null;

    private @Valid
    String serviceId = null;

    private @Valid
    String planId = null;

    private @Valid
    String appGuid = null;

    private @Valid
    ServiceBindingResourceObject bindResource = null;

    private @Valid
    java.lang.Object parameters = null;

    /**
     **/
    public ServiceBindingRequest context(Context context) {
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
    public ServiceBindingRequest serviceId(String serviceId) {
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
    public ServiceBindingRequest planId(String planId) {
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
    public ServiceBindingRequest appGuid(String appGuid) {
        this.appGuid = appGuid;
        return this;
    }

    @JsonProperty("app_guid")
    public String getAppGuid() {
        return appGuid;
    }

    public void setAppGuid(String appGuid) {
        this.appGuid = appGuid;
    }

    /**
     **/
    public ServiceBindingRequest bindResource(ServiceBindingResourceObject bindResource) {
        this.bindResource = bindResource;
        return this;
    }

    @JsonProperty("bind_resource")
    public ServiceBindingResourceObject getBindResource() {
        return bindResource;
    }

    public void setBindResource(ServiceBindingResourceObject bindResource) {
        this.bindResource = bindResource;
    }

    /**
     **/
    public ServiceBindingRequest parameters(java.lang.Object parameters) {
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
        ServiceBindingRequest serviceBindingRequest = (ServiceBindingRequest) o;
        return Objects.equals(context, serviceBindingRequest.context) && Objects.equals(serviceId,
                serviceBindingRequest.serviceId) && Objects.equals(planId, serviceBindingRequest.planId) && Objects
                .equals(appGuid, serviceBindingRequest.appGuid) && Objects.equals(bindResource, serviceBindingRequest
                .bindResource) && Objects.equals(parameters, serviceBindingRequest.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, serviceId, planId, appGuid, bindResource, parameters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceBindingRequest {\n");

        sb.append("    context: ").append(toIndentedString(context)).append("\n");
        sb.append("    serviceId: ").append(toIndentedString(serviceId)).append("\n");
        sb.append("    planId: ").append(toIndentedString(planId)).append("\n");
        sb.append("    appGuid: ").append(toIndentedString(appGuid)).append("\n");
        sb.append("    bindResource: ").append(toIndentedString(bindResource)).append("\n");
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

