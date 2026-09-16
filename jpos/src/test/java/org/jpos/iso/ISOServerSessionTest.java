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

import org.jpos.log.evt.SessionEnd;
import org.jpos.iso.channel.CSChannel;
import org.jpos.iso.packager.ISO87BPackager;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ISOServerSessionTest {
    private ServerChannel channel;
    private ISOServer server;
    private final List<LogEvent> logs = new ArrayList<>();
    private final List<EventObject> events = new ArrayList<>();
    private final AtomicBoolean connected = new AtomicBoolean(true);

    @BeforeEach
    void setUp() throws Exception {
        channel = mock(ServerChannel.class);
        when(channel.getName()).thenReturn("test-session");
        when(channel.isConnected()).thenAnswer(invocation -> connected.get());
        doAnswer(invocation -> {
            connected.set(false);
            return null;
        }).when(channel).disconnect();
        server = new ISOServer(0, channel, 1);
        Logger logger = new Logger();
        logger.addListener(event -> {
            logs.add(event);
            return event;
        });
        server.setLogger(logger, "session-test");
        server.addServerEventListener(events::add);
    }

    @Test
    void realChannelDisconnectEndsWithoutSessionError() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setSoTimeout(5000);
            CSChannel client = new CSChannel("localhost", socket.getLocalPort(), new ISO87BPackager());
            CSChannel accepted = new CSChannel(new ISO87BPackager());
            accepted.setTimeout(5000);
            try {
                client.connect();
                accepted.accept(socket);
                AtomicInteger processed = new AtomicInteger();
                server.addISORequestListener((source, message) -> {
                    processed.incrementAndGet();
                    try {
                        ((ISOChannel) source).disconnect();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                    return true;
                });
                ISOMsg message = new ISOMsg("0800");
                message.set(11, "000001");
                client.send(message);
                server.createSession(accepted).run();
                assertEquals(1, processed.get());
                assertFalse(accepted.isConnected());
                assertEquals(0, countTag("session-error"));
                assertEquals(0, countTag("session-warning"));
                assertSessionEnded();
            } finally {
                client.disconnect();
                accepted.disconnect();
            }
        }
    }

    @Test
    void handledDisconnectEndsWithoutAnotherReceive() throws Exception {
        disconnectFromListener(true);
    }

    @Test
    void unhandledDisconnectPreservesListenerDispatchThenEnds() throws Exception {
        disconnectFromListener(false);
    }

    private void disconnectFromListener(boolean handled) throws Exception {
        ISOMsg message = new ISOMsg("0800");
        when(channel.receive()).thenReturn(message).thenThrow(new IOException("unconnected ISOChannel"));
        server.addISORequestListener((source, m) -> {
            try {
                channel.disconnect();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return handled;
        });
        ISORequestListener next = mock(ISORequestListener.class);
        server.addISORequestListener(next);

        server.createSession(channel).run();

        verify(channel, times(1)).receive();
        verify(channel, times(2)).disconnect(); // listener and existing session cleanup
        verify(next, times(handled ? 0 : 1)).process(channel, message);
        assertEquals(0, countTag("session-error"));
        assertEquals(0, countTag("session-warning"));
        assertSessionEnded();
    }

    @Test
    void connectedListenerContinuesReceiving() throws Exception {
        ISOMsg first = new ISOMsg("0800");
        ISOMsg second = new ISOMsg("0800");
        when(channel.receive()).thenReturn(first, second).thenThrow(new EOFException());
        ISORequestListener listener = mock(ISORequestListener.class);
        when(listener.process(channel, first)).thenReturn(true);
        when(listener.process(channel, second)).thenReturn(true);
        server.addISORequestListener(listener);

        server.createSession(channel).run();

        verify(channel, times(3)).receive();
        verify(listener).process(channel, first);
        verify(listener).process(channel, second);
        verify(channel).disconnect();
        assertEquals(0, countTag("session-error"));
        assertEquals(0, countTag("session-warning"));
        assertSessionEnded();
    }

    @Test
    void receiveFailureRemainsAnError() throws Exception {
        IOException failure = new IOException("read failed");
        when(channel.receive()).thenThrow(failure);
        server.createSession(channel).run();
        assertLogged("session-error", failure);
        assertSessionEnded();
    }

    @Test
    void listenerFailureAfterDisconnectRemainsAnError() throws Exception {
        when(channel.receive()).thenReturn(new ISOMsg("0800"));
        IllegalStateException failure = new IllegalStateException("listener failed");
        server.addISORequestListener((source, message) -> {
            connected.set(false);
            throw failure;
        });
        server.createSession(channel).run();
        assertLogged("session-error", failure);
        assertSessionEnded();
    }

    @Test
    void peerSocketFailureRemainsAWarning() throws Exception {
        SocketException failure = new SocketException("connection reset");
        when(channel.receive()).thenThrow(failure);
        server.createSession(channel).run();
        assertLogged("session-warning", failure);
        assertEquals(0, countTag("session-error"));
        assertSessionEnded();
    }

    @Test
    void cleanupFailureAfterIntentionalDisconnectRemainsVisible() throws Exception {
        when(channel.receive()).thenReturn(new ISOMsg("0800"))
            .thenThrow(new IOException("unconnected ISOChannel"));
        IOException failure = new IOException("cleanup failed");
        AtomicInteger disconnects = new AtomicInteger();
        doAnswer(invocation -> {
            connected.set(false);
            if (disconnects.incrementAndGet() > 1)
                throw failure;
            return null;
        }).when(channel).disconnect();
        server.addISORequestListener((source, message) -> {
            try {
                channel.disconnect();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return true;
        });
        server.createSession(channel).run();
        verify(channel).receive();
        assertLogged("session-error", failure);
        assertSessionEnded();
    }

    private long countTag(String tag) {
        return logs.stream().filter(event -> tag.equals(event.getTag())).count();
    }

    private void assertLogged(String tag, Throwable failure) {
        assertEquals(1, countTag(tag));
        assertTrue(logs.stream().anyMatch(event -> tag.equals(event.getTag()) && event.getPayLoad().contains(failure)));
    }

    private void assertSessionEnded() {
        assertEquals(1, events.stream().filter(ISOServerClientDisconnectEvent.class::isInstance).count());
        assertEquals(1, logs.stream().flatMap(event -> event.getPayLoad().stream())
            .filter(SessionEnd.class::isInstance).count());
    }
}
