/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.rest;

import com.oracle.oci.osb.api.OSBV2API;
import com.oracle.oci.osb.jackson.OSBObjectMapperProvider;
import com.oracle.oci.osb.mbean.BrokerMetrics;
import com.oracle.oci.osb.util.Constants;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class OCIOSBApplication extends ResourceConfig {
    public OCIOSBApplication() {
        super(OSBV2API.class, Health.class, OSBObjectMapperProvider.class, JacksonFeature.class, LoggingFeature.class);
        BrokerMetrics brokerMBean = new BrokerMetrics();
        registerBrokerMBean(brokerMBean);
        register(new OCIOSBApplicationBinder());
        register(new RequestValidationFilter());
        register(new MetricsResponseFilter(brokerMBean));
    }


    private void registerBrokerMBean(BrokerMetrics brokerMBean) {
        try {
            String objectName = Constants.METRICS_MBEAN_OBJ_NAME;
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName mbeanName = new ObjectName(objectName);
            server.registerMBean(brokerMBean, mbeanName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stop the application, this includes any cleanup activities.
     */
    public void stop() {
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(Constants.METRICS_MBEAN_OBJ_NAME));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}