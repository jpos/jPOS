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

package org.jpos.q2.qbean;

import org.jdom2.Element;
import org.jpos.core.XmlConfigurable;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.NameRegistrar;

@SuppressWarnings("unused")
public class QXmlConfig extends QBeanSupport implements XmlConfigurable {
    public static final String XML_PREFIX = "config.xml.";

    @Override
    protected void destroyService() {
        NameRegistrar.unregister (XML_PREFIX + getName());
    }
    /**
     * @param e Configuration element
     */
    @Override
    public void setConfiguration(Element e) {
        NameRegistrar.register(XML_PREFIX + getName(), e);
    }
    public static Element getConfiguration (String name)
            throws NameRegistrar.NotFoundException
    {
        return (Element) NameRegistrar.get(XML_PREFIX + name);
    }

    /**
     * @param name configuration name
     * @param timeout in millis
     * @return Configuration Element or null
     */
    public static Element getConfiguration (String name, long timeout) {
        return (Element) NameRegistrar.get(XML_PREFIX + name, timeout);
    }
}
