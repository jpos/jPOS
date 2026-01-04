/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2025 jPOS Software SRL
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

package org.jpos.space;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LSpace (Loom-optimized Space) implementation.
 * Tests both functional correctness (similar to TSpace) and
 * specific per-key isolation and Virtual Thread efficiency features.
 *
 * @since 3.0
 */
@SuppressWarnings("unchecked")
public class LSpaceTest implements SpaceListener {
    LSpace<String, Object> sp;
    Object notifiedValue = null;

    @BeforeEach
    public void setUp() {
        sp = new LSpace<>();
    }

    @AfterEach
    public void tearDown() {
        sp.close();
    }

    @Override
    public void notify(Object key, Object value) {
        notifiedValue = value;
    }

    /**
     * Basic functional test - same as TSpace
     */
    @Test
    public void testSimpleOut() {
        sp.out("testSimpleOut_Key", "ABC");
        sp.out("testSimpleOut_Key", "XYZ");
        assertEquals("ABC", sp.rdp("testSimpleOut_Key"));
        assertEquals("ABC", sp.inp("testSimpleOut_Key"));
        assertEquals("XYZ", sp.rdp("testSimpleOut_Key"));
        assertEquals("XYZ", sp.inp("testSimpleOut_Key"));
        assertNull(sp.rdp("Test"));
        assertNull(sp.inp("Test"));
    }

    /**
     * Test expiration - same as TSpace
     */
    @Test
    public void testExpiration() throws InterruptedException {
        sp.out("testExpiration_Key", "ABC", 50);
        assertEquals("ABC", sp.rdp("testExpiration_Key"));
        Thread.sleep(75); // allow for low system timer accuracy
        assertNull(sp.rdp("testExpiration_Key"), "ABC");
    }

    /**
     * Test blocking read operations - same as TSpace
     */
    @Test
    public void testOutRdpInpRdp() throws Exception {
        Object o = Boolean.TRUE;
        String k = "testOutRdpInpRdp_Key";
        sp.out(k, o);
        assertTrue(o.equals(sp.rdp(k)));
        assertTrue(o.equals(sp.rd(k)));
        assertTrue(o.equals(sp.rd(k, 1000)));
        assertTrue(o.equals(sp.inp(k)));
        assertNull(sp.rdp(k));
        assertNull(sp.rd(k, 100));
    }

    /**
     * Test push (LIFO) operations - same as TSpace
     */
    @Test
    public void testPush() {
        sp.out("testPush_Key", "1");
        sp.push("testPush_Key", "2");
        assertEquals("2", sp.inp("testPush_Key"));
        assertEquals("1", sp.inp("testPush_Key"));
    }

    /**
     * Test put (replace) operations - same as TSpace
     */
    @Test
    public void testPut() {
        sp.out("testPut_Key", "1");
        sp.out("testPut_Key", "2");
        sp.put("testPut_Key", "3");
        assertEquals("3", sp.inp("testPut_Key"));
        assertNull(sp.rdp("testPut_Key"));
    }

    /**
     * Test Template matching - same as TSpace
     */
    @Test
    public void testTemplate() {
        sp.out("testTemplate_Key", "123");
        sp.out("testTemplate_Key", "456");
        sp.out("testTemplate_Key", "789");

        ObjectTemplate tmpl = new ObjectTemplate("testTemplate_Key", "456");
        assertEquals("456", sp.rdp(tmpl));
        assertEquals("456", sp.inp(tmpl));
        assertNull(sp.rdp(tmpl));
        assertNull(sp.inp(tmpl));

        // Other values still there
        assertNotNull(sp.inp("testTemplate_Key"));
        assertNotNull(sp.inp("testTemplate_Key"));
    }

    /**
     * Test garbage collection
     */
    @Test
    public void testGc() throws InterruptedException {
        sp.out("testGC_Key", "ABC", 50);
        assertEquals("ABC", sp.rdp("testGC_Key"));
        Thread.sleep(75);
        sp.gc();
        assertNull(sp.rdp("testGC_Key"));
    }

