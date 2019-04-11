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
public class ServiceInstanceSchemaObject {

    private @Valid
    SchemaParameters create = null;

    private @Valid
    SchemaParameters update = null;

    /**
     **/
    public ServiceInstanceSchemaObject create(SchemaParameters create) {
        this.create = create;
        return this;
    }

    @JsonProperty("create")
    public SchemaParameters getCreate() {
        return create;
    }

    public void setCreate(SchemaParameters create) {
        this.create = create;
    }

    /**
     **/
    public ServiceInstanceSchemaObject update(SchemaParameters update) {
        this.update = update;
        return this;
    }

    @JsonProperty("update")
    public SchemaParameters getUpdate() {
        return update;
    }

    public void setUpdate(SchemaParameters update) {
        this.update = update;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceInstanceSchemaObject serviceInstanceSchemaObject = (ServiceInstanceSchemaObject) o;
        return Objects.equals(create, serviceInstanceSchemaObject.create) && Objects.equals(update,
                serviceInstanceSchemaObject.update);
    }

    @Override
    public int hashCode() {
        return Objects.hash(create, update);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceInstanceSchemaObject {\n");

        sb.append("    create: ").append(toIndentedString(create)).append("\n");
        sb.append("    update: ").append(toIndentedString(update)).append("\n");
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

