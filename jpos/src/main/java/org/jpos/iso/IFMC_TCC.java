package org.jpos.iso;


public class IFMC_TCC extends IF_CHAR{
    @Override
    public byte[] pack(ISOComponent c) throws ISOException {
        if (c != null) return super.pack(c);
        else return new byte[0];
    }

    @Override
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
        if (b.length > offset &&  (Character.isAlphabetic(b[offset]) || b[offset] == ' ')) {
            return super.unpack(c, b, offset);
        }
        return 0;
    }

}
