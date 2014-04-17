package org.jpos.tlv.packager.bertlv;


import org.jpos.iso.ISOException;


/**
 * Packager for ICC Tags in BER TLV format
 *
 * @author Vishnu Pillai
 */

public abstract class DefaultICCBERTLVPackager extends BERTLVPackager {

    private static BERTLVFormatMapper DEFAULT_TAG_FORMAT_MAPPER = DefaultICCBERTLVFormatMapper.INSTANCE;

    /**
     * Use this method to globally set the BERTLVFormatMapper
     *
     * @param tagFormatMapper
     */
    public static void setTagFormatMapper(BERTLVFormatMapper tagFormatMapper) {
        DefaultICCBERTLVPackager.DEFAULT_TAG_FORMAT_MAPPER = tagFormatMapper;
    }

    @Override
    protected BERTLVFormatMapper getTagFormatMapper() {
        return DefaultICCBERTLVPackager.DEFAULT_TAG_FORMAT_MAPPER;
    }

    public DefaultICCBERTLVPackager() throws ISOException {
        super();
    }

}
