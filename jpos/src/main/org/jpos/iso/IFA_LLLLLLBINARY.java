

package org.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len BINARY
 * @author Alejandro
 * @version $Id$
 * @see ISOComponent
 */
public class IFA_LLLLLLBINARY extends ISOBinaryFieldPackager {
    public IFA_LLLLLLBINARY () {
        super(LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LLLLLL);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public  IFA_LLLLLLBINARY (int len, String description) {
        super(len, description, LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LLLLLL);
        checkLength(len, 999999);
    }

    public void setLength(int len)
    {
        checkLength(len, 999999);
        super.setLength(len);
    }
}

