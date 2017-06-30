/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2017 jPOS Software SRL
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
    private StringBuilder sb;
    private Semaphore lock = new Semaphore(1);
    private volatile boolean isScheduled = false;

    public LogEventOutputStream() {
        super();
        sb = new StringBuilder();
        baos = new ByteArrayOutputStream();
        logService = Executors.newScheduledThreadPool(1);
    }

    public LogEventOutputStream(Logger logger, String realm) {
        this();
        this.logger = logger;
        this.realm = realm;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\n') {
            try {
                lock.acquire();
                sb.append(baos.toString());
                sb.append(System.lineSeparator());
                baos = new ByteArrayOutputStream();
                if (!isScheduled) {
                    isScheduled = !isScheduled;
                    logService.schedule(this, 500, TimeUnit.MILLISECONDS);
                }
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
        try {
            lock.acquire();
            isScheduled = false;
            if (sb.length() > 0) {
                Logger.log(new LogEvent(this, "", sb.toString()));
                sb = new StringBuilder();
            }
        } catch (InterruptedException ignore) {
        } finally {
            lock.release();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        try {
            lock.acquire();
            logService.shutdown();
            sb = new StringBuilder();
        } catch (InterruptedException ignore) {
        } finally {
            lock.release();
        }
    }
}
