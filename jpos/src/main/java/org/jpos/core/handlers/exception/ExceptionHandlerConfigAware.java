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

package org.jpos.core.handlers.exception;

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QFactory;

/**
 * Adds the logic for parsing exception handler pipeline configurations to any implementing class.
 *
 * @author Alwyn Schoeman
 * @since 2.1.2
 */
public interface ExceptionHandlerConfigAware {

    default void addExceptionHandlers(ExceptionHandlerAware receiver, Element elem, QFactory fact)
            throws ConfigurationException
    {
        for (Element o : elem.getChildren("exception-handler")) {
            String clazz = o.getAttributeValue("class");
            ExceptionHandler handler = (ExceptionHandler) fact.newInstance(clazz);
            fact.setLogger(handler, o);
            fact.setConfiguration(handler, o);
            String exception = o.getAttributeValue("exception");
            if (exception == null) {
                receiver.addHandler(handler);
            } else {
                Class<? extends Exception> exceptionClass;
                try {
                    exceptionClass = (Class<? extends Exception>) Class.forName(exception);
                } catch (Exception e) {
                    throw new ConfigurationException(exception, e);
                }
                receiver.addHandler(handler, exceptionClass);
            }
        }
    }
}
