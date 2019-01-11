package org.jpos.iso;

import org.jpos.iso.PosDataCode.POSEnvironment;
import org.jpos.iso.PosDataCode.ReadingMethod;
import org.jpos.iso.PosDataCode.SecurityCharacteristic;
import org.jpos.iso.PosDataCode.VerificationMethod;
import org.junit.Test;


import java.util.BitSet;

import static org.jpos.iso.PosDataCode.POSEnvironment.M_COMMERCE;
import static org.jpos.iso.PosDataCode.POSEnvironment.RECURRING;
import static org.jpos.iso.PosDataCode.ReadingMethod.*;
import static org.jpos.iso.PosDataCode.SecurityCharacteristic.PKI_ENCRYPTION;
import static org.jpos.iso.PosDataCode.SecurityCharacteristic.PRIVATE_ALG_ENCRYPTION;
import static org.jpos.iso.PosDataCode.VerificationMethod.OFFLINE_PIN_IN_CLEAR;
import static org.jpos.iso.PosDataCode.VerificationMethod.ONLINE_PIN;
import static org.junit.Assert.*;

public class PosDataCodeTest {

    PosDataCode pdc = new PosDataCode();


    @Test
    public void defaultConstructor() {
    }

    @Test
    public void constructorWithParams() {
        PosDataCode pdc = new PosDataCode(0, 0, 0, 0);
        for (int i = 0x80000000; i != 0; i>>>=1) {
            assertFalse("No reading method should be set", pdc.hasReadingMethods(i));
            assertFalse("No verification method should be set", pdc.hasVerificationMethods(i));
            assertFalse("No pos environment should be set", pdc.hasPosEnvironments(i));
            assertFalse("No security characteristic should be set", pdc.hasSecurityCharacteristics(i));
        }
    }


