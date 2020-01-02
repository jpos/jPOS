/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.util.slf4j;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Enumeration;

@SuppressWarnings("WeakerAccess")
public class Slf4JDynamicBinder
{
    private static final String STATIC_BINDER_CLASS =
        "org.slf4j.impl.StaticLoggerBinder";
    private static final String STATIC_BINDER_RESOURCE =
        "org/slf4j/impl/StaticLoggerBinder.class";

    private static boolean bound = false;
    public static void applyMods() throws Exception
    {
        if (!bound && !bindingsExist())
        {
            final ProtectionDomain pd = Slf4JDynamicBinder.class.getProtectionDomain();
            final ClassPool cp = ClassPool.getDefault();
            CtClass clz = cp.getAndRename(StaticLoggerBinder.class.getName(),
                                          STATIC_BINDER_CLASS);
            clz.toClass(null, pd);

            CtClass clz2 = cp.get("org.slf4j.LoggerFactory");
            CtMethod bindMethod = clz2.getDeclaredMethod("bind");

            bindMethod.setBody("try " +
                               "{" +
                               " org.slf4j.impl.StaticLoggerBinder.getSingleton();" +
                               " INITIALIZATION_STATE = SUCCESSFUL_INITIALIZATION;" +
                               " fixSubstituteLoggers();" +
                               " replayEvents();" +
                               " SUBST_FACTORY.clear();" +
                               "} " +
                               "catch(Exception e)" +
                               "{" +
                               " failedBinding(e); " +
                               " throw new IllegalStateException(\"Unexpected initialization failure\", e);" +
                               "}");
            clz2.toClass(null, pd);
            clz2.detach();
            clz.detach();
            bound = true;
        }
    }

    private static boolean bindingsExist()
    {
        int cnt = 0;
        try
        {
            ClassLoader cl = Slf4JDynamicBinder.class.getClassLoader();
            Enumeration<URL> paths = cl.getResources(STATIC_BINDER_RESOURCE);
            while (paths.hasMoreElements())
            {
                paths.nextElement();
                cnt++;
            }
        }
        catch (IOException ioe)
        {
        }
        return cnt > 0;
    }
}
