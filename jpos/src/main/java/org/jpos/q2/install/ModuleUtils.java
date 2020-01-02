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
import java.util.stream.Collectors;

/**
 * @author vsalaman
 */
public class ModuleUtils
{
    private static final String MODULES_UUID_DIR = "META-INF/modules/uuids/";
    private static final String MODULES_RKEYS_DIR = "META-INF/modules/rkeys/";

    public static List<String> getModuleEntries(String prefix) throws IOException
    {
        List<String> result=new ArrayList<String>();

        Enumeration<URL> urls = ModuleUtils.class.getClassLoader().getResources(prefix);
        while (urls.hasMoreElements())
        {
            URL url = urls.nextElement();
            if(url==null) return Collections.emptyList();

            try
            {
                final List<String> lst = url.getProtocol().equals("jar") ?
                                         resolveModuleEntriesFromJar(url,prefix) :
                                         resolveModuleEntriesFromFiles(url,prefix);
                result.addAll(lst);
            }
            catch (URISyntaxException e)
            {
                throw new IOException("Bad URL",e);
            }
        }
        return result;
    }

    public static List<String> getModulesUUIDs () throws IOException {
        return ModuleUtils.getModuleEntries(MODULES_UUID_DIR)
          .stream()
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
        boolean updated = false;
        for (String s : getModulesUUIDs()) {
            digest.update(s.getBytes());
            updated = true;
        }
        return updated ? Base64.getEncoder().encodeToString(digest.digest()) : "";
    }

    private static List<String> resolveModuleEntriesFromFiles(URL url,String _prefix) throws URISyntaxException
    {
        final String prefix=_prefix.endsWith("/")?_prefix:_prefix+"/";

        List<String> resourceList =new ArrayList<String>();

        final URI uri = url.toURI();
        File f=new File(uri);
        addFiles(f,prefix, resourceList);

        return resourceList;
    }

    private static void addFiles(File f,String prefix,List<String> resourceList)
    {
        File files[]=f.listFiles();
        if(files==null) return;
        
        for (File file : files)
        {
            if(file.isDirectory())
            {
                addFiles(file,prefix+file.getName()+"/", resourceList);
            }
            else
            {
                resourceList.add(prefix+file.getName());
            }
        }
    }

    private static List<String> resolveModuleEntriesFromJar(URL url,String _prefix) throws IOException
    {
        final String prefix=_prefix.endsWith("/")?_prefix:_prefix+"/";

        List<String> resourceList =new ArrayList<String>();

        JarURLConnection conn=(JarURLConnection)url.openConnection();
        Enumeration entries=conn.getJarFile().entries();
        while (entries.hasMoreElements())
        {
            JarEntry entry = (JarEntry) entries.nextElement();
            String name=entry.getName();
            if(name.startsWith(prefix) && !entry.isDirectory())
            {
                resourceList.add(name);
            }
        }
        return resourceList;
    }
}
