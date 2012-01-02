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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class SimpleMsgTest {

    @Test
    public void testConstructor() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", true);
        assertEquals("simpleMsg.msgContent", Boolean.TRUE, simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "testSimpleMsgTagName", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "testSimpleMsgMsgName", simpleMsg.msgName);
    }

    @Test
    public void testConstructor1() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", "testString");
        assertEquals("simpleMsg.msgContent", "testString", simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "testSimpleMsgTagName", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "testSimpleMsgMsgName", simpleMsg.msgName);
    }

    @Test
    public void testConstructor2() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", 100L);
        assertEquals("simpleMsg.msgContent", Long.valueOf(100L), simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "testSimpleMsgTagName", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "testSimpleMsgMsgName", simpleMsg.msgName);
    }

    @Test
    public void testConstructor3() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", 100);
        assertEquals("simpleMsg.msgContent", Integer.valueOf(100), simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "testSimpleMsgTagName", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "testSimpleMsgMsgName", simpleMsg.msgName);
    }

    @Test
    public void testConstructor4() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", (short) 100);
        assertEquals("simpleMsg.msgContent", Short.valueOf((short) 100), simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "testSimpleMsgTagName", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "testSimpleMsgMsgName", simpleMsg.msgName);
    }

    @Test
    public void testConstructor5() throws Throwable {
        byte[] msgContent = new byte[0];
        SimpleMsg simpleMsg = new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", msgContent);
        assertEquals("simpleMsg.msgContent", "", simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "testSimpleMsgTagName", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "testSimpleMsgMsgName", simpleMsg.msgName);
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", (byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testDump() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "ISO-8859-1");
        new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName",
                100L)).dump(p, "testSimpleMsgIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump1() throws Throwable {
        SimpleMsg[] msgContent = new SimpleMsg[0];
        SimpleMsg simpleMsg = new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", msgContent);
        SimpleMsg[] msgContent2 = new SimpleMsg[2];
        msgContent2[0] = simpleMsg;
        msgContent2[1] = simpleMsg;
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "ISO-8859-1");
        new SimpleMsg("testSimpleMsgTagName1", "testSimpleMsgMsgName1", msgContent2).dump(p, "testSimpleMsgIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump2() throws Throwable {
        SimpleMsg[] msgContent = new SimpleMsg[0];
        SimpleMsg[] msgContent2 = new SimpleMsg[3];
        msgContent2[0] = new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", "~@%&|K}Id]+l\\");
        msgContent2[1] = new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", (short) 100);
        msgContent2[2] = new SimpleMsg("testSimpleMsgTagName1", "testSimpleMsgMsgName1", msgContent);
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "ISO-8859-1");
        new SimpleMsg("testSimpleMsgTagName2", "testSimpleMsgMsgName2", msgContent2).dump(p, "testSimpleMsgIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump3() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "ISO-8859-1");
        new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", "~@%&|K}Id]+l\\").dump(p, "testSimpleMsgIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump4() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "ISO-8859-1");
        SimpleMsg[] msgContent = new SimpleMsg[0];
        new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", msgContent).dump(p, "testSimpleMsgIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test(expected = NullPointerException.class)
    public void testDumpThrowsNullPointerException() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "ISO-8859-1");
        new SimpleMsg("testSimpleMsgTagName1", "testSimpleMsgMsgName1", new SimpleMsg("testSimpleMsgTagName",
                "testSimpleMsgMsgName", (Object) null)).dump(p, "testSimpleMsgIndent");
    }

    @Test(expected = NullPointerException.class)
    public void testDumpThrowsNullPointerException1() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream());
        SimpleMsg[] msgContent = new SimpleMsg[4];
        msgContent[0] = new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", 100);
        new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", msgContent).dump(p, "testSimpleMsgIndent");
    }

    @Test(expected = NullPointerException.class)
    public void testDumpThrowsNullPointerException2() throws Throwable {
        PrintStream p = new PrintStream(new PrintStream(new ByteArrayOutputStream(), true, "ISO-8859-1"), false, "UTF-16BE");
        new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", (Object) null).dump(p, "testSimpleMsgIndent");
    }

    @Test(expected = NullPointerException.class)
    public void testDumpThrowsNullPointerException4() throws Throwable {
        SimpleMsg[] msgContent = new SimpleMsg[4];
        PrintStream p = new PrintStream(new PrintStream(new ByteArrayOutputStream(), true, "ISO-8859-1"), false, "ISO-8859-1");
        new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", msgContent).dump(p, "testSimpleMsgIndent");
    }

    @Test(expected = NullPointerException.class)
    public void testDumpThrowsNullPointerException5() throws Throwable {
        new SimpleMsg("testSimpleMsgTagName", "testSimpleMsgMsgName", 100L).dump(null, "testSimpleMsgIndent");
    }
}
