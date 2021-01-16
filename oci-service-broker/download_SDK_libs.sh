#!/bin/bash -e
#
# Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
# Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
#

#oci-java-sdk is not published to any public maven repo yet. In order to build the project users are required
#to download oci-java-sdk and the dependent libraries to libs directory. This scripts takes care of downloading 
#sdk jars and their dependency jars. The jars are written to libs directory.

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SDK_VERSION="1.28.0"
TEMP_DIR="/tmp/oci-java-sdk"
rm -rf ${TEMP_DIR}
mkdir -p ${TEMP_DIR} 
mkdir -p ${SCRIPT_DIR}/libs
echo "Downloading oci-java-sdk version v${SDK_VERSION} and the dependent libraries..."
curl -sSL https://github.com/oracle/oci-java-sdk/releases/download/v${SDK_VERSION}/oci-java-sdk-${SDK_VERSION}.zip -o ${TEMP_DIR}/oci-java-sdk.zip
unzip -qq ${TEMP_DIR}/oci-java-sdk.zip -d ${TEMP_DIR}
cp ${TEMP_DIR}/lib/oci-java-sdk-full-${SDK_VERSION}.jar ${SCRIPT_DIR}/libs/
cp ${TEMP_DIR}/third-party/lib/*.jar  ${SCRIPT_DIR}/libs/
rm -rf ${TEMP_DIR}
echo "oci-java-sdk and the dependent libraries are downloaded to ${SCRIPT_DIR}/libs directory"
