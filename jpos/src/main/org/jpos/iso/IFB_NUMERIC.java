package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager Binary Numeric
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFB_NUMERIC extends ISOFieldPackager {
    private boolean pad;
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFB_NUMERIC(int len, String description, boolean pad) {
        super(len, description);
        this.pad = pad;
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        String s = ISOUtil.zeropad ((String) c.getValue(), getLength());
        return ISOUtil.str2bcd (s, pad);
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
        c.setValue (ISOUtil.bcd2str (b, offset, getLength(), pad));
        return ((getLength()+1) >> 1);
    }
    public int getMaxPackedLength() {
        return (getLength()+1) >> 1;
    }
}
