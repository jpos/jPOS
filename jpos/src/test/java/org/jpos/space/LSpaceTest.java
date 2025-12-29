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
        sp = new LSpace<String, Object>();
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
                            Thread.yield();
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
                            Thread.yield();
                    }
                } finally {
                    allDone.countDown();
                }
            });
        }

        // Wait for all operations to complete
        assertTrue(allDone.await(30, TimeUnit.SECONDS), "All operations should complete");

        // Verify space is empty
        assertEquals(0, lsp.getKeySet().size(), "Space should be empty after all ops");
    }

    /**
     * Test that memory is properly cleaned up when entries are removed.
     */
    @Test
    public void testMemoryCleanup() {
        final LSpace<String, Integer> lsp = new LSpace<>();

        // Create and remove many entries
        for (int i = 0; i < 1000; i++) {
            lsp.out("key" + i, i);
            lsp.in("key" + i);
        }

        // Force GC
        lsp.gc();

        // Verify internal maps are clean
        assertEquals(0, lsp.getKeySet().size(), "No keys should remain");
        assertTrue(lsp.isEmpty(), "Space should be empty");
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
}
