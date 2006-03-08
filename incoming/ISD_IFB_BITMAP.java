package msgsentry.msj.auth.isocommon;

import java.util.BitSet;

import org.jpos.iso.IFB_BITMAP;
import org.jpos.iso.ISOBitMapPackager;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;

/**
 * ISOFieldPackager Binary Bitmap
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOComponent
 * @see ISOBitMapPackager
 */
public class ISD_IFB_BITMAP extends IFB_BITMAP {
    public ISD_IFB_BITMAP() {
        super();
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public ISD_IFB_BITMAP(int len, String description) {
        super(len, description);
    }

    /**
     * @param c - a component
     * @return packed component
     * @exception ISOException
     */
    public byte[] pack (ISOComponent c) throws ISOException {
    	return bitSet2byte ((BitSet) c.getValue());
    }
    
    /**
     * converts a BitSet into a binary field
     * used in pack routines
     * @param b - the BitSet
     * @param fieldLength - the length of the BITMAP
     * @return binary representation
     */
    public byte[] bitSet2byte (BitSet b)
    {
    	int fieldLength =  getLength();
        int len = (b.length() > 65) ? 128 : 64;
        //Addition for BITMAPs lesser than 8 bytes
        if(fieldLength < 8 )
	       	len = fieldLength * 8;
        byte[] d = new byte[len >> 3];
        for (int i=0; i<len; i++) 
            if (b.get(i+1)) 
                d[i >> 3] |= (0x80 >> (i % 8));
        if (len>64)
            d[0] |= 0x80;
        return d;
    }
    
    /**
     * @param c - the Component to unpack
     * @param b - binary image
     * @param offset - starting offset within the binary image
     * @return consumed bytes
     * @exception ISOException
     */
    /*public int unpack (ISOComponent c, byte[] b, int offset)
        throws ISOException
    {
        //int len;
        BitSet bmap = ISOUtil.byte2BitSet (b, offset, getLength() > 8);
        c.setValue(bmap);
        //len = (bmap.get(1) == true) ? 128 : 64;
        return (getLength());
    }*/
    
    /**
     * Converts a binary representation of a Bitmap field
     * into a Java BitSet
     * @param b - binary representation
     * @param offset - staring offset
     * @param bitZeroMeansExtended - true for ISO-8583
     * @return java BitSet object
     */
    public BitSet byte2BitSet 
        (byte[] b, int offset, boolean bitZeroMeansExtended)
    {
    	int fieldLength = getLength();
    	
    	//for (int i=0; i<b.length; i++)
    	//  System.out.print(" " + b[i]);
    	
        int len = bitZeroMeansExtended ?
            ((b[offset] & 0x80) == 0x80 ? 128 : 64) : 64;

        if(fieldLength < 8 )
    	  	len = fieldLength * 8;
        
        BitSet bmap = new BitSet (len);
        for (int i=0; i<len; i++) 
            if (((b[offset + (i >> 3)]) & (0x80 >> (i % 8))) > 0)
                bmap.set(i+1);
        
        return bmap;
    }
    
    public int unpack (ISOComponent c, byte[] b, int offset)
    throws ISOException
    {
    	int len;
    	BitSet bmap = byte2BitSet (b, offset, getLength() > 8);
    	c.setValue(bmap);
    	len = (bmap.get(1) == true) ? 128 : 64; /* changed by Hani */
    	return (Math.min (getLength(), len >> 3));
    }
}

