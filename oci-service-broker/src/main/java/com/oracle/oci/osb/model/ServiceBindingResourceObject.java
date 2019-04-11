/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;

public class ServiceBindingResourceObject {

    private @Valid
    String appGuid = null;

    private @Valid
    String route = null;

    /**
     **/
    public ServiceBindingResourceObject appGuid(String appGuid) {
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
    public ServiceBindingResourceObject route(String route) {
        this.route = route;
        return this;
    }

    @JsonProperty("route")
    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceBindingResourceObject serviceBindingResourceObject = (ServiceBindingResourceObject) o;
        return Objects.equals(appGuid, serviceBindingResourceObject.appGuid) && Objects.equals(route,
                serviceBindingResourceObject.route);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appGuid, route);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceBindingResourceObject {\n");

        sb.append("    appGuid: ").append(toIndentedString(appGuid)).append("\n");
        sb.append("    route: ").append(toIndentedString(route)).append("\n");
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

