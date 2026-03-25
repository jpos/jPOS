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


import org.jpos.tlv.TLVDataFormat;

/**
 * Describes the type metadata (tag number, name, format) of an EMV data element.
 * @author Vishnu Pillai
 */
public interface EMVTagType {

    /**
     * Returns the numeric tag number.
     * @return tag number
     */
    int getTagNumber();

    /**
     * Returns the short description of this tag.
     * @return short description
     */
    String getTagShortDescription();

    /**
     * Returns the full description of this tag.
     * @return tag description
     */
    String getTagDescription();

    /**
     * Returns the data source for this tag.
     * @return data source
     */
    DataSource getSource();

    /**
     * Returns the TLV data format for this tag.
     * @return data format
     */
    TLVDataFormat getFormat();

    /**
     * Returns the data length descriptor for this tag.
     * @return data length
     */
    DataLength getDataLength();

    /**
     * Returns the byte length descriptor for this tag.
     * @return byte length
     */
    ByteLength getByteLength();

    /**
     * Returns {@code true} if this tag has a proprietary format.
     * @return true if proprietary format
     */
    boolean isProprietaryFormat();

    /**
     * Returns the tag number as an uppercase hexadecimal string.
     * @return hex tag number
     */
    String getTagNumberHex();

    /**
     * Returns the tag number as a byte array.
     * @return tag number bytes
     */
    byte[] getTagNumberBytes();

    /**
     * Returns the Java class representing the value type for this tag.
     * @return the data type class
     * @throws ProprietaryFormatException if the format is proprietary and the type is unknown
     */
    Class<?> getDataType() throws ProprietaryFormatException;

    /**
     * Returns {@code true} if this is a proprietary (non-standard) tag.
     * @return true if proprietary
     */
    boolean isProprietaryTag();

    /** The originating source of an EMV data element. */
    enum DataSource {
        /** Data element originates from the ICC (card). */
        ICC,
        /** Data element originates from the terminal. */
        TERMINAL,
        /** Data element originates from the card issuer. */
        ISSUER
    }

    /** Base class for EMV data element length descriptors (in characters/digits). */
    abstract class DataLength {

        /** Sentinel value indicating a variable length. */
        public static final int DATA_LENGTH_VAR = -2;
        /** Sentinel value indicating a proprietary length. */
        public static final int DATA_LENGTH_PROPRIETARY = -1;
        private int minLength;
        private int maxLength;

        /**
         * Creates a DataLength with separate min and max.
         * @param minLength minimum length
         * @param maxLength maximum length
         */
        public DataLength(int minLength, int maxLength) {
            this.minLength = minLength;
            this.maxLength = maxLength;
        }

        /**
         * Creates a DataLength with a single fixed-length value.
         * @param minLength the length value (used for both min and max)
         */
        public DataLength(int minLength) {
            this.minLength = minLength;
        }

        /**
         * Returns the minimum length.
         * @return minimum length
         */
        public int getMinLength() {
            return minLength;
        }

        /**
         * Sets the minimum length.
         * @param minLength the minimum length
         */
        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }

        /**
         * Returns the maximum length.
         * @return maximum length
         */
        public int getMaxLength() {
            return maxLength;
        }

        /**
         * Sets the maximum length.
         * @param maxLength the maximum length
         */
        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

        /**
         * Returns {@code true} if this is a fixed-length descriptor.
         * @return true if fixed length
         */
        public abstract boolean isFixedLength();
    }

    /** DataLength implementation for tags with a proprietary (unknown) length. */
    class ProprietaryDataLength extends DataLength {

        /** Default constructor. */
        public ProprietaryDataLength() {
            super(-1, -1);
        }

        @Override
        public int getMinLength() {
            return super.getMinLength();
        }

        @Override
        public void setMinLength(final int minLength) {
            super.setMinLength(minLength);
        }

        @Override
        public int getMaxLength() {
            return super.getMaxLength();
        }

        @Override
        public void setMaxLength(final int maxLength) {
            super.setMaxLength(maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return false;
        }
    }

    /** DataLength implementation for tags with a fixed length. */
    class FixedDataLength extends DataLength {

        /**
         * Creates a FixedDataLength with the given length.
         * @param length the fixed length
         */
        public FixedDataLength(int length) {
            super(length, length);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    /** DataLength implementation for tags with a variable length range. */
    class VariableDataLength extends DataLength {

        /**
         * Creates a VariableDataLength with the given range.
         * @param minLength minimum length
         * @param maxLength maximum length
         */
        public VariableDataLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return false;
        }
    }

    /** DataLength for tags with discrete variable-length values (treated as fixed for comparisons). */
    class VariableDiscreteDataLength extends DataLength {

        /**
         * Creates a VariableDiscreteDataLength with the given range.
         * @param minLength minimum length
         * @param maxLength maximum length
         */
        public VariableDiscreteDataLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    /** Base class for EMV data element byte-length descriptors. */
    abstract class ByteLength {

        /** Sentinel value indicating a proprietary byte length. */
        public static final int BYTE_LENGTH_PROPRIETARY = -1;
        /** Sentinel value indicating a variable byte length. */
        public static final int BYTE_LENGTH_VAR = -2;
        private int minLength;
        private int maxLength;

        /**
         * Creates a ByteLength with separate min and max.
         * @param minLength minimum byte length
         * @param maxLength maximum byte length
         */
        public ByteLength(int minLength, int maxLength) {
            this.minLength = minLength;
            this.maxLength = maxLength;
        }

        /**
         * Creates a ByteLength with a single value used for both min and max.
         * @param minLength the byte length value
         */
        public ByteLength(int minLength) {
            this.minLength = minLength;
        }

        /**
         * Returns the minimum byte length.
         * @return minimum byte length
         */
        public int getMinLength() {
            return minLength;
        }

        /**
         * Sets the minimum byte length.
         * @param minLength the minimum byte length
         */
        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }

        /**
         * Returns the maximum byte length.
         * @return maximum byte length
         */
        public int getMaxLength() {
            return maxLength;
        }

        /**
         * Sets the maximum byte length.
         * @param maxLength the maximum byte length
         */
        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

        /**
         * Returns {@code true} if this is a fixed-length descriptor.
         * @return true if fixed length
         */
        public abstract boolean isFixedLength();
    }

    /** ByteLength for tags with discrete variable byte lengths (treated as fixed for comparisons). */
    class VariableDiscreteByteLength extends ByteLength {

        /**
         * Creates a VariableDiscreteByteLength with the given range.
         * @param minLength minimum byte length
         * @param maxLength maximum byte length
         */
        public VariableDiscreteByteLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    /** ByteLength for tags with a fixed byte length. */
    class FixedByteLength extends ByteLength {

        /**
         * Creates a FixedByteLength with the given length.
         * @param length the fixed byte length
         */
        public FixedByteLength(int length) {
            super(length, length);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    /** ByteLength for tags with a variable byte length range. */
    class VariableByteLength extends ByteLength {

        /**
         * Creates a VariableByteLength with the given range.
         * @param minLength minimum byte length
         * @param maxLength maximum byte length
         */
        public VariableByteLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return false;
        }
    }
}
