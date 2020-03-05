# Installation

* [Pre-Requisites](#pre-requisites)
  * [For building OCI Service Broker image](#for-building-oci-service-broker-image)
  * [For deploying OCI Service Broker chart](#for-deploying-oci-service-broker-chart)
* [Install Service Catalog](#install-service-catalog)
* [Install svcat tool](#install-svcat-tool)
* [Deploy OCI Service Broker](#deploy-oci-service-broker)
  * [OCI credentials](#oci-credentials)
  * [Quick Setup](#quick-setup)
  * [Recommended Setup](#recommended-setup)
    * [etcd for persistence](#etcd-for-persistence)
    * [Enable TLS](#enable-tls)
    * [Recommended Setup Command](#recommended-setup-command)
* [RBAC](#rbac)
  * [RBAC required for OCI Service Broker](#rbac-required-for-oci-service-broker)
  * [RBAC Permissions for registering OCI Service Broker](#rbac-permissions-for-registering-oci-service-broker)
  * [RBAC Permissions for managing Service instances and bindings](#rbac-permissions-for-managing-service-instances-and-bindings)
* [Register OCI Service Broker](#register-oci-service-broker)
* [Monitoring OCI Service Broker](#monitoring-oci-service-broker)
  * [JMX](#jmx)
* [Build OCI Service Broker Image from the Source](#build-oci-service-broker-image-from-the-source)

## Pre-requisites

### For building OCI Service Broker image

* JDK 10 & above
* [Gradle](https://gradle.org/)
   (Recommended Gradle version: v4.10.3)
* Docker

### For deploying OCI Service Broker chart

* [Oracle Container Engine for Kubernetes](https://docs.cloud.oracle.com/iaas/Content/ContEng/Concepts/contengoverview.htm) (OKE) Cluster.
* `kubectl` to control the OKE Cluster. Please make sure it points to the above OKE Cluster.
* [Helm](https://github.com/helm/helm) client.

## Install Service Catalog

[Kubernetes Service Catalog](https://kubernetes.io/docs/concepts/extend-kubernetes/service-catalog/) client is an extension API that enables applications running in Kubernetes clusters to easily use external managed software offerings, such as a datastore service offered by a cloud provider.

Add the Kubernetes Service Catalog helm repository:

```bash
helm repo add svc-cat https://svc-catalog-charts.storage.googleapis.com
```

Install the Kubernetes Service Catalog helm chart:

Helm 3.x syntax:
```bash
helm install catalog svc-cat/catalog
```

Helm 2.x syntax:
```bash
helm install svc-cat/catalog --timeout 300 --name catalog
```

Please note that the above command will run the Service Catalog using an embedded `etcd` instance. It is not recommended to run the Service Catalog using an embedded etcd instance in production environments, instead a separate etcd cluster should be setup and used by the Service Catalog. The open source [etcd operator project](https://github.com/coreos/etcd-operator) or a commercial offering may be used to setup a production quality etcd cluster.

## Install svcat tool

`svcat` is the CLI tool for Service Catalog. Follow the instruction in the svcat [docs](https://svc-cat.io/docs/install/#installing-the-service-catalog-cli) to install the svcat tool.

If you're using MacOS, you can easily install the svcat CLI tool with Homebrew:

```bash
brew update && brew install kubernetes-service-catalog-client
```

## Deploy OCI Service Broker

The OCI Service Broker is packaged as Helm chart for making it easy to install in Kubernetes. The chart is available at [charts/oci-service-broker](../) directory.

```plain
https://github.com/oracle/oci-service-broker/releases/download/v1.4.0/oci-service-broker-1.4.0.tgz
```

### OCI credentials

The OCI Service Broker needs OCI user credentials details to provision and manage services/resources in the user tenancy. The users required to create a Kubernetes secret as detailed below.

**Important:** Please check the guidelines [here](security.md#restrict-access-of-the-oci-user-used-by-service-broker) for configuring the OCI user credentials for OCI Service Broker.

The secret should have the below Keys and respective values for it:

| Key | Description |
| --------- | ----------- |
| `tenancy` | The OCID of your tenancy |
| `fingerprint`    | The Fingerprint of your OCI user |
| `user`    | OCID of the user |
| `privatekey`    | The OCI User private key |
| `passphrase`    | The passphrase of the private key. This is mandatory and if the private key does not have a passphrase, then set the value to an empty string. |
| `region`    | The region in which the OKE cluster is running. The value should be in OCI region format. Example: us-ashburn-1 |

Run the below command to create Secret by name `ociCredentials`. (Replace values with your user credentials)

```bash
kubectl create secret generic ocicredentials \
--from-literal=tenancy=<CUSTOMER_TENANCY_OCID> \
--from-literal=user=<USER_OCID> \
--from-literal=fingerprint=<USER_PUBLIC_API_KEY_FINGERPRINT> \
--from-literal=region=<USER_OCI_REGION> \
--from-literal=passphrase=<PASSPHRASE_STRING> \
--from-file=privatekey=<PATH_OF_USER_PRIVATE_API_KEY>
```
The value for `ociCredentials.secretName` should contain the name of the Kubernetes Secret created above, that contains the OCI user and the credentials details.

### Quick Setup

For quickly testing out OCI Service Broker, TLS can be disabled and an embedded etcd container can be used. This can be used for quickly setting up the Broker but not recommended in PRODUCTION environments. Please refer to [Recommended Setup](#recommended-setup) for PRODUCTION environments

Helm 3.x syntax:
```bash
 helm install oci-service-broker https://github.com/oracle/oci-service-broker/releases/download/v1.4.0/oci-service-broker-1.4.0.tgz \
  --set ociCredentials.secretName=ocicredentials \
  --set storage.etcd.useEmbedded=true \
  --set tls.enabled=false
 ```

Helm 2.x syntax:
```bash
 helm install https://github.com/oracle/oci-service-broker/releases/download/v1.4.0/oci-service-broker-1.4.0.tgz  --name oci-service-broker \
  --set ociCredentials.secretName=ocicredentials \
  --set storage.etcd.useEmbedded=true \
  --set tls.enabled=false
 ```

Using Helm install from the charts directory in master branch. Please use below command.

Helm 3.x syntax:
 ```bash
 helm install oci-service-broker charts/oci-service-broker/. \
  --set ociCredentials.secretName=ocicredentials \
  --set storage.etcd.useEmbedded=true \
  --set tls.enabled=false
 ```

Helm 2.x syntax:
 ```bash
 helm install charts/oci-service-broker/.  --name oci-service-broker \
  --set ociCredentials.secretName=ocicredentials \
  --set storage.etcd.useEmbedded=true \
  --set tls.enabled=false
 ```

### Recommended Setup

It is strongly recommended to configure OCI Service Broker with [TLS](#enable-tls) as well as use an [external etcd cluster](#etcd-for-persistence). All of the default values in the helm chart reflects this recommendation.

#### etcd for persistence

The OCI Service Broker stores service instance related metadata in an etcd instance. By default an embedded etcd instance is available in this chart and will be used for storing the same. But it is NOT recommended to use the embedded etcd instance in PRODUCTION environments.

The etcd cluster that was setup to be used by the Service Catalog, as explained in the [Install Service Catalog](#install-service-catalog) section above can be shared by the OCI Service Broker as well. Otherwise, a separate etcd cluster can be setup to be used by the OCI Service Broker. The values under `storage.etcd` should be used to configure etcd.

| values | Description |
| --------- | ----------- |
| `storage.etcd.useEmbdded` | Set this value to false if an embedded etcd instance should not be used, the default value is true.
| `storage.etcd.image`    | The docker image to use in case of embedded etcd|
| `storage.etcd.imagePullPolicy`    | The pull policy of the embedded etcd image |
| `storage.etcd.servers`    | In case embedded etcd is not used, a comma separated list of etcd servers should be provided here |
| `storage.etcd.tls.enabled`    | Set this value to true if TLS needs to be used to communicate with the etcd servers  |
| `storage.etcd.tls.clientCertSecretName`    | The Kubernetes secret containing the necessary files to communicate with etcd using TLS |

If TLS is to be used to communicate with the etcd servers, another Kubernetes secret needs to be provided. The secret should have the following values:

| File Name | Description |
| --------- | ----------- |
| `etcd-client-ca.crt` | TLS Certificate Authority file used to secure etcd communication.
| `etcd-client.crt`    | TLS certification file used to secure etcd communication. |
| `etcd-client.key`    | TLS key file used to secure etcd communication. This should be in PKCS#8 file format|

An example command to create the secret is as follows

```bash
kubectl create secret generic etcd-tls-secret \
--from-file=${HOME}/etcd/etcd-client.crt\
--from-file=${HOME}/etcd/etcd-client.key\
--from-file=${HOME}/etcd/etcd-client-ca.crt
```

**Note:** The namespace in the above command must be the same Kubernetes namespace where OCI Service Broker
will be installed.

Please read [etcd documentation](https://coreos.com/etcd/docs/latest/) to know more about these files, these are standard TLS related files which any etcd
client needs to use to communicate with an etcd server in a TLS setup.

#### Enable TLS

In order to enable TLS for OCI Service Broker, the following values needs to be configured:

| values | Description |
| --------- | ----------- |
| `tls.enabled` | The value needs to be set to true if TLS needs to be enabled on the OCI OSB application. Default value is false.
| `tls.secretName`    | The secret which contains the PKCS#12 file and the export password

In order to enable TLS, the OCI Service Broker requires a PKCS#12 file which contains the server certificate and server key. The certificate and key has to be exported
into a PKCS#12 bundle, and the file along with the export password needs to be provided as a secret to the OCI Service Broker.

Run the below command to export the certificate and key to a PKCS#12 file.

```bash
openssl pkcs12 -inkey key.pem -in certificate.pem -export -out certificate.p12 -passout -pass:<password>
```

The above command assumes that the certificate and key files are in `pem` format. The following command can be used to create a self signed certificate and private key.

```bash
openssl req -newkey rsa:2048 -nodes -keyout key.pem -x509 -days 365 -out certificate.pem
```

Run the below command to create a secret with the contents of the PKCS#12 file and password

```bash
kubectl create secret generic certsecret --from-literal=keyStore.password=<export_password> --from-file=keyStore=<key_store_file_path>
```

Please note that the names in keys i.e. keyStore.password and keyStore must not be changed during the creation of the secret.

#### Recommended Setup Command

Replace the values of --set arguments with your appropriate values to install the OCI Service Broker. User needs to point docker images either from OCIR or from their repository.

Helm 3.x syntax:
```bash
 helm install oci-service-broker https://github.com/oracle/oci-service-broker/releases/download/v1.4.0/oci-service-broker-1.4.0.tgz \
  --set ociCredentials.secretName=ocicredentials \
  --set tls.secretName=certsecret \
  --set storage.etcd.servers=<comma separated list of etcd servers>
 ```

Helm 2.x syntax:
```bash
 helm install https://github.com/oracle/oci-service-broker/releases/download/v1.4.0/oci-service-broker-1.4.0.tgz --name oci-service-broker \
  --set ociCredentials.secretName=ocicredentials \
  --set tls.secretName=certsecret \
  --set storage.etcd.servers=<comma separated list of etcd servers>
 ```

Using Helm install from the charts directory in master branch. Please use below command.

Helm 3.x syntax:
```bash
helm install oci-service-broker charts/oci-service-broker/. \
 --set ociCredentials.secretName=ocicredentials \
 --set tls.secretName=certsecret \
 --set storage.etcd.servers=<comma separated list of etcd servers>
```

Helm 2.x syntax:
```bash
helm install  charts/oci-service-broker/. --name oci-service-broker \
 --set ociCredentials.secretName=ocicredentials \
 --set tls.secretName=certsecret \
 --set storage.etcd.servers=<comma separated list of etcd servers>
```

## RBAC

If RBAC is enabled in your cluster then the following permissions are required by the user:

### RBAC required for OCI Service Broker

During chart installation, a service account by the name 'oci-osb' is created and assigned to the  OCI Service Broker pod.

OCI Service Broker also requires access to the Kubernete's 'nodes' API in order
to discover the ClusterId of the Kubernetes cluster. ClusterId is used to tag the services created by the OCI Service Broker for auditing purposes. ClusterId is part of node labels in Oracle Container Engine for Kubernetes(OKE). The OCI Service Broker chart creates a [ClusterRole](../templates/role.yaml) and [ClusterRoleBinding](../templates/role-binding.yaml) that allows the OCI Service Broker to read the node labels and read the ClusterId.

### RBAC Permissions for registering OCI Service Broker

Typically, registering a OCI Service Broker is done by cluster-admin. The normal users then create/manage services offered by the Broker. Please ensure that the user that is registering the broker has `cluster-admin` role.

Sample command for mapping `cluster-admin` to an user:

```bash
kubectl create clusterrolebinding cluster-admin-brokers --clusterrole=cluster-admin --user=<USER_ID>
```

### RBAC Permissions for managing Service instances and bindings

Refer [Restrict access to Service Catalog resources using RBAC](security.md#restrict-access-to-service-catalog-resources-using-rbac) for configuring RBAC for `ServiceInstance` and `ServiceBinding` resources.

## Register OCI Service Broker

Sample files for various services are available under [`oci-service-broker/samples`](../samples) directory inside the charts. The below command extracts chart that contains the sample files.

```bash
curl -LO https://github.com/oracle/oci-service-broker/releases/download/v1.4.0/oci-service-broker-1.4.0.tgz | tar xz
```

Create a `ClusterServiceBroker` resource with OCI Service Broker URL to register the broker. Use the below register yaml file after updating the namespace of the OCI Service Broker.

```bash
# Ensure <NAMESPACE_OF_OCI_SERVICE_BROKER> is replaced with the a proper namespace in oci-service-broker.yaml
kubectl create -f oci-service-broker/samples/oci-service-broker.yaml
```

Get the status of the broker:

```bash
svcat get brokers
```

Sample Output:

```plain
           NAME             NAMESPACE                URL                 STATUS  
+-------------------------+-----------+--------------------------------+--------+
  oci-service-broker               http://oci-service-broker:8080   Ready
```

Get Services List

```bash
svcat get classes
```

Output:

```plain
          NAME           NAMESPACE                 DESCRIPTION
+----------------------+-----------+------------------------------------------+
  atp-service                        Autonomous Transaction Processing Service
  object-store-service               Object Storage Service
  adw-service                        Autonomous Data Warehouse Service
  oss-service                        Oracle Streaming Service
```

Get Service Plans

```bash
svcat get plans
```

Output:

```plain
    NAME            CLASS                          DESCRIPTION
+----------+----------------------+-------------------------------------------+
  standard   atp-service            A Standard plan for the OCI Autonomous
                                    Transaction Processing
  archive    object-store-service   An Archive type Object Storage
  standard   object-store-service   A Standard type Object Storage
  standard   adw-service            OCI Autonomous Data Warehouse
  standard   oss-service            Oracle Streaming Service plan
```

## Monitoring OCI Service Broker

### JMX

OCI Service Broker exposes metrics via an MBean with ObjectName `OCIOpenServiceBroker:type=BrokerMetrics`. A Prometheus
JMX exporter docker container can be used along with OCI Service Broker to push the metrics to Prometheus.


### Build OCI Service Broker Image from the Source

Instead of using the pre-built image, users can build the OCI Service Broker image from source and use that to deploy the chart.

After cloning the `oci-service-broker` source code run following command to build the Docker Image for OCI Service Broker

```bash
cd oci-service-broker/oci-service-broker

#The OCI Service Broker internally uses [oci-java-sdk](https://github.com/oracle/oci-java-sdk) to manage OCI services. But they are not published to any public maven repositories yet. In order to build the project, users are required to download oci-java-sdk archive file and add the dependent libraries to libs directory of oci-service-broker. Below command will download the required libraries and add to the libs directory.

bash download_SDK_libs.sh

#Gradle is the build tool used in OCI Service Broker. Please execute the below command to compile, build and generate a docker image.

gradle -b build.gradle clean build docker
```

If gradle build failed with error('Task :spotbugsMain FAILED'), please provide '-x spotbugsMain' option in above command. This is known issue due to latest JDK version.

After successful build, a new docker image will be by name 'oci-service-broker'. Push this image to [OCIR](https://docs.cloud.oracle.com/iaas/Content/Registry/Concepts/registryoverview.htm) or your own docker repository and refer this image in the Chart during deployment by overriding the helm values `image.repository` and `image.tag`.

example: 

 ```bash
 helm install oci-service-broker charts/oci-service-broker/.  \
  --set image.repository=<image name> --set image.tag=<image tag> \
  ...
  ...
 ```
