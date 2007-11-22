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

/* * Generated file - Do not edit! */package org.jpos.q2.qbean;import mx4j.MBeanDescriptionAdapter;import java.lang.reflect.Method;import java.lang.reflect.Constructor;/** * MBean description. * @author apr@cs.com.uy
 * @version $Id: SystemMonitor.java,v 1.2 2005/11/07 20:12:11 marklsalter Exp $
 * @see Logger
 */public class SystemMonitorMBeanDescription extends                org.jpos.q2.QBeanSupportMBeanDescription            {      public String getMBeanDescription() {        return "System Monitor";    }      public String getConstructorDescription(Constructor ctor) {        String name = ctor.getName();             return super.getConstructorDescription(ctor);    }    public String getConstructorParameterName(Constructor ctor, int index) {              return super.getConstructorParameterName(ctor, index);    }    public String getConstructorParameterDescription(Constructor ctor, int index) {              return super.getConstructorParameterDescription(ctor, index);    }    public String getAttributeDescription(String attribute) {                                                                        if (attribute.equals("SleepTime")) {            return "Milliseconds between dump";        }                                                               if (attribute.equals("DetailRequired")) {            return "Detail required?";        }                                                                      return super.getAttributeDescription(attribute);    }    public String getOperationDescription(Method operation) {        String name = operation.getName();                                                                                                                                                return super.getOperationDescription(operation);    }    public String getOperationParameterName(Method method, int index) {        String name = method.getName();                                                                                                                                                return super.getOperationParameterName(method, index);    }    public String getOperationParameterDescription(Method method, int index) {        String name = method.getName();                                                                                                                                                return super.getOperationParameterDescription(method, index);    }}