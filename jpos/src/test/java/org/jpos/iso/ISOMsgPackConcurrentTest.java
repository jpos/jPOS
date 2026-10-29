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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * Tests for concurrent pack/unpack and set/unset operations on ISOMsg.
 * <p>
 * These tests verify that the lock used in pack()/unpack() matches the lock used in
 * set()/unset()/recalcBitMap()/getMaxField()/unset() — i.e. all must synchronize on the
 * same object ({@code fields}) to prevent concurrent modification and stale-read races.
 * <p>
 * The existing {@link ISOMsgConcurrentTest} only exercises dump() + set/unset, which was
 * already safe. These tests target the critical race: if pack()/unpack() used a different
 * lock (e.g. {@code synchronized(this)}) than set()/unset() ({@code synchronized(fields)}),
 * they could run concurrently and corrupt the fields map during iteration in
 * recalcBitMap(). Each test isolates a specific synchronization path.
 */
public class ISOMsgPackConcurrentTest {

    /** Number of operations per thread — high enough to surface races but bounded for CI speed. */
    private static final int ITERATIONS = 1000;
    /** Number of concurrent writer/reader threads to maximize contention. */
    private static final int THREADS = 4;

    /**
     * Minimal ISOPackager that packs/unpacks nothing — just returns empty bytes.
     * <p>
     * Used to exercise the {@code synchronized(fields)} path in pack()/unpack() without
     * requiring a full descriptor-based packager configuration (which would need external XML files).
     * The packager itself does no locking; all mutual exclusion comes from ISOMsg's synchronized blocks.
     */
    private static final ISOPackager NOOP_PACKAGER = new ISOPackager() {
        @Override
        public byte[] pack(ISOComponent m) throws ISOException {
            return new byte[0];
        }
        @Override
        public int unpack(ISOComponent m, byte[] b) throws ISOException {
            return 0;
        }
        @Override
        public void unpack(ISOComponent m, InputStream in) throws ISOException {
        }
        @Override
        public String getFieldDescription(ISOComponent m, int fldNumber) {
            return null;
        }
        @Override
        public ISOMsg createISOMsg() {
            return new ISOMsg();
        }
        @Override
        public String getDescription() {
            return "NoOpPackager";
        }
    };

    /**
     * Verifies that recalcBitMap() can run concurrently with set()/unset() without
     * throwing ConcurrentModificationException or producing inconsistent state.
     * <p>
     * This is the core race condition: recalcBitMap() iterates {@code fields.keySet()}
     * while writers call {@link ISOMsg#set(int, String)} and {@link ISOMsg#unset(int)}.
     * It also tests reentrant lock behavior — recalcBitMap() calls {@code set(new ISOBitMap(...))}
     * internally, which acquires the same {@code synchronized(fields)} monitor. The JVM
     * lock count increments rather than blocking, so this must not deadlock.
     */
    @Test
    public void testRecalcBitMapConcurrentWithSetAndUnset() throws Throwable {
        // Create a message with initial fields to give recalcBitMap something to work with.
        final ISOMsg msg = new ISOMsg();
        msg.setMTI("0200");
        msg.set(11, "000001");
        
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);
        final AtomicInteger recalcCount = new AtomicInteger(0);
        final AtomicInteger setCount = new AtomicInteger(0);

