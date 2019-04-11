/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class ServiceBindingVolumeMount {

    private @Valid
    String driver = null;

    private @Valid
    String containerDir = null;

    private @Valid
    ModeEnum mode = null;

    private @Valid
    DeviceTypeEnum deviceType = null;

    private @Valid
    ServiceBindingVolumeMountDevice device = null;

    /**
     **/
    public ServiceBindingVolumeMount driver(String driver) {
        this.driver = driver;
        return this;
    }

    @JsonProperty("driver")
    @NotNull
    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     **/
    public ServiceBindingVolumeMount containerDir(String containerDir) {
        this.containerDir = containerDir;
        return this;
    }

    @JsonProperty("container_dir")
    @NotNull
    public String getContainerDir() {
        return containerDir;
    }

    public void setContainerDir(String containerDir) {
        this.containerDir = containerDir;
    }

    /**
     **/
    public ServiceBindingVolumeMount mode(ModeEnum mode) {
        this.mode = mode;
        return this;
    }

    @JsonProperty("mode")
    @NotNull
    public ModeEnum getMode() {
        return mode;
    }

    public void setMode(ModeEnum mode) {
        this.mode = mode;
    }

    /**
     **/
    public ServiceBindingVolumeMount deviceType(DeviceTypeEnum deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    @JsonProperty("device_type")
    @NotNull
    public DeviceTypeEnum getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceTypeEnum deviceType) {
        this.deviceType = deviceType;
    }

    /**
     **/
    public ServiceBindingVolumeMount device(ServiceBindingVolumeMountDevice device) {
        this.device = device;
        return this;
    }

    @JsonProperty("device")
    @NotNull
    public ServiceBindingVolumeMountDevice getDevice() {
        return device;
    }

    public void setDevice(ServiceBindingVolumeMountDevice device) {
        this.device = device;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceBindingVolumeMount serviceBindingVolumeMount = (ServiceBindingVolumeMount) o;
        return Objects.equals(driver, serviceBindingVolumeMount.driver) && Objects.equals(containerDir,
                serviceBindingVolumeMount.containerDir) && Objects.equals(mode, serviceBindingVolumeMount.mode) &&
                Objects.equals(deviceType, serviceBindingVolumeMount.deviceType) && Objects.equals(device,
                serviceBindingVolumeMount.device);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driver, containerDir, mode, deviceType, device);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServiceBindingVolumeMount {\n");

        sb.append("    driver: ").append(toIndentedString(driver)).append("\n");
        sb.append("    containerDir: ").append(toIndentedString(containerDir)).append("\n");
        sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
        sb.append("    deviceType: ").append(toIndentedString(deviceType)).append("\n");
        sb.append("    device: ").append(toIndentedString(device)).append("\n");
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

    public enum ModeEnum {

        R(String.valueOf("r")), RW(String.valueOf("rw"));

        private String value;

        ModeEnum(String v) {
            value = v;
        }

        public static ModeEnum fromValue(String v) {
            for (ModeEnum b : ModeEnum.values()) {
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

    public enum DeviceTypeEnum {

        SHARED(String.valueOf("shared"));

        private String value;

        DeviceTypeEnum(String v) {
            value = v;
        }

        public static DeviceTypeEnum fromValue(String v) {
            for (DeviceTypeEnum b : DeviceTypeEnum.values()) {
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

