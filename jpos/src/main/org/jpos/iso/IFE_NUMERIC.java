package org.jpos.iso;

/**
 * <pre>
 * EBCDIC version of IFA_NUMERIC
 * Right Justify, zero fill (0xf0) fields
 * </pre>
 * @author eoin.flood@orbiscom.com
 * @version $Id$
 * @see IFA_NUMERIC
 * @see ISOFieldPackager
 * @see ISOComponent
 */
public class IFE_NUMERIC extends ISOFieldPackager 
{
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFE_NUMERIC(int len, String description) 
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
        String s = (ISOUtil.zeropad ((String) c.getValue(), getLength()));
        return ISOUtil.asciiToEbcdic(s);
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
        c.setValue(ISOUtil.ebcdicToAscii(b, offset, getLength()));
        return getLength();
    }
    public int getMaxPackedLength() 
    {
        return getLength();
    }
}
