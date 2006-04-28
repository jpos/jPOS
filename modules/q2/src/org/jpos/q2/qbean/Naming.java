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

import org.jpos.q2.Q2;
import org.jpos.q2.QBean;
import org.jpos.q2.QPersist;
import org.jpos.q2.QBeanSupport;
import org.jdom.Element;
import org.jpos.util.Log;
import org.jpos.util.Logger;

import javax.management.*;

import mx4j.tools.naming.NamingService;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @version $Revision$ $Date$
 * @jmx:mbean description="Naming Service" extends="org.jpos.q2.QBeanSupportMBean"
 */
public class Naming
            extends QBeanSupport implements NamingMBean
{
    NamingService naming;

    private int port = 1099;

    public Naming () {
        super();
        naming = new NamingService();
    }

    /**
     * @jmx:managed-operation description="QBean init"
     */
    public void init () {
        log.info ("init");
        super.init ();
    }
    /**
     * @jmx:managed-operation description="QBean start"
     */
    public void start() {
        log.info ("start");
        super.start ();
    }
    /**
     * @jmx:managed-operation description="QBean stop"
     */
    public void stop () {
        log.info ("stop");
        super.stop ();
    }
    /**
     * @jmx:managed-operation description="QBean destroy"
     */
    public void destroy () {
        log.info ("destroy");
        log = null;
    }

    public Element getPersist () {
        setModified (false);
        log.info ("getPersist");
        return createElement ("naming", NamingMBean.class);
    }

    /**
     * @jmx:managed-attribute description="set port"
     */
    public void setPort (int port) {
        this.port = port;
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="get port"
     */
    public int getPort () {
        return port;
    }

    public void startService()
            throws Exception
    {
        log.info("start listening on port:" + port);
        naming.setPort(port);
        naming.start();
    }


    public void stopService()
            throws Exception
    {
        naming.stop();
    }
}
