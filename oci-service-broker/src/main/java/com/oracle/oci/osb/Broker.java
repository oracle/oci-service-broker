/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.oci.osb.ociclient.SystemPropsAuthProvider;
import com.oracle.oci.osb.rest.OCIOSBApplication;
import com.oracle.oci.osb.util.Constants;
import com.oracle.oci.osb.util.Errors;
import com.oracle.oci.osb.util.Utils;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.ws.rs.core.UriBuilder;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Broker initializes the  HTTP server that implements Open Service BrokerAPI.
 */
public class Broker {

    private static final Logger LOGGER = Utils.getLogger(Broker.class);

    private static final String SYS_PROP_LOG_LEVEL = "logLevel";

    private static final String SYS_PROP_PORT = "port";

    private static final String HTTPS_SERVER_URI = "https://localhost/";
    private static final String HTTP_SERVER_URI = "http://localhost/";

    private static final String SERVER_DEFAULT_PORT = "9998";

    private HttpServer httpServer;

    private OCIOSBApplication application = new OCIOSBApplication();

    public static void main(String[] args) {
        new Broker().start();
    }

    private static void initializeLogger() {
        String levelStr = System.getProperty(SYS_PROP_LOG_LEVEL);
        LOGGER.info("Initializing Logger..");
        if (!Utils.isNullOrEmptyString(levelStr)) {
            Level level;
            try {
                if ("debug".equalsIgnoreCase(levelStr)) {
                    level = Level.FINE;
                } else {
                    level = Level.parse(levelStr);
                }
                Logger.getLogger("").setLevel(level);
                for (Handler handler : Logger.getLogger("").getHandlers()) {
                    handler.setLevel(level);
                }
            } catch (IllegalArgumentException e) {
                Logger.getLogger("").severe("Illegal Log level string: " + levelStr);
            }
        }
    }

    private void checkOCIAuth(){
        //Check OCI authentication details
        //No explicit api available to check authentication. Hence we are using  list compartments.
        try {
            AuthenticationDetailsProvider auth = new SystemPropsAuthProvider().getAuthProvider();
            IdentityClient identityClient = new IdentityClient(auth);
            identityClient.setRegion(Region.fromRegionId(System.getProperty(Constants.REGION_ID)));
            identityClient.listCompartments(ListCompartmentsRequest.builder()
                    .limit(1)
                    .compartmentId(auth.getTenantId())
                    .accessLevel(ListCompartmentsRequest.AccessLevel.Accessible)
                    .build());
        } catch(Exception x){
            LOGGER.severe("Error verifying authentication details. "+x.getMessage());
            String errorMessage = x.getMessage();
            if(errorMessage != null) {
                if(x.getMessage().contains("401, NotAuthenticated, false")) {
                    errorMessage = "Invalid/Wrong OCI Credentials. Please verify the details provided in the " +
                            "Kubernetes secrets that has the OCI credentials.";
                }
            }
            throw new RuntimeException("Authentication failed; Cause:" + errorMessage);
        }
    }

    /**
     * Start Service Broker
     */
    public void start() {
        try {
            initializeLogger();
            LOGGER.info("Starting OCI Service Broker...");
            checkOCIAuth();
            int port = Integer.parseInt(System.getProperty(SYS_PROP_PORT, SERVER_DEFAULT_PORT));
            if (Boolean.getBoolean(Constants.TLS_ENABLED)) {
                SSLContext sslContext = createSslContext();
                String[] enabledCiphers = getEnabledCiphers();
                String[] protocols = Constants.TLS_PROTOOLS.toArray(new String[0]);

                URI baseUri = UriBuilder.fromUri(HTTPS_SERVER_URI).port(port).build();
                httpServer = JdkHttpServerFactory.createHttpServer(baseUri, application, sslContext, false);

                ((HttpsServer)httpServer).setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                    @Override
                    public void configure(HttpsParameters params) {
                        SSLParameters parameters = getSSLContext().getDefaultSSLParameters();
                        parameters.setCipherSuites(enabledCiphers);
                        parameters.setProtocols(protocols);
                        params.setSSLParameters(parameters);
                    }
                });
                httpServer.start();

            } else {
                LOGGER.warning("Insecure configuration found. TLS is not enabled. It is highly recommended to enable " +
                        "TLS.");
                URI baseUri = UriBuilder.fromUri(HTTP_SERVER_URI).port(port).build();
                httpServer = JdkHttpServerFactory.createHttpServer(baseUri, application);
            }

            LOGGER.info("Started OCI Service Broker: listening in port " + port);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Error starting Broker. Exiting application...", e);
            LOGGER.severe(e.getMessage());
            stop();

        }
    }

    /**
     * Stop Service Broker
     */
    public void stop() {
        try {
            if (application != null) {
                application.stop();
            }
            if (httpServer != null) {
                httpServer.stop(0);
            }
        } catch(Exception x){
            //ignore error while stopping application
        }
    }

    private SSLContext createSslContext() throws Exception {
        String keystoreFilename = System.getProperty(Constants.KEY_STORE);
        if (keystoreFilename == null || keystoreFilename.trim().equals("")) {
            throw Errors.invalidKeystoreFile();
        }
        char[] keyStorePassword = System.getProperty(Constants.KEY_STORE_PASSWORD).toCharArray();

        if (keyStorePassword.length == 0) {
            throw Errors.invalidKeystorePassword();
        }

        FileInputStream keystoreInputStream = null;

        try {
            keystoreInputStream = new FileInputStream(keystoreFilename);
            KeyStore keystore = KeyStore.getInstance(Constants.KEY_STORE_TYPE);
            keystore.load(keystoreInputStream, keyStorePassword);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(Constants.KEY_MGR_TYPE);
            kmf.init(keystore, keyStorePassword);

            // since we are setting up an http server, we are assuming that the system
            // trust store is the one we need to use and not a custom one
            SSLContext sslContext = SSLContext.getInstance(Constants.SSL_VERSION);
            sslContext.init(kmf.getKeyManagers(), null, null);
            return sslContext;
        } finally {
            if (keystoreInputStream != null) {
                keystoreInputStream.close();
            }
        }
    }

    private String[] getEnabledCiphers() {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(Constants.ENABLED_CIPHERS_RESOURCE)){
            String ciphersString = new String(stream.readAllBytes(), Constants.CHARSET_UTF8);
            return ciphersString.split("\\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

