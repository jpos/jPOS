/*
 * Copyright (c) 2007 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.iso;

/**
 * Esoteric version of IFA_LLCHAR where payload is in ASCII but length in EBCDIC
 * @author apr@jpos.org
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFEA_LLCHAR extends ISOStringFieldPackager {
    public IFEA_LLCHAR() {
        super(NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, EbcdicPrefixer.LL);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFEA_LLCHAR(int len, String description) {
        super(len, description, NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, EbcdicPrefixer.LL);
        checkLength(len, 99);
    }

    public void setLength(int len)
    {
        checkLength(len, 99);
        super.setLength(len);
    }
}

