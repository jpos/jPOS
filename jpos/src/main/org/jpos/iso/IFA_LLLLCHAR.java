package org.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len CHAR
 *
 * @author Victor A. Salaman <salaman@teknos.com>
 * @version $Id$
 * @see ISOComponent
 */
public class IFA_LLLLCHAR extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_LLLLCHAR (int len, String description) {
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
    
        if ((len=s.length()) > getLength() || len>9999)   // paranoia settings
            throw new ISOException (
                "invalid len "+len +" packing LLLLCHAR field "+(Integer) c.getKey()
            );

        return (ISOUtil.zeropad(Integer.toString(len), 4) + s).getBytes();
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
        int len = Integer.parseInt(new String(b, offset, 4));
        c.setValue (new String (b, offset+4, len));
        return len + 4;
    }
    public int getMaxPackedLength() {
        return getLength() + 4;
    }
}
