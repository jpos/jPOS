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
import java.lang.ref.WeakReference;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.core.Configuration;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISORequestListener;
import java.util.HashMap;
import java.io.IOException;

/** 
 * An ISO JMS Queue RequestListener.
 *
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 */
public class ISOJMSQConnector extends SimpleLogSource implements Configurable,ISORequestListener {

    private Configuration cfg;
    private SenderPool sendPool = null;
    private Queue queue;
    private HashMap sourceMap;
    private QueueSession recSession;
    private QueueReceiver receiver;

    private class ISOMsgListener implements MessageListener {
        public void onMessage (Message message) {
            ISOMsg msg;
            if (message instanceof ObjectMessage) {
                ObjectMessage obj = (ObjectMessage) message;
                try {
                    msg = (ISOMsg) obj.getObject ();
                    String srcKey = (String) msg.getValue(1000);
                    ISOSource src = (ISOSource) sourceMap.get (srcKey);
                    src.send (msg);
                } catch (JMSException e) {
                } catch (ISOException e) {
                } catch (IOException e) {
                }
            }
        }
    }

    public ISOJMSQConnector () {
        Logger.log (new LogEvent (this, "Constructor") );
        sourceMap = new HashMap();
    }

    public void setConfiguration (Configuration cfg)
        throws ConfigurationException 
    {
        LogEvent evt = new LogEvent (this, "Configuration");
        this.cfg = cfg;
        String connectionFactory = cfg.get ("connectionFactory");
        if (connectionFactory.equals(""))
            throw new ConfigurationException ("ConnectionFactory not configured");
        String queueName = cfg.get ("sendQueue");
        if (queueName.equals(""))
            throw new ConfigurationException ("Queue name not specified");
        String username = cfg.get ("username", null);
        String password = cfg.get ("password", null);
        QueueConnectionFactory qcf = null;
        QueueConnection qc = null;
        try {
            qcf = Utilities.getQueueConnectionFactory (connectionFactory);
            qc = null;
            if (username == null)
                qc = Utilities.getQueueConnection (qcf);
            else
                qc = Utilities.getQueueConnection (qcf, username, password);
            queue = Utilities.getQueue (queueName);
            sendPool = new SenderPool (qc);
            sendPool.setLogger (getLogger(), getRealm() + "sendPool");
            sendPool.setConfiguration (cfg);
            int numSenders = cfg.getInt ("senders", 1);
            evt.addMessage ("NumSenders: " + numSenders);
            for (int i = 0; i < numSenders; i++)
                sendPool.addSender (queue);
            String receiveQueue = cfg.get ("receiveQueue");
            if (!receiveQueue.equals("")) {
                Queue recQueue = Utilities.getQueue (receiveQueue);
                recSession = qc.createQueueSession (false, Session.AUTO_ACKNOWLEDGE);
                receiver = recSession.createReceiver (recQueue);
                ISOMsgListener ml = new ISOMsgListener();
                receiver.setMessageListener (ml);
            }
            qc.start();
        } catch (Exception e) {
            throw new ConfigurationException ("Could not setup JMS Queue Connection", e);
        }
        evt.addMessage ("ConnectionFactory: " + connectionFactory);
        evt.addMessage ("QueueName: " + queueName);
        Logger.log (evt);
    }
    public boolean process (ISOSource source, ISOMsg m) {
        LogEvent evt = new LogEvent (this, "process");
        String sourceKey = source.toString();
        if (sendPool != null) {
            if (!sourceMap.containsKey (sourceKey))
                sourceMap.put (sourceKey, source);
            try {
                m.set (new ISOField (1000, sourceKey));
            } catch (ISOException e) {
                evt.addMessage (e);
            }
//            try {
//                WeakISOSource wSource = new WeakISOSource (source);
//                java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
//                java.io.ObjectOutputStream out2 = new java.io.ObjectOutputStream (bout);
//                out2.writeObject (wSource);
//                out2.flush ();
//                m.set (new ISOBinaryField (1000, bout.toByteArray()));    
                evt.addMessage (source);
                sendPool.send (m);
//            } catch (ISOException e) {
//                evt.addMessage (e);
 //           } catch (java.io.IOException e) {
  //              evt.addMessage (e);
   //         } finally {
                evt.addMessage (m);
                Logger.log (evt);
    //        }
        }
        return false;
    }
}

