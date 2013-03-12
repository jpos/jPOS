/*
 * Copyright (c) 2003 - 2013 Tyro Payments Limited.
 * 125 York St, Sydney NSW 2000.
 * All rights reserved.
 */
package org.jpos.util;

import java.io.File;

public class LogRotationTestDirectory {

    private File directory;

    public LogRotationTestDirectory() {
    }

    public synchronized File getDirectory() {
        if (directory == null) {
            directory = new File(System.getProperty("java.io.tmpdir"), "jposLogRotationTestDir");
            directory.mkdirs();
        }
        return directory;
    }

    public File getFile(String filename) {
        return new File(getDirectory(), filename);
    }

    public void preventNewFileCreation() {
        getDirectory().setExecutable(false);
    }

    public void allowNewFileCreation() {
        getDirectory().setExecutable(true);
    }

    public synchronized void delete() {
        if (directory != null) {
            for (File log : directory.listFiles()) {
                System.err.println("Deleting " + log);
                log.deleteOnExit();
                log.delete();
            }

            directory.delete();
        }
    }
}
