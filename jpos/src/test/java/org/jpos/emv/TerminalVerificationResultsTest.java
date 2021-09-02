package org.jpos.emv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class TerminalVerificationResultsTest {

    @Test
    public void testConstructorEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalVerificationResults(""));
    }

    @Test
    public void testConstructorInvalidByteArray() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalVerificationResults(new byte[7]));
    }

    @Test
    public void testConstructorInvalidHex() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalVerificationResults("000"));
    }

    @Test
    public void testConstructorNull() {
        assertThrows(NullPointerException.class, () -> new TerminalVerificationResults((byte[])null));
    }

    @Test
    public void testDump() {
        TerminalVerificationResults tvr = new TerminalVerificationResults("0000008000");
        tvr.dump(System.out, "");
    }

    @Test
    public void testExceedsFloorLimit() {
        TerminalVerificationResults tvr = new TerminalVerificationResults("0000008000");
        assertTrue(tvr.transactionExceedsFloorLimit());
        assertFalse(tvr.applicationNotEffective());
        assertFalse(tvr.cardAndTerminalDiffApps());
        assertFalse(tvr.cardholderVerificationNotSuccessful());
        assertFalse(tvr.cdaFailed());
        assertFalse(tvr.ddaFailed());
        assertFalse(tvr.expiredApplication());
        assertFalse(tvr.iccDataMissing());
        assertFalse(tvr.issuerAuthenticationFailed());
        assertFalse(tvr.lowerConsecutiveOfflineLimitExceeded());
        assertFalse(tvr.merchantForcedTransactionOnline());
        assertFalse(tvr.newCard());
        assertFalse(tvr.offlineDataProcNotPerformed());
        assertFalse(tvr.onlinePINEntered());
        assertFalse(tvr.panInHotlist());
        assertFalse(tvr.pinRequiredButNoPinPadPresent());
        assertFalse(tvr.pinRequiredButNotEntered());
        assertFalse(tvr.pinTryLimitExceeded());
        assertFalse(tvr.relayResistanceProtocolNotPerformed());
        assertTrue(tvr.relayResistanceProtocolNotSupported());
        assertFalse(tvr.relayResistanceProtocolPerformed());
        assertFalse(tvr.relayResistanceThresholdExceeded());
        assertFalse(tvr.relayResistanceTimeLimitsExceeded());
        assertTrue(tvr.rfu());
        assertFalse(tvr.scriptFailedAfterFinalGenerateAC());
        assertFalse(tvr.scriptFailedBeforeFinalGenerateAC());
        assertFalse(tvr.sdaFailed());
        assertFalse(tvr.sdaSelected());
        assertFalse(tvr.serviceNotAllowedForCardProduct());
        assertFalse(tvr.transactionSelectedRandomlyOnlineProcessing());
        assertFalse(tvr.unrecognisedCVM());
        assertFalse(tvr.defaultTDOLUsed());
        assertFalse(tvr.upperConsecutiveOfflineLimitExceeded());
    }

    @Test
    public void testRFU() {
        TerminalVerificationResults tvr = new TerminalVerificationResults("0100000000");
        assertFalse(tvr.transactionExceedsFloorLimit());
        assertTrue(tvr.rfu());
    }

    @Test
    public void testPINLimitExceeded() {
        TerminalVerificationResults tvr = new TerminalVerificationResults("0100300000");
        assertTrue(tvr.pinTryLimitExceeded());
        assertFalse(tvr.transactionExceedsFloorLimit());
    }

    @Test
    public void testUnrecognisedCVM() {
        TerminalVerificationResults tvr = new TerminalVerificationResults("0000400000");
        assertTrue(tvr.unrecognisedCVM());
        assertFalse(tvr.transactionExceedsFloorLimit());
    }
}
