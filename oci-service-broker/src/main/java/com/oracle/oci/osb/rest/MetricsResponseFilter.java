/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.rest;

import com.oracle.oci.osb.mbean.BrokerMetrics;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * {@link ContainerResponseFilter} to update the broker metrics.
 */
@Provider
@OSBAPI
public class MetricsResponseFilter implements ContainerResponseFilter {

    /**
     * The {@link com.oracle.oci.osb.mbean.BrokerMetricsMBean} to update.
     */
    private final BrokerMetrics brokerMBean;

    public MetricsResponseFilter(BrokerMetrics brokerMBean) {
        this.brokerMBean = brokerMBean;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        String uri = requestContext.getUriInfo().getPath();
        switch (requestContext.getMethod().toLowerCase()) {
            case "put":
                addToPutMetrics(uri, responseContext);
                break;
            case "delete":
                addToDeleteMetrics(uri, responseContext);
                break;
            case "patch":
                addToPatchMetrics(uri, responseContext);
                break;
            case "get":
                addToGetMetrics(uri, responseContext);
                break;
            default:
                break;
        }
        // increment total request count
        brokerMBean.incrementTotalRequestCount();
        int responseCode = responseContext.getStatus();
        if (isFailed(responseCode)) {
            brokerMBean.incrementFailedTotalRequestCount();
        }
    }

    private void addToGetMetrics(String uri, ContainerResponseContext responseContext) {
        int responseCode = responseContext.getStatus();
        // there are  types of get operations, last_operation for for binding/instances
        // and also get operation for bindings/instances. Increment the correct metrics
        // based on the request uri
        if (uri.contains("last_operation")) {
            if (uri.contains("service_bindings")) {
                brokerMBean.incrementServiceBindingLastOperationCount();
                if (isFailed(responseCode)) {
                    brokerMBean.incrementFailedServiceBindingLastOperationCount();
                }
            } else if (uri.contains("service_instances")){
                brokerMBean.incrementServiceLastOperationCount();
                if (isFailed(responseCode)) {
                    brokerMBean.incrementFailedServiceLastOperationCount();
                }
            }
        } else {
            if (uri.contains("service_bindings")) {
                brokerMBean.incrementServiceBindingGetOperationCount();
                if (isFailed(responseCode)) {
                    brokerMBean.incrementFailedServiceBindinGetOperationCount();
                }
            } else if (uri.contains("service_instances")){
                brokerMBean.incrementInstanceGetOperationCount();
                if (isFailed(responseCode)) {
                    brokerMBean.incrementFailedInstanceGetOperationCount();
                }
            }
        }
    }

    private void addToPutMetrics(String uri, ContainerResponseContext responseContext) {
        // there are 2 types of put operation, create an instance or create a binding
        int responseCode = responseContext.getStatus();
        if (uri.contains("service_bindings")) {
            brokerMBean.incrementServiceBindingRequestCount();
            if (isFailed(responseCode)) {
                brokerMBean.incrementFailedServiceBindingRequestCount();
            }
        } else if (uri.contains("service_instances")) {
            brokerMBean.incrementProvisionRequestCount();
            if (isFailed(responseCode)) {
                brokerMBean.incrementFailedProvisionRequestCount();
            }

        }
    }

    private void addToDeleteMetrics(String uri, ContainerResponseContext responseContext) {
        // 2 types of delete operation, delete instance or delete binding
        int responseCode = responseContext.getStatus();
        if (uri.contains("service_bindings")) {
            brokerMBean.incrementUnBindRequestCount();
            if (isFailed(responseCode)) {
                brokerMBean.incrementFailedUnBindRequestCount();
            }
        } else if (uri.contains("service_instances")) {
            brokerMBean.incrementDeprovisionRequestCount();
            if (isFailed(responseCode)) {
                brokerMBean.incrementFailedDeprovisionRequestCount();
            }

        }
    }

    private void addToPatchMetrics(String uri, ContainerResponseContext responseContext) {
        // only one type of patch operation, which is to update an instance
        int responseCode = responseContext.getStatus();
        brokerMBean.incrementUpdateRequestCount();
        if (isFailed(responseCode)) {
            brokerMBean.incrementFailedUpdateRequestCount();
        }
    }


    private boolean isFailed(int responseCode) {
        return responseCode < 200 || responseCode >= 300;
    }
}
