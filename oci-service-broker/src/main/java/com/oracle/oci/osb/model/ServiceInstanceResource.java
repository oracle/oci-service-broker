/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceInstanceResource extends AbstractResponse {

    private @Valid
    String serviceId = null;

    private @Valid
    String planId = null;

    private @Valid
    String dashboardUrl = null;

    private @Valid
    java.lang.Object parameters = null;

    /**
     **/
    public ServiceInstanceResource serviceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    @JsonProperty("service_id")
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     **/
    public ServiceInstanceResource planId(String planId) {
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
    public ServiceInstanceResource dashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
        return this;
    }

    @JsonProperty("dashboard_url")
    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    /**
     **/
    public ServiceInstanceResource parameters(java.lang.Object parameters) {
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
        ServiceInstanceResource serviceInstanceResource = (ServiceInstanceResource) o;
        return Objects.equals(serviceId, serviceInstanceResource.serviceId) && Objects.equals(planId,
                serviceInstanceResource.planId) && Objects.equals(dashboardUrl, serviceInstanceResource.dashboardUrl)
                && Objects.equals(parameters, serviceInstanceResource.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, planId, dashboardUrl, parameters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceInstanceResource {\n");

        sb.append("    serviceId: ").append(toIndentedString(serviceId)).append("\n");
        sb.append("    planId: ").append(toIndentedString(planId)).append("\n");
        sb.append("    dashboardUrl: ").append(toIndentedString(dashboardUrl)).append("\n");
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

