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

package org.jpos.q2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jdom2.Element;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Environment;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOUtil;
import org.yaml.snakeyaml.Yaml;

public class SimpleConfigurationFactory implements ConfigurationFactory {
    @Override
    public Configuration getConfiguration(Element e) throws ConfigurationException {
        Properties props = new Properties();
        for (Element property : e.getChildren("property")) {
            String name = property.getAttributeValue("name");
            String value = property.getAttributeValue("value");
            String baseFile = property.getAttributeValue("file");
            if (baseFile != null) {
            	boolean isEnv = Boolean.parseBoolean(property.getAttributeValue("env", "false"));
                processFile(props, baseFile, isEnv);
            } else if (name != null && value != null) {
                processProperty(props, name, value);
            }
        }
        return new SimpleConfiguration(props);
    }

    protected void processProperty(Properties props, String name, String value) {
        Object obj = props.get(name);
        if (obj instanceof String[]) {
            String[] mobj = (String[]) obj;
            String[] m = new String[mobj.length + 1];
            System.arraycopy(mobj, 0, m, 0, mobj.length);
            m[mobj.length] = value;
            props.put(name, m);
        } else if (obj instanceof String) {
            String[] m = new String[2];
            m[0] = (String) obj;
            m[1] = value;
            props.put(name, m);
        } else
            props.put(name, value);
    }

    protected void processFile(Properties props, String baseFile, boolean isEnv) throws ConfigurationException {
        baseFile = Environment.get(baseFile);
        boolean foundFile = false;
        for (String file : getFiles(baseFile, isEnv?Environment.getEnvironment().getName():"")) {
            foundFile |= readYamlFile(props, file);
        }
        if (!foundFile) {
            throw new ConfigurationException("Could not find any matches for file: " + baseFile);
        }
    }

    protected List<String> getFiles(String baseFile, String environmnents) {
        List<String> files = new ArrayList<>();
        files.add(baseFile);
        if (baseFile.endsWith(".yml") || baseFile.endsWith(".properties")) {
            baseFile = baseFile.substring(0, baseFile.lastIndexOf("."));
        }
        for (String env : ISOUtil.commaDecode(environmnents)) {
            if (!ISOUtil.isBlank(env)) {
                files.add(baseFile + "-" + env + ".yml");
                files.add(baseFile + "-" + env + ".properties");
            }
        }
        return files;
    }

    protected boolean readYamlFile(Properties props, String fileName) throws ConfigurationException {
        try {
            if (fileName.endsWith(".yml")) {
                return readYAML(props, fileName);
            } else {
                return readPropertyFile(props, fileName);
            }
        } catch (Exception ex) {
            throw new ConfigurationException(fileName, ex);
        }
    }

    protected boolean readPropertyFile(Properties props, String fileName) throws IOException {
        File f = new File(fileName);
        if (f.exists() && f.canRead()) {
            try (FileInputStream in = new FileInputStream(f)) {
                props.load(in);
                return true;
            }
        }
        return false;
    }

    protected boolean readYAML(Properties props, String fileName) throws IOException {
        File f = new File(fileName);
        if (f.exists() && f.canRead()) {
            try (InputStream fis = new FileInputStream(f)) {
                Yaml yaml = new Yaml();
                Iterable<Object> document = yaml.loadAll(fis);
                document.forEach(d -> {
                    Environment.flat(props, null, (Map<String, Object>) d, true);
                });
            }
            return true;
        }
        return false;
    }
}
