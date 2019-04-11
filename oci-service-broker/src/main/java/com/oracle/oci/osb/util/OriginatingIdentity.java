/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.util;

import com.oracle.oci.osb.model.Identity;

public class OriginatingIdentity {
    private static ThreadLocal<Identity> threadLocal = new ThreadLocal<Identity>();

    public static void setIdentity(Identity identity) {
        threadLocal.set(identity);
    }

    public static String getUserName() {
        Identity identity = threadLocal.get();
        if (identity != null) {
            return identity.getUsername();
        }
        return null;
    }
}
