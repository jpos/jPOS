package org.jpos.emv;


import org.jpos.tlv.TLVDataFormat;


/**
 * @author Vishnu Pillai
 */
public class LiteralEMVTag extends EMVTag<String> {

    public LiteralEMVTag(EMVStandardTagType tagType, String value)
            throws IllegalArgumentException {
        super(tagType, value);
    }

    public LiteralEMVTag(EMVProprietaryTagType tagType, Integer tagNumber, String value)
            throws IllegalArgumentException {
        super(tagType, tagNumber, value);
    }

    public LiteralEMVTag(EMVProprietaryTagType tagType, Integer tagNumber, TLVDataFormat dataFormat, String value)
            throws IllegalArgumentException {
        super(tagType, tagNumber, dataFormat, value);
    }
}
