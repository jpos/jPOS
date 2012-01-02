/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import java.util.TimerTask;


/**
 * WatchDog will issue a warning message 
 * if not canceled on time
 */
public class WatchDog extends TimerTask {
    String message;
    String logName;
    String realm;
    public WatchDog (long maxWait, String message) {
        this.message = message;
        this.logName = "Q2";
        this.realm = "watchdog";
        DefaultTimer.getTimer().schedule (this, maxWait);
    }
    public void setLogName (String logName) {
        this.logName = logName;
    }
    public void setRealm (String realm) {
        this.realm = realm;
    }
    public void run () {
        Log.getLog (logName, realm).warn (message);
    }
}

