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

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Test;

/**
 * Test for concurrent modification exceptions in SecureKeyBlock.
 *
 * <p>SecureKeyBlock.dump() now properly synchronizes on 'this' when iterating
 * the optionalHeaders map, preventing ConcurrentModificationException when
 * multiple threads call dump() concurrently.
 */
public class SecureKeyBlockConcurrentTest {

    private static final int ITERATIONS = 2000;
    private static final int THREADS = 4;

    @Test
    public void testSecureKeyBlockConcurrentDump() throws Throwable {
        // Use working test data from SecureKeyBlockBuilderTest.testBuildMAC8
        final SecureKeyBlock keyBlock = SecureKeyBlockBuilder.newBuilder().build(
            "00040V2RG17N0003"
            + ISOUtil.hexString(ISOUtil.hex2byte("A9B8C7D6E5F49382"))  // 16 hex chars = 8 bytes key
            + ISOUtil.hexString(ISOUtil.hex2byte("E1F22F1E"))          // 8 hex chars = 4 bytes MAC
        );

        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Thread[] dumpers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            dumpers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (int j = 0; j < ITERATIONS; j++) {
                        keyBlock.dump(new PrintStream(baos), "  ");
                        baos.reset();
                        if (j % 10 == 0) Thread.yield();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    failed.set(true);
                }
            });
            dumpers[i].start();
        }

        startLatch.countDown();
        for (Thread t : dumpers) t.join();

        assertFalse(failed.get(), "ConcurrentModificationException was thrown during concurrent dump calls");
    }
}