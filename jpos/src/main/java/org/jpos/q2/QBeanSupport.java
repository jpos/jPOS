/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

import org.jdom2.Element;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Convenience base class for QBean implementations; provides configuration, logging, and lifecycle support.
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 */
public class QBeanSupport
    implements QBean, QPersist, QBeanSupportMBean, Configurable
{
    /** Persistent configuration element for this QBean. */
    Element persist;
    /** Current lifecycle state of this QBean. */
    int state;
    /** The Q2 server that manages this QBean. */
    Q2 server;
    final Object modifyLock = new Object();
    boolean modified;
    String name;
    /** Logger used by this QBean. */
    protected Log log;
    /** Configuration supplied to this QBean. */
    protected Configuration cfg;
    /** Executor for scheduled tasks; lazily initialised. */
    protected ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public QBeanSupport () {
        super();
        setLogger (Q2.LOGGER_NAME);
        state = -1;
    }

    @Override
    public void setServer (Q2 server) {
        this.server = server;
    }

    @Override
    public Q2 getServer () {
        return server;
    }
    /**
     * Returns the Q2 factory associated with the server.
     * @return the QFactory instance
     */
    public QFactory getFactory () {
        return getServer().getFactory ();
    }

    @Override
    public void setName (String name) {
        if (this.name == null)
            this.name = name;
        if (log != null)
            log.setRealm (name);
        setModified (true);
    }

    @Override
    public void setLogger (String loggerName) {
        log = Log.getLog (loggerName, getClass().getName());
        setModified (true);
    }

    @Override
    public void setRealm (String realm) {
        if (log != null)
            log.setRealm (realm);
    }

    @Override
    public String getRealm() {
        return log != null ? log.getRealm() : null;
    }

    @Override
    public String getLogger () {
        return log != null ? log.getLogger().getName() : null;
    }

    /**
     * Returns the {@link Log} instance for this QBean.
     * @return the log instance
     */
    public Log getLog () {
        return log;
    }

    @Override
    public String getName () {
        return name;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void destroy () {
        if (state == QBean.DESTROYED)
           return;
        if (state != QBean.STOPPED)
           stop();

        if (scheduledThreadPoolExecutor != null) {
            scheduledThreadPoolExecutor.shutdown();
            scheduledThreadPoolExecutor = null;
        }
        try {
           destroyService();
        }
        catch (Throwable t) {
           log.warn ("destroy", t);
        }
        state = QBean.DESTROYED;
    }

    @Override
    public int getState () {
        return state;
    }

    @Override
    public URL[] getLoaderURLS() {
        return server.getLoader().getURLs();
    }

    @Override
    public QClassLoader getLoader() {
        return server.getLoader();
    }

    @Override
    public String getStateAsString () {
        return state >= 0 ? stateString[state] : "Unknown";
    }

    /**
     * Sets the lifecycle state of this QBean directly.
     * @param state the new state value
     */
    public void setState (int state) {
        this.state = state;
    }

    @Override
    public void setPersist (Element persist) {
        this.persist = persist ;
    }

    @Override
    public Element getPersist () {
        setModified (false);
        return persist;
    }

    /**
     * Marks this QBean's configuration as modified or unmodified.
     * @param modified {@code true} if the configuration has changed
     */
    public void setModified (boolean modified) {
        synchronized (this.modifyLock) {
            this.modified = modified;
        }
    }

    @Override
    public boolean isModified () {
        synchronized (this.modifyLock) {
            return modified;
        }
    }

    /**
     * Returns {@code true} if this QBean is starting or started.
     * @return true if the bean is active
     */
    public boolean running () {
        return state == QBean.STARTING || state == QBean.STARTED;
    }

    @Override
    public void setConfiguration (Configuration cfg)
      throws ConfigurationException
    {
        this.cfg = cfg;
    }
    /**
     * Returns the configuration for this QBean.
     * @return the configuration
     */
    public Configuration getConfiguration () {
        return cfg;
    }

    /**
     * Returns a human-readable dump of this QBean's state.
     * @return dump string
     */
    public String getDump () {
        if (this instanceof Loggeable) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream p = new PrintStream(baos);
            ((Loggeable)this).dump(p, "");
            return baos.toString();
        }
        return toString();
    }
    /** Called during QBean initialization. Override to add startup logic.
     * @throws Exception on error
     */
    protected void initService()    throws Exception {}
    /** Called when the QBean is started. Override to begin processing.
     * @throws Exception on error
     */
    protected void startService()   throws Exception {}
    /** Called when the QBean is stopped. Override to cease processing.
     * @throws Exception on error
     */
    protected void stopService()    throws Exception {}
    /** Called during QBean destruction. Override to release resources.
     * @throws Exception on error
     */
    protected void destroyService() throws Exception {}

    /**
     * Returns (lazily creating) the scheduled executor for this QBean.
     * @return the scheduled thread pool executor
     */
    protected synchronized ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {
        if (scheduledThreadPoolExecutor == null)
            scheduledThreadPoolExecutor = ConcurrentUtil.newScheduledThreadPoolExecutor();
        return scheduledThreadPoolExecutor;
    }

    /**
     * Creates a persist {@link Element} for this QBean using JMX bean introspection.
     * @param name the element name
     * @param mbeanClass the MBean class to introspect
     * @return the constructed Element
     */
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
            for (PropertyDescriptor aDesc : desc) {
                if (aDesc.getWriteMethod() != null) {
                    Method read = aDesc.getReadMethod();
                    Object obj = read.invoke(this);
                    String type = read.getReturnType().getName();
                    if ("java.lang.String".equals(type))
                        type = null;

                    addAttr(e, aDesc.getName(), obj, type);
                }
            }
        } catch (Exception ex) {
            log.warn ("get-persist", ex);
        }
        return e;
    }
    /**
     * Adds an attribute element to the given persist element.
     * @param e the parent element
     * @param name the attribute name
     * @param obj the attribute value object
     * @param type the type hint, or {@code null} for String
     */
    protected void addAttr (Element e, String name, Object obj, String type) {
        String value = obj == null ? "null" : obj.toString();
        Element attr = new Element ("attr");
        attr.setAttribute ("name", name);
        if (type != null)
            attr.setAttribute ("type", type);
        attr.setText (value);
        e.addContent (attr);
    }
    /**
     * Returns an iterator over all {@code attr} child elements of the persist element.
     * @return iterator over attr elements
     */
    protected Iterator getAttrs () {
        return getPersist().getChildren ("attr").iterator();
    }
    /**
     * Returns an iterator over {@code attr} children of the named child element.
     * @param parent name of the parent child element
     * @return iterator over attr elements
     */
    protected Iterator getAttrs (String parent) {
        return getPersist().getChild(parent).
            getChildren("attr").iterator();
    }
    /**
     * Updates a named attribute in the given iterator with a new value.
     * @param attrs iterator over attr elements
     * @param name the attribute name to find
     * @param obj the new value object
     */
    protected void setAttr (Iterator attrs, String name, Object obj) {
        String value = obj == null ? "null" : obj.toString ();
        while (attrs.hasNext ()) {
            Element e = (Element) attrs.next ();
            if (name.equals (e.getAttributeValue ("name")))  {
                e.setText(value);
                break;
            }
        }
    }
    /**
     * Returns an iterator over {@code property} children of the named parent element.
     * @param parent name of the parent child element
     * @return iterator over property elements
     */
    protected Iterator getProperties (String parent) {
        return getPersist().getChild (parent).
               getChildren ("property").iterator();
    }
    /**
     * Updates a named property in the given iterator with a new value.
     * @param props iterator over property elements
     * @param name the property name to find
     * @param value the new value string
     */
    protected void setProperty (Iterator props, String name, String value) {
        while (props.hasNext()) {
            Element e = (Element) props.next();
            if (name.equals (e.getAttributeValue ("name"))) {
                e.setAttribute ("value", value);
                break;
            }
        }
    }
    /**
     * Returns the value of a named property from the given iterator.
     * @param props iterator over property elements
     * @param name the property name to find
     * @return the property value string, or {@code null} if not found
     */
    protected String getProperty (Iterator props, String name) {
        while (props.hasNext()) {
            Element e = (Element) props.next();
            if (name.equals (e.getAttributeValue ("name"))) {
                return e.getAttributeValue ("value");
            }
        }
        return null;
    }
    /**
     * Closes the given {@link Closeable} instances, logging any exceptions.
     * @param closeables the resources to close
     */
    protected void close (Closeable... closeables) {
        LogEvent evt = null;
        for (Closeable c : closeables) {
            try {
                c.close();
            } catch (Exception e) {
                if (evt == null)
                    evt = getLog().createWarn();
                evt.addMessage(e);
            }
        }
        if (evt != null)
            Logger.log(evt);
    }
}
