package org.jpos.emv.cryptogram;

import org.jpos.security.ARPCMethod;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CPACryptogramTest {
    @Test
    void test(){
        CPACryptogram spec = new CPACryptogram();
        assertEquals(spec.paddingMethod, CryptogramDataBuilder.ISO9797Method2);
        assertEquals(spec.getMKDMethod(), MKDMethod.OPTION_A);
        assertEquals(spec.getSKDMethod(), SKDMethod.EMV_CSKD);
        assertEquals(spec.getARPCMethod(), ARPCMethod.METHOD_2);
        assertTrue(spec.getDataBuilder() instanceof CVNCPADataBuilder);
    }
}
