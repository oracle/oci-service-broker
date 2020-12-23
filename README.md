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
1. [Oracle Streaming Service](https://docs.cloud.oracle.com/iaas/Content/Streaming/Concepts/streamingoverview.htm)

## Installation

See the [Installation](charts/oci-service-broker/docs/installation.md) instructions for detailed installation and configuration of OCI Service Broker.

## Documentation

See the [Documentation](charts/oci-service-broker/README.md#oci-service-broker) for complete details on installation, security and service related configurations of OCI Service Broker.

## Charts

The OCI Service Broker is packaged as Helm chart for making it easy to install in Kubernetes Clusters. The chart can be downloaded from below URL.

```
https://github.com/oracle/oci-service-broker/releases/download/v1.5.2/oci-service-broker-1.5.2.tgz
```

## Samples

Samples for creating Service Instances and Bindings using `oci-service-broker`, can be found [here](charts/oci-service-broker/samples).

## Troubleshooting

You can use the [diagnostics tool](charts/oci-service-broker/tools/diagnostics_tool.sh) to help identify the common issues in the installation.

Also see [Troubleshooting](charts/oci-service-broker/docs/troubleshoot.md#troubleshooting-guide-for-oci-service-broker) document for details on debugging common and known issues.

## Changes

See [CHANGELOG](CHANGELOG.md).

## Contributing

`oci-service-broker` is an open source project. See [CONTRIBUTING](CONTRIBUTING.md) for details.

Oracle gratefully acknowledges the contributions to `oci-service-broker` that have been made by the community.

## License

Copyright (c) 2019, Oracle and/or its affiliates.

This software is available under the [Universal Permissive License v 1.0](http://oss.oracle.com/licenses/upl)

See [LICENSE.txt](LICENSE.txt) for more details.
