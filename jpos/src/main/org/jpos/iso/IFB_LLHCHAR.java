package org.jpos.iso;

/**
 * ISOFieldPackager Binary Hex LLCHAR
 * Almost the same as IFB_LLCHAR but len is encoded as a binary
 * value. A len of 16 is encoded as 0x10 instead of 0x16
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFB_LLHCHAR extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFB_LLHCHAR (int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        int len;
        String s = (String) c.getValue();
    
        if ((len=s.length()) > getLength() || len>99)   // paranoia settings
            throw new ISOException (
                "invalid len "+len +" packing field "+(Integer) c.getKey()
            );

        byte[] b = new byte[len + 1];
        b[0] = (byte) len;
        System.arraycopy(s.getBytes(), 0, b, 1, len);
        return b;
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
        int len = (int) b[offset] & 0xFF;
        c.setValue(new String(b, ++offset, len));
        return ++len;
    }
    public int getMaxPackedLength() {
        return getLength() + 1;
    }
}
