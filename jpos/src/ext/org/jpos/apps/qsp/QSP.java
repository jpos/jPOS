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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.xerces.parsers.DOMParser;
import org.jpos.apps.qsp.config.ConfigTask;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOMUX;
import org.jpos.space.TransientSpace;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SystemMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see <a href="http://www.cebik.com/qsig.html">QSP</a>
 */
public class QSP implements ErrorHandler, LogSource, Runnable, QSPMBean {
    public static final String NAMEREGISTRAR_PREFIX = "qsp.";
    public static final String DEFAULT_NAME = "default";
    Document config;
    Logger logger;
    String realm;
    File configFile;
    long lastModified;
    static ControlPanel controlPanel = null;
    long monitorConfigInterval = 60 * 1000;
    Collection reconfigurables;
    DOMParser parser;
    Configuration cfg;
    String[] extendedTags;
    String[] supportedTags;
    boolean validation;
    protected MBeanServer server = null;
    protected long startTime;
    protected boolean newConfigFile;

    public static String[] SUPPORTED_TAGS = 
        { "logger",
          "qsp-config",
          "log-listener",
          "thread-pool",
          "connection-pool",
          "object",
          "persistent-engine",
          "sequencer",
          "s-m-adapter",
          "secure-key-store",
          "control-panel",
          "channel",
          "filter",
          "mux",
          "server",
          "request-listener",
          "card-agent",
          "dir-poll",
          "task",
          "daily-task"
        };

