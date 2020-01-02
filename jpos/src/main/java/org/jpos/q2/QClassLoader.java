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

import javax.management.*;
import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Q2 Class Loader (scans deploy/lib directory for new jars)
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class QClassLoader
    extends URLClassLoader 
    implements QClassLoaderMBean, FileFilter {
    File libDir;
    ObjectName loaderName;
    MBeanServer server;
    long lastModified;

    public QClassLoader 
        (MBeanServer server, File libDir, ObjectName loaderName, 
         ClassLoader mainClassLoader) 
    {
        super(new URL[] { }, mainClassLoader);
        this.loaderName = loaderName;
        this.libDir     = libDir;
        this.server     = server;
    }
    
    public void addURL (String url) throws MalformedURLException {
        addURL (new URL (url));
    }
    
    public boolean accept (File f) {
        return f.getName().endsWith (".jar");
    }

    public boolean isModified () {
        return libDir.canRead () && lastModified != libDir.lastModified();
    }
    public QClassLoader scan (boolean forceNewClassLoader) 
        throws InstanceAlreadyExistsException,
               InstanceNotFoundException,
               NotCompliantMBeanException,
               MBeanRegistrationException
    
    {
        if (!isModified () && !forceNewClassLoader || !libDir.canRead())
            return this;
        QClassLoader loader;
        if (server.isRegistered (loaderName)) {
            server.unregisterMBean (loaderName);
            loader = new QClassLoader (server, libDir, loaderName, getParent());
        } else
            loader = this;

        File file[] = libDir.listFiles (this);
        for (File aFile : file) {
            try {
                loader.addURL(aFile.toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        loader.lastModified = libDir.lastModified ();
        server.registerMBean (loader, loaderName);
        return loader;
    }
    public void forceNewClassLoaderOnNextScan() {
        this.lastModified = 0L;
    }
}

