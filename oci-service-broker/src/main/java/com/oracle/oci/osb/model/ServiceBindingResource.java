/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceBindingResource extends AbstractResponse {

    private @Valid
    java.lang.Object credentials = null;

    private @Valid
    String syslogDrainUrl = null;

    private @Valid
    String routeServiceUrl = null;

    private @Valid
    List<ServiceBindingVolumeMount> volumeMounts = new ArrayList<ServiceBindingVolumeMount>();

    private @Valid
    java.lang.Object parameters = null;

    /**
     **/
    public ServiceBindingResource credentials(java.lang.Object credentials) {
        this.credentials = credentials;
        return this;
    }

    @JsonProperty("credentials")
    public java.lang.Object getCredentials() {
        return credentials;
    }

    public void setCredentials(java.lang.Object credentials) {
        this.credentials = credentials;
    }

    /**
     **/
    public ServiceBindingResource syslogDrainUrl(String syslogDrainUrl) {
        this.syslogDrainUrl = syslogDrainUrl;
        return this;
    }

    @JsonProperty("syslog_drain_url")
    public String getSyslogDrainUrl() {
        return syslogDrainUrl;
    }

    public void setSyslogDrainUrl(String syslogDrainUrl) {
        this.syslogDrainUrl = syslogDrainUrl;
    }

    /**
     **/
    public ServiceBindingResource routeServiceUrl(String routeServiceUrl) {
        this.routeServiceUrl = routeServiceUrl;
        return this;
    }

    @JsonProperty("route_service_url")
    public String getRouteServiceUrl() {
        return routeServiceUrl;
    }

    public void setRouteServiceUrl(String routeServiceUrl) {
        this.routeServiceUrl = routeServiceUrl;
    }

    /**
     **/
    public ServiceBindingResource volumeMounts(List<ServiceBindingVolumeMount> volumeMounts) {
        this.volumeMounts = volumeMounts;
        return this;
    }

    @JsonProperty("volume_mounts")
    public List<ServiceBindingVolumeMount> getVolumeMounts() {
        return volumeMounts;
    }

    public void setVolumeMounts(List<ServiceBindingVolumeMount> volumeMounts) {
        this.volumeMounts = volumeMounts;
    }

    /**
     **/
    public ServiceBindingResource parameters(java.lang.Object parameters) {
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
        ServiceBindingResource serviceBindingResource = (ServiceBindingResource) o;
        return Objects.equals(credentials, serviceBindingResource.credentials) && Objects.equals(syslogDrainUrl,
                serviceBindingResource.syslogDrainUrl) && Objects.equals(routeServiceUrl, serviceBindingResource
                .routeServiceUrl) && Objects.equals(volumeMounts, serviceBindingResource.volumeMounts) && Objects
                .equals(parameters, serviceBindingResource.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentials, syslogDrainUrl, routeServiceUrl, volumeMounts, parameters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceBindingResource {\n");

        sb.append("    credentials: ").append(toIndentedString(credentials)).append("\n");
        sb.append("    syslogDrainUrl: ").append(toIndentedString(syslogDrainUrl)).append("\n");
        sb.append("    routeServiceUrl: ").append(toIndentedString(routeServiceUrl)).append("\n");
        sb.append("    volumeMounts: ").append(toIndentedString(volumeMounts)).append("\n");
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