    /**
     * Test listeners
     */
    @Test
    public void testListener() throws InterruptedException {
        sp.addListener("testListener_Key", this);
        sp.out("testListener_Key", "ABC");
        Thread.sleep(100);  // Give listener time to fire
        assertEquals("ABC", notifiedValue);
    }

    /**
     * Test that per-key isolation works correctly - threads waiting on different keys
     * should not be woken up when a different key is written to.
     */
    @Test
    public void testPerKeyIsolation() throws InterruptedException {
        final LSpace<String, String> lsp = new LSpace<>();
        final int numKeys = 100;
        final CountDownLatch allThreadsWaiting = new CountDownLatch(numKeys);
        final CountDownLatch allThreadsCompleted = new CountDownLatch(numKeys);
        final AtomicInteger completedCount = new AtomicInteger(0);

        // Start 100 virtual threads, each waiting on a different key
        for (int i = 0; i < numKeys; i++) {
            final String key = "key" + i;
            Thread.startVirtualThread(() -> {
                allThreadsWaiting.countDown();
                lsp.in(key, 5000);  // Wait up to 5 seconds
                completedCount.incrementAndGet();
                allThreadsCompleted.countDown();
            });
        }

        // Wait for all threads to start waiting
        assertTrue(allThreadsWaiting.await(2, TimeUnit.SECONDS), "All threads should start waiting");
        Thread.sleep(100);  // Give them time to actually wait

        // Write to all keys - each write should wake exactly one thread
        for (int i = 0; i < numKeys; i++) {
            lsp.out("key" + i, "value" + i);
        }

        // All threads should complete
        assertTrue(allThreadsCompleted.await(2, TimeUnit.SECONDS), "All threads should complete");
        assertEquals(numKeys, completedCount.get(), "All threads should have received their values");
    }

    /**
     * Test that writing to one key doesn't wake threads waiting on a different key.
     */
    @Test
    public void testNoSpuriousWakeups() throws InterruptedException {
        final LSpace<String, String> lsp = new LSpace<>();
        final AtomicInteger wakeupsOnA = new AtomicInteger(0);
        final CountDownLatch threadStarted = new CountDownLatch(1);

        // Start a thread waiting on key "A"
        Thread waiter = Thread.startVirtualThread(() -> {
            threadStarted.countDown();
            String result = lsp.in("A", 1000);  // Wait 1 second
            if (result != null) {
                wakeupsOnA.incrementAndGet();
            }
        });

        // Wait for thread to start
        assertTrue(threadStarted.await(1, TimeUnit.SECONDS));
        Thread.sleep(100);  // Give it time to actually wait

        // Write to key "B" - should NOT wake thread waiting on "A"
        lsp.out("B", "valueB");
        Thread.sleep(200);

        // Thread should still be waiting (no wakeup)
        assertEquals(0, wakeupsOnA.get(), "Thread waiting on A should not wake up for key B");

        // Now write to "A" - should wake the thread
        lsp.out("A", "valueA");
        waiter.join(1000);

        assertEquals(1, wakeupsOnA.get(), "Thread should wake up exactly once for key A");
    }

    /**
     * Stress test with many virtual threads on many keys.
     * Tests producer-consumer coordination without data loss under high concurrency.
     * Thread.yield() calls help virtual threads cooperate on carrier threads in CPU-intensive sections.
     */
    @Test
    public void testVirtualThreadStress() throws InterruptedException {
        final LSpace<String, Integer> lsp = new LSpace<>();
        final int numKeys = 50;
        final int opsPerKey = 100;
        final CountDownLatch allDone = new CountDownLatch(numKeys * 2);  // producers + consumers

        // Start producers and consumers for each key
        for (int k = 0; k < numKeys; k++) {
            final String key = "key" + k;

            // Producer thread
            Thread.startVirtualThread(() -> {
                try {
                    for (int i = 0; i < opsPerKey; i++) {
                        lsp.out(key, i);
                        if (i % 10 == 0)
                            Thread.yield();  // Allow other virtual threads to run
                    }
                } finally {
                    allDone.countDown();
                }
            });

            // Consumer thread
            Thread.startVirtualThread(() -> {
                try {
                    for (int i = 0; i < opsPerKey; i++) {
                        Integer val = lsp.in(key, 5000);
                        assertNotNull(val, "Should receive value for key: " + key);
                        if (i % 10 == 0)
                            Thread.yield();  // Allow other virtual threads to run
                    }
                } finally {
                    allDone.countDown();
                }
            });
        }

        // Wait for all operations to complete
        assertTrue(allDone.await(30, TimeUnit.SECONDS), "All operations should complete");

        // Verify space is empty - entries should be removed when queues become empty
        assertEquals(0, lsp.getKeySet().size(), "Space should be empty after all ops");
    }

