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
import java.io.FileWriter;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.ObjectInstance;
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
 * @version $Revision$ $Date$
 */
public class Q2 implements FileFilter {
    public static final String DEFAULT_DEPLOY_DIR  = "deploy";
    public static final String JMX_NAME            = "Q2";
    public static final String QBEAN_NAME          = "Q2:type=qbean,service=";
    public static final String Q2_CLASS_LOADER     = "Q2:type=system,service=loader";
    private MBeanServer server;
    private File deployDir, libDir;
    private Map dirMap;
    private QFactory factory;
    private QClassLoader loader;

    public Q2 (String dir) {
        super();
        this.deployDir  = new File (dir);
        this.libDir     = new File (deployDir, "lib");
        this.dirMap     = new HashMap ();
        deployDir.mkdirs ();
    }

    public void start () 
        throws MalformedObjectNameException,
               InstanceAlreadyExistsException,
               NotCompliantMBeanException,
               MBeanRegistrationException
    {
        server  = MBeanServerFactory.createMBeanServer (JMX_NAME);
        ObjectName loaderName = new ObjectName (Q2_CLASS_LOADER);
        loader = new QClassLoader (server, libDir, loaderName);
	server.registerMBean (loader, loaderName);//new
        factory = new QFactory (loaderName);
        for (;;) {
            try {
                loader = loader.scan ();
                scan ();
                deploy ();
                checkModified ();
                Thread.sleep (1000);
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }
    }

    public QClassLoader getLoader () {
        return loader;
    }

    public boolean accept (File f) {
        return f.getName().endsWith (".xml");
    }

    private void scan () {
        File file[] = deployDir.listFiles (this);
        Arrays.sort (file);
        for (int i=0; i<file.length; i++) {
            register (file[i]);
        }
    }

    private void deploy () {
        List startList = new ArrayList ();
        Iterator iter = dirMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            File   f        = (File)   entry.getKey ();
            QEntry qentry   = (QEntry) entry.getValue ();
            long deployed   = qentry.getDeployed ();
            if (deployed == 0) {
                ObjectInstance instance = deploy (f);
                qentry.setInstance (instance);
                startList.add (instance);
                qentry.setDeployed (f.lastModified ());
            } else if (deployed != f.lastModified ()) {
                undeploy (f);
                iter.remove ();
            }
        }

        iter = startList.iterator();
        while (iter.hasNext ()) {
            start ((ObjectInstance) iter.next ());
        }
    }

    private void checkModified () {
        Iterator iter = dirMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            File   f        = (File)   entry.getKey ();
            QEntry qentry   = (QEntry) entry.getValue ();
            ObjectName name = qentry.getObjectName ();
            if (getState (name) == QBean.STARTED && isModified (name)) {
                qentry.setDeployed (persist (f, name));
            }
        }
    }

    private int getState (ObjectName name) {
        int status = -1;
        if (name != null) {
            try {
                status = (
                    (Integer) server.getAttribute (name, "State")
                ).intValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return status;
    }
    private boolean isModified (ObjectName name) {
        boolean modified = false;
        if (name != null) {
            try {
                modified = (
                    (Boolean) server.getAttribute (name, "Modified")
                ).booleanValue();
            } catch (Exception e) {
                // Okay to fail
            }
        }
        return modified;
    }
    private long persist (File f, ObjectName name) {
        long deployed = f.lastModified ();
        try {
            Element e = (Element) server.getAttribute (name, "Persist");
            if (e != null) {
                XMLOutputter out = new XMLOutputter (" ", true);
                Document doc = new Document ();
                doc.setRootElement (e);
                File tmp = new File (f.getAbsolutePath () + ".tmp");
                FileWriter writer = new FileWriter (tmp);
                out.output (doc, writer);
                writer.close ();
                tmp.renameTo (f);
                deployed = f.lastModified ();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return deployed;
    }

    private void undeploy (File f) {
        QEntry qentry = (QEntry) dirMap.get (f);
        try {
            ObjectName name = qentry.getObjectName ();
            if (name != null)
                factory.destroyQBean (this, name);
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    private void register (File f) {
        if (dirMap.get (f) == null)
            dirMap.put (f, new QEntry ());
    }

    private ObjectInstance deploy (File f) {
        try {
            SAXBuilder builder = new SAXBuilder ();
            Document doc = builder.build (f);
            return factory.createQBean (this, doc.getRootElement());
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return null;
    }

    private void start (ObjectInstance instance) {
        try {
            factory.startQBean (this, instance.getObjectName());
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    public MBeanServer getMBeanServer () {
        return server;
    }

    public static void main (String[] args) throws Exception {
        new Q2 (args.length > 0 ? args[0] : DEFAULT_DEPLOY_DIR).start ();
    }

    public class QEntry {
        long deployed;
        ObjectInstance instance;
        public QEntry () {
            super();
        }
        public QEntry (long deployed, ObjectInstance instance) {
            super();
            this.deployed = deployed;
            this.instance = instance;
        }
        public long getDeployed () {
            return deployed;
        }
        public void setDeployed (long deployed) {
            this.deployed = deployed;
        }
        public void setInstance (ObjectInstance instance) {
            this.instance = instance;
        }
        public ObjectInstance getInstance () {
            return instance;
        }
        public ObjectName getObjectName () {
            return instance != null ? instance.getObjectName () : null;
        }
    }
}

