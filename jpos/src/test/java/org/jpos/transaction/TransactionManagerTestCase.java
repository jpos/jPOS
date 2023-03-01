/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2022 jPOS Software SRL
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

import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.util.Caller;
import org.jpos.util.ThroughputControl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jpos.transaction.ContextConstants.TIMESTAMP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("unchecked")
public class TransactionManagerTestCase {
    private static Q2 q2;
    private static Space sp;
    public static String QUEUE = "TXNMGRTEST";
    public static String QUEUE_EMPTY = "TXNMGRTEST.EMPTY";
    public static String QUEUE_DELAY = "TXNMGRTEST.DELAY";

    @BeforeAll
    public static void setUp (@TempDir Path deployDir) throws IOException {
        sp = SpaceFactory.getSpace("tspace:txnmgrtest");
        Files.walk(Paths.get("build/resources/test/org/jpos/transaction")).forEach( s -> {
            if (Files.isRegularFile(s)) {
                try {
                    Files.copy(s, deployDir.resolve(s.getFileName()), REPLACE_EXISTING);
                } catch (IOException e) {
                    fail();
                }
            }
        });
        q2 = new Q2(deployDir.toString());
        q2.start();
        q2.ready(10000L);
    }
//
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
    public void testTransactionNoDelay() {
        Context ctx = new Context();
        ctx.put("DELAY-0", 50L);
        ctx.put("DELAY-1", 50L);
        sp.out(QUEUE_DELAY, ctx);
        String rc = ctx.get("RC", 5000L);
        assertEquals("00", rc);
    }

    @Test
    public void testTransactionDelay0() {
        Context ctx = new Context();
        ctx.put("DELAY-0", 110L);
        ctx.put("DELAY-1", 20L);
        sp.out(QUEUE_DELAY, ctx);
        String rc = ctx.get("RC", 5000L);
        assertEquals("01", rc);
    }

    @Test
    public void testTransactionDelay1() {
        Context ctx = new Context();
        ctx.put("DELAY-0", 20L);
        ctx.put("DELAY-1", 110L);
        sp.out(QUEUE_DELAY, ctx);
        String rc = ctx.get("RC", 5000L);
        assertEquals("01", rc);
    }

    @Test
    public void testTransactionMaxDelay() {
        Context ctx = new Context();
        ctx.put("DELAY-0", 90L);
        ctx.put("DELAY-1", 90L);
        sp.out(QUEUE_DELAY, ctx);
        String rc = ctx.get("RC", 5000L);
        assertEquals("01", rc);
    }

    @Test
    public void testOKArrival() {
        Context ctx = new Context();
        ctx.put(TIMESTAMP, Instant.now());
        sp.out(QUEUE_DELAY, ctx);
        String rc = ctx.get("RC", 5000L);
        assertEquals("00", rc);
    }

    @Test
    public void testLateArrival() {
        Context ctx = new Context();
        ctx.put(TIMESTAMP, Instant.now().minusSeconds(5L));
        sp.out(QUEUE_DELAY, ctx);
        String rc = ctx.get("RC", 5000L);
        assertEquals("01", rc);
    }

    @Test
    public void testTMMaxTime() {
        Context ctx = new Context();
        ctx.log (Caller.info());
        ctx.put("DELAY-2", 550L);
        sp.out(QUEUE_DELAY, ctx);
        String rc = ctx.get("RC", 5000L);
        assertEquals("01", rc);
    }


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


    @AfterAll
    static void tearDown() throws Exception {
        q2.shutdown(true);
    }
}
