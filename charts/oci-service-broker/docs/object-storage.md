# Object Storage OCI Service Broker

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

OCI Object Storage Service is a storage class tier used for data to which users need fast, immediate, and frequent access. Object Storage Service is also offered via OCI Service Broker thereby making it easy for applications to provision and integrate seamlessly with Object Storage Service.

## Plans

The supported plan for this service are

1. standard
2. archive

These plans map directly to OCI Object Storage [Service Storage Tiers](https://docs.cloud.oracle.com/iaas/Content/Object/Tasks/managingbuckets.htm).

## OCI User Permission requirement

The OCI user for OCI Service Broker should have permission `manage` for resource type `buckets`

**Sample Policy:**

```plain
Allow group <SERVICE_BROKER_GROUP> to manage buckets in compartment <COMPARTMENT_NAME>
```

## Service Provision Request Parameters

The request parameters for Service provisioning are:

| Parameter        | Description                                                  | Type   | Mandatory |
| ---------------- | ------------------------------------------------------------ | ------ | --------- |
| name             | The name of the bucket                                       | string | Yes       |
| namespace        | The namespace of the bucket                                  | string | Yes       |
| compartmentId    | The OCID of the compartment to which the bucket shold belong | string | Yes       |
| freeFormTags     | The free form tags of the bucket                             | object | No        |
| definedTags      | The defined tags of the bucket                               | object | No        |
| metadata         | The metadata of the bucket                                   | object | No        |
| publicAccessType | The public access type of the bucket. Valid values are NoPublicAccess, ObjectRead and ObjectReadWithoutList. Default is NoPublicAccess | string | No        |

## Service Binding

Service Binding is optional in case of this service. OCI User credentials can be used to connect to the Object Storage Service. But if a [Pre-Authenticated Access URI](https://docs.cloud.oracle.com/iaas/Content/Object/Tasks/usingpreauthenticatedrequests.htm?tocpath=Services%7CObject%20Storage%7C_____5) is required, binding can be invoked.

### Request Parameters

| Parameter       | Description                                                  | Type         | Mandatory |
| --------------- | ------------------------------------------------------------ | ------------ | --------- |
| generatePreAuth | Set this flag to true if a pre-authenticated URL needs to be created for the bucket. | boolean      | No        |
| expiryTime      | Expiry time of the pre-authenticated URL, default is 20 years. The format is ISO-8601 | object(time) | No        |

### Response Credentials

| Parameter        | Description                                                  | Type   |
| ---------------- | ------------------------------------------------------------ | ------ |
| preAuthAccessUri | The [Pre-Authenticated Access URI](https://docs.cloud.oracle.com/iaas/Content/Object/Tasks/usingpreauthenticatedrequests.htm?tocpath=Services%7CObject%20Storage%7C_____5) of the bucket | string |

## Example

### Kubernetes

#### Provisioning

Create a bucket

```yaml
apiVersion: servicecatalog.k8s.io/v1beta1
kind: ServiceInstance
metadata:
 name: "InstanceName"
 namespace: "Namespace"
spec:
 clusterServiceClassExternalName: "object-store-service"
 clusterServicePlanExternalName: "standard"
 parameters:
   name: "BucketName"
   compartmentId: "CompartmentOCID"
   namespace: "OCINamespace"
   freeformTags:
      tag: "tag-value"
```

Create an archive storage bucket

```yaml
apiVersion: servicecatalog.k8s.io/v1beta1
kind: ServiceInstance
metadata:
 name: "InstanceName"
 namespace: "Namespace"
spec:
 clusterServiceClassExternalName: "object-store-service"
 clusterServicePlanExternalName: "archive"
 parameters:
   name: "BucketName"
   compartmentId: "CompartmentOCID"
   namespace: "OCINamespace"
```

Create a bucket with list permission for public

```yaml
apiVersion: servicecatalog.k8s.io/v1beta1
kind: ServiceInstance
metadata:
 name: "InstanceName"
 namespace: "Namespace"
spec:
 clusterServiceClassExternalName: "object-store-service"
 clusterServicePlanExternalName: "archive"
 parameters:
   name: "BucketName"
   compartmentId: "CompartmentOCID"
   namespace: "OCINamespace"
   publicAccessType: "ObjectRead"
```

#### Binding

Create a Pre-Authenticated Request binding

```yaml
apiVersion: servicecatalog.k8s.io/v1beta1
kind: ServiceBinding
metadata:
 name: "BindingName"
 namespace: "Namespace"
spec:
 instanceRef:
   name: "InstanceName"
 parameters:
   generatePreAuth: true
```
