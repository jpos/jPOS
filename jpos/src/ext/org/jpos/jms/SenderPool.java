/*
 * 
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
package org.jpos.jms;

import javax.jms.*;

import java.io.Serializable;

import org.jpos.util.BlockingQueue;
import org.jpos.util.BlockingQueue.Closed;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.core.Configuration;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;

/**
 * JMS Queue Sender pool.
 * <p>
 * It is assumed that all senders will connect to the same queue
 * over the same connection using different sessions
 *
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 */
public class SenderPool extends SimpleLogSource implements Configurable {
    private BlockingQueue pool = new BlockingQueue ();
    private int sessionCount = 0;
    private QueueConnection queueConnection;
    private int maxSessions;
    private String queueName;
    private boolean transactional;
    private int ackMode;

    public static class SenderPoolEmptyException extends RuntimeException {
        public SenderPoolEmptyException () {
            super ("Q Sender Pool empty, add a sender before sending");
        }
    }
    public static class SenderPoolFullException extends RuntimeException {
        public SenderPoolFullException () {
            super ("Maximum number of Senders reached in pool");
        }
    }

    private class PooledSender extends SimpleLogSource implements Runnable {
        private QueueSession queueSession;
        private Queue queue;
        private QueueSender queueSender;
        private String name;

        public PooledSender (Queue queue) throws JMSException {
            name = "PooledSender-" + (sessionCount++);
            queueSession = queueConnection.createQueueSession (transactional, ackMode);
            if (queue == null)
                this.queue = queueSession.createQueue (queueName);
            else
                this.queue = queue;
            queueSender = queueSession.createSender (this.queue);
        }
        public void run () {
            try {
                while (pool.ready ()) {
                    Object message = pool.dequeue ();
                    LogEvent evt = new LogEvent (this, name);
                    evt.addMessage ("Got iso message to send");
                    ObjectMessage objMsg = queueSession.createObjectMessage ();
                    objMsg.setObject ( (Serializable) message);
                    queueSender.send (objMsg);
                    if (transactional)
                        queueSession.commit ();
                    Logger.log (evt);
                }
            } catch (InterruptedException e) {
            } catch (Closed e) {
            } catch (JMSException e) {
            }
            sessionCount--;
            if (queueSender != null)
                try {
                    queueSender.close ();
                } catch (JMSException e) {
                }
            if (queueSession != null)
                try {
                    queueSession.close ();
                } catch (JMSException e) {
                }
        }
    }

    public SenderPool (QueueConnection queueConnection) {
        this.queueConnection = queueConnection;
    }

    public void addSender (Queue queue) throws JMSException {
        if (sessionCount < maxSessions) {
            PooledSender ps = new PooledSender (queue);
            new Thread(ps).start();
            ps.setLogger (getLogger(), getRealm());
        }
        else 
            throw new SenderPoolFullException();
    }

    public synchronized void send (Object obj) throws Closed {
        if (!pool.ready())
            throw new Closed ();
        if (sessionCount > 0) 
            pool.enqueue (obj);
        else
            throw new SenderPoolEmptyException ();
    }

    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        maxSessions = cfg.getInt ("maxSenders",10);
        transactional = cfg.getBoolean ("transactional",false);
        ackMode = cfg.getInt ("ackMode", Session.AUTO_ACKNOWLEDGE);
    }
}

