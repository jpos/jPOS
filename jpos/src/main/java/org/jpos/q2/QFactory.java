/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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


import org.jdom.Element;
import org.jpos.core.*;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

import javax.management.*;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.*;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class QFactory {
    ObjectName loaderName;
    Q2 q2;
    ResourceBundle classMapping;
    ConfigurationFactory defaultConfigurationFactory = new SimpleConfigurationFactory();

    public QFactory (ObjectName loaderName, Q2 q2) {
        super ();
        this.loaderName = loaderName;
        this.q2  = q2;
        try {
            classMapping = ResourceBundle.getBundle(this.getClass().getName());
        } catch (MissingResourceException ignored) { }
    }

    public Object instantiate (Q2 server, Element e) 
        throws ReflectionException,
               MBeanException,
               InstanceNotFoundException
    {
        String clazz  = e.getAttributeValue ("class");
        if (clazz == null) {
            try {
                clazz = classMapping.getString (e.getName());
            } catch (MissingResourceException ex) {
                // no class attribute, no mapping
                // let MBeanServer do the yelling
            }
        }
        MBeanServer mserver = server.getMBeanServer();
        getExtraPath (server.getLoader (), e);
        return mserver.instantiate (clazz, loaderName);
    }

    public ObjectInstance createQBean (Q2 server, Element e, Object obj) 
        throws ClassNotFoundException, 
               InstantiationException,
               IllegalAccessException,
               MalformedObjectNameException,
               MalformedURLException,
               InstanceAlreadyExistsException,
               InstanceNotFoundException,
               MBeanException,
               NotCompliantMBeanException,
               InvalidAttributeValueException,
               ReflectionException,
               ConfigurationException
    {
        String name   = e.getAttributeValue ("name");
        if (name == null)
            name = e.getName ();

        ObjectName objectName = new ObjectName (Q2.QBEAN_NAME + name);
        MBeanServer mserver = server.getMBeanServer();
        if(mserver.isRegistered(objectName)) {
            throw new InstanceAlreadyExistsException (name+" has already been deployed in another file.");
        }
        ObjectInstance instance = mserver.registerMBean (
            obj, objectName 
        );
        try {
            setAttribute (mserver, objectName, "Name", name);
            String logger = e.getAttributeValue ("logger");
            if (logger != null)
                setAttribute (mserver, objectName, "Logger", logger);
            String realm = e.getAttributeValue ("realm");
            if (realm != null)
                setAttribute (mserver, objectName, "Realm", realm);
            setAttribute (mserver, objectName, "Server", server);
            setAttribute (mserver, objectName, "Persist", e);
            configureQBean(mserver,objectName,e);
            setConfiguration (obj, e);  // handle legacy (QSP v1) Configurables 

            if (obj instanceof QBean) 
                mserver.invoke (objectName, "init",  null, null);
        }
        catch (ConfigurationException ce) {
            mserver.unregisterMBean(objectName);
            throw ce;
        }

        return instance;
    }
    public Q2 getQ2() {
        return q2;
    }
    public void getExtraPath (QClassLoader loader, Element e) {
        Element classpathElement = e.getChild ("classpath");
        if (classpathElement != null) {
            try {
                loader = loader.scan (true);    // force a new classloader
            } catch (Throwable t) {
                getQ2().getLog().error(t);
            }
            Iterator iter = classpathElement.getChildren ("url").iterator();
            while (iter.hasNext ()) {
                Element u = (Element) iter.next ();
                try {
                    loader.addURL (u.getTextTrim ());
                } catch (MalformedURLException ex) {
                    q2.getLog().warn (u.getTextTrim(), ex);
                }
            }
        }
    }

    public void setAttribute 
        (MBeanServer server, ObjectName objectName, 
         String attribute, Object value)
        throws InstanceNotFoundException,
               MBeanException,
               InvalidAttributeValueException,
               ReflectionException
    {
        try {
            server.setAttribute (
                objectName, new Attribute (attribute, value)
            );
        } catch (AttributeNotFoundException ex) {
            // okay to fail
        } catch (InvalidAttributeValueException ex) {
            // okay to fail (produced by some application servers instead of AttributeNotFoundException)
        }
    }

    public void startQBean (Q2 server, ObjectName objectName)
        throws ClassNotFoundException, 
               InstantiationException,
               IllegalAccessException,
               MalformedObjectNameException,
               MalformedURLException,
               InstanceAlreadyExistsException,
               MBeanRegistrationException,
               InstanceNotFoundException,
               MBeanException,
               NotCompliantMBeanException,
               InvalidAttributeValueException,
               ReflectionException
    {
        MBeanServer mserver = server.getMBeanServer();
        mserver.invoke (objectName, "start",  null, null);
    }

    public void destroyQBean (Q2 server, ObjectName objectName, Object obj)
        throws ClassNotFoundException, 
               InstantiationException,
               IllegalAccessException,
               MalformedObjectNameException,
               MalformedURLException,
               InstanceAlreadyExistsException,
               MBeanRegistrationException,
               InstanceNotFoundException,
               MBeanException,
               NotCompliantMBeanException,
               InvalidAttributeValueException,
               ReflectionException
    {
        MBeanServer mserver = server.getMBeanServer();
        if (obj instanceof QBean) {
            mserver.invoke (objectName, "stop",  null, null);
            mserver.invoke (objectName, "destroy",  null, null);
        }
        if (objectName != null)
            mserver.unregisterMBean (objectName);
    }

    public void configureQBean(MBeanServer server, ObjectName objectName, Element e)
        throws ConfigurationException
    {
        try {
            AttributeList attributeList = getAttributeList(e);
            Iterator attributes = attributeList.iterator();
            while (attributes.hasNext())
                server.setAttribute(objectName,(Attribute)attributes.next());
        } catch (Exception e1) {
            throw new ConfigurationException(e1);
        }

    }
    public AttributeList getAttributeList(Element e)
        throws ConfigurationException
    {
        AttributeList attributeList = new AttributeList();
        List childs = e.getChildren("attr");
        Iterator childsIterator = childs.iterator();
        while (childsIterator.hasNext())
        {
            Element  childElement = (Element)childsIterator.next();
            String name = childElement.getAttributeValue("name");
            name = getAttributeName(name);
            Attribute attr =  new Attribute(name,getObject(childElement));
            attributeList.add(attr);
        }
        return attributeList;
    }

    /** creates an object from a definition element.
     * The element may have an attribute called type indicating the type of the object
     * to create, if this attribute is not present java.lang.String is assumed.
     * int, long and boolean are converted to their wrappers.
     * @return The created object.
     * @param childElement Dom Element with the definition of the object.
     * @throws ConfigurationException If an exception is found trying to create the object.
     */    
    protected Object getObject(Element childElement) 
        throws ConfigurationException
    {
        String type = childElement.getAttributeValue("type","java.lang.String");
        if ("int".equals (type))
            type = "java.lang.Integer";
        else if ("long".equals (type))
            type = "java.lang.Long";
        else if ("boolean".equals (type))
            type = "java.lang.Boolean";
       
        String value = childElement.getText();
        try {
            Class attributeType = Class.forName(type);
            if(Collection.class.isAssignableFrom(attributeType))
                return getCollection(attributeType, childElement);
            else{
                Class[] parameterTypes = {"".getClass()};
                Object[] parameterValues = {value};
                return attributeType.getConstructor(parameterTypes).newInstance(parameterValues);
            }
        } catch (Exception e1) {
            throw new ConfigurationException(e1);
        }
        
    }
    
    
    /** Creats a collection from a definition element with the format.
     * <PRE>
     *    <{attr|item} type="...">
     *        <item [type="..."]>...</item>
     *        ...
     *    </attr>
     * </PRE>
     * @param type
     * @param e
     * @throws ConfigurationException
     * @return
     */    
    protected Collection getCollection(Class type, Element e) 
        throws ConfigurationException
    {
        try{
            Collection col = (Collection)type.newInstance();
            Iterator childs = e.getChildren("item").iterator();
            while(childs.hasNext()) col.add(getObject((Element)childs.next()));
            return col;
        }catch(Exception e1){
            throw new ConfigurationException(e1);
        }
    }
    /**
     * sets the first character of the string to the upper case
     * @param name
     * @return attribute name
     */
    public String getAttributeName(String name)
    {
        StringBuffer tmp = new StringBuffer(name);
        tmp.setCharAt(0,name.toUpperCase().charAt(0)) ;
        return tmp.toString();
    }

    public Object newInstance (String clazz)
        throws ConfigurationException
    {
        try {
            MBeanServer mserver = q2.getMBeanServer();
            return mserver.instantiate (clazz, loaderName);
        } catch (Exception e) {
            throw new ConfigurationException (clazz, e);
        }
    }

    public Configuration getConfiguration (Element e) 
        throws ConfigurationException
    {
        String configurationFactoryClazz = e.getAttributeValue("configuration-factory");
        ConfigurationFactory cf = configurationFactoryClazz != null ?
            (ConfigurationFactory) newInstance(configurationFactoryClazz) : defaultConfigurationFactory;
        return cf.getConfiguration(e);
    }

    public void setLogger (Object obj, Element e) {
        if (obj instanceof LogSource) {
            String loggerName = e.getAttributeValue ("logger");
            if (loggerName != null) {
                String realm = e.getAttributeValue ("realm");
                if (realm == null)
                    realm = e.getName();
                Logger logger = Logger.getLogger (loggerName);
                ((LogSource)obj).setLogger (logger, realm);
            }
        }
    }
    public void setConfiguration (Object obj, Element e) 
        throws ConfigurationException 
    {
        try {
            if (obj instanceof Configurable)
                ((Configurable)obj).setConfiguration (getConfiguration (e));
            if (obj instanceof XmlConfigurable)
                ((XmlConfigurable)obj).setConfiguration(e);
        } catch (ConfigurationException ex) {
            throw new ConfigurationException (ex);
        }
    }
   /**
    * Try to invoke a method (usually a setter) on the given object
    * silently ignoring if method does not exist
    * @param obj the object
    * @param m method to invoke
    * @param p parameter
    * @throws ConfigurationException if method happens to throw an exception
    */
    public static void invoke (Object obj, String m, Object p) 
        throws ConfigurationException 
    {
        invoke (obj, m, p, p != null ? p.getClass() : null);
    }
   /**
    * Try to invoke a method (usually a setter) on the given object
    * silently ignoring if method does not exist
    * @param obj the object
    * @param m method to invoke
    * @param p parameter
    * @param pc parameter class
    * @throws ConfigurationException if method happens to throw an exception
    */
    public static void invoke (Object obj, String m, Object p, Class pc) 
        throws ConfigurationException 
    {
        try {
            if (p!=null) {
                Class[] paramTemplate = { pc };
                Method method = obj.getClass().getMethod(m, paramTemplate);
                Object[] param = new Object[1];
                param[0] = p;
                method.invoke (obj, param);
            } else {
                Method method = obj.getClass().getMethod(m,null);
                method.invoke (obj,null);
           }
        } catch (NoSuchMethodException e) { 
        } catch (NullPointerException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
            throw new ConfigurationException (
                obj.getClass().getName() + "." + m + "("+p.toString()+")" ,
                ((Exception) e.getTargetException())
            );
        }
    }
}

