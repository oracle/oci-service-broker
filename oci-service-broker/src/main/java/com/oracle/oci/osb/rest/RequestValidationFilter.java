/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.rest;

import com.fasterxml.jackson.databind.ObjectReader;
import com.oracle.oci.osb.model.Identity;
import com.oracle.oci.osb.util.Constants;
import com.oracle.oci.osb.util.Errors;
import com.oracle.oci.osb.util.OriginatingIdentity;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

import static com.oracle.oci.osb.util.RequestUtil.abortWithError;
import static com.oracle.oci.osb.util.Utils.getLogger;
import static com.oracle.oci.osb.util.Utils.getReader;


/**
 * RequestValidationFilter is the global request validation filter.
 */
@Provider
@OSBAPI
public class RequestValidationFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = getLogger(ContainerRequestContext.class);

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        try {
            validAPIVersion(ctx);
            setOriginatingEntity(ctx);
        } catch (RuntimeException e) {
            LOGGER.finest(e.getMessage());
        }
    }

    private void validAPIVersion(ContainerRequestContext ctx) {
        String version = ctx.getHeaderString(Constants.BROKER_API_VERSION_HEADER);
        if (version != null) {
            String[] tks = version.split("\\.");
            if (tks.length > 1) {
                try {
                    int majorVer = Integer.parseInt(tks[0]);
                    int minorVer = Integer.parseInt(tks[1]);

                    if (majorVer != Constants.BROKER_API_VERSION_MAJOR || minorVer > Constants
                            .BROKER_API_VERSION_MINOR) {
                        abortWithError(ctx, Errors.brokerAPIVersionUnSupported(),
                                "Broker API Version " + version + " not supported. Current Broker Version: " +
                                        Constants.CURRENT_API_VERSION);
                    }
                    LOGGER.finest("Global Request validation successful.");
                    return;
                } catch (NumberFormatException x) {
                    //Ignore as error  will be thrown further down.
                }
            }
            LOGGER.fine("Invalid value for Broker API version: " + version);
            abortWithError(ctx, Errors.brokerAPIVersionHeaderInvalid(),
                    "Invalid value for Broker API version: " + version);
        } else {
            LOGGER.fine("Broker API Version header missing in request");
            abortWithError(ctx, Errors.brokerAPIVersionHeaderMissing(),
                    "Broker API Version header missing in request");
        }
    }

    private void setOriginatingEntity(ContainerRequestContext ctx) {
        //check if path is excluded
        if (isExcludedPath(ctx.getUriInfo().getPath())) {
            return;
        }
        String originatingIdentity = ctx.getHeaderString(Constants.IDENTITY_HEADER);
        if (originatingIdentity == null) {
            LOGGER.fine("Broker Originating Identity header missing in request");
            abortWithError(ctx, Errors.brokerOriginatingIdentityHeaderMissing(),
                    "Broker Originating Identity header missing in request");
        }
        Identity identity = parseIdentity(originatingIdentity);
        if (identity == null) {
            LOGGER.fine("Invalid value for header Broker Originating Identity: " + originatingIdentity);
            abortWithError(ctx, Errors.brokerOriginatingIdentityHeaderInvalid(),
                    "Invalid value for Broker Originating Identity: " + originatingIdentity);
        }
        //Set the identity in thread.
        OriginatingIdentity.setIdentity(identity);
    }

    private boolean isExcludedPath(String path) {
        return (path != null && (path.endsWith("/catalog") || path.endsWith("/last_operation"))) ? true : false;
    }

    private static Identity parseIdentity(String identityHeader) {
        if (identityHeader != null && !identityHeader.isEmpty()
                && identityHeader.startsWith(Constants.PLATFORM_KUBERNETES)) {
            try {
                String base64 = identityHeader.replaceFirst(Constants.PLATFORM_KUBERNETES, "");
                byte[] bytes = Base64.getDecoder().decode(base64.trim());
                String json = new String(bytes, StandardCharsets.UTF_8);
                LOGGER.finest("Originating Identity json : " + json);
                ObjectReader reader = getReader(Identity.class);
                return reader.readValue(json);
            } catch (Exception e) {
                LOGGER.finest(e.getMessage());
            }
        }
        return null;
    }
}