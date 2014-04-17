package org.jpos.tlv;


/**
 * @author Vishnu Pillai
 */
public enum TLVDataFormat {
    CONSTRUCTED,
    BINARY,
    COMPRESSED_NUMERIC,
    NUMERIC,
    DATE_YYMMDD,
    ALPHA,
    ALPHA_NUMERIC,
    ALPHA_NUMERIC_SPACE,
    ALPHA_NUMERIC_SPECIAL,
    CARD_NUMBER,
    TIME_HHMMSS,
    PROPRIETARY;
}
