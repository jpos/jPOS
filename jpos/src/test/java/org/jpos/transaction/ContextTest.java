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

package org.jpos.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Locale;

import org.jpos.util.LogEvent;
import org.junit.Test;

public class ContextTest {

    @Test
    public void testCheckPoint() throws Throwable {
        new Context().checkPoint("testContextDetail");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testCheckPointNull() throws Throwable {
        new Context().checkPoint(null);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testCheckPointNull1() throws Throwable {
        Context context = new Context();
        context.getLogEvent();
        context.checkPoint(null);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testConstructor() throws Throwable {
        new Context();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump() throws Throwable {
        PrintStream printStream = new PrintStream(new ByteArrayOutputStream(), true);
        Object[] objects = new Object[2];
        printStream.format(Locale.FRANCE, "testContextParam2", objects);
        PrintStream p = new PrintStream(printStream, true, "ISO-8859-1");
        Context context = new Context();
        context.dump(p, "testContextIndent");
    }

    @Test(expected = NullPointerException.class)
    public void testDumpThrowsNullPointerException() throws Throwable {
        Context context = new Context();
        context.dump(null, "testContextIndent");
    }

    @Test
    public void testGet() throws Throwable {
        Context context = new Context();
        context.put("", new Object(), true);
        Object result = context.get(Float.valueOf(-10.0F), -100L);
        assertNull("result", result);
    }

    @Test
    public void testGet1() throws Throwable {
        Context context = new Context();
        context.getProfiler();
        Object result = context.get(Integer.valueOf(2), 100L);
        assertNull("result", result);
    }

    @Test
    public void testGet2() throws Throwable {
        Context context = new Context();
        Object result = context.get("");
        assertNull("result", result);
    }

    @Test
    public void testGet3() throws Throwable {
        Context context = new Context();
        context.getString(new Object());
        Byte defValue = Byte.valueOf((byte) 0);
        Byte result = (Byte) context.get("", defValue);
        assertSame("result", defValue, result);
    }

    @Test
    public void testGet4() throws Throwable {
        Context context = new Context();
        context.getPausedTransaction();
        String result = (String) context.get(Long.valueOf(2L), "1 +");
        assertEquals("result", "1 +", result);
    }

    @Test
    public void testGet5() throws Throwable {
        Context key = new Context();
        key.put("", new Object(), true);
        Boolean defValue = Boolean.TRUE;
        Boolean result = (Boolean) key.get(key, defValue);
        assertSame("result", defValue, result);
    }

    @Test
    public void testGet6() throws Throwable {
        Context context = new Context();
        context.put("", new Object(), true);
        Object result = context.get(new Object(), null);
        assertNull("result", result);
    }

    @Test
    public void testGet7() throws Throwable {
        Context context = new Context();
        context.remove(null);
        Double defValue = Double.valueOf(100.0);
        Double result = (Double) context.get("", defValue);
        assertSame("result", defValue, result);
    }

    @Test
    public void testGet8() throws Throwable {
        Context context = new Context();
        Integer defValue = Integer.valueOf(-1);
        Integer result = (Integer) context.get(new Object(), defValue);
        assertSame("result", defValue, result);
    }

    @Test
    public void testGetLogEvent() throws Throwable {
        LogEvent result = new Context().getLogEvent();
        assertNotNull(result);
    }

    @Test
    public void testGetPausedTransaction() throws Throwable {
        Context context = new Context();
        PausedTransaction result = context.getPausedTransaction();
        assertNull("result", result);
    }

    @Test
    public void testGetProfiler() throws Throwable {
        new Context().getProfiler();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGetProfiler1() throws Throwable {
        Context context = new Context();
        context.getPausedTransaction();
        context.getProfiler();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGetString() throws Throwable {
        Context context = new Context();
        context.getPausedTransaction();
        String result = context.getString(Integer.valueOf(0));
        assertNull("result", result);
    }

    @Test
    public void testGetString1() throws Throwable {
        Context context = new Context();
        String result = context.getString("testString");
        assertNull("result", result);
    }

    @Test
    public void testGetString2() throws Throwable {
        Context context = new Context();
        String result = context.getString("", "");
        assertEquals("result", "", result);
    }

    @Test
    public void testGetString3() throws Throwable {
        Context context = new Context();
        context.getPausedTransaction();
        String result = context.getString("", null);
        assertNull("result", result);
    }

    @Test
    public void testGetStringThrowsClassCastException() throws Throwable {
        Context context = new Context();
        context.getPausedTransaction();
        try {
            context.getString(Integer.valueOf(0), new Object());
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals("ex.getClass()", ClassCastException.class, ex.getClass());
        }
    }

    @Test
    public void testGetThrowsNullPointerException() throws Throwable {
        Context context = new Context();
        try {
            context.get("", 49L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testLog() throws Throwable {
        Context context = new Context();
        context.getPausedTransaction();
        context.log("testString");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testLog1() throws Throwable {
        new Context().log("");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testPut() throws Throwable {
        Context context = new Context();
        context.checkPoint("testContextDetail");
        context.put("", Long.valueOf(-128L), true);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testPut1() throws Throwable {
        new Context().put("", new Object(), true);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testPut2() throws Throwable {
        new Context().put("", "", true);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testPut3() throws Throwable {
        Context context = new Context();
        context.put("", new Object(), true);
        context.put(Integer.valueOf(0), new Object(), true);
    }

    @Test
    public void testPut4() throws Throwable {
        Context context = new Context();
        context.remove(new Object());
        context.put(Integer.valueOf(0), "testString", true);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testPut5() throws Throwable {
        Context context = new Context();
        context.getPausedTransaction();
        context.put(new Object(), "testString", false);
    }

    @Test
    public void testPut6() throws Throwable {
        new Context().put("", "", false);
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testPut7() throws Throwable {
        Context context = new Context();
        context.put("", new Object(), true);
        context.put("testString", new Object());
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testPut8() throws Throwable {
        new Context().put("testString", "");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testReadExternalThrowsNullPointerException() throws Throwable {
        Context context = new Context();
        try {
            context.readExternal(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testRemove() throws Throwable {
        Context context = new Context();
        context.getProfiler();
        Object result = context.remove("testString");
        assertNull("result", result);
    }

    @Test
    public void testRemove1() throws Throwable {
        Context context = new Context();
        Object result = context.remove(Integer.valueOf(33));
        assertNull("result", result);
    }

    @Test
    public void testWriteExternal1() throws Throwable {
        Context context = new Context();
        context.writeExternal(new ObjectOutputStream(new ByteArrayOutputStream()));
    }

    @Test
    public void testWriteExternalThrowsNullPointerException() throws Throwable {
        Context context = new Context();
        try {
            context.writeExternal(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
