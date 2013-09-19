package org.jpos.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Uses java.util.zip to compress an input file and store it to the given output. Only valid for single files, not directories.
 */
public class ZipUtil {

    public static void zipFile(File input, File output) throws IOException {
        FileInputStream in = null;
        ZipOutputStream out = null;
        try {
            in = new FileInputStream(input);
            out = new ZipOutputStream(new FileOutputStream(output));
            out.putNextEntry(new ZipEntry(input.getName()));
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            out.closeEntry();
            out.finish();
        } finally{
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
