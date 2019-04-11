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
public class SchemasObject {

    private @Valid
    ServiceInstanceSchemaObject serviceInstance = null;

    private @Valid
    ServiceBindingSchemaObject serviceBinding = null;

    /**
     **/
    public SchemasObject serviceInstance(ServiceInstanceSchemaObject serviceInstance) {
        this.serviceInstance = serviceInstance;
        return this;
    }

    @JsonProperty("service_instance")
    public ServiceInstanceSchemaObject getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstanceSchemaObject serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    /**
     **/
    public SchemasObject serviceBinding(ServiceBindingSchemaObject serviceBinding) {
        this.serviceBinding = serviceBinding;
        return this;
    }

    @JsonProperty("service_binding")
    public ServiceBindingSchemaObject getServiceBinding() {
        return serviceBinding;
    }

    public void setServiceBinding(ServiceBindingSchemaObject serviceBinding) {
        this.serviceBinding = serviceBinding;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SchemasObject schemasObject = (SchemasObject) o;
        return Objects.equals(serviceInstance, schemasObject.serviceInstance) && Objects.equals(serviceBinding,
                schemasObject.serviceBinding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceInstance, serviceBinding);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SchemasObject {\n");

        sb.append("    serviceInstance: ").append(toIndentedString(serviceInstance)).append("\n");
        sb.append("    serviceBinding: ").append(toIndentedString(serviceBinding)).append("\n");
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

