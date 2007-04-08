/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.q2.qbean;

import org.jdom.Element;
import org.jpos.q2.QBeanSupport;
import org.python.util.PythonInterpreter;

/**
 * Jython Interpreter QBean.
 * 
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 *
 */
public class Jython extends QBeanSupport implements Runnable {
    PythonInterpreter jython;
    public void initService() {
        jython = new PythonInterpreter();
    }
    
    public void startService() {
        new Thread(this).start();
    }
    
    public void run() {
        Element config = getPersist();
        jython.exec(config.getText());
    }
}
