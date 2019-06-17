# Oracle Streaming OCI Service Broker

- [Introduction](#introduction)
- [Plans](#plans)
- [OCI User Permission requirement](#oci-user-permission-requirement)
- [Service Provision Request Parameters](#service-provision-request-parameters)
- [Service Binding](#service-binding)
  - [Request Parameters](#request-parameters)
  - [Response Credentials](#response-credentials)
- [Example](#example)
  - [Kubernetes](#kubernetes)
    - [Provisioning](#provisioning)
    - [Binding](#binding)

## Introduction

OCI Streaming Service (OSS) provides a fully managed, scalable, and durable storage solution for ingesting continuous, high-volume streams of data that users can consume and process in real time. OSS service is also offered via OCI Service Broker thereby making it easy for applications to provision and integrate seamlessly with OSS.

## Plans

The supported plans for this service are

1. standard

## OCI User Permission requirement

The OCI user for OCI Service Broker should have permission `manage` for resource type `streams`

**Sample Policy:**

```plain
Allow group <SERVICE_BROKER_GROUP> to manage streams in compartment <COMPARTMENT_NAME>
```

## Service Provision Request Parameters

The request parameters for Service provisioning are

| Parameter     | Description                                                   | Type   | Mandatory |
| ------------- | ------------------------------------------------------------- | ------ | --------- |
| name          | The name of the stream                                        | string | Yes       |
| compartmentId | The OCID of the compartment to which the stream should belong | string | Yes       |
| partitions    | The number of partitions of the stream                        | number | Yes       |
| freeFormTags  | The free form tags of the bucket                              | object | No        |
| definedTags   | The defined tags of the bucket                                | object | No        |

## Service Binding

### Request Parameters

The Service Binding Request does not have any parameters.

### Response Credentials

| Parameter | Description                                                                    | Type   |
| --------- | ------------------------------------------------------------------------------ | ------ |
| streamId  | The unique identifier of the stream, this can be used to connect to the stream | string |

An OCI user credential can be used to connect to the stream using streamId. The binding request does not create the user.

## Example

### Kubernetes

#### Provisioning

Create a stream

```yaml
apiVersion: servicecatalog.k8s.io/v1beta1
kind: ServiceInstance
metadata:
 name: "InstanceName"
 namespace: "Namespace"
spec:
 clusterServiceClassExternalName: "oss-service"
 clusterServicePlanExternalName: "standard"
 parameters:
   name: "StreamName"
   compartmentId: "CompartmentOCID"
   partitions: "5"
```

#### Binding

Create a Request binding

```yaml
apiVersion: servicecatalog.k8s.io/v1beta1
kind: ServiceBinding
metadata:
 name: "BindingName"
 namespace: "Namespace"
spec:
 instanceRef:
   name: "InstanceName"
```
