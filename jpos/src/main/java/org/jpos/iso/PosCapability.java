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
 * along with this program, if not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Structured representation of DE-027 (POS Capability) in the jPOS CMF.
 *
 * <p>DE-027 is a 27-byte fixed-length field that describes the capabilities
 * of the point-of-service terminal. It is the <em>capability</em> counterpart
 * of {@link PosDataCode} (DE-022), which records what actually happened.
 *
 * <h2>Wire layout</h2>
 * <pre>
 *  Bytes  1- 4  Sub-field 27-1: card reading capability      (B4, bit-flags)
 *  Bytes  5- 8  Sub-field 27-2: cardholder verification cap. (B4, bit-flags)
 *  Byte   9     Sub-field 27-3: approval code length         (N1, ASCII)
 *  Bytes 10-12  Sub-field 27-4: cardholder receipt length    (N3, ASCII)
 *  Bytes 13-15  Sub-field 27-5: card acceptor receipt length (N3, ASCII)
 *  Bytes 16-18  Sub-field 27-6: cardholder display length    (N3, ASCII)
 *  Bytes 19-21  Sub-field 27-7: card acceptor display length (N3, ASCII)
 *  Bytes 22-24  Sub-field 27-8: ICC scripts data length      (N3, ASCII)
 *  Byte  25     Sub-field 27-9: track 3 rewrite capability   (A1, 'Y'/'N')
 *  Byte  26     Sub-field 27-10: card capture capability     (A1, 'Y'/'N')
 *  Byte  27     Sub-field 27-11: PIN input length capability (B1, binary)
 * </pre>
 *
 * <p>Sub-fields 27-1 and 27-2 share the same bit-flag tables as DE-022.
 * The {@link PosDataCode.ReadingMethod} and {@link PosDataCode.VerificationMethod}
 * enums are therefore reused directly.
 *
 * <p>The bit-numbering convention follows ISO 8583: B1 = MSB = {@code 0x80}.
 * Internally, {@link PosFlags} maps flag {@code intValue()} 1 to the LSB of
 * each 4-byte word and serialises little-endian within each word—identical to
 * {@link PosDataCode}. See the DE-022 spec note for the historical rationale.
 *
 * <p>Usage example:
 * <pre>{@code
 * // Build and pack
 * PosCapability cap = PosCapability.builder()
 *     .readingCapability(PosDataCode.ReadingMethod.MAGNETIC_STRIPE)
 *     .readingCapability(PosDataCode.ReadingMethod.ICC)
 *     .verificationCapability(PosDataCode.VerificationMethod.ONLINE_PIN)
 *     .verificationCapability(PosDataCode.VerificationMethod.MANUAL_SIGNATURE)
 *     .approvalCodeLength(6)
 *     .cardholderReceiptLength(40)
 *     .cardAcceptorReceiptLength(40)
 *     .cardholderDisplayLength(16)
 *     .cardAcceptorDisplayLength(16)
 *     .iccScriptDataLength(128)
 *     .track3RewriteCapable(false)
 *     .cardCaptureCapable(false)
 *     .pinInputLength(12)
 *     .build();
 *
 * msg.set(new ISOBinaryField(27, cap.pack()));
 *
 * // Unpack
 * PosCapability received = PosCapability.unpack(msg.getBytes(27));
 * if (received.canReadICC()) { ... }
 * }</pre>
 *
 * @see PosDataCode
 * @see PosFlags
 * @see <a href="https://jpos.org/doc/jPOS-CMF.pdf">jPOS CMF — DE-027</a>
 */
public class PosCapability extends PosFlags {

    /** Wire length of DE-027 in bytes. */
    public static final int WIRE_LENGTH = 27;

    /** Byte offset of sub-field 27-1 (card reading capability) within the wire buffer. */
    private static final int READING_OFFSET = 0;

