package org.jpos.space;

import junit.framework.TestCase;

import org.jpos.util.Profiler;

public class JDBMSpaceTestCase extends TestCase {
    public static final int COUNT = 1000;
    JDBMSpace sp;
    public void setUp () {
        sp = JDBMSpace.getSpace ("space-test");
    }
    public void testSimpleOut() throws Exception {
        Object o = new Boolean (true);
        sp.out ("testSimpleOut_Key", o);
        Object o1 = sp.in ("testSimpleOut_Key");
        assertTrue (o.equals (o1));
    }
    public void testOutRdpInpRdp() throws Exception {
        Object o = new Boolean (true);
        String k = "testOutRdpInpRdp_Key";
        sp.out (k, o);
        assertTrue (o.equals (sp.rdp (k)));
        assertTrue (o.equals (sp.rd  (k)));
        assertTrue (o.equals (sp.rd  (k, 1000)));
        assertTrue (o.equals (sp.inp (k)));
        assertTrue (sp.rdp (k) == null);
        assertTrue (sp.rd  (k, 100) == null);
    }
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
}

