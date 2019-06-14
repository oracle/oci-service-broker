/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Constants {
    public static final String PROVISION_OPERATION = "provision";
    public static final String DELETE_OPERATION = "delete";
    public static final String UPDATE_OPERATION = "update";
    public static final String FREE_FORM_TAGS = "freeFormTags";
    public static final String DEFINED_TAGS = "definedTags";
    public static final String METADATA = "metadata";
    public static final String OSB_INSTANCE_ID_LABEL = "OCIOSBServiceInstanceId";
    public static final String COMPARTMENT_ID = "compartmentId";
    public static final String REGION_ID = "regionId";
    public static final String NAME = "name";
    public static final String FINGERPRINT = "fingerprint";
    public static final String USER = "user";
    public static final String TENANCY = "tenancy";
    public static final String PRIVATEKEY = "privatekey";
    public static final String PASSPHRASE = "passphrase";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String TLS_ENABLED = "tlsEnabled";
    public static final String KEY_STORE = "keyStore";
    public static final String KEY_STORE_PASSWORD = "keyStorePassword";
    public static final String KEY_STORE_TYPE = "pkcs12";
    public static final String SSL_VERSION ="TLS";
    public static final String KEY_MGR_TYPE = "SunX509";
    public static final String ADW_CATALOG_JSON = "adw-catalog.json";
    public static final String ATP_CATALOG_JSON = "atp-catalog.json";
    public static final String SVC_BROKER_PREFIX = "oci-osb/";
    public static final String ETCD_SERVERS = "etcd.servers";
    public static final String STORE_TYPE = "storeType";
    public static final String OBJECT_STORE_TYPE = "objectStorage";
    public static final String ETCD_TYPE = "etcd";
    public static final String MEMORY_TYPE = "memory";
    public static final String ETCD_CA_PATH = "CAPath";
    public static final String CLIENT_CERT = "etcdClientCert";
    public static final String CLIENT_KEY = "etcdClientKey";
    public static final String ETCD_TLS_ENABLED = "etcdTlsEnabled";
    public static final String SERVICE_TAG = "serviceTag.";
    public static final String CREATED_BY = "CreatedBy";
    public static final String CREATED_ON_BEHALF = "CreatedOnBehalfOf";
    public static final String OCI_OSB_BROKER = "OCIOpenServiceBroker";
    public static final String KUBERNETES_BEARER_API_TOKEN_FILE = "k8sApiTokenFile";
    public static final String KUBERNETES_SERVICE_HOST = "KUBERNETES_SERVICE_HOST";
    public static final String KUBERNETES_SERVICE_PORT = "KUBERNETES_SERVICE_PORT";
    public static final String NODE_NAME = "NODE_NAME";
    public static final String NODE_API = "https://%s:%s/api/v1/nodes/%s";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_BEARER = "Bearer";
    public static final Object K8S_METADATA = "metadata";
    public static final Object K8S_LABELS = "labels";
    public static final Object K8S_DISPLAYNAME = "displayName";
    public static final String OKE_PREFIX = "oke-";
    public static final String CLUSTER_ID_TAG = "ClusterId";
    public static final String BROKER_API_VERSION_HEADER = "X-Broker-API-Version";
    public static final String IDENTITY_HEADER = "X-Broker-API-Originating-Identity";
    public static final String PLATFORM_KUBERNETES = "kubernetes ";
    public static final int BROKER_API_VERSION_MAJOR = 2;
    public static final int BROKER_API_VERSION_MINOR = 14;
    public static final String CURRENT_API_VERSION = Integer.toString(BROKER_API_VERSION_MAJOR) + "." + Integer
            .toString(BROKER_API_VERSION_MINOR);
    public static final String API_SERVER_CA_CERT = "apiServerCaCert";
    public static final String METRICS_MBEAN_OBJ_NAME = "OCIOpenServiceBroker:type=BrokerMetrics";
    public static final List<String> TLS_PROTOOLS = Collections.unmodifiableList(Arrays.asList("TLSv1.2","TLSv1.2"));
    public static final String ENABLED_CIPHERS_RESOURCE = "enabledCiphers";
    public static final String POD_NAME = "POD_NAME";
    public static final String PROVISIONING = "provisioning";
    public static final String OCID = "ocid";
}
