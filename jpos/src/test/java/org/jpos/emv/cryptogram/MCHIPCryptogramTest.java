package org.jpos.emv.cryptogram;

import org.jpos.security.ARPCMethod;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Rainer Reyes
 */
class MCHIPCryptogramTest {

    @Test
    void testNotSupported(){
        assertThrows(Exception.class,()->new MChipCryptogram("FF"));
    }
    
    @Test
    void testMChipVersionBit00(){
        MChipCryptogram spec = new MChipCryptogram("10");
        assertEquals(spec.getMKDMethod(), MKDMethod.OPTION_A);
        assertEquals(spec.getSKDMethod(), SKDMethod.MCHIP);
        assertEquals(spec.getARPCMethod(), ARPCMethod.METHOD_1);
        assertTrue(spec.getDataBuilder() instanceof CVNMCDataBuilder);
    }

    @Test
    void testMChipVersionBit10(){
        MChipCryptogram spec = new MChipCryptogram("14");
        assertEquals(spec.getMKDMethod(), MKDMethod.OPTION_A);
        assertEquals(spec.getSKDMethod(), SKDMethod.EMV_CSKD);
        assertEquals(spec.getARPCMethod(), ARPCMethod.METHOD_1);
        assertTrue(spec.getDataBuilder() instanceof CVNMCDataBuilder);
    }
}