    /**
     * Test that entries are properly cleaned up when queues become empty.
     * With the double-check pattern, entries can be safely removed without
     * race conditions.
     */
    @Test
    public void testMemoryCleanup() {
        final LSpace<String, Integer> lsp = new LSpace<>();

        // Create and remove many entries
        for (int i = 0; i < 1000; i++) {
            lsp.out("key" + i, i);
            lsp.in("key" + i);
        }

        // Entries should be removed from map when queues become empty
        assertEquals(0, lsp.getKeySet().size(), "All entries should be removed after consumption");
    }

    /**
     * Test that GC doesn't cause race conditions with concurrent operations.
     * With the double-check pattern, GC can safely remove entries while threads
     * are waiting - stale references are detected and retried.
     */
    @Test
    public void testGcRaceCondition() throws InterruptedException {
        final LSpace<String, Integer> lsp = new LSpace<>();
        final int numKeys = 10;
        final int iterations = 50;
        final CountDownLatch allDone = new CountDownLatch(numKeys);

        // Start threads that repeatedly add short-lived entries and trigger GC
        for (int k = 0; k < numKeys; k++) {
            final String key = "gckey" + k;
            Thread.startVirtualThread(() -> {
                try {
                    for (int i = 0; i < iterations; i++) {
                        // Add entry with very short timeout
                        lsp.out(key, i, 10);

                        // Force GC between operations
                        if (i % 5 == 0) {
                            lsp.gc();
                        }

                        // Try to read - should either get value or timeout gracefully
                        Integer val = lsp.in(key, 100);
                        // Value may be null if it expired, but should not hang

                        Thread.yield();
                    }
                } finally {
                    allDone.countDown();
                }
            });
        }

        // All threads should complete without hanging
        assertTrue(allDone.await(10, TimeUnit.SECONDS),
            "All threads should complete without hanging due to GC race conditions");
    }

    /**
     * Test existAny with multiple keys using polling approach.
     */
    @Test
    public void testExistAnyMultipleKeys() throws InterruptedException {
        final LSpace<String, String> lsp = new LSpace<>();
        final String[] keys = {"key1", "key2", "key3"};

        // Initially no keys exist
        assertFalse(lsp.existAny(keys), "No keys should exist initially");

        // Start a thread that will check existAny with timeout
        final AtomicInteger foundCount = new AtomicInteger(0);
        Thread checker = Thread.startVirtualThread(() -> {
            if (lsp.existAny(keys, 2000)) {
                foundCount.incrementAndGet();
            }
        });

        Thread.sleep(100);  // Let checker start waiting

        // Write to one of the keys
        lsp.out("key2", "value2");

        // Checker should find it
        checker.join(3000);
        assertEquals(1, foundCount.get(), "existAny should have found the key");

        // Verify we can still read the value
        assertEquals("value2", lsp.rdp("key2"));
    }

    /**
     * Test nrd (negative read) with per-key conditions.
     */
    @Test
    public void testNrdWithPerKeyConditions() throws InterruptedException {
        final LSpace<String, String> lsp = new LSpace<>();

        // Put a value
        lsp.out("testKey", "value1");

        // Start thread that will wait for key to become empty
        final AtomicInteger nrdCompleted = new AtomicInteger(0);
        Thread nrdThread = Thread.startVirtualThread(() -> {
            lsp.nrd("testKey", 2000);  // Wait for key to be absent
            nrdCompleted.incrementAndGet();
        });

        Thread.sleep(100);

        // Key still has value, nrd should be waiting
        assertEquals(0, nrdCompleted.get());

        // Remove the value
        lsp.in("testKey");

        // nrd should complete
        nrdThread.join(1000);
        assertEquals(1, nrdCompleted.get(), "nrd should complete when key becomes absent");
    }

