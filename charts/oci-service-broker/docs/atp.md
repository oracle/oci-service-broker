# Autonomous Transaction Processing Service

- [Introduction](#introduction)
- [Plans](#plans)
- [OCI User Permission requirement](#oci-user-permission-requirement)
- [Service Provision Request Parameters](#service-provision-request-parameters)
- [Service Binding Request Parameters](#service-binding-request-parameters)
- [Service Binding Response Credentials](#service-binding-response-credentials)
- [Examples](#examples)
  - [Prerequisite](#prerequisite)
    - [OCI Service Broker](#oci-service-broker)
    - [Sample files](#sample-files)
  - [Provisioning](#provisioning)
    - [Creating an ATP ServiceInstance](#creating-an-atp-serviceinstance)
    - [Get instance status](#get-instance-status)
  - [Binding](#binding)
    - [Creating an ATP ServiceBinding resource](#creating-an-atp-servicebinding-resource)
    - [Get Binding status](#get-instance-status)
  - [Sample program to Connect to ATP](#sample-program-to-connect-to-atp)
    - [Create a Kubernetes Secret with passwords](#create-a-kubernetes-secret-with-passwords)
    - [Deploy sample application](#deploy-sample-application)
  - [Deprovision](#deprovision)
    - [Delete Service Binding](#delete-service-binding)
    - [Delete Service Instance](#delete-service-instance)
  - [Use Secret to pass passwords](#use-secret-to-pass-passwords)

## Introduction

[Autonomous Transaction Processing](https://www.oracle.com/in/database/autonomous-transaction-processing.html)(ATP) is a managed OCI Database service built on top of the Oracle Autonomous Database. ATP service is also offered via OCI Service Broker thereby making it easy for applications to provision and integrate seamlessly with ATP.

## Plans

Right now we expose a `standard` plan where the user can specify CPU count and storage size.

## OCI User Permission requirement

The OCI user for OCI Service Broker should have permission `manage` for resoruce type `autonomous-database`

**Sample Policy:**

```plain
Allow group <SERVICE_BROKER_GROUP> to manage autonomous-database in compartment <COMPARTMENT_NAME>
```

## Service Provision Request Parameters

To provision, an ATP service user needs to provide the following details

| Parameter        | Description                                                         | Type   | Mandatory |
| ---------------- | ------------------------------------------------------------------- | ------ | --------- |
| `name`           | The display name for the ATP instance.                              | string | yes       |
| `dbName`         | Database Name.                                                      | string | yes       |
| `compartmentId`  | The OCI compartment where the ATP instance will be provisioned.     | string | yes       |
| `cpuCount`       | Number of CPU cores to have.                                        | int    | yes       |
| `storageSizeTBs` | Size of the DB Storage in Terrabytes.                               | int    | yes       |
| `password`       | ATP Service will pre-provision a DB Admin user when it provisions an ATP instance. The user needs to provide a password to be set for this Admin user.<br>The OCI ATP service requires the password to satisfy the below rules.<br><ul><li>The length should be between 12-18</li>A password must include an upper case, lower case, and special character.</ul> | string | yes       |
| `licenseType`    | Use your existing database software licenses(BYOL) or Subscribe to new database software licenses and the Database Cloud Service.<br>Valid values are:<ul><li>BYOL</li><li>NEW</li></ul>.                         | string | yes       |
| `freeFormTags`   | free form tags that are to be used for tagging the ATP instance.    | object | no        |
| `definedTags`    | The defined tags that are to be used for tagging the ATP instance.  | object | No        |

## Service Binding Request Parameters

The user needs to pass the following parameters to get the binding details:

| Parameter        | Description                                                         | Type   | Mandatory |
| ---------------- | ------------------------------------------------------------------- | ------ | --------- |
| `walletPassword` | A password to be set for the Oracle wallet that will be created for the application to connect to ATP. This password must<br><ul><li>Contain more than 8 characters.</li><li>Contain at least one uppercase, lowercase, and special character</li></ul> | string | yes |

## Service Binding Response Credentials

Users can create binding to get the credentials to use the ATP. The following files/details will be made available to the user

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

The sample files for ATP are available under [`oci-service-broker/samples/atp`](charts/oci-service-broker/samples/atp) directory.

### Provisioning

A sample Kubernetes resource yaml to provision an ATP instance.

```bash
cat oci-service-broker/samples/atp/atp-instance-plain.yaml
```

Providing password in plain text may not be an idle case. Alternatively, the users can have the password stored in a  Kubernetes Secret and have the password passed from that as part of the Provisioning process.  Using Secrets to pass in the password is the preferred way.

Please refer [Use Secret to pass passwords](#use-secret-to-pass-passwords) section for passing the password from secrets.

#### Creating an ATP ServiceInstance

**NOTE:**
The  `atp-instance-plain.yaml` files contain the compartment OCID in which the user wants to provision the ATP instance. The user needs to update it with their compartment OCID.

```bash
kubectl create -f oci-service-broker/samples/atp/atp-instance-plain.yaml
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

A sample Kubernetes resource yaml to create binding.

```bash
cat oci-service-broker/samples/atp/atp-binding-plain.yaml
```

**Note:**
`instanceRef` should be same as the instance name for which binding is required.

#### Creating an ATP ServiceBinding resource

```bash
kubectl create -f oci-service-broker/samples/atp/atp-binding-plain.yaml
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

Output

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

### Sample program to Connect to ATP

#### Create a Kubernetes Secret with passwords

User need to create secret with DB Admin user password and wallet password. Edit the `oci-service-broker/samples/atp/atp-demo-secret.yaml` with proper base64 encoded value of the passwords. (Example: `echo 's123456789S@' | base64`).

Create the Secret.

```bash
kubectl create -f oci-service-broker/samples/atp/atp-demo-secret.yaml
```

#### Deploy sample application

The file `oci-service-broker/samples/atp/atp-demo.yaml` contains a Kubernetes Deployment which deploys a Simple java program. The program connects to the ATP instance and runs a simple query. Content of the Yaml file.

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
      # This decode-creds initContainer takes care of decoding the files and writing them to a shared volume from which jdbc-app contianer
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
      # Simple JDBC Program that uses the credential files to connect to ATP and run a simple Query.
      - name: jdbc-creds
        command:
        - bash
        - -c
        - "java -cp '/db-demo/ojdbc8-jars/*:/db-demo/oci-service-broker-all.jar'
                  -Doracle.net.tns_admin=/db-demo/creds
                  -Dtns_name=${TNS_NAME}
                  com.oracle.cnp.atp.DBConnectionExample;trap : TERM INT; sleep infinity & wait"
        image: iad.ocir.io/odx-platform/cnp/atp-demo:v1
        imagePullPolicy: Always
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
        - name: TNS_NAME
          value: "osbdemo_high"
        volumeMounts:
        - name: creds
          mountPath: /db-demo/creds
      volumes:
      # Volume for mouting the credentials file from Secret created by binding request.
      - name: creds-raw
        secret:
          secretName: atp-demo-binding
      # Shared Volume in which init-continer will save the decoded credential files and the jdbc-app container reads.
      - name: creds
        emptyDir: {}
```

Important things to note:

1. The volume `creds-raw` contains all credentials  files from the Secret `atp-demo-binding`
1. The initContainer `decode-creds` mounts the credentials file from 'creds-raw' and base64 decode them. The decoded files are written to volume `creds`.
1. The container `jdbc-creds` mounts the decoded credentials from volume `creds` to directory `/db-demo/creds`.
1. The DB ADMIN user name, password, wallet password are passed as environment variables (DB_ADMIN_USER, DB_ADMIN_PWD and  WALLET_PWD).
1. The DBADMIN password and the wallet password are read and passed from the secret (oci-service-broker/samples/atp/atp-demo-secret.yaml) we created earlier.
1. The demo sample java program uses the JDBC 18.X driver which can use the Oracle wallet to connect to the DB. To configure the wallet we set System property `oracle.net.tns_admin`  to the credentials directory  `/db-demo/creds` that includes the wallet file.

Deploy the app.

```bash
kubectl create -f oci-service-broker/samples/atp/atp-demo.yaml
```

View the logs.

```bash
kubectl logs $(kubectl get pod | grep ^atp-demo | grep Running | cut -d" " -f 1)
```

Output

```plain
-------- Oracle JDBC Connection Testing ------
Connecting to osbdemo_high ......

Executing Query: select * from DBA_USERS where rownum < 4



USERNAME:SYS
ACCOUNT_STATUS:OPEN
EXTERNAL_NAME:null
CREATED:2018-08-26
DEFAULT_TABLESPACE:SYSTEM
==========================================
USERNAME:AUDSYS
ACCOUNT_STATUS:EXPIRED & LOCKED
EXTERNAL_NAME:null
CREATED:2018-08-26
DEFAULT_TABLESPACE:DATA
==========================================
USERNAME:SYSTEM
ACCOUNT_STATUS:OPEN
EXTERNAL_NAME:null
CREATED:2018-08-26
DEFAULT_TABLESPACE:SYSTEM
==========================================
```

### Deprovision

#### Delete Service Binding

Deleting the Service binding created in the previous step will result in the secret(that has the credentials) getting deleted.  All Service Bindings for a ServiceInstance should be deleted before deleting the ServiceInstance.

```bash
kubectl delete -f oci-service-broker/samples/atp/atp-binding-plain.yaml
```

#### Delete Service Instance

```bash
kubectl delete -f oci-service-broker/samples/atp/atp-instance-plain.yaml
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

It usually takes 5-10 minutes for an instance to get deprovisioned. On successful deprviosining the ServiceInstance will be removed and won't be listed.

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
kubectl create -f oci-service-broker/samples/atp/atp-secret.yaml
```

Yaml to provision ATP instance with password loaded from Kubernetes Secret (notice parametersFrom part).

```bash
cat oci-service-broker/samples/atp/atp-instance.yaml
```

Yaml to provision ATP instance with password loaded from Kubernetes Secret (notice parametersFrom part).

```bash
cat oci-service-broker/samples/atp/atp-binding.yaml
```
