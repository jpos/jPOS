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

import java.io.PrintStream;

import org.jpos.util.Loggeable;

@SuppressWarnings("unused")
public class PosDataCode implements Loggeable {

    public interface Flag {
        int getOffset();
        int intValue();
    }
    public enum ReadingMethod implements Flag {
        UNKNOWN                (1, "Unknown"),
        CONTACTLESS            (1 << 1, "Information not taken from card"),  // i.e.: RFID
        PHYSICAL               (1 << 2, "Physical entry"),                   // i.e.: Manual Entry or OCR
        BARCODE                (1 << 3, "Bar code"),
        MAGNETIC_STRIPE        (1 << 4, "Magnetic Stripe"),
        ICC                    (1 << 5, "ICC"),
        DATA_ON_FILE           (1 << 6, "Data on file"),
        ICC_FAILED             (1 << 11, "ICC read but failed"),
        MAGNETIC_STRIPE_FAILED (1 << 12, "Magnetic Stripe read but failed"),
        FALLBACK               (1 << 13, "Fallback"),
        TRACK1_PRESENT         (1 << 27, "Track1 data present"), // jCard private field
        TRACK2_PRESENT         (1 << 28, "Track2 data present"); // jCard private field

        private int val;
        private String description;
        ReadingMethod (int val, String description) {
            this.val = val;
            this.description = description;
        }
        public int intValue() {
            return val;
        }
        public String toString () {
            return description;
        }

        public static int OFFSET = 0;
        @Override
        public int getOffset() {
            return OFFSET;
        }
    }
    public enum VerificationMethod implements Flag {
        UNKNOWN                              (1, "Unknown"),
        NONE                                 (1 << 1, "None"),
        MANUAL_SIGNATURE                     (1 << 2, "Manual signature"),
        ONLINE_PIN                           (1 << 3, "Online PIN"),
        OFFLINE_PIN_IN_CLEAR                 (1 << 4, "Offline PIN in clear"),
        OFFLINE_PIN_ENCRYPTED                (1 << 5, "Offline PIN encrypted"),
        OFFLINE_DIGITIZED_SIGNATURE_ANALYSIS (1 << 6, "Offline digitized signature analysis"),
        OFFLINE_BIOMETRICS                   (1 << 7, "Offline biometrics"),
        OFFLINE_MANUAL_VERIFICATION          (1 << 8, "Offline manual verification"),
        OFFLINE_BIOGRAPHICS                  (1 << 9, "Offline biographics"),
        ACCOUNT_BASED_DIGITAL_SIGNATURE      (1 << 10, "Account based digital signature"),
        PUBLIC_KEY_BASED_DIGITAL_SIGNATURE   (1 << 11, "Public key based digital signature");

        private int val;
        private String description;
        VerificationMethod (int val, String description) {
            this.val = val;
            this.description = description;
        }
        public int intValue() {
            return val;
        }
        public String toString () {
            return description;
        }

        public static int OFFSET = 4;
        @Override
        public int getOffset() {
            return OFFSET;
        }
    }
    public enum POSEnvironment implements Flag {
        UNKNOWN                 (1, "Unknown"),
        ATTENDED                (1 << 1, "Attended POS"),
        UNATTENDED              (1 << 2, "Unattended, details unknown"),
        MOTO                    (1 << 3, "Mail order / telephone order"),
        E_COMMERCE              (1 << 4, "E-Commerce"),
        M_COMMERCE              (1 << 5, "M-Commerce"),
        RECURRING               (1 << 6, "Recurring transaction"),
        STORED_DETAILS          (1 << 7, "Stored details"),
        CAT                     (1 << 8, "Cardholder Activated Terminal"),
        ATM_ON_BANK             (1 << 9, "ATM on bank premises"),
        ATM_OFF_BANK            (1 << 10, "ATM off bank premises"),
        DEFERRED_TRANSACTION    (1 << 11, "Deferred transaction"),
        INSTALLMENT_TRANSACTION (1 << 12, "Installment transaction");

        private int val;
        private String description;
        POSEnvironment (int val, String description) {
            this.val = val;
            this.description = description;
        }
        public int intValue() {
            return val;
        }
        public String toString () {
            return description;
        }


        public static int OFFSET = 8;
        @Override
        public int getOffset() {
            return OFFSET;
        }
    }

    public enum SecurityCharacteristic implements Flag {
        UNKNOWN                                      (1, "Unknown"),
        PRIVATE_NETWORK                              (1 << 1, "Private network"),
        OPEN_NETWORK                                 (1 << 2, "Open network (Internet)"),
        CHANNEL_MACING                               (1 << 3, "Channel MACing"),
        PASS_THROUGH_MACING                          (1 << 4, "Pass through MACing"),
        CHANNEL_ENCRYPTION                           (1 << 5, "Channel encryption"),
        END_TO_END_ENCRYPTION                        (1 << 6, "End-to-end encryption"),
        PRIVATE_ALG_ENCRYPTION                       (1 << 7, "Private algorithm encryption"),
        PKI_ENCRYPTION                               (1 << 8, "PKI encryption"),
        PRIVATE_ALG_MACING                           (1 << 9, "Private algorithm MACing"),
        STD_ALG_MACING                               (1 << 10, "Standard algorithm MACing"),
        CARDHOLDER_MANAGED_END_TO_END_ENCRYPTION     (1 << 11, "Cardholder managed end-to-end encryption"),
        CARDHOLDER_MANAGED_POINT_TO_POINT_ENCRYPTION (1 << 12, "Cardholder managed point-to-point encryption"),
        MERCHANT_MANAGED_END_TO_END_ENCRYPTION       (1 << 13, "Merchant managed end-to-end encryption"),
        MERCHANT_MANAGED_POINT_TO_POINT_ENCRYPTION   (1 << 14, "Merchant managed point-to-point encryption"),
        ACQUIRER_MANAGED_END_TO_END_ENCRYPTION       (1 << 15, "Acquirer managed end-to-end-encryption"),
        ACQUIRER_MANAGED_POINT_TO_POINT_ENCRYPTION   (1 << 16, "Acquirer managed point-to-point encryption");

