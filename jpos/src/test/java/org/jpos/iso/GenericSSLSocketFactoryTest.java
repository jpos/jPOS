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

import org.jpos.core.SimpleConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Regression coverage for client TLS restrictions and socket ownership. */
public class GenericSSLSocketFactoryTest {
    private static final String PASS = "testpass";
    private static final String PROTOCOL = "TLSv1.2";
    private static final String SUITE = "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";

    @TempDir
    static Path dir;
    private static Path keyStore;

    @BeforeAll
    static void createKeyStore() throws Exception {
        keyStore = dir.resolve("test.p12");
        String keytool = System.getProperty("os.name").startsWith("Windows") ? "keytool.exe" : "keytool";
        Process p = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", keytool).toString(),
                "-genkeypair", "-alias", "test", "-keyalg", "RSA", "-keysize", "2048",
                "-validity", "1", "-dname", "CN=localhost", "-ext", "SAN=dns:localhost",
                "-keystore", keyStore.toString(), "-storetype", "PKCS12",
                "-storepass", PASS, "-keypass", PASS)
                .redirectErrorStream(true).redirectOutput(dir.resolve("keytool.log").toFile()).start();
        if (!p.waitFor(30, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            fail("keytool timed out");
        }
        assertEquals(0, p.exitValue(), "keytool failed");
    }

    private static SimpleConfiguration configuration() {
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("keystore", keyStore.toString());
        cfg.put("storepassword", PASS);
        cfg.put("keypassword", PASS);
        return cfg;
    }

    private static GenericSSLSocketFactory factory(SimpleConfiguration cfg) throws Exception {
        GenericSSLSocketFactory factory = new GenericSSLSocketFactory();
        factory.setConfiguration(cfg);
        return factory;
    }

    private static SimpleConfiguration restrictedConfiguration() {
        SimpleConfiguration cfg = configuration();
        cfg.put("addEnabledProtocol", PROTOCOL);
        cfg.put("addEnabledCipherSuite", SUITE);
        return cfg;
    }

    @Test
    void clientAppliesConfiguredProtocolsAndCipherSuites() throws Exception {
        GenericSSLSocketFactory factory = factory(restrictedConfiguration());
        try (ServerSocket listener = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
             SSLSocket client = (SSLSocket) factory.createSocket("localhost", listener.getLocalPort());
             Socket peer = listener.accept()) {
            assertArrayEquals(new String[]{PROTOCOL}, client.getEnabledProtocols());
            assertArrayEquals(new String[]{SUITE}, client.getEnabledCipherSuites());
        }
    }

    @Test
    void clientKeepsJdkDefaultsWhenNothingIsConfigured() throws Exception {
        GenericSSLSocketFactory factory = factory(configuration());
        try (SSLSocket defaults = (SSLSocket) factory.createSocketFactory().createSocket();
             ServerSocket listener = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
             SSLSocket client = (SSLSocket) factory.createSocket("localhost", listener.getLocalPort());
             Socket peer = listener.accept()) {
            assertArrayEquals(defaults.getEnabledProtocols(), client.getEnabledProtocols());
            assertArrayEquals(defaults.getEnabledCipherSuites(), client.getEnabledCipherSuites());
        }
    }

    @Test
    void serverStillAppliesConfiguredProtocolsAndCipherSuites() throws Exception {
        GenericSSLSocketFactory factory = factory(restrictedConfiguration());
        try (SSLServerSocket server = (SSLServerSocket) factory.createServerSocket(0)) {
            assertArrayEquals(new String[]{PROTOCOL}, server.getEnabledProtocols());
            assertArrayEquals(new String[]{SUITE}, server.getEnabledCipherSuites());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"addEnabledProtocol", "addEnabledCipherSuite"})
    void invalidRestrictionClosesConnectedSocket(String property) throws Exception {
        SimpleConfiguration cfg = configuration();
        cfg.put(property, "UNSUPPORTED_FOR_TEST");
        GenericSSLSocketFactory factory = factory(cfg);
        try (ServerSocket listener = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            listener.setSoTimeout(2000);
            assertThrows(IllegalArgumentException.class,
                    () -> factory.createSocket("localhost", listener.getLocalPort()));
            try (Socket peer = listener.accept()) {
                peer.setSoTimeout(2000);
                assertEquals(-1, peer.getInputStream().read(), "failed creation must close the connection");
            }
        }
    }

    @Test
    void restrictedClientNegotiatesWithAuthenticatedServer() throws Exception {
        GenericSSLSocketFactory serverFactory = factory(configuration());
        SimpleConfiguration cfg = restrictedConfiguration();
        cfg.put("serverauth", "true");
        cfg.put("servername", "localhost");
        GenericSSLSocketFactory clientFactory = factory(cfg);
        var executor = Executors.newSingleThreadExecutor();
        try (SSLServerSocket listener = (SSLServerSocket) serverFactory.createServerSocket(0)) {
            listener.setSoTimeout(5000);
            var server = executor.submit(() -> {
                try (SSLSocket peer = (SSLSocket) listener.accept()) {
                    peer.setSoTimeout(5000);
                    peer.startHandshake();
                    assertEquals(PROTOCOL, peer.getSession().getProtocol());
                    assertEquals(SUITE, peer.getSession().getCipherSuite());
                    assertEquals(42, peer.getInputStream().read());
                    peer.getOutputStream().write(43);
                }
                return null;
            });
            try (SSLSocket client = (SSLSocket) clientFactory.createSocket("localhost", listener.getLocalPort())) {
                client.setSoTimeout(5000);
                assertEquals("HTTPS", client.getSSLParameters().getEndpointIdentificationAlgorithm());
                client.startHandshake();
                assertEquals(PROTOCOL, client.getSession().getProtocol());
                assertEquals(SUITE, client.getSession().getCipherSuite());
                client.getOutputStream().write(42);
                assertEquals(43, client.getInputStream().read());
            }
            server.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
