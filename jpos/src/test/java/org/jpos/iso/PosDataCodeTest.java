/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso;

import static org.jpos.iso.PosDataCode.POSEnvironment.M_COMMERCE;
import static org.jpos.iso.PosDataCode.POSEnvironment.RECURRING;
import static org.jpos.iso.PosDataCode.ReadingMethod.BARCODE;
import static org.jpos.iso.PosDataCode.ReadingMethod.CONTACTLESS;
import static org.jpos.iso.PosDataCode.ReadingMethod.PHYSICAL;
import static org.jpos.iso.PosDataCode.SecurityCharacteristic.PKI_ENCRYPTION;
import static org.jpos.iso.PosDataCode.SecurityCharacteristic.PRIVATE_ALG_ENCRYPTION;
import static org.jpos.iso.PosDataCode.VerificationMethod.OFFLINE_PIN_IN_CLEAR;
import static org.jpos.iso.PosDataCode.VerificationMethod.ONLINE_PIN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jpos.iso.PosDataCode.POSEnvironment;
import org.jpos.iso.PosDataCode.ReadingMethod;
import org.jpos.iso.PosDataCode.SecurityCharacteristic;
import org.jpos.iso.PosDataCode.VerificationMethod;
import org.junit.jupiter.api.Test;

public class PosDataCodeTest {

    PosDataCode pdc = new PosDataCode();


    @Test
    public void defaultConstructor() {
    }

    @Test
    public void constructorWithParams() {
        PosDataCode pdc = new PosDataCode(0, 0, 0, 0);
        for (int i = 0x80000000; i != 0; i>>>=1) {
            assertFalse(pdc.hasReadingMethods(i), "No reading method should be set");
            assertFalse(pdc.hasVerificationMethods(i), "No verification method should be set");
            assertFalse(pdc.hasPosEnvironments(i), "No pos environment should be set");
            assertFalse(pdc.hasSecurityCharacteristics(i), "No security characteristic should be set");
        }
    }


