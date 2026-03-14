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
/**
 * A simple thread-safe blocking queue supporting enqueue, dequeue with optional timeout, and cooperative close.
 * @deprecated Use standard {@link java.util.concurrent} classes instead.
 */
public class BlockingQueue {
    private LinkedList queue = new LinkedList();
    private boolean closed = false;
    private int consumers = 0;

    /** Default constructor. */
    public BlockingQueue() { }
    /** Exception thrown when an operation is attempted on a closed BlockingQueue. */
    public static class Closed extends RuntimeException {

        private static final long serialVersionUID = 3404885702116373450L;

        /** Constructs a Closed exception with a default message. */
        public Closed() {
            super ("queue-closed");
        }
    }

    /** Adds an object to the tail of the queue.
     * @param o the object to enqueue
     * @throws Closed if the queue has been closed
     */
    public synchronized void enqueue (Object o) throws Closed {
        if (closed)
            throw new Closed();
        queue.addLast (o);
        notify();
    }
    /** Adds an object to the head of the queue (priority re-queue).
     * @param o the object to re-queue
     * @throws Closed if the queue has been closed
     */
    public synchronized void requeue (Object o) throws Closed {
        if (closed)
            throw new Closed();
        queue.addFirst (o);
        notify();
    }

    /** Removes and returns the head of the queue, blocking until one is available.
     * @return the dequeued object
     * @throws Closed if the queue is closed
     * @throws InterruptedException if the thread is interrupted
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

    /** Removes and returns the head of the queue, waiting at most {@code timeout} ms.
     * @param timeout maximum wait time in milliseconds
     * @return the dequeued object, or null on timeout
     * @throws Closed if the queue is closed
     * @throws InterruptedException if the thread is interrupted
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
    /** Closes the queue; pending consumers are released with a {@link Closed} exception. */
    public synchronized void close() {
        closed = true;
        notifyAll();
    }
    /**
     * Returns the number of threads currently waiting to dequeue.
     * @return consumer count
     */
    public synchronized int consumerCount() {
        return consumers;
    }

    /**
     * Returns the number of additional consumers needed to drain the current backlog.
     * @return consumer deficit
     */
    public synchronized int consumerDeficit() {
        return queue.size() - consumers;
    }
    
    /**
     * Returns true if there are items ready to be dequeued.
     * @return true if items are pending
     */
    public synchronized boolean ready() {
        return !closed;
    }
    /**
     * Returns the number of items waiting in the queue.
     * @return pending item count
     */
    public synchronized int pending() {
        return queue.size();
    }
    /**
     * Returns the underlying linked list.
     * @return the backing linked list
     */
    public LinkedList getQueue () {
        return queue;
    }
    /**
     * Sets the underlying linked list.
     * @param queue the backing linked list
     */
    public void setQueue (LinkedList queue) {
        this.queue = queue;
    }
}