    /**
     * Test that multiple rd() waiters are ALL woken up when a value is added.
     * This verifies the fix for using signalAll() instead of signal().
     */
    @Test
    public void testMultipleRdWaiters() throws InterruptedException {
        final LSpace<String, String> lsp = new LSpace<>();
        final int numReaders = 10;
        final CountDownLatch allStarted = new CountDownLatch(numReaders);
        final CountDownLatch allCompleted = new CountDownLatch(numReaders);
        final AtomicInteger successCount = new AtomicInteger(0);

        // Start 10 threads all waiting on rd() for the same key
        for (int i = 0; i < numReaders; i++) {
            Thread.startVirtualThread(() -> {
                allStarted.countDown();
                String value = lsp.rd("sharedKey", 2000);  // Non-destructive read
                if (value != null && value.equals("sharedValue")) {
                    successCount.incrementAndGet();
                }
                allCompleted.countDown();
            });
        }

        // Wait for all threads to start waiting
        assertTrue(allStarted.await(1, TimeUnit.SECONDS), "All reader threads should start");
        Thread.sleep(100);  // Give them time to actually wait

        // Add ONE value - should wake ALL rd() waiters
        lsp.out("sharedKey", "sharedValue");

        // All threads should complete and read the value
        assertTrue(allCompleted.await(2, TimeUnit.SECONDS), "All reader threads should complete");
        assertEquals(numReaders, successCount.get(),
            "All " + numReaders + " rd() waiters should have read the same value");
    }

    /**
     * Regression test:
     * If an entry is removed while there are threads blocked on hasValue for that key,
     * those waiters can be stranded forever (future out() creates a new KeyEntry+Condition).
     *
     * This test uses:
     * - N waiters blocked on in(key)
     * - one consumer thread does an inp(key) *after* a value is produced, making the queue empty
     * - then a producer publishes N values
     *
     * Expected:
     * - all waiters must complete (none stranded).
     *
     * This test fails on implementations that remove the entry in inp()/non-blocking removal paths
     * without checking hasWaiters(hasValue).
     */
    @Test
    public void testNoOrphanedWaitersWhenInpEmptiesQueue() throws Exception {
        final LSpace<String, String> lsp = new LSpace<>();
        final String key = "orphanInpKey";

        final int waiters = 25;
        final CountDownLatch allWaitingStarted = new CountDownLatch(waiters);
        final CountDownLatch allWaitersDone = new CountDownLatch(waiters);
        final AtomicInteger received = new AtomicInteger(0);

        // Waiters use rd() so they will park on hasValue but will NOT consume "warmup".
        for (int i = 0; i < waiters; i++) {
            Thread.startVirtualThread(() -> {
                allWaitingStarted.countDown();
                String v = lsp.rd(key); // indefinite wait, non-destructive
                if (v != null) {
                    received.incrementAndGet();
                }
                allWaitersDone.countDown();
            });
        }

        assertTrue(allWaitingStarted.await(2, TimeUnit.SECONDS),
          "Waiters should start promptly");
        Thread.sleep(100); // give time to actually park on hasValue

        // Publish one value; this should wake rd() waiters, but value remains queued.
        lsp.out(key, "warmup");

        // Ensure the value is present before attempting inp(), avoiding any timing window.
        assertNotNull(lsp.rd(key, 1000), "warmup should be visible via rd()");
        assertEquals("warmup", lsp.inp(key), "inp should remove the warmup value deterministically");

        // Now publish a final value. Since rd() is non-destructive and we use signalAll,
        // one out is enough for all waiters to complete successfully.
        lsp.out(key, "final");

        assertTrue(allWaitersDone.await(5, TimeUnit.SECONDS),
          "All waiters should complete; stranded waiters indicate an orphaned Condition bug");
        assertEquals(waiters, received.get(), "All waiters should have received a value");
    }

