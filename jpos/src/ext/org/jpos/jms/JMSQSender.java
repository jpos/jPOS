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
public class JMSQSender extends QBeanSupport implements JMSQSenderMBean,SpaceListener {
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
        try {
            space = TransientSpace.getSpace (spaceName);
            queueConnectionFactory = Utilities.getQueueConnectionFactory (connectionFactory);
            if (username == null)
                queueConnection = Utilities.getQueueConnection (queueConnectionFactory);
            else
                queueConnection = Utilities.getQueueConnection (queueConnectionFactory, username, password);
            queue = Utilities.getQueue (queueName);
            queueSession = queueConnection.createQueueSession (false, Session.AUTO_ACKNOWLEDGE);
            sender = queueSession.createSender (queue);
            queueConnection.start ();
            listen2Space ();
        } catch (Exception e) {
            throw new Q2ConfigurationException ("Config failure", e);
        }
    }

    protected void stopService () {
        try {
            deaf2Space ();
            queueConnection.stop ();
            sender.close ();
            sender = null;
            queueSession.close ();
            queueSession = null;
            queueConnection.close ();
            queueConnection = null;
        } catch (JMSException e) {
            log.error (e);
        }
    }

    public void notify (Object k, Object value) {
        Object obj = space.inp (k);
        if (obj != null)
            try {
                sender.send ((Message) obj);
            } catch (JMSException e) {
                log.error (e);
            }
    }

    private void listen2Space() {
        space.addListener (spaceKey, this);
    }

    private void deaf2Space() {
        space.removeListener (spaceKey, this);
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
    public void setConnectionFactory (String connectionFactory) {
        this.connectionFactory = connectionFactory;
        setModified (true);
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
    public void setQueueName (String queueName) {
        this.queueName = queueName;
        setModified (true);
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
    public void setUsername (String username) {
        this.username = username;
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="Password"
     */
    public void setPassword (String password) {
        this.password = password;
        setModified (true);
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
    public void setSpace (String spaceName) {
        this.spaceName = spaceName;
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="Space Key"
     */
    public void setSpaceKey (String spaceKey) {
        this.spaceKey = spaceKey;
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="Space Key"
     */
    public String getSpaceKey () {
        return spaceKey;
    }
}
