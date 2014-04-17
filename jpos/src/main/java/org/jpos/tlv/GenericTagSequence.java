package org.jpos.tlv;

import org.jpos.iso.ISOException;

/**
 * @author Vishnu Pillai
 */
public class GenericTagSequence extends TagSequenceBase {

    public GenericTagSequence() {
        super();
    }

    protected GenericTagSequence(String tag) {
        super(tag);
    }


    protected TagSequence createTagValueSequence(String tag) {
        return new GenericTagSequence(tag);
    }

    protected TagValue createLiteralTagValuePair(String tag, String value) throws ISOException {
        return new LiteralTagValue(tag, value);
    }

    protected TagValue createBinaryTagValuePair(String tag, byte[] value) throws ISOException {
        return new BinaryTagValue(tag, value);
    }

}
