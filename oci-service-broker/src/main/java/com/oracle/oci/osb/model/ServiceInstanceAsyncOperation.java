/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oracle.oci.osb.store.ServiceData;

import javax.validation.Valid;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceInstanceAsyncOperation extends AbstractResponse {

    @Valid
    private String dashboardUrl = null;

    @Valid
    private String operation = null;

    @JsonIgnore
    private ServiceData svcData;

    /**
     **/
    public ServiceInstanceAsyncOperation dashboardUrl(String dashboardUrl) {
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
    public ServiceInstanceAsyncOperation operation(String operation) {
        this.operation = operation;
        return this;
    }

    @JsonProperty("operation")
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public ServiceData getSvcData() {
        return svcData;
    }

    public void setSvcData(ServiceData svcData) {
        this.svcData = svcData;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceInstanceAsyncOperation serviceInstanceAsyncOperation = (ServiceInstanceAsyncOperation) o;
        return Objects.equals(dashboardUrl, serviceInstanceAsyncOperation.dashboardUrl) && Objects.equals(operation,
                serviceInstanceAsyncOperation.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dashboardUrl, operation);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceInstanceAsyncOperation {\n");

        sb.append("    dashboardUrl: ").append(toIndentedString(dashboardUrl)).append("\n");
        sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
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


