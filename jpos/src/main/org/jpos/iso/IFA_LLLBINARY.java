package uy.com.cs.jpos.iso;

/**
 * ISOFieldPackager ASCII variable len BINARY
 *
 * @author salaman@teknos.com
 * @version Id: IFA_LLLBINARY.java,v 1.0 1999/05/15 01:05 salaman Exp 
 * @see ISOComponent
 */
public class IFA_LLLBINARY extends ISOFieldPackager {
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFA_LLLBINARY (int len, String description) {
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
    
        if ( (len=b.length) > getLength() || len>999)
            throw new ISOException (
                "invalid len "+len 
                +" packing LLLCHAR field "+(Integer) c.getKey()
            );
        byte[] nb=new byte[len+3];
        nb=ISOUtil.strpad(ISOUtil.zeropad(Integer.toString(len), 3),len+3).getBytes();
        System.arraycopy(b, 0, nb, 3, len);
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
        int len = Integer.parseInt(new String(b, offset, 3));       
        byte[] value = new byte[len];
        System.arraycopy(b, offset+3, value, 0, len);
        c.setValue ((Object) value);
        return len + 3;
    }
    public ISOComponent createComponent(int fieldNumber) {
        return new ISOBinaryField (fieldNumber);
    }
    public int getMaxPackedLength() {
        return getLength() + 3;
    }
}
