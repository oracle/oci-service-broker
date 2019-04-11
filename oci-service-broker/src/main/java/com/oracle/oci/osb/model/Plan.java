/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Plan {

    private @Valid
    String id = null;

    private @Valid
    String name = null;

    private @Valid
    String description = null;

    private @Valid
    Metadata metadata = null;

    private @Valid
    Boolean free = true;

    private @Valid
    Boolean bindable = null;

    private @Valid
    SchemasObject schemas = null;

    /**
     **/
    public Plan id(String id) {
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
    public Plan name(String name) {
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
    public Plan description(String description) {
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
    public Plan metadata(Metadata metadata) {
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
    public Plan free(Boolean free) {
        this.free = free;
        return this;
    }

    @JsonProperty("free")
    public Boolean isFree() {
        return free;
    }

    public void setFree(Boolean free) {
        this.free = free;
    }

    /**
     **/
    public Plan bindable(Boolean bindable) {
        this.bindable = bindable;
        return this;
    }

    @JsonProperty("bindable")
    public Boolean isBindable() {
        return bindable;
    }

    public void setBindable(Boolean bindable) {
        this.bindable = bindable;
    }

    /**
     **/
    public Plan schemas(SchemasObject schemas) {
        this.schemas = schemas;
        return this;
    }

    @JsonProperty("schemas")
    public SchemasObject getSchemas() {
        return schemas;
    }

    public void setSchemas(SchemasObject schemas) {
        this.schemas = schemas;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Plan plan = (Plan) o;
        return Objects.equals(id, plan.id) && Objects.equals(name, plan.name) && Objects.equals(description, plan
                .description) && Objects.equals(metadata, plan.metadata) && Objects.equals(free, plan.free) &&
                Objects.equals(bindable, plan.bindable) && Objects.equals(schemas, plan.schemas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, metadata, free, bindable, schemas);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Plan {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    free: ").append(toIndentedString(free)).append("\n");
        sb.append("    bindable: ").append(toIndentedString(bindable)).append("\n");
        sb.append("    schemas: ").append(toIndentedString(schemas)).append("\n");
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

