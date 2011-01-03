/*
 * Copyright (c) 2002-2007, Marc Prud'hommeaux. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 */

package org.jpos.q2;

import jline.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/*
 * This class is based on jLine's ClassNameCompletor with a two-line patch:

From 6dab177d2eb23a99933fdffae4c6fb7ebd33d2c9 Mon Sep 17 00:00:00 2001
From: Alejandro Revilla <apr@jpos.org>
Date: Thu, 16 Dec 2010 14:02:09 -0200
Subject: [PATCH] Ignore non jars (i.e. .jnilib, etc)

---
 src/main/java/jline/ClassNameCompletor.java |    2 ++
 1 files changed, 2 insertions(+), 0 deletions(-)

diff --git a/src/main/java/jline/ClassNameCompletor.java b/src/main/java/jline/ClassNameCompletor.java
index 7c6c6e0..3ef5802 100644
--- a/src/main/java/jline/ClassNameCompletor.java
+++ b/src/main/java/jline/ClassNameCompletor.java
@@ -85,6 +85,8 @@ public class ClassNameCompletor extends SimpleCompletor {
              {
                 continue;
             }
+            if (!file.toString().endsWith (".jar"))
+                continue;

             JarFile jf = new JarFile(file);

--
1.7.3.3

-- end of patch --

 *
 * This patch has been submitted to Marc Prud'hommeaux who is considering its inclusion in future versions of jLine
 * (or probably fixing the issue with a better patch). Meanwhile, he suggest we use our own ClassNameCompletor.
 */


/**
 *  A Completor implementation that completes java class names. By default,
 *  it scans the java class path to locate all the classes.
 *
 *  @author  <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 */
public class CLIClassNameCompletor extends SimpleCompletor {

    /**
     *  Complete candidates using all the classes available in the
     *  java <em>CLASSPATH</em>.
     */
    public CLIClassNameCompletor() throws IOException {
        this(null);
    }

    public CLIClassNameCompletor(final SimpleCompletorFilter filter)
        throws IOException {
        super(getClassNames(), filter);
        setDelimiter(".");
    }

    public static String[] getClassNames() throws IOException {
        Set urls = new HashSet();

        for (ClassLoader loader = ClassNameCompletor.class
            .getClassLoader(); loader != null;
                 loader = loader.getParent()) {
            if (!(loader instanceof URLClassLoader)) {
                continue;
            }

            urls.addAll(Arrays.asList(((URLClassLoader) loader).getURLs()));
        }

        // Now add the URL that holds java.lang.String. This is because
        // some JVMs do not report the core classes jar in the list of
        // class loaders.
        Class[] systemClasses = new Class[] {
            String.class, javax.swing.JFrame.class
            };

        for (int i = 0; i < systemClasses.length; i++) {
            URL classURL = systemClasses[i].getResource("/"
                + systemClasses[i].getName() .replace('.', '/') + ".class");

            if (classURL != null) {
                URLConnection uc = (URLConnection) classURL.openConnection();

                if (uc instanceof JarURLConnection) {
                    urls.add(((JarURLConnection) uc).getJarFileURL());
                }
            }
        }

        Set classes = new HashSet();

        for (Iterator i = urls.iterator(); i.hasNext();) {
            URL url = (URL) i.next();
            File file = new File(url.getFile());

            if (file.isDirectory()) {
                Set files = getClassFiles(file.getAbsolutePath(),
                    new HashSet(), file, new int[] { 200 });
                classes.addAll(files);

                continue;
            }

            if ((file == null) || !file.isFile()) // TODO: handle directories
             {
                continue;
            }
            if (!file.toString().endsWith (".jar"))
                continue;

            JarFile jf = new JarFile(file);

            for (Enumeration e = jf.entries(); e.hasMoreElements();) {
                JarEntry entry = (JarEntry) e.nextElement();

                if (entry == null) {
                    continue;
                }

                String name = entry.getName();

                if (!name.endsWith(".class")) // only use class files
                 {
                    continue;
                }

                classes.add(name);
            }
        }

        // now filter classes by changing "/" to "." and trimming the
        // trailing ".class"
        Set classNames = new TreeSet();

        for (Iterator i = classes.iterator(); i.hasNext();) {
            String name = (String) i.next();
            classNames.add(name.replace('/', '.').
                substring(0, name.length() - 6));
        }

        return (String[]) classNames.toArray(new String[classNames.size()]);
    }

    private static Set getClassFiles(String root, Set holder, File directory,
        int[] maxDirectories) {
        // we have passed the maximum number of directories to scan
        if (maxDirectories[0]-- < 0) {
            return holder;
        }

        File[] files = directory.listFiles();

        for (int i = 0; (files != null) && (i < files.length); i++) {
            String name = files[i].getAbsolutePath();

            if (!(name.startsWith(root))) {
                continue;
            } else if (files[i].isDirectory()) {
                getClassFiles(root, holder, files[i], maxDirectories);
            } else if (files[i].getName().endsWith(".class")) {
                holder.add(files[i].getAbsolutePath().
                    substring(root.length() + 1));
            }
        }

        return holder;
    }
}
