package space;

import org.jpos.util.Profiler;
import org.jpos.space.*;
import junit.framework.TestCase;

public class TestTSpace extends TestCase implements SpaceListener {
    TSpace sp;
    public static final int COUNT = 100000;
    Object notifiedValue = null;

    public void setUp () {
        sp = new TSpace();
    }
    public void testSimpleOut() {
        sp.out ("testSimpleOut_Key", "ABC");
        sp.out ("testSimpleOut_Key", "XYZ");

        assertEquals ("ABC", sp.rdp ("testSimpleOut_Key"));
        assertEquals ("ABC", sp.inp ("testSimpleOut_Key"));
        assertEquals ("XYZ", sp.rdp ("testSimpleOut_Key"));
        assertEquals ("XYZ", sp.inp ("testSimpleOut_Key"));
        assertNull (sp.rdp ("Test"));
        assertNull (sp.inp ("Test"));
    }
    public void testExpiration () {
        sp.out ("testExpiration_Key", "ABC", 50);
        assertEquals ("ABC", sp.rdp ("testExpiration_Key"));
        try {
            Thread.sleep (60);
        } catch (InterruptedException e) { }
        assertNull ("ABC", sp.rdp ("testExpiration_Key"));
    }
    public void testOutRdpInpRdp() throws Exception {
        Object o = new Boolean (true);
        String k = "testOutRdpInpRdp_Key";
        sp.out (k, o);
        assertTrue (o.equals (sp.rdp (k)));
        assertTrue (o.equals (sp.rd  (k)));
        assertTrue (o.equals (sp.rd  (k, 1000)));
        assertTrue (o.equals (sp.inp (k)));
        assertNull (sp.rdp (k));
        assertNull (sp.rd  (k, 100));
    }
    public void testMultiKeyLoad() throws Exception {
        String s = "The quick brown fox jumped over the lazy dog";
        Profiler prof = new Profiler ();
        for (int i=0; i<COUNT; i++) {
            sp.out ("testMultiKeyLoad_Key" + Integer.toString (i), s, 60000);
        }
        prof.dump (System.out, "MultiKeyLoad out >");
        prof = new Profiler ();
        for (int i=0; i<COUNT; i++) {
            assertTrue (s.equals (sp.in ("testMultiKeyLoad_Key" + Integer.toString (i))));
        }
        prof.dump (System.out, "MultiKeyLoad in  >");
    }
    public void testSingleKeyLoad() throws Exception {
        String s = "The quick brown fox jumped over the lazy dog";
        String k = "testSingleKeyLoad_SingleKey";
        Profiler prof = new Profiler ();
        for (int i=0; i<COUNT; i++) {
            sp.out (k, s, 60000);
        }
        prof.dump (System.out, "SingleKeyLoad out >");
        prof = new Profiler ();
        for (int i=0; i<COUNT; i++) {
            assertTrue (s.equals (sp.in (k)));
        }
        prof.dump (System.out, "SingleKeyLoad in  >");

        assertNull (sp.rdp (k));
    }
    public void testGC () throws Exception {
        sp.out ("testGC_Key", "ABC", 50);
        sp.out ("testGC_Key", "XYZ", 50);
        assertEquals ("ABC", sp.rdp ("testGC_Key"));
        try {
            Thread.sleep (60);
        } catch (InterruptedException e) { }

        assertEquals ("testGC_Key", sp.getKeysAsString());
        sp.gc();
        assertEquals ("", sp.getKeysAsString());
        sp.gc();
    }
    public void testTemplate () throws Exception {
        final String KEY = "TestTemplate_Key";
        sp.out (KEY, "123");
        sp.out (KEY, "456"); 
        sp.out (KEY, "789"); 

        Template tmpl = new ObjectTemplate (KEY, "456");
        assertEquals (sp.rdp (KEY),  "123");
        assertEquals (sp.rdp (tmpl), "456");
        assertEquals (sp.rdp (KEY),  "123");
        assertEquals (sp.inp (tmpl), "456");
        assertNull (sp.rdp (tmpl));
        assertNull (sp.inp (tmpl));
        assertEquals (sp.rdp (KEY),  "123");
        assertEquals (sp.inp (KEY),  "123");
        assertEquals (sp.rdp (KEY),  "789");
        assertEquals (sp.inp (KEY),  "789");
        assertNull (sp.rdp (KEY));
        assertNull (sp.inp (KEY));
    }
    public void testMD5Template () throws Exception {
        final String KEY = "TestMD5Template_Key";
        sp.out (KEY, "123");
        sp.out (KEY, "456"); 
        sp.out (KEY, "789"); 

        Template tmpl = new MD5Template (KEY, "456");
        assertEquals (sp.rdp (KEY),  "123");
        assertEquals (sp.rdp (tmpl), "456");
        assertEquals (sp.rdp (KEY),  "123");
        assertEquals (sp.inp (tmpl), "456");
        assertNull (sp.rdp (tmpl));
        assertNull (sp.inp (tmpl));
        assertEquals (sp.rdp (KEY),  "123");
        assertEquals (sp.inp (KEY),  "123");
        assertEquals (sp.rdp (KEY),  "789");
        assertEquals (sp.inp (KEY),  "789");
        assertNull (sp.rdp (KEY));
        assertNull (sp.inp (KEY));
    }
    public void testNotify() {
        sp.addListener ("TestDelayNotify_Key", this, 500);
        sp.out ("TestNotify_Key", "ABCCBA");
        assertNull (notifiedValue);

        sp.addListener ("TestNotify_Key", this);
        sp.out ("TestNotify_Key", "ABCCBA");
        assertEquals (notifiedValue, "ABCCBA");

        sp.out ("TestNotify_Key", "012345");
        assertEquals (notifiedValue, "012345");

        assertEquals (sp.inp ("TestNotify_Key"), "ABCCBA");
        assertEquals (sp.inp ("TestNotify_Key"), "ABCCBA");
        assertEquals (sp.inp ("TestNotify_Key"), "012345");

        sp.out ("TestDelayNotify_Key", "OLD");
        assertEquals (notifiedValue, "OLD");
        try {
            Thread.sleep (600);
        } catch (InterruptedException e) { }
        sp.out ("TestDelayNotify_Key", "NEW");
        assertEquals (notifiedValue, "OLD");  // still OLD
        assertEquals (sp.inp ("TestDelayNotify_Key"), "OLD");
        assertEquals (sp.inp ("TestDelayNotify_Key"), "NEW");
    }
    public void notify (Object key, Object value) {
        this.notifiedValue = value;
    }
}

