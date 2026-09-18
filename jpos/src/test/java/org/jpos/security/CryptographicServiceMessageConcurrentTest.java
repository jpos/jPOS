/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.security;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

/**
 * Test for concurrent modification exceptions in CryptographicServiceMessage.
 *
 * <p>CryptographicServiceMessage.dump() iterates fields.keySet() (LinkedHashMap)
 * while addField() modifies the same map from another thread, causing CME.
 */
public class CryptographicServiceMessageConcurrentTest {

    private static final int ITERATIONS = 2000;
    private static final int THREADS = 4;

    /**
     * CryptographicServiceMessage.dump() iterates fields.keySet() at line 194.
     * The addField() method at line 118 modifies fields.put() without synchronization.
     * Concurrent iteration during modification causes ConcurrentModificationException.
     */
    @Test
    public void testCryptographicServiceMessageConcurrentDumpAndAddField() throws Throwable {
        final CryptographicServiceMessage csm = new CryptographicServiceMessage();
        csm.setMCL(CryptographicServiceMessage.MCL_KSM);

        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Thread[] writers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        csm.addField("TAG" + (threadNum * 1000 + j), "value" + j);
                        if (j % 10 == 0) Thread.yield();
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            });
            writers[i].start();
        }

        Thread dumper = new Thread(() -> {
            try {
                startLatch.await();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (int j = 0; j < ITERATIONS; j++) {
                    csm.dump(new PrintStream(baos), "  ");
                    baos.reset();
                    if (j % 10 == 0) Thread.yield();
                }
            } catch (Exception e) {
                failed.set(true);
            }
        });
        dumper.start();

        startLatch.countDown();
        for (Thread t : writers) t.join();
        dumper.join();

        assertFalse(failed.get(), "ConcurrentModificationException was thrown during concurrent dump and addField");
    }
}