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

import java.util.LinkedList;

/**
 * implements a blocking queue 
 * @see ThreadPool
 * @since 1.1
 */
@SuppressWarnings("unchecked")
public class BlockingQueue {
    private LinkedList queue = new LinkedList();
    private boolean closed = false;
    private int consumers = 0;

    public static class Closed extends RuntimeException {

        private static final long serialVersionUID = 3404885702116373450L;

        public Closed() {
            super ("queue-closed");
        }
    }

    public synchronized void enqueue (Object o) throws Closed {
        if (closed)
            throw new Closed();
        queue.addLast (o);
        notify();
    }
    public synchronized void requeue (Object o) throws Closed {
        if (closed)
            throw new Closed();
        queue.addFirst (o);
        notify();
    }

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

    public synchronized Object dequeue (long timeout)
        throws InterruptedException, Closed
    {
        if (timeout == 0)
            return dequeue ();

        consumers++;
        long maxTime = System.currentTimeMillis() + timeout;
        try {
            while (queue.size() == 0 && System.currentTimeMillis() < maxTime) {
                wait (timeout);
                if (closed)
                    throw new Closed();
            }
        } finally {
            consumers--;
        }
        return queue.size() > 0 ? queue.removeFirst() : null;
    }
    public synchronized void close() {
        closed = true;
        notifyAll();
    }
    public synchronized int consumerCount() {
        return consumers;
    }

    public synchronized int consumerDeficit() {
        return queue.size() - consumers;
    }
    
    public synchronized boolean ready() {
        return !closed;
    }
    public synchronized int pending() {
        return queue.size();
    }
    public LinkedList getQueue () {
        return queue;
    }
    public void setQueue (LinkedList queue) {
        this.queue = queue;
    }
}

