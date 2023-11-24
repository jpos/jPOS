package org.jpos.tlv.packager.bertlv;
import org.bouncycastle.util.Arrays;
import org.jpos.iso.*;
import org.jpos.tlv.ISOTaggedField;

import org.junit.jupiter.api.*;

public class Bug568BERTLVAsciiHexPackager {

    final String HEX_ASCII = "9f2610e18d2a69a45dd09a9f360401839f3708ab34b0bd";

    @Test
    public void testLenInterpretation() throws ISOException {
        final ISOMsg ICC_DATA = new ISOMsg(55);
        final BERTLVAsciiHexPackager packager = new BERTLVAsciiHexPackager();
        final IFA_LLABINARY fieldPackager = new IFA_LLABINARY();


        packager.setFieldPackager(new org.jpos.iso.ISOFieldPackager[]{fieldPackager});
        ICC_DATA.setPackager(packager);

        ICC_DATA.unpack(HEX_ASCII.getBytes());

        ICC_DATA.getChildren().values().forEach
                (e -> {
                    try {
                        ISOTaggedField field = (ISOTaggedField) e;
                        final byte[] value = (byte[]) field.getValue();
                        System.out.println("tag " + field.getTag() + " value " + ISOUtil.hexString(value));
                        Assertions.
                                assertFalse(Arrays.isNullOrEmpty(value));

                    } catch (ISOException ex) {
                        throw new RuntimeException(ex);
                    }
                });


    }

}

