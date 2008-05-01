/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.q2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.MissingArgumentException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Log;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @author <a href="mailto:alwynschoeman@yahoo.com">Alwyn Schoeman</a>
 * @version $Revision$ $Date$
 */
public class Q2 implements FileFilter, Runnable {
    public static final String Q2_VERSION          = "@version@";
    public static final String Q2_REVISION         = "@revision@";
    public static final String Q2_DATE             = "@date@";
    public static final String DEFAULT_DEPLOY_DIR  = "deploy";
    public static final String JMX_NAME            = "Q2";
    public static final String LOGGER_NAME         = "Q2";
    public static final String REALM               = "Q2.system"; 
    public static final String LOGGER_CONFIG       = "00_logger.xml";
    public static final String QBEAN_NAME          = "Q2:type=qbean,service=";
    public static final String Q2_CLASS_LOADER     = "Q2:type=system,service=loader";
    public static final String DUPLICATE_EXTENSION = "DUP";
    public static final String ERROR_EXTENSION     = "BAD";
    public static final String ENV_EXTENSION       = "ENV";

    public static final String PROTECTED_QBEAN        = "protected-qbean";
    public static final int SCAN_INTERVAL             = 2500;
    public static final long SHUTDOWN_TIMEOUT         = 60000;

    private MBeanServer server;
    private File deployDir, libDir;
    private Map dirMap;
    private QFactory factory;
    private QClassLoader loader;
    private ClassLoader mainClassLoader;
    private Log log;
    private boolean shutdown;
    private boolean shuttingDown;
    private Thread q2Thread;
    private String[] args;
    private boolean hasSystemLogger;
    private boolean exit;
    private long startTime;
    private CLI cli;
    private boolean recursive;

