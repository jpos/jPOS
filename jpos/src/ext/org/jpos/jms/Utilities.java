/*
 * 
 * Copyright (c) 2000 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */
import javax.naming.*;
import javax.jms.*;

/**
 * Utility class for JMS programs.
 * <p>
 * Contains the following methods: 
 * <ul> 
 *   <li> getQueueConnectionFactory
 *   <li> getTopicConnectionFactory
 *   <li> getQueue
 *   <li> getTopic
 *   <li> jndiLookup
 *   <li> exit
 *   <li> receiveSynchronizeMessages
 *   <li> sendSynchronizeMessages
 * </ul>
 *
 * Also contains the class DoneLatch, which contains the following methods:
 * <ul> 
 *   <li> waitTillDone
 *   <li> allDone
 * </ul>
 *
 * <p>
 * Original code by Kim Haase and Joseph Fialli. Small modifications and rename done
 * for use with jPOS.
 * <p>
 * Original code provided for non JNDI providers. This support has been removed and
 * will be implemented with derived classes and overloaded methods for other providers.
 * <p>
 * Note that JBOSS specific naming of queues and topics has been removed. For use accessing
 * JBOSSMQ queues, add the necessary qualifiers.
 *
 * @author Kim Haase
 * @author Joseph Fialli
 * @author Alwyn Schoeman (minor jPOS changes)
 * @version $Revision$ $Date$
 */
public class Utilities {
    private static Context      jndiContext = null;
    
    /**
     * Returns a QueueConnectionFactory object.
     *
     * @param factory   String specifying factory name in JNDI.
     *
     * @return		a QueueConnectionFactory object
     * @throws		javax.naming.NamingException (or other exception)
     *                   if name cannot be found
     */
    public static QueueConnectionFactory getQueueConnectionFactory (String factory) 
      throws Exception {
            return (QueueConnectionFactory) jndiLookup (factory);
    }
    
    /**
     * Returns a TopicConnectionFactory object.
     *
     * @param factory   String specifying factory name in JNDI.
     *
     * @return		a TopicConnectionFactory object
     * @throws		javax.naming.NamingException (or other exception)
     *                   if name cannot be found
     */
    public static TopicConnectionFactory getTopicConnectionFactory (String factory) 
      throws Exception {
            return (TopicConnectionFactory) jndiLookup(factory);
    }
    
    /**
     * Returns a Queue object.
     *
     * @param name      String specifying queue name
     *
     * @return		a Queue object
     * @throws		javax.naming.NamingException (or other exception)
     *                   if name cannot be found
     */
    public static Queue getQueue(String name) 
      throws Exception {
            return (Queue) jndiLookup (name);
    }
    
    /** 
     * Returns a Queue object.
     *
     * @param name      String specifying queue name
     * @param session   QueueSession object
     *
     * @return      a Queue object
     * @throws      javax.jms.JMSException (or other exception)
     */
    public static Queue getQueue (String name, QueueSession session) 
        throws Exception {
        return session.createQueue (name);
    }

    /**
     * Returns a Topic object.
     *
     * @param name      String specifying topic name
     *
     * @return		a Topic object
     * @throws		javax.naming.NamingException (or other exception)
     *                   if name cannot be found
     */
    public static Topic getTopic(String name)
      throws Exception {
            return (Topic) jndiLookup(name);
    }
    
    /**
     * Returns a Topic object.
     *
     * @param name      String specifying topic name
     * @param session   TopicSession object
     *
     * @return      a Topic object
     * @throws      javax.jms.JMSException (or other exception)
     */
    public static Topic getTopic (String name, TopicSession session) 
        throws Exception {
        return session.createTopic (name);
    }

    /**
     * Creates a JNDI InitialContext object if none exists yet.  Then looks up 
     * the string argument and returns the associated object.
     *
     * @param name	the name of the object to be looked up
     *
     * @return		the object bound to <code>name</code>
     * @throws		javax.naming.NamingException if name cannot be found
     */
    public static Object jndiLookup(String name) throws NamingException {
        Object    obj = null;

        if (jndiContext == null) {
            try {
                jndiContext = new InitialContext();
            } catch (NamingException e) {
                throw e;
            }
        }
        try {
           obj = jndiContext.lookup(name);
        } catch (NamingException e) {
            throw e;
        }
        return obj;
    }
   
