package org.jpos.iso;

/**
 * ISOFieldPackager CHARACTERS (ASCII & BINARY)
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IF_CHAR extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IF_CHAR(int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        String s = (String) c.getValue();
        if (s.length() > getLength()) 
            s = s.substring(0, getLength());
        return (ISOUtil.strpad (s, getLength())).getBytes();
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
        c.setValue(new String(b, offset, getLength()));
        return getLength();
    }
    public int getMaxPackedLength() {
        return getLength();
    }
}
