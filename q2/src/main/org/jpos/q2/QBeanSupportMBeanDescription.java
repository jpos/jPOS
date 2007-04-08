/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.q2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:alwyn@smart.com.ph">Alwyn Schoeman</a>
 * @version $Revision$ $Date$
 */
public class QBeanSupportMBeanDescription extends mx4j.MBeanDescriptionAdapter {
    public String getConstructorDescription (Constructor ctor) {
        return "Default Service Constructor";
    }

    public String getAttributeDescription (String attribute) {
        if (attribute.equals("Loader"))
            return "Classloader";
        if (attribute.equals("LoaderURLS"))
            return "ClassLoader URLs";
        if (attribute.equals("Logger"))
            return "Logger service";
        if (attribute.equals("Modified"))
            return "Configuration modified?";
        if (attribute.equals("Name"))
            return "Service Name";
        if (attribute.equals("Persist"))
            return "XML Descriptor";
        if (attribute.equals("Server"))
            return "MBeanServer Object";
        if (attribute.equals("State"))
            return "Runtime Status";
        if (attribute.equals("StateAsString"))
            return "Runtime Status";
        return super.getAttributeDescription (attribute);
    }

    public String getOperationDescription(Method operation) {
        if (operation.getName().equals("init"))
            return "Initialise service";
        if (operation.getName().equals("start"))
            return "Start service";
        if (operation.getName().equals("stop"))
            return "Stop service";
        if (operation.getName().equals("destroy"))
            return "Destroy service";
        if (operation.getName().equals("shutdownQ2"))
            return "Shutdown and undeploy EVERYTHING";
        return super.getOperationDescription (operation);
    }
}

