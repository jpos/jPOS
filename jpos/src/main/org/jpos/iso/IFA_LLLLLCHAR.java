package org.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len CHAR
 *
 * @author Victor A. Salaman <salaman@teknos.com>
 * @version $Id$
 * @see ISOComponent
 */
public class IFA_LLLLLCHAR extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_LLLLLCHAR (int len, String description) {
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
    
        if ((len=s.length()) > getLength() || len>99999)   // paranoia settings
            throw new ISOException (
                "invalid len "+len +" packing LLLLLCHAR field "+(Integer) c.getKey()
            );

        return (ISOUtil.zeropad(Integer.toString(len), 5) + s).getBytes();
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
        int len = Integer.parseInt(new String(b, offset, 5));
        c.setValue (new String (b, offset+5, len));
        return len + 5;
    }
    public int getMaxPackedLength() {
        return getLength() + 5;
    }
}
