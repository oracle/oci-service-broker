/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.util;

import com.oracle.oci.osb.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Common Exceptions to be thrown in case of of error conditions.
 */
public class Errors {

    public static final String MISSING_PARAMETERS = "MissingParameters";
    public static final String MISSING_PARAMETER = "MissingParameter";
    public static final String INVALID_PARAMETERS = "InvalidParameters";
    public static final String INVALID_PARAMETER = "InvalidParameter";
    public static final String PROVISION_SYNCHRONOUS = "ProvisionSynchronous";
    public static final String BINDING_SYNCHRONOUS = "BindingSynchronous";
    public static final String NOT_FETCHABLE = "NotFetchable";
    public static final String INSTANCE_DOES_NOT_EXIST = "InstanceDoesNotExist";
    public static final String INSTANCE_DELETED = "InstanceDeleted";
    public static final String INSTANCE_NOT_READY = "InstanceNotReady";
    public static final String PLAN_DOES_NOT_EXIST = "PlanDoesNotExist";
    public static final String SERVICE_DOES_NOT_EXIST = "ServiceDoesNotExist";
    public static final String PLAN_NOT_SUPPORTED = "PlanNotSupported";
    public static final String BINDING_DOES_NOT_EXIST = "BindingDoesNotExist";
    public static final String OPERATION_NOT_PROVIDED = "OperationNotProvided";
    public static final String BINDING_DELETED = "BindingDeleted";
    public static final String SERVICE_DATA_MISSING = "ServiceDataMissing";
    public static final String BINDING_DATA_MISSING = "BindingDataMissing";
    public static final String NOT_RETRIEVABLE = "NotRetrievable";
    public static final String NON_BINDABLE = "NonBindable";
    public static final String PLAN_NOT_PROVIDED = "PlanNotProvided";
    public static final String INVALID_PLAN = "InvalidPlan";
    public static final int BAD_REQUEST_STATUS_CODE = Response.Status.BAD_REQUEST.getStatusCode();
    public static final String ASYNC_REQUIRED = "AsyncRequired";
    public static final String MISSING_API_VERSION_HEADER = "MissingAPIVersionHeader";
    public static final String INVALID_API_VERSION = "InvalidAPIVersion";
    public static final String MISSING_ORIGINATING_IDENTITY_HEADER = "MissingOriginatingIdentityHeader";
    public static final String INVALID_ORIGINATING_IDENTITY = "InvalidOriginatingIdentity";
    public static final String UNSUPPORTED_API_VERSION = "UnsupportedAPIVersion";
    public static final String UNSUPPORTED_OPERATION= "UnsupportedOperation";


