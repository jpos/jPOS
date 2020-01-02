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

package org.jpos.q2;

import org.jdom2.Element;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;

public class SimpleConfigurationFactory implements ConfigurationFactory {
    public Configuration getConfiguration(Element e) throws ConfigurationException {
        Properties props = new Properties ();
        Iterator iter = e.getChildren ("property").iterator();
        while (iter.hasNext()) {
            Element property = (Element) iter.next ();
            String name  = property.getAttributeValue("name");
            String value = property.getAttributeValue("value");
            String file  = property.getAttributeValue("file");
            if (file != null)
                try {
                    props.load (new FileInputStream(new File(file)));
                } catch (Exception ex) {
                    throw new ConfigurationException (file, ex);
                }
            else if (name != null && value != null) {
                Object obj = props.get (name);
                if (obj instanceof String[]) {
                    String[] mobj = (String[]) obj;
                    String[] m = new String[mobj.length + 1];
                    System.arraycopy(mobj,0,m,0,mobj.length);
                    m[mobj.length] = value;
                    props.put (name, m);
                } else if (obj instanceof String) {
                    String[] m = new String[2];
                    m[0] = (String) obj;
                    m[1] = value;
                    props.put (name, m);
                } else
                    props.put (name, value);
            }
        }
        return new SimpleConfiguration(props);
    }
}
