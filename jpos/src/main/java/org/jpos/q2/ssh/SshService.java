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

package org.jpos.q2.ssh;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.jdom2.Element;
import org.jpos.q2.QBeanSupport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.*;

public class SshService extends QBeanSupport implements SshCLIContextMBean
{
    SshServer sshd = null;

    @Override
    protected void startService() throws Exception {
        String username=cfg.get("auth-username","admin");
        String authorizedKeysFilename=cfg.get("authorized-keys-file","cfg/authorized_keys");
        String hostKeys=cfg.get("hostkeys-file","cfg/hostkeys.ser");
        int port=cfg.getInt("port",2222);
        checkAuthorizedKeys(authorizedKeysFilename);

        String[] prefixes= getPrefixes();

        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(hostKeys).toPath()));

        CliShellFactory csf = new CliShellFactory(getServer(), prefixes);
        sshd.setShellFactory(csf);
        sshd.setCommandFactory(csf);


        sshd.setUserAuthFactories(Collections.singletonList(new UserAuthPublicKeyFactory()));
        sshd.setPublickeyAuthenticator(new AuthorizedKeysFileBasedPKA(username, authorizedKeysFilename));
        sshd.start();
        log.info("Started SSHD @ port "+port);
    }

    @Override
    protected void stopService() throws Exception
    {
        log.info("Stopping SSHD");
        if(sshd!=null) {
            new Thread() {
                public void run() {
                    try {
                        sshd.stop(true);
                    } catch (IOException ignored) { }
                    sshd=null;
                }
            }.start();
        }
    }

    private String[] getPrefixes()
    {
        String[] prefixes=cfg.getAll("prefixes");
        if(prefixes!=null && prefixes.length>0) return prefixes;

        return new String[]{"org.jpos.q2.cli.",
                            "org.jpos.ee.cli.cmds."};
    }

    private void checkAuthorizedKeys (String s) throws IOException {
        String OS = System.getProperty("os.name").toLowerCase();
        if((OS.indexOf("win") >= 0)){
            log.info("Windows Detected, ignoring file permissions check: "+OS);
            return;
        }
        Path file = Paths.get(s);
        PosixFileAttributes attrs =
          Files.getFileAttributeView(file, PosixFileAttributeView.class)
          .readAttributes();
        Set<PosixFilePermission> perms =  attrs.permissions();
        if (perms.contains(GROUP_WRITE) || perms.contains(OTHERS_WRITE) ||
            (perms.contains(OWNER_WRITE) && !Files.isWritable(file)))
            throw new IllegalArgumentException(
              String.format ("Invalid permissions '%s' for file '%s'", PosixFilePermissions.toString(perms), s)
            );
    }

    public static Element createDescriptor (int port, String username, String authorizedKeysFile, String hostKeyFile) {
        return new Element("sshd")
          .setAttribute("name", "sshd")
          .addContent(createProperty ("port", Integer.toString(port)))
          .addContent(createProperty ("auth-username", username))
          .addContent (createProperty ("authorized-keys-file", authorizedKeysFile))
          .addContent (createProperty ("hostkeys-file", hostKeyFile));
    }

    private static Element createProperty (String name, String value) {
        return new Element ("property")
          .setAttribute("name", name)
          .setAttribute("value", value);
    }

}
