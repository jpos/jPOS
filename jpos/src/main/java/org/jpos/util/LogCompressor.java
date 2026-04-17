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

package org.jpos.util;

import org.jpos.space.TSpace;

/**
 * Serializes log file compression across all log listener instances
 * through a single on-demand background thread backed by a TSpace work queue.
 *
 * <p>The consumer thread is started when work is submitted and exits
 * after {@link #IDLE_TIMEOUT} milliseconds of inactivity.</p>
 */
public class LogCompressor implements Runnable {
    private static final String QUEUE_KEY = "compression";
    private static final long IDLE_TIMEOUT = 30_000L;

    private static volatile LogCompressor instance;
    private final TSpace<String, Runnable> space = new TSpace<>();
    private Thread thread;

    private LogCompressor() { }

    public static LogCompressor getInstance() {
        if (instance == null) {
            synchronized (LogCompressor.class) {
                if (instance == null)
                    instance = new LogCompressor();
            }
        }
        return instance;
    }

    public synchronized void submit(Runnable task) {
        space.out(QUEUE_KEY, task);
        if (thread == null || !thread.isAlive()) {
            thread = Thread.ofPlatform()
              .daemon(true)
              .name("log-compressor")
              .priority(Thread.NORM_PRIORITY - 1)
              .start(this);
        }
    }

    @Override
    public void run() {
        try {
            Runnable task;
            while ((task = space.in(QUEUE_KEY, IDLE_TIMEOUT)) != null) {
                try {
                    task.run();
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                }
            }
        } finally {
            synchronized (this) {
                if (space.rdp(QUEUE_KEY) == null)
                    thread = null;
            }
        }
    }
}
