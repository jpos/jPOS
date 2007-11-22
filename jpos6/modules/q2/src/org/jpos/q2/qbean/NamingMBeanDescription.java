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

/* * Generated file - Do not edit! */package org.jpos.q2.qbean;import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
/** * MBean description. * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @version $Revision: 1.2 $ $Date: 2004/04/10 16:39:59 $
 */public class NamingMBeanDescription extends                org.jpos.q2.QBeanSupportMBeanDescription            {      public String getMBeanDescription() {        return "Naming Service";    }      public String getConstructorDescription(Constructor ctor) {        String name = ctor.getName();                        return super.getConstructorDescription(ctor);    }    public String getConstructorParameterName(Constructor ctor, int index) {                           return super.getConstructorParameterName(ctor, index);    }    public String getConstructorParameterDescription(Constructor ctor, int index) {                           return super.getConstructorParameterDescription(ctor, index);    }    public String getAttributeDescription(String attribute) {                                                                                                         if (attribute.equals("Port")) {            return "get port";        }                                                return super.getAttributeDescription(attribute);    }    public String getOperationDescription(Method operation) {        String name = operation.getName();                     if (name.equals("init")) {            return "QBean init";        }                            if (name.equals("start")) {            return "QBean start";        }                            if (name.equals("stop")) {            return "QBean stop";        }                            if (name.equals("destroy")) {            return "QBean destroy";        }                                                                                      return super.getOperationDescription(operation);    }    public String getOperationParameterName(Method method, int index) {        String name = method.getName();                                                                                                                                   return super.getOperationParameterName(method, index);    }    public String getOperationParameterDescription(Method method, int index) {        String name = method.getName();                                                                                                                                   return super.getOperationParameterDescription(method, index);    }}