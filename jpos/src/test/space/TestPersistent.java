package space;

import junit.framework.*;
import java.io.*;
import java.util.*;
import org.jpos.space.*;
import org.jpos.util.*;

public class TestPersistent extends Test {
    Profiler prof;

    public TestPersistent (String name) {
        super (name);
        prof = new Profiler();
    }
    public LocalSpace getSpace () {
        return new PersistentSpace ();
    }
    public void testOut () throws Exception {
        String buf = new String (new byte [1000]);
        for (int i=0; i<1000; i++) {
            sp.out ("KeyPersist", buf);
        }
        prof.checkPoint ("write 1000 entries");
    }
    public void testIn () throws Exception {
        System.out.println ("");
        String buf = new String (new byte [1000]);
        int i;
        for (i=0; sp.inp ("KeyPersist") != null; i++) 
            ;
        prof.checkPoint (" read " + Integer.toString (i) + " entries");
        prof.dump (System.out, " ");
    }
    public void testLeasedReference () throws Exception {
        // not supported by PersistentSpace
    }
    public void testRenewReference () throws Exception {
        // not supported by PersistentSpace
    }
}

