/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class ServiceBindingVolumeMountDevice {

    private @Valid
    String volumeId = null;

    private @Valid
    java.lang.Object mountConfig = null;

    /**
     **/
    public ServiceBindingVolumeMountDevice volumeId(String volumeId) {
        this.volumeId = volumeId;
        return this;
    }

    @JsonProperty("volume_id")
    @NotNull
    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    /**
     **/
    public ServiceBindingVolumeMountDevice mountConfig(java.lang.Object mountConfig) {
        this.mountConfig = mountConfig;
        return this;
    }

    @JsonProperty("mount_config")
    public java.lang.Object getMountConfig() {
        return mountConfig;
    }

    public void setMountConfig(java.lang.Object mountConfig) {
        this.mountConfig = mountConfig;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceBindingVolumeMountDevice serviceBindingVolumeMountDevice = (ServiceBindingVolumeMountDevice) o;
        return Objects.equals(volumeId, serviceBindingVolumeMountDevice.volumeId) && Objects.equals(mountConfig,
                serviceBindingVolumeMountDevice.mountConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(volumeId, mountConfig);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceBindingVolumeMountDevice {\n");

        sb.append("    volumeId: ").append(toIndentedString(volumeId)).append("\n");
        sb.append("    mountConfig: ").append(toIndentedString(mountConfig)).append("\n");
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

