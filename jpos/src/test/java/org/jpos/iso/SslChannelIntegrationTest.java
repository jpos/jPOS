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

package org.jpos.iso;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * $Revision$
 * $Date$
 * $Author$
 */
public class SslChannelIntegrationTest {

    private static final int PORT = 4000;

    private static Path keyStore;
    private Logger logger;

    @BeforeAll
    static void createKeyStore() throws Exception {
        keyStore = Files.createTempFile("jpos-ssl-", ".jks");
        Files.delete(keyStore);
        String keytoolCommand = System.getProperty("os.name").startsWith("Windows") ? "keytool.exe" : "keytool";
        Process keytool = new ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", keytoolCommand).toString(),
            "-genkeypair", "-alias", "selfsigned", "-keyalg", "RSA",
            "-dname", "CN=unused.example.com", "-ext", "SAN=dns:*.visa.com",
            "-validity", "365", "-keystore", keyStore.toString(), "-storetype", "JKS",
            "-storepass", "password", "-keypass", "password", "-noprompt"
        ).inheritIO().start();
        if (keytool.waitFor() != 0)
            throw new IOException("Unable to create SSL test keystore");
    }

    @AfterAll
    static void removeKeyStore() throws IOException {
        Files.deleteIfExists(keyStore);
    }

    @BeforeEach
    public void setUp() throws Exception {
        logger = new Logger();
        logger.addListener(new SimpleLogListener());
    }

    @Test
    public void serverAuthenticationSupportsWildcardCertificates() throws Exception {
        ISOServer isoServer = newIsoServer();
        new Thread(isoServer).start();

        XMLChannel clientChannel = newClientChannel();

        int tries = 10;
        while (!clientChannel.isConnected() && tries > 0) {
            try {
                clientChannel.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            tries--;
            Thread.sleep(10L);
        }

        // need to push some traffic through to complete the SSL handshake
        clientChannel.send(new ISOMsg("0800"));
        assertThat(clientChannel.receive(), hasMti("0810"));

        isoServer.shutdown();

        assertThrows(EOFException.class, () -> {
            clientChannel.receive();
        }, "clientChannel should be closed");
    }

    private XMLChannel newClientChannel() throws IOException, ISOException {
        XMLChannel clientChannel = new XMLChannel(new XMLPackager());
        clientChannel.setSocketFactory(new GenericSSLSocketFactory());
        clientChannel.setConfiguration(clientConfiguration());
        clientChannel.setLogger(logger, "client.channel");
        clientChannel.setHost("localhost", PORT);
        return clientChannel;
    }

    private ISOServer newIsoServer() throws IOException, ISOException {
        XMLChannel clientSide = new XMLChannel(new XMLPackager());
        clientSide.setLogger(logger, "server.channel");

        ISOServer isoServer = new ISOServer(PORT, clientSide, 100);
        isoServer.setSocketFactory(new GenericSSLSocketFactory());
        isoServer.setConfiguration(serverConfiguration());
        isoServer.setLogger(logger, "server");
        isoServer.addISORequestListener(new TestListener());
        return isoServer;
    }

    private Configuration serverConfiguration() {
        Properties props = new Properties();
        props.put("keystore", keyStore.toString());
        props.put("storepassword", "password");
        props.put("keypassword", "password");
        // props.put("addEnabledCipherSuite", "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        return new SimpleConfiguration(props);
    }

    private Configuration clientConfiguration() {
        Properties props = new Properties();
        props.put("keystore", keyStore.toString());
        props.put("serverauth", "true");
        props.put("servername", "gateway.visa.com");
        props.put("storepassword", "password");
        props.put("keypassword", "password");
        // props.put("addEnabledCipherSuite", "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
        props.put("timeout", "1000");
        props.put("connect-timeout", "1000");
        return new SimpleConfiguration(props);
    }

    private class TestListener implements ISORequestListener {

        public boolean process(ISOSource source, ISOMsg m) {
            try {
                source.send(new ISOMsg("0810"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    private Matcher<ISOMsg> hasMti(final String mti) {
        return new TypeSafeMatcher<ISOMsg>() {
            @Override
            public boolean matchesSafely(ISOMsg isoMsg) {
                return mti.equals(isoMsg.getString(0));
            }

            public void describeTo(Description description) {
                description.appendText("ISOMsg with mti ").appendValue(mti);
            }
        };
    }

}
