/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.q2.qbean;

import java.util.Iterator;

import org.jdom.Element;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISOUtilCrypto;
import org.jpos.q2.QBeanSupport;

import bsh.BshClassManager;
import bsh.Interpreter;
import bsh.UtilEvalError;

public class BSH extends QBeanSupport implements Runnable {
    Interpreter bsh;
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
        new Thread (this).start ();
    }
    public void run () {
        Element config = getPersist();
        try {
            bsh.set  ("qbean", this);
            bsh.set  ("log", getLog());
            Iterator i = config.getChildren("property").iterator();
            while (i.hasNext()) {
            	Element e = (Element)i.next();
            	bsh.set(e.getAttributeValue("name"),e.getAttributeValue("value"));
            }
            String code = config.getText();
            String source = config.getAttributeValue ("source");
            if (!code.trim().equals("")) {
        		bsh.eval(code);
        	}
            else {
            	if (source != null) {
                    bsh.source (source);
                }
            	else {
            		code=config.getChildText("source");
            		bsh.eval(code);
            	}
            	
            }
        } catch (Throwable e) {
            getLog().warn (e);
        }
    }
}

