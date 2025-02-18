/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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


import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jpos.core.*;
import org.jpos.core.annotation.Config;
import org.jpos.q2.qbean.QConfig;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import javax.management.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
@SuppressWarnings("unchecked")
public class QFactory {
    ObjectName loaderName;
    Q2 q2;
    ResourceBundle classMapping;
    ConfigurationFactory defaultConfigurationFactory = new SimpleConfigurationFactory();

    public QFactory (ObjectName loaderName, Q2 q2) {
        super ();
        this.loaderName = loaderName;
        this.q2  = q2;
        classMapping = ResourceBundle.getBundle(this.getClass().getName());
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    public Object instantiate (Q2 server, Element e) 
        throws ReflectionException,
               MBeanException,
               InstanceNotFoundException
    {
        String clazz  = getAttributeValue (e, "class");
        if (clazz == null) {
            try {
                clazz = classMapping.getString (e.getName());
            } catch (MissingResourceException ex) {
                // no class attribute, no mapping
                // let MBeanServer do the yelling
            }
        }
        MBeanServer mserver = server.getMBeanServer();
        if (!q2.isDisableDynamicClassloader())
            getExtraPath(server.getLoader(), e);
        return mserver.instantiate (clazz, loaderName);
    }

    public ObjectInstance createQBean (Q2 server, Element e, Object obj) 
        throws MalformedObjectNameException,
               InstanceAlreadyExistsException,
               InstanceNotFoundException,
               MBeanException,
               NotCompliantMBeanException,
               InvalidAttributeValueException,
               ReflectionException,
               ConfigurationException
    {
        String name   = getAttributeValue (e, "name");
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
            String logger = getAttributeValue (e, "logger");
            if (logger != null)
                setAttribute (mserver, objectName, "Logger", logger);
            String realm = getAttributeValue (e, "realm");
            if (realm != null)
                setAttribute (mserver, objectName, "Realm", realm);
            setAttribute (mserver, objectName, "Server", server);
            setAttribute (mserver, objectName, "Persist", e);
            configureQBean(mserver,objectName,e);
            setConfiguration (obj, e);  // handle legacy (QSP v1) Configurables 

            if (obj instanceof QBean)
                mserver.invoke (objectName, "init",  null, null);
        }
        catch (Throwable t) {
            mserver.unregisterMBean(objectName);
            t.fillInStackTrace();
            throw t;
        }

        return instance;
    }
    public Q2 getQ2() {
        return q2;
    }

