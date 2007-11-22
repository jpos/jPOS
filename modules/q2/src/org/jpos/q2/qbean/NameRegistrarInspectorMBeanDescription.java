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

import mx4j.MBeanDescriptionAdapter;
/** * MBean description. * @author <a href="mailto:nevyn@debian.org">Alwyn Schoeman</a>
 * @version $Revision: 1.1.1.1 $ $Date: 2003/04/03 03:59:27 $
 */public class NameRegistrarInspectorMBeanDescription extends                         MBeanDescriptionAdapter    {      public String getMBeanDescription() {        return "Name Registrar Inspector";    }      public String getConstructorDescription(Constructor ctor) {        String name = ctor.getName();                   if (ctor.toString().equals("public NameRegistrarInspector()")) {            return "Empty default constructor";        }                   return super.getConstructorDescription(ctor);    }    public String getConstructorParameterName(Constructor ctor, int index) {                     if (ctor.toString().equals("public NameRegistrarInspector()")) {            switch (index) {                          }        }                     return super.getConstructorParameterName(ctor, index);    }    public String getConstructorParameterDescription(Constructor ctor, int index) {                           return super.getConstructorParameterDescription(ctor, index);    }    public String getAttributeDescription(String attribute) {                          if (attribute.equals("Registry")) {            return "Registry contents";        }                          return super.getAttributeDescription(attribute);    }    public String getOperationDescription(Method operation) {        String name = operation.getName();                           return super.getOperationDescription(operation);    }    public String getOperationParameterName(Method method, int index) {        String name = method.getName();                           return super.getOperationParameterName(method, index);    }    public String getOperationParameterDescription(Method method, int index) {        String name = method.getName();                           return super.getOperationParameterDescription(method, index);    }}