    /**
     * Regression test:
     * GC must not remove a KeyEntry while threads are awaiting hasValue on that key,
     * otherwise those waiters can be stranded forever.
     *
     * This test:
     * - starts waiters blocked on in(key) (indefinite)
     * - publishes an expirable that will expire quickly
     * - calls gc() to remove the expired element (queue becomes empty)
     * - then publishes enough values for all waiters
     *
     * Expected:
     * - no waiters are stranded; all complete.
     *
     * This fails if gc() removes the entry without guarding against hasValue waiters.
     */
    @Test
    public void testNoOrphanedWaitersWhenGcEmptiesQueue() throws Exception {
        final LSpace<String, String> lsp = new LSpace<>();
        final String key = "orphanGcKey";

        final int waiters = 25;
        final CountDownLatch allWaitingStarted = new CountDownLatch(waiters);
        final CountDownLatch allWaitersDone = new CountDownLatch(waiters);
        final AtomicInteger received = new AtomicInteger(0);

        for (int i = 0; i < waiters; i++) {
            Thread.startVirtualThread(() -> {
                allWaitingStarted.countDown();
                String v = lsp.in(key); // indefinite wait
                if (v != null) {
                    received.incrementAndGet();
                }
                allWaitersDone.countDown();
            });
        }

        assertTrue(allWaitingStarted.await(2, TimeUnit.SECONDS));
        Thread.sleep(100);

        // Publish a very short-lived expirable and let it expire.
        lsp.out(key, "exp", 10);
        Thread.sleep(50);

        // Force GC; it will remove expired value and potentially empty the queue.
        // If GC removes the entry while there are waiters, they can be stranded.
        lsp.gc();

        // Now publish one value per waiter.
        for (int i = 0; i < waiters; i++) {
            lsp.out(key, "v" + i);
        }

        assertTrue(allWaitersDone.await(5, TimeUnit.SECONDS),
          "All waiters should complete; stranded waiters indicate a GC orphaning bug");
        assertEquals(waiters, received.get(), "All waiters should have received a value");
    }

    /**
     * Leak-prevention test:
     * Timed in(key, timeout) uses computeIfAbsent and may create an empty entry even if no producer ever writes.
     * After timeout returns null, the empty entry must be cleaned up (not retained forever).
     *
     * This test:
     * - calls in("leakKey", 10) and expects null
     * - asserts the key is not present afterwards
     *
     * This fails if awaitValue returns null on timeout without housekeeping.
     */
    @Test
    public void testTimedInTimeoutDoesNotLeakEntry() {
        final LSpace<String, String> lsp = new LSpace<>();
        final String key = "leakTimedInKey";

        assertNull(lsp.in(key, 10), "Expected timeout to return null");
        assertFalse(lsp.getKeySet().contains(key),
          "Empty entry created by timed in() must be removed after timeout");
        assertEquals(0, lsp.getKeySet().size(), "Space should remain empty after timeout-only access");
    }

    /**
     * Leak-prevention test for rd(key, timeout):
     * same as timed in(), but non-destructive.
     */
    @Test
    public void testTimedRdTimeoutDoesNotLeakEntry() {
        final LSpace<String, String> lsp = new LSpace<>();
        final String key = "leakTimedRdKey";

        assertNull(lsp.rd(key, 10), "Expected timeout to return null");
        assertFalse(lsp.getKeySet().contains(key),
          "Empty entry created by timed rd() must be removed after timeout");
        assertEquals(0, lsp.getKeySet().size(), "Space should remain empty after timeout-only access");
    }

