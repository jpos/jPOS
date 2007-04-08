/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.q2;

import org.jdom.Element;
import org.jpos.util.Log;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import java.util.Iterator;
import java.lang.reflect.Method;
import java.net.URL;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class QBeanSupport 
    implements QBean, QPersist, QBeanSupportMBean, Configurable 
{
    Element persist;
    int state;
    Q2 server;
    boolean modified;
    String name;
    protected Log log;
    protected Configuration cfg;
    public static final String stateString[] = {
        "Stopped", "Stopping", "Starting", "Started", "Failed", "Destroyed"
    };
        
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
    public synchronized void start() {
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
    public synchronized void stop () {
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
    public void shutdownQ2 () {
        getServer().shutdown ();
    }
    public int getState () {
        return state;
    }
    public URL[] getLoaderURLS() {
        return server.getLoader().getURLs();
    }    
    public QClassLoader getLoader() {
        return server.getLoader();
    }
    public String getStateAsString () {
        return state >= 0 ? stateString[state] : "Unknown";
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
    public boolean running () {
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
    protected Iterator getAttrs () {
        return getPersist().getChildren ("attr").iterator();
    }
    protected Iterator getAttrs (String parent) {
        return getPersist().getChild (parent).
            getChildren ("attr").iterator();
    }
    protected void setAttr (Iterator attrs, String name, Object obj) {
        String value = obj == null ? "null" : obj.toString ();
        while (attrs.hasNext ()) {
            Element e = (Element) attrs.next ();
            if (name.equals (e.getAttributeValue ("name")))  {
                e.setText (value);
                break;
            }
        }
    }
    protected Iterator getProperties (String parent) {
        return getPersist().getChild (parent).
               getChildren ("property").iterator();
    }
    protected void setProperty (Iterator props, String name, String value) {
        while (props.hasNext()) {
            Element e = (Element) props.next();
            if (name.equals (e.getAttributeValue ("name"))) {
                e.setAttribute ("value", value);
                break;
            }
        }
    }
    protected String getProperty (Iterator props, String name) {
        while (props.hasNext()) {
            Element e = (Element) props.next();
            if (name.equals (e.getAttributeValue ("name"))) {
                return e.getAttributeValue ("value");
            }
        }
        return null;
    }
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        this.cfg = cfg;
    }
    public Configuration getConfiguration () {
        return cfg;
    }

}

