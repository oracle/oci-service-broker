/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.bmc.Region;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreateBucketDetails;
import com.oracle.bmc.objectstorage.requests.CreateBucketRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import com.oracle.oci.osb.ociclient.SystemPropsAuthProvider;
import com.oracle.oci.osb.util.Constants;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * ObjectStorageStore class provides DataStore implementation that is backed by
 * Oracle Object Storage for persistence.
 */
public class ObjectStorageStore implements DataStore {

    private static final String BUCKET_NAME = "bucketName";

    private static final String NAMESPACE = "namespace";

    private static final String COMPARTMENT_ID = "compartmentId";

    private final ObjectStorageClient objectStorageClient;

    private final ObjectMapper objMapper;

    private final String bucketName;

    private final String namespace;

    public ObjectStorageStore() {
        objectStorageClient = new ObjectStorageClient(new SystemPropsAuthProvider().getAuthProvider());
        objectStorageClient.setRegion(Region.fromRegionId(System.getProperty(Constants.REGION_ID)));
        objMapper = new ObjectMapper();
        bucketName = System.getProperty(BUCKET_NAME);
        namespace = System.getProperty(NAMESPACE);

        try {
            objectStorageClient.createBucket(CreateBucketRequest.builder().namespaceName(namespace)
                    .createBucketDetails(CreateBucketDetails.builder().name(bucketName).compartmentId(System
                            .getProperty(COMPARTMENT_ID)).build()).build());

        } catch (BmcException e) {
            // ignore if the bucket exists, else throw the exception and store
            // could not be instantiated
            if (!(e.getStatusCode() == Response.Status.CONFLICT.getStatusCode())) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void storeServiceData(String instanceId, ServiceData svcData) {
        try {
            byte[] value = objMapper.writeValueAsBytes(svcData);
            uploadByteArray(instanceId, value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServiceData getServiceData(String instanceId) {
        try {
            GetObjectResponse getResponse = objectStorageClient.getObject(GetObjectRequest.builder().namespaceName
                    (namespace).bucketName(bucketName).objectName(instanceId).build());

            try (final InputStream fileStream = getResponse.getInputStream()) {
                return objMapper.readValue(fileStream, ServiceData.class);
            }
        } catch (BmcException e) {
            // if the object cannot be found, simply return null
            if (e.getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
                return null;
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void storeBinding(String bindingId, BindingData bindingData) {
        try {
            byte[] value = objMapper.writeValueAsBytes(bindingData);
            uploadByteArray(bindingId, value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BindingData getBindingData(String bindingId) {
        try {
            GetObjectResponse getResponse = objectStorageClient.getObject(GetObjectRequest.builder().namespaceName
                    (namespace).bucketName(bucketName).objectName(bindingId).build());

            try (final InputStream fileStream = getResponse.getInputStream()) {
                return objMapper.readValue(fileStream, BindingData.class);
            }
        } catch (BmcException e) {
            // if the object cannot be found, simply return null
            if (e.getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
                return null;
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeServiceData(String instanceId) {
        objectStorageClient.deleteObject(DeleteObjectRequest.builder().namespaceName
                (namespace).bucketName(bucketName).objectName(instanceId).build());
    }

    @Override
    public void removeBindingData(String bindingId) {
        objectStorageClient.deleteObject(DeleteObjectRequest.builder().namespaceName
                (namespace).bucketName(bucketName).objectName(bindingId).build());
    }

    @Override
    public boolean isStoreHealthy() {
        return true;
    }

    private void uploadByteArray(String key, byte[] value) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(value);
        PutObjectRequest request = PutObjectRequest.builder().bucketName(bucketName).namespaceName(namespace)
                .objectName(key).contentType("application/json").build();

        UploadManager.UploadRequest uploadDetails = UploadManager.UploadRequest.builder(byteArrayInputStream, value
                .length).allowOverwrite(true).build(request);
        UploadConfiguration uploadConfiguration = UploadConfiguration.builder().build();

        UploadManager uploadManager = new UploadManager(objectStorageClient, uploadConfiguration);
        uploadManager.upload(uploadDetails);
    }
}