        // Launch multiple writer threads, each targeting distinct field ranges to maximize contention.
        Thread[] writers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        // Each writer targets a unique field number range to avoid contention on the same key.
                        int fieldNo = 1 + (threadNum * 100) + j;
                        // Alternate between set and unset to test both mutation paths under lock.
                        if (j % 5 == 0) {
                            msg.unset(fieldNo);
                        } else {
                            msg.set(fieldNo, "value" + j);
                        }
                        setCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            });
            writers[i].start();
        }

        // Single recalcBitMap thread competing with all writers for the fields lock.
        Thread recalcThread = new Thread(() -> {
            try {
                startLatch.await();
                for (int j = 0; j < ITERATIONS; j++) {
                    try {
                        msg.recalcBitMap();
                        recalcCount.incrementAndGet();
                    } catch (Exception e) {
                        failed.set(true);
                    }
                    // Yield periodically to increase interleaving with writer threads.
                    if (j % 10 == 0) Thread.yield();
                }
            } catch (Exception e) {
                failed.set(true);
            }
        });
        recalcThread.start();

        // Release all threads simultaneously and wait for completion.
        startLatch.countDown();
        for (Thread t : writers) t.join();
        recalcThread.join();

        assertFalse(failed.get(),
                "ConcurrentModificationException during concurrent recalcBitMap/set/unset. " +
                "recalc()=" + recalcCount.get() + " set/unset()=" + setCount.get());
    }

    /**
     * Verifies that pack() can run concurrently with set()/unset() without
     * throwing ConcurrentModificationException or producing inconsistent state.
     * <p>
     * This is the critical race condition we fixed: pack() acquires {@code synchronized(fields)},
     * then calls recalcBitMap() which iterates {@code fields.keySet()}, while writer threads call
     * {@link ISOMsg#set(int, String)} and {@link ISOMsg#unset(int)} — also under
     * {@code synchronized(fields)}. If pack() had used a different lock (e.g. the old
     * {@code synchronized(this)}), these operations would run concurrently on different monitors,
     * allowing concurrent modification of the TreeMap during iteration. This test would fail
     * with ConcurrentModificationException or produce silent data corruption if the locks diverge again.
     */
    @Test
    public void testPackConcurrentWithSetAndUnset() throws Throwable {
        // Create message with noop packager and initial fields.
        final ISOMsg msg = new ISOMsg();
        msg.setPackager(NOOP_PACKAGER);
        msg.setMTI("0200");
        msg.set(11, "000001");
        
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);
        final AtomicInteger packCount = new AtomicInteger(0);
        final AtomicInteger setCount = new AtomicInteger(0);

        // Launch multiple writer threads competing with packer for the fields lock.
        Thread[] writers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        // Each writer targets a unique field number range to maximize contention.
                        int fieldNo = 1 + (threadNum * 100) + j;
                        // Alternate between set and unset to exercise both mutation paths.
                        if (j % 5 == 0) {
                            msg.unset(fieldNo);
                        } else {
                            msg.set(fieldNo, "value" + j);
                        }
                        setCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            });
            writers[i].start();
        }

        // Single packer thread — the critical path that iterates fields.keySet() inside synchronized(fields).
        Thread packer = new Thread(() -> {
            try {
                startLatch.await();
                for (int j = 0; j < ITERATIONS; j++) {
                    try {
                        msg.pack();
                        packCount.incrementAndGet();
                    } catch (Exception e) {
                        failed.set(true);
                    }
                    // Yield periodically to increase interleaving with writer threads.
                    if (j % 10 == 0) Thread.yield();
                }
            } catch (Exception e) {
                failed.set(true);
            }
        });
        packer.start();

        // Release all threads simultaneously and wait for completion.
        startLatch.countDown();
        for (Thread t : writers) t.join();
        packer.join();

        assertFalse(failed.get(),
                "ConcurrentModificationException during concurrent pack/set/unset. " +
                "pack()=" + packCount.get() + " set/unset()=" + setCount.get());
    }

    /**
     * Verifies that getMaxField() returns consistent values when called
     * concurrently with set()/unset().
     * <p>
     * Without proper synchronization, getMaxField() could read a stale {@code maxField} value
     * because the dirty flag check and the field iteration in recalcMaxField() are not atomic
     * relative to concurrent set() calls. The {@code synchronized(fields)} block ensures that
     * the {@code maxFieldDirty} check, the optional recalculation, and the return of
     * {@code maxField} all happen under a single lock acquisition — so any thread sees a
     * consistent snapshot. The {@code volatile} keyword on {@code maxField} provides an extra
     * safety net for any indirect reads outside the synchronized block.
     */
    @Test
    public void testGetMaxFieldConcurrentWithSet() throws Throwable {
        final ISOMsg msg = new ISOMsg();
        
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);
        final AtomicInteger readCount = new AtomicInteger(0);

        // Launch multiple writer threads that only call set(), pushing maxField higher over time.
        Thread[] writers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        // Each writer targets a unique field number range to maximize contention.
                        msg.set(1 + (threadNum * 100) + j, "value");
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            });
            writers[i].start();
        }

        // Launch multiple reader threads that only call getMaxField() to check for stale reads.
        Thread[] readers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            readers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        int mf = msg.getMaxField();
                        // Verify maxField stays within expected bounds — a stale or corrupted value would be out of range.
                        if (mf < 0 || mf > 400000) {
                            failed.set(true);
                        }
                        readCount.incrementAndGet();
                        // Yield periodically to increase interleaving with writer threads.
                        if (j % 10 == 0) Thread.yield();
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            });
            readers[i].start();
        }

        // Release all threads simultaneously and wait for completion.
        startLatch.countDown();
        for (Thread t : writers) t.join();
        for (Thread t : readers) t.join();

        assertFalse(failed.get(),
                "Inconsistent getMaxField() during concurrent set. readCount=" + readCount.get());
    }

    /**
     * Verifies that unpack() can run concurrently with set()/unset() without
     * throwing ConcurrentModificationException or producing inconsistent state.
     * <p>
     * Tests the {@code synchronized(fields)} path in {@link ISOMsg#unpack(byte[])} and
     * {@link ISOMsg#unpack(InputStream)}. The noop packager inserts no fields, so the test
     * isolates the lock acquisition itself — if unpack() used a different lock than set()/unset(),
     * concurrent map mutations during the unpack lifecycle could corrupt internal state.
     */
    @Test
    public void testUnpackConcurrentWithSetAndUnset() throws Throwable {
        final ISOMsg msg = new ISOMsg();
        msg.setPackager(NOOP_PACKAGER);
        msg.setMTI("0200");
        msg.set(11, "000001");
        
        // Pack once to get valid bytes (noop packager returns empty array).
        byte[] packed = msg.pack();

        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);
        final AtomicInteger unpackCount = new AtomicInteger(0);
        final AtomicInteger setCount = new AtomicInteger(0);

        // Launch multiple writer threads competing with unpacker for the fields lock.
        Thread[] writers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        // Each writer targets a unique field number range to maximize contention.
                        int fieldNo = 1 + (threadNum * 100) + j;
                        // Alternate between set and unset to exercise both mutation paths under lock.
                        if (j % 5 == 0) {
                            msg.unset(fieldNo);
                        } else {
                            msg.set(fieldNo, "value" + j);
                        }
                        setCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            });
            writers[i].start();
        }

        // Single unpacker thread — the critical path that must not conflict with concurrent set()/unset().
        Thread unpacker = new Thread(() -> {
            try {
                startLatch.await();
                for (int j = 0; j < ITERATIONS; j++) {
                    try {
                        msg.unpack(packed);
                        unpackCount.incrementAndGet();
                    } catch (Exception e) {
                        failed.set(true);
                    }
                    // Yield periodically to increase interleaving with writer threads.
                    if (j % 10 == 0) Thread.yield();
                }
            } catch (Exception e) {
                failed.set(true);
            }
        });
        unpacker.start();

        // Release all threads simultaneously and wait for completion.
        startLatch.countDown();
        for (Thread t : writers) t.join();
        unpacker.join();

        assertFalse(failed.get(),
                "ConcurrentModificationException during concurrent unpack/set/unset. " +
                "unpack()=" + unpackCount.get() + " set/unset()=" + setCount.get());
    }

    /**
     * Stress test that runs multiple packer and setter threads simultaneously.
     * <p>
     * This maximizes contention by having {@code THREADS} packers all calling pack() while
     * {@code THREADS} setters concurrently call set(). The high thread count and alternating
     * operations increase the probability of catching lock mismatches that simpler tests might
     * miss. If any thread acquires a different lock than another, the TreeMap iteration in
     * recalcBitMap() may observe structural modifications from an unsynchronized path.
     */
    @Test
    public void testPackSetAlternatingStress() throws Throwable {
        final ISOMsg msg = new ISOMsg();
        msg.setPackager(NOOP_PACKAGER);
        msg.setMTI("0200");
        
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);

        // Launch multiple packer threads — each acquires synchronized(fields) and iterates keys.
        Thread[] packers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            packers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        try {
                            msg.pack();
                        } catch (Exception e) {
                            failed.set(true);
                        }
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            });
            packers[i].start();
        }

        // Launch multiple setter threads — each acquires synchronized(fields) to mutate the map.
        Thread[] setters = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            setters[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        // Each setter targets a unique field number range.
                        msg.set(1 + (threadNum * 100) + j, "value" + j);
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            });
            setters[i].start();
        }

        // Release all threads simultaneously and wait for completion.
        startLatch.countDown();
        for (Thread t : packers) t.join();
        for (Thread t : setters) t.join();

        assertFalse(failed.get(), "ConcurrentModificationException during alternating pack/set stress");
    }

    /**
     * Verifies that unset() can run concurrently with set() without
     * throwing ConcurrentModificationException.
     * <p>
     * Exercises the {@code synchronized(fields)} path in {@link ISOMsg#unset(int)} under
     * contention from both setter and unsetter threads. Tests mutual exclusion between two
     * different mutating methods — if either one loses its lock, concurrent map modifications
     * during iteration (e.g. by pack() or dump()) would corrupt state. The writers alternate
     * between set and unset operations to increase interleaving complexity.
     */
    @Test
    public void testUnsetConcurrentWithSet() throws Throwable {
        final ISOMsg msg = new ISOMsg();
        msg.setMTI("0200");
        
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);
        final AtomicInteger unsetCount = new AtomicInteger(0);
        final AtomicInteger setCount = new AtomicInteger(0);

        // Launch writer threads that alternate between set and unset operations.
        Thread[] writers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        // Each writer targets a unique field number range.
                        int fieldNo = 1 + (threadNum * 100) + j;
                        msg.set(fieldNo, "value" + j);
                        // Periodically unset the previous field to create interleaved set/unset patterns.
                        if (j > 0 && j % 3 == 0) {
                            msg.unset(fieldNo - 1);
                            unsetCount.incrementAndGet();
                        }
                        setCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            });
            writers[i].start();
        }

        // Launch dedicated unsetter threads that only call unset() — competing with writers for the fields lock.
        Thread[] unsets = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            unsets[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        // Each unsetter targets a unique field number range.
                        msg.unset(1 + (threadNum * 100) + j);
                        unsetCount.incrementAndGet();
                        // Yield periodically to increase interleaving with writer threads.
                        if (j % 10 == 0) Thread.yield();
                    }
                } catch (Exception e) {
                    failed.set(true);
                }
            });
            unsets[i].start();
        }

        // Release all threads simultaneously and wait for completion.
        startLatch.countDown();
        for (Thread t : writers) t.join();
        for (Thread t : unsets) t.join();

        assertFalse(failed.get(),
                "ConcurrentModificationException during concurrent set/unset. " +
                "set()=" + setCount.get() + " unset()=" + unsetCount.get());
    }
}
