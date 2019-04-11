/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.store;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.oci.osb.api.OSBV2API;
import com.oracle.oci.osb.util.Constants;
import com.oracle.oci.osb.util.Errors;
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.oci.osb.util.Utils.getLogger;

public class EtcdStore implements DataStore {

    private KV kvClient;
    private ObjectMapper objMapper;
    private final ByteSequence readyKey;

    private static final Logger LOGGER = getLogger(OSBV2API.class);

    public EtcdStore() {
        String podName = System.getenv(Constants.POD_NAME);
        if (podName == null) {
            throw Errors.podNameNotPresent();
        }

        readyKey = getByteSequence(Constants.SVC_BROKER_PREFIX + podName + "-ready");

        String serverHosts = System.getProperty(Constants.ETCD_SERVERS);
        if (serverHosts == null || serverHosts.trim().equals("")) {
            throw new RuntimeException(Errors.noEtcdServersConfigured());
        }
        String[] servers = serverHosts.split(",");
        if (servers.length == 0) {
            throw new RuntimeException(Errors.noEtcdServersConfigured());
        }

        //Check if we are using embedded etcd
        if(servers.length == 1 && servers[0].contains("localhost")){
            LOGGER.warning("Insecure configuration found. OCI Service Broker seems to have been configured with embedded" +
                    " etcd. Embedded etcd is not recommended to be used in any non-development environment");
        }

        if (Boolean.getBoolean(Constants.ETCD_TLS_ENABLED)) {
            try {
                String capath = System.getProperty(Constants.ETCD_CA_PATH);
                try (InputStream is = new FileInputStream(new File(capath))) {
                    // GRPC forces us to provide an authority which will be used during hostname validation.
                    // During hostname validation, GRPC checks if the authority matches with any
                    // of the provided SAN's or CN in the server certificate chain.
                    // This validation will make sure that GRC connects to a server which can provide the correct
                    // certificate and the hostname has not bee spoofed. Refer
                    // https://github.com/grpc/grpc-java/pull/2662
                    // Since we have a load balanced client side, we just use the first hostanme
                    // as the the authority
                    String authority = new URL(servers[0]).getHost();
                    SslContextBuilder builder = GrpcSslContexts.forClient()
                            .trustManager(is);
                    File certFile = new File(System.getProperty(Constants.CLIENT_CERT));
                    File keyFile = new File(System.getProperty(Constants.CLIENT_KEY));
                    if (certFile.exists() && keyFile.exists()) {
                        builder.keyManager(certFile, keyFile);
                    }
                    Client client = Client.builder()
                            .authority(authority)
                            .sslContext(builder.build())
                            .endpoints(servers).build();

                    kvClient = client.getKVClient();
                    kvClient.put(readyKey, getByteSequence(Boolean.TRUE.toString()));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            LOGGER.warning("Insecure configuration found. etcd server used as store, is not TLS enabled. It " +
                    "is highly recommended to enable TLS.");
            Client client = Client.builder().endpoints(servers)
                    .build();
            kvClient = client.getKVClient();
            kvClient.put(readyKey, getByteSequence(Boolean.TRUE.toString()));
        }
        objMapper = new ObjectMapper();
    }

    @Override
    public void storeServiceData(String instanceId, ServiceData svcData) {
        try {
            byte[] value = objMapper.writeValueAsBytes(svcData);
            kvClient.put(getByteSequence(Constants.SVC_BROKER_PREFIX + instanceId), ByteSequence.from(value));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServiceData getServiceData(String instanceId) {
        try {
            GetResponse response = kvClient.get(getByteSequence(Constants.SVC_BROKER_PREFIX + instanceId)).get();
            List<KeyValue> listKeyValues = response.getKvs();
            if (listKeyValues != null) {
                if (listKeyValues.size() == 1) {
                    return objMapper.readValue(listKeyValues.get(0).getValue().getBytes(), ServiceData.class);
                } else if (listKeyValues.size() > 1) {
                    throw new RuntimeException("Ambiguous key value pair in the etcd database");
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void storeBinding(String bindingId, BindingData bindingData) {
        try {
            byte[] value = objMapper.writeValueAsBytes(bindingData);
            kvClient.put(getByteSequence(Constants.SVC_BROKER_PREFIX + bindingId), ByteSequence.from(value));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BindingData getBindingData(String bindingId) {
        try {
            GetResponse response = kvClient.get(getByteSequence(Constants.SVC_BROKER_PREFIX + bindingId)).get();
            List<KeyValue> listKeyValues = response.getKvs();
            if (listKeyValues != null) {
                if (listKeyValues.size() == 1) {
                    return objMapper.readValue(listKeyValues.get(0).getValue().getBytes(), BindingData.class);
                } else if (listKeyValues.size() > 1) {
                    throw new RuntimeException("Ambiguous key value pair in the etcd database");
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void removeServiceData(String instanceId) {
        kvClient.delete(getByteSequence(Constants.SVC_BROKER_PREFIX + instanceId));
    }

    @Override
    public void removeBindingData(String bindingId) {
        kvClient.delete(getByteSequence(Constants.SVC_BROKER_PREFIX + bindingId));
    }

    @Override
    public boolean isStoreHealthy() {
        try {
            return kvClient.get(readyKey) != null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception occurred while checking health of etcd cluster", e);
        }
        return false;
    }


    private ByteSequence getByteSequence(String str) {
        try {
            return ByteSequence.from(str.getBytes(Constants.CHARSET_UTF8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
