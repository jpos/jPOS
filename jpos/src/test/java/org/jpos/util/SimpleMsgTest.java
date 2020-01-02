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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

public class SimpleMsgTest {

    PrintStream p;
    ByteArrayOutputStream os;
    private static final String NL = System.getProperty("line.separator");

    @BeforeEach
    public void setUp() {
      os = new ByteArrayOutputStream();
      p = new PrintStream(os);
    }

    @Test
    public void testConstructor() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", true);
        assertEquals(Boolean.TRUE, simpleMsg.msgContent, "simpleMsg.msgContent");
        assertEquals("tag", simpleMsg.tagName, "simpleMsg.tagName");
        assertEquals("Some Name", simpleMsg.msgName, "simpleMsg.msgName");
    }

    @Test
    public void testConstructor1() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", "testString");
        assertEquals("testString", simpleMsg.msgContent, "simpleMsg.msgContent");
        assertEquals("tag", simpleMsg.tagName, "simpleMsg.tagName");
        assertEquals("Some Name", simpleMsg.msgName, "simpleMsg.msgName");
    }

    @Test
    public void testConstructor2() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", 100L);
        assertEquals(100L, simpleMsg.msgContent, "simpleMsg.msgContent");
        assertEquals("tag", simpleMsg.tagName, "simpleMsg.tagName");
        assertEquals("Some Name", simpleMsg.msgName, "simpleMsg.msgName");
    }

    @Test
    public void testConstructor3() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", 100);
        assertEquals(100, simpleMsg.msgContent, "simpleMsg.msgContent");
        assertEquals("tag", simpleMsg.tagName, "simpleMsg.tagName");
        assertEquals("Some Name", simpleMsg.msgName, "simpleMsg.msgName");
    }

    @Test
    public void testConstructor4() throws Throwable {
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", (short) 100);
        assertEquals((short) 100, simpleMsg.msgContent, "simpleMsg.msgContent");
        assertEquals("tag", simpleMsg.tagName, "simpleMsg.tagName");
        assertEquals("Some Name", simpleMsg.msgName, "simpleMsg.msgName");
    }

    @Test
    public void testConstructor5() throws Throwable {
        byte[] msgContent = new byte[0];
        SimpleMsg simpleMsg = new SimpleMsg("tag", "Some Name", msgContent);
        assertEquals("", simpleMsg.msgContent, "simpleMsg.msgContent");
        assertEquals("tag", simpleMsg.tagName, "simpleMsg.tagName");
        assertEquals("Some Name", simpleMsg.msgName, "simpleMsg.msgName");
    }

    @Test
    public void testDump() throws Throwable {
        new SimpleMsg("tag", "Some Name", new SimpleMsg("inner-tag", "Inner Name",
                100L)).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  <inner-tag name=\"Inner Name\">" + NL +
                      "--||--    100" + NL +
                      "--||--  </inner-tag>" + NL +
                      "--||--</tag>" + NL
                     ,os.toString());
    }

    @Test
    public void testDump1() throws Throwable {
        SimpleMsg[] msgContent = new SimpleMsg[0];
        SimpleMsg simpleMsg = new SimpleMsg("inner-tag", "Inner Name", msgContent);
        SimpleMsg[] msgContent2 = new SimpleMsg[2];
        msgContent2[0] = simpleMsg;
        msgContent2[1] = simpleMsg;
        new SimpleMsg("tag", "Some Name", msgContent2).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  <inner-tag name=\"Inner Name\">" + NL +
                      "--||--  </inner-tag>" + NL +
                      "--||--  <inner-tag name=\"Inner Name\">" + NL +
                      "--||--  </inner-tag>" + NL +
                      "--||--</tag>" + NL
                     ,os.toString());
    }

    @Test
    public void testDump2() throws Throwable {
        SimpleMsg[] msgContent = new SimpleMsg[0];
        SimpleMsg[] msgContent2 = new SimpleMsg[3];
        msgContent2[0] = new SimpleMsg("inner-tag1", "Inner Name1", "~@%&|K}Id]+l\\");
        msgContent2[1] = new SimpleMsg("inner-tag2", "Inner Name2", (short) 100);
        msgContent2[2] = new SimpleMsg("inner-tag3", "Inner Name3", msgContent);
        new SimpleMsg("tag", "Some Name", msgContent2).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  <inner-tag1 name=\"Inner Name1\">" + NL +
                      "--||--    ~@%&|K}Id]+l\\" + NL +
                      "--||--  </inner-tag1>" + NL +
                      "--||--  <inner-tag2 name=\"Inner Name2\">" + NL +
                      "--||--    100" + NL +
                      "--||--  </inner-tag2>" + NL +
                      "--||--  <inner-tag3 name=\"Inner Name3\">" + NL +
                      "--||--  </inner-tag3>" + NL +
                      "--||--</tag>" +  NL
                      ,os.toString());
    }

    @Test
    public void testDump3() throws Throwable {
        new SimpleMsg("tag", "Some Name", "~@%&|K}Id]+l\\").dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  ~@%&|K}Id]+l\\" + NL +
                      "--||--</tag>" +  NL
                      ,os.toString());
    }

    @Test
    public void testDump4() throws Throwable {
        SimpleMsg[] msgContent = new SimpleMsg[0];
        new SimpleMsg("tag", "Some Name", msgContent).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--</tag>" +  NL
                      ,os.toString());
    }

    @Test
    public void testDumpContentNull() throws Throwable {
        new SimpleMsg("tag", "Some Name", null).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\"/>" + NL
                      ,os.toString());
    }

    @Test
    public void testDumpContentNullByteArr() throws Throwable {
        new SimpleMsg("tag", "Some Name", null).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\"/>" + NL
                      ,os.toString());
    }

    @Test
    public void testDumpContentNullWithoutName() throws Throwable {
        new SimpleMsg("tag", null).dump(p, "--||--");
        assertEquals( "--||--<tag/>" + NL
                      ,os.toString());
    }

    @Test
    public void testDumpContentWithoutName() throws Throwable {
        new SimpleMsg("tag", 100).dump(p, "--||--");
        assertEquals( "--||--<tag>100</tag>" + NL
                      ,os.toString());
    }

    @Test
    public void testDumpContentByteArr() throws Throwable {
        byte[] b = ISOUtil.hex2byte("3AF1");
        new SimpleMsg("tag", "Some Name", b).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  3AF1" + NL +
                      "--||--</tag>" + NL
                      ,os.toString());
    }

    @Test
    public void testDumpContentByteArrWithoutName() throws Throwable {
        byte[] b = ISOUtil.hex2byte("f13a");
        new SimpleMsg("tag", b).dump(p, "--||--");
        assertEquals( "--||--<tag>F13A</tag>" + NL
                      ,os.toString());
    }

    @Test
    public void testDumpContentBoolean() throws Throwable {
        new SimpleMsg("tag", "Some Name", true).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  true" + NL +
                      "--||--</tag>" + NL
                      ,os.toString());
    }

    @Test
    public void testDumpContentShort() throws Throwable {
        new SimpleMsg("tag", "Some Name", (short)123).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  123" + NL +
                      "--||--</tag>" + NL
                      ,os.toString());
    }

    @Test
    public void testDumpContentLong() throws Throwable {
        new SimpleMsg("tag", "Some Name", -123L).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  -123" + NL +
                      "--||--</tag>" + NL
                      ,os.toString());
    }

    @Test
    public void testDumpContentDouble() throws Throwable {
        new SimpleMsg("tag", "Some Name", -12.3).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  -12.3" + NL +
                      "--||--</tag>" + NL
                      ,os.toString());
    }

    @Test
    public void testDumpContentDoubleWithoutName() throws Throwable {
        new SimpleMsg("tag",  -12.3).dump(p, "--||--");
        assertEquals( "--||--<tag>-12.3</tag>" + NL
                      ,os.toString());
    }

    @Test
    public void testDumpInnerNull() throws Throwable {
        new SimpleMsg("tag", "Some Name", new SimpleMsg("inner-tag",
                "Inner Name", null)).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  <inner-tag name=\"Inner Name\"/>" + NL +
                      "--||--</tag>" +  NL
                      ,os.toString());
    }

    @Test
    public void testDumpInnerNulls() throws Throwable {
        SimpleMsg[] msgContent = new SimpleMsg[4];
        new SimpleMsg("tag", "Some Name", msgContent).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  null" + NL +
                      "--||--  null" + NL +
                      "--||--  null" + NL +
                      "--||--  null" + NL +
                      "--||--</tag>" +  NL
                      ,os.toString());
    }

    @Test
    public void testDumpExceptionStackTrace() {
        Exception ex = new IllegalArgumentException("This value is illegal"
                , new IllegalStateException("Some illegal state")
        );
        Loggeable msg = new SimpleMsg("unexpected-exception", ex);

        msg.dump(p, "--||--");
        String res = os.toString();

        assertThat(res, Matchers.startsWith("--||--<unexpected-exception>" + NL));
        assertThat(res, Matchers.endsWith("--||--</unexpected-exception>" + NL));

        assertThat(res, Matchers.containsString(NL + "--||--    java.lang.IllegalArgumentException: This value is illegal" + NL));
        assertThat(res, Matchers.containsString(NL + "--||--    Caused by: java.lang.IllegalStateException: Some illegal state" + NL));
        assertThat(res, Matchers.containsString(NL + "--||--            at org.jpos.util.SimpleMsgTest.test"));
    }

    @Test
    public void testDumpInnerCompositeAndNulls() throws Throwable {
        List<Loggeable> msgContent = new ArrayList<>();
        msgContent.add(new SimpleMsg("inner-tag", "Inner Name", 100));
        new SimpleMsg("tag", "Some Name", msgContent).dump(p, "--||--");
        assertEquals( "--||--<tag name=\"Some Name\">" + NL +
                      "--||--  <inner-tag name=\"Inner Name\">" + NL +
                      "--||--    100" + NL +
                      "--||--  </inner-tag>" + NL +
                      "--||--</tag>" +  NL
                      ,os.toString());
    }

    @Test
    public void testDumpPrintStreamNull() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            new SimpleMsg("tag", "Some Name", 100L).dump(null, "--||--");
        });
    }
}
