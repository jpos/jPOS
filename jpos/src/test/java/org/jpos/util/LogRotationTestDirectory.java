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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class LogRotationTestDirectory {

    private Path directory;

    public LogRotationTestDirectory(Path tempDir) {
        directory = tempDir;
    }

    public synchronized Path getDirectory() {
        return directory;
    }

    public Path getFile(String filename) {
        return directory.resolve(filename);
    }

    public void preventNewFileCreation() throws IOException {
        Set<PosixFilePermission> perms = Files.readAttributes(directory, PosixFileAttributes.class).permissions();
        perms.remove(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(directory, perms);
    }

    public void allowNewFileCreation() throws IOException {
        Set<PosixFilePermission> perms = Files.readAttributes(directory, PosixFileAttributes.class).permissions();
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(directory, perms);
    }
}
