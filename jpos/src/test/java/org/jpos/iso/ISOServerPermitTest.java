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
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Timeout(10)
class ISOServerPermitTest {
    private void set(Object target, String name, Object value) throws Exception {
        Field f = ISOServer.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private ExecutorService executor(ISOServer server) throws Exception {
        Field f = ISOServer.class.getDeclaredField("executor");
        f.setAccessible(true);
        return (ExecutorService) f.get(server);
    }

    private ServerChannel channel() {
        ServerChannel c = mock(ServerChannel.class);
        when(c.clone()).thenReturn(c);
        return c;
    }

    private void runOnce(ISOServer server, ServerChannel channel) throws Exception {
        doAnswer(call -> {
            // End the accept loop after this connection; submission still runs.
            set(server, "shutdown", true);
            call.getArgument(0, ServerSocket.class).close();
            return null;
        }).when(channel).accept(any());
        server.run();
    }

    private void rejected(Throwable failure, boolean disconnectFails) throws Exception {
        ServerChannel c = channel();
        ISOServer server = new ISOServer(0, c, 1);
        server.setConfiguration(new SimpleConfiguration());
        ExecutorService original = executor(server);
        ExecutorService rejecting = mock(ExecutorService.class);
        when(rejecting.submit(any(Runnable.class))).thenThrow(failure);
        set(server, "executor", rejecting);
        if (disconnectFails)
            doThrow(new IOException("injected")).when(c).disconnect();
        try {
            runOnce(server, c);
            assertEquals(0, server.getActiveConnections());
            verify(c).disconnect();
            if (disconnectFails)
                assertEquals(1, failure.getSuppressed().length);
        } finally {
            original.shutdownNow();
        }
    }

    @Test
    void rejectionReturnsPermit() throws Exception {
        rejected(new RejectedExecutionException("injected"), false);
    }

    @Test
    void submissionErrorReturnsPermit() throws Exception {
        rejected(new AssertionError("injected"), false);
    }

    @Test
    void disconnectFailureCannotPreventRelease() throws Exception {
        rejected(new RejectedExecutionException("injected"), true);
    }

    private void session(boolean virtual, int failurePoint) throws Exception {
        ServerChannel c = channel();
        ISOServer server = new ISOServer(0, c, 1) {
            @Override
            protected Session createSession(ServerChannel channel) {
                if (failurePoint == 1)
                    throw new AssertionError("creation failure");
                return new Session(channel) {
                    @Override
                    public void run() {
                        if (failurePoint == 2)
                            throw new AssertionError("execution failure");
                    }
                };
            }
        };
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("virtual-threads", Boolean.toString(virtual));
        server.setConfiguration(cfg);
        ExecutorService e = executor(server);
        try {
            runOnce(server, c);
            e.shutdown();
            assertTrue(e.awaitTermination(5, TimeUnit.SECONDS));
            assertEquals(0, server.getActiveConnections());
        } finally {
            e.shutdownNow();
        }
    }

    @Test
    void normalReturnReleasesForBothExecutors() throws Exception {
        session(false, 0);
        session(true, 0);
    }

    @Test
    void sessionCreationErrorReleasesForBothExecutors() throws Exception {
        session(false, 1);
        session(true, 1);
    }

    @Test
    void sessionExecutionErrorReleasesForBothExecutors() throws Exception {
        session(false, 2);
        session(true, 2);
    }
}
