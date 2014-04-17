package org.jpos.tlv.packager.bertlv;


import org.jpos.emv.EMVProprietaryTagType;
import org.jpos.emv.EMVStandardTagType;
import org.jpos.emv.EMVTagType;
import org.jpos.tlv.TLVDataFormat;
import org.jpos.emv.UnknownTagNumberException;
import org.jpos.iso.ISOException;

/**
 * @author Vishnu Pillai
 */
public class DefaultICCBERTLVFormatMapper implements BERTLVFormatMapper {

    public static DefaultICCBERTLVFormatMapper INSTANCE = new DefaultICCBERTLVFormatMapper();

    private EMVTagType getTagType(final Integer tagNumber) throws UnknownTagNumberException {
        if (EMVStandardTagType.isProprietaryTag(tagNumber)) {
            return getProprietaryTagType(tagNumber);
        } else {
            return EMVStandardTagType.forCode(tagNumber);
        }
    }

    @Override
    public TLVDataFormat getFormat(Integer tagNumber) throws ISOException {
        EMVTagType tagType;
        try {
            tagType = getTagType(tagNumber);
        } catch (UnknownTagNumberException e) {
            throw new ISOException(e);
        }
        final TLVDataFormat dataFormat = tagType.getFormat();
        return dataFormat;
    }

    /**
     * Subclasses should override this method to provide an implementation of org.jpos.emv.EMVProprietaryTagType
     * @param tagNumber
     * @return EMVProprietaryTagType
     * @throws UnknownTagNumberException
     */
    protected EMVProprietaryTagType getProprietaryTagType(Integer tagNumber) throws UnknownTagNumberException {
        throw new UnknownTagNumberException(Integer.toHexString(tagNumber));
    }

}
