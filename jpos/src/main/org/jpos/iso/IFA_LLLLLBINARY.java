

package org.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len BINARY
 * @author Alejandro
 * @version $Id$
 * @see ISOComponent
 */
public class IFA_LLLLLBINARY extends ISOBinaryFieldPackager {
    public IFA_LLLLLBINARY () {
        super(LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LLLLL);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public  IFA_LLLLLBINARY (int len, String description) {
        super(len, description, LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LLLLL);
        checkLength(len, 99999);
    }

    public void setLength(int len)
    {
        checkLength(len, 99999);
        super.setLength(len);
    }
}
