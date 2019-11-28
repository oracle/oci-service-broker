# Guidelines for Securing OCI Service Broker

* [Restrict access to Service Catalog resources using RBAC](#restrict-access-to-service-catalog-resources-using-rbac)
  * [Read-only access](#read-only-access)
  * [Full access](#full-access)
* [TLS enable endpoint](#tls-enable-endpoint)
* [Restrict access of the OCI User used by OCI Service Broker](#restrict-access-of-the-oci-user-used-by-oci-service-broker)
  * [OCI User Capabilities](#oci-user-capabilities)
  * [Policies to allow access to Services](#policies-to-allow-access-to-services)
  * [Restrict the permissions only to the required Compartments](#restrict-the-permissions-only-to-the-required-compartments)
* [Limit access to OCI Service Broker endpoint using Networkpolicy](#limit-access-to-oci-service-broker-endpoint-using-networkpolicy)
  * [Strategy 1-Allow access only for Service Catalog running in the same namespace](#strategy-1-allow-access-only-for-service-catalog-running-in-the-same-namespace)
  * [Strategy 2-Allow access only for Service Catalog from any namespace](#strategy-2-allow-access-only-for-service-catalog-from-any-namespace)
  * [Strategy 3-Allow access for any pods running in a certain namespace](#strategy-3-allow-access-for-any-pods-running-in-a-certain-namespace)
* [Using secured etcd Cluster](#using-secured-etcd-cluster)
* [Configuring Sensitive values in the Parameters](#configuring-sensitive-values-in-the-parameters)

## Restrict access to Service Catalog resources using RBAC

The users can request OCI Service Broker for CUD(Create/Update/Delete) operation on OCI service instances by doing CUD operations on Kubernetes resource `ServiceInstance`. Hence access to this resource should be controlled. The cluster admin should provide CUD operation permissions on `ServiceInstance` to only the required entities (users/service accounts). Similarly, access should be controlled for `ServiceBinding` resource too.

The samples files for configuring rbac are available in directory [`samples/rbac/`](charts/oci-service-broker/samples/rbac)

### Read-only access

Some users might require permission to just view/list the service instances and bindings created. For this, admin can create a role that gives read-only access to the required entities for `ServiceInstance` and `ServiceBinding` resources.

* Create a `ClusterRole` that provides read access on `ServiceInstance` and `ServiceBinding`

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: servicecatalog.k8s.io:servicebroker:read-only
rules:
- apiGroups: ["servicecatalog.k8s.io"]
  resources: ["ClusterServiceBroker","ClusterServiceClass","ClusterServicePlan"]
  verbs: ["*"]
```

* Create `RoleBinding` to provide read-only access to a particular entity

```bash
kubectl create rolebinding servicecatalog.k8s.io:service-read:usera --clusterrole=servicecatalog.k8s.io:servicebroker:read-only --user=<USER>
```

### Full access

The below role gives full permissions, including creating/deleting Services and getting binding details to access the Services. Hence this permission should be given only to limited entites that are required to manage services.

* Create `ClusterRole` that provides CRUD operations on `ServiceInstance` and `ServiceBinding`

```yaml
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: servicecatalog.k8s.io:service:all
rules:
- apiGroups: ["servicecatalog.k8s.io"]
  resources: ["ServiceInstance","ServiceBinding"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
```

* Create `RoleBinding` to provide CRUD access to a particular entity

```bash
kubectl create rolebinding servicecatalog.k8s.io:service-all:usera --clusterrole=servicecatalog.k8s.io:service:all --user=<USER>
```

**Note:** Creating `RoleBinding` provides the entity the access mentioned in the role only in the current namespace. This is recommended instead of using `ClusterRoleBinding` which provides the same access but across all namespaces.

## TLS enable endpoint

OCI Service Broker chart by default enables TLS for Service Broker endpoint. An option is provided to disable TLS just for running in non-development environment for testing and development purposes only. TLS should never be dsiabled in any non-development environment.

It is also recommened to use a proper CA Certifcate instead of self-signed certifcate in production. Please refer installation document [here](installation.md#enable-tls) for configuring TLS for OCI Service Broker.

## Restrict access of the OCI User used by OCI Service Broker

The OCI Service Broker requires an OCI User credential to manage the OCI services.  It is strongly recommended to create a separate OCI user/group for OCI Service Broker and provide only the permissions that are required by the OCI Service Broker to that user by creating policies. Refer OCI documents for creating OCI users, groups and configuring policies.

### OCI User Capabilities

OCI Service Broker uses the OCI user credentials only for authenticating the calls to OCI APIs. It is recommended to provide only the "Can use API keys" [capability](https://docs.cloud.oracle.com/iaas/Content/Identity/Tasks/managingusers.htm?Highlight=user%20capabilities#AboutUserCapabilities) to the OCI Service Broker user.

### Policies to allow access to Services

In OCI by default, access to all resources for an user is denied. The tenancy administrator is required to explicitly whitelist a user to have access for the required resources. It is strongly recommended to restrict access for the user used by OCI Service Broker to only region in which OCI Service Broker is expected to manage resources.

Below table lists the services supported by OCI Service Broker and the policy statement required in order for the service broker to manage the service.

| Service-Name | [Verbs](https://docs.cloud.oracle.com/iaas/Content/Identity/Reference/policyreference.htm?Highlight=policy#Verbs) | [Resources-Types](https://docs.cloud.oracle.com/iaas/Content/Identity/Reference/policyreference.htm?Highlight=policy#Resource) | Sample Policy Statement |
| ------------ | ----- | --------------- | ----------------------- |
| Autonomous Database (ATP/ADW) |`manage` |`autonomous-database` |Allow group service-broker-group to manage  autonomous-database where request.region='<region_short_id>'|
| Objectstore Buckets |`manage` |`buckets` |Allow group service-broker-group to manage buckets where request.region='<region_short_id>'|
| Streaming | `manage` | `streams` | Allow group service-broker-group to manage streams where request.region='<region_short_id>'|

### Restrict the permissions only to the required Compartments and Region

While creating the policies to allow OCI Service Broker user to manage services, it is important to consider restricting those permissions to only the required compartment(s) and region.  This can be done by adding compartment name and region in the policy.

**Example:**

`Allow group service-broker-group to manage autonomous-database in compartment service-broker where request.region='phx''`

The above policy provides access for group `service-broker-group` to manage ATP only in compartment `service-broker` in region `US West (Phoenix)`.

## Limit access to OCI Service Broker endpoint using Networkpolicy

The OCI Service Broker Pod endpoint is not exposed outside the Kubernetes cluster. Only the pods running in the same Kubernetes cluster can be able to access this endpoint. But we recommend accessing OCI Service Broker endpoint only from Service Catalog. Even if users want to use their own OSB clients, it is better to provide access only to that client and restrict all other entities from accessing the OCI Service Broker endpoint.  The best way to achieve this is by using [NetworkPolicy](https://kubernetes.io/docs/concepts/services-networking/network-policies/). `NetworkPolicy` resources define rules which specify what traffic is allowed to the selected pods.

**Note:**  In order for the `NetworkPolicy` to work the network plugin used by the cluster needs to support it. Not all network plugins support `NetworkPolicy`. For example, the network plugin used by OKE  does not support `NetworkPolicy`. But by installing [Calico](https://docs.projectcalico.org/v2.0/getting-started/kubernetes/), `NetworkPolicy` can be configured in OKE. Please ensure your Cluster's network plugin supports `NetworkPolicy`.

The samples files forconfiguring network policies are available in directory `samples/network-policy/`

### Strategy 1-Allow access only for Service Catalog running in the same namespace

Deploy the Service Catalog and the OCI Service Broker in the same namespace and create the below `NetworkPolicy` in that namespace. This policy allows access to OCI Service Broker pod only from Service Catalog(with label `app = catalog-catalog-controller-manager`) pod.  Neither Pods running in other namespaces (even with label `app = catalog-catalog-controller-manager`) nor the other Pods in the same namespace can access OCI Service Broker.

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-service-catalog-same-nm
spec:
  podSelector:
    matchLabels:
      app: oci-service-broker
  ingress:
  - from:
      - podSelector:
          matchLabels:
            app: catalog-catalog-controller-manager
```

### Strategy 2-Allow access only for Service Catalog from any namespace

Deploy Service Catalog in any namespace, but add a label to that namespace so that access to OCI Service Broker is allowed only from Service Catalog running in those namespaces

**Note:** This requires Kubernetes 1.11 & above. Also, this may not be supported by all network plugins. If this is not supported in your cluster try #3 listed below.

Label the namespace in which Service Catalog is deployed:

```bash
kubectl label namespace/<YOUR_NAMESPACE> app=service-catalog
```

Create `NetworkPolicy` having namespaceSelector with label `app=service-catalog`

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-catalog-from-any-nm
spec:
  podSelector:
    matchLabels:
      app: oci-service-broker
  ingress:
  - from:
      - namespaceSelector:
          matchLabels:
            app: service-catalog
        podSelector:
          matchLabels:
            app: catalog-catalog-controller-manager
```

### Strategy 3-Allow access for any pods running in a certain namespace

This is a variation of #2. It allows access to OCI Service Broker from a particular namespace only. But unlike #2 any pods( not just the Service Catalog) running in that namespace can access OCI Service Broker.

Label the namespace in which Service Catalog is deployed:

```bash
kubectl label namespace/<YOUR_NAMESPACE> app=service-catalog
```

Create `NetworkPolicy` having namespaceSelector with label `app=service-catalog`

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-access-from-nm
spec:
  podSelector:
    matchLabels:
      app: oci-service-broker
  ingress:
  - from:
      - namespaceSelector:
          matchLabels:
            app: service-catalog
```

## Using secured etcd Cluster

Both Service Catalog and OCI Service Broker use `etcd` for persistence. Both the charts provide a way to deploy an embedded `etcd` during installation. We strongly recommend not to use embedded `etcd` in any non-development environments. They are added in the charts for quick deployment for development and testing purposes only. The expectation is that the user should stand-up their own `etcd` cluster. We recommend using the same `etcd` cluster for both Service Catalog and OCI Service Broker. The expectation is that this `etcd` cluster is properly secured and has proper persistence support. Configuring security in `etcd` is beyond the scope of this document and there are enough materials like this [one](https://github.com/etcd-io/etcd/blob/master/Documentation/op-guide/security.md) that provides in-depth details on this subject.

As an extra level of security, we recommend the users to apply `NetworkPolicy` to ensure that the `etcd` is accessible only from OCI Service Broker and Service Catalog.

## Configuring Sensitive values in the Parameters

The user may be required to add sensitive information as part of the `ServiceInstance` or `ServiceBinding` definitions. Specifying this information as plain text even when the `ServiceInstance` or `ServiceBinding` resources are secured using RBAC is not a good practice. Instead, we recommend the users to create a Kubernetes secret with these sensitive pieces of information and just refer this secret in the  `ServiceInstance` or `ServiceBinding` definitions. This way even un-intended users with read access to `ServiceInstance` or `ServiceBinding` does not get to see these details.

**Example: **

* Create Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: atp-secret
data:
  # {"password":"ds12132@12dwdsdww"}
  password: eyJwYXNzd29yZCI6ImRzMTIxMzJAMTJkd2RzZHd3In0K
  # {"walletPassword":"Ade13fgl0k#dsd"}
  walletPassword: eyJ3YWxsZXRQYXNzd29yZCI6IkFkZTEzZmdsMGsjZHNkIn0K
```

**Note:** The string `eyJ3YWxsZXRQYXNzd29yZCI6IkFkZTEzZmdsMGsjZHNkIn0K` is base64 encoded value of JSON `{"walletPassword":"Ade13fgl0k#dsd"}` where walletPassword is the name of actual parameter (in the ServiceInstance or ServiceBinding) and `Ade13fgl0k#dsd`. For more details please refer [here](https://github.com/kubernetes-incubator/service-catalog/blob/master/docs/parameters.md#referencing-sensitive-data-stored-in-secret).

* Refer them in the definition:

```yaml
apiVersion: servicecatalog.k8s.io/v1beta1
kind: ServiceBinding
metadata:
  name: atp-demo-binding
spec:
  instanceRef:
    name: osb-atp-demo-1
  parametersFrom:
    - secretKeyRef:
        name: atp-secret
        key: walletPassword
```

Here instead of just passing the `walletPassword` directly, we refer it from the secret.
