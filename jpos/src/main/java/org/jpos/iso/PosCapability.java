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

package org.jpos.iso;

import java.io.PrintStream;

import org.jpos.util.Loggeable;

@SuppressWarnings("unused")
public class PosCapability extends PosFlags implements Loggeable {
    public enum ReadingCapability implements Flag {
        UNKNOWN                (1,      "Unknown"),
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
        ReadingCapability (int val, String description) {
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

    public enum VerificationCapability implements Flag {
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
        VerificationCapability (int val, String description) {
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

    private byte[] b = new byte[8];

    public PosCapability() {}

    public PosCapability (
            int readingCapability,
            int verificationCapability)
    {
        super();

        b[0]  = (byte) readingCapability;
        b[1]  = (byte) (readingCapability >>> 8);
        b[2]  = (byte) (readingCapability >>> 16);
        b[3]  = (byte) (readingCapability >>> 24);

        b[4]  = (byte) verificationCapability;
        b[5]  = (byte) (verificationCapability >>> 8);
        b[6]  = (byte) (verificationCapability >>> 16);
        b[7]  = (byte) (verificationCapability >>> 24);
    }

    private PosCapability (byte[] b) {
        if (b != null) {
            // will always use our own internal copy of array
            int copyLen= Math.min(b.length, 16);
            System.arraycopy(b, 0, this.b, 0, copyLen);
        }
    }

    public boolean hasReadingCapability (int readingMethods) {
        int i = b[3] << 24 | b[2] << 16  & 0xFF0000 | b[1] << 8  & 0xFF00 | b[0] & 0xFF ;
        return (i & readingMethods) == readingMethods;
    }
    public boolean hasReadingCapability (ReadingCapability method) {
        return hasReadingCapability (method.intValue());
    }
    public boolean hasVerificationCapability (int verificationMethods) {
        int i = b[7] << 24 | b[6] << 16 & 0xFF0000 | b[5] << 8  & 0xFF00 | b[4] & 0xFF;
        return (i & verificationMethods) == verificationMethods;
    }
    public boolean hasVerificationCapability (VerificationCapability method) {
        return hasVerificationCapability(method.intValue());
    }
    public byte[] getBytes() {
        return b;
    }
    public boolean canEMV() {
        return hasReadingCapability(ReadingCapability.ICC) || hasReadingCapability(ReadingCapability.CONTACTLESS);
    }
    public boolean canManualEntry() {
        return hasReadingCapability(ReadingCapability.PHYSICAL);
    }
    public boolean isSwiped() {
        return hasReadingCapability(ReadingCapability.MAGNETIC_STRIPE);
    }
    public String toString() {
        return super.toString() + "[" + ISOUtil.hexString (getBytes())+ "]";
    }

    public static PosCapability valueOf (byte[] b) {
        return new PosCapability(b);  // we create new objects for now, but may return cached instances in the future
    }

    public void dump(PrintStream p, String indent) {
        String inner = indent + "  ";
        StringBuilder sb = new StringBuilder();
        p.printf("%s<pvc value='%s'>%n", indent, ISOUtil.hexString(getBytes()));
        for (ReadingCapability m : ReadingCapability.values()) {
            if (hasReadingCapability(m)) {
                if (sb.length() > 0)
                    sb.append(',');
                sb.append(m.name());
            }
        }
        p.printf ("%src: %s%n", inner, sb);
        sb = new StringBuilder();
        for (VerificationCapability m : VerificationCapability.values()) {
            if (hasVerificationCapability(m)) {
                if (sb.length() > 0)
                    sb.append(',');
                sb.append(m.name());
            }
        }
        p.printf ("%svc: %s%n", inner, sb);
        p.println("</pvc>");
    }

    public void setReadingCapabilities(boolean value, ReadingCapability ... capabilities ){
        setFlags(value, capabilities);
    }

    public void unsetReadingCapabilities(ReadingCapability ... capabilities) {
        setReadingCapabilities(false, capabilities);
    }

    public void setReadingCapabilities(ReadingCapability ... capabilities) {
        setReadingCapabilities(true, capabilities);
    }

    public void setVerificationCapabilities(boolean value, VerificationCapability ... capabilities){
        setFlags(value, capabilities);
    }

    public void unsetVerificationCapabilities(VerificationCapability ... capabilities) {
        setVerificationCapabilities(false, capabilities);
    }

    public void setVerificationCapabilities(VerificationCapability ... capabilities) {
        setVerificationCapabilities(true, capabilities);
    }
}
