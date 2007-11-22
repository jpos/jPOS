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

/* * Generated file - Do not edit! */package org.jpos.q2;import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import mx4j.MBeanDescriptionAdapter;
/** * MBean description. * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @version $Revision: 2072 $ $Date: 2004-12-07 08:21:24 -0300 (Tue, 07 Dec 2004) $
 */public class QClassLoaderMBeanDescription extends                         MBeanDescriptionAdapter    {      public String getMBeanDescription() {        return "Q2 Class Loader";    }      public String getConstructorDescription(Constructor ctor) {        String name = ctor.getName();                        return super.getConstructorDescription(ctor);    }    public String getConstructorParameterName(Constructor ctor, int index) {                           return super.getConstructorParameterName(ctor, index);    }    public String getConstructorParameterDescription(Constructor ctor, int index) {                           return super.getConstructorParameterDescription(ctor, index);    }    public String getAttributeDescription(String attribute) {                                                         return super.getAttributeDescription(attribute);    }    public String getOperationDescription(Method operation) {        String name = operation.getName();                     if (name.equals("addURL")) {            return "";        }                                                            return super.getOperationDescription(operation);    }    public String getOperationParameterName(Method method, int index) {        String name = method.getName();                     if (name.equals("addURL")) {            switch (index) {                              case 0:                    return "url";                          }        }                                                            return super.getOperationParameterName(method, index);    }    public String getOperationParameterDescription(Method method, int index) {        String name = method.getName();                     if (name.equals("addURL")) {            switch (index) {                              case 0:                    return "";                          }        }                                                            return super.getOperationParameterDescription(method, index);    }}