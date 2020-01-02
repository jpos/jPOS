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
