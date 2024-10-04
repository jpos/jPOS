/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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
import org.jdom2.JDOMException;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Environment;
import org.jpos.core.XmlConfigurable;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TemplateDeployer extends QBeanSupport implements XmlConfigurable {
    Element config;

    @Override
    protected void initService() throws ISOException, GeneralSecurityException, IOException, JDOMException {
        for (Element e : config.getChildren("template")) {
            try {
                String resource = Environment.get(e.getAttributeValue("resource"));
                String descriptorPrefix = Environment.get(e.getAttributeValue("descriptor-prefix"));
                if (QFactory.isEnabled(e)) {
                    String prefix = Environment.get(e.getTextTrim());
                    String[] ss = ISOUtil.commaDecode(prefix);
                    for (String s : ss) {
                        s = s.trim();
                        getServer().deployTemplate(resource, "%s_%s.xml".formatted(descriptorPrefix, s), s);
                    }
                } else
                    getLog().warn("%s (%s) not enabled".formatted(resource, descriptorPrefix));
            } catch (Exception ex) {
                getLog().error(ex);
            }
        }
    }

    @Override
    public void setConfiguration(Element config) throws ConfigurationException {
        this.config = config;
    }
}
