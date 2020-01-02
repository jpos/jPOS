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

import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author vsalaman
 */
public class Install
{
    private static final String DEFAULT_PREFIX = "META-INF/q2/installs/";

    public static void main(String[] args) throws Exception
    {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options ();
        options.addOption ("p", "prefix", true, String.format("prefix, defaults to '%s'", DEFAULT_PREFIX));
        options.addOption ("q", "quiet", false, "do not show information about files being extracted");
        options.addOption ("f", "force", false, "override existing files in output directory");
        options.addOption ("o", "outputDir", true, "output directory, defaults to " + new File(".").getAbsolutePath());
        options.addOption ("h", "help", false, "Usage information");

        CommandLine line = parser.parse (options, args);
        if (line.hasOption ("h")) {
            HelpFormatter helpFormatter = new HelpFormatter ();
            helpFormatter.printHelp ("install", options);
            return;
        }
        String prefix = line.hasOption("p") ? line.getOptionValue("prefix") : DEFAULT_PREFIX;
        String outputBasePath = line.hasOption("o") ? line.getOptionValue("o") : ".";
        new Install().install(
            line.hasOption("f"),
            new File(outputBasePath),
            !line.hasOption("q"),
            prefix
        );
    }

    public void install(boolean allowOverride,File outputBasePath, boolean verbose, String prefix) throws IOException
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
                if (!dir.exists()) {
                    if (verbose)
                        System.out.println("Created " + dir.getAbsolutePath());
                    dir.mkdirs();
                }
            }
            String path = s.replaceAll("/", "\\" + File.separator);
            File outputFile = new File(outputBasePath,path);
            if(outputFile.exists() && !allowOverride)
            {
                if (verbose) {
                    System.out.printf ("%s exists, use --force to override%n", outputFile);
                }
                //outputFile = new File(outputBasePath,path+".sample");
                continue;
            }
            copyResourceToFile(resource, outputFile, verbose);
        }
    }

    private void copyResourceToFile(String resource, File destination, boolean verbose) throws IOException
    {
        InputStream source=null;
        try
        {
            source = getClass().getClassLoader().getResourceAsStream(resource);
            FileOutputStream output = new FileOutputStream(destination);
            if (verbose) {
                System.out.println("extracting " + destination);
            }
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
                try { output.close(); } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
        finally
        {
            try { if(source!=null) source.close(); } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }
}
