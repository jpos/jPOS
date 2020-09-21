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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import static org.apache.commons.lang3.JavaVersion.JAVA_13;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.space.SpaceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class FSDMsgTest {

    static final String SCHEMA_PREFIX = "test-";

    @Mock
    FSDMsg msg;

    @Mock
    InputStreamReader is;

    private static Element createSchema() {
        Element schema = new Element("schema");
        schema.setAttribute("id","base");
        SpaceFactory.getSpace().put(SCHEMA_PREFIX+"base.xml", schema);
        return schema;
    }

    private static void appendField(Element schema, String id, String type
            , String separator, int len) {
        Element field = new Element("field");
        field.setAttribute("id", id);
        field.setAttribute("type", type);
        if (separator!=null)
          field.setAttribute("separator", separator);
        field.setAttribute("length", String.valueOf(len));
        schema.addContent(field);
    }

    @Test
    public void testConstructor() throws Throwable {

        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        assertEquals("testFSDMsgBasePath", fSDMsg.basePath, "fSDMsg.basePath");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        assertEquals(6, fSDMsg.separators.size(), "fSDMsg.separators.size()");
        assertEquals("base", fSDMsg.baseSchema, "fSDMsg.baseSchema");
    }

    @Test
    public void testConstructor1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        assertEquals("testFSDMsgBasePath", fSDMsg.basePath, "fSDMsg.basePath");
        assertEquals(6, fSDMsg.separators.size(), "fSDMsg.separators.size()");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        assertEquals("testFSDMsgBaseSchema", fSDMsg.baseSchema, "fSDMsg.baseSchema");
    }

    @Test
    public void testCopy() throws Throwable {
        FSDMsg msg = new FSDMsg("testFSDMsgBasePath");
        msg.copy("testFSDMsgFieldName", msg);
        assertEquals(1, msg.fields.size(), "msg.fields.size()");
        assertNull(msg.fields.get("testFSDMsgFieldName"), "msg.fields.get(\"testFSDMsgFieldName\")");
    }

    @Test
    void testCopyWithDefault() throws Throwable {
        FSDMsg original = new FSDMsg("testFSDMsgBasePath");
        FSDMsg copy = new FSDMsg("testFSDMsgBasePath");
        original.set("testfield", "avalue");
        copy.copy("testfield", original, "default");
        copy.copy("notset", original, "default");
        assertEquals("avalue", copy.get("testfield"));
        assertEquals("default", copy.get("notset"));
    }

    @Test
    public void testCopyThrowsNullPointerException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        try {
            fSDMsg.copy("testFSDMsgFieldName", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.util.FSDMsg.get(String)\" because \"msg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testDump() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.copy("testFSDMsgFieldName", new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema"));
        byte[] h = new byte[3];
        fSDMsg.setHeader(h);
        fSDMsg.dump(new PrintStream(new ByteArrayOutputStream(), true, "UTF-16"), "testFSDMsgIndent");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testDump1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.copy("testFSDMsgFieldName", new FSDMsg("testFSDMsgBasePath1"));
        fSDMsg.dump(new PrintStream(new ByteArrayOutputStream(), true, "UTF-16"), "testFSDMsgIndent");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testDump2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        fSDMsg.dump(new PrintStream(new ByteArrayOutputStream(), true, "UTF-16"), "testFSDMsgIndent");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testDump3() throws Throwable {
        byte[] h = new byte[3];
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        fSDMsg.setHeader(h);
        fSDMsg.dump(new PrintStream(new ByteArrayOutputStream(), true, "UTF-16"), "testFSDMsgIndent");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.dump(null, "testFSDMsgIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.PrintStream.println(String)\" because \"p\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testDumpThrowsStringIndexOutOfBoundsException() throws Throwable {
        byte[] h = new byte[0];
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        fSDMsg.setHeader(h);
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-16");
        try {
            fSDMsg.dump(p, "testFSDMsgIndent");
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_13)) {
                assertEquals("String index out of range: -2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("begin 2, end 0, length 0", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGet() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        String result = fSDMsg.get("testFSDMsgFieldName");
        assertNull(result, "result");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGet10() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        String result = fSDMsg.get("testFSDMsgId", "AD", 100, null, null);
        assertEquals("                                                                                                    ",
                result, "result");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGet11() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        String result = fSDMsg.get("testFSDMsgId", "Kv", 100, null, null);
        assertEquals("", result, "result");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGet12() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        String result = fSDMsg.get("testFSDMsgId", "AD", 0, "testFSDMsgDefValue", null);
        assertEquals("", result, "result");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGet2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        String result = fSDMsg.get("testFSDMsgId", "2C", 100, null, null);
        assertEquals("", result, "result");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGet3() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.get("testFSDMsgId", "B", 100, "testFSDMsgDefValue", null);
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGet4() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set(" is used, but ", "testFSDMsgValue");
        String result = fSDMsg.get(" is used, but ", "Kv", 100, "testFSDMsgDefValue", null);
        assertEquals("testFSDMsgDefValue", result, "result");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGet5() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");

        when(msg.get("testString")).thenReturn("testString");

        fSDMsg.copy("testString", msg);

        String result = fSDMsg.get("testFSDMsgId", "N", 100, "testFSDMsgDefValue", null);
        assertEquals("0000000000000000000000000000000000000000000000000000000000000000000000000000000000testFSDMsgDefValue",
                result, "result");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGet6() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        String result = fSDMsg.get("testFSDMsgId", " ", 100, "testFSDMsgDefValue", null);
        assertEquals("testFSDMsgDefValue", result, "result");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGet7() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set(" is used, but ", "testFSDMsgValue");
        String result = fSDMsg.get(" is used, but ", "A", 100, "testFSDMsgDefValue", null);
        assertEquals("testFSDMsgValue                                                                                     ",
                result, "result");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGet8() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        String result = fSDMsg.get("testFSDMsgId", "Kv", 100, "testFSDMsgDefValue", null);
        assertEquals("testFSDMsgDefValue", result, "result");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGet9() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        fSDMsg.set("testString", "testFSDMsgValue");
        String result = fSDMsg.get("testString", "Kv", 100, null, null);
        assertEquals("testFSDMsgValue", result, "result");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGetHeader() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        byte[] h = new byte[2];
        fSDMsg.setHeader(h);
        byte[] result = fSDMsg.getHeader();
        assertSame(h, result, "result");
        assertEquals((byte) 0, h[0], "h[0]");
    }

    @Test
    public void testGetHexBytes() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testString", "testFSDMsgValue1");
        byte[] result = fSDMsg.getHexBytes("testString");
        assertEquals(8, result.length, "result.length");
        assertEquals((byte) -2, result[0], "result[0]");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGetHexBytes1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        byte[] result = fSDMsg.getHexBytes("testFSDMsgName");
        assertNull(result, "result");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGetHexBytes2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[1]);
        fSDMsg.readField(new InputStreamReader(bais), "testString", 0, "", null);
        byte[] result = fSDMsg.getHexBytes("testString");
        assertEquals(0, result.length, "result.length");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGetHexBytesThrowsRuntimeException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testString", "testFSDMsgValue");
        byte[] val = fSDMsg.getHexBytes("testString");
        assertArrayEquals(ISOUtil.hex2byte("testFSDMsgValue"), val);
    }

    @Test
    public void testGetHexHeader() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] h = new byte[1];
        fSDMsg.setHeader(h);
        String result = fSDMsg.getHexHeader();
        assertEquals("", result, "result");
    }

    @Test
    public void testGetHexHeader1() throws Throwable {
        String result = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema").getHexHeader();
        assertEquals("", result, "result");
    }

    @Test
    public void testGetHexHeaderThrowsStringIndexOutOfBoundsException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        byte[] h = new byte[0];
        fSDMsg.setHeader(h);
        try {
            fSDMsg.getHexHeader();
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_13)) {
                assertEquals("String index out of range: -2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("begin 2, end 0, length 0", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetInt() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testString", "1");
        int result = fSDMsg.getInt("testString");
        assertEquals(1, result, "result");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testGetInt1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        int result = fSDMsg.getInt("testFSDMsgName");
        assertEquals(0, result, "result");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetMap() throws Throwable {
        LinkedHashMap result = (LinkedHashMap) new FSDMsg("testFSDMsgBasePath").getMap();
        assertEquals(0, result.size(), "result.size()");
    }

    @Test
    public void testGetSchemaThrowsMalformedURLException() throws Throwable {
        assertThrows(MalformedURLException.class, () -> {
            FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
            fSDMsg.getSchema();
        });
    }

    @Test
    public void testGetSchemaThrowsMalformedURLException1() throws Throwable {
        assertThrows(MalformedURLException.class, () -> {
            FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
            fSDMsg.getSchema("testFSDMsgMessage");
        });
    }

    @Test
    public void testGetSchemaThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            FSDMsg fSDMsg = new FSDMsg(null, "testFSDMsgBaseSchema");
            fSDMsg.getSchema();
        });
    }

    @Test
    public void testGetSchemaThrowsNullPointerException1() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            FSDMsg fSDMsg = new FSDMsg(null, "testFSDMsgBaseSchema");
            fSDMsg.getSchema("testFSDMsgMessage");
        });
    }

    @Test
    public void testGetThrowsISOException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");

        when(msg.get("testString")).thenReturn("<fsdmsg schem='");

        fSDMsg.copy("testString", msg);

        try {
            fSDMsg.get("testFSDMsgId", "NB", 4, "testFSDMsgDefValue", null);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsISOException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "NB", 0, "testFSDMsgDefValue", null);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("invalid len 18/0", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsNullPointerException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testString", "testFSDMsgValue");
        try {
            fSDMsg.get("testString", null, 100, "testFSDMsgDefValue", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.toUpperCase()\" because \"type\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsNullPointerException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        try {
            fSDMsg.get("testFSDMsgId", null, 100, "testFSDMsgDefValue", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.toUpperCase()\" because \"type\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "3Ch", 100, "testFSDMsgDefValue", "3Ch");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("Invalid separator '3Ch'", ex.getMessage(),
                    "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testString", "testFSDMsgValue");
        try {
            fSDMsg.get("testString", "B]Z", 100, "testFSDMsgDefValue", "B]Z");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("Invalid separator 'B]Z'", ex.getMessage(),
                    "ex.getMessage()");
            assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException10() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testString", "testFSDMsgValue");
        try {
            fSDMsg.get("testString", "B]Z", 0, "testFSDMsgDefValue", "B]Z");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("field content=testFSDMsgValue is too long to fit in field testString whose length is 0",
                    ex.getMessage(), "ex.getMessage()");
            assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException11() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testString", "testFSDMsgValue");
        try {
            fSDMsg.get("testString", "", 100, "testFSDMsgDefValue", "");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("String index out of range: 0", ex.getMessage(), "ex.getMessage()");
            assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException12() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "testFSDMsgType", 100, null, "testFSDMsgType");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("Invalid separator 'testFSDMsgType'",
                    ex.getMessage(), "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        fSDMsg.set("testString", "testFSDMsgValue");
        try {
            fSDMsg.get("testString", "testFSDMsgType", 100, "testFSDMsgDefValue", "testFSDMsgType");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("Invalid separator 'testFSDMsgType'",
                    ex.getMessage(), "ex.getMessage()");
            assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException3() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testString", "testFSDMsgValue");
        try {
            fSDMsg.get("testString", "Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@", 100, "testFSDMsgDefValue",
                    "Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals(
                    "Invalid separator 'Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@'",
                    ex.getMessage(), "ex.getMessage()");
            assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException4() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@", 100, null,
                    "Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals(
                    "Invalid separator 'Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@'",
                    ex.getMessage(), "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException5() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "B]Z", 100, null, "B]Z");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("Invalid separator 'B]Z'", ex.getMessage(),
                    "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException6() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "B]Z", 100, "testFSDMsgDefValue", "B]Z");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("Invalid separator 'B]Z'", ex.getMessage(),
                    "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException7() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "B]Z", 0, "testFSDMsgDefValue", "B]Z");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("field content=testFSDMsgDefValue is too long to fit in field testFSDMsgId whose length is 0",
                    ex.getMessage(), "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException8() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@", 100, "testFSDMsgDefValue",
                    "Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals(
                    "Invalid separator 'Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@'",
                    ex.getMessage(), "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsRuntimeException9() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "", 100, null, null);
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("String index out of range: 0", ex.getMessage(), "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testGetThrowsStringIndexOutOfBoundsException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "", 100, "testFSDMsgDefValue", null);
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("String index out of range: 0", ex.getMessage(), "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testHasField() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        fSDMsg.readField(null, "testString", 0, "", null);
        boolean result = fSDMsg.hasField("testString");
        assertTrue(result, "result");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testHasField1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        boolean result = fSDMsg.hasField("testFSDMsgFieldName");
        assertFalse(result, "result");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testIsSeparator1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.setSeparator("testFSDMsgSeparatorName", ' ');
        boolean result = fSDMsg.isSeparator((byte) 32);
        assertTrue(result, "result");
    }

    @Test
    public void testPack() throws Throwable {
        Element schema = new FSDMsg("testFSDMsgBasePath").toXML();
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        StringBuilder sb = new StringBuilder();
        fSDMsg.pack(schema, sb);
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        assertEquals("message", schema.getName(), "schema.getName()");
        assertEquals("", sb.toString(), "sb.toString()");
    }

    @Test
    public void testPackThrowsMalformedURLException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        try {
            fSDMsg.pack();
            fail("Expected MalformedURLException to be thrown");
        } catch (MalformedURLException ex) {
            assertEquals(MalformedURLException.class, ex.getClass(), "ex.getClass()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
            assertEquals("testFSDMsgBaseSchema", fSDMsg.baseSchema, "fSDMsg.baseSchema");
        }
    }

    @Test
    public void testPackThrowsNullPointerException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg(null, "testFSDMsgBaseSchema");
        try {
            fSDMsg.pack();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("basePath can not be null", ex.getMessage(), "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
            assertEquals("testFSDMsgBaseSchema", fSDMsg.baseSchema, "fSDMsg.baseSchema");
        }
    }

    @Test
    public void testPackThrowsNullPointerException1() throws Throwable {
        StringBuilder sb = new StringBuilder();
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        try {
            fSDMsg.pack(null, sb);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildren(String)\" because \"schema\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
            assertEquals("", sb.toString(), "sb.toString()");
        }
    }

    @Test
    public void testRead() throws Throwable {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[3]);
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        String result = fSDMsg.read(new InputStreamReader(bais), 1, "", null);
        assertEquals("\u0000", result, "result");
    }

    @Test
    public void testRead1() throws Throwable {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[2]);
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        String result = fSDMsg.read(new InputStreamReader(bais), 0, " ", null);
        assertEquals("", result, "result");
    }

    @Test
    public void testRead2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");

        String result = fSDMsg.read(is, 0, "2C", null);
        assertEquals("", result, "result");
    }

    @Test
    public void testReadField() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[1]);
        String result = fSDMsg.readField(new InputStreamReader(bais), "testFSDMsgFieldName", 1, " ", null);
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        assertEquals("\u0000", result, "result");
    }

    @Test
    public void testReadField1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[1]);
        String result = fSDMsg.readField(new InputStreamReader(bais), "testFSDMsgFieldName", 1, "B", null);
        assertEquals("00", result);
    }

    @Test
    public void testReadField2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");

        String result = fSDMsg.readField(is, "testFSDMsgFieldName", 0, "2C", null);
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        assertEquals("", result, "result");
    }

    @Test
    public void testReadFieldThrowsNullPointerException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.readField(null, "testFSDMsgFieldName", 100, "2C", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.InputStreamReader.read(char[])\" because \"r\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testReadFieldTypeNullLengthToMuchThrowsEOFException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        InputStream is = new ByteArrayInputStream(new byte[1]);
        InputStreamReader r = new InputStreamReader(is);
        try {
            fSDMsg.readField(r, "testFSDMsgFieldName", 100, null, null);
            fail("Expected EOFException to be thrown");
        } catch (EOFException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
            assertEquals(0, is.available(), "(ByteArrayInputStream) is.available()");
        }
    }

    @Test
    public void testReadFieldThrowsRuntimeException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        InputStream is = new ByteArrayInputStream(new byte[0]);
        InputStreamReader r = new InputStreamReader(is);
        try {
            fSDMsg.readField(r, "testFSDMsgFieldName", 100, "3Ch", "Ch");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("Invalid separator 'Ch'", ex.getMessage(),
                    "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
            assertEquals(0, is.available(), "(ByteArrayInputStream) is.available()");
        }
    }

    @Test
    public void testReadThrowsEOFException() throws Throwable {
        assertThrows(EOFException.class, () -> {
            FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
            when(is.read(new char[] { (char) 00 })).thenReturn(Integer.valueOf(1));
            when(is.read(new char[] { (char) 00 })).thenReturn(Integer.valueOf(1));
            when(is.read(new char[] { (char) 00 })).thenReturn(Integer.valueOf(-1));
            fSDMsg.read(is, 100, " ", null);
            fail("Expected EOFException to be thrown");
        });
    }

    @Test
    public void testReadMoreThanInputThrowsEOFException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        InputStream is = new ByteArrayInputStream(new byte[2]);
        InputStreamReader r = new InputStreamReader(is);
        try {
            fSDMsg.read(r, 100, null, null);
            fail("Expected EOFException to be thrown");
        } catch (EOFException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
            assertEquals(0, is.available(), "(ByteArrayInputStream) is.available()");
        }
    }

    @Test
    public void testReadThrowsRuntimeException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        InputStream is = new ByteArrayInputStream(new byte[2]);
        InputStreamReader r = new InputStreamReader(is);
        try {
            fSDMsg.read(r, 100, "3Ch", "Ch");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("Invalid separator 'Ch'", ex.getMessage(),
                    "ex.getMessage()");
            assertEquals(2, is.available(), "(ByteArrayInputStream) is.available()");
        }
    }

    @Test
    public void testSet() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testFSDMsgName", null);
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testSet1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testFSDMsgName", "testFSDMsgValue");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        assertEquals("testFSDMsgValue", fSDMsg.fields.get("testFSDMsgName"), "fSDMsg.fields.get(\"testFSDMsgName\")");
    }

    @Test
    public void testSetHeader() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        byte[] h = new byte[2];
        fSDMsg.setHeader(h);
        assertSame(h, fSDMsg.header, "fSDMsg.header");
    }

    @Test
    public void testSetSeparator() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.setSeparator("testFSDMsgSeparatorName", ' ');
        assertEquals(Character.valueOf(' '), fSDMsg.separators.get("testFSDMsgSeparatorName"),
                "fSDMsg.separators.get(\"testFSDMsgSeparatorName\")");
    }

    @Test
    public void testToXML() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        byte[] h = new byte[2];
        fSDMsg.setHeader(h);
        Element result = fSDMsg.toXML();
        assertEquals("message", result.getName(), "result.getName()");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testToXML1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testFSDMsgName", "testFSDMsgValue");
        Element result = fSDMsg.toXML();
        assertEquals("message", result.getName(), "result.getName()");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testToXML2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] h = new byte[2];
        fSDMsg.setHeader(h);
        fSDMsg.set("testFSDMsgName", "testFSDMsgValue");
        Element result = fSDMsg.toXML();
        assertEquals("message", result.getName(), "result.getName()");
        assertEquals(1, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testToXML3() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        Element result = fSDMsg.toXML();
        assertEquals("message", result.getName(), "result.getName()");
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
    }

    @Test
    public void testToXMLThrowsStringIndexOutOfBoundsException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] h = new byte[0];
        fSDMsg.setHeader(h);
        try {
            fSDMsg.toXML();
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_13)) {
                assertEquals("String index out of range: -2", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("begin 2, end 0, length 0", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        }
    }

    @Test
    public void testUnpack() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        InputStream is = new ByteArrayInputStream(new byte[1]);
        InputStreamReader r = new InputStreamReader(is);
        Element schema = new Element("testFSDMsgName", "testFSDMsgUri");
        fSDMsg.unpack(r, schema);
        assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
        assertEquals(1, is.available(), "(ByteArrayInputStream) is.available()");
        assertEquals("testFSDMsgName", schema.getName(), "schema.getName()");
    }

    @Test
    public void testUnpackThrowsMalformedURLException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] b = new byte[1];
        try {
            fSDMsg.unpack(b);
            fail("Expected MalformedURLException to be thrown");
        } catch (MalformedURLException ex) {
            assertEquals(MalformedURLException.class, ex.getClass(), "ex.getClass()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
            assertEquals("base", fSDMsg.baseSchema, "fSDMsg.baseSchema");
            assertEquals(1, b.length, "b.length");
        }
    }

    @Test
    public void testUnpackThrowsMalformedURLException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        byte[] bytes = new byte[3];
        InputStream is = new ByteArrayInputStream(bytes);
        try {
            fSDMsg.unpack(is);
            fail("Expected MalformedURLException to be thrown");
        } catch (MalformedURLException ex) {
            assertEquals(MalformedURLException.class, ex.getClass(), "ex.getClass()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
            assertEquals("testFSDMsgBaseSchema", fSDMsg.baseSchema, "fSDMsg.baseSchema");
            assertEquals(3, is.available(), "(ByteArrayInputStream) is.available()");
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg(null, "testFSDMsgBaseSchema");
        byte[] b = new byte[1];
        try {
            fSDMsg.unpack(b);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("basePath can not be null", ex.getMessage(), "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
            assertEquals("testFSDMsgBaseSchema", fSDMsg.baseSchema, "fSDMsg.baseSchema");
            assertEquals(1, b.length, "b.length");
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        InputStream is = new ByteArrayInputStream(new byte[0]);
        InputStreamReader r = new InputStreamReader(is);
        try {
            fSDMsg.unpack(r, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildren(String)\" because \"schema\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
            assertEquals(0, is.available(), "(ByteArrayInputStream) is.available()");
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg(null, "testFSDMsgBaseSchema");
        byte[] bytes = new byte[3];
        InputStream is = new ByteArrayInputStream(bytes);
        try {
            fSDMsg.unpack(is);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("basePath can not be null", ex.getMessage(), "ex.getMessage()");
            assertEquals(0, fSDMsg.fields.size(), "fSDMsg.fields.size()");
            assertEquals("testFSDMsgBaseSchema", fSDMsg.baseSchema, "fSDMsg.baseSchema");
            assertEquals(3, is.available(), "(ByteArrayInputStream) is.available()");
        }
    }

    @Test
    public void testUnsetSeparator() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.unsetSeparator("US");
        assertFalse(fSDMsg.separators.containsKey("US"), "fSDMsg.separators.containsKey(\"US\")");
    }

    @Test
    public void testUnsetSeparatorThrowsRuntimeException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.unsetSeparator("testFSDMsgSeparatorName");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("unsetSeparator was attempted for testFSDMsgSeparatorName which was not previously defined.",
                    ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testPackASCIIPadding() throws Throwable {
        Element schema = createSchema();
        appendField(schema, "name", "A", null, 14);
        FSDMsg fSDMsg = new FSDMsg(SCHEMA_PREFIX);

        byte[] expected = "Test message  ".getBytes();
        fSDMsg.set("name", "Test message");
        byte[] b = fSDMsg.packToBytes();
        assertArrayEquals(expected, b, "FSDMsg.packToBytes() don't properly padd ASCII fields");
    }

    @Test
    public void testPackNumericPadding() throws Throwable {
        Element schema = createSchema();
        appendField(schema, "name", "N", null, 6);
        FSDMsg fSDMsg = new FSDMsg(SCHEMA_PREFIX);

        byte[] expected = "001234".getBytes();
        fSDMsg.set("name", "1234");
        byte[] b = fSDMsg.packToBytes();
        assertArrayEquals(expected, b, "FSDMsg.packToBytes() don't properly padd numeric fields");
    }

    @Test
    public void testPackBinaryNoPadding() throws Throwable {
        Element schema = createSchema();
        appendField(schema, "name", "B", null, 14);
        FSDMsg fSDMsg = new FSDMsg(SCHEMA_PREFIX);

        byte[] expected = "Test message".getBytes();
        expected = ISOUtil.concat(new byte[2], expected);
        fSDMsg.set("name", ISOUtil.hexString("Test message".getBytes()));
        byte[] b = fSDMsg.packToBytes();
        assertArrayEquals(expected, b, "FSDMsg.packToBytes() can't padd binary fields");
    }


    @Test
    public void testPackToOldStyleDS() throws Throwable {
        Element schema = createSchema();
        appendField(schema, "name", "ADS", null, 32);
        FSDMsg fSDMsg = new FSDMsg(SCHEMA_PREFIX);

        byte[] expected = "Test message".getBytes();
        fSDMsg.set("name", "Test message");
        byte[] b = fSDMsg.packToBytes();
        assertArrayEquals(expected, b, "FSDMsg.packToBytes() don't properly handle old style DS");
    }

    @Test
    public void testPackToUnpadADS() throws Throwable {
        Element schema = createSchema();
        appendField(schema, "name", "A", "DS", 32);
        FSDMsg fSDMsg = new FSDMsg(SCHEMA_PREFIX);

        byte[] expected = "Test message".getBytes();
        fSDMsg.set("name", "Test message ");
        byte[] b = fSDMsg.packToBytes();
        assertArrayEquals(expected, b, "FSDMsg.packToBytes() don't properly handle ADS unpadding");
    }

    @Test
    public void testPackToNoUnpadBDS() throws Throwable {
        Element schema = createSchema();
        appendField(schema, "name", "B", "DS", 32);
        FSDMsg fSDMsg = new FSDMsg(SCHEMA_PREFIX);

        byte[] expected = "Test message ".getBytes();
        fSDMsg.set("name", ISOUtil.hexString("Test message ".getBytes()));
        byte[] b = fSDMsg.packToBytes();
        assertArrayEquals(expected, b, "FSDMsg.packToBytes() can't unpadding BDS fields");
    }

    @Test
    public void testPackToBDStoLong() throws Throwable {
        Element schema = createSchema();
        appendField(schema, "name", "B", "DS", 8);
        FSDMsg fSDMsg = new FSDMsg(SCHEMA_PREFIX);

        fSDMsg.set("name", ISOUtil.hexString("Test message".getBytes()));
        try {
          fSDMsg.packToBytes();
          fail("FSDMsg.packToBytes() should throw RuntimeException when content is too long");
        } catch (RuntimeException ex) {}
    }

    @Test
    public void testPackToADStoLong() throws Throwable {
        Element schema = createSchema();
        appendField(schema, "name", "A", "DS", 8);
        FSDMsg fSDMsg = new FSDMsg(SCHEMA_PREFIX);

        byte[] expected = "Test mes".getBytes();
        fSDMsg.set("name", "Test message");
        byte[] b = fSDMsg.packToBytes();
        assertArrayEquals(expected, b, "FSDMsg.packToBytes() don't properly truncat ADS field");
    }

    @Test
    public void testPackToBytesCharset() throws Throwable {
        Element schema = createSchema();
        appendField(schema, "name", "A", "DS", 32);
        FSDMsg fSDMsg = new FSDMsg(SCHEMA_PREFIX);

        //bytes represents "Zażółć żółtą gęś" it's a sample in polish
        byte[] expected = ISOUtil.hex2byte("5A61BFF3B3E620BFF3B374B12067EAB6");
        Charset charset = Charset.forName("ISO-8859-2");
        fSDMsg.setCharset(charset);
        fSDMsg.set("name", new String(expected,charset));
        byte[] b = fSDMsg.packToBytes();
//        System.out.println(new String(b, charset));
        assertArrayEquals(expected, b, "FSDMsg.packToBytes() don't properly handle character encodings");

        fSDMsg.unpack(b);
//        System.out.println(fSDMsg.get("name"));
        b = fSDMsg.get("name").getBytes(charset);
        assertArrayEquals(expected, b, "FSDMsg.unpack(b) don't properly handle character encodings");

    }

    @Test
    public void testLLVAR() throws JDOMException, IOException, ISOException {
        Element schema = createSchema();
        appendField(schema, "header", "A", null, 3);
        appendField(schema, "llfield", "LLA", null, 99);
        appendField(schema, "lllfield", "LLLA", null, 999);
        FSDMsg msg = new FSDMsg(SCHEMA_PREFIX);
        msg.set("header", "ISO");
        msg.set("llfield", "ABCDE");
        msg.set("lllfield", "123456789A");
        String packed = msg.pack();
        assertEquals("ISO05ABCDE010123456789A", packed);

        FSDMsg msg1 = new FSDMsg(SCHEMA_PREFIX);
        msg1.unpack(packed.getBytes());
        assertEquals(msg, msg1);
    }
}
