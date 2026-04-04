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

package org.jpos.iso;

import java.io.PrintStream;

import org.jpos.util.Loggeable;

/**
 * Represents POS Data Code flags as defined in jPOS/jCard specifications,
 * encoding reading method, verification method, POS environment, and security characteristics.
 */
@SuppressWarnings("unused")
public class PosDataCode extends PosFlags implements Loggeable {
    /**
     * Enumeration of card/data reading methods used at the POS terminal.
     */
    public enum ReadingMethod implements Flag {
        /** Reading method is unknown. */
        UNKNOWN                (1, "Unknown"),
        /** Contactless reading (e.g. RFID); information not taken from card. */
        CONTACTLESS            (1 << 1, "Information not taken from card"),  // i.e.: RFID
        /** Physical entry (e.g. manual entry or OCR). */
        PHYSICAL               (1 << 2, "Physical entry"),                   // i.e.: Manual Entry or OCR
        /** Bar code reading. */
        BARCODE                (1 << 3, "Bar code"),
        /** Magnetic stripe reading. */
        MAGNETIC_STRIPE        (1 << 4, "Magnetic Stripe"),
        /** ICC (chip) reading. */
        ICC                    (1 << 5, "ICC"),
        /** Data on file (no card present). */
        DATA_ON_FILE           (1 << 6, "Data on file"),
        /** ICC was read but the read failed. */
        ICC_FAILED             (1 << 11, "ICC read but failed"),
        /** Magnetic stripe was read but the read failed. */
        MAGNETIC_STRIPE_FAILED (1 << 12, "Magnetic Stripe read but failed"),
        /** Fallback from preferred reading method. */
        FALLBACK               (1 << 13, "Fallback"),
        /** Track 1 data is present (jCard private field). */
        TRACK1_PRESENT         (1 << 27, "Track1 data present"), // jCard private field
        /** Track 2 data is present (jCard private field). */
        TRACK2_PRESENT         (1 << 28, "Track2 data present"); // jCard private field

        /** Integer bitmask value for this reading method. */
        private int val;
        /** Human-readable description of this reading method. */
        private String description;
        ReadingMethod (int val, String description) {
            this.val = val;
            this.description = description;
        }
        /** {@inheritDoc} */
        public int intValue() {
            return val;
        }
        /** {@inheritDoc} */
        public String toString () {
            return description;
        }

        /** Bit offset within the PosDataCode byte array for reading method flags. */
        public static int OFFSET = 0;
        /** {@inheritDoc} */
        @Override
        public int getOffset() {
            return OFFSET;
        }
    }

    /**
     * Enumeration of cardholder verification methods used at the POS terminal.
     */
    public enum VerificationMethod implements Flag {
        /** Verification method is unknown. */
        UNKNOWN                              (1, "Unknown"),
        /** No verification performed. */
        NONE                                 (1 << 1, "None"),
        /** Cardholder verified by manual signature. */
        MANUAL_SIGNATURE                     (1 << 2, "Manual signature"),
        /** Cardholder verified by online PIN. */
        ONLINE_PIN                           (1 << 3, "Online PIN"),
        /** Cardholder verified by offline PIN in clear. */
        OFFLINE_PIN_IN_CLEAR                 (1 << 4, "Offline PIN in clear"),
        /** Cardholder verified by offline encrypted PIN. */
        OFFLINE_PIN_ENCRYPTED                (1 << 5, "Offline PIN encrypted"),
        /** Cardholder verified by offline digitized signature analysis. */
        OFFLINE_DIGITIZED_SIGNATURE_ANALYSIS (1 << 6, "Offline digitized signature analysis"),
        /** Cardholder verified by offline biometrics. */
        OFFLINE_BIOMETRICS                   (1 << 7, "Offline biometrics"),
        /** Cardholder verified by offline manual verification. */
        OFFLINE_MANUAL_VERIFICATION          (1 << 8, "Offline manual verification"),
        /** Cardholder verified by offline biographics. */
        OFFLINE_BIOGRAPHICS                  (1 << 9, "Offline biographics"),
        /** Cardholder verified by account-based digital signature. */
        ACCOUNT_BASED_DIGITAL_SIGNATURE      (1 << 10, "Account based digital signature"),
        /** Cardholder verified by public key-based digital signature. */
        PUBLIC_KEY_BASED_DIGITAL_SIGNATURE   (1 << 11, "Public key based digital signature");

