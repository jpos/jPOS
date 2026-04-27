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

import org.jpos.q2.Q2;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Serializes log file compression across all log listener instances
 * through a single on-demand background worker.
 *
 * <p>The worker thread is created on demand, runs as a low-priority daemon,
 * and exits after {@link #IDLE_TIMEOUT} milliseconds of inactivity.</p>
 */
public class LogCompressor {
    private static final long IDLE_TIMEOUT = 30_000L;

    private static volatile LogCompressor instance;
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
      0,
      1,
      IDLE_TIMEOUT,
      TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue<>(),
      r -> {
          Thread t = Thread.ofPlatform()
            .daemon(true)
            .name("log-compressor", 0)
            .unstarted(r);
          t.setPriority(Thread.NORM_PRIORITY - 1);
          return t;
      }
    );

    private LogCompressor() {
        executor.allowCoreThreadTimeOut(true);
    }

    /**
     * Returns the lazy-initialised singleton.
     *
     * @return the shared {@link LogCompressor}
     */
    public static LogCompressor getInstance() {
        if (instance == null) {
            synchronized (LogCompressor.class) {
                if (instance == null)
                    instance = new LogCompressor();
            }
        }
        return instance;
    }

    /**
     * Submits a compression task for {@code logFile} on the shared executor.
     *
     * @param logFile log file being compressed (used for diagnostic logging)
     * @param task compression task to run
     */
    public void submit(File logFile, Runnable task) {
        executor.execute(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                Q2.getQ2().getLog().warn(
                  String.format("error running log compression task for '%s'", logFile),
                  t
                );
            }
        });
    }
}
