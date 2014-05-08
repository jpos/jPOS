/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2014 Alejandro P. Revilla
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import org.jpos.iso.ISOUtil;
import org.junit.Before;

import org.junit.Test;

public class SimpleMsgTest {

    PrintStream p;
    ByteArrayOutputStream os;
    private static final String NL = System.getProperty("line.separator");

    @Before
    public void setUp() {
      os = new ByteArrayOutputStream();
      try {
        p = new PrintStream(os, true, ISOUtil.ENCODING);
      } catch (UnsupportedEncodingException ex) {
        fail(ex.getMessage());
      }
    }

    @Test
    public void testConstructor() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", true);
        assertEquals("simpleMsg.msgContent", Boolean.TRUE, simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "tag", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "Some Name", simpleMsg.msgName);
    }

    @Test
    public void testConstructor1() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", "testString");
        assertEquals("simpleMsg.msgContent", "testString", simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "tag", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "Some Name", simpleMsg.msgName);
    }

    @Test
    public void testConstructor2() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", 100L);
        assertEquals("simpleMsg.msgContent", 100L, simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "tag", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "Some Name", simpleMsg.msgName);
    }

    @Test
    public void testConstructor3() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", 100);
        assertEquals("simpleMsg.msgContent", 100, simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "tag", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "Some Name", simpleMsg.msgName);
    }

    @Test
    public void testConstructor4() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", (short) 100);
        assertEquals("simpleMsg.msgContent", (short) 100, simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "tag", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "Some Name", simpleMsg.msgName);
    }

    @Test
    public void testConstructor5() throws Throwable {
        byte[] msgContent = new byte[0];
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", msgContent);
        assertEquals("simpleMsg.msgContent", "", simpleMsg.msgContent);
        assertEquals("simpleMsg.tagName", "tag", simpleMsg.tagName);
        assertEquals("simpleMsg.msgName", "Some Name", simpleMsg.msgName);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorThrowsNullPointerException() throws Throwable {
        new SimpleMsg("tag", "Some Name", (byte[]) null);
    }

    @Test
    public void testDump() throws Throwable {
        new SimpleMsg("tag", "Some Name", new SimpleMsg("inner-tag", "Inner Name",
                100L)).dump(p, "--||--");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump1() throws Throwable {
        SimpleMsg[] msgContent = new SimpleMsg[0];
        SimpleMsg simpleMsg = new SimpleMsg("inner-tag", "Inner Name", msgContent);
        SimpleMsg[] msgContent2 = new SimpleMsg[2];
        msgContent2[0] = simpleMsg;
        msgContent2[1] = simpleMsg;
        new SimpleMsg("tag", "Some Name", msgContent2).dump(p, "--||--");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump2() throws Throwable {
        SimpleMsg[] msgContent = new SimpleMsg[0];
        SimpleMsg[] msgContent2 = new SimpleMsg[3];
        msgContent2[0] = new SimpleMsg("inner-tag1", "Inner Name1", "~@%&|K}Id]+l\\");
        msgContent2[1] = new SimpleMsg("inner-tag2", "Inner Name2", (short) 100);
        msgContent2[2] = new SimpleMsg("inner-tag3", "Inner Name3", msgContent);
        new SimpleMsg("tag", "Some Name", msgContent2).dump(p, "--||--");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump3() throws Throwable {
        new SimpleMsg("tag", "Some Name", "~@%&|K}Id]+l\\").dump(p, "--||--");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump4() throws Throwable {
        SimpleMsg[] msgContent = new SimpleMsg[0];
        new SimpleMsg("tag", "Some Name", msgContent).dump(p, "--||--");
        assertTrue("Test completed without Exception", true);
    }

    @Test(expected = NullPointerException.class)
    public void testDumpThrowsNullPointerException() throws Throwable {
        new SimpleMsg("tag", "Some Name", new SimpleMsg("inner-tag",
                "Inner Name", (Object) null)).dump(p, "--||--");
    }

    @Test(expected = NullPointerException.class)
    public void testDumpThrowsNullPointerException1() throws Throwable {
        PrintStream ps = new PrintStream(new ByteArrayOutputStream());
        SimpleMsg[] msgContent = new SimpleMsg[4];
        msgContent[0] = new SimpleMsg("inner-tag", "Inner Name", 100);
        new SimpleMsg("tag", "Some Name", msgContent).dump(ps, "--||--");
    }

    @Test(expected = NullPointerException.class)
    public void testDumpThrowsNullPointerException2() throws Throwable {
        PrintStream ps = new PrintStream(p, false, "UTF-16BE");
        new SimpleMsg("tag", "Some Name", (Object) null).dump(ps, "--||--");
    }

    @Test(expected = NullPointerException.class)
    public void testDumpThrowsNullPointerException4() throws Throwable {
        SimpleMsg[] msgContent = new SimpleMsg[4];
        PrintStream ps = new PrintStream(p, false, ISOUtil.ENCODING);
        new SimpleMsg("tag", "Some Name", msgContent).dump(ps, "--||--");
    }

    @Test(expected = NullPointerException.class)
    public void testDumpThrowsNullPointerException5() throws Throwable {
        new SimpleMsg("tag", "Some Name", 100L).dump(null, "--||--");
    }
}
