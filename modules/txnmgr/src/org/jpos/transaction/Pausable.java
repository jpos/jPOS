/*
 * Copyright (c) 2007 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.transaction;

public interface Pausable {
    public void setPausedTransaction (PausedTransaction p);
    public PausedTransaction getPausedTransaction();
}

