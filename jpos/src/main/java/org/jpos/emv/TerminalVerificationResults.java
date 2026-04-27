/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.emv;

import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Terminal verification results (TVR) parser.
 */
public final class TerminalVerificationResults implements Loggeable {
    
    /** Raw 5-byte TVR value. */
    private final byte[] tvr;

    /**
     * Constructs a TerminalVerificationResults from a 5-byte TVR array.
     *
     * @param tvr the 5-byte Terminal Verification Results value
     * @throws IllegalArgumentException if the array length is not 5
     */
    public TerminalVerificationResults(byte[] tvr) {
        Objects.requireNonNull(tvr);
        if (tvr.length != 5)
            throw new IllegalArgumentException("TVR length must be 5.");
        this.tvr = tvr;
    }

    /**
     * Constructs a TerminalVerificationResults from a hex-encoded TVR string.
     *
     * @param hexTVR 10-character hexadecimal string representing the 5-byte TVR
     */
    public TerminalVerificationResults(String hexTVR) {
        this(ISOUtil.hex2byte(hexTVR));
    }

    /**
     * Returns {@code true} if offline data authentication was not performed.
     *
     * @return {@code true} if byte 1 bit 8 is set
     */
    public boolean offlineDataProcNotPerformed() {
        return isBitOn(tvr[0], 8);
    }

    /**
     * Returns {@code true} if Static Data Authentication (SDA) failed.
     *
     * @return {@code true} if byte 1 bit 7 is set
     */
    public boolean sdaFailed() {
        return isBitOn(tvr[0], 7);
    }

    /**
     * Returns {@code true} if ICC data is missing.
     *
     * @return {@code true} if byte 1 bit 6 is set
     */
    public boolean iccDataMissing() {
        return isBitOn(tvr[0], 6);
    }

    /**
     * Returns {@code true} if the PAN appears in the hotlist.
     *
     * @return {@code true} if byte 1 bit 5 is set
     */
    public boolean panInHotlist() {
        return isBitOn(tvr[0], 5);
    }

    /**
     * Returns {@code true} if Dynamic Data Authentication (DDA) failed.
     *
     * @return {@code true} if byte 1 bit 4 is set
     */
    public boolean ddaFailed() {
        return isBitOn(tvr[0], 4);
    }

    /**
     * Returns {@code true} if Combined DDA/Application Cryptogram Generation (CDA) failed.
     *
     * @return {@code true} if byte 1 bit 3 is set
     */
    public boolean cdaFailed() {
        return isBitOn(tvr[0], 3);
    }

    /**
     * Returns {@code true} if SDA was selected.
     *
     * @return {@code true} if byte 1 bit 2 is set
     */
    public boolean sdaSelected() {
        return isBitOn(tvr[0], 2);
    }

    /**
     * Returns {@code true} if any reserved-for-future-use (RFU) bit is unset as expected.
     *
     * @return {@code true} if none of the RFU bits are unexpectedly set
     */
    public boolean rfu() {
        return !isBitOn(tvr[0], 1) || !isBitOn(tvr[1], 1) ||
                !isBitOn(tvr[1], 2) || !isBitOn(tvr[1], 3) ||
                !isBitOn(tvr[2], 2) || !isBitOn(tvr[2], 1) ||
                !isBitOn(tvr[3], 3) || !isBitOn(tvr[3], 2) ||
                !isBitOn(tvr[3], 1) ||
                (isBitOn(tvr[4], 2) && isBitOn(tvr[4], 1));
    }

    /**
     * Returns {@code true} if the card and terminal have different application versions.
     *
     * @return {@code true} if byte 2 bit 8 is set
     */
    public boolean cardAndTerminalDiffApps() {
        return isBitOn(tvr[1], 8);
    }

    /**
     * Returns {@code true} if the application has expired.
     *
     * @return {@code true} if byte 2 bit 7 is set
     */
    public boolean expiredApplication() {
        return isBitOn(tvr[1], 7);
    }

