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

/*
 * $Log$
 * Revision 1.6  2003/10/13 10:46:16  apr
 * tabs expanded to spaces
 *
 * Revision 1.5  2003/05/16 04:11:04  alwyns
 * Import cleanups.
 *
 * Revision 1.4  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.3  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.2  2000/02/03 00:41:55  apr
 * .
 *
 * Revision 1.1  2000/01/30 23:32:53  apr
 * pre-Alpha - CVS sync
 *
 */

/**
 * @author apr@cs.com.uy
 * @since jPOS 1.1
 * @version $Id$
 */

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SimpleLockManager implements LockManager {
    Map locks;

    public SimpleLockManager () {
        locks = new HashMap();
    }

    public class SimpleTicket implements Ticket {
        String resourceName;
        long expiration;
        public SimpleTicket (String resourceName, long duration) {
            this.resourceName = resourceName;
            this.expiration = System.currentTimeMillis() + duration;
        }
        public boolean renew (long duration) {
            if (!isExpired()) {
                this.expiration = System.currentTimeMillis() + duration;
                return true;
            }
            return false;
        }
        public long getExpiration() {
            return expiration;
        }
        public boolean isExpired() {
            return System.currentTimeMillis() > expiration;
        }
        public String getResourceName () {
            return resourceName;
        }
        public void cancel() {
            expiration = 0;
            locks.remove (resourceName);
            synchronized (this) {
                notify();
            }
        }
        public String toString() {
            return super.toString() 
                + "[" + resourceName + "/" +isExpired() + "/"
                + (expiration - System.currentTimeMillis()) + "ms left]";
        }
    }
    public Ticket lock (String resourceName, long duration, long wait)
    {
        long maxWait = System.currentTimeMillis() + wait;

        while (System.currentTimeMillis() < maxWait) {
            Ticket t = null;
            synchronized (this) {
                t = (Ticket) locks.get (resourceName);
                if (t == null) {
                    t = new SimpleTicket (resourceName, duration);
                    locks.put (resourceName, t);
                    return t;
                } 
                else if (t.isExpired()) {
                    t.cancel();
                    continue;
                }
            }
            synchronized (t) {
                try {
                    t.wait (Math.min (1000, wait));
                } catch (InterruptedException e) { }
            }
        }
        return null;
    }
}
