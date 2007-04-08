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

package org.jpos.apps.qsp;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.naming.InitialContext;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.ThreadPool;

/**
 * QSP EJBConnector implements ISORequestListener
 * and forward all incoming messages to a given
 * DownStream Adapter, handling back responses
 *
 * @author Bharavi
 * @version 1.0
 * @see org.jpos.iso.ISORequestListener
 */
public class EJBConnector
    implements EJBConnectorMBean,ISORequestListener, LogSource, Configurable
{
    String theURL=null;
    String context = null;
    String adapterJNDI = null;
    InitialContext ic =null;
    Logger logger;
    String realm;

    int timeout = 0;
    String ip="127.0.0.1";
    String protocol="t3"; //Default for Weblogic
    int port=8000;
    boolean bounce = false;
    ThreadPool pool;
    public EJBConnector () {
        super();
        pool = new ThreadPool (1, 100);
    }

    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    public String getRealm () {
        return realm;
    }
    public Logger getLogger() {
        return logger;
    }
   /**
    * Destination is a be a DownStreamAdapter.
    * <ul>
    * <li>DownStream Adapter
    * <li>destination-IP address
    * <li>destination port
    * <li>bounce
    * </ul>
    * @param cfg Configuration
    */
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {

        bounce  = cfg.getBoolean ("bounce");
        ip = cfg.get ("ip");
        port = cfg.getInt ("port");
        protocol = cfg.get ("protocol");
        theURL=protocol+"://"+ip+":"+port;
        context = cfg.get ("jndicontext");
        adapterJNDI = cfg.get("adapterJNDI");
        ic=null;
    }
    /**
     * hook used to optional bounce an unanswered message
     * to its source channel
     * @param s message source
     * @param m unanswered message
     * @exception ISOException
     * @exception IOException
     */
    protected void processNullResponse (ISOSource s, ISOMsg m, LogEvent evt)
        throws ISOException, IOException
    {
    if (bounce) {
        ISOMsg c = (ISOMsg) m.clone();
        c.setResponseMTI();
        if (c.hasField (39))
            c.unset (39);
        s.send (c);
        evt.addMessage ("<bounced/>");
    } else
        evt.addMessage ("<null-response/>");
    }

    //Inner Thread class to handle requests
    protected class Process implements Runnable {
        ISOSource source;
        ISOMsg m;
        Process (ISOSource source, ISOMsg m) {
            super();
            this.source = source;
            this.m = m;
        }
        public void run () {
            LogEvent evt = 
                new LogEvent (EJBConnector.this, "connector-request-listener");

            try {
                ISOMsg c = (ISOMsg) m.clone();
                evt.addMessage (c);
                if(ic==null)  ic = getInitialContext(theURL, context);
                Object theHomeObject = ic.lookup( adapterJNDI );
                Class theHomeClass = theHomeObject.getClass();
                Method theHomeMethod = theHomeClass.getMethod( "create", null );
                Object theRemoteObject = 
                    theHomeMethod.invoke( theHomeObject, null );
                Class theRemoteClass = theRemoteObject.getClass();
                Class theRemoteArgs[] = { ISOMsg.class };
                Method theRemoteMethod = 
                    theRemoteClass.getMethod( "process", theRemoteArgs );
                Object theRemoteParams[] = { c };
                ISOMsg response = 
                    (ISOMsg)theRemoteMethod.invoke ( 
                        theRemoteObject, theRemoteParams
                    );
                if(response!=null) {
                    evt.addMessage ("<got-response/>");
                    evt.addMessage (response);
                    source.send(response);
                }
                else {
                    processNullResponse (source, m, evt);
                }
            } catch (ISOException  e) {
                evt.addMessage (e);
            } catch (java.io.IOException  e) {
                evt.addMessage (e);
            } catch (Exception e) {
                evt.addMessage (e);
            }
            Logger.log (evt);
        }

        private InitialContext getInitialContext(String theURL, String theICF) {
            try {
                final Properties theProperties=new Properties();
                theProperties.put(InitialContext.PROVIDER_URL,theURL);
                theProperties.put(
                    InitialContext.INITIAL_CONTEXT_FACTORY,theICF
                );
                final InitialContext ic = new InitialContext(theProperties);
            return ic;
            } catch(Exception theException) {
                Logger.log (
                    new LogEvent 
                        (EJBConnector.this, "initial-context", theException)
                );
            }
            return null;
        }
    }
    public boolean process (ISOSource source, ISOMsg m) {
        pool.execute (new Process (source, m));
        return true;
    }

    //MBean Methods
    public void setHost(String host)
    {
        ip=host;
    }
    public String getHost()
    {
        return ip;
    }
    public void setAdapter(String adapter)
    {
        adapterJNDI=adapter;
    }
    public String getAdapter()
    {
        return adapterJNDI;
    }
    public void setPort(int port)
    {
        this.port=port;
    }
    public int getPort()
    {
        return port;
    }
    public void setProtocol(String protocol)
    {
        this.protocol=protocol;
    }
    public String getProtocol()
    {
        return protocol;
    }
}

