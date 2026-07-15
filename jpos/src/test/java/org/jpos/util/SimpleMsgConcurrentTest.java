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

package org.jpos.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

/**
 * Test for concurrent modification exceptions in SimpleMsg.
 *
 * <p>SimpleMsg.dump() iterates over Collection derived from msgContent (line 112)
 * while setMsgContent() may modify the msgContent from another thread,
 * causing ConcurrentModificationException.
 */
public class SimpleMsgConcurrentTest {

    private static final int ITERATIONS = 2000;
    private static final int THREADS = 4;

    /**
     * SimpleMsg.dump() iterates the Collection derived from msgContent.
     * When msgContent is a Collection (or becomes one via setMsgContent),
     * the for loop at line 112 can throw ConcurrentModificationException
     * if another thread calls setMsgContent with a new Collection.
     */
    @Test
    public void testSimpleMsgConcurrentDumpAndSetMsgContent() throws Throwable {
        final SimpleMsg msg = new SimpleMsg("test", "name", new ArrayList<>());
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Thread[] writers = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int threadNum = i;
            writers[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        List<String> content = new ArrayList<>();
                        content.add("item" + (threadNum * 1000 + j));
                        msg.setMsgContent(content);
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

        assertFalse(failed.get(), "ConcurrentModificationException was thrown during concurrent dump and setMsgContent");
    }

    /**
     * SimpleMsg with a shared mutable List that is modified while dump iterates it.
     * This simulates the actual problematic case where msgContent is a Collection
     * that is shared and modified by multiple threads.
     */
    @Test
    public void testSimpleMsgConcurrentDumpWithSharedCollection() throws Throwable {
        final List<String> sharedList = new ArrayList<>();
        sharedList.add("initial");
        final SimpleMsg msg = new SimpleMsg("test", "shared", sharedList);

        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean(false);

        Thread modifier = new Thread(() -> {
            try {
                startLatch.await();
                for (int j = 0; j < ITERATIONS; j++) {
                    synchronized (sharedList) {
                        sharedList.add("item" + j);
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
        modifier.join();
        dumper.join();

        assertFalse(failed.get(), "ConcurrentModificationException was thrown during concurrent dump and list modification");
    }
}