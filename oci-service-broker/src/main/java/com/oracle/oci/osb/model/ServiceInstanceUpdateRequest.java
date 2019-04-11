/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class ServiceInstanceUpdateRequest {

    private @Valid
    Context context = null;

    private @Valid
    String serviceId = null;

    private @Valid
    String planId = null;

    private @Valid
    java.lang.Object parameters = null;

    private @Valid
    ServiceInstancePreviousValues previousValues = null;

    /**
     **/
    public ServiceInstanceUpdateRequest context(Context context) {
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
    public ServiceInstanceUpdateRequest serviceId(String serviceId) {
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
    public ServiceInstanceUpdateRequest planId(String planId) {
        this.planId = planId;
        return this;
    }

    @JsonProperty("plan_id")
    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    /**
     **/
    public ServiceInstanceUpdateRequest parameters(java.lang.Object parameters) {
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

    /**
     **/
    public ServiceInstanceUpdateRequest previousValues(ServiceInstancePreviousValues previousValues) {
        this.previousValues = previousValues;
        return this;
    }

    @JsonProperty("previous_values")
    public ServiceInstancePreviousValues getPreviousValues() {
        return previousValues;
    }

    public void setPreviousValues(ServiceInstancePreviousValues previousValues) {
        this.previousValues = previousValues;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceInstanceUpdateRequest serviceInstanceUpdateRequest = (ServiceInstanceUpdateRequest) o;
        return Objects.equals(context, serviceInstanceUpdateRequest.context) && Objects.equals(serviceId,
                serviceInstanceUpdateRequest.serviceId) && Objects.equals(planId, serviceInstanceUpdateRequest
                .planId) && Objects.equals(parameters, serviceInstanceUpdateRequest.parameters) && Objects.equals
                (previousValues, serviceInstanceUpdateRequest.previousValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, serviceId, planId, parameters, previousValues);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceInstanceUpdateRequest {\n");

        sb.append("    context: ").append(toIndentedString(context)).append("\n");
        sb.append("    serviceId: ").append(toIndentedString(serviceId)).append("\n");
        sb.append("    planId: ").append(toIndentedString(planId)).append("\n");
        sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
        sb.append("    previousValues: ").append(toIndentedString(previousValues)).append("\n");
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

