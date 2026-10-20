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
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * addEnabledProtocol / addEnabledCipherSuite were only honoured by
 * createServerSocket, never by createSocket, so configuring them on a client
 * channel (e.g. an ISOChannel talking to a host that requires a specific
 * protocol version or cipher suite) silently had no effect.
 */
public class GenericSSLSocketFactoryTest {

    private static final String PASS = "testpass";

    @Test
    void clientAppliesConfiguredProtocolsAndCipherSuites() throws Exception {
        Path dir = Files.createTempDirectory("gsf-test");
        try {
            SimpleConfiguration cfg = new SimpleConfiguration();
            cfg.put("keystore", selfSignedKeyStore(dir).toString());
            cfg.put("storepassword", PASS);
            cfg.put("keypassword", PASS);
            // no server to validate against in this test; not what's under test here
            cfg.put("serverauth", "false");
            cfg.put("addEnabledProtocol", "TLSv1.2");
            cfg.put("addEnabledCipherSuite", "TLS_AES_128_GCM_SHA256");

            GenericSSLSocketFactory factory = new GenericSSLSocketFactory();
            factory.setConfiguration(cfg);

            // createSocket defers the handshake, so a plain TCP listener is enough
            // to inspect what the client socket was configured with
            try (TcpListener listener = TcpListener.start();
                 Socket ignored = factory.createSocket("127.0.0.1", listener.port())) {

                SSLSocket client = (SSLSocket) ignored;
                assertArrayEquals(new String[]{"TLSv1.2"}, client.getEnabledProtocols(),
                        "addEnabledProtocol must be applied to the socket createSocket returns");
                assertArrayEquals(new String[]{"TLS_AES_128_GCM_SHA256"},
                        client.getEnabledCipherSuites(),
                        "addEnabledCipherSuite must be applied to the socket createSocket returns");
            }
        } finally {
            deleteTree(dir);
        }
    }

    @Test
    void clientKeepsJdkDefaultsWhenNothingIsConfigured() throws Exception {
        Path dir = Files.createTempDirectory("gsf-test");
        try {
            SimpleConfiguration cfg = new SimpleConfiguration();
            cfg.put("keystore", selfSignedKeyStore(dir).toString());
            cfg.put("storepassword", PASS);
            cfg.put("keypassword", PASS);
            cfg.put("serverauth", "false");

            GenericSSLSocketFactory factory = new GenericSSLSocketFactory();
            factory.setConfiguration(cfg);

            try (TcpListener listener = TcpListener.start();
                 Socket ignored = factory.createSocket("127.0.0.1", listener.port())) {

                SSLSocket client = (SSLSocket) ignored;
                assertTrue(client.getEnabledProtocols().length > 1,
                        "with no addEnabledProtocol the JDK defaults must be left alone");
                assertTrue(Arrays.stream(client.getEnabledCipherSuites())
                                .anyMatch(c -> !c.equals("TLS_AES_128_GCM_SHA256")),
                        "with no addEnabledCipherSuite the JDK defaults must be left alone");
            }
        } finally {
            deleteTree(dir);
        }
    }

    @Test
    void serverStillAppliesConfiguredProtocolsAndCipherSuites() throws Exception {
        Path dir = Files.createTempDirectory("gsf-test");
        try {
            SimpleConfiguration cfg = new SimpleConfiguration();
            cfg.put("keystore", selfSignedKeyStore(dir).toString());
            cfg.put("storepassword", PASS);
            cfg.put("keypassword", PASS);
            cfg.put("addEnabledProtocol", "TLSv1.2");
            cfg.put("addEnabledCipherSuite", "TLS_AES_128_GCM_SHA256");

            GenericSSLSocketFactory factory = new GenericSSLSocketFactory();
            factory.setConfiguration(cfg);

            try (SSLServerSocket server = (SSLServerSocket) factory.createServerSocket(0)) {
                assertArrayEquals(new String[]{"TLSv1.2"}, server.getEnabledProtocols(),
                        "createServerSocket must keep applying addEnabledProtocol");
                assertArrayEquals(new String[]{"TLS_AES_128_GCM_SHA256"},
                        server.getEnabledCipherSuites(),
                        "createServerSocket must keep applying addEnabledCipherSuite");
            }
        } finally {
            deleteTree(dir);
        }
    }

    /** Self-signed key store: only needs to be loadable, never trusted. */
    private static Path selfSignedKeyStore(Path dir) throws Exception {
        Path ks = dir.resolve("test.p12");
        Process p = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "keytool").toString(),
                "-genkeypair", "-alias", "test", "-keyalg", "RSA", "-keysize", "2048",
                "-validity", "1", "-dname", "CN=GenericSSLSocketFactoryTest",
                "-keystore", ks.toString(), "-storetype", "PKCS12",
                "-storepass", PASS, "-keypass", PASS)
                .redirectErrorStream(true)
                .start();
        String out = new String(p.getInputStream().readAllBytes());
        assertEquals(0, p.waitFor(), "keytool failed: " + out);
        return ks;
    }

    private static void deleteTree(Path dir) throws Exception {
        if (!Files.exists(dir)) return;
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (Exception ignored) {
                    // best effort
                }
            });
        }
    }

    /** Bare TCP listener; the TLS handshake is never needed for these assertions. */
    private static final class TcpListener implements AutoCloseable {
        private final ServerSocket socket;

        static TcpListener start() throws Exception {
            ServerSocket ss = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
            Thread t = new Thread(() -> {
                try {
                    Socket s = ss.accept();
                    Thread.sleep(5_000);
                    s.close();
                } catch (Exception ignored) {
                    // listener closed
                }
            });
            t.setDaemon(true);
            t.start();
            return new TcpListener(ss);
        }

        private TcpListener(ServerSocket socket) {
            this.socket = socket;
        }

        int port() {
            return socket.getLocalPort();
        }

        @Override
        public void close() throws Exception {
            socket.close();
        }
    }
}