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
public class QTest extends QBeanSupport implements Runnable {
    Log log;
    public QTest () {
        super();
        log = Log.getLog (Q2.LOGGER_NAME, "QTest");
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
        log.info ("getPersist");
        return super.getPersist ();
    }
    public void run () {
        int tickCount = 0;
        while (running ()) {
            log.info ("tick " + tickCount);
            if (tickCount++ % 10 == 0) {
                super.getPersist ().addContent (
                    new Comment (" tickCount = " + (tickCount-1) + " ")
                );
                setModified (true);
            }
            ISOUtil.sleep (1000);
        }
        setState (QBean.STOPPED);
    }
    public void startService() {
        new Thread(this).start();
    }
    public void setName (String name) {
        log.setRealm ("QTest:" + name);
        super.setName (name);
    }
}

