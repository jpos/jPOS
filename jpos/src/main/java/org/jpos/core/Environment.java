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

package org.jpos.core;

import org.jpos.util.Loggeable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Environment implements Loggeable {
    private static final String SYSTEM_PREFIX = "sys";
    private static final String ENVIRONMENT_PREFIX = "env";

    // older pattern, not supporting defaults, left for reference (delete if not needed anymore)
    // private static Pattern valuePattern = Pattern.compile("^([\\w\\W]*)(\\$)([\\w\\W]*)?\\{([\\w\\W]+)\\}([\\w\\W]*)$");

    private static Pattern valuePattern = Pattern.compile("^(.*)(\\$)([\\w]*)\\{([-\\w.]+)(:(.+?))?\\}(.*)$");
    // make groups easier to read :-)                       11112222233333333   444444444455666665    7777

    private static Pattern verbPattern = Pattern.compile("^\\$verb\\{([\\w\\W]+)\\}$");
    private static Environment INSTANCE;
    private String name;
    private AtomicReference<Properties> propRef = new AtomicReference<>(new Properties());
    private static String SP_PREFIX = "system.property.";
    private static int SP_PREFIX_LENGTH = SP_PREFIX.length();
    private String errorString;
    private ServiceLoader<EnvironmentProvider> serviceLoader;

    static {
        try {
            INSTANCE = new Environment();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Environment() throws IOException {
        name = System.getProperty ("jpos.env");
        name = name == null ? "default" : name;
        readConfig ();
        serviceLoader = ServiceLoader.load(EnvironmentProvider.class);
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
    public static String get (String p) {
        return getEnvironment().getProperty(p, p);
    }
    public static String get (String p, String def) {
        return getEnvironment().getProperty(p, def);
    }
    public String getProperty (String p, String def) {
        String s = getProperty (p);
        return s != null ? s : def;
    }

    public String getErrorString() {
        return errorString;
    }

    /**
     * If property name has the pattern <code>${propname}</code>, this method will
     *
     * <ul>
     *     <li>Attempt to get it from an operating system environment variable called 'propname'</li>
     *     <li>If not present, it will try to pick it from the a Java system.property</li>
     *     <li>If not present either, it will try the target environment (either <code>.yml</code> or <code>.cfg</code></li>
     *     <li>Otherwise it returns null</li>
     * </ul>
     *
     * The special pattern <code>$env{propname}</code> would just try to pick it from the OS environment.
     * <code>$sys{propname}</code> will just try to get it from a System.property and
     * <code>$verb{propname}</code> will return a verbatim copy of the value.
     *
     * @param s property name
     * @return property value
     */
    public String getProperty (String s) {
        String r = s;
        if (s != null) {
            Matcher m = verbPattern.matcher(s);
            if (m.matches()) {
                return m.group(1);
            }
            m = valuePattern.matcher(s);
            if (!m.matches())
                return s;
            while (m != null && m.matches()) {
                String gPrefix = m.group(3);
                String gValue = m.group(4);
                gPrefix = gPrefix != null ? gPrefix : "";
                switch (gPrefix) {
                    case SYSTEM_PREFIX:
                        r = System.getProperty(gValue);
                        break;
                    case ENVIRONMENT_PREFIX:
                        r = System.getenv(gValue);
                        break;
                    default:
                        if (gPrefix.length() == 0) {
                            r = System.getenv(gValue); // ENV has priority
                            r = r == null ? System.getProperty(gValue) : r; // then System.property
                            r = r == null ? propRef.get().getProperty(gValue) : r; // then jPOS --environment
                        } else {
                            return s; // do nothing - unknown prefix
                        }
                }

                if (r == null) {
                    String defValue = m.group(6);
                    if (defValue != null)
                        r = defValue;
                }

                if (r != null) {
                    if (m.group(1) != null) {
                        r = m.group(1) + r;
                    }
                    if (m.group(7) != null)
                        r = r + m.group(7);
                    m = valuePattern.matcher(r);
                }
                else
                    m = null;
            }
        }
        for (EnvironmentProvider p : serviceLoader) {
            int l = p.prefix().length();
            if (r != null && r.length() > l && r.startsWith(p.prefix())) {
                r = p.get(r.substring(l));
            }
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    private void readConfig () throws IOException {
        if (name != null) {
            if (!readYAML())
                readCfg();

            extractSystemProperties();
        }
    }

    private void extractSystemProperties() {
        Properties properties = propRef.get();
        properties
          .stringPropertyNames()
          .stream()
          .filter(e -> e.startsWith(SP_PREFIX))
          .forEach(prop -> System.setProperty(prop.substring(SP_PREFIX_LENGTH), (String) properties.get(prop)));
    }

    private boolean readYAML () throws IOException {
        File f = new File("cfg/" + name + ".yml");
        errorString = null;
        if (f.exists() && f.canRead()) {
            Properties properties = new Properties();
            try (InputStream fis = new FileInputStream(f)) {
                Yaml yaml = new Yaml();
                Iterable<Object> document = yaml.loadAll(fis);
                document.forEach(d -> {
                    flat(properties, null, (Map<String, Object>) d);
                });
                propRef.set(properties);
                return true;
            } catch (IOException e) {
                throw e;
            } catch (ScannerException e) {
                errorString = "Environment (" + getName() + ") error " + e.getMessage();
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
            if (serviceLoader.iterator().hasNext()) {
                sb.append ("  providers:");
                sb.append (System.lineSeparator());
                for (EnvironmentProvider provider : serviceLoader) {
                    sb.append(String.format("    %s%n", provider.getClass().getCanonicalName()));
                }
            }
        }
        return sb.toString();
    }
}
