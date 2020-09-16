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

package org.jpos.space;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.AbstractSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class TSpaceTest {

    @Test
    public void testConstructor() throws Throwable {
        TSpace tSpace = new TSpace();
        assertEquals(0, tSpace.entries.size(), "tSpace.entries.size()");
    }

    @Test
    public void testDump() throws Throwable {
        TSpace tSpace = new TSpace();
        tSpace.out("", "testString");
        tSpace.out(Integer.valueOf(0), Integer.valueOf(-1));
        tSpace.out(Integer.valueOf(31), "");
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true);
        tSpace.dump(p, "testTSpaceIndent");
        assertEquals(3, tSpace.entries.size(), "tSpace.entries.size()");
    }

    @Test
    public void testExpirableCompareTo() throws Throwable {
        int result = new TSpace.Expirable(Integer.valueOf(0), 1L).compareTo(new TSpace.Expirable(new Object(), 0L));
        assertEquals(1, result, "result");
    }

    @Test
    public void testExpirableCompareTo1() throws Throwable {
        TSpace.Expirable obj = new TSpace.Expirable(new Object(), 0L);
        int result = new TSpace.Expirable(new Object(), 0L).compareTo(obj);
        assertEquals(0, result, "result");
    }

    @Test
    public void testExpirableCompareTo2() throws Throwable {
        int result = new TSpace.Expirable(null, 0L).compareTo(new TSpace.Expirable(new Object(), 1L));
        assertEquals(-1, result, "result");
    }

    @Test
    public void testExpirableCompareToThrowsNullPointerException() throws Throwable {
        try {
            new TSpace.Expirable(new Object(), 100L).compareTo(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read field \"expires\" because \"other\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testExpirableConstructor() throws Throwable {
        Object value = new Object();
        TSpace.Expirable expirable = new TSpace.Expirable(value, 100L);
        assertEquals(100L, expirable.expires, "expirable.expires");
        assertSame(value, expirable.value, "expirable.value");
    }

    @Test
    public void testExpirableGetValue() throws Throwable {
        String result = (String) new TSpace.Expirable("", 9184833384926L).getValue();
        assertEquals("", result, "result");
    }

    @Test
    public void testExpirableGetValue1() throws Throwable {
        Object result = new TSpace.Expirable(null, 9184833384926L).getValue();
        assertNull(result, "result");
    }

    @Test
    public void testExpirableGetValue2() throws Throwable {
        Object result = new TSpace.Expirable(new Object(), 100L).getValue();
        assertNull(result, "result");
    }

    @Test
    public void testExpirableIsExpired() throws Throwable {
        boolean result = new TSpace.Expirable("", 9184833384926L).isExpired();
        assertFalse(result, "result");
    }

    @Test
    public void testExpirableIsExpired1() throws Throwable {
        boolean result = new TSpace.Expirable(new Object(), 100L).isExpired();
        assertTrue(result, "result");
    }

    @Test
    public void testExpirableToString() throws Throwable {
        new TSpace.Expirable(";\"i", 100L).toString();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testExpirableToStringThrowsNullPointerException() throws Throwable {
        try {
            new TSpace.Expirable(null, 100L).toString();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"Object.toString()\" because \"this.value\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGc() throws Throwable {

        TSpace tSpace = new TSpace();
        tSpace.gc();
        assertEquals(0, tSpace.entries.size(), "tSpace.entries.size()");
    }

    @Test
    public void testGetKeysAsString() throws Throwable {
        TSpace tSpace = new TSpace();
        String result = tSpace.getKeysAsString();
        assertEquals("", result, "result");
        assertEquals(0, tSpace.entries.size(), "tSpace.entries.size()");
    }

    @Test
    public void testGetKeySet() throws Throwable {
        TSpace tSpace = new TSpace();
        AbstractSet result = (AbstractSet) tSpace.getKeySet();
        assertEquals(0, result.size(), "result.size()");
        assertEquals(0, tSpace.entries.size(), "tSpace.entries.size()");
    }

    @Test
    public void testIn() throws Throwable {
        TSpace tSpace = new TSpace();
        Object result = tSpace.in(Long.valueOf(0L), 1L);
        assertNull(result, "result");
        assertEquals(0, tSpace.entries.size(), "tSpace.entries.size()");
    }

    @Test
    public void testInp() throws Throwable {
        TSpace tSpace = new TSpace();
        MD5Template key = new MD5Template("", "");
        Object result = tSpace.inp(key);
        assertNull(result, "result");
        assertEquals(0, tSpace.entries.size(), "tSpace.entries.size()");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInp1() throws Throwable {
        TSpace tSpace = new TSpace();
        tSpace.out("", "testString");
        tSpace.out(Integer.valueOf(0), Integer.valueOf(-1));
        tSpace.out(Integer.valueOf(31), Integer.valueOf(-32), 100L);
        tSpace.inp(new MD5Template("", (Object) null));
        tSpace.out("", "", 1000L);
        String result = (String) tSpace.inp("");
        assertEquals(1, ((List) tSpace.entries.get("")).size(), "tSpace.entries.get(\"\").size()");
        assertFalse("testString".equals(((List) tSpace.entries.get("")).get(0)),
                "tSpace.entries.get(\"\").get(0) had \"testString\" removed");
        assertEquals("testString", result, "result");
        assertEquals(3, tSpace.entries.size(), "tSpace.entries.size()");
    }

    @Test
    public void testInp2() throws Throwable {
        TSpace tSpace = new TSpace();
        Object result = tSpace.inp(null);
        assertNull(result, "result");
        assertEquals(0, tSpace.entries.size(), "tSpace.entries.size()");
    }

    @Test
    public void testIsEmpty() throws Throwable {
        TSpace tSpace = new TSpace();
        boolean result = tSpace.isEmpty();
        assertTrue(result, "result");
        assertEquals(0, tSpace.entries.size(), "tSpace.entries.size()");
    }

    @Test
    public void testOut() throws Throwable {
        TSpace tSpace = new TSpace();
        tSpace.out(Integer.valueOf(0), "testString", 100L);
        assertEquals(1, tSpace.entries.size(), "tSpace.entries.size()");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNotifyReaders() {
        final Space sp = new TSpace();
        final AtomicInteger ai = new AtomicInteger(10);
        for (int i=0; i<10; i++) {
            new Thread() {
                public void run() {
                    if (sp.rd("TEST", 5000L) != null)
                        ai.decrementAndGet();
                }
            }.start();
        }
        sp.out("TEST", Boolean.TRUE);
        ISOUtil.sleep(500L);
        assertTrue(ai.get() == 0, "Counter should be zero");
    }
}
