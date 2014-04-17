package org.jpos.tlv;

/**
 * @author Vishnu Pillai
 */
public class BinaryTagValue extends TagValueBase<byte[]> {

    public BinaryTagValue(String tag, byte[] value) {
        super(tag, value);
    }

}
