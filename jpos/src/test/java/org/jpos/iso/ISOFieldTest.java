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

package org.jpos.iso;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class ISOFieldTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOField iSOField = new ISOField(100);
        assertEquals(100, iSOField.fieldNumber, "iSOField.fieldNumber");
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOField iSOField = new ISOField(100, "testISOFieldv");
        assertEquals(100, iSOField.fieldNumber, "iSOField.fieldNumber");
        assertEquals("testISOFieldv", iSOField.value, "iSOField.value");
    }

    @Test
    public void testConstructor2() throws Throwable {
        ISOField iSOField = new ISOField();
        assertEquals(-1, iSOField.fieldNumber, "iSOField.fieldNumber");
    }

    @Test
    public void testDump() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true);
        new ISOField(100, "testISOFieldv").dump(p, "testISOFieldIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testGetBytes() throws Throwable {
        ISOField iSOField = new ISOField(100);
        iSOField.setValue("");
        byte[] result = iSOField.getBytes();
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testGetBytes1() throws Throwable {
        byte[] result = new ISOField(100, "testISOFieldv").getBytes();
        assertEquals(13, result.length, "result.length");
        assertEquals((byte) 116, result[0], "result[0]");
    }

    @Test
    public void testGetBytesThrowsNullPointerException() throws Throwable {
        byte[] bytes = new ISOField(100).getBytes();
        assertArrayEquals(new byte[0], bytes);
    }

    @Test
    public void testGetKey() throws Throwable {
        Integer result = (Integer) new ISOField(100).getKey();
        assertEquals(100, result.intValue(), "result");
    }

    @Test
    public void testGetKey1() throws Throwable {
        Integer result = (Integer) new ISOField(0).getKey();
        assertEquals(0, result.intValue(), "result");
    }

    @Test
    public void testGetValue() throws Throwable {
        String result = (String) new ISOField(100, "testISOFieldv").getValue();
        assertEquals("testISOFieldv", result, "result");
    }

    @Test
    public void testGetValue1() throws Throwable {
        Object result = new ISOField(100).getValue();
        assertNull(result, "result");
    }

    @Test
    public void testPackThrowsISOException() throws Throwable {
        try {
            new ISOField(100).pack();
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Not available on Leaf", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
        }
    }

    @Test
    public void testSetFieldNumber() throws Throwable {
        ISOField iSOField = new ISOField(100);
        iSOField.setFieldNumber(1000);
        assertEquals(1000, iSOField.fieldNumber, "iSOField.fieldNumber");
    }

    @Test
    public void testSetValue() throws Throwable {
        ISOField iSOField = new ISOField(100);
        iSOField.setValue(" W");
        assertEquals(" W", iSOField.value, "iSOField.value");
    }

    @Test
    public void testSetValue1() throws Throwable {
        ISOField iSOField = new ISOField(100);
        iSOField.setValue(200);
        assertThat("isoField setter", "200", equalTo(iSOField.value));
    }

    @Test
    public void testUnpackThrowsISOException() throws Throwable {
        try {
            new ISOField(100).unpack(new ByteArrayInputStream("".getBytes()));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Not available on Leaf", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
        }
    }

    @Test
    public void testUnpackThrowsISOException1() throws Throwable {
        byte[] b = new byte[2];
        try {
            new ISOField(100, "testISOFieldv").unpack(b);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("Not available on Leaf", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.nested, "ex.nested");
        }
    }

    @Test
    public void testWriteExternal() throws Throwable {
        ObjectOutput out = new ObjectOutputStream(new ByteArrayOutputStream());
        new ISOVField(new ISOField(100, "testISOFieldv"), new ISOVError("testISOFieldDescription", "testISOFieldRejectCode"))
                .writeExternal(out);
    }

    @Test
    public void testWriteExternalThrowsNullPointerException() throws Throwable {
        ObjectOutput out = new ObjectOutputStream(new ByteArrayOutputStream());
        try {
            new ISOField(100).writeExternal(out);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"s\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testWriteExternalThrowsNullPointerException1() throws Throwable {
        try {
            new ISOField(100).writeExternal(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.ObjectOutput.writeShort(int)\" because \"out\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
