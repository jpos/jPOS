/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.q2;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistrationException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.NotCompliantMBeanException;

/**
 * Q2 Class Loader (scans deploy/lib directory for new jars)
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @version $Revision$ $Date$
 * @jmx:mbean description="Q2 Class Loader"
 */
public class QClassLoader 
    extends URLClassLoader 
    implements QClassLoaderMBean, FileFilter {
    File libDir;
    ObjectName loaderName;
    MBeanServer server;
    long lastModified;

    public QClassLoader 
        (MBeanServer server, File libDir, ObjectName loaderName) 
    {
        super(new URL[] { }, Thread.currentThread().getContextClassLoader());
        this.loaderName = loaderName;
        this.libDir     = libDir;
        this.server     = server;
    }
    
    /**
     * @jmx:managed-operation description=""
     * @jmx:managed-operation-parameter name="url" position="0" description=""
     */
    public void addURL (String url) throws MalformedURLException {
        addURL (new URL (url));
    }
    
    public boolean accept (File f) {
        return f.getName().endsWith (".jar");
    }

    public boolean isModified () {
        return libDir.canRead () && (lastModified != libDir.lastModified());
    }

    public QClassLoader scan () 
        throws InstanceAlreadyExistsException,
               InstanceNotFoundException,
               NotCompliantMBeanException,
               MalformedURLException,
               MBeanRegistrationException
    
    {
        if (!isModified ()) 
            return this;
        QClassLoader loader;
        if (server.isRegistered (loaderName)) {
            server.unregisterMBean (loaderName);
            loader = new QClassLoader (server, libDir, loaderName);
        } else
            loader = this;

        File file[] = libDir.listFiles (this);
        for (int i=0; i<file.length; i++) {
            try {
                loader.addURL (file[i].toURL ());
            } catch (MalformedURLException e) {
                e.printStackTrace ();
            }
        }
        loader.lastModified = libDir.lastModified ();
        server.registerMBean (loader, loaderName);
        return loader;
    }
    
}

