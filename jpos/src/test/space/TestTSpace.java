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
        sp.out ("Test", "ABC");
        sp.out ("Test", "XYZ");

        assertEquals ("ABC", sp.rdp ("Test"));
        assertEquals ("ABC", sp.inp ("Test"));
        assertEquals ("XYZ", sp.rdp ("Test"));
        assertEquals ("XYZ", sp.inp ("Test"));
        assertNull (sp.rdp ("Test"));
        assertNull (sp.inp ("Test"));
    }
    public void testExpiration () {
        sp.out ("TestExp", "ABC", 50);
        assertEquals ("ABC", sp.rdp ("TestExp"));
        try {
            Thread.sleep (60);
        } catch (InterruptedException e) { }
        assertNull ("ABC", sp.rdp ("TestExp"));
    }
    public void testOutRdpInpRdp() throws Exception {
        Object o = new Boolean (true);
        String k = "key";
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
            sp.out ("Key" + Integer.toString (i), s, 60000);
        }
        prof.dump (System.out, "MultiKeyLoad out >");
        prof = new Profiler ();
        for (int i=0; i<COUNT; i++) {
            assertTrue (s.equals (sp.in ("Key" + Integer.toString (i))));
        }
        prof.dump (System.out, "MultiKeyLoad in  >");
    }
    public void testSingleKeyLoad() throws Exception {
        String s = "The quick brown fox jumped over the lazy dog";
        String k = "SingleKey";
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
        sp.out ("TestExp", "ABC", 50);
        sp.out ("TestExp", "XYZ", 50);
        assertEquals ("ABC", sp.rdp ("TestExp"));
        try {
            Thread.sleep (60);
        } catch (InterruptedException e) { }

        assertEquals ("TestExp", sp.getKeysAsString());
        sp.gc();
        assertEquals ("", sp.getKeysAsString());
        sp.gc();
    }
    public void testTemplate () throws Exception {
        final String KEY = "TestTemplate";
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
        final String KEY = "TestMD5Template";
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
        sp.addListener ("Test", this, 500);
        sp.out ("TestNotify", "ABCCBA");
        assertNull (notifiedValue);

        sp.addListener ("TestNotify", this);
        sp.out ("TestNotify", "ABCCBA");
        assertEquals (notifiedValue, "ABCCBA");

        sp.out ("TestNotify", "012345");
        assertEquals (notifiedValue, "012345");

        assertEquals (sp.inp ("TestNotify"), "ABCCBA");
        assertEquals (sp.inp ("TestNotify"), "ABCCBA");
        assertEquals (sp.inp ("TestNotify"), "012345");

        sp.out ("Test", "OLD");
        assertEquals (notifiedValue, "OLD");
        try {
            Thread.sleep (600);
        } catch (InterruptedException e) { }
        sp.out ("Test", "NEW");
        assertEquals (notifiedValue, "OLD");  // still OLD
        assertEquals (sp.inp ("Test"), "OLD");
        assertEquals (sp.inp ("Test"), "NEW");
    }
    public void notify (Object key, Object value) {
        this.notifiedValue = value;
    }
}