    @Test
    public void setFlags() {
        pdc.setFlags(true,
                ReadingMethod.UNKNOWN, VerificationMethod.UNKNOWN,
                POSEnvironment.UNKNOWN, SecurityCharacteristic.UNKNOWN);
        for (int i = 0x80000000; i != 1; i>>>=1) {
            assertFalse("Only UNKNOWN reading method should be set", pdc.hasReadingMethods(i));
            assertFalse("Only UNKNOWN verification method should be set", pdc.hasVerificationMethods(i));
            assertFalse("Only UNKNOWN pos environment should be set", pdc.hasPosEnvironments(i));
            assertFalse("Only UNKNOWN security characteristic should be set",
                    pdc.hasSecurityCharacteristics(i));
        }
        assertTrue("UNKNOWN reading method should be set", pdc.hasReadingMethods(1));
        assertTrue("UNKNOWN verification method should be set", pdc.hasVerificationMethods(1));
        assertTrue("UNKNOWN pos environment should be set", pdc.hasPosEnvironments(1));
        assertTrue("UNKNOWN security characteristic should be set", pdc.hasSecurityCharacteristics(1));
        pdc.setFlags(false, ReadingMethod.UNKNOWN, VerificationMethod.UNKNOWN,
                POSEnvironment.UNKNOWN, SecurityCharacteristic.UNKNOWN);
        for (int i = 0x80000000; i != 0; i>>>=1) {
            assertFalse("No reading method should be set", pdc.hasReadingMethods(i));
            assertFalse("No verification method should be set", pdc.hasVerificationMethods(i));
            assertFalse("No pos environment should be set", pdc.hasPosEnvironments(i));
            assertFalse("No security characteristic should be set", pdc.hasSecurityCharacteristics(i));
        }
        pdc.setFlags(true,
                CONTACTLESS, BARCODE,
                ONLINE_PIN, OFFLINE_PIN_IN_CLEAR,
                M_COMMERCE, RECURRING,
                PRIVATE_ALG_ENCRYPTION, PKI_ENCRYPTION);
        assertTrue("Reding methods missing",
                pdc.hasReadingMethods(CONTACTLESS.intValue() | BARCODE.intValue()));
        assertTrue("Verification methods missing",
                pdc.hasVerificationMethods(ONLINE_PIN.intValue() | OFFLINE_PIN_IN_CLEAR.intValue()));
        assertTrue("POS enironments missing",
                pdc.hasPosEnvironments(M_COMMERCE.intValue() | RECURRING.intValue()));
        assertTrue("Security characteristics  missing",
                pdc.hasSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION.intValue() | PKI_ENCRYPTION.intValue()));
    }

    @Test
    public void setReadingMethods() {
        pdc.setReadingMethods(CONTACTLESS, PHYSICAL, BARCODE);
        assertTrue("Reding methods missing",
                pdc.hasReadingMethods(CONTACTLESS.intValue() | PHYSICAL.intValue() | BARCODE.intValue()));
    }

    @Test
    public void unsetReadingMethods() {
        pdc.setReadingMethods(CONTACTLESS, PHYSICAL, BARCODE);
        pdc.unsetReadingMethods(BARCODE);
        assertFalse("Reding method BARCODE should not be present",
                pdc.hasReadingMethods(CONTACTLESS.intValue() | PHYSICAL.intValue() | BARCODE.intValue()));
        assertTrue("Reding methods CONTACTLESS and PHYSICAL should be present",
                pdc.hasReadingMethods(CONTACTLESS.intValue() | PHYSICAL.intValue()));
    }


    @Test
    public void setVerificationMethods() {
        pdc.setVerificationMethods(ONLINE_PIN, OFFLINE_PIN_IN_CLEAR);
        assertTrue("Verification methods missing",
                pdc.hasVerificationMethods(ONLINE_PIN.intValue() | OFFLINE_PIN_IN_CLEAR.intValue()));
    }

    @Test
    public void unsetVerificationMethods() {
        pdc.setVerificationMethods(ONLINE_PIN, OFFLINE_PIN_IN_CLEAR);
        pdc.unsetVerificationMethods(OFFLINE_PIN_IN_CLEAR);
        assertFalse("Reding methods OFFLINE_PIN_IN_CLEAR should not be present",
                pdc.hasVerificationMethods(ONLINE_PIN.intValue() | OFFLINE_PIN_IN_CLEAR.intValue()));
        assertTrue("Reding methods ONLINE_PIN should be present",
                pdc.hasVerificationMethods(ONLINE_PIN.intValue()));
    }


    @Test
    public void setPOSEnvironments() {
        pdc.setPOSEnvironments(M_COMMERCE, RECURRING);
        assertTrue("Missing POS environments",
                pdc.hasPosEnvironments(M_COMMERCE.intValue() | RECURRING.intValue()));
    }

    @Test
    public void unsetPOSEnvironments() {
        pdc.setPOSEnvironments(M_COMMERCE, RECURRING);
        pdc.unsetPOSEnvironments(M_COMMERCE);
        assertFalse("POS environment M_COMMERCE should not be present",
                pdc.hasPosEnvironments(M_COMMERCE.intValue() | RECURRING.intValue()));
        assertTrue("RECURRING POS environment should be present",
                pdc.hasPosEnvironments(RECURRING.intValue()));
    }


    @Test
    public void setSecurityCharacteristics() {
        pdc.setSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION, PKI_ENCRYPTION);
        assertTrue("Missing Security Characteristics",
                pdc.hasSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION.intValue() | PKI_ENCRYPTION.intValue()));
    }

    @Test
    public void unsetSecurityCharacteristics() {
        pdc.setSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION, PKI_ENCRYPTION);
        pdc.unsetSecurityCharacteristics(PKI_ENCRYPTION);
        assertFalse("PKI_ENCRYPTION Security Characteristics should not be present",
                pdc.hasSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION.intValue() | PKI_ENCRYPTION.intValue()));
        assertTrue("PRIVATE_ALG_ENCRYPTION should be present",
                pdc.hasSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION.intValue()));
   }

}