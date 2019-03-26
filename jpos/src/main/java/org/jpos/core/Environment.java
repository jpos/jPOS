/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2019 jPOS Software SRL
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

package org.jpos.core;

import org.jpos.util.Loggeable;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Environment implements Loggeable {
    private static final String SYSTEM_PREFIX = "sys";
    private static final String ENVIRONMENT_PREFIX = "env";
    private static final String VERBATIM_PREFIX = "verb";
    private static final String BSH_PREFIX = "bsh";
    private static Pattern valuePattern = Pattern.compile(
      String.format("^(\\$)(%s|%s|%s|%s)?\\{([\\w\\W]+)\\}$",
        SYSTEM_PREFIX,
        ENVIRONMENT_PREFIX,
        VERBATIM_PREFIX,
        BSH_PREFIX)

    );
    private static Environment INSTANCE;
    public  static String ENVNAME_PROPERTY = "${jpos.env}";
    private String name;
    private AtomicReference<Properties> propRef = new AtomicReference<>(new Properties());

    static {
        try {
            INSTANCE = new Environment();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Environment() throws IOException {
        name = getProperty (ENVNAME_PROPERTY);
        name = name == null ? "default" : name;
        readConfig ();
    }

    public String getName() {
        return name;
    }

    public static Environment reload() throws IOException {
        return (INSTANCE = new Environment());
    }

    public static Environment getEnvironment() {
        return INSTANCE;
    }

    public String getProperty (String p, String def) {
        String s = getProperty (p);
        return s != null ? s : def;
    }

    public String getProperty (String s) {
        String r = s;

        if (s != null && s.length() > 0 && s.charAt(0) == '$') {
            Matcher m = valuePattern.matcher(s);
            if (m.matches() && m.groupCount() == 3) {
                String g3 = m.group(3);
                String g2 = m.group(2);
                g2 = g2 != null ? g2 : "";
                switch (g2) {
                    case SYSTEM_PREFIX:
                        r = System.getProperty(g3);
                        r = r == null ? propRef.get().getProperty(g3) : r;
                        break;
                    case ENVIRONMENT_PREFIX:
                        r = System.getenv(g3);
                        break;
                    case VERBATIM_PREFIX:
                        r = m.group(3);
                        break;
                    default:
                        r = System.getProperty(g3);
                        r = r == null ? propRef.get().getProperty(g3) : r;
                        r = r == null ? System.getenv(g3) : r;
                }
            } else {
                r = s;
            }
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    private void readConfig () throws IOException {
        if (name != null) {
            if (!readYAML())
                readCfg();
        }
    }

    private boolean readYAML () throws IOException {
        File f = new File("cfg/" + name + ".yml");
        if (f.exists() && f.canRead()) {
            Properties properties = new Properties();
            try (InputStream fis = new FileInputStream(f)) {
                Yaml yaml = new Yaml();
                Iterable<Object> document = yaml.loadAll(fis);
                document.forEach(d -> { flat(properties, null, (Map<String,Object>) d); });
                propRef.set(properties);
                return true;
            } catch (IOException e) {
                throw e;
            }
        }
        return false;
    }

    private boolean readCfg () throws IOException {
        File f = new File("cfg/" + name + ".cfg");
        if (f.exists() && f.canRead()) {
            Properties properties = new Properties();
            try (InputStream fis = new FileInputStream(f)) {
                properties.load(new BufferedInputStream(fis));
                propRef.set(properties);
                return true;
            } catch (IOException e) {
                throw e;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void flat (Properties properties, String prefix, Map<String,Object> c) {
        for (Object o : c.entrySet()) {
            Map.Entry<String,Object> entry = (Map.Entry<String,Object>) o;
            String p = prefix == null ? entry.getKey() : (prefix + "." + entry.getKey());
            if (entry.getValue() instanceof Map) {
                flat(properties, p, (Map) entry.getValue());
            } else {
                properties.put (p, "" + entry.getValue());
            }
        }
    }

    @Override
    public void dump(final PrintStream p, String indent) {
        p.printf ("%s<environment name='%s'>%n", indent, name);
        Properties properties = propRef.get();
        properties.stringPropertyNames().stream().
          forEachOrdered(prop -> { p.printf ("%s  %s=%s%n", indent, prop, properties.getProperty(prop));
          });
        p.printf ("%s</environment>%n", indent);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(String.format("jpos.env=%s%n", name));
            Properties properties = propRef.get();
            properties.stringPropertyNames().stream().
              forEachOrdered(prop -> { sb.append(String.format ("  %s=%s%n", prop, properties.getProperty(prop)));
              });
        }
        return sb.toString();
    }
}
