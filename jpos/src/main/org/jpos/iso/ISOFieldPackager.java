package uy.com.cs.jpos.iso;

/**
 * base class for the various IF*.java Field Packagers
 * Implements "FlyWeight" pattern
 *
 * @author apr@cs.com.uy
 * @version $Id$
 *
 * @see IFA_AMOUNT
 * @see IFA_BINARY
 * @see IFA_BITMAP
 * @see IFA_FLLCHAR
 * @see IFA_FLLNUM
 * @see IFA_LLCHAR
 * @see IFA_LLLBINARY
 * @see IFA_LLLCHAR
 * @see IFA_LLLNUM
 * @see IFA_LLNUM
 * @see IFA_NUMERIC
 * @see IFB_AMOUNT
 * @see IFB_BINARY
 * @see IFB_BITMAP
 * @see IFB_CHAR
 * @see IFB_LLBINARY
 * @see IFB_LLCHAR
 * @see IFB_LLHBINARY
 * @see IFB_LLHCHAR
 * @see IFB_LLHECHAR
 * @see IFB_LLHNUM
 * @see IFB_LLLBINARY
 * @see IFB_LLLCHAR
 * @see IFB_LLLNUM
 * @see IFB_LLNUM
 * @see IFB_NUMERIC
 * @see IF_CHAR
 * @see IF_ECHAR
 */
public abstract class ISOFieldPackager {
    private int len;
    private String description;
    /**
     * @param len - field Len
     * @param description - details
     */
    public ISOFieldPackager(int len, String description) {
        this.len = len;
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
    /**
     * @deprecated Use getLength() instead
     */
    public int getLen() {
        return getLength();
    }
    public int getLength() {
        return len;
    }
    public abstract int getMaxPackedLength();

    public ISOComponent createComponent(int fieldNumber) {
        return new ISOField (fieldNumber);
    }
    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public abstract byte[] pack (ISOComponent c) throws ISOException;

    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    public abstract int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException;
}
