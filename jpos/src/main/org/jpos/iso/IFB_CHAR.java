package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager Binary Char
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFB_CHAR extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFB_CHAR(int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        return (ISOUtil.strpad ((String) c.getValue(), getLength())).getBytes();
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
