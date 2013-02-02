/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2013 Alejandro P. Revilla
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author vsalaman
 */
public class BasicSetup
{
    private static final String prefix = "META-INF/q2/installs/";

    public static void main(String[] args) throws Exception
    {
        String dir=args.length>0?args[0]:".";
        new BasicSetup().install(false, dir);
    }

    public void install(boolean allowOverride,String outputBasePath) throws IOException
    {
        install(allowOverride,new File(outputBasePath));
    }

    public void install(boolean allowOverride,File outputBasePath) throws IOException
    {
        if(!outputBasePath.exists())
        {
            outputBasePath.mkdirs();
        }
        
        List<String> moduleConfigs = ModuleUtils.getModuleEntries(prefix);
        for (String resource : moduleConfigs)
        {
            final String s = resource.substring(prefix.length());
            int end = s.lastIndexOf("/");
            String dirPrefix = end < 0 ? null : s.substring(0, end);
            if (dirPrefix != null)
            {
                File dir = new File(outputBasePath,dirPrefix);
                if (!dir.exists()) { dir.mkdirs(); }
            }
            String path = s.replaceAll("/", "\\" + File.separator);

            File outputFile = new File(outputBasePath,path);
            if(outputFile.exists() && !allowOverride)
            {
                //outputFile = new File(outputBasePath,path+".sample");
                continue;
            }
            copyResourceToFile(resource, outputFile);
        }
    }

    private void copyResourceToFile(String resource, File destination) throws IOException
    {
        InputStream source=null;
        try
        {
            source = getClass().getClassLoader().getResourceAsStream(resource);
            FileOutputStream output = new FileOutputStream(destination);
            try
            {
                byte[] buffer = new byte[4096];
                int n;
                while (-1 != (n = source.read(buffer)))
                {
                    output.write(buffer, 0, n);
                }
            }
            finally
            {
                try { output.close(); } catch (IOException ignored) {}
            }
        }
        finally
        {
            try { if(source!=null) source.close(); } catch (IOException ignored) {}
        }
    }
}