    /**
     * Correctness test:
     * Untimed in(key) must not return null unless interrupted.
     *
     * This test attempts to exercise "mapping changed" / "entry replaced" logic by:
     * - starting an untimed in(key) waiter
     * - concurrently creating/removing the entry via a timed wait (which may remove the entry on timeout)
     * - then publishing a value
     *
     * Expected:
     * - the untimed waiter must return the value (not null), and must not get stuck.
     *
     * This tends to fail on implementations that:
     * - break out of inner loops on entries.get(key) != entry and then return result (null),
     * - or orphan the waiter by removing the entry it is awaiting on.
     */
    @Test
    public void testUntimedInDoesNotReturnNullOnEntryChurn() throws Exception {
        final LSpace<String, String> lsp = new LSpace<>();
        final String key = "churnKey";

        final CountDownLatch waiterStarted = new CountDownLatch(1);
        final CountDownLatch waiterDone = new CountDownLatch(1);
        final AtomicInteger gotValue = new AtomicInteger(0);

        Thread waiter = Thread.startVirtualThread(() -> {
            waiterStarted.countDown();
            String v = lsp.in(key); // indefinite
            if (v != null) {
                gotValue.incrementAndGet();
            }
            waiterDone.countDown();
        });

        assertTrue(waiterStarted.await(1, TimeUnit.SECONDS));
        Thread.sleep(50);

        // Churn: a timed in() that times out may create and then remove an empty entry.
        // Run it a few times to increase chances of mapping churn.
        for (int i = 0; i < 25; i++) {
            assertNull(lsp.in(key, 1));
        }

        // Now publish a value; waiter must receive it.
        lsp.out(key, "value");
        assertTrue(waiterDone.await(2, TimeUnit.SECONDS),
          "Untimed waiter should complete after value is produced");
        assertEquals(1, gotValue.get(), "Untimed in() should not return null under churn");
        waiter.join(1000);
    }

    /**
     * Similar churn test for rd(key) (untimed):
     * - rd() should not return null once a value is produced (and should complete).
     */
    @Test
    public void testUntimedRdDoesNotReturnNullOnEntryChurn() throws Exception {
        final LSpace<String, String> lsp = new LSpace<>();
        final String key = "churnRdKey";

        final CountDownLatch waiterStarted = new CountDownLatch(1);
        final CountDownLatch waiterDone = new CountDownLatch(1);
        final AtomicInteger gotValue = new AtomicInteger(0);

        Thread waiter = Thread.startVirtualThread(() -> {
            waiterStarted.countDown();
            String v = lsp.rd(key); // indefinite
            if (v != null) {
                gotValue.incrementAndGet();
            }
            waiterDone.countDown();
        });

        assertTrue(waiterStarted.await(1, TimeUnit.SECONDS));
        Thread.sleep(50);

        // Churn via timed rd() timeouts (create/remove entries).
        for (int i = 0; i < 25; i++) {
            assertNull(lsp.rd(key, 1));
        }

        lsp.out(key, "value");
        assertTrue(waiterDone.await(2, TimeUnit.SECONDS),
          "Untimed rd waiter should complete after value is produced");
        assertEquals(1, gotValue.get(), "Untimed rd() should not return null under churn");
        waiter.join(1000);
    }

    /**
     * Targeted race test for non-blocking inp():
     * Ensure inp() removal does not remove the entry if there are waiters,
     * but still allows the entry to be removed once waiters are gone.
     *
     * This is a “liveness + cleanup” combined check:
     * - start waiters
     * - do warmup out+inp (empties queue)
     * - provide values and let waiters drain
     * - ensure key removed after all consumed (no retention).
     */
    @Test
    public void testEntryEventuallyRemovedAfterWaitersDrain() throws Exception {
        final LSpace<String, String> lsp = new LSpace<>();
        final String key = "eventualRemovalKey";

        final int waiters = 10;
        final CountDownLatch started = new CountDownLatch(waiters);
        final CountDownLatch done = new CountDownLatch(waiters);

        for (int i = 0; i < waiters; i++) {
            Thread.startVirtualThread(() -> {
                started.countDown();
                assertNotNull(lsp.in(key), "Waiter should eventually get a value");
                done.countDown();
            });
        }

        assertTrue(started.await(2, TimeUnit.SECONDS));
        Thread.sleep(100);

        // Warmup: create the emptying scenario.
        lsp.out(key, "warmup");
        assertEquals("warmup", lsp.inp(key));

        // Feed values.
        for (int i = 0; i < waiters; i++) {
            lsp.out(key, "v" + i);
        }

        assertTrue(done.await(5, TimeUnit.SECONDS));

        // Now the key should be removable (no queue, no waiters). Give a small grace window.
        // Any correct housekeeping should remove it promptly.
        for (int i = 0; i < 50 && lsp.getKeySet().contains(key); i++) {
            Thread.sleep(10);
        }
        assertFalse(lsp.getKeySet().contains(key),
          "KeyEntry should be removed once queue is empty and no waiters remain");
    }

