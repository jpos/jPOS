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
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.Q2ConfigurationException;
import org.jpos.space.LocalSpace;
import org.jpos.space.TransientSpace;
import org.jpos.space.SpaceListener;

/**
 * QBean reading data from space and sending synchronously to a JMS Queue. 
 *
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 * @jmx:mbean description="JMS Q Sender"
 *      extends="org.jpos.q2.QBeanSupportMBean"
 */
public class JMSQSender extends QBeanSupport implements JMSQSenderMBean,SpaceListener,Runnable {
    private QueueConnectionFactory queueConnectionFactory = null;
    private QueueConnection queueConnection = null;
    private Queue queue = null;
    private QueueSession queueSession = null;
    private QueueSender sender = null;

    private String connectionFactory = null;
    private String queueName = null;
    private String username = null;
    private String password = null;
    private String spaceName = null;
    private String spaceKey = null;

    private LocalSpace space = null;

    private int jmsState = STOPPED;
    private long jmsRetryInterval = 5000;

    private class ExcListener implements ExceptionListener {
        public void onException (JMSException exception) {
            log.error (exception);
            restart ();
        }
    }

    public JMSQSender () {
        super ();
    }

    protected void startService () throws Exception {
        if (connectionFactory == null)
            throw new Q2ConfigurationException ("ConnectionFactory not configured");
        if (queueName == null)
            throw new Q2ConfigurationException ("Queue name not specified");
        if ((spaceName == null) || (spaceKey == null))
            throw new Q2ConfigurationException ("Space Name or Key not specified");
        space = TransientSpace.getSpace (spaceName);
        try {
            connect2JMS ();
            listen2Space ();
            jmsState = STARTED;
            log.info ("JMS connection established");
        } catch (Exception e) {
            log.warn ("JMS connection problems! Misconfigured? Retrying anyway", e);
            new Thread (this).start ();
        }
    }

    protected void stopService () {
        deaf2Space ();
        disconnectJMS ();
    }

    public void notify (Object k, Object value) {
        Object obj = space.inp (k);
        if (obj != null)
            try {
                sender.send ((Message) obj);
            } catch (JMSException e) {
                log.error (e);
                restart ();
                space.out (k, obj);
            }
    }

    public void run () {
        log.info ("run");
        while (jmsState != STARTED) {
            relax (jmsRetryInterval);
            try {
                connect2JMS ();
                jmsState = STARTED;
                listen2Space ();
                log.info ("JMS connection established");
            } catch (Exception e) {
                log.warn ("Still no JMS connection! Retrying", e);
            }
        }
    }

    private void connect2JMS() throws Exception {
        log.info ("connect2JMS");
        queueConnectionFactory = Utilities.getQueueConnectionFactory (connectionFactory);
        if (username == null)
            queueConnection = Utilities.getQueueConnection (queueConnectionFactory);
        else
            queueConnection = Utilities.getQueueConnection (queueConnectionFactory, username, password);
        ExcListener el = new ExcListener ();
        queueConnection.setExceptionListener (el);
        queue = Utilities.getQueue (queueName);
        queueSession = queueConnection.createQueueSession (false, Session.AUTO_ACKNOWLEDGE);
        sender = queueSession.createSender (queue);
        queueConnection.start ();
    }

    private void disconnectJMS() {
        log.info ("disconnectJMS");
        try {
            queueConnection.stop ();
        } catch (JMSException e) { }
        try {
            sender.close ();
        } catch (JMSException e) { }
        try {
            queueSession.close ();
        } catch (JMSException e) { }
        try {
            queueConnection.close ();
        } catch (JMSException e) {
            log.error (e);
        } finally {
            sender = null;
            queueSession = null;
            queueConnection = null;
            jmsState = STOPPED;
        }
    }

    private void listen2Space() {
        log.info ("listen2Space");
        space.addListener (spaceKey, this);
    }

    private void deaf2Space() {
        log.info ("deaf2Space");
        space.removeListener (spaceKey, this);
    }

    private void relax (long sleep) {
        try {
            Thread.sleep (sleep);
        } catch (InterruptedException e) { }
    }

    private void restart () {
        deaf2Space ();
        disconnectJMS ();
        new Thread (this).start ();
    }

    /**
     * @jmx:managed-attribute description="Queue Connection Factory"
     */
    public String getConnectionFactory () {
        return connectionFactory;
    }
    
    /**
     * @jmx:managed-attribute description="Queue Connection Factory"
     */
    public synchronized void setConnectionFactory (String connectionFactory) {
        this.connectionFactory = connectionFactory;
        setAttr (getAttrs(), "connectionFactory", connectionFactory);
        setModified (true);
        if (jmsState == STARTED)
            restart ();
    }

    /**
     * @jmx:managed-attribute description="Queue Name"
     */
    public String getQueueName () {
        return queueName;
    }

    /**
     * @jmx:managed-attribute description="Queue Name"
     */
    public synchronized void setQueueName (String queueName) {
        this.queueName = queueName;
        setAttr (getAttrs(), "queueName", queueName);
        setModified (true);
        if (jmsState == STARTED)
            restart ();
    }

    /**
     * @jmx:managed-attribute description="Username"
     */
    public String getUsername () {
        return username;
    }

    /**
     * @jmx:managed-attribute description="Username"
     */
    public synchronized void setUsername (String username) {
        this.username = username;
        setAttr (getAttrs(), "username", username);
        setModified (true);
        if (jmsState == STARTED)
            restart ();
    }

    /**
     * @jmx:managed-attribute description="Password"
     */
    public synchronized void setPassword (String password) {
        this.password = password;
        setAttr (getAttrs(), "password", password);
        setModified (true);
        if (jmsState == STARTED)
            restart ();
    }

    /**
     * @jmx:managed-attribute description="Password"
     */
    public String getPassword () {
        return password;
    }
        
    /** 
     * @jmx:managed-attribute description="Out Space"
     */
    public String getSpace () {
        return spaceName;
    }

    /**
     * @jmx:managed-attribute description="Out Space"
     */
    public synchronized void setSpace (String spaceName) {
        this.spaceName = spaceName;
        setAttr (getAttrs(), "space", spaceName);
        setModified (true);
        if (jmsState == STARTED) {
            deaf2Space ();
            space = TransientSpace.getSpace (spaceName);
            listen2Space ();
        } else 
            space = TransientSpace.getSpace (spaceName);
    }

    /**
     * @jmx:managed-attribute description="Space Key"
     */
    public synchronized void setSpaceKey (String spaceKey) {
        if (jmsState == STARTED)
            deaf2Space ();
        this.spaceKey = spaceKey;
        if (jmsState == STARTED)
            listen2Space ();
        setAttr (getAttrs(), "spaceKey", spaceKey);
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="Space Key"
     */
    public String getSpaceKey () {
        return spaceKey;
    }

    /**
     * @jmx:managed-attribute description="JMS connection retry interval"
     */
    public long getJmsRetryInterval () {
        return jmsRetryInterval;
    }

    /**
     * @jmx:managed-attribute description="JMS connection retry interval"
     */
    public synchronized void setJmsRetryInterval (long retryInterval) {
        jmsRetryInterval = retryInterval;
        setAttr (getAttrs(), "jmsRetryInterval", new Long(jmsRetryInterval));
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="JMS state"
     */
    public int getJmsState () {
        return jmsState;
    }

    /**
     * @jmx:managed-attribute description "JMS state As String"
     */
    public String getJmsStateAsString () {
        return jmsState >= 0 ? stateString[jmsState] : "Unknown";
    }
}
