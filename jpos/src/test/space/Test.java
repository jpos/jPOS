    package space;

    import junit.framework.*;
    import java.io.*;
    import java.util.*;
    import org.jpos.space.*;

    public class Test extends TestCase implements SpaceListener {
        LocalSpace sp;
        String rx;

        public Test (String name) {
            super (name);
            sp = getSpace ();
        }
        public LocalSpace getSpace () {
            return new TransientSpace ();
        }
        public void testSimpleOut() throws Exception {
            Object o = new Boolean (true);

            sp.out ("Key1", o);
            Object o1 = sp.in ("Key1");

            assertTrue (o.equals (o1));
        }
        public void notify (Object key, Object value) {
            rx = (String) sp.in (key);
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
        public void testListener () throws Exception {
            sp.addListener ("Key2", this);
            sp.out ("Key2", "Test");
            assertTrue (sp.inp ("Key2") == null);
            assertTrue ("Test".equals (rx));
        }
        public void testDefaultSpace () throws Exception {
            Space sp = TransientSpace.getSpace ();
            assertTrue (sp != null);
            TransientSpace.getSpace ("OtherSpace");
            TransientSpace.getSpace ("OtherSpace");
            Object obj = sp.rdp ("jpos:space/OtherSpace");
            assertTrue (obj != null);
            assertTrue (obj instanceof TransientSpace);
        }
    }