    private void getExtraPath (QClassLoader loader, Element e) {
        Element classpathElement = e.getChild ("classpath");
        if (classpathElement != null) {
            try {
                loader = loader.scan (true);    // force a new classloader
            } catch (Throwable t) {
                getQ2().getLog().error(t);
            }
            for (Object o : classpathElement.getChildren("url")) {
                Element u = (Element) o;
                try {
                    loader.addURL(u.getTextTrim());
                } catch (MalformedURLException ex) {
                    q2.getLog().warn(u.getTextTrim(), ex);
                }
            }
        }
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
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
        throws InstanceNotFoundException,
               MBeanException,
               ReflectionException
    {
        MBeanServer mserver = server.getMBeanServer();
        mserver.invoke (objectName, "start",  null, null);
    }

    public void destroyQBean (Q2 server, ObjectName objectName, Object obj)
        throws InstanceNotFoundException,
               MBeanException,
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
            for (Object anAttributeList : attributeList)
                server.setAttribute(objectName, (Attribute) anAttributeList);
        } catch (Exception e1) {
            throw new ConfigurationException(e1);
        }

    }
    public AttributeList getAttributeList(Element e)
        throws ConfigurationException
    {
        AttributeList attributeList = new AttributeList();
        List childs = e.getChildren("attr");
        for (Object child : childs) {
            Element childElement = (Element) child;
            String name = childElement.getAttributeValue("name");
            name = getAttributeName(name);
            Attribute attr = new Attribute(name, getObject(childElement));
            attributeList.add(attr);
        }
        return attributeList;
    }

    /**
     * Creates an object from a definition element.
     * The element may have an attribute called type indicating the type of the object
     * to create, if this attribute is not present java.lang.String is assumed.
     * int, long and boolean are converted to their wrappers.
     * @return The created object.
     * @param childElement Dom Element with the definition of the object.
     * @throws ConfigurationException If an exception is found trying to create the object.
     */
    @SuppressWarnings("unchecked")
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
        value = Environment.getEnvironment().getProperty(value, value);
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
     * @param type class type
     * @param e the Element
     * @throws ConfigurationException
     * @return the object collection
     */
    @SuppressWarnings("unchecked")
    protected Collection getCollection(Class type, Element e)
        throws ConfigurationException
    {
        try{
            Collection<Object> col = (Collection<Object>) type.newInstance();
            for (Object o : e.getChildren("item")) {
                col.add(getObject((Element) o));
            }
            return col;
        }catch(Exception e1){
            throw new ConfigurationException(e1);
        }
    }
    /**
     * sets the first character of the string to the upper case
     * @param name attribute name
     * @return attribute name
     */
    public String getAttributeName(String name)
    {
        if (name == null)
            throw new NullPointerException("attribute name can not be null");
        StringBuilder tmp = new StringBuilder(name);
        if (tmp.length() > 0)
            tmp.setCharAt(0,name.toUpperCase().charAt(0)) ;
        return tmp.toString();
    }

    public <T> T newInstance (String clazz)
        throws ConfigurationException
    {
        try {
            MBeanServer mserver = q2.getMBeanServer();
            return (T)mserver.instantiate (clazz, loaderName);
        } catch (Exception e) {
            throw new ConfigurationException (clazz, e);
        }
    }

    public <T> T newInstance (Class<T> clazz)
            throws ConfigurationException
    {
        return newInstance(clazz.getName());
    }

    public Configuration getConfiguration (Element e)
        throws ConfigurationException
    {
        String configurationFactoryClazz = getAttributeValue(e, "configuration-factory");
        ConfigurationFactory cf = configurationFactoryClazz != null ?
            (ConfigurationFactory) newInstance(configurationFactoryClazz) : defaultConfigurationFactory;

        Configuration cfg = cf.getConfiguration(e);
        String merge = getAttributeValue(e, "merge-configuration");
        if (merge != null) {
            StringTokenizer st = new StringTokenizer(merge, ", ");
            while (st.hasMoreElements()) {
                try {
                    Configuration c = QConfig.getConfiguration(st.nextToken());
                    for (String k : c.keySet()) {
                        if (cfg.get(k, null) == null) {
                            String[] v = c.getAll(k);
                            switch (v.length) {
                                case 0:
                                    break;
                                case 1:
                                    cfg.put(k, v[0]);
                                    break;
                                default:
                                    cfg.put(k, v);
                            }
                        }
                    }
                } catch (NameRegistrar.NotFoundException ex) {
                    throw new ConfigurationException (ex.getMessage());
                }
            }
        }
        return cfg;
    }

    public void setLogger (Object obj, Element e) {
        if (obj instanceof LogSource) {
            String loggerName = getAttributeValue (e, "logger");
            if (loggerName != null) {
                String realm = getAttributeValue (e, "realm");
                if (realm == null)
                    realm = e.getName();
                Logger logger = Logger.getLogger (loggerName);
                ((LogSource)obj).setLogger (logger, realm);
            }
        }
    }

    public static String getAttributeValue (Element e, String name) {
        String s = e.getAttributeValue(name);
        return Environment.getEnvironment().getProperty(s, s);
    }
    public void setConfiguration (Object obj, Element e)
        throws ConfigurationException 
    {
        try {
            Configuration cfg = getConfiguration (e);
            autoconfigure(obj, cfg);

            if (obj instanceof Configurable)
                ((Configurable)obj).setConfiguration (cfg);
            if (obj instanceof XmlConfigurable)
                ((XmlConfigurable)obj).setConfiguration(e);
        } catch (ConfigurationException | IllegalAccessException ex) {
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
   @SuppressWarnings("PMD.EmptyCatchBlock")
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
                Method method = obj.getClass().getMethod(m);
                method.invoke (obj);
           }
        } catch (NoSuchMethodException ignored) {
        } catch (NullPointerException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException e) {
            throw new ConfigurationException (
                obj.getClass().getName() + "." + m + "(" + p +")" ,
                    e.getTargetException()
            );
        }
    }
    public static boolean isEnabled(Element e) {
        return isTrue(getEnabledAttribute(e));
    }
    public static boolean isEagerStart(Element e) {
        return isTrue(getEagerStartAttribute(e));
    }
    private static boolean isTrue(String attribute) {
        return "true".equalsIgnoreCase(attribute) ||
          "yes".equalsIgnoreCase(attribute) ||
          attribute.contains(Environment.getEnvironment().getName());
    }

    public static String getEnabledAttribute (Element e) {
        return getAttribute(e, "enabled", "true");
    }
    public static String getEagerStartAttribute (Element e) {
        return getAttribute(e, "eager-start", "false");
    }
    private static String getAttribute (Element e, String attr, String def) {
        return Environment.get(e.getAttributeValue(attr, def));
    }

    @SuppressWarnings("rawtypes")
    public static void autoconfigure (Object obj, Configuration cfg) throws IllegalAccessException {
        Class cc = obj.getClass();
        do {
            Field[] fields = cc.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Config.class)) {
                    Config config = field.getAnnotation(Config.class);
                    String v = cfg.get(config.value(), null);
                    if (v != null) {
                        if (!field.isAccessible())
                            field.setAccessible(true);
                        Class<?> c = field.getType();
                        if (c.isAssignableFrom(String.class))
                            field.set(obj, v);
                        else if (c.isAssignableFrom(int.class) || c.isAssignableFrom(Integer.class))
                            field.set(obj, cfg.getInt(config.value()));
                        else if (c.isAssignableFrom(long.class) || c.isAssignableFrom(Long.class))
                            field.set(obj, cfg.getLong(config.value()));
                        else if (c.isAssignableFrom(double.class) || c.isAssignableFrom(Double.class))
                            field.set(obj, cfg.getDouble(config.value()));
                        else if (c.isAssignableFrom(boolean.class) || c.isAssignableFrom(Boolean.class))
                            field.set(obj, cfg.getBoolean(config.value()));
                        else if (c.isEnum()) 
                            field.set(obj, Enum.valueOf((Class<Enum>) c, v));
                        else if (c.isArray()) {
                            Class<?> ct = c.getComponentType();
                            if (ct.isAssignableFrom(String.class))
                                field.set(obj, cfg.getAll(config.value()));
                            else if (ct.isAssignableFrom(int.class) || ct.isAssignableFrom(Integer.class))
                                field.set(obj, cfg.getInts(config.value()));
                            else if (ct.isAssignableFrom(long.class) || ct.isAssignableFrom(Long.class))
                                field.set(obj, cfg.getLongs(config.value()));
                            else if (ct.isAssignableFrom(double.class) || ct.isAssignableFrom(Double.class))
                                field.set(obj, cfg.getDoubles(config.value())); 
                        } 
                    }
                }
            }
            cc = cc.getSuperclass();
        } while (!cc.equals(Object.class));
    }

    /**
     * Decorates an {@link Element} by replacing its attributes, and content {@link Environment} properties references.
     * @param e The element being decorated.
     * @return The modified element, it is modified in place, but it is returned to ease method chaining or call composition.
     */
    public static Element expandEnvProperties(Element e) {
       expandEnvProperties(e, Environment.getEnvironment());
       return e;
    }

    public static ExecutorService executorService(boolean virtual) {
        return virtual ?
            Executors.newVirtualThreadPerTaskExecutor() :
            Executors.newThreadPerTaskExecutor(
              Thread.ofPlatform().inheritInheritableThreadLocals(true)
                .factory()
            );
    }

    /**
     * Recursively replaces {@link Environment} properties in an element's attributes, its content and its children.
     * Properties are replaced in place.
     * @param e The element in which properties are being replaced.
     * @param env The {@link Environment}'s singleton instance.
     */
    private static void expandEnvProperties(Element e, Environment env) {
        if (Boolean.parseBoolean(e.getAttributeValue("verbatim"))) return;
        for (org.jdom2.Attribute attr : e.getAttributes()) {
            String value = attr.getValue();
            attr.setValue(env.getProperty(value, value));
        }
        for (Content child : e.getContent()) {
            if (child instanceof Element) {
                expandEnvProperties((Element) child, env);
            } else if (child instanceof Text text) {
                String textValue = text.getText();
                text.setText(env.getProperty(textValue, textValue));
            }
        }
    }
            
}
