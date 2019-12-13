package org.jpos.emv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Test;

public class CVRMastercardTest {

    @Test
    public void testConstructorWithInvalidByteLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CVRMastercard(new byte[8]);
        });
    }

    @Test
    public void testConstructorWithInvalidHexLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CVRMastercard("0000");
        });
    }

    @Test
    public void testSuccessfulHexConstructor() {
        CVRMastercard cvr = new CVRMastercard("250000044000");
        cvr.dump(System.out, "");
        assertTrue(cvr.aacReturnedInSecondGenerateAC());
        assertTrue(cvr.arqcReturnedInFirstGenerateAC());
        assertTrue(cvr.offlinePINVerificationPerformed());
        assertTrue(cvr.internationalTransaction());
        assertTrue(cvr.upperConsecutiveOfflineLimitExceeded());
        assertFalse(cvr.upperCumulativeOfflineLimitExceeded());
        assertFalse(cvr.domesticTransaction(), "Transaction is not domestic.");
        assertEquals(0, cvr.rightNibbleOfScriptCounter());
        assertEquals(0, cvr.rightNibbleOfPINTryCounter());
    }

    @Test
    public void testSuccessfulByteArrayConstructor() {
        CVRMastercard cvr = new CVRMastercard(ISOUtil.hex2byte("250000044000"));
        cvr.dump(System.out, "");
        assertTrue(cvr.aacReturnedInSecondGenerateAC());
        assertTrue(cvr.arqcReturnedInFirstGenerateAC());
        assertTrue(cvr.offlinePINVerificationPerformed());
        assertTrue(cvr.internationalTransaction());
        assertTrue(cvr.upperConsecutiveOfflineLimitExceeded());
        assertFalse(cvr.upperCumulativeOfflineLimitExceeded());
        assertFalse(cvr.domesticTransaction(), "Transaction is not domestic.");
        assertEquals(0, cvr.rightNibbleOfScriptCounter());
        assertEquals(0, cvr.rightNibbleOfPINTryCounter());
    }    
}