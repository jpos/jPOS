/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.q2;

import java.net.URL;

import org.jdom.Element;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public interface QBeanSupportMBean extends QBean, QPersist {
    public void setServer (Q2 server);
    public Q2 getServer ();
    public void setPersist (Element e);
    public void setName (String name);
    public String getName ();
    public void setLogger (String name);
    public String getLogger ();
    public void shutdownQ2 ();
    public URL[] getLoaderURLS();
    public QClassLoader getLoader();
}

