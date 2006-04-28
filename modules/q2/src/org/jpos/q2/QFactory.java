/*
 * Copyright (c) 2005 jPOS.org.  All rights reserved.
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
import java.io.FileInputStream;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.net.MalformedURLException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import javax.management.*;

import org.jdom.Element;
import org.jpos.core.Configurable;
import org.jpos.core.XmlConfigurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.util.Logger;
import org.jpos.util.LogSource;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class QFactory {
    ObjectName loaderName;
    Q2 q2;
    ResourceBundle classMapping;

    public QFactory (ObjectName loaderName, Q2 q2) {
        super ();
        this.loaderName = loaderName;
        this.q2  = q2;
        classMapping = ResourceBundle.getBundle(this.getClass().getName());
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
               MBeanRegistrationException,
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

    public void getExtraPath (QClassLoader loader, Element e) {
        Element classpathElement = e.getChild ("classpath");
        if (classpathElement != null) {
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
        Properties props = new Properties ();
        Iterator iter = e.getChildren ("property").iterator();
        while (iter.hasNext()) {
            Element property = (Element) iter.next ();
            String name  = property.getAttributeValue("name");
            String value = property.getAttributeValue("value");
            String file  = property.getAttributeValue("file");
            if (file != null)
                try {
                    props.load (new FileInputStream (new File (file)));
                } catch (Exception ex) {
                    throw new ConfigurationException (file, ex);
                }
            else if (name != null && value != null) {
                Object obj = props.get (name);
                if (obj instanceof String[]) {
                    String[] mobj = (String[]) obj;
                    String[] m = new String[mobj.length + 1];
                    System.arraycopy(mobj,0,m,0,mobj.length);
                    m[mobj.length] = value;
                    props.put (name, m);
                } else if (obj instanceof String) {
                    String[] m = new String[2];
                    m[0] = (String) obj;
                    m[1] = value;
                    props.put (name, m);
                } else
                    props.put (name, value);
            }
        }
        return new SimpleConfiguration (props);
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
            Class[] paramTemplate = { pc };
            Method method = obj.getClass().getMethod(m, paramTemplate);
            Object[] param = new Object[1];
            param[0] = p;
            method.invoke (obj, param);
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

