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

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;

public class CLIPrefixedClassNameCompleter implements Completer {
    protected final Collection<Candidate> candidates = new ArrayList<>();

    public CLIPrefixedClassNameCompleter(Collection<String> prefixes) throws IOException {
        for (String s : getClassNames(prefixes)) {
            candidates.add(new Candidate(AttributedString.stripAnsi(s), s, null, null, null, null, true));
        }
    }

    private static String[] getClassNames(Collection<String> prefixes) throws IOException {
        Set<String> classes = new HashSet<>();
        for (String prefix : prefixes) {
            classes.addAll(getClassEntries(prefix));
        }
        Set<String> classNames = new TreeSet<String>();
        for (String name : classes) {
            if (name.endsWith(".class")) {
                classNames.add(name.replace('/', '.').substring(0, name.length() - 6));
            }
        }
        return classNames.toArray(new String[classNames.size()]);
    }

    private static List<String> getClassEntries(String prefix) throws IOException {
        final String p = prefix.replaceAll("\\.", "\\/");
        List<String> result = new ArrayList<String>();

        Enumeration<URL> urls = CLIPrefixedClassNameCompleter.class.getClassLoader().getResources(p);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url == null) {
                return Collections.emptyList();
            }

            try {
                final List<String> lst = url.getProtocol().equals("jar") ?
                  resolveModuleEntriesFromJar(url, p) :
                  resolveModuleEntriesFromFiles(url, p);
                result.addAll(lst);
            } catch (URISyntaxException e) {
                throw new IOException("Bad URL", e);
            }
        }
        return result;
    }

    private static List<String> resolveModuleEntriesFromFiles(URL url, String _prefix) throws IOException, URISyntaxException {
        final String prefix = _prefix.endsWith("/") ? _prefix : _prefix + "/";
        List<String> resourceList = new ArrayList<String>();
        final URI uri = url.toURI();
        File f = new File(uri);
        addFiles(f, prefix, resourceList);
        return resourceList;
    }

    private static void addFiles(File f, String prefix, List<String> resourceList) {
        File files[] = f.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                addFiles(file, prefix + file.getName() + System.getProperty("file.separator"), resourceList);
            } else {
                resourceList.add(file.getName());
            }
        }
    }

    private static List<String> resolveModuleEntriesFromJar(URL url, String _prefix) throws IOException {
        final String prefix = _prefix.endsWith("/") ? _prefix : _prefix + "/";
        List<String> resourceList = new ArrayList<String>();
        JarURLConnection conn = (JarURLConnection) url.openConnection();
        Enumeration entries = conn.getJarFile().entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = (JarEntry) entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(prefix) && !name.contains("$") && !entry.isDirectory()) {
                name = name.substring(prefix.length()).toLowerCase();
                if (!name.contains("/")) {
                    resourceList.add(name);
                }
            }
        }
        return resourceList;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> cand) {
        if (line.wordIndex() == 0 && cand != null)
            cand.addAll(this.candidates);
    }

    public String toString () {
        StringBuilder sb = new StringBuilder ("CLIPrefixedClassNameCompletor[");
        sb.append(hashCode());
        for (Candidate c : candidates) {
            sb.append (System.getProperty("line.separator"));
            sb.append (" ");
            sb.append (c.value());
        }
        sb.append(']');
        return sb.toString();
    }
}
