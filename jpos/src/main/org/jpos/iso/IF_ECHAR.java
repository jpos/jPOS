package org.jpos.iso;

/**
 * ISOFieldPackager CHARACTERS (ASCII & BINARY)
 * EBCDIC version of IF_CHAR
 * @author apr@cs.com.uy
 * @version $Id$
 * @see IF_CHAR
 * @see ISOComponent
 */
public class IF_ECHAR extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IF_ECHAR(int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        String s = (ISOUtil.strpad ((String) c.getValue(), getLength()));
        return ISOUtil.asciiToEbcdic(s);
    }
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    public int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException
    {
        c.setValue(ISOUtil.ebcdicToAscii(b, offset, getLength()));
        return getLength();
    }
    public int getMaxPackedLength() {
        return getLength();
    }
}