    /**
     * @return exception to be thrown when the request is missing some mandatory parameters
     */
    public static BrokerHttpException missingParameters() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "Missing Parameters", MISSING_PARAMETERS);
    }

    /**
     *
     * @param paramName missing parameter name.
     * @return exception to be thrown when the request is missing some mandatory parameters.
     */
    public static BrokerHttpException missingParameter(String paramName) {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "Missing Parameter " + paramName, MISSING_PARAMETER);
    }

    /**
     * @return exception to be thrown then there was an asynchronous provision request to a synchronous service
     */
    public static BrokerHttpException provisionSynchronousError() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE
                , "Provision is a synchronous operation", PROVISION_SYNCHRONOUS);
    }

    /**
     * @return exception to be thrown when the provided parameters are invalid
     */
    public static BrokerHttpException invalidParameters() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "Invalid Parameters", INVALID_PARAMETERS);
    }

    /**
     * @param paramName missing parameter name.
     * @return exception to be thrown when the provided parameters are invalid
     */
    public static BrokerHttpException invalidParameter(String paramName) {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "Invalid Parameter " + paramName, INVALID_PARAMETER);
    }

    /**
     * @return exception to be thrown if a binding with same id already exists
     */
    public static BrokerHttpException bindingAlreadyExistsError() {
        return new BrokerHttpException(Response.Status.CONFLICT.getStatusCode(),
                "Binding Already Exists",
                "BindingExist");
    }

    /**
     * @return exception to be thrown when there was an asynchronous binding request to a synchronously bindable service
     */
    public static BrokerHttpException bindingSynchronousError() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE
                , "Binding is a synchronous operation", BINDING_SYNCHRONOUS);
    }

    /**
     * @return exception to be thrown if the binding is not fetchable
     */
    public static BrokerHttpException bindingNotFetchable() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE
                , "Binding is not fetchable", NOT_FETCHABLE);
    }

    /**
     * @return exception to be thrown if the instance with provided id does not exist
     */
    public static BrokerHttpException instanceDoesNotExistException() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "The service instance does not exist", INSTANCE_DOES_NOT_EXIST);
    }

    /**
     * @return exception to be thrown if the instance with provided id has been deleted
     */
    public static BrokerHttpException instanceDeletedError() {
        return new BrokerHttpException(Response.Status.GONE.getStatusCode(),
                "The service instance has been deleted", INSTANCE_DELETED);
    }

    /**
     * @return exception to be thrown if the instance is not ready yet.
     */
    public static BrokerHttpException instanceNotReadyError() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "The service instance is not ready yet", INSTANCE_NOT_READY);
    }

    /**
     * @return exception to be thrown if the provided service id does not match the existing service id
     */
    public static BrokerHttpException invalidServiceError() {
        throw new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "The provided service_id does not match the existing service_id", "InvalidService");
    }

    /**
     * @return exception to be thrown if a plan with provided id does not exist
     */
    public static BrokerHttpException planDoesNotExistError() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "The plan does not exist", PLAN_DOES_NOT_EXIST);
    }

    /**
     * @return exception to be thrown if a plan with provided id does not exist
     */
    public static BrokerHttpException unSupportedOperation() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "The update request is not supported for binding only Instance.", UNSUPPORTED_OPERATION);
    }

    /**
     * @return exception to be thrown if a service with provided id does not exist
     */
    public static BrokerHttpException serviceDoesNotExistError() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "The service does not exist", SERVICE_DOES_NOT_EXIST);
    }

    /**
     * @return exception to be thrown if the service does not support the provided plan
     */
    public static BrokerHttpException planNotSupportByService() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "Plan is not supported by the service", PLAN_NOT_SUPPORTED);
    }

    /**
     * @return exception to be thrown if the plan id is not provided in the request
     */
    public static BrokerHttpException planNotProvidedError() {
        return new BrokerHttpException(Response.Status.BAD_REQUEST.getStatusCode(),
                "Plan is not provided in the request", PLAN_NOT_PROVIDED);
    }

    /**
     * @return exception to be thrown if the provided plan id does not match the existing plan id
     */
    public static BrokerHttpException invalidPlanError() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "The provided plan_id does not match the existing plan_id", INVALID_PLAN);
    }

    /**
     * @return exception to be thrown when the service binding does not exist
     */
    public static BrokerHttpException bindingDoesNotExistError() {
        return new BrokerHttpException(Response.Status.GONE.getStatusCode(),
                "The service binding does not exist", BINDING_DOES_NOT_EXIST);
    }

    /**
     * @return thrown in case 2 services starts with same id
     */
    public static RuntimeException serviceWithSameId() {
        return new RuntimeException("Service with same id already exists");
    }

    /**
     * @return exception to be thrown when 2 services have plans with same id
     */
    public static RuntimeException planWithSameId() {
        return new RuntimeException("Plan with same id already exists");
    }

    /**
     * @return exception to be thrown when the operation paremetr is not provided in the last operation request
     */
    public static BrokerHttpException operationParameterNotProvided() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "The operation parameter is not provided", OPERATION_NOT_PROVIDED);
    }

    /**
     * @return exception to be thrown if the binding has been deleted
     */
    public static BrokerHttpException bindingDeletedError() {
        return new BrokerHttpException(Response.Status.GONE.getStatusCode(),
                "The service binding has been deleted", BINDING_DELETED);
    }

    /**
     * @return exception to be thrown when Service Data is missing from the provision response
     */
    public static Exception svcDataMissing() {
        return new BrokerHttpException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Service data is not present in the response", SERVICE_DATA_MISSING);
    }

    /**
     * @return  exception to be thrown when Binding Data is missing from the provision response
     */
    public static Exception bindingDataMissing() {
        return new BrokerHttpException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Binding data is not present in the response", BINDING_DATA_MISSING);
    }

    /**
     * @return  exception to be thrown when binding with same id exists
     */
    public static Exception bindingWithSameIdExists() {
        return new BrokerHttpException(Response.Status.CONFLICT.getStatusCode(),
                "Binding with same id exists for another service instance", "BindingExists");
    }

    /**
     * @return exception to be thrown when the caller must support async behaviour
     */
    public static Exception asyncRequired() {
        return new BrokerHttpException(422,
                "This Service Plan requires client support for asynchronous service operations", ASYNC_REQUIRED);
    }

    /**
     * @return exception to be thrown when Binding Data is missing from the provision response
     */
    public static Exception instancesNotRetrievable() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "This Service instance is not retrievable", NOT_RETRIEVABLE);
    }

    /**
     * @return exception to be thrown if the binding is not retrievable
     */
    public static Exception bindingNotRetrievable() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "This Service binding is not retrievable", NOT_RETRIEVABLE);
    }

    /**
     * @return exception to be thrown if the service is not bindab;e
     */
    public static Exception unbindable() {
        return new BrokerHttpException(BAD_REQUEST_STATUS_CODE,
                "This Service does not require a binding", NON_BINDABLE);
    }

    /**
     * @return exception to be thrown when Broker API version header is missing in the request.
     */
    public static Response brokerAPIVersionHeaderMissing() {
        return Response.status(Response.Status
                .PRECONDITION_FAILED.getStatusCode())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(MISSING_API_VERSION_HEADER, "Broker API Version Header " + Constants
                        .BROKER_API_VERSION_HEADER + " is mandatory but missing in the request"))
                .build();
    }


    /**
     * @return exception to be thrown when Broker API version header is having invalid value.
     */
    public static Response brokerAPIVersionHeaderInvalid() {
          return Response.status(Response.Status
                .PRECONDITION_FAILED.getStatusCode())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(INVALID_API_VERSION, "Invalid Broker API Version value"))
                .build();
    }

    /**
     * @return exception to be thrown when Broker Originating identity header is having invalid value.
     */
    public static Response brokerOriginatingIdentityHeaderInvalid() {
        return Response.status(Response.Status
                .PRECONDITION_FAILED.getStatusCode())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(INVALID_ORIGINATING_IDENTITY, "Invalid "+ Constants.IDENTITY_HEADER + " header value"))
                .build();
    }

    /**
     * @return exception to be thrown when Broker Originating identity header is missing in the request.
     */
    public static Response brokerOriginatingIdentityHeaderMissing() {
        return Response.status(Response.Status
                .PRECONDITION_FAILED.getStatusCode())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(MISSING_ORIGINATING_IDENTITY_HEADER, "Broker Originating Identity Header " + Constants
                        .IDENTITY_HEADER + " is mandatory but missing in the request"))
                .build();
    }

    /**
     * @return exception to be thrown when Broker API version in the request is not supported.
     */
    public static Response brokerAPIVersionUnSupported() {
        return Response.status(Response.Status
                .PRECONDITION_FAILED.getStatusCode())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(UNSUPPORTED_API_VERSION, "Unsupported Broker API Version. OCI Service " +
                        "Broker Version is : " + Constants.CURRENT_API_VERSION))
                .build();
    }


    /**
     * @return exception to be thrown when the keystore file cannot be found
     */
    public static Exception invalidKeystoreFile() {
        return new RuntimeException("Invalid keystore file");
    }

    /**
     * @return exception to be thrown when the keystore password is not provided
     */
    public static Exception invalidKeystorePassword() {
        return new RuntimeException("Invalid keystore password");
    }

    /**
     * @return exception to be thrown when the store type provided is invalid
     */
    public static RuntimeException invalidStoreTypeError() {
        return new RuntimeException("Invalid Store Type");
    }

    /**
     * @return exception to be thrown when no etcd servers are configured
     */
    public static Exception noEtcdServersConfigured() {
        return new RuntimeException("No etcd servers configured");
    }

    /**
     * @return exception to be thrown when pod name is not passed on to the container as an environment variable
     */
    public static RuntimeException podNameNotPresent() {
        return new RuntimeException("Pod name is not passed on as an environment variable");
    }
}
