/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2022 jPOS Software SRL
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
    
    private final byte[] tvr;

    public TerminalVerificationResults(byte[] tvr) {
        Objects.requireNonNull(tvr);
        if (tvr.length != 5)
            throw new IllegalArgumentException("TVR length must be 5.");
        this.tvr = tvr;
    }

    public TerminalVerificationResults(String hexTVR) {
        this(ISOUtil.hex2byte(hexTVR));
    }

    public boolean offlineDataProcNotPerformed() {
        return isBitOn(tvr[0], 8);
    }

    public boolean sdaFailed() {
        return isBitOn(tvr[0], 7);
    }

    public boolean iccDataMissing() {
        return isBitOn(tvr[0], 6);
    }

    public boolean panInHotlist() {
        return isBitOn(tvr[0], 5);
    }

    public boolean ddaFailed() {
        return isBitOn(tvr[0], 4);
    }

    public boolean cdaFailed() {
        return isBitOn(tvr[0], 3);
    }

    public boolean sdaSelected() {
        return isBitOn(tvr[0], 2);
    }

    public boolean rfu() {
        return !isBitOn(tvr[0], 1) || !isBitOn(tvr[1], 1) ||
                !isBitOn(tvr[1], 2) || !isBitOn(tvr[1], 3) ||
                !isBitOn(tvr[2], 2) || !isBitOn(tvr[2], 1) ||
                !isBitOn(tvr[3], 3) || !isBitOn(tvr[3], 2) ||
                !isBitOn(tvr[3], 1) ||
                (isBitOn(tvr[4], 2) && isBitOn(tvr[4], 1));
    }

    public boolean cardAndTerminalDiffApps() {
        return isBitOn(tvr[1], 8);
    }

    public boolean expiredApplication() {
        return isBitOn(tvr[1], 7);
    }

    public boolean applicationNotEffective() {
        return isBitOn(tvr[1], 6);
    }

    public boolean serviceNotAllowedForCardProduct() {
        return isBitOn(tvr[1], 5);
    }

    public boolean newCard() {
        return isBitOn(tvr[1], 4);
    }

    public boolean cardholderVerificationNotSuccessful() {
        return isBitOn(tvr[2], 8);
    }

    public boolean unrecognisedCVM() {
        return isBitOn(tvr[2], 7);
    }

    public boolean pinTryLimitExceeded() {
        return isBitOn(tvr[2], 6);
    }

    public boolean pinRequiredButNoPinPadPresent() {
        return isBitOn(tvr[2], 5);
    }

    public boolean pinRequiredButNotEntered() {
        return isBitOn(tvr[2], 4);
    }

    public boolean onlinePINEntered() {
        return isBitOn(tvr[2], 3);
    }

    public boolean transactionExceedsFloorLimit() {
        return isBitOn(tvr[3], 8);
    }

    public boolean lowerConsecutiveOfflineLimitExceeded() {
        return isBitOn(tvr[3], 7);
    }

    public boolean upperConsecutiveOfflineLimitExceeded() {
        return isBitOn(tvr[3], 6);
    }

    public boolean transactionSelectedRandomlyOnlineProcessing() {
        return isBitOn(tvr[3], 5);
    }

    public boolean merchantForcedTransactionOnline() {
        return isBitOn(tvr[3], 4);
    }

    public boolean defaultTDOLUsed() {
        return isBitOn(tvr[4], 8);
    }

    public boolean issuerAuthenticationFailed() {
        return isBitOn(tvr[4], 7);
    }

    public boolean scriptFailedBeforeFinalGenerateAC() {
        return isBitOn(tvr[4], 6);
    }

    public boolean scriptFailedAfterFinalGenerateAC() {
        return isBitOn(tvr[4], 5);
    }

    public boolean relayResistanceThresholdExceeded() {
        return isBitOn(tvr[4], 4);
    }

    public boolean relayResistanceTimeLimitsExceeded() {
        return isBitOn(tvr[4], 3);
    }

    public boolean relayResistanceProtocolNotSupported() {
        return !isBitOn(tvr[4], 2) && !isBitOn(tvr[4], 1);
    }

    public boolean relayResistanceProtocolNotPerformed() {
        return !isBitOn(tvr[4], 2) && isBitOn(tvr[4], 1);
    }

    public boolean relayResistanceProtocolPerformed() {
        return isBitOn(tvr[4], 2) && !isBitOn(tvr[4], 1);
    }

    private boolean isBitOn(byte value, int position) {
        return ((value >> (position - 1)) & 1) == 1;
    }

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
