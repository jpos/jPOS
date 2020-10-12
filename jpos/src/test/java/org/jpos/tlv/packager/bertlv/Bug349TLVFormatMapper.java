package org.jpos.tlv.packager.bertlv;

import org.jpos.emv.EMVTagType;
import org.jpos.emv.UnknownTagNumberException;
import org.jpos.iso.ISOException;
import org.jpos.tlv.TLVDataFormat;

public class Bug349TLVFormatMapper extends DefaultICCBERTLVFormatMapper {
    public static Bug349TLVFormatMapper INSTANCE = new Bug349TLVFormatMapper();

    private Bug349TLVFormatMapper() {
        super();
    }

    private EMVTagType getTagType(final Integer tagNumber) throws UnknownTagNumberException {
        if (Bug349TagType.isProprietaryTag(tagNumber)) {
            return getProprietaryTagType(tagNumber);
        } else {
            return Bug349TagType.forCode(tagNumber);
        }
    }

    @Override
    public TLVDataFormat getFormat(Integer tagNumber) throws ISOException {

        try {
            return super.getFormat(tagNumber);
        } catch (ISOException e) {
            EMVTagType tagType;
            try {
                tagType = getTagType(tagNumber);
            } catch (UnknownTagNumberException e1) {
                throw new ISOException(e1);
            }
            return tagType.getFormat();
        }
    }
}
