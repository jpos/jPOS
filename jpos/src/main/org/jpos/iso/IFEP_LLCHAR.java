package org.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len CHAR 
 * suitable for EuroPay subfield 48<br>
 * <pre>
 * Format LLTT....
 * Where LL is the 2 digit field length
 *       TT is the 2 digit field number (Tag)
 *       is the field content   
 * </pre>
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 */
public class IFEP_LLCHAR extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFEP_LLCHAR (int len, String description) {
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
                "invalid len "+len +" packing LLEPCHAR field "+(Integer) c.getKey()
            );

        return (
	    ISOUtil.zeropad(Integer.toString(len), 2) 
	   +ISOUtil.zeropad(((Integer) c.getKey()).toString(), 2) 
	   +s
	).getBytes();
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
	if (!(c instanceof ISOField))
	    throw new ISOException 
		(c.getClass().getName() + " is not an ISOField");

	((ISOField)c).setFieldNumber (
	    Integer.parseInt(new String(b, offset+2, 2))
	);
        c.setValue (new String (b, offset+4, len-2));
        return len + 2;
    }
    public int getMaxPackedLength() {
        return getLength() + 2;
    }
}
