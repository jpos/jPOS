/*
 * Copyright (c) 2006 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.transaction;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import org.jpos.iso.ISOUtil;
import junit.framework.*;

public class ContextTestCase extends TestCase {
    public void testExternalizable () throws Exception {
        Context ctx = new Context();
        ctx.put ("volatile", "the quick brown fox");
        ctx.put ("persistent", "jumped over the lazy dog", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream (baos);
        os.writeObject (ctx);

        ByteArrayInputStream bain = new ByteArrayInputStream (baos.toByteArray());
        ObjectInputStream is = new ObjectInputStream (bain);
        Context ctx1 = (Context) is.readObject();

        // ctx.dump (System.out, "ctx> ");
        // ctx1.dump (System.out, "ctx1>");

        assertNull (ctx1.getString ("volatile"));
        assertNotNull (ctx1.getString ("persistent"));
        assertEquals ("jumped over the lazy dog", ctx1.getString ("persistent"));
    }
}