        /** Integer bitmask value for this verification method. */
        private int val;
        /** Human-readable description of this verification method. */
        private String description;
        VerificationMethod (int val, String description) {
            this.val = val;
            this.description = description;
        }
        /** {@inheritDoc} */
        public int intValue() {
            return val;
        }
        /** {@inheritDoc} */
        public String toString () {
            return description;
        }

        /** Bit offset within the PosDataCode byte array for verification method flags. */
        public static int OFFSET = 4;
        /** {@inheritDoc} */
        @Override
        public int getOffset() {
            return OFFSET;
        }
    }

    /**
     * Enumeration of POS terminal environment types.
     */
    public enum POSEnvironment implements Flag {
        /** POS environment is unknown. */
        UNKNOWN                 (1, "Unknown"),
        /** Attended POS terminal. */
        ATTENDED                (1 << 1, "Attended POS"),
        /** Unattended POS terminal, details unknown. */
        UNATTENDED              (1 << 2, "Unattended, details unknown"),
        /** Mail order / telephone order transaction. */
        MOTO                    (1 << 3, "Mail order / telephone order"),
        /** E-Commerce transaction. */
        E_COMMERCE              (1 << 4, "E-Commerce"),
        /** M-Commerce (mobile commerce) transaction. */
        M_COMMERCE              (1 << 5, "M-Commerce"),
        /** Recurring transaction. */
        RECURRING               (1 << 6, "Recurring transaction"),
        /** Transaction using stored cardholder details. */
        STORED_DETAILS          (1 << 7, "Stored details"),
        /** Cardholder Activated Terminal. */
        CAT                     (1 << 8, "Cardholder Activated Terminal"),
        /** ATM located on bank premises. */
        ATM_ON_BANK             (1 << 9, "ATM on bank premises"),
        /** ATM located off bank premises. */
        ATM_OFF_BANK            (1 << 10, "ATM off bank premises"),
        /** Deferred transaction. */
        DEFERRED_TRANSACTION    (1 << 11, "Deferred transaction"),
        /** Installment transaction. */
        INSTALLMENT_TRANSACTION (1 << 12, "Installment transaction");

        /** Integer bitmask value for this POS environment. */
        private int val;
        /** Human-readable description of this POS environment. */
        private String description;
        POSEnvironment (int val, String description) {
            this.val = val;
            this.description = description;
        }
        /** {@inheritDoc} */
        public int intValue() {
            return val;
        }
        /** {@inheritDoc} */
        public String toString () {
            return description;
        }


        /** Bit offset within the PosDataCode byte array for POS environment flags. */
        public static int OFFSET = 8;
        /** {@inheritDoc} */
        @Override
        public int getOffset() {
            return OFFSET;
        }
    }

    /**
     * Enumeration of security characteristics for the transaction channel.
     */
    public enum SecurityCharacteristic implements Flag {
        /** Security characteristic is unknown. */
        UNKNOWN                                      (1, "Unknown"),
        /** Transaction carried over a private network. */
        PRIVATE_NETWORK                              (1 << 1, "Private network"),
        /** Transaction carried over an open network (Internet). */
        OPEN_NETWORK                                 (1 << 2, "Open network (Internet)"),
        /** Channel-level MACing applied. */
        CHANNEL_MACING                               (1 << 3, "Channel MACing"),
        /** Pass-through MACing applied. */
        PASS_THROUGH_MACING                          (1 << 4, "Pass through MACing"),
        /** Channel-level encryption applied. */
        CHANNEL_ENCRYPTION                           (1 << 5, "Channel encryption"),
        /** End-to-end encryption applied. */
        END_TO_END_ENCRYPTION                        (1 << 6, "End-to-end encryption"),
        /** Private algorithm encryption applied. */
        PRIVATE_ALG_ENCRYPTION                       (1 << 7, "Private algorithm encryption"),
        /** PKI encryption applied. */
        PKI_ENCRYPTION                               (1 << 8, "PKI encryption"),
        /** Private algorithm MACing applied. */
        PRIVATE_ALG_MACING                           (1 << 9, "Private algorithm MACing"),
        /** Standard algorithm MACing applied. */
        STD_ALG_MACING                               (1 << 10, "Standard algorithm MACing"),
        /** Cardholder-managed end-to-end encryption applied. */
        CARDHOLDER_MANAGED_END_TO_END_ENCRYPTION     (1 << 11, "Cardholder managed end-to-end encryption"),
        /** Cardholder-managed point-to-point encryption applied. */
        CARDHOLDER_MANAGED_POINT_TO_POINT_ENCRYPTION (1 << 12, "Cardholder managed point-to-point encryption"),
        /** Merchant-managed end-to-end encryption applied. */
        MERCHANT_MANAGED_END_TO_END_ENCRYPTION       (1 << 13, "Merchant managed end-to-end encryption"),
        /** Merchant-managed point-to-point encryption applied. */
        MERCHANT_MANAGED_POINT_TO_POINT_ENCRYPTION   (1 << 14, "Merchant managed point-to-point encryption"),
        /** Acquirer-managed end-to-end encryption applied. */
        ACQUIRER_MANAGED_END_TO_END_ENCRYPTION       (1 << 15, "Acquirer managed end-to-end-encryption"),
        /** Acquirer-managed point-to-point encryption applied. */
        ACQUIRER_MANAGED_POINT_TO_POINT_ENCRYPTION   (1 << 16, "Acquirer managed point-to-point encryption");

