/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.ociclient;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;

/**
 * Interface for obtaining Authentication Provider for interacting with OCI
 * Services.
 */
public interface AuthProvider {
    /**
     * Returns instance of {@code AuthenticationDetailsProvider}
     *
     * @return AuthenticationDetailsProvider
     */
    AuthenticationDetailsProvider getAuthProvider();
}
