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

package org.jpos.space;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MDBSpaceTest {
    @TempDir
    Path tempDir;

    private MDBSpace<String, String> newSpace(String slug) {
        String name = slug + "-" + System.nanoTime();
        Path dbFile = tempDir.resolve(name + ".db");
        return MDBSpace.getSpace(name, dbFile.toString());
    }

    @Test
    void testBasicQueueOperations() {
        try (MDBSpace<String, String> sp = newSpace("basic")) {
            sp.out("key", "A");
            sp.out("key", "B");

            assertEquals("A", sp.rdp("key"));
            assertEquals("A", sp.inp("key"));
            assertEquals("B", sp.in("key", 1000));
            assertNull(sp.rdp("key"));
            assertNull(sp.inp("key"));
        }
    }

    @Test
    void testPushAndPut() {
        try (MDBSpace<String, String> sp = newSpace("push-put")) {
            sp.out("key", "1");
            sp.push("key", "2");
            assertEquals("2", sp.in("key", 1000));
            assertEquals(1, sp.size("key"));
            assertEquals("1", sp.in("key", 1000));

            sp.out("key", "old");
            sp.out("key", "older");
            sp.put("key", "fresh");
            assertEquals("fresh", sp.in("key"));
            assertNull(sp.rdp("key"));
        }
    }

    @Test
    void testTemplateMatching() {
        try (MDBSpace<String, String> sp = newSpace("template")) {
            String key = "template-key";
            sp.out(key, "alpha");
            sp.out(key, "beta");
            sp.out(key, "gamma");

            ObjectTemplate tmpl = new ObjectTemplate(key, "beta");
            assertEquals("beta", sp.rdp(tmpl));
            assertEquals("beta", sp.inp(tmpl));
            assertNull(sp.rdp(tmpl));
            assertEquals("alpha", sp.inp(key));
            assertEquals("gamma", sp.inp(key));
        }
    }

    @Test
    void testExpirationAndGc() throws Exception {
        try (MDBSpace<String, String> sp = newSpace("expire")) {
            String key = "expire-key";
            sp.out(key, "short", 25);
            assertEquals("short", sp.rdp(key));
            Thread.sleep(60);
            sp.gc();
            assertNull(sp.rdp(key));
        }
    }

    @Test
    void testPersistenceAcrossRestart() {
        String name = "persist-" + UUID.randomUUID();
        Path db = tempDir.resolve(name + ".db");

        try (MDBSpace<String, String> first = MDBSpace.getSpace(name, db.toString())) {
            first.out("persist-key", "value1");
        }

        try (MDBSpace<String, String> second = MDBSpace.getSpace(name, db.toString())) {
            assertEquals("value1", second.rdp("persist-key"));
            assertEquals("value1", second.in("persist-key", 1000));
            assertNull(second.rdp("persist-key"));
        }
    }

    @Test
    void testListenersReceiveNotifications() throws Exception {
        try (MDBSpace<String, String> sp = newSpace("listener")) {
            CountDownLatch notified = new CountDownLatch(1);
            AtomicReference<Object> seen = new AtomicReference<>();
            sp.addListener("listen-key", (key, value) -> {
                seen.set(value);
                notified.countDown();
            });

            sp.out("listen-key", "ping");
            assertTrue(notified.await(1, TimeUnit.SECONDS));
            assertEquals("ping", seen.get());
        }
    }

    @Test
    void testExistAnyAndNrd() {
        try (MDBSpace<String, String> sp = newSpace("exist")) {
            String[] keys = {"a", "b", "c"};
            assertFalse(sp.existAny(keys));

            sp.out("b", "value");
            assertTrue(sp.existAny(keys));
            assertTrue(sp.existAny(keys, 100));

            assertEquals("value", sp.nrd("b", 1));
            assertEquals("value", sp.in("b", 1000));
            assertNull(sp.nrd("b", 10));
        }
    }

    @Test
    void testSpaceFactoryIntegration() throws Exception {
        String name = "factory-" + UUID.randomUUID();
        Path db = tempDir.resolve(name + ".db");
        String uri = String.format("mdb:%s:%s", name, db);

        Space<String, String> space = SpaceFactory.getSpace(uri);
        try (space) {
            assertTrue(space instanceof MDBSpace);
            space.out("factory-key", "data");
            assertEquals("data", space.in("factory-key", 1000));
        }
    }

    @Test
    void testCloseRejectsOperations() {
        MDBSpace<String, String> sp = newSpace("close");
        sp.close();

        assertThrows(IllegalStateException.class, () -> sp.out("k", "v"));
        assertThrows(IllegalStateException.class, () -> sp.in("k"));
        assertThrows(IllegalStateException.class, () -> sp.rd("k"));
        assertThrows(IllegalStateException.class, () -> sp.nrd("k"));
    }

    @Test
    void testNrdWaitsUntilQueueIsEmpty() throws Exception {
        try (MDBSpace<String, String> sp = newSpace("nrd")) {
            String key = "nrd-key";
            sp.out(key, "one");

            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch finished = new CountDownLatch(1);

            Thread waiter = Thread.startVirtualThread(() -> {
                started.countDown();
                sp.nrd(key);
                finished.countDown();
            });

            assertTrue(started.await(1, TimeUnit.SECONDS));
            Thread.sleep(50);
            assertEquals(1, finished.getCount());

            sp.inp(key);
            assertTrue(finished.await(1, TimeUnit.SECONDS));
            waiter.join(Duration.ofSeconds(1).toMillis());
        }
    }
}
