package org.jpos.q2.qbean;

import org.jdom.Element;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBean;
import org.jpos.q2.QBeanSupport;

public class QTest extends QBeanSupport implements Runnable {
    public QTest () {
        super();
        System.out.println ("QTest: constructor");
    }
    public void init () {
        System.out.println ("QTest: init");
        super.init ();
    }
    public void start() {
        System.out.println ("QTest: start");
        super.start ();
    }
    public void stop () {
        System.out.println ("QTest: stop");
        super.stop ();
    }
    public void destroy () {
        System.out.println ("QTest: destroy");
    }
    public void setPersist (Element e) {
        System.out.println ("QTest: setPersist");
        super.setPersist (e);
    }
    public Element getPersist () {
        System.out.println ("QTest: getPersist");
        return super.getPersist ();
    }
    public void run () {
        while (getState() == QBean.STARTED) {
            System.out.println ("QTest: tick " + this.hashCode());
            ISOUtil.sleep (1000);
        }
        setState (QBean.STOPPED);
    }
}

