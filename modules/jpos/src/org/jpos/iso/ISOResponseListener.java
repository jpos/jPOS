/*
 * Copyright (c) 2007 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

public interface ISOResponseListener {
    void responseReceived(ISOMsg req, ISOMsg resp);
    void expired(ISOMsg req);
}

