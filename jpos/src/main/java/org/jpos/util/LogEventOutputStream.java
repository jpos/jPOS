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

package org.jpos.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class LogEventOutputStream extends OutputStream implements LogSource, Runnable {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private Logger logger;
    private String realm;
    private ScheduledExecutorService logService;
    private Semaphore lock = new Semaphore(1);
    private volatile LogEvent evt;
    private long delay;

    public LogEventOutputStream() {
        super();
        baos = new ByteArrayOutputStream();
        logService = Executors.newScheduledThreadPool(1);
    }

    public LogEventOutputStream(Logger logger, String realm, long delay) {
        this();
        this.logger = logger;
        this.realm = realm;
        this.delay = delay;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\n') {
            try {
                lock.acquire();
                if (evt == null) {
                    evt = new LogEvent(this, "");
                    logService.schedule(this, delay, TimeUnit.MILLISECONDS);
                }
                evt.addMessage(baos.toString());
                baos = new ByteArrayOutputStream();
            } catch (InterruptedException ignored) {
            } finally {
                lock.release();
            }
        } else {
            baos.write(b);
        }
    }

    @Override
    public void setLogger(Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void run() {
        LogEvent event = null;
        if (evt != null) {
            try {
                lock.acquire();
                event = evt;
                evt = null;
            } catch (InterruptedException ignore) {
            } finally {
                lock.release();
            }
            if (event != null)
                Logger.log(event);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        try {
            lock.acquire();
            logService.shutdown();
        } catch (InterruptedException ignore) {
        } finally {
            lock.release();
        }
    }
}
