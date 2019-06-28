/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utils class provides common utility methods.
 */
public class Utils {

    private static final Logger LOGGER = getLogger(Utils.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static String K8S_MASTER_URL = null;

    /**
     * Return Logger for a given class
     * @param loggerClass class that requires the logger.
     * @return instance of Logger.
     */
    public static Logger getLogger(Class loggerClass){
        return Logger.getLogger(loggerClass.getName());
    }

    /**
     * Returns true if passed string is either empty or an empty string.
     * @param value string to be checked.
     * @return true if passed string is either empty or an empty string.
     */
    public static boolean isNullOrEmptyString(String value){
        if(value == null) {
            return true;
        } else {
            if("".equals(value.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Log debug message using the given logger.
     * @param logger logger to use log the message.
     * @param format log message format
     * @param level  log level
     * @param logArgs log arguments
     */
    public static void debugLog(Logger logger,  String format, Level level, Object... logArgs){
        if(logger.isLoggable(level)) {
            logger.log(level, String.format(format, logArgs));
        }
    }

    /**
     * Read data from a Inputstream and return it as byte array.
     * @param is Inputstream from which data needs to be read.
     * @return byte array with the data read from Inputstream.
     * @throws IOException If error reading data from InputStream.
     */
    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if(is != null) {
            byte[] chunk = new byte[4096];
            int read;
            while ((read = is.read(chunk, 0, chunk.length)) != -1) {
                bos.write(chunk, 0, read);
            }
        }
        return bos.toByteArray();
    }

    /**
     * Returns the k8s master url by reading environment variables set by k8s.
     *
     * @return String k8s master url
     */
    public static String getK8sMasterUrl() {
        if (K8S_MASTER_URL == null) {
            StringBuilder sb = new StringBuilder("https://");
            String k8sHost = System.getenv("KUBERNETES_SERVICE_HOST");
            String k8sPort = System.getenv("KUBERNETES_SERVICE_PORT");
            sb.append(k8sHost).append(":").append(k8sPort);
            K8S_MASTER_URL = sb.toString();
        }
        return K8S_MASTER_URL;
    }

    public static <T> ObjectReader getReader(Class<T> type) {
        return mapper.readerFor(type);
    }

    public static String serializeToJson(Object obj) {
        if (obj != null) {
            ObjectWriter writer = mapper.writerFor(obj.getClass());
            try {
                return writer.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                LOGGER.finest(e.getMessage());
            }
        }
        return null;
    }
}
