/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.q2.iso;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.jdom.Element;
import org.jpos.q2.Q2;
import org.jpos.q2.QFactory;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.Destroyable;
import org.jpos.util.LogSource;
import org.jpos.util.NameRegistrar;
import org.jpos.core.Configurable;
import org.jpos.iso.ISOUtil;

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
        String s = cfg.get ("start")+":00:00";
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
            Date now = new Date();
            if (now.before (when)) {
                long sleepTime = when.getTime() - now.getTime();
                getLog().info ("sleeping",
                    (sleepTime/1000) + " secs until " + when.toString()
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

