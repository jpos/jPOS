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
import java.util.*;
import java.net.MalformedURLException;
import java.lang.reflect.InvocationTargetException;
import javax.management.*;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class QFactory {
    ObjectName loaderName;

    public QFactory (ObjectName loaderName) {
        super ();
        this.loaderName = loaderName;
    }
    public ObjectInstance createQBean (Q2 server, Element e) 
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
               Q2ConfigurationException
    {
        String clazz  = e.getAttributeValue ("class");
        String name   = e.getAttributeValue ("name");
        if (name == null)
            name = "unknown-" + Long.toString (System.currentTimeMillis ());

        ObjectName objectName = new ObjectName (Q2.QBEAN_NAME + name);
        MBeanServer mserver = server.getMBeanServer();

        getExtraPath (server.getLoader (), e);

        ObjectInstance instance = mserver.createMBean (
            clazz, objectName, loaderName
        );

        setAttribute (mserver, objectName, "Server", server);
        setAttribute (mserver, objectName, "Persist", e);
        setAttribute (mserver, objectName, "Name", e);
        configureQBean(mserver,objectName,e);
        mserver.invoke (objectName, "init",  null, null);

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
                    ex.printStackTrace();
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


    public void destroyQBean (Q2 server, ObjectName objectName)
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
        mserver.invoke (objectName, "stop",  null, null);
        mserver.invoke (objectName, "destroy",  null, null);
        mserver.unregisterMBean (objectName);
    }


    public void configureQBean(MBeanServer server, ObjectName objectName,Element e)
        throws Q2ConfigurationException
    {
        try {
            AttributeList attributeList = getAttributeList(e);
            Iterator attributes = attributeList.iterator();
            while (attributes.hasNext())
                server.setAttribute(objectName,(Attribute)attributes.next());
        } catch (Exception e1) {
            throw new Q2ConfigurationException(e1);
        }

    }
    public AttributeList getAttributeList(Element e)
        throws Q2ConfigurationException
    {
        AttributeList attributeList = new AttributeList();
        List childs = e.getChildren("attr");
        Iterator childsIterator = childs.iterator();
        while (childsIterator.hasNext())
        {
            Element  childElement = (Element)childsIterator.next();
            String name = childElement.getAttributeValue("name");
            name = getAttributeName(name);
            String type = childElement.getAttributeValue("type","java.lang.String");
            String value = childElement.getText();
            try {
                Class attributeType = Class.forName(type);
                Class[] parameterTypes = {"".getClass()};
                Object[] parameterValues = {value};
                Object obj = attributeType.getConstructor(parameterTypes).newInstance(parameterValues);
                Attribute attr =  new Attribute(name,obj);
                attributeList.add(attr);
            } catch (Exception e1) {
                throw new Q2ConfigurationException(e1);
            }
        }
        return attributeList;
    }

    /**
     * sets the first character of the string to the upper case
     * @param name
     * @return
     */
    public static String getAttributeName(String name)
    {
        StringBuffer tmp = new StringBuffer(name);
        tmp.setCharAt(0,name.toUpperCase().charAt(0)) ;
        return tmp.toString();
    }


}

