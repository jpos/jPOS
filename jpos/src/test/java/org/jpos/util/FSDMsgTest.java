/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;

import org.jdom.Element;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FSDMsgTest {
    @Mock
    FSDMsg msg;

    @Mock
    ByteArrayInputStream is;

    @Test
    public void testConstructor() throws Throwable {

        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        assertEquals("fSDMsg.basePath", "testFSDMsgBasePath", fSDMsg.basePath);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        assertEquals("fSDMsg.separators.size()", 6, fSDMsg.separators.size());
        assertEquals("fSDMsg.baseSchema", "base", fSDMsg.baseSchema);
    }

    @Test
    public void testConstructor1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        assertEquals("fSDMsg.basePath", "testFSDMsgBasePath", fSDMsg.basePath);
        assertEquals("fSDMsg.separators.size()", 6, fSDMsg.separators.size());
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        assertEquals("fSDMsg.baseSchema", "testFSDMsgBaseSchema", fSDMsg.baseSchema);
    }

    @Test
    public void testCopy() throws Throwable {
        FSDMsg msg = new FSDMsg("testFSDMsgBasePath");
        msg.copy("testFSDMsgFieldName", msg);
        assertEquals("msg.fields.size()", 1, msg.fields.size());
        assertNull("msg.fields.get(\"testFSDMsgFieldName\")", msg.fields.get("testFSDMsgFieldName"));
    }

    @Test
    public void testCopyThrowsNullPointerException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        try {
            fSDMsg.copy("testFSDMsgFieldName", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        }
    }

    @Test
    public void testDump() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.copy("testFSDMsgFieldName", new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema"));
        byte[] h = new byte[3];
        fSDMsg.setHeader(h);
        fSDMsg.dump(new PrintStream(new ByteArrayOutputStream(), true, "UTF-16"), "testFSDMsgIndent");
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
    }

    @Test
    public void testDump1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.copy("testFSDMsgFieldName", new FSDMsg("testFSDMsgBasePath1"));
        fSDMsg.dump(new PrintStream(new ByteArrayOutputStream(), true, "UTF-16"), "testFSDMsgIndent");
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
    }

    @Test
    public void testDump2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        fSDMsg.dump(new PrintStream(new ByteArrayOutputStream(), true, "UTF-16"), "testFSDMsgIndent");
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testDump3() throws Throwable {
        byte[] h = new byte[3];
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        fSDMsg.setHeader(h);
        fSDMsg.dump(new PrintStream(new ByteArrayOutputStream(), true, "UTF-16"), "testFSDMsgIndent");
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.dump(null, "testFSDMsgIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
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
            assertEquals("ex.getMessage()", "String index out of range: -2", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        }
    }

    @Test
    public void testGet() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        String result = fSDMsg.get("testFSDMsgFieldName");
        assertNull("result", result);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testGet10() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        String result = fSDMsg.get("testFSDMsgId", "AD", 100, null, null);
        assertEquals("result",
                "                                                                                                    ", result);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testGet11() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        String result = fSDMsg.get("testFSDMsgId", "Kv", 100, null, null);
        assertEquals("result", "", result);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testGet12() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        String result = fSDMsg.get("testFSDMsgId", "AD", 0, "testFSDMsgDefValue", null);
        assertEquals("result", "", result);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testGet2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        String result = fSDMsg.get("testFSDMsgId", "2C", 100, null, null);
        assertEquals("result", "", result);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testGet3() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.get("testFSDMsgId", "B", 100, "testFSDMsgDefValue", null);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testGet4() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set(" is used, but ", "testFSDMsgValue");
        String result = fSDMsg.get(" is used, but ", "Kv", 100, "testFSDMsgDefValue", null);
        assertEquals("result", "testFSDMsgDefValue", result);
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
    }

    @Test
    public void testGet5() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");

        when(msg.get("testString")).thenReturn("testString");

        fSDMsg.copy("testString", msg);

        String result = fSDMsg.get("testFSDMsgId", "N", 100, "testFSDMsgDefValue", null);
        assertEquals("result",
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000testFSDMsgDefValue", result);
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
    }

    @Test
    public void testGet6() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        String result = fSDMsg.get("testFSDMsgId", " ", 100, "testFSDMsgDefValue", null);
        assertEquals("result", "testFSDMsgDefValue", result);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testGet7() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set(" is used, but ", "testFSDMsgValue");
        String result = fSDMsg.get(" is used, but ", "A", 100, "testFSDMsgDefValue", null);
        assertEquals("result",
                "testFSDMsgValue                                                                                     ", result);
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
    }

    @Test
    public void testGet8() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        String result = fSDMsg.get("testFSDMsgId", "Kv", 100, "testFSDMsgDefValue", null);
        assertEquals("result", "testFSDMsgDefValue", result);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testGet9() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        fSDMsg.set("testString", "testFSDMsgValue");
        String result = fSDMsg.get("testString", "Kv", 100, null, null);
        assertEquals("result", "testFSDMsgValue", result);
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
    }

    @Test
    public void testGetHeader() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        byte[] h = new byte[2];
        fSDMsg.setHeader(h);
        byte[] result = fSDMsg.getHeader();
        assertSame("result", h, result);
        assertEquals("h[0]", (byte) 0, h[0]);
    }

    @Test
    public void testGetHexBytes() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testString", "testFSDMsgValue1");
        byte[] result = fSDMsg.getHexBytes("testString");
        assertEquals("result.length", 8, result.length);
        assertEquals("result[0]", (byte) -2, result[0]);
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
    }

    @Test
    public void testGetHexBytes1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        byte[] result = fSDMsg.getHexBytes("testFSDMsgName");
        assertNull("result", result);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testGetHexBytes2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        byte[] bytes = new byte[1];
        fSDMsg.readField(new ByteArrayInputStream(bytes), "testString", 0, "", null);
        byte[] result = fSDMsg.getHexBytes("testString");
        assertEquals("result.length", 0, result.length);
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
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
        assertEquals("result", "", result);
    }

    @Test
    public void testGetHexHeader1() throws Throwable {
        String result = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema").getHexHeader();
        assertEquals("result", "", result);
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
            assertEquals("ex.getMessage()", "String index out of range: -2", ex.getMessage());
        }
    }

    @Test
    public void testGetInt() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testString", "1");
        int result = fSDMsg.getInt("testString");
        assertEquals("result", 1, result);
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
    }

    @Test
    public void testGetInt1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        int result = fSDMsg.getInt("testFSDMsgName");
        assertEquals("result", 0, result);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetMap() throws Throwable {
        LinkedHashMap result = (LinkedHashMap) new FSDMsg("testFSDMsgBasePath").getMap();
        assertEquals("result.size()", 0, result.size());
    }

    @Test
    public void testGetSchemaThrowsMalformedURLException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        try {
            fSDMsg.getSchema();
            fail("Expected MalformedURLException to be thrown");
        } catch (MalformedURLException ex) {
            assertEquals("ex.getClass()", MalformedURLException.class, ex.getClass());
        }
    }

    @Test
    public void testGetSchemaThrowsMalformedURLException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        try {
            fSDMsg.getSchema("testFSDMsgMessage");
            fail("Expected MalformedURLException to be thrown");
        } catch (MalformedURLException ex) {
            assertEquals("ex.getClass()", MalformedURLException.class, ex.getClass());
        }
    }

    @Test
    public void testGetSchemaThrowsNullPointerException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg(null, "testFSDMsgBaseSchema");
        try {
            fSDMsg.getSchema();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetSchemaThrowsNullPointerException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg(null, "testFSDMsgBaseSchema");
        try {
            fSDMsg.getSchema("testFSDMsgMessage");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
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
            assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
        }
    }

    @Test
    public void testGetThrowsISOException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "NB", 0, "testFSDMsgDefValue", null);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "invalid len 18/0", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
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
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
        }
    }

    @Test
    public void testGetThrowsNullPointerException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        try {
            fSDMsg.get("testFSDMsgId", null, 100, "testFSDMsgDefValue", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        }
    }

    @Test
    public void testGetThrowsRuntimeException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "3Ch", 100, "testFSDMsgDefValue", "3Ch");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("ex.getMessage()", "FSDMsg.isSeparated(String) found that 3Ch has not been defined as a separator!",
                    ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
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
            assertEquals("ex.getMessage()", "FSDMsg.isSeparated(String) found that B]Z has not been defined as a separator!",
                    ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
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
            assertEquals("ex.getMessage()",
                    "field content=testFSDMsgValue is too long to fit in field testString whose length is 0", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
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
            assertEquals("ex.getMessage()", "String index out of range: 0", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
        }
    }

    @Test
    public void testGetThrowsRuntimeException12() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "testFSDMsgType", 100, null, "testFSDMsgType");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("ex.getMessage()",
                    "FSDMsg.isSeparated(String) found that testFSDMsgType has not been defined as a separator!", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
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
            assertEquals("ex.getMessage()",
                    "FSDMsg.isSeparated(String) found that testFSDMsgType has not been defined as a separator!", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
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
                    "ex.getMessage()",
                    "FSDMsg.isSeparated(String) found that Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@ has not been defined as a separator!",
                    ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
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
                    "ex.getMessage()",
                    "FSDMsg.isSeparated(String) found that Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@ has not been defined as a separator!",
                    ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        }
    }

    @Test
    public void testGetThrowsRuntimeException5() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "B]Z", 100, null, "B]Z");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("ex.getMessage()", "FSDMsg.isSeparated(String) found that B]Z has not been defined as a separator!",
                    ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        }
    }

    @Test
    public void testGetThrowsRuntimeException6() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "B]Z", 100, "testFSDMsgDefValue", "B]Z");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("ex.getMessage()", "FSDMsg.isSeparated(String) found that B]Z has not been defined as a separator!",
                    ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        }
    }

    @Test
    public void testGetThrowsRuntimeException7() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "B]Z", 0, "testFSDMsgDefValue", "B]Z");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("ex.getMessage()",
                    "field content=testFSDMsgDefValue is too long to fit in field testFSDMsgId whose length is 0", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
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
                    "ex.getMessage()",
                    "FSDMsg.isSeparated(String) found that Ka`xc-3DywniD\"+9W\"Uh/mY~23E0(V)P_^sv )@ has not been defined as a separator!",
                    ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        }
    }

    @Test
    public void testGetThrowsRuntimeException9() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "", 100, null, null);
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("ex.getMessage()", "String index out of range: 0", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        }
    }

    @Test
    public void testGetThrowsStringIndexOutOfBoundsException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.get("testFSDMsgId", "", 100, "testFSDMsgDefValue", null);
            fail("Expected StringIndexOutOfBoundsException to be thrown");
        } catch (StringIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "String index out of range: 0", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        }
    }

    @Test
    public void testHasField() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        fSDMsg.readField(null, "testString", 0, "", null);
        boolean result = fSDMsg.hasField("testString");
        assertTrue("result", result);
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
    }

    @Test
    public void testHasField1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        boolean result = fSDMsg.hasField("testFSDMsgFieldName");
        assertFalse("result", result);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testIsSeparator1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.setSeparator("testFSDMsgSeparatorName", ' ');
        boolean result = fSDMsg.isSeparator((byte) 32);
        assertTrue("result", result);
    }

    @Test
    public void testPack() throws Throwable {
        Element schema = new FSDMsg("testFSDMsgBasePath").toXML();
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        StringBuffer sb = new StringBuffer();
        fSDMsg.pack(schema, sb);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        assertEquals("schema.getName()", "message", schema.getName());
        assertEquals("sb.toString()", "", sb.toString());
    }

    @Test
    public void testPackThrowsMalformedURLException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        try {
            fSDMsg.pack();
            fail("Expected MalformedURLException to be thrown");
        } catch (MalformedURLException ex) {
            assertEquals("ex.getClass()", MalformedURLException.class, ex.getClass());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
            assertEquals("fSDMsg.baseSchema", "testFSDMsgBaseSchema", fSDMsg.baseSchema);
        }
    }

    @Test
    public void testPackThrowsNullPointerException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg(null, "testFSDMsgBaseSchema");
        try {
            fSDMsg.pack();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
            assertEquals("fSDMsg.baseSchema", "testFSDMsgBaseSchema", fSDMsg.baseSchema);
        }
    }

    @Test
    public void testPackThrowsNullPointerException1() throws Throwable {
        StringBuffer sb = new StringBuffer();
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        try {
            fSDMsg.pack(null, sb);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
            assertEquals("sb.toString()", "", sb.toString());
        }
    }

    @Test
    public void testRead() throws Throwable {
        byte[] bytes = new byte[3];
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        String result = fSDMsg.read(new ByteArrayInputStream(bytes), 1, "", null);
        assertEquals("result", "\u0000", result);
    }

    @Test
    public void testRead1() throws Throwable {
        byte[] bytes = new byte[2];
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        String result = fSDMsg.read(new ByteArrayInputStream(bytes), 0, " ", null);
        assertEquals("result", "", result);
    }

    @Test
    public void testRead2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");

        String result = fSDMsg.read(is, 0, "2C", null);
        assertEquals("result", "", result);
    }

    @Test
    public void testReadField() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] bytes = new byte[1];
        String result = fSDMsg.readField(new ByteArrayInputStream(bytes), "testFSDMsgFieldName", 1, " ", null);
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
        assertEquals("result", "\u0000", result);
    }

    @Test
    public void testReadField1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] bytes = new byte[1];
        String result = fSDMsg.readField(new ByteArrayInputStream(bytes), "testFSDMsgFieldName", 1, "B", null);
        assertEquals("00", result);
    }

    @Test
    public void testReadField2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");

        String result = fSDMsg.readField(is, "testFSDMsgFieldName", 0, "2C", null);
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
        assertEquals("result", "", result);
    }

    @Test
    public void testReadFieldThrowsNullPointerException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.readField(null, "testFSDMsgFieldName", 100, "2C", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        }
    }

    @Test
    public void testReadFieldTypeNullLengthToMuchThrowsEOFException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] bytes = new byte[1];
        InputStream is = new ByteArrayInputStream(bytes);
        try {
            fSDMsg.readField(is, "testFSDMsgFieldName", 100, null, null);
            fail("Expected EOFException to be thrown");
        } catch (EOFException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
            assertEquals("(ByteArrayInputStream) is.available()", 0, is.available());
        }
    }

    @Test
    public void testReadFieldThrowsRuntimeException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] bytes = new byte[0];
        InputStream is = new ByteArrayInputStream(bytes);
        try {
            fSDMsg.readField(is, "testFSDMsgFieldName", 100, "3Ch", "Ch");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("ex.getMessage()", "FSDMsg.isSeparated(String) found that Ch has not been defined as a separator!",
                    ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
            assertEquals("(ByteArrayInputStream) is.available()", 0, is.available());
        }
    }

    @Test
    public void testReadThrowsEOFException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        when(is.read(new byte[] { (byte) 00 })).thenReturn(Integer.valueOf(1));
        when(is.read(new byte[] { (byte) 00 })).thenReturn(Integer.valueOf(1));
        when(is.read(new byte[] { (byte) 00 })).thenReturn(Integer.valueOf(-1));
        try {
            fSDMsg.read(is, 100, " ", null);
            fail("Expected EOFException to be thrown");
        } catch (EOFException ex) {
            assertEquals("ex.getClass()", EOFException.class, ex.getClass());
        }
    }

    @Test
    public void testReadMoreThanInputThrowsEOFException() throws Throwable {
        byte[] bytes = new byte[2];
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        InputStream is = new ByteArrayInputStream(bytes);
        try {
            fSDMsg.read(is, 100, null, null);
            fail("Expected EOFException to be thrown");
        } catch (EOFException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("(ByteArrayInputStream) is.available()", 0, is.available());
        }
    }

    @Test
    public void testReadThrowsRuntimeException() throws Throwable {
        byte[] bytes = new byte[2];
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        InputStream is = new ByteArrayInputStream(bytes);
        try {
            fSDMsg.read(is, 100, "3Ch", "Ch");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("ex.getMessage()", "FSDMsg.isSeparated(String) found that Ch has not been defined as a separator!",
                    ex.getMessage());
            assertEquals("(ByteArrayInputStream) is.available()", 2, is.available());
        }
    }

    @Test
    public void testSet() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testFSDMsgName", null);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testSet1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testFSDMsgName", "testFSDMsgValue");
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
        assertEquals("fSDMsg.fields.get(\"testFSDMsgName\")", "testFSDMsgValue", fSDMsg.fields.get("testFSDMsgName"));
    }

    @Test
    public void testSetHeader() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        byte[] h = new byte[2];
        fSDMsg.setHeader(h);
        assertSame("fSDMsg.header", h, fSDMsg.header);
    }

    @Test
    public void testSetSeparator() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.setSeparator("testFSDMsgSeparatorName", ' ');
        assertEquals("fSDMsg.separators.get(\"testFSDMsgSeparatorName\")", Character.valueOf(' '),
                fSDMsg.separators.get("testFSDMsgSeparatorName"));
    }

    @Test
    public void testToXML() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath", "testFSDMsgBaseSchema");
        byte[] h = new byte[2];
        fSDMsg.setHeader(h);
        Element result = fSDMsg.toXML();
        assertEquals("result.getName()", "message", result.getName());
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
    }

    @Test
    public void testToXML1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.set("testFSDMsgName", "testFSDMsgValue");
        Element result = fSDMsg.toXML();
        assertEquals("result.getName()", "message", result.getName());
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
    }

    @Test
    public void testToXML2() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] h = new byte[2];
        fSDMsg.setHeader(h);
        fSDMsg.set("testFSDMsgName", "testFSDMsgValue");
        Element result = fSDMsg.toXML();
        assertEquals("result.getName()", "message", result.getName());
        assertEquals("fSDMsg.fields.size()", 1, fSDMsg.fields.size());
    }

    @Test
    public void testToXML3() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        Element result = fSDMsg.toXML();
        assertEquals("result.getName()", "message", result.getName());
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
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
            assertEquals("ex.getMessage()", "String index out of range: -2", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        }
    }

    @Test
    public void testUnpack() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] bytes = new byte[1];
        InputStream is = new ByteArrayInputStream(bytes);
        Element schema = new Element("testFSDMsgName", "testFSDMsgUri");
        fSDMsg.unpack(is, schema);
        assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
        assertEquals("(ByteArrayInputStream) is.available()", 1, is.available());
        assertEquals("schema.getName()", "testFSDMsgName", schema.getName());
    }

    @Test
    public void testUnpackThrowsMalformedURLException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] b = new byte[1];
        try {
            fSDMsg.unpack(b);
            fail("Expected MalformedURLException to be thrown");
        } catch (MalformedURLException ex) {
            assertEquals("ex.getClass()", MalformedURLException.class, ex.getClass());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
            assertEquals("fSDMsg.baseSchema", "base", fSDMsg.baseSchema);
            assertEquals("b.length", 1, b.length);
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
            assertEquals("ex.getClass()", MalformedURLException.class, ex.getClass());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
            assertEquals("fSDMsg.baseSchema", "testFSDMsgBaseSchema", fSDMsg.baseSchema);
            assertEquals("(ByteArrayInputStream) is.available()", 3, is.available());
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
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
            assertEquals("fSDMsg.baseSchema", "testFSDMsgBaseSchema", fSDMsg.baseSchema);
            assertEquals("b.length", 1, b.length);
        }
    }

    @Test
    public void testUnpackThrowsNullPointerException1() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        byte[] bytes = new byte[0];
        InputStream is = new ByteArrayInputStream(bytes);
        try {
            fSDMsg.unpack(is, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
            assertEquals("(ByteArrayInputStream) is.available()", 0, is.available());
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
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("fSDMsg.fields.size()", 0, fSDMsg.fields.size());
            assertEquals("fSDMsg.baseSchema", "testFSDMsgBaseSchema", fSDMsg.baseSchema);
            assertEquals("(ByteArrayInputStream) is.available()", 3, is.available());
        }
    }

    @Test
    public void testUnsetSeparator() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        fSDMsg.unsetSeparator("US");
        assertFalse("fSDMsg.separators.containsKey(\"US\")", fSDMsg.separators.containsKey("US"));
    }

    @Test
    public void testUnsetSeparatorThrowsRuntimeException() throws Throwable {
        FSDMsg fSDMsg = new FSDMsg("testFSDMsgBasePath");
        try {
            fSDMsg.unsetSeparator("testFSDMsgSeparatorName");
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException ex) {
            assertEquals("ex.getMessage()",
                    "unsetSeparator was attempted for testFSDMsgSeparatorName which was not previously defined.", ex.getMessage());
        }
    }
}
