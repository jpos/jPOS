/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.transaction;

import junit.framework.*;
import org.jpos.q2.Q2;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;

public class TransactionManagerTestCase extends TestCase {
    Q2 q2;
    Space sp;
    public static String QUEUE = "TXNMGRTEST";

    public void setUp () throws Exception {
        sp = SpaceFactory.getSpace();
        q2 = new Q2("target/test-classes/org/jpos/transaction");
        q2.start();
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
        q2.stop();
    }
}

