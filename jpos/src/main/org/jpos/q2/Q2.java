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
import java.util.TreeMap;
import java.util.Set;
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

import org.jpos.util.Log;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.SimpleLogListener;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class Q2 implements FileFilter {
    public static final String DEFAULT_DEPLOY_DIR  = "deploy";
    public static final String JMX_NAME            = "Q2";
    public static final String LOGGER_NAME         = "Q2";
    public static final String REALM               = "Q2.system"; 
    public static final String LOGGER_CONFIG       = "00_logger.xml";
    public static final String QBEAN_NAME          = "Q2:type=qbean,service=";
    public static final String Q2_CLASS_LOADER     = "Q2:type=system,service=loader";

    public static final int SCAN_INTERVAL = 2500;

    private MBeanServer server;
    private File deployDir, libDir;
    private Map dirMap;
    private QFactory factory;
    private QClassLoader loader;
    private Log log;
    private boolean shutdown;
    private Thread q2Thread;

    public Q2 (String dir) {
        super();
        this.deployDir  = new File (dir);
        this.libDir     = new File (deployDir, "lib");
        this.dirMap     = new TreeMap ();
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
	server.registerMBean (loader, loaderName);
        factory = new QFactory (loaderName, this);
        initSystemLogger ();
        addShutdownHook ();
        q2Thread = Thread.currentThread ();
        while (!shutdown) {
            try {
                loader = loader.scan ();
                scan ();
                deploy ();
                checkModified ();
                relax (SCAN_INTERVAL);
            } catch (Exception e) {
                log.error ("start", e);
                relax ();
            }
        }
        q2Thread = null;
        undeploy ();
    }

    public QClassLoader getLoader () {
        return loader;
    }
    public QFactory getFactory () {
        return factory;
    }

    public boolean accept (File f) {
        return f.getName().endsWith (".xml");
    }

    private void scan () {
        File file[] = deployDir.listFiles (this);
        // Arrays.sort (file); --apr not required - we use TreeMap
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
                deploy (f);
                if (qentry.isQBean ())
                    startList.add (qentry.getInstance());
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

    private void undeploy () {
        Object[] set = dirMap.entrySet().toArray ();
        int l = set.length;

        while (l-- > 0) {
            Map.Entry entry = (Map.Entry) set[l];
            File   f  = (File) entry.getKey ();
            undeploy (f);
        }
    }

    private void addShutdownHook () {
        Runtime.getRuntime().addShutdownHook (
            new Thread () {
                public void run () {
                    shutdown = true;
                    log.info ("shutting down");
                    if (q2Thread != null) {
                        try {
                            q2Thread.join ();
                        } catch (InterruptedException e) { }
                    }
                }
            }
        );
    }

    private void checkModified () {
        Iterator iter = dirMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            File   f        = (File)   entry.getKey ();
            QEntry qentry   = (QEntry) entry.getValue ();
            if (qentry.isQBean() && qentry.isQPersist()) {
                ObjectName name = qentry.getObjectName ();
                if (getState (name) == QBean.STARTED && isModified (name)) {
                    qentry.setDeployed (persist (f, name));
                }
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
                log.warn ("getState", e);
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
            log.warn ("persist", ex);
        }
        return deployed;
    }

    private void undeploy (File f) {
        QEntry qentry = (QEntry) dirMap.get (f);
        try {
            if (log != null)
                log.trace ("undeploying:" + f.getName());

            Object obj      = qentry.getObject ();
            ObjectName name = qentry.getObjectName ();
            factory.destroyQBean (this, name, obj);
            if (log != null)
                log.info ("undeployed:" + f.getName());
        } catch (Exception e) {
            getLog().warn ("undeploy", e);
        }
    }

    private void register (File f) {
        if (dirMap.get (f) == null)
            dirMap.put (f, new QEntry ());
    }

    private void deploy (File f) {
        try {
            if (log != null)
                log.info ("deploy:" + f.getName());
            QEntry qentry = (QEntry) dirMap.get (f);
            SAXBuilder builder = new SAXBuilder ();
            Document doc = builder.build (f);

            Object obj = factory.instantiate (this, doc.getRootElement ());
            qentry.setObject (obj);

            ObjectInstance instance = factory.createQBean (
                this, doc.getRootElement(), obj
            );
            qentry.setInstance (instance);
        } catch (Exception e) {
            getLog().warn ("deploy", e);
        } 
    }

    private void start (ObjectInstance instance) {
        try {
            factory.startQBean (this, instance.getObjectName());
        } catch (Exception e) {
            getLog().warn ("start", e);
        }
    }
    public void relax (long sleep) {
        try {
            Thread.sleep (sleep);
        } catch (InterruptedException e) { }
    }
    public void relax () {
        relax (1000);
    }
    private void initSystemLogger () {
        File loggerConfig = new File (deployDir, LOGGER_CONFIG);
        if (loggerConfig.canRead()) {
            try {
                register (loggerConfig);
                deploy ();
            } catch (Exception e) {
                getLog().warn ("init-system-logger", e);
            }
        }
        getLog().info ("Q2 started, deployDir="+deployDir.getAbsolutePath());
    }
    public Log getLog () {
        if (log == null) {
            Logger logger = Logger.getLogger (LOGGER_NAME);
            if (!logger.hasListeners())
                logger.addListener (new SimpleLogListener (System.out));
            log = new Log (logger, REALM);
        }
        return log;
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
        Object obj;
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
        public void setObject (Object obj) {
            this.obj = obj;
        }
        public Object getObject () {
            return obj;
        }
        public boolean isQBean () {
            return obj instanceof QBean;
        }
        public boolean isQPersist () {
            return obj instanceof QPersist;
        }
    }
}

