package org.jpos.emv.cryptogram;

import org.jpos.security.ARPCMethod;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Rainer Reyes
 */
class VISACryptogramTest {

    @Test
    void testNotSupported() {
        assertThrows(Exception.class, () -> new VISACryptogram("99"));
    }

    @Test
    void testCVN10() {
        VISACryptogram spec = new VISACryptogram("0A");
        assertEquals(spec.getMKDMethod(), MKDMethod.OPTION_A);
        assertEquals(spec.getSKDMethod(), SKDMethod.VSDC);
        assertEquals(spec.getARPCMethod(), ARPCMethod.METHOD_1);
        assertTrue(spec.getDataBuilder() instanceof CVN10DataBuilder);
    }

    @Test
    void testCVN18() {
        VISACryptogram spec = new VISACryptogram("12");
        assertEquals(spec.getMKDMethod(), MKDMethod.OPTION_A);
        assertEquals(spec.getSKDMethod(), SKDMethod.EMV_CSKD);
        assertEquals(spec.getARPCMethod(), ARPCMethod.METHOD_2);
        assertTrue(spec.getDataBuilder() instanceof CVN18DataBuilder);
    }

    @Test
    void testCVN22() {
        VISACryptogram spec = new VISACryptogram("22");
        assertEquals(spec.getMKDMethod(), MKDMethod.OPTION_A);
        assertEquals(spec.getSKDMethod(), SKDMethod.EMV_CSKD);
        assertEquals(spec.getARPCMethod(), ARPCMethod.METHOD_2);
        assertTrue(spec.getDataBuilder() instanceof CVN22DataBuilder);
    }

}
