/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.ociclient;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.oci.osb.util.Utils;
import com.oracle.oci.osb.util.Constants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * SystemPropsAuthProvider constructs the {@code AuthenticationDetailsProvider}
 * by gathering the required details from the system properties.
 */
public class SystemPropsAuthProvider implements AuthProvider {

    private final AuthenticationDetailsProvider authDetails;

    public SystemPropsAuthProvider() {
        SimpleAuthenticationDetailsProvider.SimpleAuthenticationDetailsProviderBuilder authBuilder =
                SimpleAuthenticationDetailsProvider.builder().fingerprint(System.getProperty(Constants.FINGERPRINT))
                        .tenantId(System.getProperty(Constants.TENANCY))
                        .userId(System.getProperty(Constants.USER))
                        .privateKeySupplier(() -> {
                            try {
                                return new FileInputStream(System.getProperty(Constants.PRIVATEKEY));
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        });

        String passphrase = System.getProperty(Constants.PASSPHRASE);
        if (!Utils.isNullOrEmptyString(passphrase)) {
            authBuilder.passphraseCharacters(passphrase.toCharArray());
        }
        authDetails = authBuilder.build();
    }

    @Override
    public AuthenticationDetailsProvider getAuthProvider() {
        return authDetails;
    }
}