    @Test
    public void testGcDoesNotLoseExpirableKeysUnderConcurrentRegistration() throws Exception {
        final LSpace<String, String> lsp = new LSpace<>();
        try {
            final int keyCount = 200;
            final String[] keys = new String[keyCount];
            for (int i = 0; i < keyCount; i++) {
                keys[i] = "expKey-" + i;
            }

            // Run GC concurrently while we register expirables at a high rate.
            final long runMillis = 750; // keep test fast but high enough to hit the window
            final long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(runMillis);

            final CountDownLatch started = new CountDownLatch(2);
            final CountDownLatch done = new CountDownLatch(2);

            Thread gcThread = Thread.startVirtualThread(() -> {
                started.countDown();
                try {
                    while (System.nanoTime() < deadline) {
                        lsp.gc();
                        // Encourage interleavings.
                        Thread.yield();
                    }
                } finally {
                    done.countDown();
                }
            });

            Thread producerThread = Thread.startVirtualThread(() -> {
                started.countDown();
                int i = 0;
                try {
                    while (System.nanoTime() < deadline) {
                        // Very short timeout => value will expire quickly and should be removed by GC.
                        // Spread registrations across many keys.
                        String k = keys[i++ % keyCount];
                        lsp.out(k, "v", 5); // 5ms
                        if ((i & 0x3F) == 0) {
                            Thread.yield();
                        }
                    }
                } finally {
                    done.countDown();
                }
            });

            assertTrue(started.await(1, TimeUnit.SECONDS), "Worker threads must start");
            assertTrue(done.await(5, TimeUnit.SECONDS), "Worker threads must complete");

            // Let all expirables expire.
            Thread.sleep(50);

            // Now run several GC cycles to ensure all expired wrappers are removed and entries cleaned up.
            // IMPORTANT: Do not call rdp/inp on these keys; that could clean them up and mask the bug.
            for (int i = 0; i < 25; i++) {
                lsp.gc();
                Thread.sleep(5);
            }

            // After expiry + repeated GC, there should be no keys left.
            // If expirable keys were "lost" due to the copy+clear race, some entries can remain forever.
            assertEquals(0, lsp.getKeySet().size(),
              "All expired expirable entries should be removed by GC; " +
                "non-empty keyset indicates lost expirable-key tracking under concurrent registration");
        } finally {
            // With the lifecycle patch, this prevents the scheduled GC task from pinning the instance.
            lsp.close();
        }
    }
    public void testOperationsThrowAfterClose() {
        LSpace<String, String> sp = new LSpace<>();

        sp.close();

        // A representative sample of externally visible operations that must fail once closed.
        assertThrows(IllegalStateException.class, () -> sp.out("k", "v"));
        assertThrows(IllegalStateException.class, () -> sp.out("k", "v", 1000L));
        assertThrows(IllegalStateException.class, () -> sp.push("k", "v"));
        assertThrows(IllegalStateException.class, () -> sp.push("k", "v", 1000L));
        assertThrows(IllegalStateException.class, () -> sp.put("k", "v"));
        assertThrows(IllegalStateException.class, () -> sp.put("k", "v", 1000L));

        assertThrows(IllegalStateException.class, () -> sp.rdp("k"));
        assertThrows(IllegalStateException.class, () -> sp.inp("k"));
        assertThrows(IllegalStateException.class, () -> sp.in("k"));
        assertThrows(IllegalStateException.class, () -> sp.in("k", 10L));
        assertThrows(IllegalStateException.class, () -> sp.rd("k"));
        assertThrows(IllegalStateException.class, () -> sp.rd("k", 10L));
        assertThrows(IllegalStateException.class, () -> sp.nrd("k"));
        assertThrows(IllegalStateException.class, () -> sp.nrd("k", 10L));

        assertThrows(IllegalStateException.class, () -> sp.size("k"));
        assertThrows(IllegalStateException.class, sp::getKeySet);
        assertThrows(IllegalStateException.class, sp::isEmpty);
        assertThrows(IllegalStateException.class, sp::getKeysAsString);

        // Close must be idempotent.
        assertDoesNotThrow(sp::close);
    }
}
