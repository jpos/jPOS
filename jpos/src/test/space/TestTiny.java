package space;

import junit.framework.*;
import java.io.*;
import java.util.*;
import org.jpos.space.*;

public class TestTiny extends TestCase {
    Space sp;
    String rx;

    public TestTiny (String name) {
        super (name);
        sp = new TinySpace ();
    }
    public void testSimpleOut() throws Exception {
        Object o = new Boolean (true);

        sp.out ("Key1", o);
        Object o1 = sp.in ("Key1");

        assertTrue (o.equals (o1));
    }
    public void testRenewReference () throws Exception {
        final String KEY = "TestRenew";
        LeasedReference ref = new LeasedReference ("TEST", 100);
        sp.out (KEY, ref);
        assertTrue (sp.rdp (KEY) != null);
        ref.renew (200);
        assertTrue (sp.rdp (KEY) != null);
        Thread.sleep (100);
        assertTrue (sp.rdp (KEY) != null);
        Thread.sleep (200);
        assertTrue (sp.rdp (KEY) == null);
    }

    public void testLeasedReference () throws Exception {
        Object o = new Boolean (true);

        sp.out ("Key1", new LeasedReference (o, 100));
        Object o1 = sp.in ("Key1");
        assertTrue (o.equals (o1));

        sp.out ("Key1", new LeasedReference (o, 100));
        o1 = sp.rdp ("Key1");
        assertTrue (o1 != null);
        Thread.sleep (50);
        o1 = sp.rdp ("Key1");
        assertTrue (o1 != null);
        Thread.sleep (200);
        o1 = sp.rdp ("Key1");
        assertTrue (o1 == null);
    }
}

