package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len CHAR
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFA_LLCHAR extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_LLCHAR (int len, String description) {
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
                "invalid len "+len +" packing LLCHAR field "+(Integer) c.getKey()
            );

        return (ISOUtil.zeropad(Integer.toString(len), 2) + s).getBytes();
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
        return len + 2;
    }
    public int getMaxPackedLength() {
        return getLength() + 2;
    }
}
