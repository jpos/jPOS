package org.jpos.emv;


import org.jpos.tlv.TLVDataFormat;


/**
 * @author Vishnu Pillai
 */
public class BinaryEMVTag extends EMVTag<byte[]> {

    public BinaryEMVTag(EMVStandardTagType tagType, byte[] value)
            throws IllegalArgumentException, NoTagNumberForProprietaryTagException {
        super(tagType, value);
    }

    public BinaryEMVTag(EMVProprietaryTagType tagType, Integer tagNumber, byte[] value)
            throws IllegalArgumentException {
        super(tagType, tagNumber, value);
    }

    public BinaryEMVTag(EMVProprietaryTagType tagType, Integer tagNumber, TLVDataFormat dataFormat, byte[] value)
            throws IllegalArgumentException {
        super(tagType, tagNumber, dataFormat, value);
    }
}
