package org.jpos.emv.cryptogram;

import org.jpos.security.ARPCMethod;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;

/**
 * M/Chip Cryptogram Specification 
 * 
 * @author Rainer Reyes
 */
public class MChipCryptogram implements CryptogramSpec {

    private final CVNMCDataBuilder dataBuilder;
    private final SKDMethod skdMethod;


    public MChipCryptogram(String cryptogramVersionNumber) {
        byte data = (byte) Integer.parseInt(cryptogramVersionNumber, 16);
        this.dataBuilder = new CVNMCDataBuilder((data & 1) == 1); // byte 1 = 1
        // byte 3-2
        if ((data >> 1 & 0x03) == 0x00) { // bits = 00
            // byte 3-2 = 00
            skdMethod = SKDMethod.MCHIP;
        } else if ((data >> 1 & 0x03) == 0x02) { // bits = 10
            // byte 3-2 = 10
            skdMethod = SKDMethod.EMV_CSKD;
        } else {
            throw new RuntimeException("Cryptogram version not supported");
        }
    }

    @Override
    public MKDMethod getMKDMethod() {
        return MKDMethod.OPTION_A;
    }

    @Override
    public SKDMethod getSKDMethod() {
        return skdMethod;
    }

    @Override
    public ARPCMethod getARPCMethod() {
        return ARPCMethod.METHOD_1;
    }

    @Override
    public CryptogramDataBuilder getDataBuilder() {
        return dataBuilder;
    }
}
