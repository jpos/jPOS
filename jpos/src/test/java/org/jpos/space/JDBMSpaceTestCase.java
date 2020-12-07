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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jpos.iso.ISOUtil;
import org.jpos.util.Profiler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

@SuppressWarnings("unchecked")
public class JDBMSpaceTestCase {
    public static final int COUNT = 100;
    JDBMSpace<String,Object> sp;
    @BeforeEach
    public void setUp (TestInfo testInfo, @TempDir Path filename) {
        sp = (JDBMSpace<String,Object>) JDBMSpace.getSpace (testInfo.getDisplayName(), filename.toString());
        sp.run();
    }
    @AfterEach
    public void tearDown () {
        sp.close();
    }
    @Test
    public void testSimpleOut() throws Exception {
        Object o =Boolean.TRUE;
        sp.out ("testSimpleOut_Key", o);
        Object o1 = sp.in ("testSimpleOut_Key");
        assertTrue (o.equals (o1));
    }
    @Test
    public void testOutRdpInpRdp() throws Exception {
        Object o =  Boolean.TRUE;
        String k = "testOutRdpInpRdp_Key";
        sp.out (k, o);
        assertTrue (o.equals (sp.rdp (k)));
        assertTrue (o.equals (sp.rd  (k)));
        assertTrue (o.equals (sp.rd  (k, 1000)));
        assertTrue (o.equals (sp.inp (k)));
        assertTrue (sp.rdp (k) == null);
        assertTrue (sp.rd  (k, 100) == null);
    }
    @Test
    public void testMultiKeyLoad() throws Exception {
        String s = "The quick brown fox jumped over the lazy dog";
        Profiler prof = new Profiler ();
        for (int i=0; i<COUNT; i++) {
            sp.out ("testMultiKeyLoad_Key" + Integer.toString (i), s);
            if (i % 100 == 0)
                prof.checkPoint ("out " + i);
        }
        // prof.dump (System.err, "MultiKeyLoad out >");
        prof = new Profiler ();
        for (int i=0; i<COUNT; i++) {
            assertTrue (s.equals (sp.in ("testMultiKeyLoad_Key" + Integer.toString (i))));
            if (i % 100 == 0)
                prof.checkPoint ("in " + i);
        }
        // prof.dump (System.err, "MultiKeyLoad in  >");
    }
    @Test
    public void testNoAutoCommit () throws Exception {
        String s = "The quick brown fox jumped over the lazy dog";
        Profiler prof = new Profiler ();
        synchronized (sp) {
            sp.setAutoCommit (false);
            for (int i=0; i<COUNT; i++) {
                sp.out ("testNoAutoCommit_Key" + Integer.toString (i), s);
                if (i % 100 == 0)
                    prof.checkPoint ("out " + i);
            }
            prof.checkPoint ("pre-commit");
            sp.commit();
            sp.setAutoCommit (true);
            prof.checkPoint ("commit");
        }
        // prof.dump (System.err, "NoAutoCommit out >");
        prof = new Profiler ();
        synchronized (sp) {
            sp.setAutoCommit (false);
            for (int i=0; i<COUNT; i++) {
                assertTrue (s.equals (sp.in ("testNoAutoCommit_Key" + Integer.toString (i))));
                if (i % 100 == 0)
                    prof.checkPoint ("in " + i);
            }
            prof.checkPoint ("pre-commit");
            sp.commit();
            sp.setAutoCommit (true);
            prof.checkPoint ("commit");
        }
        // prof.dump (System.err, "NoAutoCommit in  >");
    }
    @Test
    public void testSingleKeyLoad() throws Exception {
        String s = "The quick brown fox jumped over the lazy dog";
        String k = "testSingleKeyLoad_Key";
        Profiler prof = new Profiler ();
        for (int i=0; i<COUNT; i++) {
            sp.out (k, s);
            if (i % 100 == 0)
                prof.checkPoint ("out " + i);
        }
        // prof.dump (System.err, "SingleKeyLoad out >");
        prof = new Profiler ();
        for (int i=0; i<COUNT; i++) {
            assertTrue (s.equals (sp.in (k)));
            if (i % 100 == 0)
                prof.checkPoint ("in " + i);
        }
        // prof.dump (System.err, "SingleKeyLoad in  >");
        assertTrue (sp.rdp (k) == null);
    }
    @Test
    public void testTemplate () throws Exception {
        String key = "TemplateTest_Key";
        sp.out (key, "Value 1");
        sp.out (key, "Value 2");
        sp.out (key, "Value 3");

        String k2r = (String)sp.rdp (new MD5Template (key, "Value 2"));
        assertEquals (k2r, "Value 2");

        String k2i = (String)sp.inp (new MD5Template (key, "Value 2"));
        assertEquals (k2i, "Value 2");
        assertEquals ("Value 1", (String) sp.inp (key));
        assertEquals ("Value 3", (String) sp.inp (key));
    }
    @Test
    public void testPush() {
        sp.push ("PUSH", "ONE");
        sp.push ("PUSH", "TWO");
        sp.push ("PUSH", "THREE");
        sp.out  ("PUSH", "FOUR");
        assertEquals ("THREE", sp.rdp ("PUSH"));
        assertEquals ("THREE", sp.inp ("PUSH"));
        assertEquals ("TWO", sp.inp ("PUSH"));
        assertEquals ("ONE", sp.inp ("PUSH"));
        assertEquals ("FOUR", sp.inp ("PUSH"));
        assertNull (sp.rdp ("PUSH"));
    }
    @Test
    @DisabledIfEnvironmentVariable(named = "GITHUB_ACTIONS", matches = "true")
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
    @DisabledIfEnvironmentVariable(named = "GITHUB_ACTIONS", matches = "true")
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
    public void testExist() {
        sp.out ("KEYA", Boolean.TRUE);
        sp.out ("KEYB", Boolean.TRUE);

        assertTrue (
            sp.existAny(new String[]{"KEYA"}),
            "existAny ([KEYA])"
        );

        assertTrue (
            sp.existAny(new String[]{"KEYB"}),
            "existAny ([KEYB])"
        );
        assertTrue (
            sp.existAny(new String[]{"KEYA", "KEYB"}),
            "existAny ([KEYA,KEYB])"
        );
        assertFalse (
            sp.existAny(new String[]{"KEYC", "KEYD"}),
            "existAny ([KEYC,KEYD])"
        );
    }
    @Test
    public void testExistWithTimeout() {
        assertFalse (
            sp.existAny(new String[]{"KA", "KB"}),
            "existAnyWithTimeout ([KA,KB])"
        );
        assertFalse (
            sp.existAny(new String[]{"KA", "KB"}, 1000L),
            "existAnyWithTimeout ([KA,KB], delay)"
        );
        new Thread() {
            public void run() {
                ISOUtil.sleep (1000L);
                sp.out ("KA", Boolean.TRUE);
            }
        }.start();
        Instant now = Instant.now();
        assertTrue (
            sp.existAny(new String[]{"KA", "KB"}, 2000L),
            "existAnyWithTimeout ([KA,KB], delay)"
        );
        long elapsed = Duration.between(now, Instant.now()).toMillis();
        assertTrue (elapsed > 900L, "delay was > 1000");
        assertNotNull (sp.inp("KA"), "Entry should not be null");
    }
    @Test
    public void testPut () {
       sp.out ("PUT", "ONE");
       sp.out ("PUT", "TWO");
       sp.put ("PUT", "ZERO");
       assertEquals ("ZERO", sp.rdp ("PUT"));
       assertEquals ("ZERO", sp.inp ("PUT"));
       assertNull (sp.rdp ("PUT"));
       }
}

