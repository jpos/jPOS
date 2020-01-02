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

package org.jpos.q2.iso;

import org.jdom2.Element;
import org.jpos.core.Configurable;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * DailyTask Adaptor
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
public class DailyTaskAdaptor extends QBeanSupport implements Runnable {
    Runnable task;
    Thread thisThread = null;

    public DailyTaskAdaptor () {
        super ();
    }

    protected void initService () throws Exception {
        QFactory factory = getServer().getFactory();
        Element e = getPersist ();
        task = (Runnable) factory.newInstance (e.getChildTextTrim ("class"));
        factory.setLogger (task, e);
    }
    protected void startService () throws Exception {
        if (task instanceof Configurable) {
            Element e = getPersist ();
            QFactory factory = getServer().getFactory();
            ((Configurable)task).setConfiguration (
                factory.getConfiguration (e)
            );
        }
        (thisThread = new Thread(this)).start();
    }
    protected void stopService () throws Exception {
        if (thisThread != null)
            thisThread.interrupt();
    }
    public void run () {
        while (running()) {
            waitUntilStartTime();
            if (running()) {
                Thread taskThread = new Thread(task);
                taskThread.setDaemon (true);
                taskThread.start();
                ISOUtil.sleep (1000);
            }
        }
    }
    public Date getWhen() {
        String s = cfg.get ("start")+":00:00"; // NOPMD
        int hh = Integer.parseInt(s.substring (0, 2));
        int mm = Integer.parseInt(s.substring (3, 5));
        int ss = Integer.parseInt(s.substring (6, 8));

        Date now = new Date();
        Calendar cal = new GregorianCalendar();

        cal.setTime (now);
        cal.set (Calendar.HOUR_OF_DAY, hh);
        cal.set (Calendar.MINUTE, mm);
        cal.set (Calendar.SECOND, ss);

        Date when = cal.getTime();
        if (when.before(now)) 
            when = new Date(when.getTime() + 24*60*60*1000);

        return when;
    }
    protected void waitUntilStartTime() {
        Date when = getWhen();
        while (running()) {
            Date now = new GregorianCalendar().getTime();
            if (now.before (when)) {
                long sleepTime = when.getTime() - now.getTime();
                if (sleepTime <= 0) {
                    ISOUtil.sleep(1000);
                    continue;
                }
                getLog().info ("sleeping",
                    sleepTime/1000 + " secs until " + when.toString()
                );
                try {
                    Thread.sleep (sleepTime);
                } catch (InterruptedException e) { 
                    when = getWhen();
                }
            } else
                break;
        }
    }
}