    public QSP () {
        super();
        reconfigurables = new ArrayList();
        supportedTags = SUPPORTED_TAGS;
        extendedTags = new String[0];
        validation = true;
        startTime = System.currentTimeMillis();
    }
    /**
     * @param configFile XML based QSP config file
     * @param supportedTags array of supported tags (default if null)
     * @param extendedTags array of extended tags (none if null)
     * @param validation true to validate XML file
     */
    public QSP (    
        String[] supportedTags, 
        String[] extendedTags, 
        boolean validation)
    {
        this();
        if (supportedTags != null)
            this.supportedTags = supportedTags;
        if (extendedTags != null)
            this.extendedTags = extendedTags;
        this.validation = validation;
    }
    public long getElapsed () {
        return System.currentTimeMillis() - startTime;
    }
    public MBeanServer getMBeanServer () 
        throws IOException, MalformedObjectNameException,
        InstanceAlreadyExistsException, 
        MBeanRegistrationException,
        NotCompliantMBeanException,
        MalformedObjectNameException
    {
        if (server == null)
            createMBeanServer();
        return server;
    }
    public void setSupportedTags (String[] supportedTags) {
        this.supportedTags = supportedTags;
    }
    public String[] getSupportedTags () {
        return supportedTags;
    }
    public void setExtendedTags (String[] extendedTags) {
        this.extendedTags = extendedTags;
    }
    public String[] getExtendedTags () {
        return extendedTags;
    }
    public void setValidation (boolean validation) {
        this.validation = validation;
    }
    public boolean getValidation() {
        return validation;
    }
    public void setParser (DOMParser parser) {
        this.parser     = parser;
    }
    public void setConfig (Document config) {
        this.config = config;
    }
    public void setConfigFile (String f) throws IOException {
        File newFile = new File (f);
        if (!newFile.equals (configFile)) {
            if (this.configFile != null) {
                this.newConfigFile = true;
                Logger.log (new LogEvent (this, "set-config-file", f));
            }
            this.configFile = newFile;
            this.lastModified = configFile.lastModified();
            // this is causing problems on JDK1.4's AWT (when using panels)
            // Thread.currentThread().interrupt ();
        }
    }
    public void setMonitorConfigInterval (long l) {
        monitorConfigInterval = l;
        // this is causing problems on JDK1.4's AWT (when using panels)
        // Thread.currentThread().interrupt ();
    }
    public long getMonitorConfigInterval () {
        return monitorConfigInterval;
    }
    public String getConfigFile () {
        return configFile.toString ();
    }
    public Collection getReConfigurables() {
        return reconfigurables;
    }
    public ControlPanel initControlPanel (int rows, int cols) {
        if (controlPanel == null) {
            synchronized (QSP.class) {
                if (controlPanel == null) 
                    controlPanel = new ControlPanel (this, rows, cols);
            }
        }
        return controlPanel;
    }
    public ControlPanel getControlPanel (){
        return controlPanel;
    }
    public void warning (SAXParseException e) throws SAXException {
        Logger.log (new LogEvent (this, "warning", e));
        throw e;
    }
    public void error (SAXParseException e) throws SAXException {
        Logger.log (new LogEvent (this, "error", e));
        throw e;
    }
    public void fatalError (SAXParseException e) throws SAXException {
        Logger.log (new LogEvent (this, "fatalError", e));
        throw e;
    }
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    public String getRealm () {
        return realm;
    }
    public Logger getLogger () {
        return logger;
    }
    public void configure (String tagname) throws ConfigurationException {
        QSPConfigurator configurator = QSPConfiguratorFactory.create (tagname);
        NodeList nodes = config.getElementsByTagName (tagname);
        if (configurator instanceof QSPReConfigurator && nodes.getLength()>0)
            reconfigurables.add (tagname);
        for (int i=0; i<nodes.getLength(); i++) {
            configurator.config (this, nodes.item(i));
        }
    }
    public void reconfigure (String tagname) throws ConfigurationException {
        QSPConfigurator configurator = QSPConfiguratorFactory.create (tagname);
        if (configurator instanceof QSPReConfigurator) {
            NodeList nodes = config.getElementsByTagName (tagname);
            for (int i=0; i<nodes.getLength(); i++) 
                ((QSPReConfigurator)configurator).reconfig 
                    (this, nodes.item(i));
        }
    }
    protected void configure () throws ConfigurationException {
        LogEvent evt = new LogEvent (this, "configure");
        String[] st = getSupportedTags ();
        for (int i=0; i<st.length; i++) {
            evt.addMessage (st [i]);
            configure (st [i]);
        }
        evt.addMessage ("-extended tags-");

        st = getExtendedTags ();
        for (int i=0; i<st.length; i++) {
            evt.addMessage (st [i]);
            configure (st [i]);
        }
        Logger.log (evt);
    }
    private boolean monitorConfigFile () {
        long l;
        while (lastModified == (l=configFile.lastModified())) {
            if (newConfigFile) {
                Logger.log (new LogEvent (this, "new-config-detected"));
                return true;
            }
            try {
                Thread.interrupted ();  // clear interrupt flag
                Thread.sleep (monitorConfigInterval);
            } catch (InterruptedException e) { 
                Logger.log (
                    new LogEvent (this, "sleep-interrupted", 
                        Long.toString (monitorConfigInterval) + "/" +
                        Long.toString (l) + "/" + 
                        Long.toString (lastModified)
                    )
                );
                return true;
            }
        }
        return (lastModified = l) != 0;
    }
    public void shutdown () {
        Logger.log (new LogEvent (this, "shutdown"));
        new Thread() {
            public void run() {
                try {
                    Thread.sleep (1000);
                } catch (InterruptedException e) { }
                System.exit (0);
            }
        }.start ();
    }
    public static void shutdownMuxes() {
        Iterator iter = NameRegistrar.getMap().values().iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof ISOMUX) {
                ((ISOMUX)obj).terminate (60000);
            }
        }
    }
    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
    }
    public Configuration getConfiguration () {
        return cfg;
    }
    public String get (String propName) {
        return
            (cfg != null) ? cfg.get (propName) : null;
    }
    public void run () {
        while (newConfigFile || monitorConfigFile ()) {
            try {
                parser.parse (configFile.getPath());
                setConfig (parser.getDocument());
                if (newConfigFile) {
                    Logger.log (
                        new LogEvent (this, "new-config-file", configFile)
                    );
                    configure();
                    newConfigFile = false;
                } else {
                    Iterator iter = getReConfigurables().iterator();
                    while (iter.hasNext())
                        reconfigure ((String) iter.next());
                }
            } catch (Exception e) {
                Logger.log (new LogEvent (this, "QSP", e));
                try {
                    Thread.sleep (1000);
                } catch (InterruptedException ie) { }
            }
        }
        Logger.log (new LogEvent (this, "shutdown-start"));
        shutdownMuxes ();
        Logger.log (new LogEvent (this, "shutdown"));
        System.exit (0);
    }
    public static QSP getInstance (String name) 
        throws NameRegistrar.NotFoundException
    {
        return (QSP) NameRegistrar.get (NAMEREGISTRAR_PREFIX + name);
    }
    public static QSP getInstance () 
        throws NameRegistrar.NotFoundException
    {
        return getInstance (DEFAULT_NAME);
    }

    /**
     * @return QSP's config instance (global properties)
     */
    public static Configuration getGlobalConfiguration () {
        Configuration cfg;
        try {
            cfg = getInstance().getConfiguration();
        } catch (NameRegistrar.NotFoundException e) {
            cfg = new SimpleConfiguration();
        }
        return cfg;
    }
    /**
     * Launches QSP
     * @param configFile XML based QSP config file
     * @param supportedTags array of supported tags
     * @param extendedTags array of extended tags
     * @param validation true to validate XML file
     */
    public static void launch (
        String configFile, 
        String[] supportedTags, 
        String[] extendedTags, boolean validation)
    {
        DOMParser parser = new DOMParser();
        QSP qsp = new QSP (supportedTags, extendedTags, validation);
        try {
            qsp.setParser (parser);
            qsp.setConfigFile (configFile);
            parser.setFeature(
                "http://xml.org/sax/features/validation", 
                qsp.getValidation()
            );
            parser.setErrorHandler (qsp);
            parser.parse (qsp.configFile.getPath());
            qsp.setConfig (parser.getDocument());
            qsp.configure ();
                qsp.registerMBean (
                TransientSpace.getSpace (), "type=space,name=default"
            );
            if (controlPanel != null)
                controlPanel.showUp();

            if (qsp.getLogger() != null) 
                new SystemMonitor (3600000, qsp.getLogger(), "monitor");

            ThreadGroup group = new ThreadGroup("QSP");
            new Thread (group, qsp).start();
        } catch (Exception e) {
            Logger.log (new LogEvent (qsp, "error", e));
            e.printStackTrace ();
        }
    }
    /**
     * @return task instance with given name.
     * @throws NameRegistrar.NotFoundException;
     * @see NameRegistrar
     */
    public static Object getTask (String name)
        throws NameRegistrar.NotFoundException
    {
        return NameRegistrar.get (
            ConfigTask.NAMEREGISTRAR_PREFIX+name
        );
    }

    protected void createMBeanServer () 
        throws IOException, MalformedObjectNameException,
        InstanceAlreadyExistsException, 
        MBeanRegistrationException,
        MalformedObjectNameException,
        NotCompliantMBeanException
    {
        // Trace.parseTraceProperties ();
        String domain = cfg.get ("jmx.domain", "QSP");
        server = MBeanServerFactory.createMBeanServer(domain);
        ObjectName mbeanObjectName = new ObjectName(domain + ":type=QSP");
        server.registerMBean (this, mbeanObjectName);
    }

    public void registerMBean (Object bean, String type) 
        throws IOException, NotCompliantMBeanException,
               InstanceAlreadyExistsException,
               InstanceAlreadyExistsException, 
               MalformedObjectNameException,
               MBeanRegistrationException
    {
        MBeanServer server = getMBeanServer();
        ObjectName name = new ObjectName (
            server.getDefaultDomain() + ":" + type 
        );
        server.registerMBean (bean, name);
    }

    /**
     * Launches QSP with default values for supportedTags and validation
     * @param configFile XML based QSP config file
     */
    public static void launch (String configFile) {
        launch (configFile, SUPPORTED_TAGS, new String[0], true);
    }
    public static void main (String args[]) {
        if (args.length != 1) {
            System.out.println ("Usage: org.jpos.apps.qsp.QSP <configfile>");
            System.exit (1);
        }
        launch (args[0]);
    }
}
