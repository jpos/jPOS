/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

import org.jpos.q2.Q2;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@SuppressWarnings("unchecked")
public class TransactionManagerTestCase {
    Q2 q2;
    Space sp;
    public static String QUEUE = "TXNMGRTEST";
    public static String QUEUE_EMPTY = "TXNMGRTEST.EMPTY";

    @BeforeEach
    public void setUp (@TempDir Path deployDir) throws Exception {
        sp = SpaceFactory.getSpace("tspace:txnmgrtest");
        Files.copy(Paths.get("build/resources/test/org/jpos/transaction"), deployDir, REPLACE_EXISTING);
        q2 = new Q2(deployDir.toString());
        q2.start();
    }
//    public void testSimpleTransaction() {
//        for (int i=0; i<100; i++) {
//            Context ctx = new Context();
//            ctx.put("volatile", "the quick brown fox");
//            ctx.put("persistent", "jumped over the lazy dog", true);
//            sp.out(QUEUE, ctx);
//        }
//    }
//    public void testRetryTransaction() {
//        Context ctx = new Context();
//        ctx.put ("RETRY", Integer.valueOf(10), true);
//        sp.out (QUEUE, ctx);
//    }
    @Test
    public void testEmptyTM() {
        Context ctx = new Context();
        ctx.put("volatile", "the quick brown empty fox");
        ctx.put("persistent", "jumped over the lazy empty dog", true);
        sp.out(QUEUE_EMPTY, ctx);
    }

    @Test
    public void testFastAbort() {
        Context ctx = new Context();
        ctx.put("volatile", "the quick brown fox");
        ctx.put("persistent", "jumped over the lazy dog", true);
        sp.out(QUEUE, ctx);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Thread.sleep(5000);
        q2.shutdown(true);
    }
}
