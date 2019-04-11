/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.rest;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The {@link NameBinding} that needs to be applied to filters interceptors
 * or resource classes which are related to OSB API related calls. Any no OSB API
 * related applications or filters should not use this.
 */
@Retention(RetentionPolicy.RUNTIME)
@NameBinding
public @interface OSBAPI
{
}
