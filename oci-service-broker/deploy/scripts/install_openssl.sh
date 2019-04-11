#!/bin/bash
#
# Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
# Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
#

# Install build tools
yum  install -y  \
     autoconf.noarch 0:2.69-11.el7.x86_64 \
     libtool.x86_64 0:2.4.2-22.el7_3.x86_64 \
     make.x86_64 1:3.82-23.el7.x86_64 \

OPEN_SSL_PACKAGE="openssl-1.1.1a.tar.gz"
OPEN_SSL_PACKAGE_SHA256="fc20130f8b7cbd2fb918b2f14e2f429e109c31ddd0fb38fc5d71d9ffed3f9f41"
APR_PACKAGE="apr-1.6.5.tar.gz"
APR_PACKAGE_SHA256="70dcf9102066a2ff2ffc47e93c289c8e54c95d8dda23b503f9e61bb0cbd2d105"

mkdir /apr-build
cd /apr-build
curl -L http://archive.apache.org/dist/apr/${APR_PACKAGE} -o ${APR_PACKAGE}
sha256sum ${APR_PACKAGE} | grep ${APR_PACKAGE_SHA256}
tar zxvf ${APR_PACKAGE} 
cd apr*
./configure

make
make install


mkdir /openssl-build
mkdir /openssl
cd /openssl-build
# Build openssl
cd /openssl-build 

curl -L https://www.openssl.org/source/${OPEN_SSL_PACKAGE} -o ${OPEN_SSL_PACKAGE}
sha256sum ${OPEN_SSL_PACKAGE} | grep ${OPEN_SSL_PACKAGE_SHA256}
tar zxvf ${OPEN_SSL_PACKAGE}
cd openssl*
./config shared --prefix=/openssl --openssldir=/openssl
make depend
make
make install

# Delete all non-English locale
localedef --list-archive | grep -v -i ^en | xargs localedef --delete-from-archive
rm -rf /usr/lib/locale/locale-archive.tmpl
rm -rf /openssl-build
rm -rf /apr-build

mv /usr/lib/locale/locale-archive /usr/lib/locale/locale-archive.tmpl
build-locale-archive

# First yum installs 61 packages. But remove deletes only 30 packages or so due to common dependencies.
# Hence we delete the dependencies installed earlier explicitly.
yum remove -y autoconf make git ruby libtool perl \
    gcc cpp kernel-headers libgomp mpfr groff-base libgnome-keyring libyaml m4 rsync ruby-libs
    
yum clean all
rm -rf /var/cache/yum
rm -f /var/log/yum.log

