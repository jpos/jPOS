package org.jpos.util;

import java.util.LinkedList;

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.3  2000/02/02 00:07:27  apr
 * sync sync sync
 *
 * Revision 1.2  2000/02/02 00:06:27  apr
 * CVS sync
 *
 * Revision 1.1  2000/01/11 17:16:51  apr
 * Added ThreadPool support
 *
 */

/**
 * implements a blocking queue 
 * @see ThreadPool
 * @since 1.1
 */
public class BlockingQueue {
    private LinkedList queue = new LinkedList();
    private boolean closed = false;
    private int consumers = 0;

    public static class Closed extends RuntimeException {
	public Closed() {
	    super ("queue closed");
	}
    }

    public synchronized void enqueue (Object o) throws Closed {
	if (closed)
	    throw new Closed();
	queue.addLast (o);
	notify();
    }

    public synchronized Object dequeue()
	throws InterruptedException, Closed
    {
	consumers++;
	while (queue.size() == 0) {
	    wait();
	    if (closed)
		throw new Closed();
	}
	consumers--;
	return queue.removeFirst();
    }
    public synchronized void close() {
	closed = true;
	notifyAll();
    }
    public synchronized int consumerCount() {
	return consumers;
    }
    public synchronized boolean ready() {
	return !closed;
    }
    public synchronized int pending() {
	return queue.size();
    }
}
