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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;


/**
 * WatchDog will issue a warning message 
 * if not canceled on time
 */
@SuppressWarnings("unused")
public class WatchDog extends TimerTask {
    String message;
    String logName;
    String realm;
    private static Timer timer = new Timer(true);
    private static AtomicLong counter = new AtomicLong(0L);
    /** Number of timer schedules between proactive cancellation purges. */
    public static long PURGE_INTERVAL = 1000L;

    /**
     * Schedules a silent watchdog that fires after {@code duration} milliseconds.
     *
     * @param duration delay in milliseconds before {@link #run()} fires
     */
    public WatchDog (long duration) {
        timer.schedule(this, duration);
        if (counter.incrementAndGet() % PURGE_INTERVAL == 0)
            timer.purge();  // pro-active purge due to excessive number of timertask cancels.
    }
    /**
     * Schedules a watchdog that, if not cancelled in time, logs {@code message} as a warning.
     *
     * @param duration delay in milliseconds before {@link #run()} fires
     * @param message warning message to log when the timer expires
     */
    public WatchDog (long duration, String message) {
        this(duration);
        this.logName = "Q2";
        this.realm = "watchdog";
        this.message = message;
    }
    /**
     * Overrides the logger name used for the warning.
     *
     * @param logName logger name (defaults to {@code Q2})
     */
    public void setLogName (String logName) {
        this.logName = logName;
    }
    /**
     * Overrides the realm used for the warning.
     *
     * @param realm logger realm (defaults to {@code watchdog})
     */
    public void setRealm (String realm) {
        this.realm = realm;
    }
    /** Logs the configured message at warning level when the timer expires. */
    public void run () {
        if (message != null)
            Log.getLog (logName, realm).warn (message);
    }
}
