/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedList;

import org.junit.jupiter.api.Test;

public class BlockingQueueTest {

    @Test
    public void testClose() {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.close();
        assertFalse(blockingQueue.ready(), "blockingQueue.ready()");
    }

    @Test
    public void testClosedConstructor() {
        BlockingQueue.Closed closed = new BlockingQueue.Closed();
        assertEquals("queue-closed", closed.getMessage(), "closed.getMessage()");
    }

    @Test
    public void testConstructor() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
        assertTrue(blockingQueue.ready(), "blockingQueue.ready()");
        assertEquals(0, blockingQueue.getQueue().size(), "blockingQueue.getQueue().size()");
    }

    @Test
    public void testConsumerCount() throws Throwable {
        int result = new BlockingQueue().consumerCount();
        assertEquals(0, result, "result");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDequeue() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        LinkedList queue = new LinkedList<Object>();
        Boolean boolean2 = Boolean.TRUE;
        queue.add(true);
        queue.add("");
        blockingQueue.setQueue(queue);
        Boolean result = (Boolean) blockingQueue.dequeue(-1L);
        assertSame(queue, blockingQueue.getQueue(), "blockingQueue.getQueue()");
        assertFalse(boolean2.equals(blockingQueue.getQueue().get(0)), "blockingQueue.getQueue().get(0) had boolean2 removed");
        assertSame(true, result, "result");
        assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDequeue10() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        LinkedList queue = new LinkedList();
        Object obj = new Object();
        queue.add(obj);
        BlockingQueue blockingQueue2 = new BlockingQueue();
        blockingQueue2.setQueue(queue);
        blockingQueue2.dequeue();
        LinkedList queue2 = new LinkedList();
        queue2.add("testString".toCharArray());
        queue2.add(obj);
        blockingQueue.setQueue(queue2);
        blockingQueue.dequeue(100L);
        blockingQueue.dequeue(-1L);
        Integer o = Integer.valueOf(12);
        blockingQueue.requeue(o);
        Integer result = (Integer) blockingQueue.dequeue();
        assertSame(queue2, blockingQueue.getQueue(), "blockingQueue.getQueue()");
        assertFalse(blockingQueue.getQueue().contains(o), "blockingQueue.getQueue().contains(o)");
        assertSame(o, result, "result");
        assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDequeue2() throws Throwable {
        LinkedList queue = new LinkedList();
        Boolean boolean2 = Boolean.FALSE;
        queue.add(false);
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.setQueue(queue);
        Boolean result = (Boolean) blockingQueue.dequeue();
        assertSame(queue, blockingQueue.getQueue(), "blockingQueue.getQueue()");
        assertFalse(blockingQueue.getQueue().contains(false), "blockingQueue.getQueue().contains(boolean2)");
        assertSame(false, result, "result");
        assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
    }

    @Test
    public void testDequeue3() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        LinkedList<Object> queue = new LinkedList<Object>();
        Integer integer = Integer.valueOf(100);
        queue.add(integer);
        queue.add("testString");
        blockingQueue.setQueue(queue);
        Integer result = (Integer) blockingQueue.dequeue(0L);
        assertSame(queue, blockingQueue.getQueue(), "blockingQueue.getQueue()");
        assertFalse(integer.equals(blockingQueue.getQueue().get(0)), "blockingQueue.getQueue().get(0) had integer removed");
        assertSame(integer, result, "result");
        assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDequeue4() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        LinkedList queue = new LinkedList();
        Object obj = new Object();
        queue.add(obj);
        BlockingQueue blockingQueue2 = new BlockingQueue();
        blockingQueue2.setQueue(queue);
        blockingQueue2.dequeue();
        LinkedList queue2 = new LinkedList();
        queue2.add("testString".toCharArray());
        queue2.add(obj);
        blockingQueue.setQueue(queue2);
        blockingQueue.dequeue(100L);
        Object result = blockingQueue.dequeue(-1L);
        assertSame(queue2, blockingQueue.getQueue(), "blockingQueue.getQueue()");
        assertFalse(blockingQueue.getQueue().contains(obj), "blockingQueue.getQueue().contains(obj)");
        assertSame(obj, result, "result");
        assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
    }

    @Test
    public void testDequeue5() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        Object result = blockingQueue.dequeue(2L);
        assertNull(result, "result");
        assertEquals(0, blockingQueue.getQueue().size(), "blockingQueue.getQueue().size()");
        assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
    }

    @Test
    public void testDequeue6() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        Integer o = Integer.valueOf(0);
        blockingQueue.enqueue(o);
        Integer result = (Integer) blockingQueue.dequeue(100L);
        assertEquals(0, blockingQueue.getQueue().size(), "blockingQueue.getQueue().size()");
        assertFalse(blockingQueue.getQueue().contains(o), "blockingQueue.getQueue().contains(o)");
        assertSame(o, result, "result");
        assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDequeue7() throws Throwable {
        LinkedList queue = new LinkedList();
        Boolean boolean2 = Boolean.FALSE;
        queue.add(false);
        queue.add(Boolean.FALSE);
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.setQueue(queue);
        Boolean result = (Boolean) blockingQueue.dequeue(-1L);
        assertSame(queue, blockingQueue.getQueue(), "blockingQueue.getQueue()");
        assertSame(false, result, "result");
        assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDequeue8() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        LinkedList queue = new LinkedList();
        Integer integer = Integer.valueOf(100);
        queue.add(integer);
        blockingQueue.setQueue(queue);
        Integer result = (Integer) blockingQueue.dequeue(0L);
        assertSame(queue, blockingQueue.getQueue(), "blockingQueue.getQueue()");
        assertFalse(blockingQueue.getQueue().contains(integer), "blockingQueue.getQueue().contains(integer)");
        assertSame(integer, result, "result");
        assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
    }

    @Test
    public void testDequeue9() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.enqueue("");
        blockingQueue.enqueue("tr;e");
        blockingQueue.requeue("");
        blockingQueue.dequeue(0L);
        blockingQueue.dequeue(0L);
        blockingQueue.enqueue("");
        blockingQueue.dequeue(0L);
        String result = (String) blockingQueue.dequeue();
        assertEquals(0, blockingQueue.getQueue().size(), "blockingQueue.getQueue().size()");
        assertFalse(blockingQueue.getQueue().contains(""), "blockingQueue.getQueue().contains(\"\")");
        assertEquals("", result, "result");
        assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
    }

    @Test
    public void testDequeueThrowsNullPointerException() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.setQueue(null);
        try {
            blockingQueue.dequeue(0L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.LinkedList.size()\" because \"this.queue\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(blockingQueue.getQueue(), "blockingQueue.getQueue()");
            assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
        }
    }

    @Test
    public void testDequeueThrowsNullPointerException1() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.setQueue(null);
        try {
            blockingQueue.dequeue(-1L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.LinkedList.size()\" because \"this.queue\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(blockingQueue.getQueue(), "blockingQueue.getQueue()");
            assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
        }
    }

    @Test
    public void testDequeueThrowsNullPointerException2() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.setQueue(null);
        try {
            blockingQueue.dequeue();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.LinkedList.size()\" because \"this.queue\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(0, blockingQueue.consumerCount(), "blockingQueue.consumerCount()");
            assertNull(blockingQueue.getQueue(), "blockingQueue.getQueue()");
        }
    }

    @Test
    public void testEnqueue() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.enqueue("testString");
        assertEquals(1, blockingQueue.getQueue().size(), "blockingQueue.getQueue().size()");
        assertEquals("testString", blockingQueue.getQueue().get(0), "blockingQueue.getQueue().get(0)");
    }

    @Test
    public void testEnqueueThrowsClosed() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.close();
        try {
            blockingQueue.enqueue(Integer.valueOf(-32));
            fail("Expected Closed to be thrown");
        } catch (BlockingQueue.Closed ex) {
            assertEquals("queue-closed", ex.getMessage(), "ex.getMessage()");
            assertEquals(0, blockingQueue.getQueue().size(), "blockingQueue.getQueue().size()");
        }
    }

    @Test
    public void testEnqueueThrowsNullPointerException() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.setQueue(null);
        try {
            blockingQueue.enqueue("");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.LinkedList.addLast(Object)\" because \"this.queue\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(blockingQueue.getQueue(), "blockingQueue.getQueue()");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPending() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        LinkedList queue = new LinkedList();
        queue.add(Integer.valueOf(100));
        blockingQueue.setQueue(queue);
        int result = blockingQueue.pending();
        assertEquals(1, result, "result");
    }

    @Test
    public void testPending1() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.dequeue(-2L);
        int result = blockingQueue.pending();
        assertEquals(0, result, "result");
    }

    @Test
    public void testPendingThrowsNullPointerException() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.setQueue(null);
        try {
            blockingQueue.pending();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.LinkedList.size()\" because \"this.queue\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReady() throws Throwable {
        boolean result = new BlockingQueue().ready();
        assertTrue(result, "result");
    }

    @Test
    public void testReady1() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.close();
        boolean result = blockingQueue.ready();
        assertFalse(result, "result");
    }

    @Test
    public void testRequeue() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.requeue("testString");
        assertEquals(1, blockingQueue.getQueue().size(), "blockingQueue.getQueue().size()");
        assertEquals("testString", blockingQueue.getQueue().get(0), "blockingQueue.getQueue().get(0)");
    }

    @Test
    public void testRequeueThrowsClosed() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.close();
        try {
            blockingQueue.requeue("");
            fail("Expected Closed to be thrown");
        } catch (BlockingQueue.Closed ex) {
            assertEquals("queue-closed", ex.getMessage(), "ex.getMessage()");
            assertEquals(0, blockingQueue.getQueue().size(), "blockingQueue.getQueue().size()");
        }
    }

    @Test
    public void testRequeueThrowsNullPointerException() throws Throwable {
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.setQueue(null);
        try {
            blockingQueue.requeue(Integer.valueOf(0));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.LinkedList.addFirst(Object)\" because \"this.queue\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(blockingQueue.getQueue(), "blockingQueue.getQueue()");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetQueue() throws Throwable {
        LinkedList queue = new LinkedList();
        BlockingQueue blockingQueue = new BlockingQueue();
        blockingQueue.setQueue(queue);
        assertSame(queue, blockingQueue.getQueue(), "blockingQueue.getQueue()");
    }
}
