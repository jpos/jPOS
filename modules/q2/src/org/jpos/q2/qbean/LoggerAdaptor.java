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

package org.jpos.q2.qbean;

import java.util.Iterator;

import org.jdom.Element;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.util.LogListener;
import org.jpos.util.Logger;

public class LoggerAdaptor extends QBeanSupport {
    Logger logger;

    protected void initService () {
        logger = Logger.getLogger (getName());
    }
    protected void startService () throws ConfigurationException {
        logger.removeAllListeners ();
        Iterator iter = getPersist ().getChildren ("log-listener").iterator();
        while (iter.hasNext()) 
            addListener ((Element) iter.next ());
    }
    protected void stopService() {
        logger.removeAllListeners ();
    }
    protected void destroyService() {
        // we don't destroy (that would unregister the logger from the
        // NameRegistrar) because other components might have references
        // to this logger.
        //
        // logger.destroy ();
    }
    private void addListener (Element e) 
        throws ConfigurationException
    {
        QFactory factory = getServer().getFactory();
        String clazz  = e.getAttributeValue ("class");
        LogListener listener = (LogListener) factory.newInstance (clazz);
        if (listener instanceof Configurable) {
            try {
                ((Configurable) listener).setConfiguration (
                    factory.getConfiguration (e)
                );
            } catch (ConfigurationException ex) {
                throw new ConfigurationException (ex);
            }
        }
        
        logger.addListener (listener);
    }
}

