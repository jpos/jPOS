package org.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len BINARY
 *
 * @author apr@cs.com.uy
 * @version Id: $
 * @see ISOComponent
 */
public class IFA_LLBINARY extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_LLBINARY (int len, String description) {
        super(len, description);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
        int len;
        byte[] b = (byte[]) c.getValue();
    
        if ( (len=b.length) > getLength() || len>99)
            throw new ISOException (
                "invalid len "+len 
                +" packing LLLCHAR field "+(Integer) c.getKey()
            );
        byte[] nb=new byte[len+2];
        nb=ISOUtil.strpad(ISOUtil.zeropad(Integer.toString(len), 2),len+2).getBytes();
        System.arraycopy(b, 0, nb, 2, len);
        return (nb);
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
        byte[] value = new byte[len];
        System.arraycopy(b, offset+2, value, 0, len);
        c.setValue ((Object) value);
        return len + 2;
    }
    public ISOComponent createComponent(int fieldNumber) {
        return new ISOBinaryField (fieldNumber);
    }
    public int getMaxPackedLength() {
        return getLength() + 2;
    }
}
