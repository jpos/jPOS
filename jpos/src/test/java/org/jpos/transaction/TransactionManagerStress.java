/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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
import org.jpos.space.LocalSpace;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.util.Chronometer;
import org.jpos.util.NameRegistrar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("unchecked")
public class TransactionManagerStress {
    private static Q2 q2;
    private static Space sp;
    private static Context ctx;
    public static String QUEUE_STRESS = "TXNMGRTEST.STRESS";
    private static Chronometer chronometer = new Chronometer();

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
        Random r = new Random();
        for (int i=0; i<100_000; i++) {
            ctx = new Context();
            ctx.put("DELAY-0", 100L + r.nextInt(4900));
            // ctx.put("DELAY-1", 400L + r.nextInt(50));
            ctx.log ("size=" + ((LocalSpace)sp).size(QUEUE_STRESS));
            sp.out(QUEUE_STRESS, ctx);
        }
        q2 = new Q2(deployDir.toString());
        q2.start();
        q2.ready(10000L);
    }

    @Test
    public void testStress() {
        String rc = ctx.get("RC", 120_000L);
        assertEquals("00", rc);
        System.out.println (chronometer);
    }


    @AfterAll
    static void tearDown() {
        TransactionManager tm = NameRegistrar.getIfExists("txnmgr-stress");
        while (tm.getActiveSessions() > 0)
            ISOUtil.sleep(500L);
        q2.shutdown(true);
    }
}
