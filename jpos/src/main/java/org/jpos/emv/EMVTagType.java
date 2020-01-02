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

package org.jpos.emv;


import org.jpos.tlv.TLVDataFormat;

/**
 * @author Vishnu Pillai
 */
public interface EMVTagType {

    int getTagNumber();

    String getTagShortDescription();

    String getTagDescription();

    DataSource getSource();

    TLVDataFormat getFormat();

    DataLength getDataLength();

    ByteLength getByteLength();

    boolean isProprietaryFormat();

    String getTagNumberHex();

    byte[] getTagNumberBytes();

    Class<?> getDataType() throws ProprietaryFormatException;

    boolean isProprietaryTag();

    enum DataSource {
        ICC,
        TERMINAL,
        ISSUER
    }

    abstract class DataLength {

        public static final int DATA_LENGTH_VAR = -2;
        public static final int DATA_LENGTH_PROPRIETARY = -1;
        private int minLength;
        private int maxLength;


        public DataLength(int minLength, int maxLength) {
            this.minLength = minLength;
            this.maxLength = maxLength;
        }


        public DataLength(int minLength) {
            this.minLength = minLength;
        }

        public int getMinLength() {
            return minLength;
        }

        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

        public abstract boolean isFixedLength();
    }

    class ProprietaryDataLength extends DataLength {

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

    class FixedDataLength extends DataLength {

        public FixedDataLength(int length) {
            super(length, length);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    class VariableDataLength extends DataLength {

        public VariableDataLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return false;
        }
    }

    class VariableDiscreteDataLength extends DataLength {

        public VariableDiscreteDataLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    abstract class ByteLength {

        public static final int BYTE_LENGTH_PROPRIETARY = -1;
        public static final int BYTE_LENGTH_VAR = -2;
        private int minLength;
        private int maxLength;


        public ByteLength(int minLength, int maxLength) {
            this.minLength = minLength;
            this.maxLength = maxLength;
        }


        public ByteLength(int minLength) {
            this.minLength = minLength;
        }

        public int getMinLength() {
            return minLength;
        }

        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

        public abstract boolean isFixedLength();
    }

    class VariableDiscreteByteLength extends ByteLength {

        public VariableDiscreteByteLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    class FixedByteLength extends ByteLength {

        public FixedByteLength(int length) {
            super(length, length);
        }

        @Override
        public boolean isFixedLength() {
            return true;
        }
    }

    class VariableByteLength extends ByteLength {

        public VariableByteLength(int minLength, int maxLength) {
            super(minLength, maxLength);
        }

        @Override
        public boolean isFixedLength() {
            return false;
        }
    }
}
