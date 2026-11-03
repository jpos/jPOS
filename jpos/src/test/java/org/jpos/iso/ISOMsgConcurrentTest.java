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

package org.jpos.iso;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jpos.tlv.TLVList;
import org.jpos.util.FSDMsg;
import org.junit.jupiter.api.Test;

import org.jpos.iso.Dataset;
import org.jpos.iso.DatasetFormat;
import org.jpos.iso.ISODataset;

/**
 * Tests for concurrent modification exceptions in ISO components.
 *
 * <p>These tests verify that dump() operations on ISOMsg, TLVList, FSDMsg,
 * and ISODatasetField are safe when called concurrently with set/unset operations
 * from other threads, matching the pattern used in ContextTest and ProfilerTest.
 */
public class ISOMsgConcurrentTest {

    private static final int ITERATIONS = 2000;
    private static final int THREADS = 4;

    /**
     * ISOMsg.dump() iterates fields.keySet() (TreeMap) while set()/unset()
     * modify the same map from other threads, causing ConcurrentModificationException.
     *
     * <p>This is THE highest risk because ISOMsg is the most common object stored
     * in Context for request/response messages.
     */
    @Test
    public void testISOMsgConcurrentDumpAndSet() throws Throwable {
        final ISOMsg msg = new ISOMsg();
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Thread[] writers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        msg.set(1 + (threadNum * 100) + j, "value");
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
                    msg.dump(new PrintStream(baos), "  ");
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

        assertFalse(failed.get(), "ConcurrentModificationException was thrown during concurrent dump and set");
    }

    /**
     * Verifies that ISOMsg.dump() with nested ISOMsg (field path) does not throw
     * ConcurrentModificationException when fields are modified concurrently.
     */
    @Test
    public void testISOMsgNestedFieldConcurrentDumpAndSet() throws Throwable {
        final ISOMsg msg = new ISOMsg();
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Thread writer = new Thread(() -> {
            try {
                startLatch.await();
                for (int j = 0; j < ITERATIONS; j++) {
                    msg.set("63." + (j % 10) + ".1", "value" + j);
                    if (j % 10 == 0) Thread.yield();
                }
            } catch (Exception e) {
                failed.set(true);
            }
        });
        writer.start();

        Thread dumper = new Thread(() -> {
            try {
                startLatch.await();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (int j = 0; j < ITERATIONS; j++) {
                    msg.dump(new PrintStream(baos), "  ");
                    baos.reset();
                    if (j % 10 == 0) Thread.yield();
                }
            } catch (Exception e) {
                failed.set(true);
            }
        });
        dumper.start();

        startLatch.countDown();
        writer.join();
        dumper.join();

        assertFalse(failed.get(), "ConcurrentModificationException was thrown during nested field dump");
    }

    /**
     * TLVList.dump() iterates getTags() which returns the raw internal ArrayList,
     * while append() and remove() modify it from other threads, causing CME.
     *
     * <p>TLVList is commonly nested in ISOMsg fields for EMV data and proprietary
     * TLV structures.
     */
    @Test
    public void testTLVListConcurrentDumpAndAppend() throws Throwable {
        final TLVList tlvList = new TLVList();
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Thread[] writers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        tlvList.append(0xD0 + threadNum, ("value" + j).getBytes());
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
                    tlvList.dump(new PrintStream(baos), "  ");
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

        assertFalse(failed.get(), "ConcurrentModificationException was thrown during concurrent dump and append");
    }

    /**
     * TLVList with nested TLVMsg that triggers modification during toString()
     * (similar to the Profiler Entry scenario).
     */
    @Test
    public void testTLVListDumpWithMutatingNestedMsg() throws Exception {
        final TLVList tlvList = new TLVList();
        final AtomicBoolean failed = new AtomicBoolean(false);

        Thread mutator = new Thread(() -> {
            for (int j = 0; j < ITERATIONS; j++) {
                try {
                    tlvList.append(0xD0 + (j % 10), ("value" + j).getBytes());
                    if (j % 10 == 0) Thread.yield();
                } catch (Exception e) {
                    failed.set(true);
                }
            }
        });
        mutator.start();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int j = 0; j < ITERATIONS; j++) {
                tlvList.dump(new PrintStream(baos), "  ");
                baos.reset();
                if (j % 10 == 0) Thread.yield();
            }
        } catch (Exception e) {
            failed.set(true);
        }

        mutator.join();
        assertFalse(failed.get(), "ConcurrentModificationException was thrown during dump with concurrent append");
    }

    /**
     * FSDMsg.dump() iterates fields.keySet() (LinkedHashMap) while set()/unset()
     * modify it from other threads, causing CME.
     */
    @Test
    public void testFSDMsgConcurrentDumpAndSet() throws Throwable {
        final FSDMsg msg = new FSDMsg("test-schema", "test");
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Thread[] writers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        msg.set("field" + (threadNum * 100 + j), "value" + j);
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
                    msg.dump(new PrintStream(baos), "  ");
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

        assertFalse(failed.get(), "ConcurrentModificationException was thrown during concurrent dump and set");
    }

    /**
     * ISODatasetField.dump() iterates datasets ArrayList while addDataset()
     * and setValue() modify it from other threads, causing CME.
     */
    @Test
    public void testISODatasetFieldConcurrentDumpAndAddDataset() throws Throwable {
        final ISODatasetField datasetField = new ISODatasetField(1);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Thread[] writers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        Dataset ds = new ISODataset(0x01 + threadNum, DatasetFormat.TLV);
                        datasetField.addDataset(ds);
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
                    datasetField.dump(new PrintStream(baos), "  ");
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

        assertFalse(failed.get(), "ConcurrentModificationException was thrown during concurrent dump and addDataset");
    }
}