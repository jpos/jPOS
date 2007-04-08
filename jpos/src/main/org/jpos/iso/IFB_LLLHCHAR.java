/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

/**
 * ISOFieldPackager Binary Hex LLLCHAR
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFB_LLLHCHAR extends ISOStringFieldPackager {
    public IFB_LLLHCHAR() {
        super(NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, BinaryPrefixer.BB);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFB_LLLHCHAR(int len, String description) {
        super(len, description, NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, BinaryPrefixer.BB);
        checkLength(len, 999);
    }

    public void setLength(int len)
    {
        checkLength(len, 999);
        super.setLength(len);
    }
}

