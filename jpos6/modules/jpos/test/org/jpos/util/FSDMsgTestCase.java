package org.jpos.util;

import org.jpos.iso.ISOUtil;
import java.util.Arrays;
import junit.framework.*;

public class FSDMsgTestCase extends TestCase {
    FSDMsg msg;

    public void setUp() throws Exception {
        msg = new FSDMsg ("file:../test/org/jpos/util/msg-");
    }
    public void testLeadingBlanks () throws Exception {
        msg.set ("testafs", "   123");
        assertEquals ("Leading blanks",
            ISOUtil.hex2byte ("2020203132331C"),
            msg.pack().getBytes()
        );
    }
    public void testTraillingBlanks () throws Exception {
        msg.set ("testafs", "123   ");
        assertEquals ("Trailing blanks",
            ISOUtil.hex2byte ("3132331C"),
            msg.pack().getBytes()
        );
    }
    public void testMixedBlanks () throws Exception {
        msg.set ("testafs", "  123 ");
        assertEquals ("Mixed blanks",
            ISOUtil.hex2byte ("20203132331C"),
            msg.pack().getBytes()
        );
    }
    public void testFinalField() throws Exception {
        msg.set ("testafs", "  123 ");
        msg.set ("finalfield", "ABC");
        assertEquals ("Final Field",
            ISOUtil.hex2byte ("20203132331C414243"),
            msg.pack().getBytes()
        );

        FSDMsg m = new FSDMsg ("file:../test/org/jpos/util/msg-");
        m.unpack (ISOUtil.hex2byte ("20203132331C414243"));
        assertEquals ("Final Field",
            ISOUtil.hex2byte ("20203132331C414243"),
            m.pack().getBytes()
        );
    }
    public void assertEquals (String msg, byte[] b1, byte[] b2) {
        assertTrue (msg, Arrays.equals (b1, b2));
    }
}

