/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.rest;

import com.oracle.oci.osb.ociclient.AuthProvider;
import com.oracle.oci.osb.ociclient.SystemPropsAuthProvider;
import com.oracle.oci.osb.store.DataStore;
import com.oracle.oci.osb.store.MemoryStore;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

public class OCIOSBApplicationBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(SystemPropsAuthProvider.class).to(AuthProvider.class);
        bind(MemoryStore.class).to(DataStore.class).in(Singleton.class);
    }
}
