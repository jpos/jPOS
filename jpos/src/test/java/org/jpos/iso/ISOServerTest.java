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

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.channel.CSChannel;
import org.jpos.iso.packager.ISO87BPackager;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;
import org.jpos.util.SimpleLogListener;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ISOServerTest {

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new ISOServer(100, null, 5);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.iso.ServerChannel.getPackager()\" because \"clientSide\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetServerThrowsNotFoundException() throws Throwable {
        try {
            ISOServer.getServer("testISOServerName");
            fail("Expected NotFoundException to be thrown");
        } catch (NameRegistrar.NotFoundException ex) {
            assertEquals("server.testISOServerName", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testSimultaneousConnections() throws ISOException, InterruptedException, IOException, ParseException {
        int runs = 1000;

        Recording recording = new Recording(Configuration.getConfiguration("default"));
        recording.setMaxAge(Duration.ofSeconds(300));
        // jfr print --stack-depth 64 --events jdk.VirtualThreadPinned build/reports/isoserver.jfr
        Path outputPath = Paths.get("build/reports/isoserver.jfr");
        recording.setDestination(outputPath);
        recording.start();

        CSChannel channel = new CSChannel();
        channel.setTimeout(30000);
        channel.setPackager(new ISO87BPackager());

        ISOServer server = new ISOServer(9999, channel, runs+10);
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("backlog", "100");
        cfg.put("connect-timeout", "60000");
        server.setConfiguration(cfg);
        Logger logger = new Logger();
        // logger.addListener (new SimpleLogListener());
        server.setLogger(logger, "ISOServerTest");
        server.addISORequestListener(new AutoResponder());

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        executor.submit(server);
        ISOUtil.sleep (5000L);

        CountDownLatch latch = new CountDownLatch(runs);
        for (int i=0; i<runs; i++) {
            final int j = i;
            executor.submit (() -> {
                 try {
                    CSChannel c = new CSChannel("localhost", 9999, new ISO87BPackager());
                    c.setTimeout(60000);
                    c.setLogger(logger, "test-client");
                    c.setConfiguration(cfg); // we want a connect-timeout
                    c.connect();
                    ISOMsg m = new ISOMsg("0800");
                    m.set(11, ISOUtil.zeropad(j+1, 6));
                    c.send (m);
                    c.receive();
                    ISOUtil.sleep(5000L);
                    c.disconnect();
                } catch (Throwable t) {
                     fail ("%d: could not receive (%s)".formatted(j, t.getMessage()));
                    throw new RuntimeException(t);
                } finally {
                     latch.countDown();
                 }
            });
            LockSupport.parkNanos(Duration.ofMillis(4).toNanos());
        }
        latch.await(300, TimeUnit.SECONDS);
        ISOUtil.sleep (1000L); // let JFR catch-up with latests messages
        recording.dump(outputPath);
        recording.stop();
        recording.close();
    }

    @Test
    public void testSessionEventsUseStableRealmAndDynamicTags() throws Exception {
        List<org.jpos.util.LogEvent> events = new ArrayList<>();
        Logger logger = new Logger();
        logger.setName("test-server-logger");
        logger.addListener(ev -> {
            synchronized (events) {
                events.add(ev);
            }
            return ev;
        });

        int port;
        try (java.net.ServerSocket probe = new java.net.ServerSocket(0)) {
            port = probe.getLocalPort();
        }

        CSChannel channel = new CSChannel();
        channel.setPackager(new ISO87BPackager());
        ISOServer server = new ISOServer(port, channel, 5);
        server.setLogger(logger, "comm/server");
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("backlog", "10");
        server.setConfiguration(cfg);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(server);
        try {
            ISOUtil.sleep(250L);
            try (java.net.Socket client = new java.net.Socket("127.0.0.1", port)) {
                ISOUtil.sleep(250L);
            }

            long deadline = System.currentTimeMillis() + 5000L;
            while (System.currentTimeMillis() < deadline) {
                synchronized (events) {
                    boolean hasStart = events.stream().anyMatch(ev -> ev.getPayLoad().stream().anyMatch(p -> p instanceof org.jpos.log.evt.SessionStart));
                    boolean hasEnd = events.stream().anyMatch(ev -> ev.getPayLoad().stream().anyMatch(p -> p instanceof org.jpos.log.evt.SessionEnd));
                    if (hasStart && hasEnd)
                        break;
                }
                ISOUtil.sleep(100L);
            }

            synchronized (events) {
                org.jpos.util.LogEvent sessionEvent = events.stream()
                  .filter(ev -> ev.getPayLoad().stream().anyMatch(p -> p instanceof org.jpos.log.evt.SessionStart))
                  .findFirst()
                  .orElseThrow();
                assertEquals("comm/server", sessionEvent.getRealm(), "sessionEvent.getRealm()");
                assertEquals(org.jpos.util.Kind.ISO_SESSION, sessionEvent.getTag(), "sessionEvent.getTag()");
                assertTrue(sessionEvent.getTags().containsKey("session"), "sessionEvent.getTags().containsKey(session)");
                assertTrue(sessionEvent.getTags().containsKey("endpoint"), "sessionEvent.getTags().containsKey(endpoint)");
                org.jpos.log.evt.SessionStart start = sessionEvent.getPayLoad().stream()
                  .filter(p -> p instanceof org.jpos.log.evt.SessionStart)
                  .map(p -> (org.jpos.log.evt.SessionStart) p)
                  .findFirst().orElseThrow();
                assertEquals("127.0.0.1", start.host(), "start.host()");
                assertEquals(port, start.localPort(), "start.localPort()");
                assertTrue(start.remotePort() > 0, "start.remotePort()");
                org.jpos.log.evt.SessionEnd end = events.stream()
                  .flatMap(ev -> ev.getPayLoad().stream())
                  .filter(p -> p instanceof org.jpos.log.evt.SessionEnd)
                  .map(p -> (org.jpos.log.evt.SessionEnd) p)
                  .findFirst().orElseThrow();
                assertEquals(start.remotePort(), end.remotePort(), "end.remotePort()");
                assertEquals(port, end.localPort(), "end.localPort()");
                assertTrue(end.duration() != null && !end.duration().isNegative(), "end.duration()");
            }
        } finally {
            server.shutdown();
            executor.shutdownNow();
        }
    }

    @Test
    public void testChannelEventsCarryTheExchangeTraceIdAndSessionTag() throws Exception {
        List<org.jpos.util.LogEvent> events = new ArrayList<>();
        Logger logger = new Logger();
        logger.setName("test-trace-logger");
        logger.addListener(ev -> {
            synchronized (events) {
                events.add(ev);
            }
            return ev;
        });

        int port;
        try (java.net.ServerSocket probe = new java.net.ServerSocket(0)) {
            port = probe.getLocalPort();
        }

        CSChannel serverChannel = new CSChannel();
        serverChannel.setPackager(new ISO87BPackager());
        serverChannel.setLogger(logger, "comm/server");
        ISOServer server = new ISOServer(port, serverChannel, 5);
        server.setLogger(logger, "comm/server");
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("backlog", "10");
        server.setConfiguration(cfg);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(server);
        CSChannel client = new CSChannel("127.0.0.1", port, new ISO87BPackager());
        client.setLogger(logger, "comm/client");
        ISOMsg m = new ISOMsg("0800");
        m.set(7, "0904120000");
        m.set(11, "000321");
        m.set(41, "TERM0001");
        m.set(42, "MERCHANT0000001");
        m.set(70, "301");
        String expected = m.getTraceId();
        try {
            ISOUtil.sleep(250L);
            client.connect();
            client.send(m);
            ISOUtil.sleep(250L);
            client.disconnect();

            long deadline = System.currentTimeMillis() + 5000L;
            while (System.currentTimeMillis() < deadline) {
                synchronized (events) {
                    if (events.stream().anyMatch(ev -> "receive".equals(ev.getTag())))
                        break;
                }
                ISOUtil.sleep(100L);
            }
            synchronized (events) {
                org.jpos.util.LogEvent send = events.stream().filter(ev -> "send".equals(ev.getTag())).findFirst().orElseThrow();
                org.jpos.util.LogEvent receive = events.stream().filter(ev -> "receive".equals(ev.getTag())).findFirst().orElseThrow();
                assertEquals(expected, send.getTags().get("trace-id"), "send trace-id");
                assertEquals(expected, receive.getTags().get("trace-id"), "receive trace-id");
                assertTrue(send.getTags().containsKey("session"), "send session tag");
                assertTrue(receive.getTags().containsKey("session"), "receive session tag");
                assertFalse(send.getTags().containsKey("trace-claimed"), "no claim on send");
                assertFalse(receive.getTags().containsKey("trace-claimed"), "no claim on receive");
            }
        } finally {
            server.shutdown();
            executor.shutdownNow();
        }
    }

    private class AutoResponder implements ISORequestListener {
        @Override
        public boolean process(ISOSource source, ISOMsg m) {
            try {
                m.setResponseMTI();
                m.set(39, "00");
                source.send(m);
            } catch (ISOException | IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

}
