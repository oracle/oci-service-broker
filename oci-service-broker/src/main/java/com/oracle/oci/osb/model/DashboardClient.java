/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;

public class DashboardClient {

    private @Valid
    String id = null;

    private @Valid
    String secret = null;

    private @Valid
    String redirectUri = null;

    /**
     **/
    public DashboardClient id(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     **/
    public DashboardClient secret(String secret) {
        this.secret = secret;
        return this;
    }

    @JsonProperty("secret")
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     **/
    public DashboardClient redirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    @JsonProperty("redirect_uri")
    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DashboardClient dashboardClient = (DashboardClient) o;
        return Objects.equals(id, dashboardClient.id) && Objects.equals(secret, dashboardClient.secret) && Objects
                .equals(redirectUri, dashboardClient.redirectUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, secret, redirectUri);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DashboardClient {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
        sb.append("    redirectUri: ").append(toIndentedString(redirectUri)).append("\n");
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
}

