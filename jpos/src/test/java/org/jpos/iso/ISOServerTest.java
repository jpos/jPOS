/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.jpos.util.NameRegistrar;
import org.jpos.util.ThreadPool;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ISOServerTest {

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new ISOServer(100, null, null);
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
    public void testDump() throws Exception {
        ISOServer server = newServer();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(os, true, "UTF-8")) {
            server.dump(ps, "");
        }
        assertEquals("connected=0, rx=0, tx=0, last=0" + System.lineSeparator(),
                os.toString("UTF-8"));
    }

    @Test
    public void testDumpPreservesFormatAndSkipsLastDisconnectedAndClearedChannels() throws Exception {
        ISOServer server = newServer();
        Map<String, WeakReference<ISOChannel>> channels = channels(server);
        BaseChannel connected = connectedChannel();
        connected.getCounters()[ISOChannel.RX] = 12;
        connected.getCounters()[ISOChannel.TX] = 34;
        BaseChannel disconnected = new BaseChannel() { };
        channels.put("connected", new WeakReference<>(connected));
        channels.put(ISOServer.LAST, new WeakReference<>(connected));
        channels.put("disconnected", new WeakReference<>(disconnected));
        channels.put("cleared", new WeakReference<>(null));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Locale previous = Locale.getDefault();
        try (PrintStream ps = new PrintStream(os, true, "UTF-8")) {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"));
            server.dump(ps, "  ");
        } finally {
            Locale.setDefault(previous);
        }
        assertEquals("  connected=1, rx=12, tx=34, last=0" + System.lineSeparator()
                        + "    connected: rx=12, tx=34, last=0" + System.lineSeparator(),
                os.toString("UTF-8"));
    }

    @Test
    public void testDumpWhileChannelsChange() throws Exception {
        ISOServer server = newServer();
        Map<String, WeakReference<ISOChannel>> channels = channels(server);
        BaseChannel first = connectedChannel();
        BaseChannel second = connectedChannel();
        channels.put("first", new WeakReference<>(first));
        channels.put("second", new WeakReference<>(second));
        ExecutorService writer = Executors.newSingleThreadExecutor();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(os, true, "UTF-8") {
            private int lines;

            @Override
            public void println(String line) {
                super.println(line);
                // The summary is followed by the first channel line. At this point
                // dump's iterator is active and still has another channel to visit.
                if (++lines == 2) {
                    try {
                        writer.submit(() -> {
                            channels.put("temporary", new WeakReference<>(first));
                            channels.remove("temporary");
                        }).get(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        throw new AssertionError("Channel update failed during dump", e);
                    }
                }
            }
        }) {
            server.dump(ps, "");
        } finally {
            writer.shutdownNow();
            assertTrue(writer.awaitTermination(5, TimeUnit.SECONDS));
        }
        String output = os.toString("UTF-8");
        assertTrue(output.contains("  first: rx=0, tx=0, last=0" + System.lineSeparator()));
        assertTrue(output.contains("  second: rx=0, tx=0, last=0" + System.lineSeparator()));
    }

    private ISOServer newServer() {
        // No listening socket or worker threads are needed to exercise dump.
        return new ISOServer(0, new BaseChannel() { }, mock(ThreadPool.class));
    }

    private BaseChannel connectedChannel() {
        return new BaseChannel() {
            @Override
            public boolean isConnected() {
                return true;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, WeakReference<ISOChannel>> channels(ISOServer server) throws Exception {
        Field field = ISOServer.class.getDeclaredField("channels");
        field.setAccessible(true);
        return (Map<String, WeakReference<ISOChannel>>) field.get(server);
    }
}