        /** Integer bitmask value for this security characteristic. */
        private int val;
        /** Human-readable description of this security characteristic. */
        private String description;
        SecurityCharacteristic (int val, String description) {
            this.val = val;
            this.description = description;
        }
        /** {@inheritDoc} */
        public int intValue() {
            return val;
        }
        /** {@inheritDoc} */
        public String toString () {
            return description;
        }

        /** Bit offset within the PosDataCode byte array for security characteristic flags. */
        public static int OFFSET = 12;
        /** {@inheritDoc} */
        @Override
        public int getOffset() {
            return OFFSET;
        }
    }

    /** Raw 16-byte array holding the four groups of POS data code flags. */
    private byte[] b = new byte[16];

    /** Default constructor. */
    public PosDataCode() {
    }

    /**
     * Constructs a PosDataCode with the given flag integers for each category.
     *
     * @param readingMethod        bitmask of {@link ReadingMethod} values
     * @param verificationMethod   bitmask of {@link VerificationMethod} values
     * @param posEnvironment       bitmask of {@link POSEnvironment} values
     * @param securityCharacteristic bitmask of {@link SecurityCharacteristic} values
     */
    public PosDataCode (
            int readingMethod,
            int verificationMethod,
            int posEnvironment,
            int securityCharacteristic)
    {
        super();

        b[0]  = (byte) readingMethod;
        b[1]  = (byte) (readingMethod >>> 8);
        b[2]  = (byte) (readingMethod >>> 16);
        b[3]  = (byte) (readingMethod >>> 24);

        b[4]  = (byte) verificationMethod;
        b[5]  = (byte) (verificationMethod >>> 8);
        b[6]  = (byte) (verificationMethod >>> 16);
        b[7]  = (byte) (verificationMethod >>> 24);

        b[8]  = (byte) posEnvironment;
        b[9]  = (byte) (posEnvironment >>> 8);
        b[10] = (byte) (posEnvironment >>> 16);
        b[11] = (byte) (posEnvironment >>> 24);

        b[12] = (byte) securityCharacteristic;
        b[13] = (byte) (securityCharacteristic >>> 8);
        b[14] = (byte) (securityCharacteristic >>> 16);
        b[15] = (byte) (securityCharacteristic >>> 24);
    }

    /**
     * Constructs a PosDataCode from a raw byte array.
     *
     * @param b source byte array (up to 16 bytes are copied)
     */
    private PosDataCode (byte[] b) {
        if (b != null) {
            // will always use our own internal copy of array
            int copyLen= Math.min(b.length, 16);
            System.arraycopy(b, 0, this.b, 0, copyLen);
        }
    }

    /**
     * Returns {@code true} if all specified reading method bits are set.
     *
     * @param readingMethods bitmask of reading methods to test
     * @return {@code true} if all specified bits are set
     */
    public boolean hasReadingMethods (int readingMethods) {
        int i = b[3] << 24 | b[2] << 16  & 0xFF0000 | b[1] << 8  & 0xFF00 | b[0] & 0xFF ;
        return (i & readingMethods) == readingMethods;
    }

    /**
     * Returns {@code true} if the given reading method is set.
     *
     * @param method the {@link ReadingMethod} to test
     * @return {@code true} if the reading method bit is set
     */
    public boolean hasReadingMethod (ReadingMethod method) {
        return hasReadingMethods (method.intValue());
    }

    /**
     * Returns {@code true} if all specified verification method bits are set.
     *
     * @param verificationMethods bitmask of verification methods to test
     * @return {@code true} if all specified bits are set
     */
    public boolean hasVerificationMethods (int verificationMethods) {
        int i = b[7] << 24 | b[6] << 16 & 0xFF0000 | b[5] << 8  & 0xFF00 | b[4] & 0xFF;
        return (i & verificationMethods) == verificationMethods;
    }