    /** Byte offset of sub-field 27-2 (cardholder verification capability). */
    private static final int VERIFICATION_OFFSET = 4;

    /** Byte offset of sub-field 27-3 (approval code length, 1 ASCII digit). */
    private static final int APPROVAL_CODE_LENGTH_OFFSET = 8;

    /** Byte offset of sub-field 27-4 (cardholder receipt data length, 3 ASCII digits). */
    private static final int CARDHOLDER_RECEIPT_OFFSET = 9;

    /** Byte offset of sub-field 27-5 (card acceptor receipt data length, 3 ASCII digits). */
    private static final int CARD_ACCEPTOR_RECEIPT_OFFSET = 12;

    /** Byte offset of sub-field 27-6 (cardholder display data length, 3 ASCII digits). */
    private static final int CARDHOLDER_DISPLAY_OFFSET = 15;

    /** Byte offset of sub-field 27-7 (card acceptor display data length, 3 ASCII digits). */
    private static final int CARD_ACCEPTOR_DISPLAY_OFFSET = 18;

    /** Byte offset of sub-field 27-8 (ICC scripts data length, 3 ASCII digits). */
    private static final int ICC_SCRIPT_LENGTH_OFFSET = 21;

    /** Byte offset of sub-field 27-9 (track 3 rewrite capability, 'Y'/'N'). */
    private static final int TRACK3_REWRITE_OFFSET = 24;

    /** Byte offset of sub-field 27-10 (card capture capability, 'Y'/'N'). */
    private static final int CARD_CAPTURE_OFFSET = 25;

    /** Byte offset of sub-field 27-11 (PIN input length capability, binary). */
    private static final int PIN_INPUT_LENGTH_OFFSET = 26;

    private final byte[] b;

    private PosCapability(byte[] b) {
        this.b = b;
    }

    // -------------------------------------------------------------------------
    // PosFlags contract
    // -------------------------------------------------------------------------

