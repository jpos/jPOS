/**
 * 
 */
package org.jpos.tlv;

import java.nio.ByteBuffer;

/**
 * @author Mark
 * 
 * A poor cousin of a real TLVList where the length of the length is fixed.
 *
 */
public class FixedLengthTLVList extends TLVList {
    
    int lengthOfLength;
    
    /**
     * empty constructor
     */
    public FixedLengthTLVList() {
        setLengthLength(1);
    }
    
    public FixedLengthTLVList(int lengthOfLength) {
        setLengthLength(lengthOfLength);
    }
    
    public void setLengthLength(int lengthOfLength) {
        this.lengthOfLength = lengthOfLength;
    }
     
    /*
     * FixedLength TLVList must use FixedLengthTLVMsg for pack!
     */
    protected TLVMsg getTLVMsg(int tag, byte[] arrValue) {
        return new FixedLengthTLVMsg(tag,arrValue, lengthOfLength);
    }
     
    /*
     * Read length bytes and return the int value
     * @return value length
     */
    protected int getValueLength(ByteBuffer buffer) {
        int length;
        byte b;
        b = buffer.get();

        int count = lengthOfLength - 1;
        for (length = 0; count > 0; count--) {
            length <<= 8;
            b = buffer.get();
            length |= b & 0xFF;
        }

        return length;
    }

}
