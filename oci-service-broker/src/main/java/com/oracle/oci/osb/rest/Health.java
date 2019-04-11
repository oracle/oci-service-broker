/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.rest;

import com.oracle.oci.osb.store.DataStoreFactory;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/health")
@Singleton
public class Health {
    @GET
    public Response get() {
        boolean isDataStoreHealthy = DataStoreFactory.getDataStore().isStoreHealthy();
        if (!isDataStoreHealthy) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE.getStatusCode()).build();
        }
        return Response.ok().build();
    }
}
