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

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;

public class ZipUtilTest {

    @Test
    public void testZipFile() throws Throwable {
        File testFile = File.createTempFile("testFile",".txt");
        File testFileZip = File.createTempFile("testFile",".zip");

        PrintWriter writer = new PrintWriter(testFile);
        writer.write("this is a test");
        writer.close();

        assertThat(testFile.exists(), is(true));
        assertThat(testFileZip.exists(), is(true));

        assertThat(testFileZip.length(), is(0L));

        ZipUtil.zipFile(testFile, testFileZip);

        assertThat(testFileZip.length(), not(0L));

        //unzip it and check the content.
        ZipFile zipFile = new ZipFile(testFileZip);
        ZipEntry zipEntry = zipFile.getEntry(testFile.getName());
        assertThat(zipEntry, notNullValue());
        assertThat(new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry))).readLine(), is("this is a test"));

        zipFile.close();
        testFile.delete();
        testFileZip.delete();
    }
}