        private int val;
        private String description;
        SecurityCharacteristic (int val, String description) {
            this.val = val;
            this.description = description;
        }
        public int intValue() {
            return val;
        }
        public String toString () {
            return description;
        }

        public static int OFFSET = 12;
        @Override
        public int getOffset() {
            return OFFSET;
        }
    }

    private byte[] b = new byte[16];

    public PosDataCode() {
    }

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

    private PosDataCode (byte[] b) {
        if (b != null) {
            // will always use our own internal copy of array
            int copyLen= Math.min(b.length, 16);
            System.arraycopy(b, 0, this.b, 0, copyLen);
        }
    }

    public boolean hasReadingMethods (int readingMethods) {
        int i = b[3] << 24 | b[2] << 16  & 0xFF0000 | b[1] << 8  & 0xFF00 | b[0] & 0xFF ;
        return (i & readingMethods) == readingMethods;
    }
    public boolean hasReadingMethod (ReadingMethod method) {
        return hasReadingMethods (method.intValue());
    }
    public boolean hasVerificationMethods (int verificationMethods) {
        int i = b[7] << 24 | b[6] << 16 & 0xFF0000 | b[5] << 8  & 0xFF00 | b[4] & 0xFF;
        return (i & verificationMethods) == verificationMethods;
    }
    public boolean hasVerificationMethod (VerificationMethod method) {
        return hasVerificationMethods(method.intValue());
    }
    public boolean hasPosEnvironments (int posEnvironments) {
        int i = b[11] << 24  | b[10] << 16 & 0xFF0000 | b[9] << 8 & 0xFF00 | b[8] & 0xFF;
        return (i & posEnvironments) == posEnvironments;
    }
    public boolean hasPosEnvironment (POSEnvironment method) {
        return hasPosEnvironments(method.intValue());
    }
    public boolean hasSecurityCharacteristics (int securityCharacteristics) {
        int i = b[15] << 24 | b[14] << 16 & 0xFF0000 | b[13] << 8  & 0xFF00 | b[12] & 0xFF;
        return (i & securityCharacteristics) == securityCharacteristics;
    }
    public boolean hasSecurityCharacteristic (SecurityCharacteristic characteristic) {
        return  hasSecurityCharacteristics(characteristic.intValue());
    }
    public byte[] getBytes() {
        return b;
    }
    public boolean isEMV() {
        return hasReadingMethod(ReadingMethod.ICC) || hasReadingMethod(ReadingMethod.CONTACTLESS);
    }
    public boolean isManualEntry() {
        return hasReadingMethod(ReadingMethod.PHYSICAL);
    }
    public boolean isSwiped() {
        return hasReadingMethod(ReadingMethod.MAGNETIC_STRIPE);
    }
    public boolean isRecurring() {
        return hasPosEnvironment(POSEnvironment.RECURRING);
    }
    public boolean isECommerce() {
        return hasPosEnvironment(POSEnvironment.E_COMMERCE);
    }
    public String toString() {
        return super.toString() + "[" + ISOUtil.hexString (getBytes())+ "]";
    }

    public static PosDataCode valueOf (byte[] b) {
        return new PosDataCode(b);  // we create new objects for now, but may return cached instances in the future
    }

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
     * Sets or unsets a set of flags according to value
     * @param value if true flags are set, else unset
     * @param flags flag set to set or unset
     */
    public void setFlags(boolean value, Flag... flags) {
        if (value) {
            for (Flag flag  : flags) {
                for (int v = flag.intValue(), offset = flag.getOffset(); v != 0; v >>>= 8, offset++) {
                    b[offset] |= (byte) v;
                }
            }
        } else {
            for (Flag flag  : flags) {
                for (int v = flag.intValue(), offset = flag.getOffset(); v != 0; v >>>= 8, offset++) {
                    b[offset] &= (byte) ~v;
                }
            }
        }

    }

    public void setReadingMethods(boolean value, ReadingMethod ... methods ){
        setFlags(value, methods);
    }

    public void unsetReadingMethods(ReadingMethod ... methods ) {
        setReadingMethods(false, methods);
    }

    public void setReadingMethods(ReadingMethod ... methods ) {
        setReadingMethods(true, methods);
    }

    public void setVerificationMethods(boolean value, VerificationMethod ... methods ){
        setFlags(value, methods);
    }

    public void unsetVerificationMethods(VerificationMethod ... methods){
        setVerificationMethods(false, methods);
    }

    public void setVerificationMethods(VerificationMethod ... methods){
        setVerificationMethods(true, methods);
    }

    public void setPOSEnvironments(boolean value, POSEnvironment ... envs){
        setFlags(value, envs);
    }

    public void unsetPOSEnvironments(POSEnvironment ... envs){
        setPOSEnvironments(false, envs);
    }

    public void setPOSEnvironments(POSEnvironment ... envs){
        setPOSEnvironments(true, envs);
    }

    public void setSecurityCharacteristics(boolean value, SecurityCharacteristic ... securityCharacteristics){
        setFlags(value, securityCharacteristics);
    }

    public void unsetSecurityCharacteristics(SecurityCharacteristic ... envs){
        setSecurityCharacteristics(false, envs);
    }

    public void setSecurityCharacteristics(SecurityCharacteristic ... envs){
        setSecurityCharacteristics(true, envs);
    }


}
