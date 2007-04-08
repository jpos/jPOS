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

package org.jpos.apps.qsp.task;

import java.rmi.RemoteException;

import javax.management.ObjectName;

import mx4j.tools.naming.NamingService;

import org.jpos.apps.qsp.QSP;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 *
 *
 * <pre>
 *  &lt;task name="NamingService" class="org.jpos.apps.qsp.task.Naming"
 *     logger="qsp" realm="Naming-Service"&gt;
 *     &lt;property name="port" value="1099" /&gt;
 *  &lt;/task&gt;
 * </pre>
 *
 */
public class Naming implements LogSource, Configurable, Runnable
{
    Configuration cfg;
    Logger logger;
    String realm;
    NamingService naming;

    public Naming() {
        super();
        naming = new NamingService();
    }
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }
    public Logger getLogger () {
        return logger;
    }
    public String getRealm () {
        return realm;
    }
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
        try {
            ObjectName name = new ObjectName(
                "RMIAdaptor:name=NamingService");
            QSP.getInstance().getMBeanServer().registerMBean (naming, name);
            int port = cfg.getInt ("port",1099);
            naming.setPort(port);

            Logger.log (new LogEvent (this, "register", name.toString()));
        } catch (Exception e) {
            throw new ConfigurationException (e);
        }
    }
    public void run () {
        try {
            naming.start();
        } catch (RemoteException e) {
            Logger.log (new LogEvent (this, "start", e.getMessage()));
        }
    }
}

