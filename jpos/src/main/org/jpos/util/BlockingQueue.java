package uy.com.cs.jpos.util;

import java.util.LinkedList;

/*
 * $Id$
 *
 * $Log$
 * Revision 1.1  2000/01/11 17:16:51  apr
 * Added ThreadPool support
 *
 */

/**
 * implements a blocking queue 
 * @see ThreadPool
 * @see http://www.javaworld.com/javaworld/jw-05-1999/jw-05-toolbox.html
 * @since 1.1
 */
public class BlockingQueue {
    private LinkedList queue = new LinkedList();
    private boolean closed = false;
    private int consumers = 0;

    public static class Closed extends RuntimeException {
	public Closed() {
	    super ("queue is closed");
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
