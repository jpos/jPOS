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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Encodes and decodes the ISO 8583 Message Error Indicator (DE-018).
 *
 * <p>DE-018 carries up to ten error sets, each exactly 14 positions long.
 * Each set identifies the type of error and the precise location within the
 * message (data element, sub-element for constructed fields, or dataset
 * identifier plus bit/tag for composite fields).</p>
 *
 * <p>Error sets are concatenated without separators. The overall field is
 * transmitted as an {@code LLLVAR} character field.</p>
 *
 * <h2>Error set wire layout</h2>
 * <pre>
 *  Pos  Len  Type        Subfield
 *  1–2    2  N2 (ASCII)  Error severity
 *  3–6    4  N4 (ASCII)  Message error code
 *  7–9    3  N3 (ASCII)  Data element in error (001–128)
 * 10–11   2  N2 (ASCII)  Data sub-element in error (constructed DEs), else "00"
 *   12    1  B1 (binary) Dataset identifier (composite DEs), else 0x00
 * 13–14   2  B2 (binary) Dataset bit or TLV tag (composite DEs), else 0x0000
 * </pre>
 *
 * <p>Usage:</p>
 * <pre>
 * MessageErrorIndicator mei = new MessageErrorIndicator()
 *     .add(FieldError.primitiveError(ErrorCode.REQUIRED_MISSING, 37))
 *     .add(FieldError.compositeError(ErrorCode.INVALID_VALUE, 55, 0x37, 0x9F26));
 *
 * msg.set(18, new String(mei.pack(), ISOUtil.CHARSET));
 *
 * // or
 *
 * MessageErrorIndicator parsed = MessageErrorIndicator.unpack(msg.getBytes(18));
 * </pre>
 *
 * @see <a href="https://jpos.org/doc/jPOS-CMF.pdf">jPOS CMF Specification — DE-018</a>
 */
public class MessageErrorIndicator {

    /** Maximum number of error sets per DE-018 field. */
    public static final int MAX_ERROR_SETS = 10;

    /** Length in bytes of one error set on the wire. */
    public static final int ERROR_SET_LENGTH = 14;

    private final List<FieldError> errors = new ArrayList<>();

    /**
     * Creates an empty indicator.
     */
    public MessageErrorIndicator() {
    }

    /**
     * Appends an error set.
     *
     * @param error error set to add
     * @return this indicator for fluent chaining
     * @throws IllegalStateException when the maximum of 10 error sets has been reached
     */
    public MessageErrorIndicator add(FieldError error) {
        if (errors.size() >= MAX_ERROR_SETS) {
            throw new IllegalStateException("DE-018 may carry at most " + MAX_ERROR_SETS + " error sets");
        }
        Objects.requireNonNull(error, "error cannot be null");
        errors.add(error);
        return this;
    }

    /**
     * Returns an unmodifiable view of the current error sets.
     *
     * @return error set list
     */
    public List<FieldError> errors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Indicates whether this indicator contains no error sets.
     *
     * @return {@code true} when empty
     */
    public boolean isEmpty() {
        return errors.isEmpty();
    }

    /**
     * Returns the number of error sets.
     *
     * @return error set count
     */
    public int size() {
        return errors.size();
    }

    /**
     * Serializes all error sets to the DE-018 wire format.
     *
     * @return packed bytes suitable for setting on DE-018 of an ISOMsg
     */
    public byte[] pack() {
        byte[] result = new byte[errors.size() * ERROR_SET_LENGTH];
        int offset = 0;
        for (FieldError error : errors) {
            byte[] set = error.pack();
            System.arraycopy(set, 0, result, offset, ERROR_SET_LENGTH);
            offset += ERROR_SET_LENGTH;
        }
        return result;
    }

