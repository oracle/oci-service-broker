# Services

The OCI Service Brokers supports the following services

1. [Object Storage Service](object-storage.md#object-storage-oci-service-broker).
1. [Autonomous Transaction Processing](atp.md#autonomous-transaction-processing-service).
1. [Autonomous Data Warehouse](adw.md#autonomous-data-warehouse-service).
1. [Oracle Streaming Service](oss.md#oracle-streaming-oci-service-broker).

### Using an Existing Service Instance

One of the common use cases is to have the applications use the existing service instance instead of creating a new instance every time. 

For example
1. An organization want to have a shared service instance that many applications can use. 
2. Teams might want to have the infrastructure provisioning process separate from the deployment process of the application that uses that infrastructure.

In these cases, the application just needs the details/credentials to connect to the existing instance. However, the Open Service Broker specification allows creating a service binding only to instances that were created through the Service Broker. To overcome this, OCI Service Broker allows you to perform the provisioning process without actually provisioning a service. Instead, you pass in the details required to identify the existing instance. The result is that the existing instance is effectively registered with OSB, and you can then create Service Bindings to that existing instance.

Note that OCI Service Broker will not manage the lifecycle of an existing instance that is “provisioned” or registered this way. You cannot update any parameters of the existing service instance using OCI Service Broker. You can unbind and deprovision the existing service instance. However, during deprovisioning, the existing service instance will not be deleted by OCI Service Broker. It is simply unregistered with Service Broker.