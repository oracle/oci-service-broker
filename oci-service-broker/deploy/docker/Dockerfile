#
# Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
# Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
#
FROM openjdk:12.0.1-jdk-oracle
USER root
RUN yum -y install unzip &&\
    yum clean all &&\
    rm -f /var/log/yum.log 

RUN mkdir -p /oci-service-broker/config && \
    mkdir /oci-service-broker/lib

ADD install_openssl.sh /tmp
RUN bash -e /tmp/install_openssl.sh

ADD start-broker.sh /oci-service-broker
ADD *.jar /oci-service-broker/lib/
RUN groupadd -g 999 svcbroker && \
    useradd -r -u 999 -g svcbroker svcbroker
RUN chown -R svcbroker:svcbroker /oci-service-broker
USER svcbroker
ENTRYPOINT ["/oci-service-broker/start-broker.sh"]
