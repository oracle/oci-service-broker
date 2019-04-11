/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Extra {

    @JsonProperty("contexttype")
    private List<String> contexttype = null;
    @JsonProperty("tenancyid")
    private List<String> tenancyid = null;

    @JsonProperty("contexttype")
    public List<String> getContexttype() {
        return contexttype;
    }

    @JsonProperty("contexttype")
    public void setContexttype(List<String> contexttype) {
        this.contexttype = contexttype;
    }

    @JsonProperty("tenancyid")
    public List<String> getTenancyid() {
        return tenancyid;
    }

    @JsonProperty("tenancyid")
    public void setTenancyid(List<String> tenancyid) {
        this.tenancyid = tenancyid;
    }
}