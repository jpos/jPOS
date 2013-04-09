package org.jpos.iso;

/**
 *
 * @author edwin < edwinkun at gmail dot com >
 */

public class IFE_LLLLCHAR extends ISOStringFieldPackager 
{
    public IFE_LLLLCHAR() {
        super(NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LLLL);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFE_LLLLCHAR(int len, String description) {
        super(len, description, NullPadder.INSTANCE, EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LLLL);
        checkLength(len, 9999);
    }

    public void setLength(int len)
    {
        checkLength(len, 9999);
        super.setLength(len);
    }
}

