package org.jpos.emv.cryptogram;

import org.jpos.security.ARPCMethod;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;

import org.jpos.emv.cryptogram.CryptogramDataBuilder.PaddingMethod;

/**
 * Common Payment Application (CPA) Cryptogram Specification
 */
public class CPACryptogram implements CryptogramSpec {

    final PaddingMethod paddingMethod = CryptogramDataBuilder.ISO9797Method2;

    @Override
    public MKDMethod getMKDMethod() {
        return MKDMethod.OPTION_A;
    }

    @Override
    public SKDMethod getSKDMethod() {
        return SKDMethod.EMV_CSKD;
    }

    @Override
    public ARPCMethod getARPCMethod() {
        return ARPCMethod.METHOD_2;
    }

    @Override
    public CryptogramDataBuilder getDataBuilder() {
        return new CVNCPADataBuilder();
    }
}
