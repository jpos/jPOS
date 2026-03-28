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

import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps a regular binary field packager so its payload can be exposed as an
 * {@link ISODatasetField}.
 */
public class DatasetFieldPackager extends ISOFieldPackager {
    protected ISODatasetPackager datasetPackager;
    protected ISOFieldPackager fieldPackager;

    /**
     * Creates a dataset-aware field packager.
     *
     * @param fieldPackager outer field packager used for length and transport encoding
     * @param datasetPackager inner dataset payload packager
     */
    public DatasetFieldPackager(ISOFieldPackager fieldPackager, ISODatasetPackager datasetPackager) {
        super(fieldPackager.getLength(), fieldPackager.getDescription());
        this.datasetPackager = datasetPackager;
        this.fieldPackager = fieldPackager;
    }

    /**
     * Packs a dataset field by first encoding the inner dataset payload and
     * then delegating the outer field framing to the wrapped field packager.
     *
     * @param c field component to pack
     * @return packed field bytes
     * @throws ISOException on packing errors
     */
    @Override
    public byte[] pack(ISOComponent c) throws ISOException {
        if (c instanceof ISODatasetField) {
            ISODatasetField datasetField = (ISODatasetField) c;
            int fieldNumber = datasetField.getFieldNumber() >= 0 ? datasetField.getFieldNumber() : 0;
            ISOBinaryField field = new ISOBinaryField(fieldNumber, datasetPackager.pack(datasetField));
            if (datasetPackager.getFieldNumber() > -1) {
                field.setFieldNumber(datasetPackager.getFieldNumber());
            }
            return fieldPackager.pack(field);
        }
        return fieldPackager.pack(c);
    }

    /**
     * Unpacks a dataset field from a byte array.
     *
     * @param c destination component
     * @param b source buffer
     * @param offset starting offset
     * @return number of bytes consumed from the outer field
     * @throws ISOException on unpacking errors
     */
    @Override
    public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
        ISOBinaryField field = new ISOBinaryField(0);
        if (datasetPackager.getFieldNumber() > -1) {
            field.setFieldNumber(datasetPackager.getFieldNumber());
        }
        int consumed = fieldPackager.unpack(field, b, offset);
        if (field.getValue() != null && c instanceof ISODatasetField) {
            datasetPackager.unpack(c, (byte[]) field.getValue());
        }
        return consumed;
    }

    /**
     * Unpacks a dataset field from a stream.
     *
     * @param c destination component
     * @param in source stream
     * @throws IOException on stream errors
     * @throws ISOException on unpacking errors
     */
    @Override
    public void unpack(ISOComponent c, InputStream in) throws IOException, ISOException {
        ISOBinaryField field = new ISOBinaryField(0);
        if (datasetPackager.getFieldNumber() > -1) {
            field.setFieldNumber(datasetPackager.getFieldNumber());
        }
        fieldPackager.unpack(field, in);
        if (field.getValue() != null && c instanceof ISODatasetField) {
            datasetPackager.unpack(c, (byte[]) field.getValue());
        }
    }

    /**
     * Creates an {@link ISODatasetField} for the supplied field number.
     *
     * @param fieldNumber outer field number
     * @return dataset field component
     */
    @Override
    public ISOComponent createComponent(int fieldNumber) {
        return new ISODatasetField(fieldNumber);
    }

    /**
     * Returns the maximum packed length of the outer field.
     *
     * @return maximum packed length
     */
    @Override
    public int getMaxPackedLength() {
        return fieldPackager.getLength();
    }

    /**
     * Returns the inner dataset payload packager.
     *
     * @return dataset packager
     */
    public ISODatasetPackager getISODatasetPackager() {
        return datasetPackager;
    }

    /**
     * Returns the wrapped outer field packager.
     *
     * @return outer field packager
     */
    public ISOFieldPackager getISOFieldPackager() {
        return fieldPackager;
    }
}
