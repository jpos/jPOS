package org.jpos.iso;

/**
 * <pre>
 * EBCDIC version of IF_LLCHAR
 * Uses a 2 EBCDIC byte length field
 * </pre>
 * @author eoin.flood@orbiscom.com
 * @version $Id$
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFE_LLCHAR extends ISOFieldPackager 
{
    /**
    * @param len - field len
    * @param description symbolic descrption
    */
    public IFE_LLCHAR(int len, String description) 
    {
	super(len, description);
    }
    /**
    * @param c - a component
    * @return packed component
    * @exception ISOException
    */
    public byte[] pack (ISOComponent c) throws ISOException 
    {
    	int len;
	String s = (String) c.getValue();
	if ((len=s.length()) > getLength() || len>99)   // paranoia settings
            throw new ISOException (
                "invalid len "+len +" packing LLECHAR field "+(Integer) c.getKey());

	String l = ISOUtil.zeropad (Integer.toString(len), 2);

	return ISOUtil.asciiToEbcdic(l + s);
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
    	int len = ((b[offset] & 0x0f) * 10)  + (b[offset+1] & 0x0f);
	c.setValue(ISOUtil.ebcdicToAscii(b, offset+2, len));
	return len+2;
    }

    public int getMaxPackedLength() 
    {
	return getLength()+2;
    }
}
