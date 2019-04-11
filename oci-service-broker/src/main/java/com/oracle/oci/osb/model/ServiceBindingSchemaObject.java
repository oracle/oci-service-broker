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
public class ServiceBindingSchemaObject {

    private @Valid
    SchemaParameters create = null;

    /**
     **/
    public ServiceBindingSchemaObject create(SchemaParameters create) {
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

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceBindingSchemaObject serviceBindingSchemaObject = (ServiceBindingSchemaObject) o;
        return Objects.equals(create, serviceBindingSchemaObject.create);
    }

    @Override
    public int hashCode() {
        return Objects.hash(create);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceBindingSchemaObject {\n");

        sb.append("    create: ").append(toIndentedString(create)).append("\n");
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

