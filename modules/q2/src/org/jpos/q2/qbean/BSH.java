/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.q2.qbean;

import org.jdom.Element;
import org.jpos.q2.QBeanSupport;

import bsh.BshClassManager;
import bsh.Interpreter;
import bsh.UtilEvalError;

public class BSH extends QBeanSupport implements Runnable {
    protected Interpreter bsh;
    public void initService() {
        bsh = new Interpreter ();
        BshClassManager bcm = bsh.getClassManager();
        try {
            bcm.setClassPath(getServer().getLoader().getURLs());
        } catch (UtilEvalError e) {
            e.printStackTrace();
        }
        bcm.setClassLoader(getServer().getLoader());
    }
    public void startService() {
        new Thread (this, "BSH-" + getName()).start ();
    }
    public void run () {
        Element config = getPersist();
        try {
            bsh.set  ("qbean", this);
            bsh.set  ("log", getLog());
            bsh.eval (config.getText());
            String source = config.getAttributeValue ("source");
            if (source != null)
                bsh.source (source);
        } catch (Throwable e) {
            getLog().warn (e);
        }
    }
}