    /**
     * Deserializes DE-018 wire bytes into a {@code MessageErrorIndicator}.
     *
     * @param data DE-018 field bytes (must be a multiple of 14)
     * @return parsed indicator
     * @throws ISOException when the byte array is malformed
     */
    public static MessageErrorIndicator unpack(byte[] data) throws ISOException {
        if (data == null || data.length == 0) {
            return new MessageErrorIndicator();
        }
        if (data.length % ERROR_SET_LENGTH != 0) {
            throw new ISOException("DE-018 length " + data.length
                    + " is not a multiple of " + ERROR_SET_LENGTH);
        }
        int count = data.length / ERROR_SET_LENGTH;
        if (count > MAX_ERROR_SETS) {
            throw new ISOException("DE-018 contains " + count
                    + " error sets, maximum is " + MAX_ERROR_SETS);
        }
        MessageErrorIndicator mei = new MessageErrorIndicator();
        for (int i = 0; i < count; i++) {
            int off = i * ERROR_SET_LENGTH;
            mei.errors.add(FieldError.unpack(data, off));
        }
        return mei;
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Standard message error codes defined by ISO 8583:2023, Table D.15.
     *
     * <p>Codes 0014–3999 are reserved for ISO use; 4000–5999 for national use;
     * 6000–9999 for private use.</p>
     */
    public enum ErrorCode {
        /** Required data element is missing. */
        REQUIRED_MISSING(1),
        /** Data element length is invalid. */
        INVALID_LENGTH(2),
        /** Data element contains an invalid value. */
        INVALID_VALUE(3),
        /** Amount field has a format error. */
        AMOUNT_FORMAT(4),
        /** Date field has a format error. */
        DATE_FORMAT(5),
        /** Account identifier has a format error. */
        ACCOUNT_FORMAT(6),
        /** Name field has a format error. */
        NAME_FORMAT(7),
        /** Other format error. */
        FORMAT_OTHER(8),
        /** Data inconsistent with POS data code. */
        INCONSISTENT_WITH_POS_CODE(9),
        /** Data does not match the original request. */
        INCONSISTENT_WITH_ORIGINAL(10),
        /** Other inconsistent data. */
        INCONSISTENT_OTHER(11),
        /** Recurring data error. */
        RECURRING_DATA(12),
        /** Customer vendor format error. */
        CUSTOMER_VENDOR_FORMAT(13);

        private final int code;

        ErrorCode(int code) {
            this.code = code;
        }

        /**
         * Returns the 4-digit numeric code as carried on the wire.
         *
         * @return numeric code
         */
        public int code() {
            return code;
        }

        /**
         * Returns the error code as a 4-character left-zero-padded ASCII string.
         *
         * @return wire-format code string
         */
        public String codeString() {
            return String.format("%04d", code);
        }

        /**
         * Resolves an {@link ErrorCode} from its numeric value.
         *
         * @param code numeric code
         * @return matching constant or {@code null} when not in the ISO-defined range
         */
        public static ErrorCode of(int code) {
            for (ErrorCode ec : values()) {
                if (ec.code == code) {
                    return ec;
                }
            }
            return null;
        }
    }

    /**
     * Error severity carried in positions 1–2 of each error set.
     */
    public enum Severity {
        /** The message was rejected due to this error. */
        REJECTED(0),
        /** The message was accepted but contains a non-critical error. */
        WARNING(1);

        private final int value;

        Severity(int value) {
            this.value = value;
        }

        /**
         * Returns the 2-digit numeric value as carried on the wire.
         *
         * @return numeric severity value
         */
        public int value() {
            return value;
        }

        /**
         * Resolves a {@link Severity} from its numeric value.
         *
         * @param value 0 or 1
         * @return matching constant, defaulting to {@link #REJECTED} for unknown values
         */
        public static Severity of(int value) {
            for (Severity s : values()) {
                if (s.value == value) {
                    return s;
                }
            }
            return REJECTED;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * One error set within a {@link MessageErrorIndicator} field.
     *
     * <p>The wire layout is exactly 14 bytes:</p>
     * <ul>
     *   <li>2 ASCII digits — severity</li>
     *   <li>4 ASCII digits — message error code</li>
     *   <li>3 ASCII digits — data element number (001–128)</li>
     *   <li>2 ASCII digits — sub-element (constructed DEs) or "00"</li>
     *   <li>1 binary byte — dataset identifier (composite DEs) or 0x00</li>
     *   <li>2 binary bytes — dataset bit or TLV tag (composite DEs) or 0x0000</li>
     * </ul>
     */
    public static class FieldError {

        private final Severity severity;
        private final int errorCode;
        private final int deNumber;
        private final int subElement;
        private final int datasetIdentifier;
        private final int datasetBitOrTag;

        private FieldError(Severity severity, int errorCode, int deNumber,
                           int subElement, int datasetIdentifier, int datasetBitOrTag) {
            if (deNumber < 1 || deNumber > 128) {
                throw new IllegalArgumentException("deNumber must be 1–128");
            }
            this.severity = severity;
            this.errorCode = errorCode;
            this.deNumber = deNumber;
            this.subElement = subElement;
            this.datasetIdentifier = datasetIdentifier;
            this.datasetBitOrTag = datasetBitOrTag;
        }

        /**
         * Creates an error set for a primitive data element.
         *
         * @param errorCode   error code
         * @param deNumber    data element number (1–128)
         * @return new error set
         */
        public static FieldError primitiveError(ErrorCode errorCode, int deNumber) {
            return new FieldError(Severity.REJECTED, errorCode.code(), deNumber, 0, 0, 0);
        }

        /**
         * Creates an error set for a primitive data element with a specified severity.
         *
         * @param severity    error severity
         * @param errorCode   error code
         * @param deNumber    data element number (1–128)
         * @return new error set
         */
        public static FieldError primitiveError(Severity severity, ErrorCode errorCode, int deNumber) {
            return new FieldError(severity, errorCode.code(), deNumber, 0, 0, 0);
        }

        /**
         * Creates an error set for a sub-element within a constructed data element.
         *
         * @param errorCode   error code
         * @param deNumber    data element number (1–128)
         * @param subElement  sub-element part number (1-based)
         * @return new error set
         */
        public static FieldError constructedError(ErrorCode errorCode, int deNumber, int subElement) {
            return new FieldError(Severity.REJECTED, errorCode.code(), deNumber, subElement, 0, 0);
        }

        /**
         * Creates an error set for a sub-element within a composite (dataset) data element.
         *
         * @param errorCode         error code
         * @param deNumber          data element number (1–128)
         * @param datasetIdentifier dataset identifier byte (0x01–0xFE)
         * @param datasetBitOrTag   DBM bit number or BER-TLV tag (packed into 2 bytes big-endian)
         * @return new error set
         */
        public static FieldError compositeError(ErrorCode errorCode, int deNumber,
                                                int datasetIdentifier, int datasetBitOrTag) {
            return new FieldError(Severity.REJECTED, errorCode.code(), deNumber, 0,
                    datasetIdentifier, datasetBitOrTag);
        }

        /**
         * Creates an error set with a raw numeric error code (for private-use or
         * national-use codes outside the ISO-defined {@link ErrorCode} enum).
         *
         * @param severity    error severity
         * @param errorCode   raw 4-digit error code (0001–9999)
         * @param deNumber    data element number (1–128)
         * @return new error set
         */
        public static FieldError withRawCode(Severity severity, int errorCode, int deNumber) {
            return new FieldError(severity, errorCode, deNumber, 0, 0, 0);
        }

        /** @return error severity */
        public Severity severity() { return severity; }

        /** @return message error code numeric value */
        public int errorCode() { return errorCode; }

        /** @return resolved {@link ErrorCode} or {@code null} for private/national-use codes */
        public ErrorCode errorCodeEnum() { return ErrorCode.of(errorCode); }

        /** @return data element number in error */
        public int deNumber() { return deNumber; }

        /** @return sub-element number (constructed DEs), 0 otherwise */
        public int subElement() { return subElement; }

        /** @return dataset identifier (composite DEs), 0 otherwise */
        public int datasetIdentifier() { return datasetIdentifier; }

        /** @return dataset bit number or TLV tag (composite DEs), 0 otherwise */
        public int datasetBitOrTag() { return datasetBitOrTag; }

        /**
         * Serializes this error set to exactly 14 bytes.
         *
         * @return 14-byte wire representation
         */
        public byte[] pack() {
            byte[] buf = new byte[ERROR_SET_LENGTH];
            // Severity: 2 ASCII digits
            buf[0] = (byte) ('0' + (severity.value() / 10));
            buf[1] = (byte) ('0' + (severity.value() % 10));
            // Error code: 4 ASCII digits
            String codeStr = String.format("%04d", errorCode);
            buf[2] = (byte) codeStr.charAt(0);
            buf[3] = (byte) codeStr.charAt(1);
            buf[4] = (byte) codeStr.charAt(2);
            buf[5] = (byte) codeStr.charAt(3);
            // DE number: 3 ASCII digits
            String deStr = String.format("%03d", deNumber);
            buf[6] = (byte) deStr.charAt(0);
            buf[7] = (byte) deStr.charAt(1);
            buf[8] = (byte) deStr.charAt(2);
            // Sub-element: 2 ASCII digits
            String seStr = String.format("%02d", subElement);
            buf[9]  = (byte) seStr.charAt(0);
            buf[10] = (byte) seStr.charAt(1);
            // Dataset identifier: 1 binary byte
            buf[11] = (byte) (datasetIdentifier & 0xFF);
            // Dataset bit/tag: 2 binary bytes big-endian
            buf[12] = (byte) ((datasetBitOrTag >> 8) & 0xFF);
            buf[13] = (byte) (datasetBitOrTag & 0xFF);
            return buf;
        }

        /**
         * Deserializes one error set from 14 bytes at the given offset.
         *
         * @param data   source buffer
         * @param offset starting offset
         * @return parsed error set
         * @throws ISOException on malformed data
         */
        static FieldError unpack(byte[] data, int offset) throws ISOException {
            if (data.length - offset < ERROR_SET_LENGTH) {
                throw new ISOException("Insufficient data for DE-018 error set at offset " + offset);
            }
            try {
                int sev      = Integer.parseInt(new String(data, offset, 2));
                int code     = Integer.parseInt(new String(data, offset + 2, 4));
                int deNum    = Integer.parseInt(new String(data, offset + 6, 3));
                int subEl    = Integer.parseInt(new String(data, offset + 9, 2));
                int dsId     = data[offset + 11] & 0xFF;
                int bitOrTag = ((data[offset + 12] & 0xFF) << 8) | (data[offset + 13] & 0xFF);
                return new FieldError(Severity.of(sev), code, deNum, subEl, dsId, bitOrTag);
            } catch (NumberFormatException e) {
                throw new ISOException("Malformed DE-018 error set at offset " + offset, e);
            }
        }

        @Override
        public String toString() {
            ErrorCode ec = errorCodeEnum();
            return "FieldError{sev=" + severity
                    + ", code=" + String.format("%04d", errorCode)
                    + (ec != null ? "(" + ec.name() + ")" : "")
                    + ", de=" + String.format("%03d", deNumber)
                    + (subElement != 0 ? ", sub=" + subElement : "")
                    + (datasetIdentifier != 0 ? String.format(", dsId=0x%02X", datasetIdentifier) : "")
                    + (datasetBitOrTag != 0 ? String.format(", bitOrTag=0x%04X", datasetBitOrTag) : "")
                    + "}";
        }
    }
}
