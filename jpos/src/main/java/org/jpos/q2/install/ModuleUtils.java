/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.q2.install;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author vsalaman
 */
public class ModuleUtils
{
    private static final String MODULES_UUID_DIR = "META-INF/modules/uuids/";
    private static final String MODULES_RKEYS_DIR = "META-INF/modules/rkeys/";

    public static List<String> getModuleEntries(String prefix) throws IOException {
        List<String> result = new ArrayList<>();

        Enumeration<URL> urls = ModuleUtils.class.getClassLoader().getResources(prefix);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url == null) continue;

            try {
                List<String> entries;
                String protocol = url.getProtocol();
                if ("jar".equals(protocol)) {
                    entries = resolveModuleEntriesFromJar(url, prefix);
                } else if ("file".equals(protocol)) {
                    entries = resolveModuleEntriesFromFiles(url, prefix);
                } else {
                    // Unsupported protocol, skip with optional logging
                    continue;
                }
                result.addAll(entries);
            } catch (URISyntaxException e) {
                throw new IOException("Bad URL: " + url, e);
            }
        }
        return result;
    }

    public static List<String> getModulesUUIDs() throws IOException {
        return getModuleEntries(MODULES_UUID_DIR).stream()
          .sorted()
          .map(p -> p.substring(MODULES_UUID_DIR.length()))
          .collect(Collectors.toList());
    }

    public static List<String> getRKeys () throws IOException {
        return ModuleUtils.getModuleEntries(MODULES_RKEYS_DIR)
          .stream()
          .sorted()
          .map(p -> p.substring(MODULES_RKEYS_DIR.length()))
          .collect(Collectors.toList());
    }

    public static String getSystemHash() throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        List<String> uuids = getModulesUUIDs();
        if (uuids.isEmpty()) return "";

        uuids.forEach(uuid -> digest.update(uuid.getBytes()));
        return Base64.getEncoder().encodeToString(digest.digest());
    }

    private static List<String> resolveModuleEntriesFromFiles(URL url, String prefix)
      throws URISyntaxException {
        String normalizedPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
        List<String> resourceList = new ArrayList<>();
        File dir = new File(url.toURI());
        addFiles(dir, normalizedPrefix, resourceList);
        return resourceList;
    }

    private static void addFiles(File dir, String prefix, List<String> resourceList) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                addFiles(file, prefix + file.getName() + "/", resourceList);
            } else {
                resourceList.add(prefix + file.getName());
            }
        }
    }

    private static List<String> resolveModuleEntriesFromJar(URL url, String prefix)
      throws IOException {
        String normalizedPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
        List<String> resourceList = new ArrayList<>();

        JarURLConnection conn = (JarURLConnection) url.openConnection();
        try (JarFile jarFile = conn.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(normalizedPrefix) && !entry.isDirectory()) {
                    resourceList.add(name);
                }
            }
        }
        return resourceList;
    }
}
