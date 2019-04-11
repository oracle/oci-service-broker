/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Service {
    private @Valid

    String name = null;

    private @Valid
    String id = null;

    private @Valid
    String description = null;

    private @Valid
    List<String> tags = new ArrayList<String>();

    private @Valid
    List<RequiresEnum> requires = new ArrayList<RequiresEnum>();

    private @Valid
    Boolean bindable = null;

    private @Valid
    Metadata metadata = null;

    private @Valid
    DashboardClient dashboardClient = null;

    private @Valid
    Boolean planUpdateable = null;

    //@JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIgnore
    private Boolean asyncProvision = null;

    //@JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIgnore
    private Boolean asyncBinding = null;

    @JsonIgnore
    private Boolean asyncDelete = null;

    @JsonIgnore
    private Boolean asyncUpdate = null;

    private Boolean instancesRetrievable;

    private Boolean bindingsRetrievable;

    private @Valid
    List<Plan> plans = new ArrayList<Plan>();

    /**
     **/
    public Service name(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("name")
    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     **/
    public Service id(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("id")
    @NotNull
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     **/
    public Service description(String description) {
        this.description = description;
        return this;
    }

    @JsonProperty("description")
    @NotNull
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     **/
    public Service tags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    @JsonProperty("tags")
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     **/
    public Service requires(List<RequiresEnum> requires) {
        this.requires = requires;
        return this;
    }

    @JsonProperty("requires")
    public List<RequiresEnum> getRequires() {
        return requires;
    }

    public void setRequires(List<RequiresEnum> requires) {
        this.requires = requires;
    }

    /**
     **/
    public Service bindable(Boolean bindable) {
        this.bindable = bindable;
        return this;
    }

    @JsonProperty("bindable")
    @NotNull
    public Boolean isBindable() {
        return bindable;
    }

    public void setBindable(Boolean bindable) {
        this.bindable = bindable;
    }

    /**
     **/
    public Service metadata(Metadata metadata) {
        this.metadata = metadata;
        return this;
    }

    @JsonProperty("metadata")
    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     **/
    public Service dashboardClient(DashboardClient dashboardClient) {
        this.dashboardClient = dashboardClient;
        return this;
    }

    @JsonProperty("dashboard_client")
    public DashboardClient getDashboardClient() {
        return dashboardClient;
    }

    public void setDashboardClient(DashboardClient dashboardClient) {
        this.dashboardClient = dashboardClient;
    }

    /**
     **/
    public Service planUpdateable(Boolean planUpdateable) {
        this.planUpdateable = planUpdateable;
        return this;
    }

    @JsonProperty("plan_updateable")
    public Boolean isPlanUpdateable() {
        return planUpdateable;
    }

    public void setPlanUpdateable(Boolean planUpdateable) {
        this.planUpdateable = planUpdateable;
    }

    /**
     **/
    public Service plans(List<Plan> plans) {
        this.plans = plans;
        return this;
    }

    @JsonProperty("plans")
    @NotNull
    public List<Plan> getPlans() {
        return plans;
    }

    public void setPlans(List<Plan> plans) {
        this.plans = plans;
    }

    @JsonIgnore
    public Boolean isAsyncProvision() {
        return asyncProvision;
    }

    @JsonProperty("asyncProvision")
    public void setAsyncProvision(Boolean asyncProvision) {
        this.asyncProvision = asyncProvision;
    }

    @JsonIgnore
    public Boolean isAsyncBinding() {
        return asyncBinding;
    }

    @JsonProperty("asyncBinding")
    public void setAsyncBinding(Boolean asyncBinding) {
        this.asyncBinding = asyncBinding;
    }

    @JsonIgnore
    public Boolean isAsyncDelete() {
        return asyncDelete;
    }

    @JsonProperty("asyncDelete")
    public void setAsyncDelete(Boolean asyncDelete) {
        this.asyncDelete = asyncDelete;
    }

    @JsonIgnore
    public Boolean isAsyncUpdate() {
        return asyncUpdate;
    }

    @JsonProperty("asyncUpdate")
    public void setAsyncUpdate(Boolean asyncUpdate) {
        this.asyncUpdate = asyncUpdate;
    }

    @JsonProperty("instances_retrievable")
    public Boolean isInstancesRetrievable() {
        return instancesRetrievable;
    }

    public void setInstancesRetrievable(Boolean instancesRetrievable) {
        this.instancesRetrievable = instancesRetrievable;
    }

    @JsonProperty("bindings_retrievable")
    public Boolean isBindingsRetrievable() {
        return bindingsRetrievable;
    }

    public void setBindingsRetrievable(Boolean bindingsRetrievable) {
        this.bindingsRetrievable = bindingsRetrievable;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Service service = (Service) o;
        return Objects.equals(name, service.name) && Objects.equals(id, service.id) && Objects.equals(description,
                service.description) && Objects.equals(tags, service.tags) && Objects.equals(requires, service
                .requires) && Objects.equals(bindable, service.bindable) && Objects.equals(metadata, service
                .metadata) && Objects.equals(dashboardClient, service.dashboardClient) && Objects.equals
                (planUpdateable, service.planUpdateable) && Objects.equals(plans, service.plans)
                && Objects.equals(asyncBinding, service.asyncBinding) && Objects.equals(asyncProvision,
                service.asyncProvision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, description, tags, requires, bindable, metadata, dashboardClient,
                planUpdateable, plans);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Service {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
        sb.append("    requires: ").append(toIndentedString(requires)).append("\n");
        sb.append("    bindable: ").append(toIndentedString(bindable)).append("\n");
        sb.append("    asyncProvision: ").append(toIndentedString(asyncProvision)).append("\n");
        sb.append("    asyncBinding: ").append(toIndentedString(asyncBinding)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    dashboardClient: ").append(toIndentedString(dashboardClient)).append("\n");
        sb.append("    planUpdateable: ").append(toIndentedString(planUpdateable)).append("\n");
        sb.append("    plans: ").append(toIndentedString(plans)).append("\n");
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

    public enum RequiresEnum {

        SYSLOG_DRAIN(String.valueOf("syslog_drain")), ROUTE_FORWARDING(String.valueOf("route_forwarding")),
        VOLUME_MOUNT(String.valueOf("volume_mount"));

        private String value;

        RequiresEnum(String v) {
            value = v;
        }

        public static RequiresEnum fromValue(String v) {
            for (RequiresEnum b : RequiresEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
            return null;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}

