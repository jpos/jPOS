/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jpos.core.Environment;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.install.ModuleUtils;
import org.jpos.q2.ssh.SshService;
import org.jpos.security.SystemSeed;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.PGPHelper;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.slf4j.Slf4JDynamicBinder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.xml.sax.SAXException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.*;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;
import static java.util.ResourceBundle.getBundle;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @author <a href="mailto:alwynschoeman@yahoo.com">Alwyn Schoeman</a>
 * @author <a href="mailto:vsalaman@vmantek.com">Victor Salaman</a>
 */
@SuppressWarnings("unchecked")
public class Q2 implements DirectoryStream.Filter<Path>, Runnable {
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
    public static final String LICENSEE            = "LICENSEE.asc";
    public static final byte[] PUBKEYHASH          = ISOUtil.hex2byte("C0C73A47A5A27992267AC825F3C8B0666DF3F8A544210851821BFCC1CFA9136C");

    public static final String PROTECTED_QBEAN        = "protected-qbean";
    public static final int SCAN_INTERVAL             = 2500;
    public static final long SHUTDOWN_TIMEOUT         = 60000;

    private MBeanServer server;
    private Path deployDir, libDir;
    private Map<Path,QEntry> dirMap;
    private QFactory factory;
    private QClassLoader loader;
    private ClassLoader mainClassLoader;
    private Log log;
    private volatile boolean started;
    private CountDownLatch ready = new CountDownLatch(1);
    private CountDownLatch shutdown = new CountDownLatch(1);
    private volatile boolean shuttingDown;
    private volatile Thread q2Thread;
    private String[] args;
    private boolean hasSystemLogger;
    private boolean exit;
    private Instant startTime;
    private CLI cli;
    private boolean recursive;
    private ConfigDecorationProvider decorator=null;
    private UUID instanceId;
    private Framework osgiFramework;
    private boolean startOSGI = false;
    private BundleContext bundleContext;
    private String pidFile;
    private Path pidFilePath;
    private AsynchronousFileChannel pidFileChannel;
    private String name = JMX_NAME;
    private long lastVersionLog;
    private String watchServiceClassname;
    private boolean enableSsh;
    private boolean disableDeployScan;
    private boolean disableDynamicClassloader;
    private int sshPort;
    private String sshAuthorizedKeys;
    private String sshUser;
    private String sshHostKeyFile;
    private static String DEPLOY_PREFIX = "META-INF/q2/deploy/";
    private static String CFG_PREFIX = "META-INF/q2/cfg/";
    private String nameRegistrarKey;
    
