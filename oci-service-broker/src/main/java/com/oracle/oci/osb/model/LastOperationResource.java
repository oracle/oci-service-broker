/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LastOperationResource extends AbstractResponse {

    private @Valid
    StateEnum state = null;

    private @Valid
    String description = null;

    /**
     **/
    public LastOperationResource state(StateEnum state) {
        this.state = state;
        return this;
    }

    @JsonProperty("state")
    @NotNull
    public StateEnum getState() {
        return state;
    }

    public void setState(StateEnum state) {
        this.state = state;
    }

    /**
     **/
    public LastOperationResource description(String description) {
        this.description = description;
        return this;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LastOperationResource lastOperationResource = (LastOperationResource) o;
        return Objects.equals(state, lastOperationResource.state) && Objects.equals(description,
                lastOperationResource.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, description);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LastOperationResource {\n");

        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum StateEnum {

        IN_PROGRESS(String.valueOf("in progress")), SUCCEEDED(String.valueOf("succeeded")), FAILED(String.valueOf
                ("failed"));

        private String value;

        StateEnum(String v) {
            value = v;
        }

        public static StateEnum fromValue(String v) {
            for (StateEnum b : StateEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
            return null;
        }

        @JsonValue
        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}

