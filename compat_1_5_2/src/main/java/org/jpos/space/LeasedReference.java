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

package org.jpos.space;

import java.util.Timer;
import java.util.TimerTask;

/**
 * LeasedReference references an object for a limited amount of time
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
public class LeasedReference extends TimerTask {
    private Object referent;
    private long expiration;

    private static Timer timer = new Timer (true);

    private LeasedReference () { }

    public LeasedReference (Object referent, long duration) {
        super ();
        this.referent = referent;
        this.expiration = System.currentTimeMillis() + duration;
        timer.schedule (this, duration);
    }
    public synchronized Object get() {
        if (isValid ())
            return this.referent;
        this.referent   = null;
        this.expiration = 0;
        super.cancel ();
        return null;
    }
    public synchronized long getExpiration () {
        return expiration;
    }
    public synchronized boolean discard () {
        if (isValid()) {
            this.referent   = null;
            this.expiration = 0;
            super.cancel ();
            return true;
        }
        return false;
    }
    public synchronized long renew (long duration) {
        if (isExpired ())
            return -1;
        return expiration = System.currentTimeMillis() + duration;
    }
    public synchronized boolean isExpired () {
        return expiration <= System.currentTimeMillis();
    }
    public synchronized boolean isValid () {
        return expiration > System.currentTimeMillis();
    }
    public void run () {
        long duration = expiration - System.currentTimeMillis();
        if (duration < 0)
            discard ();
    }
}