    public Q2 (String[] args, BundleContext bundleContext) {
        super();
        this.args = args;
        startTime = Instant.now();
        instanceId = UUID.randomUUID();
        parseCmdLine (args);
        libDir     = deployDir.resolve("lib");
        dirMap     = new TreeMap<>();
        try {
            Files.createDirectories(deployDir);
        } catch (IOException e) {
            if (log != null)
                log.error (e);
            else
                e.printStackTrace();
        }
        mainClassLoader = getClass().getClassLoader();
        this.bundleContext = bundleContext;
        try {
            Slf4JDynamicBinder.applyMods();
        }
        catch (Exception ignored) {
            // We won't stop anything just because we could
            // inot initialize slf4j.
            ignored.printStackTrace();
        }
        registerQ2();
    }
    public Q2 () {
        this (new String[] {}, null );
    }
    public Q2 (String deployDir) {
        this (new String[] { "-d", deployDir }, null);
    }
    public Q2 (String[] args) {
        this(args, null);
    }
    public void start () {
        if (shutdown.getCount() == 0)
            throw new IllegalStateException("Q2 has been stopped");
        new Thread (this).start();
    }
    public void stop () {
        shutdown(true);
    }
    public void run () {
        started = true;
        Thread.currentThread().setName ("Q2-"+getInstanceId().toString());

        Path dir = deployDir.toAbsolutePath();
        FileSystem fs = dir.getFileSystem();
        try (WatchService service = fs.newWatchService()) {
            watchServiceClassname = service.getClass().getName();
            dir.register(
              service,
              StandardWatchEventKinds.ENTRY_CREATE,
              StandardWatchEventKinds.ENTRY_MODIFY,
              StandardWatchEventKinds.ENTRY_DELETE
            );
            /*
            * The following code determines whether a MBeanServer exists 
            * already. If so then the first one in the list is used. 
            * I have not yet find a way to interrogate the server for 
            * information other than MBeans so to pick a specific one 
            * would be difficult.
            */
            ArrayList mbeanServerList =
                    MBeanServerFactory.findMBeanServer(null);
            if (mbeanServerList.isEmpty()) {
                server = MBeanServerFactory.createMBeanServer(JMX_NAME);
            } else {
                server = (MBeanServer) mbeanServerList.get(0);
            }
            final ObjectName loaderName = new ObjectName(Q2_CLASS_LOADER);

            try {
                loader = (QClassLoader) java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction() {
                            public Object run() {
                                return new QClassLoader(server, libDir.toFile(), loaderName, mainClassLoader);
                            }
                        }
                );
                if (server.isRegistered(loaderName))
                    server.unregisterMBean(loaderName);
                server.registerMBean(loader, loaderName);
                loader = loader.scan(false);
            } catch (Throwable t) {
                if (log != null)
                    log.error("initial-scan", t);
                else
                    t.printStackTrace();
            }
            factory = new QFactory(loaderName, this);
            writePidFile();
            initSystemLogger();
            if (bundleContext == null)
                addShutdownHook();
            q2Thread = Thread.currentThread();
            q2Thread.setContextClassLoader(loader);
            if (cli != null)
                cli.start();
            initConfigDecorator();
            if (startOSGI)
                startOSGIFramework();
            if (enableSsh) {
                deployElement(SshService.createDescriptor(sshPort, sshUser, sshAuthorizedKeys, sshHostKeyFile),
                  "05_sshd-" + getInstanceId() + ".xml", false, true);
            }

            deployInternal();
            for (int i = 1; shutdown.getCount() > 0; i++) {
                try {
                    if (i > 1 && disableDeployScan) {
                        shutdown.await();
                        break;
                    }
                    boolean forceNewClassLoader = scan() && i > 1;
                    QClassLoader oldClassLoader = loader;
                    loader = loader.scan(forceNewClassLoader);
                    if (loader != oldClassLoader) {
                        oldClassLoader = null; // We want this to be null so it gets GCed.
                        System.gc();  // force a GC
                        log.info(
                          "new classloader ["
                            + Integer.toString(loader.hashCode(), 16)
                            + "] has been created"
                        );
                        q2Thread.setContextClassLoader(loader);
                    }
                    logVersion();

                    deploy();
                    checkModified();
                    ready.countDown();
                    if (!waitForChanges(service))
                        break;
                } catch (InterruptedException | IllegalAccessError ignored) {
                    // NOPMD
                } catch (Throwable t) {
                    log.error("start", t.getMessage());
                    relax();
                }
            }
            undeploy();
            try {
                if (server.isRegistered(loaderName))
                    server.unregisterMBean(loaderName);
            } catch (InstanceNotFoundException e) {
                log.error(e);
            }
            if (decorator != null) {
                decorator.uninitialize();
            }
            if (exit && !shuttingDown)
                System.exit(0);
        } catch (IllegalAccessError ignored) {
            // NOPMD OK to happen
        } catch (Exception e) {
            if (log != null)
                log.error (e);
            else
                e.printStackTrace();
            System.exit (1);
        }
    }
    public void shutdown () {
        shutdown(false);
    }
    public boolean running() {
        return started && shutdown.getCount() > 0;
    }
    public boolean ready() {
        return ready.getCount() == 0 && shutdown.getCount() > 0;
    }
    public boolean ready (long millis) {
        try {
            ready.await(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) { }
        return ready();
    }
    public void shutdown (boolean join) {
        shutdown.countDown();
        unregisterQ2();
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
        stopOSGIFramework();
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
    public boolean accept (Path f) {
        return Files.isReadable(f) &&
            (isXml(f) || isBundle(f) ||
                    recursive && Files.isDirectory(f) && !"lib".equalsIgnoreCase (f.getFileName().toString()));
    }
    public boolean accept (File f) {
        return accept(f.toPath());
    }
    public File getDeployDir () {
        return deployDir.toFile();
    }

    public String getWatchServiceClassname() {
        return watchServiceClassname;
    }

    public static Q2 getQ2() {
        return (Q2) NameRegistrar.getIfExists(JMX_NAME);
    }
    public static Q2 getQ2(long timeout) {
        return (Q2) NameRegistrar.get(JMX_NAME, timeout);
    }

    private boolean isXml(Path f) {
        return f != null && f.getFileName().toString().toLowerCase().endsWith(".xml");
    }
    private boolean isBundle(Path f) {
        return osgiFramework != null && f != null && f.getFileName().toString().toLowerCase().endsWith(".jar");
    }
    private boolean scan () throws IOException {
        boolean rc = false;
        DirectoryStream<Path> file = Files.newDirectoryStream(deployDir,this);
        // Arrays.sort (file); --apr not required - we use TreeMap
        if (file == null) {
            // Shutting down might be best, how to trigger from within?
            throw new Error("Deploy directory \""+deployDir.toAbsolutePath().toString()+"\" is not available");
        } else {
            for (Path f : file) {
                if (register(f))
                    rc = true;
            }
        }
        return rc;
    }

    private void deploy () {
        List<ObjectInstance> startList = new ArrayList<>();
        List<Path> osgiBundelList = new ArrayList<>();
        Iterator<Map.Entry<Path,QEntry>> iter = dirMap.entrySet().iterator();

        try {
            while (iter.hasNext() && shutdown.getCount() > 0) {
                Map.Entry<Path,QEntry> entry = iter.next();
                Path f          = entry.getKey ();
                QEntry qentry   = entry.getValue ();
                long deployed   = qentry.getDeployed ();
                if (deployed == 0) {
                    if (qentry.isOSGIBundle()) {
                        osgiBundelList.add(f);
                        qentry.setDeployed (Files.getLastModifiedTime(f).toMillis());
                    }
                    else if (deploy(f)) {
                        if (qentry.isQBean ())
                            startList.add (qentry.getInstance());
                        qentry.setDeployed (Files.getLastModifiedTime(f).toMillis());
                    } else {
                        // deploy failed, clean up.
                        iter.remove();
                    }
                } else if (deployed != Files.getLastModifiedTime(f).toMillis()) {
                    undeploy (f);
                    iter.remove ();
                    loader.forceNewClassLoaderOnNextScan();
                }
            }
            for (Path f : osgiBundelList)
                registerOSGIBundle(f);
            for (ObjectInstance instance : startList)
                start(instance);
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
            Path   f  = (Path) entry.getKey ();
            undeploy (f);
        }
    }

    private void addShutdownHook () {
        Runtime.getRuntime().addShutdownHook (
            new Thread ("Q2-ShutdownHook") {
                public void run () {
                    shuttingDown = true;
                    shutdown.countDown();
                    if (q2Thread != null) {
                        log.info ("shutting down (hook)");
                        try {
                            q2Thread.join (SHUTDOWN_TIMEOUT);
                        } catch (InterruptedException ignored) {
                            // NOPMD nothing to do
                        } catch (NullPointerException ignored) {
                            // NOPMD
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

    private void checkModified () throws IOException {
        Iterator iter = dirMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Path   f        = (Path)   entry.getKey ();
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
                status = (Integer) server.getAttribute(name, "State");
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
                modified = (Boolean) server.getAttribute(name, "Modified");
            } catch (Exception ignored) {
                // NOPMD Okay to fail
            }
        }
        return modified;
    }
    private long persist (Path f, ObjectName name) throws IOException {
        long deployed = Files.getLastModifiedTime(f).toMillis();
        try {
            Element e = (Element) server.getAttribute (name, "Persist");
            if (e != null) {
                XMLOutputter out = new XMLOutputter (Format.getPrettyFormat());
                Document doc = new Document ();
                e.detach();
                doc.setRootElement (e);
                Path tmp = f.getParent().resolve(f.getFileName() + ".tmp");
                Writer writer = new BufferedWriter(new FileWriter(tmp.toFile()));
                try {
                    out.output (doc, writer);
                } finally {
                    writer.close ();
                }
                Files.move(tmp, f, REPLACE_EXISTING);
                deployed = Files.getLastModifiedTime(f).toMillis();
            }
        } catch (Exception ex) {
            log.warn ("persist", ex);
        }
        return deployed;
    }

    private void undeploy (Path f) {
        QEntry qentry = (QEntry) dirMap.get (f);
        try {
            if (log != null)
                log.trace ("undeploying:" + f.toAbsolutePath().normalize());

            if (qentry.isQBean()) {
                Object obj      = qentry.getObject ();
                ObjectName name = qentry.getObjectName ();
                factory.destroyQBean (this, name, obj);
            } else if (qentry.isOSGIBundle()) {
                getLog().warn("OSGI bundle " + f.getFileName() + " no longer available in deploy directory");
            }
            if (log != null)
                log.info ("undeployed:" + f.toAbsolutePath().normalize());

        } catch (Exception e) {
            getLog().warn ("undeploy", e);
        }
    }

    private boolean register (Path f) throws IOException {
        boolean rc = false;
        if (Files.isDirectory(f)) {
            DirectoryStream<Path> file = Files.newDirectoryStream(f, this);
            for (Path aFile : file) {
                if (register(aFile))
                    rc = true;
            }
        } else if (dirMap.get (f) == null) {
            dirMap.put (f, new QEntry (isBundle(f)));
            rc = true;
        }
        return rc;
    }

    private boolean deploy (Path f) throws IOException {
        LogEvent evt = log != null ? log.createInfo() : null;
        try {
            QEntry qentry = dirMap.get (f);
            SAXBuilder builder = createSAXBuilder();
            Document doc;
            if(decorator!=null && !f.getFileName().toString().equals(LOGGER_CONFIG))
            {
                doc=decrypt(builder.build(new StringReader(decorator.decorateFile(f.toFile()))));
            }
            else
            {
                doc=decrypt(builder.build(f.toFile()));
            }

            Element rootElement = doc.getRootElement();
            String iuuid = rootElement.getAttributeValue ("instance");
            if (iuuid != null) {
                UUID uuid = UUID.fromString(iuuid);
                if (!uuid.equals (getInstanceId())) {
                    deleteFile (f, iuuid);
                    return false;
                }
            }
            if (QFactory.isEnabled(rootElement)) {
                if (evt != null)
                    evt.addMessage("deploy: " + f.toAbsolutePath().normalize());
                Object obj = factory.instantiate (this, rootElement);
                qentry.setObject (obj);

                ObjectInstance instance = factory.createQBean (
                    this, doc.getRootElement(), obj
                );
                qentry.setInstance (instance);
            } else if (evt != null) {
                evt.addMessage("deploy ignored (enabled='" + QFactory.getEnabledAttribute(rootElement) + "'): " + f.toAbsolutePath().normalize());
            }
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
            if (evt != null)
                evt.addMessage(e);
            return false;
        }
        catch (Exception e) {
            if (evt != null)
                evt.addMessage(e);
            tidyFileAway(f,ERROR_EXTENSION);
            // This will also save deploy error repeats...
            return false;
        } 
        catch (Error e) {
            if (evt != null)
                evt.addMessage(e);
            tidyFileAway(f,ENV_EXTENSION);
            // This will also save deploy error repeats...
            return false;
        } finally {
            if (evt != null)
                Logger.log(evt);
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
            shutdown.await(sleep, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) { }
    }
    public void relax () {
        relax (1000);
    }
    private void initSystemLogger () {
        Path loggerConfig = deployDir.resolve(LOGGER_CONFIG);
        if (Files.isReadable(loggerConfig)) {
            hasSystemLogger = true;
            try {
                register (loggerConfig);
                deploy ();
            } catch (Exception e) {
                getLog().warn ("init-system-logger", e);
            }
        }
        Environment env = Environment.getEnvironment();
        getLog().info("Q2 started, deployDir=" + deployDir.toAbsolutePath().toString() + ", environment=" + env.getName());
        if (env.getErrorString() != null)
            getLog().error(env.getErrorString());

    }
    public Log getLog () {
        if (log == null) {
            Logger logger = Logger.getLogger (LOGGER_NAME);
            if (!hasSystemLogger && !logger.hasListeners() && cli == null)
                logger.addListener (new SimpleLogListener (System.out));
            log = new Log (logger, REALM);
        }
        return log;
    }
    public MBeanServer getMBeanServer () {
        return server;
    }
    public long getUptime() {
        return Duration.between(startTime, Instant.now()).toMillis();
    }
    public void displayVersion () {
        System.out.println(getVersionString());
    }
    public UUID getInstanceId() {
        return instanceId;
    }
    public static String getVersionString() {
        String appVersionString = getAppVersionString();
        int l = PGPHelper.checkLicense();
        String sl = l > 0 ? " " + Integer.toString(l,16) : "";
        StringBuilder vs = new StringBuilder();
        if (appVersionString != null) {
            vs.append(
              String.format ("jPOS %s %s/%s%s (%s)%n%s%s",
                getVersion(), getBranch(), getRevision(), sl, getBuildTimestamp(), appVersionString, getLicensee()
              )
            );
        } else {
            vs.append(
              String.format("jPOS %s %s/%s%s (%s) %s",
                    getVersion(), getBranch(), getRevision(), sl, getBuildTimestamp(), getLicensee()
              )
            );
        }
//        if ((l & 0xE0000) > 0)
//            throw new IllegalAccessError(vs);

        return vs.toString();
    }

    private static String getLicensee() {
        String s = null;
        try {
            s = PGPHelper.getLicensee();
        } catch (IOException ignored) {
            // NOPMD: ignore
        }
        return s;
    }
    private void parseCmdLine (String[] args) {
        CommandLineParser parser = new DefaultParser ();

        Options options = new Options ();
        options.addOption ("v","version", false, "Q2's version");
        options.addOption ("d","deploydir", true, "Deployment directory");
        options.addOption ("r","recursive", false, "Deploy subdirectories recursively");
        options.addOption ("h","help", false, "Usage information");
        options.addOption ("C","config", true, "Configuration bundle");
        options.addOption ("e","encrypt", true, "Encrypt configuration bundle");
        options.addOption ("i","cli", false, "Command Line Interface");
        options.addOption ("c","command", true, "Command to execute");
        options.addOption ("O", "osgi", false, "Start experimental OSGi framework server");
        options.addOption ("p", "pid-file", true, "Store project's pid");
        options.addOption ("n", "name", true, "Optional name (defaults to 'Q2')");
        options.addOption ("s", "ssh", false, "Enable SSH server");
        options.addOption ("sp", "ssh-port", true, "SSH port (defaults to 2222)");
        options.addOption ("sa", "ssh-authorized-keys", true, "Path to authorized key file (defaults to 'cfg/authorized_keys')");
        options.addOption ("su", "ssh-user", true, "SSH user (defaults to 'admin')");
        options.addOption ("sh", "ssh-host-key-file", true, "SSH host key file, defaults to 'cfg/hostkeys.ser'");
        options.addOption ("Ns", "no-scan", false, "Disables deploy directory scan");
        options.addOption ("Nd", "no-dynamic", false, "Disables dynamic classloader");
        options.addOption ("E", "environment", true, "Environment name");

        try {
            CommandLine line = parser.parse (options, args);
            if (line.hasOption ("v")) {
                displayVersion();
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
            } else if (cli != null)
                dir = dir + "-" + "cli";
            recursive = line.hasOption ("r");
            this.deployDir  = Paths.get(dir);
            if (line.hasOption ("C"))
                deployBundle (Paths.get(line.getOptionValue ("C")), false);
            if (line.hasOption ("e"))
                deployBundle (Paths.get(line.getOptionValue ("e")), true);
            if (line.hasOption("O"))
                startOSGI = true;
            if (line.hasOption("p"))
                pidFile = line.getOptionValue("p");
            if (line.hasOption("n"))
                name = line.getOptionValue("n");
            if (line.hasOption("E")) {
                System.setProperty("jpos.env", line.getOptionValue("E"));
                Environment.getEnvironment();
            }
            disableDeployScan = line.hasOption("Ns");
            disableDynamicClassloader = line.hasOption("Nd");
            enableSsh = line.hasOption("s");
            sshPort = Integer.parseInt(line.getOptionValue("sp", "2222"));
            sshAuthorizedKeys = line.getOptionValue ("sa", "cfg/authorized_keys");
            sshUser = line.getOptionValue("su", "admin");
            sshHostKeyFile = line.getOptionValue("sh", "cfg/hostkeys.ser");
        } catch (MissingArgumentException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.exit(1);
        } catch (IllegalAccessError | UnrecognizedOptionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace ();
            System.exit (1);
        }
    }
    private void deployBundle (Path bundle, boolean encrypt)
        throws JDOMException, IOException, 
                ISOException, GeneralSecurityException
    {
        SAXBuilder builder = createSAXBuilder();
        Document doc = builder.build (bundle.toFile());
        Iterator iter = doc.getRootElement().getChildren ().iterator ();
        for (int i=1; iter.hasNext (); i ++) {
            Element e = (Element) iter.next();
            deployElement (e, String.format ("%02d_%s.xml",i, e.getName()), encrypt, !encrypt);
            // the !encrypt above is tricky and deserves an explanation
            // if we are encrypting a QBean, we want it to stay in the deploy
            // directory for future runs. If on the other hand we are deploying
            // a bundle, we want it to be transient.
        }
    }
    public void deployElement (Element e, String fileName, boolean encrypt, boolean isTransient)
        throws IOException, GeneralSecurityException
    {
        e = e.clone ();

        XMLOutputter out = new XMLOutputter (Format.getPrettyFormat());
        Document doc = new Document ();
        doc.setRootElement(e);
        Path qbean = deployDir.resolve(fileName);
        if (isTransient) {
            e.setAttribute("instance", getInstanceId().toString());
            qbean.toFile().deleteOnExit();
        }
        if (encrypt) {
            doc = encrypt (doc);
        }
        try (Writer writer = new BufferedWriter(new FileWriter(qbean.toFile()))) {
            out.output(doc, writer);
        }
    }

    public Framework getOSGIFramework() {
        return osgiFramework;
    }

    private byte[] dodes (byte[] data, int mode) 
       throws GeneralSecurityException
    {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init (mode, new SecretKeySpec(getKey(), "DES"));
        return cipher.doFinal (data);
    }
    protected byte[] getKey() {
        return
          ISOUtil.xor(SystemSeed.getSeed(8, 8),
          ISOUtil.hex2byte(System.getProperty("jpos.deploy.key", "BD653F60F980F788")));
    }
    protected Document encrypt (Document doc)
        throws GeneralSecurityException, IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (os);
        XMLOutputter out = new XMLOutputter (Format.getPrettyFormat());
        out.output(doc, writer);
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
                SAXBuilder builder = createSAXBuilder();
                doc = builder.build (is);
            }
        }
        return doc;
    }

    private void startOSGIFramework() throws BundleException {
        Iterator<FrameworkFactory> iter = ServiceLoader.load(FrameworkFactory.class, loader).iterator();
        if (iter.hasNext()) {
            FrameworkFactory frameworkFactory = iter.next();
            Map<String, String> config = new HashMap<String, String>();
            osgiFramework = frameworkFactory.newFramework(config);
            osgiFramework.start();
        } else {
            getLog().warn("OSGI framework not found");
        }
    }
    private boolean registerOSGIBundle (Path f) {
        BundleContext context = osgiFramework.getBundleContext();
        LogEvent evt = getLog().createLogEvent("osgi", f.getFileName());
        try {
            Bundle bundle = context.installBundle("file:" + f.toAbsolutePath().normalize());
            evt.addMessage("registered");
            bundle.start();
            evt.addMessage("started");
        } catch (BundleException e) {
            evt.addMessage(e);
            return false;
        } finally {
            Logger.log(evt);
        }
        return true;
    }
    private void stopOSGIFramework() {
        if (osgiFramework != null) {
            try {
                osgiFramework.stop();
                osgiFramework.waitForStop(0L);
            } catch (Exception e) {
                getLog().warn(e);
            }
        }
    }
    private void tidyFileAway (Path f, String extension) throws IOException {
        Path rename = f.getParent().resolve(f.getFileName()+"."+extension);
        while (Files.exists(rename)){
            rename = rename.getParent().resolve(rename.getFileName()+"."+extension);
        }
        try {
            Files.move(f, rename);
            getLog().warn("Tidying "+f.toAbsolutePath()+" out of the way, by adding ."+extension,"It will be called: "+rename.toAbsolutePath()+" see log above for detail of problem.");
        } catch (IOException e) {
            getLog().warn("Error (" + e.getMessage() + ") Tidying. Could not tidy  "+f.toAbsolutePath()+" out of the way, by adding ."+extension,"It could not be called: "+rename.toAbsolutePath()+" see log above for detail of problem.");
        }
    }

    private void deleteFile (Path f, String iuuid) throws IOException {
        Files.delete(f);
        getLog().info(String.format("Deleted transient descriptor %s (%s)", f.toAbsolutePath(), iuuid));
    }

    private void initConfigDecorator()
    {
        InputStream in=Q2.class.getClassLoader().getResourceAsStream("META-INF/org/jpos/config/Q2-decorator.properties");
        try
        {
            if(in!=null)
            {
                PropertyResourceBundle bundle=new PropertyResourceBundle(in);
                String ccdClass=bundle.getString("config-decorator-class");
                if(log!=null) log.info("Initializing config decoration provider: "+ccdClass);
                decorator= (ConfigDecorationProvider) Q2.class.getClassLoader().loadClass(ccdClass).newInstance();
                decorator.initialize(deployDir.toFile());
            }
        }
        catch (IOException ignored)
        {
            // NOPMD OK to happen
        }
        catch (Exception e)
        {
            if(log!=null) log.error(e);
            else
            {
                e.printStackTrace();
            }
        }
        finally
        {
            if(in!=null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ignored)
                {
                    // NOPMD nothing to do
                }
            }
        }
    }
    private void logVersion () {
        long now = System.currentTimeMillis();
        if (now - lastVersionLog > 86400000L) {
            LogEvent evt = getLog().createLogEvent("version");
            evt.addMessage(getVersionString());
            Logger.log(evt);
            lastVersionLog = now;
            while (running() && (PGPHelper.checkLicense() & 0xF0000) != 0)
                relax(60000L);
        }
    }
    private void setExit (boolean exit) {
        this.exit = exit;
    }
    private SAXBuilder createSAXBuilder () {
        SAXBuilder builder = new SAXBuilder ();
        builder.setFeature("http://xml.org/sax/features/namespaces", true);
        builder.setFeature("http://apache.org/xml/features/xinclude", true);
        return builder;
    }
    public static void main (String[] args) throws Exception {
        Q2 q2 = new Q2(args);
        q2.setExit (true);
        q2.start();
    }
    public static String getVersion() {
        return getBundle("org/jpos/q2/buildinfo").getString ("version");
    }
    public static String getRevision() {
        return getBundle("org/jpos/q2/revision").getString ("revision");
    }
    public static String getBranch() {
        return getBundle("org/jpos/q2/revision").getString ("branch");
    }
    public static String getBuildTimestamp() {
        return getBundle("org/jpos/q2/buildinfo").getString ("buildTimestamp");
    }
    public static String getRelease() {
        return getVersion() + " " + getRevision();
    }
    public static String getAppVersionString() {
        try {
            ResourceBundle buildinfo = getBundle("buildinfo");
            ResourceBundle revision = getBundle("revision");

            return String.format ("%s %s %s/%s (%s)",
                buildinfo.getString("projectName"),
                buildinfo.getString("version"),
                revision.getString("branch"),
                revision.getString("revision"),
                buildinfo.getString("buildTimestamp")
            );
        } catch (MissingResourceException ignored) {
            return null;
        }
    }
    public boolean isDisableDynamicClassloader() {
        return disableDynamicClassloader;
    }
    public static class QEntry {
        long deployed;
        ObjectInstance instance;
        Object obj;
        boolean osgiBundle;
        public QEntry (boolean osgiBundle) {
            super();
            this.osgiBundle = osgiBundle;
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
        public boolean isOSGIBundle() {
            return osgiBundle;
        }
        public boolean isQPersist () {
            return obj instanceof QPersist;
        }
    }
    public Path getPidPath() {
        if (pidFileChannel == null)
            return null;
        if (pidFileChannel.isOpen()) {
            return pidFilePath;
        }
        return null;
    }
    private void writePidFile() {
        if (pidFile == null)
            return;

        pidFilePath = Paths.get(pidFile);
        try {
            if (Files.isDirectory(pidFilePath)) {
                System.err.printf("Q2: pid-file (%s) is a directory%n", pidFile);
                System.exit(21); // EISDIR
            }

            byte[] mxbean =ManagementFactory.getRuntimeMXBean().getName().split("@")[0].getBytes();
            byte[] linesep= System.lineSeparator().getBytes();
            int len = mxbean.length + linesep.length;
            ByteBuffer buf = ByteBuffer.allocate(len);
            buf.put(mxbean);
            buf.put(linesep);
            buf.flip();
            pidFilePath.toFile().deleteOnExit();
            try {
                pidFileChannel = AsynchronousFileChannel.open(pidFilePath, WRITE, CREATE, DSYNC);
            } catch (IOException e) {
                System.err.printf("Q2: Unable to open pid-file (%s), (%s),%n", pidFile, e.getMessage());
                System.exit(17); // EEXIST
            }
            try {
                int write = pidFileChannel.write(buf, 0).get();
                if (write != len) {
                    throw new IOException ("Failed to write " + len + " bytes to pid-file, return: " + write);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new IOException (e.getMessage());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to write pid-file (%s)", pidFile), e);
        }
    }

    private boolean waitForChanges (WatchService service) throws InterruptedException {
        WatchKey key = service.poll (SCAN_INTERVAL, TimeUnit.MILLISECONDS);
        if (key != null) {
            LogEvent evt = getLog().createInfo();
            for (WatchEvent<?> ev : key.pollEvents()) {
                if (ev.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    evt.addMessage(String.format ("created %s/%s", deployDir.getFileName(), ev.context()));
                } else if (ev.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    evt.addMessage(String.format ("removed %s/%s", deployDir.getFileName(), ev.context()));
                } else if (ev.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    evt.addMessage(String.format ("modified %s/%s", deployDir.getFileName(), ev.context()));
                }
            }
            Logger.log(evt);
            if (!key.reset()) {
                getLog().warn(String.format (
                  "deploy directory '%s' no longer valid",
                  deployDir.toAbsolutePath())
                );
                return false; // deploy directory no longer valid
            }
            try {
                Environment.reload();
            } catch (IOException e) {
                getLog().warn(e);
            }
        }
        return true;
    }

    private void registerQ2() {
        synchronized (Q2.class) {
            for (int i=0; ; i++) {
                String key = name + (i > 0 ? "-" + i : "");
                if (NameRegistrar.getIfExists(key) == null) {
                    NameRegistrar.register(key, this);
                    this.nameRegistrarKey = key;
                    break;
                }
            }
        }
    }

    private void unregisterQ2() {
        synchronized (Q2.class) {
            if (nameRegistrarKey != null) {
                NameRegistrar.unregister(nameRegistrarKey);
                nameRegistrarKey = null;
            }
        }

    }

    private void deployInternal() throws IOException, JDOMException, SAXException, ISOException, GeneralSecurityException {
        extractCfg();
        extractDeploy();
    }
    private void extractCfg() throws IOException {
        List<String> resources = ModuleUtils.getModuleEntries(CFG_PREFIX);
        Path cfg = Paths.get("cfg");
        if (resources.size() > 0)
            Files.createDirectories(cfg);
        for (String resource : resources)
            copyResourceToFile(resource, cfg.resolve(resource.substring(CFG_PREFIX.length())));
    }
    private void extractDeploy() throws IOException, JDOMException, SAXException, ISOException, GeneralSecurityException {
        List<String> qbeans = ModuleUtils.getModuleEntries(DEPLOY_PREFIX);
        for (String resource : qbeans) {
            if (resource.toLowerCase().endsWith(".xml"))
                deployResource(resource);
            else
                copyResourceToFile(resource, Paths.get("cfg").resolve(resource.substring(DEPLOY_PREFIX.length())));

        }
    }

    private void copyResourceToFile(String resource, Path destination) throws IOException {
        // taken from @vsalaman's Install using human readable braces as God mandates
        Path rpath;
        try {
            rpath = Paths.get(getClass().getClassLoader().getResource(resource).toURI());
        } catch (URISyntaxException e) {
            throw new IOException (e.getMessage());
        }
        try (AsynchronousFileChannel source = AsynchronousFileChannel.open(rpath, READ)) {
            int size = (int) source.size();
            ByteBuffer buffer = ByteBuffer.allocate(size);
            int read = 0;
            while (read < size) {
                read += source.read(buffer, read).get();
            }
            buffer.flip();
            try (AsynchronousFileChannel output = AsynchronousFileChannel.open(destination, WRITE, CREATE, SYNC)) {
                int write = output.write(buffer, 0).get();
                if (write != size) {
                    throw new IOException ("Failed to write " + size + " byte resource, return: " + write);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new IOException (e.getMessage());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException (e.getMessage());
        }
    }
    private void deployResource(String resource)
      throws IOException, JDOMException, GeneralSecurityException
    {
        SAXBuilder builder = new SAXBuilder();
        try (InputStream source = getClass().getClassLoader().getResourceAsStream(resource)) {
            Document doc = builder.build(source);
            deployElement (doc.getRootElement(), resource.substring(DEPLOY_PREFIX.length()), false,true);
        }
    }
}
