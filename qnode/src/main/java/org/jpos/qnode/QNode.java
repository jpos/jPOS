/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2013 Alejandro P. Revilla
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

package org.jpos.qnode;

import org.apache.commons.cli.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;


import java.io.File;
import java.io.FileFilter;
import java.util.*;

import static java.util.ResourceBundle.getBundle;

public class QNode {
    private Framework osgiFramework;
    private File bundleDir;
    public static final String DEFAULT_BUNDLE_DIR = "bundle";

    @SuppressWarnings("unused")
    private QNode() { }

    private QNode (String[] args) throws ParseException {
        parseCmdLine(args);
    }

    public static void main (String[] args) {
        try {
            QNode qnode = new QNode(args);
            qnode.addShutdownHook();
            qnode.startOSGIFramework();
        } catch (Exception e) {
            warn(e);
            System.exit(1);
        }
    }

    public static void displayVersion () {
        System.out.println (getVersionString());
    }

    public static String getVersionString() {
        return String.format("jPOS %s %s/%s (%s)",
                getVersion(), getBranch(), getRevision(), getBuildTimestamp()
        );
    }

    public static String getVersion() {
        return getBundle("org/jpos/qnode/buildinfo").getString("version");
    }

    public static String getRevision() {
        return getBundle("org/jpos/qnode/revision").getString ("revision");
    }

    public static String getBranch() {
        return getBundle("org/jpos/qnode/revision").getString ("branch");
    }

    public static String getBuildTimestamp() {
        return getBundle("org/jpos/qnode/buildinfo").getString ("buildTimestamp");
    }

    private static void warn(String warn) {
        System.out.println(warn);
    }

    private static void warn(Throwable t) {
        t.printStackTrace();
    }

    private void parseCmdLine (String[] args) throws ParseException {
        CommandLineParser parser = new PosixParser();
        Options options = new Options ();
        options.addOption ("v", "version", false, "QNode's version");
        options.addOption ("b", "bundleDir", true, "bundle directory (defaults to 'bundle')");
        CommandLine line = parser.parse (options, args);
        if (line.hasOption ("v")) {
            displayVersion();
            System.exit (0);
        }
        bundleDir = new File(line.getOptionValue("b", DEFAULT_BUNDLE_DIR));
    }

    private void addShutdownHook () {
        Runtime.getRuntime().addShutdownHook (
            new Thread ("QNode-ShutdownHook") {
                public void run () {
                    stopOSGIFramework();
                }
            }
        );
    }

    private void startOSGIFramework() throws BundleException {
        Iterator<FrameworkFactory> iter = ServiceLoader.load(FrameworkFactory.class).iterator();
        if (iter.hasNext()) {
            FrameworkFactory frameworkFactory = iter.next();
            Map<String, String> config = new HashMap<String, String>();
            osgiFramework = frameworkFactory.newFramework(config);
            osgiFramework.start();
            scanBundleDir();
        } else {
            warn("OSGI framework not found");
        }
    }

    private void stopOSGIFramework() {
        if (osgiFramework != null) {
            try {
                osgiFramework.stop();
                osgiFramework.waitForStop(0L);
            } catch (Exception e) {
                warn(e);
            }
        }
    }

    private void scanBundleDir() throws BundleException {
        File bundles[] = bundleDir.listFiles (new FileFilter() {
            @Override
            public boolean accept(File f) {
            return f.canRead() && f.getName().toLowerCase().endsWith(".jar");
            }
        });
        if (bundles != null) {
            Arrays.sort (bundles);
            if (bundles != null) {
                List<Bundle> bundleList = new ArrayList<Bundle>(bundles.length);
                for (File f : bundles) {
                    bundleList.add(installOSGIBundle(f));
                }
                for (Bundle b : bundleList) {
                    b.start();
                }
            }
        }
    }

    private Bundle installOSGIBundle (File b) throws BundleException {
        BundleContext context = osgiFramework.getBundleContext();
        return context.installBundle("file:" + b.getAbsolutePath());
    }
}
