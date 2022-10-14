package org.jpos.emv.cryptogram;

import org.jpos.security.ARPCMethod;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;

/**
 * VISA Cryptogram Specification
 *
 * @author Rainer Reyes
 */
public class VISACryptogram implements CryptogramSpec {

    private final Integer number;
    private final CryptogramDataBuilder dataBuilder;
    private final ARPCMethod arpcMethod;
    private final SKDMethod skdMethod;

    public VISACryptogram(String cryptogramVersionNumber) {
        this.number = Integer.parseInt(cryptogramVersionNumber, 16);
        if (number == 10) {
            this.dataBuilder = new CVN10DataBuilder();
            this.arpcMethod = ARPCMethod.METHOD_1;
            this.skdMethod = SKDMethod.VSDC;
        } else if (number == 18) { // CVN 18
            this.dataBuilder = new CVN18DataBuilder();
            this.arpcMethod = ARPCMethod.METHOD_2;
            this.skdMethod = SKDMethod.EMV_CSKD;
        } else if (number == 34) {//CVN '22'
            this.dataBuilder = new CVN22DataBuilder();
            this.arpcMethod = ARPCMethod.METHOD_2;
            this.skdMethod = SKDMethod.EMV_CSKD;
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
        return arpcMethod;
    }

    @Override
    public CryptogramDataBuilder getDataBuilder() {
        return dataBuilder;
    }
}
