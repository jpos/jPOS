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

import java.util.HashSet;
import java.util.Set;

import org.jpos.iso.ISOUtil;
import org.jpos.util.Profiler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class TSpaceTestCase implements SpaceListener {
    TSpace<String, Object> sp;
    public static final int COUNT = 100000;
    Object notifiedValue = null;

    @BeforeEach
    public void setUp() {
        sp = new TSpace<String, Object>();
    }

    @AfterEach
    public void tearDown() {
        Set keySet = new HashSet(sp.getKeySet());
        for (Object key : keySet) {
            sp.inp(key);
        }
        sp.gc();
        sp = null;
    }

    @Test
    public void testSimpleOut() {
        sp.out("testSimpleOut_Key", "ABC");
        sp.out("testSimpleOut_Key", "XYZ");
        assertEquals("ABC", sp.rdp("testSimpleOut_Key"));
        assertEquals("ABC", sp.inp("testSimpleOut_Key"));
        assertEquals("XYZ", sp.rdp("testSimpleOut_Key"));
        assertEquals("XYZ", sp.inp("testSimpleOut_Key"));
        assertNull(sp.rdp("Test"));
        assertNull(sp.inp("Test"));
    }

    @Test
    public void testNullEntry() {
        try {
            sp.out("testNull", null);
            fail("NullPointerException should have been called");
        } catch (NullPointerException e) {
            assertNull(sp.rdp("testNull"), "Verify null entry (rdp)");
            assertNull(sp.inp("testNull"), "Verify null entry (inp)");
        }
    }

    @Test
    public void testExpiration() {
        sp.out("testExpiration_Key", "ABC", 50);
        assertEquals("ABC", sp.rdp("testExpiration_Key"));
        try {
            Thread.sleep(60);
        } catch (InterruptedException e) {
        }
        assertNull(sp.rdp("testExpiration_Key"), "ABC");
    }

    @Test
    public void testOutRdpInpRdp() throws Exception {
        Object o = Boolean.TRUE;
        String k = "testOutRdpInpRdp_Key";
        sp.out(k, o);
        assertTrue(o.equals(sp.rdp(k)));
        assertTrue(o.equals(sp.rd(k)));
        assertTrue(o.equals(sp.rd(k, 1000)));
        assertTrue(o.equals(sp.inp(k)));
        assertNull(sp.rdp(k));
        assertNull(sp.rd(k, 100));
    }

    @Test
    public void testMultiKeyLoad() throws Exception {
        String s = "The quick brown fox jumped over the lazy dog";
        Profiler prof = new Profiler();
        for (int i = 0; i < COUNT; i++) {
            sp.out("testMultiKeyLoad_Key" + Integer.toString(i), s, 60000);
        }
        // prof.dump (System.err, "MultiKeyLoad out >");
        prof = new Profiler();
        for (int i = 0; i < COUNT; i++) {
            assertTrue(s.equals(sp.in("testMultiKeyLoad_Key" + Integer.toString(i))));
        }
        // prof.dump (System.err, "MultiKeyLoad in  >");
    }

    @Test
    public void testSingleKeyLoad() throws Exception {
        String s = "The quick brown fox jumped over the lazy dog";
        String k = "testSingleKeyLoad_SingleKey";
        Profiler prof = new Profiler();
        for (int i = 0; i < COUNT; i++) {
            sp.out(k, s, 60000);
        }
        // prof.dump (System.err, "SingleKeyLoad out >");
        prof = new Profiler();
        for (int i = 0; i < COUNT; i++) {
            assertTrue(s.equals(sp.in(k)));
        }
        // prof.dump (System.err, "SingleKeyLoad in  >");
        assertNull(sp.rdp(k));
    }

    @Test
    public void testGC() throws Exception {
        sp.out("testGC_Key", "ABC", 50);
        sp.out("testGC_Key", "XYZ", 50);
        assertEquals("ABC", sp.rdp("testGC_Key"));
        try {
            Thread.sleep(60);
        } catch (InterruptedException e) {
        }
        assertEquals("testGC_Key", sp.getKeysAsString());
        sp.gc();
        assertEquals("", sp.getKeysAsString());
        sp.gc();
    }

    @Test
    public void testTemplate() throws Exception {
        final String KEY = "TestTemplate_Key";
        sp.out(KEY, "123");
        sp.out(KEY, "456");
        sp.out(KEY, "789");
        Template tmpl = new ObjectTemplate(KEY, "456");
        assertEquals(sp.rdp(KEY), "123");
        assertEquals(sp.rdp(tmpl), "456");
        assertEquals(sp.rdp(KEY), "123");
        assertEquals(sp.inp(tmpl), "456");
        assertNull(sp.rdp(tmpl));
        assertNull(sp.inp(tmpl));
        assertEquals(sp.rdp(KEY), "123");
        assertEquals(sp.inp(KEY), "123");
        assertEquals(sp.rdp(KEY), "789");
        assertEquals(sp.inp(KEY), "789");
        assertNull(sp.rdp(KEY));
        assertNull(sp.inp(KEY));
    }

    @Test
    public void testMD5Template() throws Exception {
        final String KEY = "TestMD5Template_Key";
        sp.out(KEY, "123");
        sp.out(KEY, "456");
        sp.out(KEY, "789");
        Template tmpl = new MD5Template(KEY, "456");
        assertEquals(sp.rdp(KEY), "123");
        assertEquals(sp.rdp(tmpl), "456");
        assertEquals(sp.rdp(KEY), "123");
        assertEquals(sp.inp(tmpl), "456");
        assertNull(sp.rdp(tmpl));
        assertNull(sp.inp(tmpl));
        assertEquals(sp.rdp(KEY), "123");
        assertEquals(sp.inp(KEY), "123");
        assertEquals(sp.rdp(KEY), "789");
        assertEquals(sp.inp(KEY), "789");
        assertNull(sp.rdp(KEY));
        assertNull(sp.inp(KEY));
    }

    @Test
    public void testNotify() {
        sp.addListener("TestDelayNotify_Key", this, 500);
        sp.out("TestNotify_Key", "ABCCBA");
        assertNull(notifiedValue);
        sp.addListener("TestNotify_Key", this);
        sp.out("TestNotify_Key", "ABCCBA");
        assertEquals(notifiedValue, "ABCCBA");
        sp.out("TestNotify_Key", "012345");
        assertEquals(notifiedValue, "012345");
        assertEquals(sp.inp("TestNotify_Key"), "ABCCBA");
        assertEquals(sp.inp("TestNotify_Key"), "ABCCBA");
        assertEquals(sp.inp("TestNotify_Key"), "012345");
        sp.out("TestDelayNotify_Key", "OLD");
        assertEquals(notifiedValue, "OLD");
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
        }
        sp.out("TestDelayNotify_Key", "NEW");
        assertEquals(notifiedValue, "OLD"); // still OLD
        assertEquals(sp.inp("TestDelayNotify_Key"), "OLD");
        assertEquals(sp.inp("TestDelayNotify_Key"), "NEW");
    }

    @Test
    public void testPush() {
        sp.push("PUSH", "ONE");
        sp.push("PUSH", "TWO");
        sp.push("PUSH", "THREE");
        sp.out("PUSH", "FOUR");
        assertEquals("THREE", sp.rdp("PUSH"));
        assertEquals("THREE", sp.inp("PUSH"));
        assertEquals("TWO", sp.inp("PUSH"));
        assertEquals("ONE", sp.inp("PUSH"));
        assertEquals("FOUR", sp.inp("PUSH"));
        assertNull(sp.rdp("PUSH"));
    }

    @Test
    public void testOutExpire() {
        sp.out ("OUT", "ONE", 1000L);
        sp.out ("OUT", "TWO", 2000L);
        sp.out ("OUT", "THREE", 3000L);
        sp.out  ("OUT", "FOUR", 4000L);
        assertEquals ("ONE", sp.rdp ("OUT"));
        ISOUtil.sleep (1500L);
        assertEquals ("TWO", sp.rdp ("OUT"));
        ISOUtil.sleep (1000L);
        assertEquals ("THREE", sp.rdp ("OUT"));
        assertEquals ("THREE", sp.inp ("OUT"));
        assertEquals ("FOUR", sp.inp ("OUT"));
        assertNull (sp.rdp ("OUT"));
    }

    @Test
    public void testPushExpire() {
        sp.push ("PUSH", "FOUR", 4000L);
        sp.push ("PUSH", "THREE", 3000L);
        sp.push ("PUSH", "TWO", 2000L);
        sp.push  ("PUSH", "ONE", 1000L);
        assertEquals ("ONE", sp.rdp ("PUSH"));
        ISOUtil.sleep (1500L);
        assertEquals ("TWO", sp.rdp ("PUSH"));
        ISOUtil.sleep (1000L);
        assertEquals ("THREE", sp.rdp ("PUSH"));
        assertEquals ("THREE", sp.inp ("PUSH"));
        assertEquals ("FOUR", sp.inp ("PUSH"));
        assertNull (sp.rdp ("PUSH"));
    }

    @Test
    public void testPut() {
        sp.out("PUT", "ONE");
        sp.out("PUT", "TWO");
        sp.put("PUT", "ZERO");
        assertEquals("ZERO", sp.rdp("PUT"));
        assertEquals("ZERO", sp.inp("PUT"));
        assertNull(sp.rdp("PUT"));
    }

    @Test
    public void testExist() {
        sp.out("KEYA", Boolean.TRUE);
        sp.out("KEYB", Boolean.TRUE);
        assertTrue(sp.existAny(new String[] { "KEYA" }), "existAny ([KEYA])");
        assertTrue(sp.existAny(new String[] { "KEYB" }), "existAny ([KEYB])");
        assertTrue(sp.existAny(new String[] { "KEYA", "KEYB" }), "existAny ([KEYA,KEYB])");
        assertFalse(sp.existAny(new String[] { "KEYC", "KEYD" }), "existAny ([KEYC,KEYD])");
    }

    @Test
    public void testExistWithTimeout() {
        assertFalse(sp.existAny(new String[] { "KA", "KB" }), "existAnyWithTimeout ([KA,KB])");
        assertFalse(sp.existAny(new String[] { "KA", "KB" }, 1000L), "existAnyWithTimeout ([KA,KB], delay)");
        new Thread() {
            public void run() {
                ISOUtil.sleep(1000L);
                sp.out("KA", Boolean.TRUE);
            }
        }.start();
        long now = System.currentTimeMillis();
        assertTrue(sp.existAny(new String[] { "KA", "KB" }, 2000L), "existAnyWithTimeout ([KA,KB], delay)");
        long elapsed = System.currentTimeMillis() - now;
        assertTrue(elapsed > 900L, "delay was > 1000");
    }

    @Test
    public void testNRD() {
        long now  = System.currentTimeMillis();
        sp.out("NRD", "NRDTEST", 1000L);
        sp.nrd("NRD");
        long elapsed = System.currentTimeMillis() - now;
        assertTrue(elapsed >= 1000L, "Invalid elapsed time " + elapsed);
    }
    @Test
    public void testNRDWithDelay() {
        long now  = System.currentTimeMillis();
        sp.out("NRD", "NRDTEST", 1000L);
        Object obj = sp.nrd("NRD", 500L);
        assertNotNull(obj, "Object should not be null");
        obj = sp.nrd("NRD", 5000L);
        long elapsed = System.currentTimeMillis() - now;
        assertTrue(elapsed >= 1000L && elapsed <= 2000L, "Invalid elapsed time " + elapsed);
        assertNull(obj, "Object should be null");
    }
    public void notify(Object key, Object value) {
        this.notifiedValue = value;
    }
}
