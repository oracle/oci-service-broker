/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.osb.adapters.adb;

import com.oracle.oci.osb.util.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ADBUtils {

    /**
     * generateCredentialsMap reads credential/configuration files for connecting to an ATP/ADW instance from passed
     * InputStream and builds and returns a MAP with filename as key and base64 encoded content of the
     * credential/configuration file as value.
     *
     * @param dbName name of the database.
     * @param in     inputStream of the Credentials Zip file.
     * @return Map with filename/attribute name as keys and filename/attribute base64 encoded contents as values.
     * @throws IOException if downloading credential zip file fails.
     */
    public static Map<String, String> generateCredentialsMap(String dbName, InputStream in) throws IOException {
        Path tempDir = Files.createTempDirectory(dbName);
        Path tmpFile = Files.createTempFile(tempDir, dbName, ".zip");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tmpFile.toFile());
            byte[] b = new byte[1024];
            int count;
            while ((count = in.read(b)) >= 0) {
                out.write(b, 0, count);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }

        Map<String, String> credMap = new HashMap<>();
        //unzip file
        try (FileInputStream fip = new FileInputStream(tmpFile.toFile()); ZipInputStream zis = new ZipInputStream
                (fip)) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                byte[] data = Utils.toByteArray(zis);
                String b64Data = Base64.getEncoder().encodeToString(data);
                credMap.put(ze.getName(), b64Data);
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
        } finally {
            Files.delete(tmpFile);
            Files.delete(tempDir);
        }
        return credMap;
    }
}
