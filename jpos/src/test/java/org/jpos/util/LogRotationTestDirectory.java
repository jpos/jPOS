/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
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
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.util.List;
import java.util.Set;

public class LogRotationTestDirectory {

    private final Path directory;

    private final FileStore filestore;

    private AclFileAttributeView view;
    private AclEntry denyEntry;

    public LogRotationTestDirectory(Path tempDir) throws IOException {
        directory = tempDir;
        filestore = Files.getFileStore(directory);
        if (!filestore.supportsFileAttributeView(PosixFileAttributeView.class) &&
                filestore.supportsFileAttributeView(AclFileAttributeView.class)) {
            view = Files.getFileAttributeView(directory, AclFileAttributeView.class);
            denyEntry = AclEntry
                    .newBuilder()
                    .setType(AclEntryType.DENY)
                    .setPrincipal(view.getOwner())
                    .setPermissions(AclEntryPermission.ADD_FILE)
                    .setFlags(AclEntryFlag.FILE_INHERIT,
                            AclEntryFlag.DIRECTORY_INHERIT)
                    .build();
        }
    }

    public synchronized Path getDirectory() {
        return directory;
    }

    public Path getFile(String filename) {
        return directory.resolve(filename);
    }

    public void preventNewFileCreation() throws IOException {
        if (filestore.supportsFileAttributeView(PosixFileAttributeView.class)) {
            Set<PosixFilePermission> perms = Files.readAttributes(directory, PosixFileAttributes.class).permissions();
            perms.remove(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(directory, perms);
        } else if (filestore.supportsFileAttributeView(AclFileAttributeView.class)) {
            List<AclEntry> acl = view.getAcl();
            acl.add(0, denyEntry);
            view.setAcl(acl);
        } else {
            throw new IOException("Directory " + directory.toString() + " has unsupported FileStore type: " + filestore.type());
        }
    }

    public void allowNewFileCreation() throws IOException {
        if (filestore.supportsFileAttributeView(PosixFileAttributeView.class)) {
            Set<PosixFilePermission> perms = Files.readAttributes(directory, PosixFileAttributes.class).permissions();
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(directory, perms);
        } else if (filestore.supportsFileAttributeView(AclFileAttributeView.class)) {
            List<AclEntry> acl = view.getAcl();
            acl.remove(denyEntry);
            view.setAcl(acl);
        } else {
            throw new IOException("Directory " + directory.toString() + " has unsupported FileStore type: " + filestore.type());
        }
    }
}