    /**
     * Returns the raw 27-byte wire buffer backing this instance.
     * The returned array is the live backing store; callers should not modify it.
     *
     * @return the 27-byte wire buffer
     */
    @Override
    public byte[] getBytes() {
        return b;
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    /**
     * Returns a new {@link Builder} for constructing a {@code PosCapability}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Unpacks a {@code PosCapability} from a 27-byte wire buffer.
     *
     * @param data exactly {@value #WIRE_LENGTH} bytes
     * @return the decoded {@code PosCapability}
     * @throws IllegalArgumentException if {@code data} is null or not exactly 27 bytes
     */
    public static PosCapability unpack(byte[] data) {
        if (data == null || data.length != WIRE_LENGTH)
            throw new IllegalArgumentException(
                "DE-027 wire buffer must be exactly " + WIRE_LENGTH + " bytes, got "
                + (data == null ? "null" : data.length));
        byte[] copy = new byte[WIRE_LENGTH];
        System.arraycopy(data, 0, copy, 0, WIRE_LENGTH);
        return new PosCapability(copy);
    }

    /**
     * Packs this {@code PosCapability} into a {@value #WIRE_LENGTH}-byte array
     * suitable for placing directly into DE-027 of an {@link ISOMsg}.
     *
     * @return a fresh 27-byte copy of the wire buffer
     */
    public byte[] pack() {
        byte[] copy = new byte[WIRE_LENGTH];
        System.arraycopy(b, 0, copy, 0, WIRE_LENGTH);
        return copy;
    }

    /**
     * Returns a {@link Builder} pre-populated with all values from this instance,
     * allowing selective modification.
     *
     * @return a builder initialised from this instance
     */
    public Builder toBuilder() {
        Builder bld = new Builder();
        System.arraycopy(b, 0, bld.b, 0, WIRE_LENGTH);
        return bld;
    }

    // -------------------------------------------------------------------------
    // Sub-field 27-1: card reading capability
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the terminal supports the given card reading method.
     *
     * @param method the reading method to test
     * @return {@code true} if the bit for {@code method} is set
     */
    public boolean canRead(PosDataCode.ReadingMethod method) {
        Objects.requireNonNull(method);
        int v = method.intValue();
        // method.getOffset() is 0 for ReadingMethod (its absolute PosDataCode offset)
        // READING_OFFSET is also 0, so no correction needed here — kept explicit for clarity
        int offset = READING_OFFSET;
        for (; v != 0; v >>>= 8, offset++) {
            if (offset >= READING_OFFSET + 4) break;
            if ((b[offset] & (byte) v) != (byte) v) return false;
        }
        return true;
    }

    /**
     * Returns {@code true} if the terminal can read ICC (chip) cards.
     *
     * @return {@code true} if {@link PosDataCode.ReadingMethod#ICC} is set
     */
    public boolean canReadICC() {
        return canRead(PosDataCode.ReadingMethod.ICC);
    }

    /**
     * Returns {@code true} if the terminal can read magnetic stripe cards.
     *
     * @return {@code true} if {@link PosDataCode.ReadingMethod#MAGNETIC_STRIPE} is set
     */
    public boolean canReadMagstripe() {
        return canRead(PosDataCode.ReadingMethod.MAGNETIC_STRIPE);
    }

    /**
     * Returns {@code true} if the terminal supports contactless (RFID) reading.
     *
     * @return {@code true} if {@link PosDataCode.ReadingMethod#CONTACTLESS} is set
     */
    public boolean canReadContactless() {
        return canRead(PosDataCode.ReadingMethod.CONTACTLESS);
    }

    // -------------------------------------------------------------------------
    // Sub-field 27-2: cardholder verification capability
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the terminal supports the given cardholder verification method.
     *
     * @param method the verification method to test
     * @return {@code true} if the bit for {@code method} is set
     */
    public boolean canVerify(PosDataCode.VerificationMethod method) {
        Objects.requireNonNull(method);
        int v = method.intValue();
        // method.getOffset() is 4 in PosDataCode's 16-byte buffer; here the
        // verification sub-field lives at VERIFICATION_OFFSET (byte 4), so we
        // do NOT add method.getOffset() — we read relative to VERIFICATION_OFFSET only.
        int offset = VERIFICATION_OFFSET;
        for (; v != 0; v >>>= 8, offset++) {
            if (offset >= VERIFICATION_OFFSET + 4) break;
            if ((b[offset] & (byte) v) != (byte) v) return false;
        }
        return true;
    }

    /**
     * Returns {@code true} if the terminal supports online PIN entry.
     *
     * @return {@code true} if {@link PosDataCode.VerificationMethod#ONLINE_PIN} is set
     */
    public boolean canVerifyOnlinePin() {
        return canVerify(PosDataCode.VerificationMethod.ONLINE_PIN);
    }

    /**
     * Returns {@code true} if the terminal supports offline PIN entry in clear.
     *
     * @return {@code true} if {@link PosDataCode.VerificationMethod#OFFLINE_PIN_IN_CLEAR} is set
     */
    public boolean canVerifyOfflinePinInClear() {
        return canVerify(PosDataCode.VerificationMethod.OFFLINE_PIN_IN_CLEAR);
    }

    /**
     * Returns {@code true} if the terminal supports offline encrypted PIN entry.
     *
     * @return {@code true} if {@link PosDataCode.VerificationMethod#OFFLINE_PIN_ENCRYPTED} is set
     */
    public boolean canVerifyOfflinePinEncrypted() {
        return canVerify(PosDataCode.VerificationMethod.OFFLINE_PIN_ENCRYPTED);
    }

    /**
     * Returns {@code true} if the terminal supports manual signature verification.
     *
     * @return {@code true} if {@link PosDataCode.VerificationMethod#MANUAL_SIGNATURE} is set
     */
    public boolean canVerifySignature() {
        return canVerify(PosDataCode.VerificationMethod.MANUAL_SIGNATURE);
    }

    // -------------------------------------------------------------------------
    // Sub-fields 27-3 through 27-11: scalar accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the maximum approval code length supported by this terminal (sub-field 27-3).
     *
     * @return approval code length, 0–9
     */
    public int getApprovalCodeLength() {
        return b[APPROVAL_CODE_LENGTH_OFFSET] - '0';
    }

    /**
     * Returns the maximum cardholder receipt data length in characters (sub-field 27-4).
     * A value of 0 indicates no receipt capability.
     *
     * @return cardholder receipt length, 0–999
     */
    public int getCardholderReceiptLength() {
        return readN3(CARDHOLDER_RECEIPT_OFFSET);
    }

    /**
     * Returns the maximum card acceptor (merchant) receipt data length in characters (sub-field 27-5).
     * A value of 0 indicates no receipt capability.
     *
     * @return card acceptor receipt length, 0–999
     */
    public int getCardAcceptorReceiptLength() {
        return readN3(CARD_ACCEPTOR_RECEIPT_OFFSET);
    }

    /**
     * Returns the maximum cardholder display data length in characters (sub-field 27-6).
     * A value of 0 indicates no display capability.
     *
     * @return cardholder display length, 0–999
     */
    public int getCardholderDisplayLength() {
        return readN3(CARDHOLDER_DISPLAY_OFFSET);
    }

    /**
     * Returns the maximum card acceptor display data length in characters (sub-field 27-7).
     * A value of 0 indicates no display capability.
     *
     * @return card acceptor display length, 0–999
     */
    public int getCardAcceptorDisplayLength() {
        return readN3(CARD_ACCEPTOR_DISPLAY_OFFSET);
    }

    /**
     * Returns the maximum ICC scripts data length in bytes (sub-field 27-8).
     * A value of 0 indicates no ICC script capability.
     *
     * @return ICC scripts data length, 0–999
     */
    public int getIccScriptDataLength() {
        return readN3(ICC_SCRIPT_LENGTH_OFFSET);
    }

    /**
     * Returns {@code true} if the terminal can rewrite magnetic stripe track 3 (sub-field 27-9).
     *
     * @return {@code true} if track 3 rewrite is supported
     */
    public boolean canRewriteTrack3() {
        return b[TRACK3_REWRITE_OFFSET] == 'Y';
    }

    /**
     * Returns {@code true} if the terminal can capture (retain) cards (sub-field 27-10).
     *
     * @return {@code true} if card capture is supported
     */
    public boolean canCaptureCard() {
        return b[CARD_CAPTURE_OFFSET] == 'Y';
    }

    /**
     * Returns the maximum PIN length the terminal's PIN pad can accept (sub-field 27-11).
     *
     * @return PIN input length capability, 0–255
     */
    public int getPinInputLength() {
        return b[PIN_INPUT_LENGTH_OFFSET] & 0xFF;
    }

    // -------------------------------------------------------------------------
    // Loggeable / toString
    // -------------------------------------------------------------------------

    /**
     * Dumps a human-readable representation of this {@code PosCapability} to the given stream.
     *
     * @param p      the output stream
     * @param indent indentation prefix
     */
    public void dump(PrintStream p, String indent) {
        String inner = indent + "  ";
        p.printf("%s<pos-capability value='%s'>%n", indent, ISOUtil.hexString(b));
        p.printf("%sreading-cap:     %s%n", inner, ISOUtil.hexString(b, 0, 4));
        p.printf("%sverification-cap:%s%n", inner, ISOUtil.hexString(b, 4, 4));
        p.printf("%sapproval-code-len:%d%n", inner, getApprovalCodeLength());
        p.printf("%scardholder-receipt-len:%d%n", inner, getCardholderReceiptLength());
        p.printf("%scard-acceptor-receipt-len:%d%n", inner, getCardAcceptorReceiptLength());
        p.printf("%scardholder-display-len:%d%n", inner, getCardholderDisplayLength());
        p.printf("%scard-acceptor-display-len:%d%n", inner, getCardAcceptorDisplayLength());
        p.printf("%sicc-script-len:%d%n", inner, getIccScriptDataLength());
        p.printf("%strack3-rewrite:%b%n", inner, canRewriteTrack3());
        p.printf("%scard-capture:%b%n", inner, canCaptureCard());
        p.printf("%spin-input-len:%d%n", inner, getPinInputLength());
        p.printf("%s</pos-capability>%n", indent);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private int readN3(int offset) {
        return (b[offset] - '0') * 100
             + (b[offset + 1] - '0') * 10
             + (b[offset + 2] - '0');
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /**
     * Fluent builder for {@link PosCapability}.
     *
     * <p>All numeric capacity fields default to 0 (not capable).
     * Track 3 rewrite and card capture default to {@code false}.
     * PIN input length defaults to 0.
     * Reading and verification capability bits default to all-zero (no capabilities declared).
     */
    public static final class Builder {

        private final byte[] b = new byte[WIRE_LENGTH];

        private Builder() {
            // Initialise N1 and N3 ASCII fields to '0'
            b[APPROVAL_CODE_LENGTH_OFFSET] = '0';
            for (int i = CARDHOLDER_RECEIPT_OFFSET; i < TRACK3_REWRITE_OFFSET; i++)
                b[i] = '0';
            b[TRACK3_REWRITE_OFFSET] = 'N';
            b[CARD_CAPTURE_OFFSET]   = 'N';
        }

        /**
         * Sets a card reading capability bit (sub-field 27-1).
         * Multiple capabilities may be set by calling this method repeatedly.
         *
         * @param method the reading method to declare as supported
         * @return this builder
         */
        public Builder readingCapability(PosDataCode.ReadingMethod method) {
            Objects.requireNonNull(method);
            // ReadingMethod.getOffset() == 0; READING_OFFSET == 0 — both start at byte 0
            setFlag(READING_OFFSET, method.intValue());
            return this;
        }

        /**
         * Sets a cardholder verification capability bit (sub-field 27-2).
         * Multiple capabilities may be set by calling this method repeatedly.
         *
         * @param method the verification method to declare as supported
         * @return this builder
         */
        public Builder verificationCapability(PosDataCode.VerificationMethod method) {
            Objects.requireNonNull(method);
            // method.getOffset() == 4 in PosDataCode; here the verification sub-field
            // is at VERIFICATION_OFFSET (4) — do NOT add method.getOffset()
            setFlag(VERIFICATION_OFFSET, method.intValue());
            return this;
        }

        /**
         * Sets the maximum approval code length (sub-field 27-3, N1, range 0–9).
         *
         * @param length approval code length
         * @return this builder
         * @throws IllegalArgumentException if {@code length} is outside 0–9
         */
        public Builder approvalCodeLength(int length) {
            if (length < 0 || length > 9)
                throw new IllegalArgumentException("approvalCodeLength must be 0-9, got " + length);
            b[APPROVAL_CODE_LENGTH_OFFSET] = (byte) ('0' + length);
            return this;
        }

        /**
         * Sets the maximum cardholder receipt data length (sub-field 27-4, N3, range 0–999).
         *
         * @param length receipt length; 0 = not capable
         * @return this builder
         * @throws IllegalArgumentException if {@code length} is outside 0–999
         */
        public Builder cardholderReceiptLength(int length) {
            writeN3(CARDHOLDER_RECEIPT_OFFSET, length, "cardholderReceiptLength");
            return this;
        }

        /**
         * Sets the maximum card acceptor receipt data length (sub-field 27-5, N3, range 0–999).
         *
         * @param length receipt length; 0 = not capable
         * @return this builder
         * @throws IllegalArgumentException if {@code length} is outside 0–999
         */
        public Builder cardAcceptorReceiptLength(int length) {
            writeN3(CARD_ACCEPTOR_RECEIPT_OFFSET, length, "cardAcceptorReceiptLength");
            return this;
        }

        /**
         * Sets the maximum cardholder display data length (sub-field 27-6, N3, range 0–999).
         *
         * @param length display length; 0 = not capable
         * @return this builder
         * @throws IllegalArgumentException if {@code length} is outside 0–999
         */
        public Builder cardholderDisplayLength(int length) {
            writeN3(CARDHOLDER_DISPLAY_OFFSET, length, "cardholderDisplayLength");
            return this;
        }

        /**
         * Sets the maximum card acceptor display data length (sub-field 27-7, N3, range 0–999).
         *
         * @param length display length; 0 = not capable
         * @return this builder
         * @throws IllegalArgumentException if {@code length} is outside 0–999
         */
        public Builder cardAcceptorDisplayLength(int length) {
            writeN3(CARD_ACCEPTOR_DISPLAY_OFFSET, length, "cardAcceptorDisplayLength");
            return this;
        }

        /**
         * Sets the maximum ICC scripts data length (sub-field 27-8, N3, range 0–999).
         *
         * @param length ICC script length in bytes; 0 = not capable
         * @return this builder
         * @throws IllegalArgumentException if {@code length} is outside 0–999
         */
        public Builder iccScriptDataLength(int length) {
            writeN3(ICC_SCRIPT_LENGTH_OFFSET, length, "iccScriptDataLength");
            return this;
        }

        /**
         * Sets the track 3 magnetic stripe rewrite capability (sub-field 27-9).
         *
         * @param capable {@code true} if the terminal can rewrite track 3
         * @return this builder
         */
        public Builder track3RewriteCapable(boolean capable) {
            b[TRACK3_REWRITE_OFFSET] = capable ? (byte) 'Y' : (byte) 'N';
            return this;
        }

        /**
         * Sets the card capture capability (sub-field 27-10).
         *
         * @param capable {@code true} if the terminal can capture (retain) cards
         * @return this builder
         */
        public Builder cardCaptureCapable(boolean capable) {
            b[CARD_CAPTURE_OFFSET] = capable ? (byte) 'Y' : (byte) 'N';
            return this;
        }

        /**
         * Sets the maximum PIN input length capability (sub-field 27-11, binary, range 0–255).
         *
         * @param length maximum PIN length the PIN pad accepts
         * @return this builder
         * @throws IllegalArgumentException if {@code length} is outside 0–255
         */
        public Builder pinInputLength(int length) {
            if (length < 0 || length > 255)
                throw new IllegalArgumentException("pinInputLength must be 0-255, got " + length);
            b[PIN_INPUT_LENGTH_OFFSET] = (byte) length;
            return this;
        }

        /**
         * Builds the {@link PosCapability}.
         *
         * @return a new {@code PosCapability} backed by a copy of the accumulated state
         */
        public PosCapability build() {
            byte[] copy = new byte[WIRE_LENGTH];
            System.arraycopy(b, 0, copy, 0, WIRE_LENGTH);
            return new PosCapability(copy);
        }

        // Internal helpers

        private void setFlag(int byteOffset, int flagValue) {
            for (int v = flagValue; v != 0; v >>>= 8, byteOffset++) {
                if (byteOffset < WIRE_LENGTH)
                    b[byteOffset] |= (byte) v;
            }
        }

        private void writeN3(int offset, int value, String name) {
            if (value < 0 || value > 999)
                throw new IllegalArgumentException(name + " must be 0-999, got " + value);
            b[offset]     = (byte) ('0' + value / 100);
            b[offset + 1] = (byte) ('0' + (value / 10) % 10);
            b[offset + 2] = (byte) ('0' + value % 10);
        }
    }
}
