/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;

public class ServiceInstancePreviousValues {

    private @Valid
    String serviceId = null;

    private @Valid
    String planId = null;

    private @Valid
    String organizationId = null;

    private @Valid
    String spaceId = null;

    /**
     **/
    public ServiceInstancePreviousValues serviceId(String serviceId) {
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
    public ServiceInstancePreviousValues planId(String planId) {
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
    public ServiceInstancePreviousValues organizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    @JsonProperty("organization_id")
    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     **/
    public ServiceInstancePreviousValues spaceId(String spaceId) {
        this.spaceId = spaceId;
        return this;
    }

    @JsonProperty("space_id")
    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceInstancePreviousValues serviceInstancePreviousValues = (ServiceInstancePreviousValues) o;
        return Objects.equals(serviceId, serviceInstancePreviousValues.serviceId) && Objects.equals(planId,
                serviceInstancePreviousValues.planId) && Objects.equals(organizationId, serviceInstancePreviousValues
                .organizationId) && Objects.equals(spaceId, serviceInstancePreviousValues.spaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, planId, organizationId, spaceId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceInstancePreviousValues {\n");

        sb.append("    serviceId: ").append(toIndentedString(serviceId)).append("\n");
        sb.append("    planId: ").append(toIndentedString(planId)).append("\n");
        sb.append("    organizationId: ").append(toIndentedString(organizationId)).append("\n");
        sb.append("    spaceId: ").append(toIndentedString(spaceId)).append("\n");
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

