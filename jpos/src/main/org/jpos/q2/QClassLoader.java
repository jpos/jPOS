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
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
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

