/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.util;

import java.util.LinkedList;

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
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