    /**
     * Returns {@code true} if the given verification method is set.
     *
     * @param method the {@link VerificationMethod} to test
     * @return {@code true} if the verification method bit is set
     */
    public boolean hasVerificationMethod (VerificationMethod method) {
        return hasVerificationMethods(method.intValue());
    }

    /**
     * Returns {@code true} if all specified POS environment bits are set.
     *
     * @param posEnvironments bitmask of POS environments to test
     * @return {@code true} if all specified bits are set
     */
    public boolean hasPosEnvironments (int posEnvironments) {
        int i = b[11] << 24  | b[10] << 16 & 0xFF0000 | b[9] << 8 & 0xFF00 | b[8] & 0xFF;
        return (i & posEnvironments) == posEnvironments;
    }

    /**
     * Returns {@code true} if the given POS environment is set.
     *
     * @param method the {@link POSEnvironment} to test
     * @return {@code true} if the POS environment bit is set
     */
    public boolean hasPosEnvironment (POSEnvironment method) {
        return hasPosEnvironments(method.intValue());
    }

    /**
     * Returns {@code true} if all specified security characteristic bits are set.
     *
     * @param securityCharacteristics bitmask of security characteristics to test
     * @return {@code true} if all specified bits are set
     */
    public boolean hasSecurityCharacteristics (int securityCharacteristics) {
        int i = b[15] << 24 | b[14] << 16 & 0xFF0000 | b[13] << 8  & 0xFF00 | b[12] & 0xFF;
        return (i & securityCharacteristics) == securityCharacteristics;
    }

    /**
     * Returns {@code true} if the given security characteristic is set.
     *
     * @param characteristic the {@link SecurityCharacteristic} to test
     * @return {@code true} if the security characteristic bit is set
     */
    public boolean hasSecurityCharacteristic (SecurityCharacteristic characteristic) {
        return  hasSecurityCharacteristics(characteristic.intValue());
    }

    /**
     * Returns the raw 16-byte representation of this PosDataCode.
     *
     * @return byte array holding all POS data code flags
     */
    public byte[] getBytes() {
        return b;
    }

    /**
     * Returns {@code true} if the transaction used an EMV (ICC or contactless) reading method.
     *
     * @return {@code true} if ICC or contactless reading method is set
     */
    public boolean isEMV() {
        return hasReadingMethod(ReadingMethod.ICC) || hasReadingMethod(ReadingMethod.CONTACTLESS);
    }

    /**
     * Returns {@code true} if the transaction used manual (physical) entry.
     *
     * @return {@code true} if physical reading method is set
     */
    public boolean isManualEntry() {
        return hasReadingMethod(ReadingMethod.PHYSICAL);
    }

    /**
     * Returns {@code true} if the transaction used magnetic stripe reading.
     *
     * @return {@code true} if magnetic stripe reading method is set
     */
    public boolean isSwiped() {
        return hasReadingMethod(ReadingMethod.MAGNETIC_STRIPE);
    }

    /**
     * Returns {@code true} if the transaction is a recurring transaction.
     *
     * @return {@code true} if the recurring POS environment flag is set
     */
    public boolean isRecurring() {
        return hasPosEnvironment(POSEnvironment.RECURRING);
    }

    /**
     * Returns {@code true} if the transaction is an e-commerce transaction.
     *
     * @return {@code true} if the e-commerce POS environment flag is set
     */
    public boolean isECommerce() {
        return hasPosEnvironment(POSEnvironment.E_COMMERCE);
    }

    /**
     * Returns {@code true} if the card was not physically present (e-commerce, manual entry, or recurring).
     *
     * @return {@code true} if card-not-present conditions are detected
     */
    public boolean isCardNotPresent() {
        return isECommerce() || isManualEntry() || isRecurring();
    }

    /** {@inheritDoc} */
    public String toString() {
        return super.toString() + "[" + ISOUtil.hexString (getBytes())+ "]";
    }

    /**
     * Creates a PosDataCode instance from a raw byte array.
     *
     * @param b source byte array
     * @return a new {@link PosDataCode} initialized from {@code b}
     */
    public static PosDataCode valueOf (byte[] b) {
        return new PosDataCode(b);  // we create new objects for now, but may return cached instances in the future
    }

