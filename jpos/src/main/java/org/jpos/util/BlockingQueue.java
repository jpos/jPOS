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

import java.util.LinkedList;

/**
 * implements a blocking queue 
 * @see ThreadPool
 * @since 1.1
 */
@SuppressWarnings("unchecked")
public class BlockingQueue {
    /** Creates an empty, open queue. */
    public BlockingQueue() {}
    private LinkedList queue = new LinkedList();
    private boolean closed = false;
    private int consumers = 0;

    /** Thrown by queue operations after the queue has been closed. */
    public static class Closed extends RuntimeException {

        private static final long serialVersionUID = 3404885702116373450L;

        /** Constructs a new {@code Closed} exception with a default message. */
        public Closed() {
            super ("queue-closed");
        }
    }

    /**
     * Appends an object to the tail of the queue and wakes one waiter.
     *
     * @param o the object to enqueue
     * @throws Closed if the queue has been closed
     */
    public synchronized void enqueue (Object o) throws Closed {
        if (closed)
            throw new Closed();
        queue.addLast (o);
        notify();
    }
    /**
     * Inserts an object at the head of the queue and wakes one waiter.
     *
     * @param o the object to requeue
     * @throws Closed if the queue has been closed
     */
    public synchronized void requeue (Object o) throws Closed {
        if (closed)
            throw new Closed();
        queue.addFirst (o);
        notify();
    }

    /**
     * Removes and returns the head of the queue, blocking until an object is available.
     *
     * @return the object at the head of the queue
     * @throws InterruptedException if the thread is interrupted while waiting
     * @throws Closed if the queue is closed while waiting
     */
    public synchronized Object dequeue()
        throws InterruptedException, Closed
    {
        consumers++;
        try {
            while (queue.size() == 0) {
                wait();
                if (closed)
                    throw new Closed();
            }
        } finally {
            consumers--;
        }
        return queue.removeFirst();
    }

    /**
     * Removes and returns the head of the queue, blocking up to {@code timeout} ms.
     *
     * @param timeout maximum wait in milliseconds; {@code 0} blocks indefinitely
     * @return the object at the head of the queue, or {@code null} if the wait elapsed
     * @throws InterruptedException if the thread is interrupted while waiting
     * @throws Closed if the queue is closed while waiting
     */
    public synchronized Object dequeue (long timeout)
        throws InterruptedException, Closed
    {
        if (timeout == 0)
            return dequeue ();

        consumers++;
        long maxTime = System.currentTimeMillis() + timeout;
        try {
            while (queue.size() == 0 && System.currentTimeMillis() < maxTime) {
                if (timeout > 0L)
                    wait (timeout);
                if (closed)
                    throw new Closed();
            }
        } finally {
            consumers--;
        }
        return queue.size() > 0 ? queue.removeFirst() : null;
    }
    /**
     * Closes the queue and wakes all waiting consumers, who will then receive {@link Closed}.
     */
    public synchronized void close() {
        closed = true;
        notifyAll();
    }
    /**
     * Returns the number of consumers currently waiting on this queue.
     *
     * @return the live consumer count
     */
    public synchronized int consumerCount() {
        return consumers;
    }

    /**
     * Returns the difference between queued items and waiting consumers.
     *
     * @return queue size minus consumer count (negative when consumers exceed items)
     */
    public synchronized int consumerDeficit() {
        return queue.size() - consumers;
    }

    /**
     * Indicates whether the queue is open for new operations.
     *
     * @return {@code true} if the queue has not been closed
     */
    public synchronized boolean ready() {
        return !closed;
    }
    /**
     * Returns the number of items currently in the queue.
     *
     * @return the queue size
     */
    public synchronized int pending() {
        return queue.size();
    }
    /**
     * Returns the underlying list backing this queue.
     *
     * @return the internal list (live reference, not a copy)
     */
    public LinkedList getQueue () {
        return queue;
    }
    /**
     * Replaces the underlying list backing this queue.
     *
     * @param queue the list to use as the new backing store
     */
    public void setQueue (LinkedList queue) {
        this.queue = queue;
    }
}

