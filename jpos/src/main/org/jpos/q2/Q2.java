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

package org.jpos.q2;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.NotCompliantMBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.MBeanException;
import javax.management.MBeanServerFactory;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 */
public class Q2 implements FileFilter {
    public static final String DEFAULT_DEPLOY_DIR = "deploy";
    private MBeanServer server;
    private File deployDir;
    private Map dirMap;

    public Q2 (String dir) {
        super();
        this.deployDir = new File (dir);
        this.dirMap = new HashMap ();
    }

    public void start () {
        server = MBeanServerFactory.createMBeanServer("Q2");
        for (;;) {
            try {
                scan ();
                deploy ();
                Thread.sleep (1000);
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }
    }
    public boolean accept (File f) {
        return f.getName().endsWith (".xml");
    }

    private void scan () {
        File file[] = deployDir.listFiles (this);
        for (int i=0; i<file.length; i++) 
            register (file[i]);
    }

    private void deploy () {
        Iterator iter = dirMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            File   f = (File)   entry.getKey ();
            long deployed = ((Long) entry.getValue ()).longValue ();
            if (deployed == 0) {
                deploy (f);
                entry.setValue (new Long (f.lastModified ()));
            } else if (deployed != f.lastModified ()) {
                undeploy (f);
                iter.remove ();
            }
        }
    }

    private ObjectName getObjectName (File f) 
        throws MalformedObjectNameException,
               MalformedURLException
    {
        return new ObjectName ("q2:url=" + f.toURL());
    }

    private void undeploy (File f) {
        try {
            ObjectName name = getObjectName (f);
            server.invoke (name, "stop", null, null);
            server.invoke (name, "destroy", null, null);
            server.unregisterMBean (name);
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    private void register (File f) {
        if (dirMap.get (f) == null)
            dirMap.put (f, new Long (0));
    }

    private QBean deploy (File f) {
        QBean qbean = null;
        try {
            SAXBuilder builder = new SAXBuilder ();
            Document doc = builder.build (f);
            qbean = deploy (f, doc.getRootElement ());
            server.registerMBean (qbean, getObjectName (f));
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return qbean;
    }

    private QBean deploy (File f, Element e) throws Exception {
        return QFactory.createQBean (this, e);
    }
    
    public MBeanServer getMBeanServer () {
        return server;
    }

    public static void main (String[] args) {
        new Q2 (args.length > 0 ? args[0] : DEFAULT_DEPLOY_DIR).start ();
    }
}