    /**
     * Calls System.exit().
     * 
     * @param result	The exit result; 0 indicates no errors
     */
    public static void exit(int result) {
        System.exit(result);
    }
   
    /**
     * Wait for 'count' messages on controlQueue before continuing.  Called by
     * a publisher to make sure that subscribers have started before it begins
     * publishing messages.
     * <p>
     * If controlQueue doesn't exist, the method throws an exception.
     *
     * @param factory   String specifying factory.
     * @param prefix	prefix (publisher or subscriber) to be displayed
     * @param controlQueueName	name of control queue 
     * @param count	number of messages to receive
     */
    public static void receiveSynchronizeMessages(String factory,
                                                  String prefix,
                                                  String controlQueueName, 
                                                  int count) 
      throws Exception {
        QueueConnectionFactory  queueConnectionFactory = null;
        QueueConnection         queueConnection = null;
        QueueSession            queueSession = null;
        Queue                   controlQueue = null;
        QueueReceiver           queueReceiver = null;

        try {
            queueConnectionFactory = Utilities.getQueueConnectionFactory(factory);
            queueConnection = queueConnectionFactory.createQueueConnection();
            queueSession = queueConnection.createQueueSession(false, 
                                                 Session.AUTO_ACKNOWLEDGE);
            controlQueue = getQueue(controlQueueName, queueSession);
            queueConnection.start();
        } catch (Exception e) {
            System.out.println("Connection problem: " + e.toString());
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException ee) {}
            }
            throw e;
        } 

        try {
            System.out.println(prefix + "Receiving synchronize messages from "
                               + controlQueueName + "; count = " + count);
            queueReceiver = queueSession.createReceiver(controlQueue);
            while (count > 0) {
                queueReceiver.receive();
                count--;
                System.out.println(prefix 
                                   + "Received synchronize message; expect " 
                                   + count + " more");
            }
        } catch (JMSException e) {
            System.out.println("Exception occurred: " + e.toString());
            throw e;
        } finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException e) {}
            }
        }
    }

    /**
     * Send a message to controlQueue.  Called by a subscriber to notify a
     * publisher that it is ready to receive messages.
     * <p>
     * If controlQueue doesn't exist, the method throws an exception.
     *
     * @param factory   String specifying factory.
     * @param prefix	prefix (publisher or subscriber) to be displayed
     * @param controlQueueName	name of control queue
     */
    public static void sendSynchronizeMessage(String factory,
                                              String prefix,
                                              String controlQueueName) 
      throws Exception {
        QueueConnectionFactory  queueConnectionFactory = null;
        QueueConnection         queueConnection = null;
        QueueSession            queueSession = null;
        Queue                   controlQueue = null;
        QueueSender             queueSender = null;
        TextMessage             message = null;

        try {
            queueConnectionFactory = Utilities.getQueueConnectionFactory(factory);
            queueConnection = queueConnectionFactory.createQueueConnection();
            queueSession = queueConnection.createQueueSession(false,
                                                 Session.AUTO_ACKNOWLEDGE);
            controlQueue = getQueue(controlQueueName, queueSession);
        } catch (Exception e) {
            System.out.println("Connection problem: " + e.toString());
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException ee) {}
            }
            throw e;
        } 

        try {
            queueSender = queueSession.createSender(controlQueue);
            message = queueSession.createTextMessage();
            message.setText("synchronize");
            System.out.println(prefix + "Sending synchronize message to " 
                               + controlQueueName);
            queueSender.send(message);
        } catch (JMSException e) {
            System.out.println("Exception occurred: " + e.toString());
            throw e;
        } finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException e) {}
            }
        }
    }

    /**
     * Monitor class for asynchronous examples.  Producer signals end of
     * message stream; listener calls allDone() to notify consumer that the 
     * signal has arrived, while consumer calls waitTillDone() to wait for this 
     * notification.
     *
     * @author Joseph Fialli
     */
    static public class DoneLatch {
        boolean  done = false;

        /**
         * Waits until done is set to true.
         */
        public void waitTillDone() {
            synchronized (this) {
                while (! done) {
                    try {
                        this.wait();
                    } catch (InterruptedException ie) {}
                }
            }
        }
        
        /**
         * Sets done to true.
         */
        public void allDone() {
            synchronized (this) {
                done = true;
                this.notify();
            }
        }
    }
}

