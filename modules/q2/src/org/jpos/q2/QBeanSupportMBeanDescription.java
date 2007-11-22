/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2007 Alejandro P. Revilla
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

