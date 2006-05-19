/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.transaction;

import org.jpos.q2.QBeanSupportMBean;

public interface TransactionManagerMBean extends QBeanSupportMBean {
    public long getHead ();
    public long getTail ();
}

