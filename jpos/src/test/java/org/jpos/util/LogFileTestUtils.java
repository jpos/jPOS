/*
 * Copyright (c) 2003 - 2013 Tyro Payments Limited.
 * 125 York St, Sydney NSW 2000.
 * All rights reserved.
 */
package org.jpos.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

public class LogFileTestUtils {
    public static String getStringFromCompressedFile(File file) throws IOException {
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(file));
        try {
            return getStringFromInputStream(in);
        } finally {
            in.close();
        }
    }

    public static String getStringFromFile(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            return getStringFromInputStream(in);
        } finally {
            in.close();
        }
    }

    public static String getStringFromInputStream(InputStream inStream) {
       Scanner in = new Scanner(inStream);
       StringBuilder logFileContentsBuilder = new StringBuilder();
       while (in.hasNextLine()) {
           logFileContentsBuilder.append(in.nextLine()).append('\n');
       }
       in.close();
       return logFileContentsBuilder.toString();
   }
}
