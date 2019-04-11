/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.util;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.oracle.oci.osb.util.Utils.getLogger;

/**
 * RequestUtil contains utility function for reading and validating parameters
 * from the Request body.
 */
public class RequestUtil {

    private static final Logger LOGGER = getLogger(RequestUtil.class);

    /**
     * Returns the value of parameter {@code key} that is of type String. Throws
     * an exception if {@code mandatory} is true and the value is empty or not
     * present.
     *
     * @param mapParameters
     * @param key
     * @param mandatory
     * @return parameter value.
     */
    public static String getStringParameter(Map mapParameters, String key, boolean mandatory) {
        Object parameter = mapParameters.get(key);
        if (parameter == null && mandatory) {
            throw Errors.missingParameter(key);
        }
        return getStringObject(parameter);
    }

    /**
     * Returns the value of parameter {@code key} that is of type String. Throws
     * an exception if the value is empty or not present.
     *
     * @param mapParameters
     * @param key
     * @return parameter value.
     */
    public static String getNonEmptyStringParameter(Map mapParameters, String key) {
        String value = getStringParameter(mapParameters, key, true);
        if(Utils.isNullOrEmptyString(value)) {
            throw Errors.invalidParameter(key);
        }
        return value;
    }


    /**
     * Returns the value of parameter {@code key} that is of type integer. Throws an exception if {@code mandatory}
     * is true and the value is empty. Also throws exception if value is not a valid integer.
     *
     * This methods <b>returns null</b> if the parameter is not present and it is not {@code mandatory}.
     *
     * @param mapParameters
     * @param key
     * @param mandatory
     * @return parameter value.
     */
    public static Integer getIntegerParameter(Map mapParameters, String key, boolean mandatory) {
        Object parameter = mapParameters.get(key);
        if (parameter == null) {
            if (mandatory) {
                throw Errors.missingParameters();
            }
            return null;
        } else {
            if (parameter instanceof Integer || parameter instanceof Short) {
                return (Integer) parameter;
            } else {
                if(parameter.toString().chars().allMatch(Character::isDigit)) {
                    try {
                        return Integer.parseInt(parameter.toString());
                    } catch(NumberFormatException e){
                        //We may have got Long value.
                        //ignore.
                    }
                }
                LOGGER.severe("Got invalid integer value " + parameter.toString() + " for key " + key);
                throw Errors.invalidParameter(key);
            }
        }
    }

    private static String getStringObject(Object parameter) {
        if (parameter != null) {
            return parameter.toString();
        }
        return null;
    }



    /**
     * Returns the value of parameter {@code key} that is of type Map. Throws
     * an exception if {@code mandatory} is true and the value is empty or not
     * present.
     *
     * @param mapParameters
     * @param key
     * @param mandatory
     * @return parameter value of type Map.
     */
    public static Map<String, String> getMapStringParameter(Map mapParameters, String key, boolean mandatory) {
        Object parameter = mapParameters.get(key);
        if (parameter == null) {
            if (mandatory) {
                throw Errors.missingParameters();
            } else {
                return new HashMap<>();
            }
        }
        if (!(parameter instanceof Map)) {
            throw Errors.invalidParameters();
        }
        Map map = (Map) parameter;
        Map<String, String> resultMap = new HashMap<>();

        Set<Map.Entry> entrySet = map.entrySet();
        for (Map.Entry entry : entrySet) {
            String keyStr = getStringObject(entry.getKey());
            String valueStr = getStringObject(entry.getValue());
            resultMap.put(keyStr, valueStr);
        }
        return resultMap;
    }

    private static Map<String, Object> getMapObjectParameter(Map mapParameters, String key) {
        Object parameter = mapParameters.get(key);
        if (!(parameter instanceof Map)) {
            throw Errors.invalidParameters();
        }
        Map map = (Map) parameter;
        Map<String, Object> resultMap = new HashMap<>();

        for (Object keyObj : map.keySet()) {
            String keyStr = getStringObject(keyObj);
            resultMap.put(keyStr, map.get(keyStr));
        }
        return resultMap;
    }

    /**
     * Returns the value of parameter {@code key} that is of type Map of Map.
     * Throws an exception if {@code mandatory} is true and the value is empty
     * or not present.
     *
     * @param mapParameters
     * @param key
     * @param mandatory
     * @return parameter value of type Map of Map.
     */
    public static Map<String, Map<String, Object>> getMapMapObjectParameter(Map     mapParameters,
                                                                            String  key,
                                                                            boolean mandatory) {
        Object parameter = mapParameters.get(key);
        if (parameter == null) {
            if (mandatory) {
                throw Errors.missingParameters();
            } else {
                return null;
            }
        }

        if (!(parameter instanceof Map)) {
            throw Errors.invalidParameters();
        }
        Map map = (Map) parameter;
        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        for (Object keyObj : map.keySet()) {
            String keyStr = getStringObject(keyObj);
            Object value = map.get(keyStr);
            if (!(value instanceof Map)) {
                throw Errors.invalidParameters();
            }

            resultMap.put(keyStr, getMapObjectParameter(map, keyStr));
        }
        return resultMap;
    }

    public static void abortWithError(ContainerRequestContext context, Response response, String msg) {
        if (context != null && response != null) {
            context.abortWith(response);
            throw new RuntimeException(msg);
        }
    }

}
