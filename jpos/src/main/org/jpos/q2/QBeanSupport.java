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

import org.jdom.Element;
import org.jpos.util.Log;
import org.jpos.util.Logger;

import java.lang.reflect.Method;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class QBeanSupport implements QBean, QPersist, QBeanSupportMBean {
    Element persist;
    int state;
    Q2 server;
    boolean modified;
    String name;
    protected Log log;

    public QBeanSupport () {
        super();
        setLogger (Q2.LOGGER_NAME);
        state = -1;
    }
    public void setServer (Q2 server) {
        this.server = server;
    }
    public Q2 getServer () {
        return server;
    }
    public QFactory getFactory () {
        return getServer().getFactory ();
    }
    public void setName (String name) {
        if (this.name == null) 
            this.name = name;
        log.setRealm (name);
        setModified (true);
    }
    public void setLogger (String loggerName) {
        log = Log.getLog (loggerName, getClass().getName());
        setModified (true);
    }

    public String getLogger () {
    	return log.getLogger().getName();
    }
    public Log getLog () {
        return log;
    }
    public String getName () {
        return name;
    }
    public void init () {
        if (state == -1) {
            setModified (false);
            try {
                initService();
                state = QBean.STOPPED;
            } catch (Throwable t) {
                log.warn ("init", t);
            }
        }
    }
    public void start() {
        if (state != QBean.DESTROYED && 
            state != QBean.STOPPED   && 
            state != QBean.FAILED)
           return;

        this.state = QBean.STARTING;

        try {
           startService();
        } catch (Throwable t) {
           state = QBean.FAILED;
           log.warn ("start", t);
           return;
        }
        state = QBean.STARTED;
    }
    public void stop () {
        if (state != QBean.STARTED)
           return;
        state = QBean.STOPPING;
        try {
           stopService();
        } catch (Throwable t) {
           state = QBean.FAILED;
           log.warn ("stop", t);
           return;
        }
        state = QBean.STOPPED;
    }
    public void destroy () {
        if (state == QBean.DESTROYED)
           return;
        if (state != QBean.STOPPED)
           stop();

        try {
           destroyService();
        }
        catch (Throwable t) {
           log.warn ("destroy", t);
        }
        state = QBean.DESTROYED;
    }
    public int getState () {
        return state;
    }
    public void setState (int state) {
        this.state = state;
    }
    public void setPersist (Element persist) {
        this.persist = persist ;
    }
    public synchronized Element getPersist () {
        setModified (false);
        return persist;
    }
    public synchronized void setModified (boolean modified) {
        this.modified = modified;
    }
    public synchronized boolean isModified () {
        return modified;
    }
    protected boolean running () {
        return state == QBean.STARTING || state == QBean.STARTED;
    }
    protected void initService()    throws Exception {}
    protected void startService()   throws Exception {}
    protected void stopService()    throws Exception {}
    protected void destroyService() throws Exception {}

    protected Element createElement (String name, Class mbeanClass) {
        Element e = new Element (name);
        Element classPath = persist != null ?
            persist.getChild ("classpath") : null;
        if (classPath != null)
            e.addContent (classPath);
        e.setAttribute ("class", getClass().getName());
        if (!e.getName().equals (getName ()))
            e.setAttribute ("name", getName());
        String loggerName = getLogger();
        if (loggerName != null)
            e.setAttribute ("logger", loggerName);

        try {
            BeanInfo info = Introspector.getBeanInfo (mbeanClass);
            PropertyDescriptor[] desc = info.getPropertyDescriptors();
            for (int i=0; i<desc.length; i++) {
                if (desc[i].getWriteMethod() != null) {
                    Method read = desc[i].getReadMethod();
                    Object obj  = read.invoke (this, new Object[] { } );
                    String type = read.getReturnType().getName();
                    if ("java.lang.String".equals (type))
                        type = null;

                    addAttr (e, desc[i].getName(), obj, type);
                }
            }
        } catch (Exception ex) {
            log.warn ("get-persist", ex);
        } 
        return e;
    }
    protected void addAttr (Element e, String name, Object obj, String type) {
        String value = obj == null ? "null" : obj.toString();
        Element attr = new Element ("attr");
        attr.setAttribute ("name", name);
        if (type != null)
            attr.setAttribute ("type", type);
        attr.setText (value);
        e.addContent (attr);
    }
}

