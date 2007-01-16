/*
 * Copyright (c) 2006 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.transaction;

import junit.framework.*;
import org.jpos.q2.Q2;
import org.jpos.iso.ISOUtil;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;

public class TransactionManagerTestCase extends TestCase {
    Q2 q2;
    Space sp;
    public static String QUEUE = "TXNMGRTEST";

    public void setUp () throws Exception {
        sp = SpaceFactory.getSpace();
        q2 = new Q2(new String[] { "-d", "../test/org/jpos/transaction" });
        new Thread() {
            public void run() {
                try {
                    q2.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public void testSimpleTransaction() {
        Context ctx = new Context();
        ctx.put ("volatile", "the quick brown fox");
        ctx.put ("persistent", "jumped over the lazy dog", true);
        sp.out (QUEUE, ctx);
    }
    public void testRetryTransaction() {
        Context ctx = new Context();
        ctx.put ("RETRY", new Integer(10), true);
        sp.out (QUEUE, ctx);
    }
    public void tearDown() throws Exception {
        Thread.sleep (3000); // let the thing run
        q2.shutdown();
        Thread.sleep (3000); // let the thing actually shutdown
    }
}

