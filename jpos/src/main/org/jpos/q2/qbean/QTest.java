package org.jpos.q2.qbean;

import org.jdom.Element;
import org.jdom.Comment;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.jpos.q2.QBean;
import org.jpos.q2.QBeanSupport;

import org.jpos.util.Log;
import org.jpos.util.Logger;

/**
 * Sample QBean
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @version $Revision$ $Date$
 */
public class QTest extends QBeanSupport implements Runnable, QTestMBean {
    long tickInterval = 1000;

    public QTest () {
        super();
        log.info ("constructor");
    }
    public void init () {
        log.info ("init");
        super.init ();
    }
    public void start() {
        log.info ("start");
        super.start ();
    }
    public void stop () {
        log.info ("stop");
        super.stop ();
    }
    public void destroy () {
        log.info ("destroy");
        log = null;
    }
    public void setPersist (Element e) {
        log.info ("setPersist");
        super.setPersist (e);
    }
    public Element getPersist () {
        setModified (false);
        log.info ("getPersist");
        return createElement ("qtest", QTestMBean.class);
    }
    public void setName (String name) {
        log.info ("setName " + name);
        super.setName (name);
    }
    public void setTickInterval (long tickInterval) {
        this.tickInterval = tickInterval;
        setModified (true);
    }
    public long getTickInterval () {
        return tickInterval;
    }
    public void run () {
        for (int tickCount=0; running (); tickCount++) {
            log.info ("tick " + tickCount);
            ISOUtil.sleep (tickInterval);
        }
        setState (QBean.STOPPED);
    }
    public void startService() {
        new Thread(this).start();
    }
}

