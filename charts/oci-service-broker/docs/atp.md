# Autonomous Transaction Processing Service

- [Introduction](#introduction)
- [Plans](#plans)
- [OCI User Permission requirement](#oci-user-permission-requirement)
- [Service Provision Request Parameters](#service-provision-request-parameters)
    - [Provisioning a new ATP Service Instance](#provisioning-a-new-atp-service-instance)
    - [Using an Existing ATP Service Instance](#using-an-existing-atp-service-instance)
- [Service Binding Request Parameters](#service-binding-request-parameters)
- [Service Binding Response Credentials](#service-binding-response-credentials)
- [Examples](#examples)
  - [Prerequisite](#prerequisite)
    - [OCI Service Broker](#oci-service-broker)
    - [Sample files](#sample-files)
  - [Provisioning](#provisioning)
    - [Creating an ATP Instance](#creating-a-new-atp-instance)
    - [Using an Existing ATP Instance](#using-an-existing-atp-instance)
    - [Get instance status](#get-instance-status)
  - [Binding](#binding)
    - [Creating an ATP ServiceBinding resource](#creating-an-atp-servicebinding-resource)
    - [Get Binding status](#get-instance-status)
  - [Connecting to a provisioned ATP instance](#connecting-to-a-provisioned-atp-instance)
    - [Create a Kubernetes Secret with passwords](#create-a-kubernetes-secret-with-passwords)
    - [Injecting credentials and configurations](#injecting-credentials-and-configurations)
  - [Deprovision](#deprovision)
    - [Delete Service Binding](#delete-service-binding)
    - [Delete Service Instance](#delete-service-instance)
  - [Use Secret to pass passwords](#use-secret-to-pass-passwords)

## Introduction

[Autonomous Transaction Processing](https://www.oracle.com/in/database/autonomous-transaction-processing.html)(ATP) is a managed OCI Database service built on top of the Oracle Autonomous Database. ATP service is also offered via OCI Service Broker thereby making it easy for applications to provision and integrate seamlessly with ATP.

## Plans

Right now we expose a `standard` plan where the user can specify CPU count and storage size.

## OCI User Permission requirement

The OCI user for OCI Service Broker should have permission `manage` for resource type `autonomous-database`.

**Sample Policy:**

```plain
Allow group <SERVICE_BROKER_GROUP> to manage autonomous-database in compartment <COMPARTMENT_NAME>
```

### Service Provision Request Parameters

## Provisioning a new ATP Service Instance

To provision, an ATP service user needs to provide the following details:

| Parameter             | Description                                                         | Type   | Mandatory |
| --------------------- | ------------------------------------------------------------------- | ------ | --------- |
| `name`                | The display name for the ATP instance.                              | string | yes       |
| `dbName`              | Database Name.                                                      | string | yes       |
| `compartmentId`       | The OCI compartment where the ATP instance will be provisioned.     | string | yes       |
| `cpuCount`            | Number of CPU cores to have.                                        | int    | yes       |
| `storageSizeTBs`      | Size of the DB Storage in Terrabytes.                               | int    | yes       |
| `password`            | ATP Service will pre-provision a DB Admin user when it provisions an ATP instance. The user needs to provide a password to be set for this Admin user. <br>The OCI ATP service requires the password to satisfy the below rules.<br><ul><li>The length should be 12 to 18 characters.</li><li>A password must include an upper case, lower case, and special character.</li></ul> | string | yes       |
| `licenseType`         | Use your existing database software licenses(BYOL) or Subscribe to new database software licenses and the Database Cloud Service.<br>Valid values are:<ul><li>BYOL</li><li>NEW</li></ul>.                         | string | yes       |
| `autoScaling`         | The flag to enable auto-scaling in ATP Instance. Allows system to use up to three times the provisioned number of cores as the workload increases. By default, this flag is set to false.                    | boolean| no        |
| `freeFormTags`        | free form tags that are to be used for tagging the ATP instance.    | object | no        |
| `definedTags`         | The defined tags that are to be used for tagging the ATP instance.  | object | no        |

## Using an Existing ATP Service Instance

For more information about binding to an existing ATP service instance, see [Using an Existing Service Instance](services.md#using-an-existing-service-instance).

To attach to an existing ATP service, the user needs to provide the following details. In this case, OCI Service broker will neither provision a new instance nor update/change the existing instance.

| Parameter        | Description                                                         | Type    | Mandatory |
| ---------------- | ------------------------------------------------------------------- | ------- | --------- |
| `name`           | The display name for the ATP instance.                              | string  | yes       |
| `ocid`           | The OCID for existing ATP Instance.                                 | string  | yes       |
| `provisioning`   | Set provisioning flag value as false.                               | boolean | yes       |


## Service Binding Request Parameters

The user needs to pass the following parameters to get the binding details:

| Parameter        | Description                                                         | Type   | Mandatory |
| ---------------- | ------------------------------------------------------------------- | ------ | --------- |
| `walletPassword` | A password to be set for the Oracle wallet that will be created for the application to connect to ATP. This password must<br><ul><li>Contain more than 8 characters.</li><li>Contain at least one uppercase, lowercase, and special character</li></ul> | string | yes |

## Service Binding Response Credentials

Users can create binding to get the credentials to use the ATP. The following files/details will be made available to the user:

| Parameter          | Description                                                              | Type   |
| ------------------ | ------------------------------------------------------------------------ | ------ |
| `ewallet.p12`      | Oracle Wallet.                                                           | string |
| `cwallet.sso`      | Oracle wallet with autologin.                                            | string |
| `tnsnames.ora`     | Configuration file containing service name and other connection details. | string |
| `sqlnet.ora`       |                                                                          | string |
| `ojdbc.properties` |                                                                          | string |
| `keystore.jks`     | Java Keystore.                                                           | string |
| `truststore.jks`   | Java trustore.                                                           | string |
| `user_name`        | Pre-provisioned DB ADMIN Username.                                       | string |

## Examples

### Prerequisite

- Oracle Container Engine for Kubernetes (OKE)/Kubernetes Cluster.
- OCI Service Broker is installed in the OKE cluster.
- `Kubectl` to control the OKE Cluster. Please make sure it points to the above OKE Cluster.
- `svcat` tool.

#### OCI Service Broker

Ensure the OCI Service Broker is installed.

```bash
svcat get brokers
```

Sample Output:

```plain
           NAME             NAMESPACE                URL                 STATUS  
+-------------------------+-----------+--------------------------------+--------+
  oci-service-broker               http://oci-service-broker:8080   Ready
```

If no brokers are listed then it means the OCI Service Broker is not installed. Please follow the instructions [here](installation.md#installation) to install it first.

#### Sample files

The sample files for ATP are available under [`oci-service-broker/samples/atp`](../samples/atp) directory.

### Provisioning

A sample Kubernetes resource yaml to provision an ATP instance.

```bash
cat oci-service-broker/samples/atp/atp-instance-plain.yaml
```

Providing password in plain text may not be an idle case. Alternatively, the users can have the password stored in a  Kubernetes Secret and have the password passed from that as part of the Provisioning process.  Using Secrets to pass in the password is the preferred way.

Please refer [Use Secret to pass passwords](#use-secret-to-pass-passwords) section for passing the password from secrets.

#### Creating a New ATP Instance

**NOTE:**
The  [`atp-instance-plain.yaml`](../samples/atp/atp-instance-plain.yaml) files contain the compartment OCID in which the user wants to provision the ATP instance. The user needs to update it with their compartment OCID.

```bash
kubectl create -f charts/oci-service-broker/samples/atp/atp-instance-plain.yaml
```

#### Using an existing ATP Instance

**NOTE:**
The  [`atp-existing-instance.yaml`](../samples/atp/atp-existing-instance.yaml) files contain the instance OCID and compartment OCID which the user wants to provision as existing ATP instance. The user needs to update it with their instance OCID and compartment OCID.

```bash
kubectl create -f charts/oci-service-broker/samples/atp/atp-existing-instance.yaml
```

#### Get instance status

```bash
svcat get instances
```

Output:

```plain
NAME NAMESPACE CLASS PLAN STATUS
+----------------+-----------+-------------+----------+--------------+
osb-atp-demo-1 catalog atp-service standard Provisioning
```

It usually takes 10-15 minutes for the provisioning to complete. Once the ATP instance is provisioned the Status should change to `Ready`. All other actions on this Service Instances are ignored till the status remain in Provisioning.

```plain
NAME NAMESPACE CLASS PLAN STATUS
+----------------+-----------+-------------+----------+--------------+
osb-atp-demo-1 catalog atp-service standard Ready
```

### Binding

Once the ATP Instance is provisioned the applications will require credentials/configuration to connect to the provisioned ATP instance. To do this the user needs to do:

- Creating a ServiceBinding resource. This will create a Kubernetes secret with the credentials/configurations.
- The user needs to mount the credentials/configurations into the containers so that application can use this configuration.

A sample Kubernetes resource yaml to create binding:

```bash
cat oci-service-broker/samples/atp/atp-binding-plain.yaml
```

**Note:**
`instanceRef` should be same as the instance name for which binding is required.

#### Creating an ATP ServiceBinding resource

```bash
kubectl create -f charts/oci-service-broker/samples/atp/atp-binding-plain.yaml
```

#### Get Binding status

```bash
svcat get bindings
```

Output:

```plain
NAME NAMESPACE INSTANCE STATUS
+-------------+-----------+----------------+--------+
atp-demo-binding   catalog     osb-atp-demo-1      Ready
```

When the ServiceBinding request completes successfully the user should see a secret created in the Kuberenetes Cluster with all the credentials/configurations. The name of the secret will be the same as the ServiceBinding name.

```bash
kubectl get secrets atp-demo-binding -o yaml
```

Output:

```yaml
apiVersion: v1
kind: Secret
type: Opaque
metadata:
  creationTimestamp: 2018-09-20T19:54:02Z
  name: atp-demo-binding
  namespace: catalog
  resourceVersion: "116279449"
  selfLink: /api/v1/namespaces/catalog/secrets/atp-demo-binding
  uid: ec556735-bd0e-11e8-9999-0a580aed122c
data:
  cwallet.sso: b2ZoT05nQUFBQVlBQUFBaEJvblVPWDB2bWJiTHpwZXp
  ewallet.p12: TE1JSVpnekNDR1g4R0NTcUdTSWIzRFFFSEJxQ0NHWEF3Z2hsc0FnR
  keystore.jks: L3UzKzdRQUFBQUlBQUFBQkFBQUFBUUFHYjNKaGEyVjVBQUFCWmU0YnB
  ojdbc.properties: YjNKaFkyeGxMbTVsZEM1M1lXeHNaWFJmYkc5allYUnBiMjQ5S0ZOUFZWSkRSVDBvVFVWVVNFOUVQVVpKVEVVcEtFMUZWRWhQUkY5RVFWUkJQU2hFU1ZKRlExUlBVbGs5Skh0VVRsTmZRVVJOU1U1OUtTa3A=
  sqlnet.ora: VjBGTVRFVlVYMHhQUTBGVVNVOU9JRDBnS0ZOUFZWSkRSU0E5SUNoTlJWUklUMFFnUFNCbWFXeGxLU0FvVFVWVVNFOUVYMFJCVkVFZ1BTQW9SRWxTUlVOVVQxSlpQU0kvTDI1bGRIZHZjbXN2WVdSdGFXNGlLU2twQ2xOVFRGOVRSVkpXU=
  tnsnames.ora: L3UzKzdRQUFBQUlBQUFBREFBQUFBZ0FoWTI0OVpHbG5hV05sY25RZ2MyaGhNaUJ6WldOMWNtVWdjMlZ5ZG1W
  truststore.jks: L3UzKzdRQUFBQUlBQUFBREFBQUFBZ0FoWTI0OVpHbG5hV05sY25RZ2MyaGhNaUJ6WldOMWNtVWdjMlZ5ZG1W
  user_name: QURNSU4=
```

**NOTE:**

1. Except for `user_name` rest of the data in the Secret are binary files.
1. **Due to a known issue in service catalog/OSB, the binary data in the Secret are encoded in base64 twice. While mounting the data from Secrets, Kubernetes decodes it once. Hence user will be required to decode the data one more time to get the actual binary file. We are trying to come up with a workaround for this.**
1. The client will require the DB ADMIN password and the wallet password(optional) also to connect/manage ATP. Users can pass these from the secret file created earlier(atp-secret.yaml). This is required as we don't want to store any credentials inside the broker.

### Connecting to a provisioned ATP instance

#### Create a Kubernetes Secret with passwords

User need to create secret with DB Admin user password and wallet password. Edit the [`oci-service-broker/samples/atp/atp-demo-secret.yaml`](../samples/atp/atp-demo-secret.yaml) with proper base64 encoded value of the passwords. (Example: `echo 's123456789S@' | base64`).

Create the Secret.

```bash
kubectl create -f charts/oci-service-broker/samples/atp/atp-demo-secret.yaml
```

#### Injecting credentials and configurations 

The file [`oci-service-broker/samples/atp/atp-demo.yaml`](../samples/atp/atp-demo.yaml) contains a sample Kubernetes deployment yaml that shows how to:
1. Read the credentials/configuration from the secret and decode it using initContainer.
1. Inject admin password and wallet password from secret as environment variable into an application container.

**Note:**  Please refer [here](https://www.oracle.com/technetwork/database/application-development/jdbc/documentation/atp-5073445.html) for details on connecting to ATP from a java application. `Download the Client Credentials` step mentioned in the document is not required as it is already available in the secret created by the binding request.

```yaml
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: atp-demo
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: atp-demo
    spec:
      # The credential files in the secret are base64 encoded twice and hence they need to be decoded for the programs to use them.
      # This decode-creds initContainer takes care of decoding the files and writing them to a shared volume from which db-app container
      # can read them and use it for connecting to ATP.
      initContainers:
      - name: decode-creds
        command:
        - bash
        - -c
        - "for i in `ls -1 /tmp/creds | grep -v user_name`; do cat /tmp/creds/$i  | base64 --decode > /creds/$i; done; ls -l /creds/*;"
        image: oraclelinux:7.4
        volumeMounts:
        - name: creds-raw
          mountPath: /tmp/creds
          readOnly: false
        - name: creds
          mountPath: /creds
      containers:
      # User application that uses credential files to connect to ATP.
      - name: db-app
        image: <USER_APPLICATION_IMAGE>
        env:
        # Pass DB ADMIN user name that is part of the secret created by the binding request.
        - name: DB_ADMIN_USER
          valueFrom:
            secretKeyRef:
              name: atp-demo-binding
              key: user_name
        # Pass DB ADMIN password. The password is managed by the user and hence not part of the secret created by the binding request.
        # In this example we read the password form secret atp-user-cred that is required to be created by the user.  
        - name: DB_ADMIN_PWD
          valueFrom:
            secretKeyRef:
              name: atp-user-cred
              key: password
        # Pass  Wallet password to enable application to read Oracle wallet. The password is managed by the user and hence not part of the secret created by the binding request.
        # In this example we read the password form secret atp-user-cred that is required to be created by the user.  
        - name: WALLET_PWD
          valueFrom:
            secretKeyRef:
              name: atp-user-cred
              key: walletPassword
        volumeMounts:
        - name: creds
          mountPath: /db-demo/creds
      volumes:
      # Volume for mouting the credentials file from Secret created by binding request.
      - name: creds-raw
        secret:
          secretName: atp-demo-binding
      # Shared Volume in which initContainer will save the decoded credential files and the db-app container reads.
      - name: creds
        emptyDir: {}
```

Important things to note:

1. The volume `creds-raw` contains all credentials  files from the Secret `atp-demo-binding` create by the bidning request.
1. The initContainer `decode-creds` mounts the credentials file from 'creds-raw' and base64 decode them. The decoded files are written to volume `creds`.
1. The container `db-app` mounts the decoded credentials from volume `creds` to directory `/db-demo/creds`.
1. The DB ADMIN user name, password, wallet password are passed as environment variables (DB_ADMIN_USER, DB_ADMIN_PWD and  WALLET_PWD).
1. The DBADMIN password and the wallet password are read and passed from the secret (oci-service-broker/samples/atp/atp-demo-secret.yaml) we created earlier.

### Deprovision

#### Delete Service Binding

Deleting the Service binding created in the previous step will result in the secret(that has the credentials) getting deleted. All Service Bindings for a ServiceInstance should be deleted before deleting the ServiceInstance.

```bash
kubectl delete -f charts/oci-service-broker/samples/atp/atp-binding-plain.yaml
```

#### Delete Service Instance

```bash
kubectl delete -f charts/oci-service-broker/samples/atp/atp-instance-plain.yaml
```

```bash
svcat get instances
```

Output:

```plain
NAME NAMESPACE CLASS PLAN STATUS
+----------------+-----------+-------------+----------+----------------+
atp-instance-1 catalog atp-service standard Deprovisioning
```

It usually takes 5-10 minutes for an instance to get deprovisioned. On successful deprviosining the ServiceInstance will be removed and won't be listed. In case of the existing instance the actual instance won't be removed.

### Use Secret to pass passwords

For the provisioning, resource requires the password contained in the secret file to be in JSON format

```plain
{ "password" : "s123456789S@"}
```

Kubernetes requires the secrets to be base64 encoded. One way to do this is to use `base64` utility in Linux.

```bash
echo '{ "password" : "s123456789S@"}' | base64
```

Create Secret:

Edit the values of 'password:' and 'walletPassword:' in `oci-service-broker/samples/atp/atp-secret.yaml` with appropriate base64 encoded strings.

```bash
kubectl create -f charts/oci-service-broker/samples/atp/atp-secret.yaml
```

Yaml to provision ATP instance with password loaded from Kubernetes Secret (notice parametersFrom part).

```bash
cat charts/oci-service-broker/samples/atp/atp-instance.yaml
```

Yaml to provision ATP instance with password loaded from Kubernetes Secret (notice parametersFrom part).

```bash
cat charts/oci-service-broker/samples/atp/atp-binding.yaml
```
