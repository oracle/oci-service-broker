/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.util;

import javax.ws.rs.WebApplicationException;

public class BrokerHttpException extends WebApplicationException {

    private final String message;

    private final String errorCode;

    /**
     * Constructor for the HTTPException
     *
     * @param statusCode {@code int} for the HTTP status code
     **/
    public BrokerHttpException(int statusCode, String message, String errorCode) {
        super(statusCode);
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

