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
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Test for concurrent modification exceptions in SecureKeySpec.
 *
 * <p>SecureKeySpec.dump() iterates optionalHeaders.entrySet() (LinkedHashMap)
 * while optionalHeaders may be modified from another thread via reflection,
 * causing ConcurrentModificationException.
 */
public class SecureKeySpecConcurrentTest {

    private static final int ITERATIONS = 2000;

    /**
     * SecureKeySpec.dump() iterates optionalHeaders LinkedHashMap at line 514.
     * The optionalHeaders map can be modified via reflection or direct field access,
     * causing CME when dump() iterates concurrently.
     */
    @Test
    public void testSecureKeySpecConcurrentDumpAndModifyHeaders() throws Throwable {
        final SecureKeySpec spec = new SecureKeySpec();
        spec.setKeyType("TYPE_ZPK");
        spec.setKeyLength(128);

        // Use reflection to add initial headers
        Field field = SecureKeySpec.class.getDeclaredField("optionalHeaders");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, String> optionalHeaders =
            (java.util.Map<String, String>) field.get(spec);
        optionalHeaders.put("INIT", "initial-value");

        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Thread modifier = new Thread(() -> {
            try {
                startLatch.await();
                for (int j = 0; j < ITERATIONS; j++) {
                    synchronized (optionalHeaders) {
                        optionalHeaders.put("KEY" + j, "VALUE" + j);
                    }
                    if (j % 10 == 0) Thread.yield();
                }
            } catch (Exception e) {
                failed.set(true);
            }
        });
        modifier.start();

        Thread dumper = new Thread(() -> {
            try {
                startLatch.await();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (int j = 0; j < ITERATIONS; j++) {
                    spec.dump(new PrintStream(baos), "  ");
                    baos.reset();
                    if (j % 10 == 0) Thread.yield();
                }
            } catch (Exception e) {
                failed.set(true);
            }
        });
        dumper.start();

        startLatch.countDown();
        modifier.join();
        dumper.join();

        assertFalse(failed.get(), "ConcurrentModificationException was thrown during concurrent dump and modify");
    }
}