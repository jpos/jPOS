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

package org.jpos.apps.qsp.config;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Properties;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.core.ConfigurationException;

/**
 * Config Helper methods
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class ConfigUtil {
    public static String DIGEST_PROPERTY = "digest";
   /**
    * @param propertyName property name
    * @param attributeName attribute name
    * @param props  container (created if null)
    * @param node   context node
    * @param evt    optional LogEvent (can be null)
    */
    public static String addProperty 
	(String name, Properties props, Node node, LogEvent evt)
    {
	String value = null;
	Node n = node.getAttributes().getNamedItem (name);
	if (n != null) {
	    value = n.getNodeValue();
	    props.put (name, value);
	    if (evt != null)
		evt.addMessage (name+"="+value);
	}
	return value;
    }

   /**
    * @param node   context node
    * @param names  name pairs (node-attribute/property-name)
    * @param props  container (created if null)
    * @param evt    optional LogEvent (can be null)
    * @return [possibly created] props
    */
    public static Properties addAttributes (
	Node node, String[] names, Properties props, LogEvent evt)
	throws ConfigurationException
    {
	if (props == null)
	    props = new Properties();

	try {
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    for (int i=0; i<names.length; i++) {
		String value = addProperty (names[i], props, node, evt);
		if (value != null) {
		    md.update (names[i].getBytes());
		    md.update (value.getBytes());
		}
	    }
	    byte[] digest = md.digest();
	    evt.addMessage ("digest " + ISOUtil.hexString (digest));
	    props.put (DIGEST_PROPERTY, digest);
	} catch (NoSuchAlgorithmException e) {
	    throw new ConfigurationException (e);
	}
	return props;
    }

   /**
    * @param node   context node
    * @param props  container (created if null)
    * @param evt    optional LogEvent (can be null)
    * @return [possibly created] props
    */
    public static Properties addProperties 
	(Node node, Properties props, LogEvent evt)
    {
	if (props == null)
	    props = new Properties();

	NodeList childs = node.getChildNodes();
	for (int i=0; i<childs.getLength(); i++) {
	    Node n = childs.item(i);

	    if (n.getNodeName().equals ("property")) {
		Node file = 
		    n.getAttributes().getNamedItem ("file");
		if (file != null) 
		    addFileProperties (props, file.getNodeValue(), evt);
		else {
		    String name  = 
			n.getAttributes().getNamedItem ("name").getNodeValue();
		    String value = 
			n.getAttributes().getNamedItem ("value").getNodeValue();
		    props.put (name, value);
		    if (evt != null)
			evt.addMessage (name + "=" + value);
		}
	    }
	}
	return props;
    }

   /**
    * @param className class Name
    * @return new Object instance
    * @throws ConfigurationException (with wrapped exception)
    */
    public static Object newInstance (String className)
	throws ConfigurationException
    {
        try {
            return Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
	    throw new ConfigurationException (className, e);
        } catch (InstantiationException e) {
	    throw new ConfigurationException (className, e);
        } catch (IllegalAccessException e) {
	    throw new ConfigurationException (className, e);
	}
    }

    public static int getAttributeAsInt (Node node, String name, int defValue) {
	int i = defValue;
	Node n = node.getAttributes().getNamedItem (name);
	if (n != null)
	    i = Integer.parseInt (n.getNodeValue());
	return i;
    }
    public static String getAttribute (Node node, String name, String defValue) 
    {
	String s = defValue;
	Node n = node.getAttributes().getNamedItem (name);
	if (n != null)
	    s = n.getNodeValue();
	return s;
    }

    //--------------------------------------------------------private helpers
    private static void addFileProperties 
	(Properties props, String filename, LogEvent evt)
    {
	FileInputStream fis = null;
	evt.addMessage ("<!-- reading properties from "+filename+ " -->");
	try {
	    props.load(
		new BufferedInputStream(
		    fis = new FileInputStream (filename)
		)
	    );
	} catch (Exception e) {
	    evt.addMessage (e);
	} finally {
	    if (fis != null) {
		try { 
		    fis.close(); 
		} catch (IOException ex) { 
		    evt.addMessage (ex);
		}
	    }
	}
    }
   /**
    * Tries to invoke a method (usually a setter) on the given object
    * silently ignoring if method does not exist
    * @param obj the object
    * @param m method to invoke
    * @param p parameter
    * @throws ConfigurationException if method happens to throw an exception
    */
    public static void invoke (Object obj, String m, Object p) 
	throws ConfigurationException 
    {
	try {
	    Class[] paramTemplate = { p.getClass() };
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
