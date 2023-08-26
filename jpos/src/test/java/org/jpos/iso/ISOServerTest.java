/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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
