/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

public class LogFileTestUtils {
    public static String getStringFromCompressedFile(Path file) throws IOException {
        GZIPInputStream in = new GZIPInputStream(Files.newInputStream(file));
        try {
            return getStringFromInputStream(in);
        } finally {
            in.close();
        }
    }

    public static String getStringFromFile(Path file) throws IOException {
        InputStream in = Files.newInputStream(file);
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
