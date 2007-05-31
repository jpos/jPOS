/*
 * Copyright (c) 2007 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len BINARY
 *
 * @author salaman@teknos.com
 * @version Id: IFA_LLLBINARY.java,v 1.0 1999/05/15 01:05 salaman Exp 
 * @see ISOComponent
 */
public class IFA_LLLLBINARY extends ISOBinaryFieldPackager {
    public IFA_LLLLBINARY() {
        super(LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LLLL);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_LLLLBINARY (int len, String description) {
        super(len, description, LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LLLL);
        checkLength(len, 9999);
    }

    public void setLength(int len)
    {
        checkLength(len, 9999);
        super.setLength(len);
    }
}

