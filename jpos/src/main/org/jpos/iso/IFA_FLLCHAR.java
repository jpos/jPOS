package org.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len padded (fixed) CHAR
 * (suitable to use in ANSI X9.2 interchanges.
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see IFA_LLCHAR
 */
public class IFA_FLLCHAR extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_FLLCHAR (int len, String description) {
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
                "invalid len "+len +" packing FLLCHAR field "+(Integer) c.getKey()
            );

        s = ISOUtil.strpad(s, getLength());
        return (ISOUtil.zeropad(Integer.toString(len), 2) + s).getBytes();
    }
    public int getMaxPackedLength() {
        return getLength() + 2;
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
        int len = Integer.parseInt(new String(b, offset, 2));
        c.setValue (new String (b, offset+2, len));
        return getLength() + 2;
    }
}