    /**
     * Returns {@code true} if the application is not yet effective.
     *
     * @return {@code true} if byte 2 bit 6 is set
     */
    public boolean applicationNotEffective() {
        return isBitOn(tvr[1], 6);
    }

    /**
     * Returns {@code true} if the service is not allowed for the card product.
     *
     * @return {@code true} if byte 2 bit 5 is set
     */
    public boolean serviceNotAllowedForCardProduct() {
        return isBitOn(tvr[1], 5);
    }

    /**
     * Returns {@code true} if this is a new card (first transaction).
     *
     * @return {@code true} if byte 2 bit 4 is set
     */
    public boolean newCard() {
        return isBitOn(tvr[1], 4);
    }

    /**
     * Returns {@code true} if cardholder verification was not successful.
     *
     * @return {@code true} if byte 3 bit 8 is set
     */
    public boolean cardholderVerificationNotSuccessful() {
        return isBitOn(tvr[2], 8);
    }

    /**
     * Returns {@code true} if an unrecognised CVM (Cardholder Verification Method) was encountered.
     *
     * @return {@code true} if byte 3 bit 7 is set
     */
    public boolean unrecognisedCVM() {
        return isBitOn(tvr[2], 7);
    }

    /**
     * Returns {@code true} if the PIN try limit has been exceeded.
     *
     * @return {@code true} if byte 3 bit 6 is set
     */
    public boolean pinTryLimitExceeded() {
        return isBitOn(tvr[2], 6);
    }

    /**
     * Returns {@code true} if a PIN is required but no PIN pad is present.
     *
     * @return {@code true} if byte 3 bit 5 is set
     */
    public boolean pinRequiredButNoPinPadPresent() {
        return isBitOn(tvr[2], 5);
    }

    /**
     * Returns {@code true} if a PIN is required but was not entered.
     *
     * @return {@code true} if byte 3 bit 4 is set
     */
    public boolean pinRequiredButNotEntered() {
        return isBitOn(tvr[2], 4);
    }

    /**
     * Returns {@code true} if an online PIN was entered.
     *
     * @return {@code true} if byte 3 bit 3 is set
     */
    public boolean onlinePINEntered() {
        return isBitOn(tvr[2], 3);
    }

    /**
     * Returns {@code true} if the transaction amount exceeds the floor limit.
     *
     * @return {@code true} if byte 4 bit 8 is set
     */
    public boolean transactionExceedsFloorLimit() {
        return isBitOn(tvr[3], 8);
    }

    /**
     * Returns {@code true} if the lower consecutive offline limit was exceeded.
     *
     * @return {@code true} if byte 4 bit 7 is set
     */
    public boolean lowerConsecutiveOfflineLimitExceeded() {
        return isBitOn(tvr[3], 7);
    }

    /**
     * Returns {@code true} if the upper consecutive offline limit was exceeded.
     *
     * @return {@code true} if byte 4 bit 6 is set
     */
    public boolean upperConsecutiveOfflineLimitExceeded() {
        return isBitOn(tvr[3], 6);
    }

    /**
     * Returns {@code true} if the transaction was selected randomly for online processing.
     *
     * @return {@code true} if byte 4 bit 5 is set
     */
    public boolean transactionSelectedRandomlyOnlineProcessing() {
        return isBitOn(tvr[3], 5);
    }

    /**
     * Returns {@code true} if the merchant forced the transaction online.
     *
     * @return {@code true} if byte 4 bit 4 is set
     */
    public boolean merchantForcedTransactionOnline() {
        return isBitOn(tvr[3], 4);
    }

    /**
     * Returns {@code true} if the default TDOL (Transaction Data Object List) was used.
     *
     * @return {@code true} if byte 5 bit 8 is set
     */
    public boolean defaultTDOLUsed() {
        return isBitOn(tvr[4], 8);
    }

