#!/bin/bash
#
# Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
# Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
#

set -x
TAGS=""

init() {
   while (( $# > 0 )); do
      case "${1}" in
         "--port" )
            PORT="${2}"
            shift
            ;;
         "--privatekey" )
            PRIVATEKEY="${2}"
            shift
            ;;
         "--log.configfile" )
            LOG_CONFIG_FILE="${2}"
            shift
            ;;
         "--logLevel" )
            LOG_LEVEL="${2}"
            shift
            ;;
         "--ociSdkLogLevel" )
            OCI_SDK_LOG_LEVEL="${2}"
            shift
            ;;
         "--jvmProps" )
            JVM_PROPS="${2}"
            shift
            ;;
         "--tlsEnabled" )
            TLS_ENABLED="${2}"
            shift
            ;;
         "--etcd.servers" )
            ETCD_SERVERS="${2}"
            shift
            ;;
         "--storeType" )
            STORE_TYPE="${2}"
            shift
            ;;
         "--apiServerCaCerts" )
            API_SERVER_CA_CERTS="${2}"
            shift
            ;;
         "--etcd.client.tls.enabled" )
            ETCD_CLIENT_TLS_ENABLED="${2}"
            shift
            ;;
         "--serviceTag" )
            key="${2%%=*}"; value="${2#*=}"
            TAGS="$TAGS -DserviceTag.${key}=${value}"
            shift
            ;;
      esac
      shift
   done
}

init "$@"

export LD_LIBRARY_PATH="/openssl/lib"

if [ ${TLS_ENABLED} = true ]
then
   KEYSTORE_PASSWORD=$(cat /oci-service-broker/tlsBundle/keyStore.password)
   TLS_PROPS="-DkeyStore=/oci-service-broker/tlsBundle/keyStore -DkeyStorePassword=${KEYSTORE_PASSWORD}"
fi

LIB_DIR="/oci-service-broker/lib"
if ! ls /oci-service-broker/lib/oci-java-sdk* &> /dev/null; then
   # Donwload OCI java SDK jar
   SDK_VERSION="1.3.1"
   TEMP_DIR="/tmp/oci-java-sdk"
   rm -rf ${TEMP_DIR}
   mkdir -p ${TEMP_DIR} 
   mkdir -p ${LIB_DIR}
   curl -LsS https://github.com/oracle/oci-java-sdk/releases/download/v${SDK_VERSION}/oci-java-sdk.zip -o ${TEMP_DIR}/oci-java-sdk.zip
   unzip -qq ${TEMP_DIR}/oci-java-sdk.zip -d ${TEMP_DIR}
   cp ${TEMP_DIR}/lib/oci-java-sdk-full-${SDK_VERSION}.jar ${LIB_DIR}/
   rm -rf ${TEMP_DIR}
fi

exec java -cp "${LIB_DIR}/*" ${JVM_PROPS} -Dport="$PORT" -Dtenancy=${TENANCY} -Dfingerprint=${FINGERPRINT} -Duser=${USER}\
                       -Dpassphrase=${PASSPHRASE} -Dprivatekey="$PRIVATEKEY" -DregionId=${REGION} -Djava.util.logging.config.file="${LOG_CONFIG_FILE}"\
                       -DlogLevel="${LOG_LEVEL}" -Dorg.slf4j.simpleLogger.defaultLogLevel="${OCI_SDK_LOG_LEVEL}" -Dio.netty.noUnsafe="true"\
                       -DapiServerCaCert="${API_SERVER_CA_CERTS}" -DtlsEnabled="${TLS_ENABLED}" ${TLS_PROPS} -DstoreType="${STORE_TYPE}" -Detcd.servers="${ETCD_SERVERS}"\
                       -DetcdTlsEnabled="${ETCD_CLIENT_TLS_ENABLED}" -DCAPath="/oci-service-broker/etcdTlsSecret/etcd-client-ca.crt"\
                       -DetcdClientCert="/oci-service-broker/etcdTlsSecret/etcd-client.crt"\
                       -DetcdClientKey="/oci-service-broker/etcdTlsSecret/etcd-client.key" ${TAGS} -Dk8sApiTokenFile="/var/run/secrets/kubernetes.io/serviceaccount/token"\
                         com.oracle.oci.osb.Broker

