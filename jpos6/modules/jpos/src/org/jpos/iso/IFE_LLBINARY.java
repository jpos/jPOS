/*
 * Copyright (c) 2007 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.iso;

/**
 * BINARY version of IFE_LLLCHAR
 * Uses a 3 EBCDIC byte length field, and the binary data is stored as is.
 * 
 * @author Alejandro Revila
 * @author Jonathan O'Connor
 * @version $Id: IFE_LLLBINARY.java 1830 2003-11-18 01:18:48Z ninki $
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFE_LLBINARY extends ISOBinaryFieldPackager 
{
    public IFE_LLBINARY()
    {
        super(LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LL);
    }
    /**
    * @param len - field len
    * @param description symbolic descrption
    */
    public IFE_LLBINARY(int len, String description) 
    {
        super(len, description, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LL);
        checkLength(len, 99);
    }

    public void setLength(int len)
    {
        checkLength(len, 99);
        super.setLength(len);
    }
}