    /**
     * Returns {@code true} if issuer authentication failed.
     *
     * @return {@code true} if byte 5 bit 7 is set
     */
    public boolean issuerAuthenticationFailed() {
        return isBitOn(tvr[4], 7);
    }

    /**
     * Returns {@code true} if an issuer script failed before the final Generate AC command.
     *
     * @return {@code true} if byte 5 bit 6 is set
     */
    public boolean scriptFailedBeforeFinalGenerateAC() {
        return isBitOn(tvr[4], 6);
    }

    /**
     * Returns {@code true} if an issuer script failed after the final Generate AC command.
     *
     * @return {@code true} if byte 5 bit 5 is set
     */
    public boolean scriptFailedAfterFinalGenerateAC() {
        return isBitOn(tvr[4], 5);
    }

    /**
     * Returns {@code true} if the relay resistance threshold was exceeded.
     *
     * @return {@code true} if byte 5 bit 4 is set
     */
    public boolean relayResistanceThresholdExceeded() {
        return isBitOn(tvr[4], 4);
    }

    /**
     * Returns {@code true} if the relay resistance time limits were exceeded.
     *
     * @return {@code true} if byte 5 bit 3 is set
     */
    public boolean relayResistanceTimeLimitsExceeded() {
        return isBitOn(tvr[4], 3);
    }

    /**
     * Returns {@code true} if the relay resistance protocol is not supported.
     *
     * @return {@code true} if byte 5 bits 2 and 1 are both clear
     */
    public boolean relayResistanceProtocolNotSupported() {
        return !isBitOn(tvr[4], 2) && !isBitOn(tvr[4], 1);
    }

    /**
     * Returns {@code true} if the relay resistance protocol was not performed.
     *
     * @return {@code true} if byte 5 bit 2 is clear and bit 1 is set
     */
    public boolean relayResistanceProtocolNotPerformed() {
        return !isBitOn(tvr[4], 2) && isBitOn(tvr[4], 1);
    }

    /**
     * Returns {@code true} if the relay resistance protocol was performed.
     *
     * @return {@code true} if byte 5 bit 2 is set and bit 1 is clear
     */
    public boolean relayResistanceProtocolPerformed() {
        return isBitOn(tvr[4], 2) && !isBitOn(tvr[4], 1);
    }

    /**
     * Tests whether a specific bit position is set in the given byte.
     *
     * @param value    the byte to test
     * @param position bit position (1 = least significant)
     * @return {@code true} if the bit at the given position is 1
     */
    private boolean isBitOn(byte value, int position) {
        return ((value >> (position - 1)) & 1) == 1;
    }

    /**
     * Dumps a human-readable binary representation of the TVR to the given stream.
     *
     * @param p      the output stream
     * @param indent indentation prefix string
     */
    @Override
    public void dump(PrintStream p, String indent) {
        String inner = indent + "  ";
        p.printf("%s<terminal-verification-results value='%s'>%n", indent, ISOUtil.hexString(tvr));
        p.printf("%sByte 1: %s%n", inner, String.format("%8s", Integer.toBinaryString(tvr[0] & 0xFF)).replace(' ', '0'));
        p.printf("%sByte 2: %s%n", inner, String.format("%8s", Integer.toBinaryString(tvr[1] & 0xFF)).replace(' ', '0'));
        p.printf("%sByte 3: %s%n", inner, String.format("%8s", Integer.toBinaryString(tvr[2] & 0xFF)).replace(' ', '0'));
        p.printf("%sByte 4: %s%n", inner, String.format("%8s", Integer.toBinaryString(tvr[3] & 0xFF)).replace(' ', '0'));
        p.printf("%sByte 5: %s%n", inner, String.format("%8s", Integer.toBinaryString(tvr[4] & 0xFF)).replace(' ', '0'));
        p.printf("%s</terminal-verification-results>%n", indent);
    }
}
