package org.jpos.iso;

/**
 * <pre>
 * EBCDIC version of IF_LLLCHAR
 * Uses a 3 EBCDIC byte length field
 * </pre>
 * @author eoin.flood@orbiscom.com
 * @version $Id$
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFE_LLLCHAR extends ISOFieldPackager 
{
    /**
    * @param len - field len
    * @param description symbolic descrption
    */
    public IFE_LLLCHAR(int len, String description) 
    {
	super(len, description);
    }
    /**
    * @param c - a component
    * @return packed component
    * @exception ISOException
    */
    public byte[] pack(ISOComponent c) throws ISOException 
    {
	int len;
	String s = (String) c.getValue();
	if ((len=s.length()) > getLength() || len>999)   // paranoia settings
            throw new ISOException (
                "invalid len "+len +" packing LLLECHAR field "+(Integer) c.getKey());

	String l = ISOUtil.zeropad(Integer.toString(len), 3);

	return ISOUtil.asciiToEbcdic(l + s);
    }
    /**
    * @param c - the Component to unpack
    * @param b - binary image
    * @param offset - starting offset within the binary image
    * @return consumed bytes
    * @exception ISOException
    */
    public int unpack(ISOComponent c, byte[] b, int offset)
	throws ISOException
    {
    	int len = ((b[offset] & 0x0f) * 100)  + 
		  ((b[offset+1] & 0x0f) * 10) +
		   (b[offset+2] & 0x0f);
	c.setValue(ISOUtil.ebcdicToAscii(b, offset+3, len));
	return len+3;
    }
    public int getMaxPackedLength() 
    {
	return getLength()+3;
    }
}