    public Q2 (String[] args) {
        super();
        this.args = args;
        parseCmdLine (args);
        this.libDir     = new File (deployDir, "lib");
        this.dirMap     = new TreeMap ();
        deployDir.mkdirs ();
        startTime = System.currentTimeMillis();
        mainClassLoader = Thread.currentThread().getContextClassLoader();
    }
    public Q2 () {
        this (new String[] {} );
    }
    public void run() {
        try {
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void start () 
        throws MalformedObjectNameException,
               InstanceAlreadyExistsException,
               NotCompliantMBeanException,
               MBeanRegistrationException
    {
        /*
         * The following code determines whether a MBeanServer exists already.
         * If so then the first one in the list is used.  I have not yet find a way to
         * interrogate the server for information other than MBeans so to pick a
         * specific one would be difficult.
         */
        ArrayList mbeanServerList = MBeanServerFactory.findMBeanServer(null);
        if (mbeanServerList.size() == 0) {
            server  = MBeanServerFactory.createMBeanServer (JMX_NAME);
        } else {
            server = (MBeanServer) mbeanServerList.get(0);
        }
        final ObjectName loaderName = new ObjectName (Q2_CLASS_LOADER);
        try {
            loader = (QClassLoader) java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return new QClassLoader (server, libDir, loaderName, mainClassLoader);
                    }
                }
            );
            server.registerMBean (loader, loaderName);
            loader = loader.scan(false);
        } catch (Throwable t) {
            if (log != null)
                log.error ("initial-scan", t);
            else
                t.printStackTrace();
        }
        factory = new QFactory (loaderName, this);
        initSystemLogger ();
        addShutdownHook ();
        q2Thread = Thread.currentThread ();
        q2Thread.setContextClassLoader (loader);
        if (cli != null)
            cli.start();
        while (!shutdown) {
            try {
                boolean forceNewClassLoader = scan ();
                QClassLoader oldClassLoader = loader;
                loader = loader.scan (forceNewClassLoader);
                if (loader != oldClassLoader) {
                    oldClassLoader = null;
                    System.gc();  // force a GC
                    log.info (
                      "new classloader ["
                      + Integer.toString(loader.hashCode(),16)
                      + "] has been created"
                    );
                }
                deploy ();
                checkModified ();
                relax (SCAN_INTERVAL);
            } catch (Throwable t) {
                log.error ("start", t);
                relax ();
            }
        }
        undeploy ();
        try {
            server.unregisterMBean (loaderName);
        } catch (InstanceNotFoundException e) {
            log.error (e);
        }
        if (exit && !shuttingDown)
            System.exit (0);
    }
    public void shutdown () {
        shutdown(false);
    }
    public boolean running() {
        return !shutdown;
    }
    public void shutdown (boolean join) {
        shutdown = true;
        if (q2Thread != null) {
            log.info ("shutting down");
            q2Thread.interrupt ();
            if (join) {
                try {
                    q2Thread.join();
                    log.info ("shutdown done");
                } catch (InterruptedException e) {
                    log.warn (e);
                }
            }
        }
        q2Thread = null;

    }
    public QClassLoader getLoader () {
        return loader;
    }
    public QFactory getFactory () {
        return factory;
    }
    public String[] getCommandLineArgs() {
        return args;
    }
    public boolean accept (File f) {
        return f.canRead() && 
            (f.getName().endsWith (".xml") || 
             (recursive && f.isDirectory() && !"lib".equalsIgnoreCase (f.getName())));
    }
    public File getDeployDir () {
        return deployDir;
    }
    private boolean scan () {
            boolean rc = false;
            File file[] = deployDir.listFiles (this);
            // Arrays.sort (file); --apr not required - we use TreeMap
            if (file == null) {
                // Shutting down might be best, how to trigger from within?
                throw new Error("Deploy directory \""+deployDir.getAbsolutePath()+"\" is not available");
            } else {
                for (int i=0; i<file.length; i++) {
                    if (register (file[i]))
                        rc = true;
                }
            }
            return rc;
    }

    private void deploy () {
        List startList = new ArrayList ();
        Iterator iter = dirMap.entrySet().iterator();
        try {
            while (iter.hasNext() && !shutdown) {
                Map.Entry entry = (Map.Entry) iter.next();
                File   f        = (File)   entry.getKey ();
                QEntry qentry   = (QEntry) entry.getValue ();
                long deployed   = qentry.getDeployed ();
                if (deployed == 0) {
                    if (deploy (f)) {
                        if (qentry.isQBean ())
                            startList.add (qentry.getInstance());
                        qentry.setDeployed (f.lastModified ());
                    } else {
                        // deploy failed, clean up.
                        iter.remove();
                    }
                } else if (deployed != f.lastModified ()) {
                    undeploy (f);
                    iter.remove ();
                    loader.forceNewClassLoaderOnNextScan();
                }
            }
            iter = startList.iterator();
            while (iter.hasNext ()) {
                start ((ObjectInstance) iter.next ());
            }
        }
        catch (Exception e){
            log.error ("deploy", e);
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
            new Thread ("Q2-ShutdownHook") {
                public void run () {
                    shuttingDown = true;
                    shutdown = true;
                    if (q2Thread != null) {
                        log.info ("shutting down (hook)");
                        try {
                            q2Thread.join (SHUTDOWN_TIMEOUT);
                        } catch (InterruptedException e) { 
                        } catch (NullPointerException e) {
                            // on thin Q2 systems where shutdown is very fast, 
                            // q2Thread can become null between the upper if and
                            // the actual join. Not a big deal so we ignore the
                            // exception.
                        }
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
                XMLOutputter out = new XMLOutputter (Format.getPrettyFormat());
                Document doc = new Document ();
                e.detach();
                doc.setRootElement (e);
                File tmp = new File (f.getAbsolutePath () + ".tmp");
                FileWriter writer = new FileWriter (tmp);
                out.output (doc, writer);
                writer.close ();
                f.delete();
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
                log.trace ("undeploying:" + f.getCanonicalPath());

            Object obj      = qentry.getObject ();
            ObjectName name = qentry.getObjectName ();
            factory.destroyQBean (this, name, obj);
            if (log != null)
                log.info ("undeployed:" + f.getCanonicalPath());
        } catch (Exception e) {
            getLog().warn ("undeploy", e);
        }
    }

    private boolean register (File f) {
        boolean rc = false;
        if (f.isDirectory()) {
            File file[] = f.listFiles (this);
            for (int i=0; i<file.length; i++) {
                if (register (file[i]))
                    rc = true;
            }
        } else if (dirMap.get (f) == null) {
            dirMap.put (f, new QEntry ());
            rc = true;
        }
        return rc;
    }

    private boolean deploy (File f) {
        try {
            if (log != null)
                log.info ("deploy:" + f.getCanonicalPath());
            QEntry qentry = (QEntry) dirMap.get (f);
            SAXBuilder builder = new SAXBuilder ();
            Document doc = decrypt (builder.build (f));

            Object obj = factory.instantiate (this, doc.getRootElement ());
            qentry.setObject (obj);

            ObjectInstance instance = factory.createQBean (
                this, doc.getRootElement(), obj
            );
            qentry.setInstance (instance);
        } 
        catch (InstanceAlreadyExistsException e) {
           /*
            * Ok, the file we tried to deploy, holds an object
            *  that already has been deployed.
            *  
            * Rename it out of the way.
            * 
            */
            tidyFileAway(f,DUPLICATE_EXTENSION);
            getLog().warn ("deploy", e);
            return false;
        }
        catch (Exception e) {
            getLog().warn ("deploy", e);
            tidyFileAway(f,ERROR_EXTENSION);
            // This will also save deploy error repeats...
            return false;
        } 
        catch (Error e) {
            getLog().warn ("deploy", e);
            tidyFileAway(f,ENV_EXTENSION);
            // This will also save deploy error repeats...
            return false;
        }
        return true ;
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
            hasSystemLogger = true;
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
            if (!hasSystemLogger && !logger.hasListeners())
                logger.addListener (new SimpleLogListener (System.out));
            log = new Log (logger, REALM);
        }
        return log;
    }
    public MBeanServer getMBeanServer () {
        return server;
    }
    public long getUptime() {
        return System.currentTimeMillis() - startTime;
    }
    private void parseCmdLine (String[] args) {
        CommandLineParser parser = new PosixParser ();

        Options options = new Options ();
        options.addOption ("v","version", false, "Q2's version");
        options.addOption ("d","deploydir", true, "Deployment directory");
        options.addOption ("r","recursive", false, "Deploy subdirectories recursively");
        options.addOption ("h","help", false, "Usage information");
        options.addOption ("C","config", true, "Configuration bundle");
        options.addOption ("e","encrypt", true, "Encrypt configuration bundle");
        options.addOption ("i","cli", false, "Command Line Interface");
        options.addOption ("c","command", true, "Command to execute");

        try {
            CommandLine line = parser.parse (options, args);
            if (line.hasOption ("v")) {
                System.out.println (
                    "Q2 version: " +Q2_VERSION + "(" + Q2_REVISION + ")"
                );
                System.exit (0);
            } 
            if (line.hasOption ("h")) {
                HelpFormatter helpFormatter = new HelpFormatter ();
                helpFormatter.printHelp ("Q2", options);
                System.exit (0);
            } 
            if (line.hasOption ("c")) {
                cli = new CLI(this, line.getOptionValue("c"), line.hasOption("i"));
            } else if (line.hasOption ("i")) 
                cli = new CLI(this, null, true);

            String dir = DEFAULT_DEPLOY_DIR;
            if (line.hasOption ("d")) {
                dir = line.getOptionValue ("d");
            }
            recursive = line.hasOption ("r");
            this.deployDir  = new File (dir);
            if (line.hasOption ("C"))
                deployBundle (new File (line.getOptionValue ("C")), false);
            if (line.hasOption ("e"))
                deployBundle (new File (line.getOptionValue ("e")), true);
        } catch (MissingArgumentException e) {
            System.out.println ("ERROR: " + e.getMessage());
            System.exit (1);
        } catch (Exception e) {
            e.printStackTrace ();
            System.exit (1);
        }
    }
    private void deployBundle (File bundle, boolean encrypt) 
        throws JDOMException, IOException, 
                ISOException, GeneralSecurityException
    {
        SAXBuilder builder = new SAXBuilder ();
        Document doc = builder.build (bundle);
        Iterator iter = doc.getRootElement().getChildren ().iterator ();
        for (int i=0; iter.hasNext (); i += 5) {
            deployElement ((Element) iter.next (), i, encrypt);
        }
    }
    private void deployElement (Element e, int i, boolean encrypt) 
        throws ISOException, IOException, GeneralSecurityException
    {
        e = ((Element) e.clone ());

        XMLOutputter out = new XMLOutputter (Format.getPrettyFormat());
        Document doc = new Document ();
        doc.setRootElement (e);
        File qbean = new File (
            deployDir, ISOUtil.zeropad (Integer.toString (i),3) + "_" 
            + e.getName () + ".xml"
        );
        FileWriter writer = new FileWriter (qbean);
        if (encrypt)
            doc = encrypt (doc);
        out.output (doc, writer);
        writer.close ();
    }

    private byte[] dodes (byte[] data, int mode) 
       throws GeneralSecurityException
    {
        Cipher cipher = Cipher.getInstance ("DES");
        cipher.init (mode, new SecretKeySpec(getKey(), "DES"));
        return cipher.doFinal (data);
    }
    protected byte[] getKey() {
        return "CAFEBABE".getBytes();
    }
    protected Document encrypt (Document doc)
        throws GeneralSecurityException, IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (os);
        XMLOutputter out = new XMLOutputter (Format.getPrettyFormat());
        out.output (doc, writer);
        writer.close ();

        byte[] crypt = dodes (os.toByteArray(), Cipher.ENCRYPT_MODE);

        Document secureDoc = new Document ();
        Element root = new Element (PROTECTED_QBEAN);
        secureDoc.setRootElement (root);
        Element secureData = new Element ("data");
        root.addContent (secureData);

        secureData.setText (
            ISOUtil.hexString (crypt)
        );
        return secureDoc;
    }

    protected Document decrypt (Document doc) 
        throws GeneralSecurityException, IOException, JDOMException
    {
        Element root = doc.getRootElement ();
        if (PROTECTED_QBEAN.equals (root.getName ())) {
            Element data = root.getChild ("data");
            if (data != null) {
                ByteArrayInputStream is = new ByteArrayInputStream (
                    dodes (
                        ISOUtil.hex2byte (data.getTextTrim()),
                        Cipher.DECRYPT_MODE)
                );
                SAXBuilder builder = new SAXBuilder ();
                doc = builder.build (is);
            }
        }
        return doc;
    }
    
    private void tidyFileAway (File f, String extension) {
        File rename = new File(f.getAbsolutePath()+"."+extension);
        while (rename.exists()){
            rename = new File(rename.getAbsolutePath()+"."+extension);
        }
        getLog().warn("Tidying "+f.getAbsolutePath()+" out of the way, by adding ."+extension,"It will be called: "+rename.getAbsolutePath()+" see log above for detail of problem.");
        f.renameTo(rename);
    }

    private void setExit (boolean exit) {
        this.exit = exit;
    }
    public static void main (String[] args) throws Exception {
        Q2 q2 = new Q2(args);
        q2.setExit (true);
        q2.start();
    }
    public String getVersion() {
        return Q2_VERSION;
    }
    public String getRevision() {
        return Q2_REVISION;
    }
    public String getDate() {
        return Q2_DATE;
    }
    public String getRelease() {
        return getVersion() + getRevision();
    }
    public static class QEntry {
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

