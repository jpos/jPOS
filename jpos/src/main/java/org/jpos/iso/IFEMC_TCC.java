package org.jpos.iso;

/**
 *  EBCDIC version of IFMC_TCC
 */
public class IFEMC_TCC extends IFE_CHAR{

    @Override
    public byte[] pack(ISOComponent c) throws ISOException {
        if (c != null) return super.pack(c);
        else return new byte[0];
    }

    @Override
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
        if (b.length > offset) {
            byte[] ch= ISOUtil.ebcdicToAsciiBytes(b, offset, 1);        // decode a single char
            if (Character.isAlphabetic(ch[0]) || ch[0] == ' ')
                return super.unpack(c, b, offset);
        }
        return 0;
    }

}
