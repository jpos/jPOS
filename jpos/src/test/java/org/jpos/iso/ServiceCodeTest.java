package org.jpos.iso;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public final class ServiceCodeTest {

    @Test
    public void nullServiceCode() {
        assertThrows(NullPointerException.class, () -> {
            @SuppressWarnings("unused")
            ServiceCode sc = new ServiceCode(null);
        });
    }
    
    @Test
    public void invalidLengthServiceCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            ServiceCode sc = new ServiceCode("1234");
        });
    }

    @Test
    public void invalidServiceCode12E() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            ServiceCode sc = new ServiceCode("12E");
        });
    }
    
    @Test
    public void invalidServiceCodeSign22() {
        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unused")
            ServiceCode sc = new ServiceCode("+22");
        });
    }    
    
    @Test
    public void validLengthServiceCode() {
        @SuppressWarnings("unused")
        ServiceCode sc = new ServiceCode("201");
    }
    
    @Test
    public void testServiceCode201() {
        ServiceCode sc = new ServiceCode("201");        
        assertTrue(sc.isICC(), "Service code should be ICC");
        assertTrue(sc.hasNoRestrictions(), "Service code should have no restrictions");
        assertTrue(sc.isInternational(), "Service code should be international");
        assertTrue(sc.isNormalAuthorization(), "Service code should be normal authorization");
        assertFalse(sc.isPINRequired(), "Service code should not require PIN");
        assertFalse(sc.isPrivate(), "Service code should not be private");
        assertFalse(sc.isNational(), "Service code should not be national");
        assertFalse(sc.isTest(), "Service code should not be test");
        assertFalse(sc.isGoodsAndServicesOnly(), "Service code should not be good and services only");
        assertFalse(sc.isATMOnly(), "Service code should not be ATM only");
        assertFalse(sc.isCashOnly(), "Service code should not be cash only");
    } 
}