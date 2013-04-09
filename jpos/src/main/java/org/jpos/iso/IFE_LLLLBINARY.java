package org.jpos.iso;

/**
 *
 * @author edwin < edwinkun at gmail dot com >
 */
public class IFE_LLLLBINARY extends ISOBinaryFieldPackager 
{
    public IFE_LLLLBINARY()
    {
        super(LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLLL);
    }
    /**
    * @param len - field len
    * @param description symbolic descrption
    */
    public IFE_LLLLBINARY(int len, String description) 
    {
        super(len, description, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLLL);
        checkLength(len, 9999);
    }

    public void setLength(int len)
    {
        checkLength(len, 9999);
        super.setLength(len);
    }
}

