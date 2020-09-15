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

package org.jpos.transaction;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ConcurrentModificationException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.jpos.util.LogEvent;
import org.jpos.util.Serializer;
import org.junit.jupiter.api.Test;

public class ContextTest {

    @Test
    public void testCheckPoint() throws Throwable {
        new Context().checkPoint("testContextDetail");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testCheckPointNull() throws Throwable {
        new Context().checkPoint(null);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testCheckPointNull1() throws Throwable {
        Context context = new Context();
        context.getLogEvent();
        context.checkPoint(null);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testConstructor() throws Throwable {
        new Context();
        assertTrue(true, "Test completed without Exception");
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

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        assertThrows(NullPointerException.class, () -> {
            Context context = new Context();
            context.dump(null, "testContextIndent");
        });
    }

    @Test
    public void testGet() throws Throwable {
        Context context = new Context();
        context.put("", new Object(), true);
        Object result = context.get(Float.valueOf(-10.0F), -100L);
        assertNull(result, "result");
    }

    @Test
    public void testGet1() throws Throwable {
        Context context = new Context();
        context.getProfiler();
        Object result = context.get(Integer.valueOf(2), 100L);
        assertNull(result, "result");
    }

    @Test
    public void testGet2() throws Throwable {
        Context context = new Context();
        Object result = context.get("");
        assertNull(result, "result");
    }

    @Test
    public void testGet7() throws Throwable {
        Context context = new Context();
        context.remove(null);
        Double defValue = Double.valueOf(100.0);
        Double result = (Double) context.get("", defValue);
        assertSame(defValue, result, "result");
    }

    @Test
    public void testGet8() throws Throwable {
        Context context = new Context();
        Integer defValue = -1;
        Integer result = (Integer) context.get("", defValue);
        assertSame(defValue, result, "result");
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
        assertNull(result, "result");
    }

    @Test
    public void testGetProfiler() throws Throwable {
        new Context().getProfiler();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testGetProfiler1() throws Throwable {
        Context context = new Context();
        context.getPausedTransaction();
        context.getProfiler();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testGetString1() throws Throwable {
        Context context = new Context();
        String result = context.getString("testString");
        assertNull(result, "result");
    }

    @Test
    public void testGetString2() throws Throwable {
        Context context = new Context();
        String result = context.getString("", "");
        assertEquals("", result, "result");
    }

    @Test
    public void testGetString3() throws Throwable {
        Context context = new Context();
        context.getPausedTransaction();
        String result = context.getString("", null);
        assertNull(result, "result");
    }

    @Test
    public void testGetThrowsNullPointerException() throws Throwable {
        Context context = new Context();
        try {
            context.get("", 49L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Map.get(Object)\" because \"this.map\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testLog() throws Throwable {
        Context context = new Context();
        context.getPausedTransaction();
        context.log("testString");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testLog1() throws Throwable {
        new Context().log("");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testPut() throws Throwable {
        Context context = new Context();
        context.checkPoint("testContextDetail");
        context.put("", Long.valueOf(-128L), true);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testPut1() throws Throwable {
        new Context().put("", new Object(), true);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testPut2() throws Throwable {
        new Context().put("", "", true);
        assertTrue(true, "Test completed without Exception");
    }


    @Test
    public void testPut6() throws Throwable {
        new Context().put("", "", false);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testPut7() throws Throwable {
        Context context = new Context();
        context.put("", new Object(), true);
        context.put("testString", new Object());
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testPut8() throws Throwable {
        new Context().put("testString", "");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testReadExternalThrowsNullPointerException() throws Throwable {
        Context context = new Context();
        try {
            context.readExternal(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.ObjectInput.readByte()\" because \"in\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testRemove() throws Throwable {
        Context context = new Context();
        context.getProfiler();
        Object result = context.remove("testString");
        assertNull(result, "result");
    }

    @Test
    public void testRemove1() throws Throwable {
        Context context = new Context();
        Object result = context.remove(Integer.valueOf(33));
        assertNull(result, "result");
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.ObjectOutput.writeByte(int)\" because \"out\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testPersist() throws IOException, ClassNotFoundException {
        Context context = new Context();
        context.put ("A", "ABC");
        context.put ("B", "BCD");
        context.persist("A");
        context.persist("B");
        Context deser = Serializer.serializeDeserialize(context);
        assertEquals(deser.get("A"), "ABC");
        assertEquals(deser.get("B"), "BCD");
        deser.evict("A");
        deser = Serializer.serializeDeserialize(deser);
        assertEquals(deser.get("B"), "BCD");
        assertNull(deser.get("A"), "A should be null");
    }

    @Test
    public void testClone() throws IOException, ClassNotFoundException {
        Context context = new Context();
        context.put ("A", "ABC", true);
        context.put ("B", "BCD");
        Context cloned = context.clone();
        assertEquals(cloned.get("A"), "ABC");
        assertEquals(cloned.get("B"), "BCD");
        assertEquals(context, cloned);

        context = Serializer.serializeDeserialize(cloned);
        assertEquals(context.get("A"), "ABC");
        assertNull(context.get("B"), "A should be null");
    }

    @Test
    public void testConcurrentException() throws InterruptedException {
        final Context ctx = new Context();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger errorsPut = new AtomicInteger();
        AtomicInteger errorsDump = new AtomicInteger();
        Runnable addEvent = () -> {
            for (int i=0; i<10000; i++) {
                try {
                    ctx.put("Prop-" + i, i);
                } catch (ConcurrentModificationException e) {
                    errorsPut.incrementAndGet();
                }
                Thread.yield();
            }
            latch.countDown();
        };

        Runnable newFrozen = () -> {
            while (latch.getCount() > 0) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    ctx.dumpMap(new PrintStream(baos), "");
                } catch (ConcurrentModificationException e) {
                    errorsDump.incrementAndGet();
                }
                Thread.yield();
            }
        };
        new Thread(addEvent).start();
        Thread t1 = new Thread(newFrozen);
        t1.start();
        t1.join();
        if (errorsPut.get() + errorsDump.get() > 0)
            fail ("Concurrent Exception has been raised " + errorsPut.get() + "/" + errorsDump.get() + " time(s)");
    }
}
