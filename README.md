# OCI Service Broker

## Introduction

The OCI Service Broker is an open source implementation of [Open service broker API Spec](https://github.com/openservicebrokerapi/servicebroker/blob/v2.14/spec.md) for OCI services. Customers can use this implementation to install Open Service Broker in [Oracle Container Engine for Kubernetes](https://docs.cloud.oracle.com/iaas/Content/ContEng/Concepts/contengoverview.htm) or in other Kubernetes clusters. This implementation is targeted to achieve:

* Easy installation.
* Easy extension.
* Provide OOTB implementations for common OCI services.
* OCI Service Broker Installation.

**Services Supported**

1. [Object Storage](https://docs.cloud.oracle.com/iaas/Content/Object/Concepts/objectstorageoverview.htm)
1. [Autonomous Transaction Processing](https://www.oracle.com/in/database/autonomous-transaction-processing.html)
1. [Autonomous Data Warehouse](https://www.oracle.com/in/database/data-warehouse.html)

## Installation

See the [Installation](charts/oci-service-broker/docs/installation.md) instructions for detailed installation and configuration of OCI Service Broker.

### Build

**Pre-requisites:**

* JDK 10 & above
* [Gradle](https://gradle.org/)
   (Recommended Gradle version: v4.10.3)
* Docker

The source code for OCI Service Broker is written in java and the code, can be found [here](oci-service-broker).

 **Step 1:** Download [oci-javasdk v1.3.1](https://github.com/oracle/oci-java-sdk/releases/download/v1.3.1/oci-java-sdk.zip) archive file.

 The OCI Service Broker internally uses [oci-java-sdk](https://github.com/oracle/oci-java-sdk) to manage OCI services. But they are not published to any public maven repositories yet. In order to build the project, users are required to download oci-java-sdk archive file and add the dependent libraries to libs directory of oci-service-broker. The can be done by running [download_SDK_libs.sh](oci-service-broker/download_SDK_libs.sh) script.

 ```bash
 bash oci-service-broker/download_SDK_libs.sh
 ```

 **Step 2:** Compile and build oci-service-broker docker image. Gradle Build Tool is used for building the oci-service-broker.

```bash
gradle -b oci-service-broker/build.gradle clean build docker
```

 **Step 3:** The docker image oci-service-broker is available in the local docker repository. Push the docker image to [OCIR](https://docs.cloud.oracle.com/iaas/Content/Registry/Concepts/registryoverview.htm) or your own docker repository and refer this image in the Helm Chart.

 **Step 4:** Install oci-service-broker chart

Update the [chart](charts/oci-service-broker) to use the image that was built in **Step 3** with values for `image.repository` and `image.tag` and deploy the chart.

## Documentation

See the [Documentation](charts/oci-service-broker/README.md#oci-service-broker) for complete details on installation, security and service related configurations of OCI Service Broker.

## Charts

The OCI Service Broker is packaged as Helm chart for making it easy to install in Kubernetes Clusters. Please refer to [Documentation](#documentation) for detailed instructions.

## Samples

Samples for creating Service Instances and Bindings using `oci-service-broker`, can be found [here](charts/oci-service-broker/samples).

## Troubleshooting

See [Troubleshooting](charts/oci-service-broker/docs/troubleshoot.md#troubleshooting-guide-for-oci-service-broker) document for details on debugging common and known issues.

## Changes

See [CHANGELOG](CHANGELOG.md).

## Contributing

`oci-service-broker` is an open source project. See [CONTRIBUTING](CONTRIBUTING.md) for details.

Oracle gratefully acknowledges the contributions to `oci-service-broker` that have been made by the community.

## License

Copyright (c) 2019, Oracle and/or its affiliates.

This software is available under the [Universal Permissive License v 1.0](http://oss.oracle.com/licenses/upl)

See [LICENSE.txt](LICENSE.txt) for more details.
