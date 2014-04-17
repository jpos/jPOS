package org.jpos.tlv.packager.bertlv;


import org.jpos.tlv.TLVDataFormat;
import org.jpos.iso.ISOException;


/**
 * @author Vishnu Pillai
 */
public interface BERTLVFormatMapper {

    public TLVDataFormat getFormat(Integer tagNumber) throws ISOException;

}