    /**
     * Dumps a human-readable representation of this PosDataCode to the given stream.
     *
     * @param p      the output stream
     * @param indent indentation prefix string
     */
    public void dump(PrintStream p, String indent) {
        String inner = indent + "  ";
        StringBuilder sb = new StringBuilder();
        p.printf("%s<pdc value='%s'>%s%n", indent, ISOUtil.hexString(getBytes()), sb.toString());
        for (ReadingMethod m : ReadingMethod.values()) {
            if (hasReadingMethod(m)) {
                if (sb.length() > 0)
                    sb.append(',');
                sb.append(m.name());
            }
        }
        p.printf ("%srm: %s%n", inner, sb.toString());
        sb = new StringBuilder();
        for (VerificationMethod m : VerificationMethod.values()) {
            if (hasVerificationMethod(m)) {
                if (sb.length() > 0)
                    sb.append(',');
                sb.append(m.name());
            }
        }
        p.printf ("%svm: %s%n", inner, sb.toString());
        sb = new StringBuilder();
        for (POSEnvironment m : POSEnvironment.values()) {
            if (hasPosEnvironment(m)) {
                if (sb.length() > 0)
                    sb.append(',');
                sb.append(m.name());
            }
        }
        p.printf ("%spe: %s%n", inner, sb.toString());
        sb = new StringBuilder();
        for (SecurityCharacteristic m : SecurityCharacteristic.values()) {
            if (hasSecurityCharacteristic(m)) {
                if (sb.length() > 0)
                    sb.append(',');
                sb.append(m.name());
            }
        }
        p.printf ("%ssc: %s%n", inner, sb.toString());
        p.println("</pdc>");
    }

    /**
     * Sets or clears one or more reading methods.
     *
     * @param value   {@code true} to set, {@code false} to clear
     * @param methods the reading methods to modify
     */
    public void setReadingMethods(boolean value, ReadingMethod ... methods ){
        setFlags(value, methods);
    }

    /**
     * Clears one or more reading methods.
     *
     * @param methods the reading methods to clear
     */
    public void unsetReadingMethods(ReadingMethod ... methods ) {
        setReadingMethods(false, methods);
    }

    /**
     * Sets one or more reading methods.
     *
     * @param methods the reading methods to set
     */
    public void setReadingMethods(ReadingMethod ... methods ) {
        setReadingMethods(true, methods);
    }

    /**
     * Sets or clears one or more verification methods.
     *
     * @param value   {@code true} to set, {@code false} to clear
     * @param methods the verification methods to modify
     */
    public void setVerificationMethods(boolean value, VerificationMethod ... methods ){
        setFlags(value, methods);
    }

    /**
     * Clears one or more verification methods.
     *
     * @param methods the verification methods to clear
     */
    public void unsetVerificationMethods(VerificationMethod ... methods){
        setVerificationMethods(false, methods);
    }

    /**
     * Sets one or more verification methods.
     *
     * @param methods the verification methods to set
     */
    public void setVerificationMethods(VerificationMethod ... methods){
        setVerificationMethods(true, methods);
    }

    /**
     * Sets or clears one or more POS environment flags.
     *
     * @param value {@code true} to set, {@code false} to clear
     * @param envs  the POS environments to modify
     */
    public void setPOSEnvironments(boolean value, POSEnvironment ... envs){
        setFlags(value, envs);
    }

    /**
     * Clears one or more POS environment flags.
     *
     * @param envs the POS environments to clear
     */
    public void unsetPOSEnvironments(POSEnvironment ... envs){
        setPOSEnvironments(false, envs);
    }

    /**
     * Sets one or more POS environment flags.
     *
     * @param envs the POS environments to set
     */
    public void setPOSEnvironments(POSEnvironment ... envs){
        setPOSEnvironments(true, envs);
    }

    /**
     * Sets or clears one or more security characteristic flags.
     *
     * @param value                  {@code true} to set, {@code false} to clear
     * @param securityCharacteristics the security characteristics to modify
     */
    public void setSecurityCharacteristics(boolean value, SecurityCharacteristic ... securityCharacteristics){
        setFlags(value, securityCharacteristics);
    }

    /**
     * Clears one or more security characteristic flags.
     *
     * @param envs the security characteristics to clear
     */
    public void unsetSecurityCharacteristics(SecurityCharacteristic ... envs){
        setSecurityCharacteristics(false, envs);
    }

    /**
     * Sets one or more security characteristic flags.
     *
     * @param envs the security characteristics to set
     */
    public void setSecurityCharacteristics(SecurityCharacteristic ... envs){
        setSecurityCharacteristics(true, envs);
    }


}
