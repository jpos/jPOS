/*
 * Copyright (c) 2007 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

public interface ISOResponseListener {
    void responseReceived (ISOMsg resp, Object handBack);
    void expired (Object handBack);
}

