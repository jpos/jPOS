/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2016 Alejandro P. Revilla
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

import jline.SimpleCompletor;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;

public class CLIPrefixedClassNameCompletor extends SimpleCompletor
{
    public CLIPrefixedClassNameCompletor(Collection<String> prefixes)
            throws IOException
    {
        super(getClassNames(prefixes), new CmdFilter(prefixes));
        setDelimiter(".");
    }

    private static String[] getClassNames(Collection<String> prefixes) throws IOException
    {
        Set<String> classes = new HashSet<String>();
        for (String prefix : prefixes)
        {
            classes.addAll(getClassEntries(prefix));
        }

        // now filter classes by changing "/" to "." and trimming the
        // trailing ".class"
        Set<String> classNames = new TreeSet<String>();

        for (String name : classes) {
            if (name.endsWith(".class")) {
                classNames.add(name.replace('/', '.').
                        substring(0, name.length() - 6));
            }
        }

        return classNames.toArray(new String[classNames.size()]);
    }

    private static List<String> getClassEntries(String prefix) throws IOException
    {
        final String p = prefix.replaceAll("\\.", "\\/");
        List<String> result = new ArrayList<String>();

        Enumeration<URL> urls = CLIPrefixedClassNameCompletor.class.getClassLoader().getResources(p);
        while (urls.hasMoreElements())
        {
            URL url = urls.nextElement();
            if (url == null) { return Collections.emptyList(); }

            try
            {
                final List<String> lst = url.getProtocol().equals("jar") ?
                                         resolveModuleEntriesFromJar(url, p) :
                                         resolveModuleEntriesFromFiles(url, p);
                result.addAll(lst);
            }
            catch (URISyntaxException e)
            {
                throw new IOException("Bad URL", e);
            }
        }
        return result;
    }

    private static List<String> resolveModuleEntriesFromFiles(URL url, String _prefix) throws IOException, URISyntaxException
    {
        final String prefix = _prefix.endsWith("/") ? _prefix : _prefix + "/";

        List<String> resourceList = new ArrayList<String>();

        final URI uri = url.toURI();
        File f = new File(uri);
        addFiles(f, prefix, resourceList);

        return resourceList;
    }

    private static void addFiles(File f, String prefix, List<String> resourceList)
    {
        File files[] = f.listFiles();
        if (files == null) { return; }

        for (File file : files)
        {
            if (file.isDirectory())
            {
                addFiles(file, prefix + file.getName() + "/", resourceList);
            }
            else
            {
                resourceList.add(prefix + file.getName());
            }
        }
    }

    private static List<String> resolveModuleEntriesFromJar(URL url, String _prefix) throws IOException
    {
        final String prefix = _prefix.endsWith("/") ? _prefix : _prefix + "/";

        List<String> resourceList = new ArrayList<String>();

        JarURLConnection conn = (JarURLConnection) url.openConnection();
        Enumeration entries = conn.getJarFile().entries();
        while (entries.hasMoreElements())
        {
            JarEntry entry = (JarEntry) entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(prefix) && !entry.isDirectory())
            {
                resourceList.add(name);
            }
        }
        return resourceList;
    }

    public static class CmdFilter implements SimpleCompletorFilter
    {
        Collection<String> prefixes;

        public CmdFilter(Collection<String> prefixes)
        {
            this.prefixes = prefixes;
        }

        public String filter(String element)
        {
            for (String prefix : prefixes)
            {
                if (element.startsWith(prefix) && !element.contains("$")) {
                    return element.substring(prefix.length()).toLowerCase();
                }
            }
            return null;
        }
    }
}
