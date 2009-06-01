/**
 * 
 */
package org.jpos.tlv;

/**
 * @author Mark
 * 
 * A poor cousin of a real TLVMsg where the length of the length is fixed.
 *
 */
public class FixedLengthTLVMsg extends TLVMsg {
    
    private int lengthOfLength = 1;
    
    public FixedLengthTLVMsg(int tag, byte[] arrValue, int lengthOfLength) {
        super(tag,arrValue);
        setLengthOfLength(lengthOfLength);
    }
    
    public FixedLengthTLVMsg(int tag, byte[] arrValue) {
        super(tag,arrValue);
        setLengthOfLength(1);
    }


    public void setLengthOfLength(int lengthOfLength) {
        this.lengthOfLength = lengthOfLength;
    }
    
    
    /**
     * @return encoded length
     */
    public byte[] getL() {

        if (value == null)
            return new byte[lengthOfLength];
        int ix = 0;
        int tmp = value.length;
        int bytes = 0;

        /* Value to be encoded on multiple bytes as dictated by lengthOfLength */

        byte[] rBytes = new byte[lengthOfLength];
 
        tmp = value.length;
        while (ix < lengthOfLength) {
            /* Mask off 8 bits of the value at a time */
            rBytes[(bytes - ix)] = (byte) (tmp & 0xFF);
            bytes--;
            /* Shift value right 8 bits, effectively removing them */
            tmp = (tmp >> 8);
        }
        //}
        return rBytes;
    }

}
