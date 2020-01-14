# Troubleshooting Guide for OCI Service Broker

- [About](#about)
- [Incidents](#incidents)
  - [ErrorFetchingCatalog](#1-errorfetchingcatalog)
- [Common Steps to Debug](#common-steps-to-debug)

## About

The Trouble Shooting Guide (TSG) for OCI Service Broker desribes the various scenarios where the user might face issues and also provides solution for the problem faced by the users.

## Incidents

## 1. ErrorFetchingCatalog

```bash
svcat get brokers
         NAME          NAMESPACE                URL                        STATUS
+--------------------+-----------+--------------------------------+----------------------+
  oci-service-broker               http://oci-service-broker:8080   ErrorFetchingCatalog  

```

### Symptom

The ErrorFetchingCatalog status is the most common issue we face during the installion of OCI Service Broker. This status indicates Kubernetes Service Catalog is unable to commincate with the OCI Service Broker.

### Root cause

There are multiple reasons for the communication failure between Service Catalog and OCI Service Broker.

#### Invalid OCI Credentials

User details in the Kuberentes secret for OCI Service Broker is having invalid credentials. As part of the [OCI Credentials](../docs/installation.md#oci-credentials) in Installation instructions. This might lead to OCI Service Broker pod in `CrashLoopBackOff` status

```bash
kubectl get pods
NAME                                                     READY   STATUS              RESTARTS   AGE
catalog-catalog-apiserver-5bd5cbffd7-76629               2/2     Running             8          5d
catalog-catalog-controller-manager-f7cdfcd9c-7jrp7       1/1     Running             0          5d
oci-service-broker-oci-service-broker-57b76b66f7-g269x   0/2     CrashLoopBackOff   8          17s
```

**Solution:**
Ensure the [OCI Credentials Kubernetes secret](../docs/installation.md#oci-credentials) is created and the user credentials used for creating Kubernetes secret is valid.

```bash
kubectl create secret generic ocicredentials \
--from-literal=tenancy=<CUSTOMER_TENANCY_OCID> \
--from-literal=user=<USER_OCID> \
--from-literal=fingerprint=<USER_PUBLIC_API_KEY_FINGERPRINT> \
--from-literal=region=<USER_OCI_REGION> \
--from-literal=passphrase=<PASSPHRASE_STRING> \
--from-file=privatekey=<PATH_OF_USER_PRIVATE_API_KEY>
```

#### Service Catalog and Service Broker are in different namespaces

Kubernetes Service Catalog and OCI Service Broker are in different namespaces. For example

```bash
kubectl get pods --all-namespaces=true
..
default          catalog-catalog-apiserver-5bd5cbffd7-76629               2/2     Running   7          5d
default          catalog-catalog-controller-manager-f7cdfcd9c-7jrp7       1/1     Running   0          5d
different-ns   oci-service-broker-oci-service-broker-57b76b66f7-lnvmw   2/2     Running   0          6m
```

**Solution:**

If the Kubernetes Service Catalog and OCI Service Broker are running in different Kubernetes namespaces then, the below modifications need to be made to the `oci-service-broker.yaml` while [registering the `oci-service-broker`](installation.md#register-oci-service-broker):

```bash
apiVersion: servicecatalog.k8s.io/v1beta1
kind: ClusterServiceBroker
metadata:
  name: oci-service-broker
spec:
  url: http://oci-service-broker.<NAMESPACE_OF_OCI_SERVICE_BROKER>:8080
```

#### Missing OCI Credentials Secret

Kuberentes secret for OCI Service Broker is not created. As part of the [OCI Credentials](installation.md#oci-credentials) in Installation instructions. This might lead to OCI Service Broker pod in `ContainerCreating` status

```bash
kubectl get pods
NAME                                                     READY   STATUS              RESTARTS   AGE
catalog-catalog-apiserver-5bd5cbffd7-76629               2/2     Running             8          5d
catalog-catalog-controller-manager-f7cdfcd9c-7jrp7       1/1     Running             0          5d
oci-service-broker-oci-service-broker-57b76b66f7-g269x   0/2     ContainerCreating   0          17s
```

**Solution:**
Ensure the [OCI Credentials Kubernetes secret](installation.md#oci-credentials)  is created and passed correctly in the `helm` install command. Also, ensure that both the OCI credentials and OCI Service Broker are installed in the same namespace.

Command to check the pod for OCI Credentials not found error:

```bash
kubectl -n <NAMESPACE_OF_OCI_SERVICE_BROKER> describe pod $(kubectl -n <NAMESPACE_OF_OCI_SERVICE_BROKER> get pods | grep 'oci-service-broker-' | cut -d" " -f1) | grep 'secret "ocicredentials" not found'
```

Command to check the secret:

```bash
kubectl -n <NAMESPACE_OF_OCI_SERVICE_BROKER> get secret ocicredentials -o yaml
```

In the helm install command the secret should have been passed as shown below:

 ```bash
 helm install oci-service-broker charts/oci-service-broker/. \
  --set ociCredentials.secretName=ocicredentials \
  ...
 ```

## Common Steps to Debug

### 1. Logs of the OCI Service Broker Container

1. Get the complete Logs of OCI Service Broker Container

```
kubectl logs $(kubectl get pods | grep oci-service-broker | cut -d" " -f1) -c oci-service-broker
```

2. Check for authentication failure

```
kubectl logs $(kubectl get pods | grep oci-service-broker | cut -d" " -f1) -c oci-service-broker | grep "Authentication failed"
```

`Authentication Failed` message in the logs indicates that the user credentials in [Kubernetes secret](installation.md#oci-credentials) is invalid. Please verify the credentials and restart the OCI Service Broker pod.

### 2. Helm values used for creating the OCI Service Broker

Sometimes it will be required to check the helm values, that are overriden during helm install or update. Users can get these details using the below steps.

```bash
$ helm ls
NAME              	REVISION	UPDATED                 	STATUS  	CHART                   	NAMESPACE
catalog           	1       	Wed Apr  3 14:58:55 2019	DEPLOYED	catalog-0.1.34          	default  
oci-service-broker	1       	Tue Apr  9 12:01:01 2019	DEPLOYED	oci-service-broker-<VERSION>	default

# Use the RELEASE NAME of the OCI Service Broker
$ helm get values oci-service-broker
ociCredentials:
  secretName: <KUBERNETES_SECRET_NAME>
storage:
  etcd:
    useEmbedded: <FLAG_TO_USE_EMBEDDED_ETCD>
tls:
  enabled: <FLAG_TO_ENABLE_TLS>
```

**Note:** The above command gives only the overriden helm values. To get all the values add `-a` option. Example: `helm get values -a oci-service-broker`

### 3. Command to get status of pods

```bash
$ kubectl get pods -o wide
NAME                                                         READY   STATUS    RESTARTS   AGE   IP            NODE
pod/catalog-catalog-apiserver-5bd5cbffd7-76629               2/2     Running   11         7d    10.244.2.38   130.35.11.57
pod/catalog-catalog-controller-manager-f7cdfcd9c-7jrp7       1/1     Running   0          7d    10.244.2.39   130.35.11.57
pod/oci-service-broker-oci-service-broker-57b76b66f7-54hsv   2/2     Running   0          54s   10.244.1.21   130.35.5.110
```