    @Test
    public void setFlags() {
        pdc.setFlags(true,
                ReadingMethod.UNKNOWN, VerificationMethod.UNKNOWN,
                POSEnvironment.UNKNOWN, SecurityCharacteristic.UNKNOWN);
        for (int i = 0x80000000; i != 1; i>>>=1) {
            assertFalse(pdc.hasReadingMethods(i), "Only UNKNOWN reading method should be set");
            assertFalse(pdc.hasVerificationMethods(i), "Only UNKNOWN verification method should be set");
            assertFalse(pdc.hasPosEnvironments(i), "Only UNKNOWN pos environment should be set");
            assertFalse(pdc.hasSecurityCharacteristics(i),
                    "Only UNKNOWN security characteristic should be set");
        }
        assertTrue(pdc.hasReadingMethods(1), "UNKNOWN reading method should be set");
        assertTrue(pdc.hasVerificationMethods(1), "UNKNOWN verification method should be set");
        assertTrue(pdc.hasPosEnvironments(1), "UNKNOWN pos environment should be set");
        assertTrue(pdc.hasSecurityCharacteristics(1), "UNKNOWN security characteristic should be set");
        pdc.setFlags(false, ReadingMethod.UNKNOWN, VerificationMethod.UNKNOWN,
                POSEnvironment.UNKNOWN, SecurityCharacteristic.UNKNOWN);
        for (int i = 0x80000000; i != 0; i>>>=1) {
            assertFalse(pdc.hasReadingMethods(i), "No reading method should be set");
            assertFalse(pdc.hasVerificationMethods(i), "No verification method should be set");
            assertFalse(pdc.hasPosEnvironments(i), "No pos environment should be set");
            assertFalse(pdc.hasSecurityCharacteristics(i), "No security characteristic should be set");
        }
        pdc.setFlags(true,
                CONTACTLESS, BARCODE,
                ONLINE_PIN, OFFLINE_PIN_IN_CLEAR,
                M_COMMERCE, RECURRING,
                PRIVATE_ALG_ENCRYPTION, PKI_ENCRYPTION);
        assertTrue(pdc.hasReadingMethods(CONTACTLESS.intValue() | BARCODE.intValue()),
                "Reding methods missing");
        assertTrue(pdc.hasVerificationMethods(ONLINE_PIN.intValue() | OFFLINE_PIN_IN_CLEAR.intValue()),
                "Verification methods missing");
        assertTrue(pdc.hasPosEnvironments(M_COMMERCE.intValue() | RECURRING.intValue()),
                "POS enironments missing");
        assertTrue(pdc.hasSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION.intValue() | PKI_ENCRYPTION.intValue()),
                "Security characteristics  missing");
    }

    @Test
    public void setReadingMethods() {
        pdc.setReadingMethods(CONTACTLESS, PHYSICAL, BARCODE);
        assertTrue(pdc.hasReadingMethods(CONTACTLESS.intValue() | PHYSICAL.intValue() | BARCODE.intValue()),
                "Reding methods missing");
    }

    @Test
    public void unsetReadingMethods() {
        pdc.setReadingMethods(CONTACTLESS, PHYSICAL, BARCODE);
        pdc.unsetReadingMethods(BARCODE);
        assertFalse(pdc.hasReadingMethods(CONTACTLESS.intValue() | PHYSICAL.intValue() | BARCODE.intValue()),
                "Reding method BARCODE should not be present");
        assertTrue(pdc.hasReadingMethods(CONTACTLESS.intValue() | PHYSICAL.intValue()),
                "Reding methods CONTACTLESS and PHYSICAL should be present");
    }


    @Test
    public void setVerificationMethods() {
        pdc.setVerificationMethods(ONLINE_PIN, OFFLINE_PIN_IN_CLEAR);
        assertTrue(pdc.hasVerificationMethods(ONLINE_PIN.intValue() | OFFLINE_PIN_IN_CLEAR.intValue()),
                "Verification methods missing");
    }

    @Test
    public void unsetVerificationMethods() {
        pdc.setVerificationMethods(ONLINE_PIN, OFFLINE_PIN_IN_CLEAR);
        pdc.unsetVerificationMethods(OFFLINE_PIN_IN_CLEAR);
        assertFalse(pdc.hasVerificationMethods(ONLINE_PIN.intValue() | OFFLINE_PIN_IN_CLEAR.intValue()),
                "Reding methods OFFLINE_PIN_IN_CLEAR should not be present");
        assertTrue(pdc.hasVerificationMethods(ONLINE_PIN.intValue()),
                "Reding methods ONLINE_PIN should be present");
    }


    @Test
    public void setPOSEnvironments() {
        pdc.setPOSEnvironments(M_COMMERCE, RECURRING);
        assertTrue(pdc.hasPosEnvironments(M_COMMERCE.intValue() | RECURRING.intValue()),
                "Missing POS environments");
    }

    @Test
    public void unsetPOSEnvironments() {
        pdc.setPOSEnvironments(M_COMMERCE, RECURRING);
        pdc.unsetPOSEnvironments(M_COMMERCE);
        assertFalse(pdc.hasPosEnvironments(M_COMMERCE.intValue() | RECURRING.intValue()),
                "POS environment M_COMMERCE should not be present");
        assertTrue(pdc.hasPosEnvironments(RECURRING.intValue()),
                "RECURRING POS environment should be present");
    }


    @Test
    public void setSecurityCharacteristics() {
        pdc.setSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION, PKI_ENCRYPTION);
        assertTrue(pdc.hasSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION.intValue() | PKI_ENCRYPTION.intValue()),
                "Missing Security Characteristics");
    }

    @Test
    public void unsetSecurityCharacteristics() {
        pdc.setSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION, PKI_ENCRYPTION);
        pdc.unsetSecurityCharacteristics(PKI_ENCRYPTION);
        assertFalse(pdc.hasSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION.intValue() | PKI_ENCRYPTION.intValue()),
                "PKI_ENCRYPTION Security Characteristics should not be present");
        assertTrue(pdc.hasSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION.intValue()),
                "PRIVATE_ALG_ENCRYPTION should be present");
   }

    @Test
    public void checkIsEMV() {
        pdc.setSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION, PKI_ENCRYPTION);
        pdc.unsetSecurityCharacteristics(PKI_ENCRYPTION);
        pdc.setReadingMethods(ReadingMethod.ICC);
        
        assertTrue(pdc.isEMV());
        assertFalse(pdc.isManualEntry());
        assertFalse(pdc.isSwiped());        
   }

   @Test
   public void checkIsManualEntry() {
        pdc.setSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION, PKI_ENCRYPTION);
        pdc.unsetSecurityCharacteristics(PKI_ENCRYPTION);
        pdc.setReadingMethods(ReadingMethod.PHYSICAL);
        
        assertFalse(pdc.isEMV());
        assertTrue(pdc.isManualEntry());
        assertFalse(pdc.isSwiped());        
   }
   
   @Test
   public void checkIsSwiped() {
        pdc.setSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION, PKI_ENCRYPTION);
        pdc.unsetSecurityCharacteristics(PKI_ENCRYPTION);
        pdc.setReadingMethods(ReadingMethod.MAGNETIC_STRIPE);
        
        assertFalse(pdc.isEMV());
        assertFalse(pdc.isManualEntry());
        assertTrue(pdc.isSwiped());        
   }
      
   @Test
   public void checkIsECommerce() {
        pdc.setSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION, PKI_ENCRYPTION);
        pdc.unsetSecurityCharacteristics(PKI_ENCRYPTION);
        pdc.setPOSEnvironments(POSEnvironment.E_COMMERCE);
        pdc.setReadingMethods(ReadingMethod.PHYSICAL);
        
        assertFalse(pdc.isEMV());
        assertTrue(pdc.isManualEntry());
        assertTrue(pdc.isECommerce());
        assertFalse(pdc.isSwiped());        
   }   

   @Test
   public void checkIsRecurring() {
        pdc.setSecurityCharacteristics(PRIVATE_ALG_ENCRYPTION, PKI_ENCRYPTION);
        pdc.unsetSecurityCharacteristics(PKI_ENCRYPTION);
        pdc.setPOSEnvironments(POSEnvironment.RECURRING);
        pdc.setReadingMethods(ReadingMethod.PHYSICAL);
        
        assertFalse(pdc.isEMV());
        assertTrue(pdc.isManualEntry());
        assertFalse(pdc.isECommerce());
        assertTrue(pdc.isRecurring());
        assertFalse(pdc.isSwiped());        
   }   

}