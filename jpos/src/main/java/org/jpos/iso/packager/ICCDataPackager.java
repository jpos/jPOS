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

package org.jpos.iso.packager;

import org.jpos.iso.Dataset;
import org.jpos.iso.DatasetFormat;
import org.jpos.iso.ISODatasetField;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISODataset;
import org.jpos.iso.ISOException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Special-case packager for DE 055 ICC data, which carries raw BER-TLV without
 * the standard dataset identifier and length envelope.
 */
public class ICCDataPackager extends DatasetPackager {
    /**
     * Creates an ICC data packager.
     *
     * @throws ISOException on packager initialization errors
     */
    public ICCDataPackager() throws ISOException {
        super();
    }

    /**
     * ICC data is encoded as raw BER-TLV, without the standard dataset
     * identifier and length envelope.
     *
     * @return always {@code false}
     */
    @Override
    public boolean hasDatasetEnvelope() {
        return false;
    }

    /**
     * Packs raw ICC BER-TLV bytes without the standard dataset envelope.
     *
     * @param m ICC dataset field
     * @return packed BER-TLV bytes
     * @throws ISOException on packing errors
     */
    @Override
    public byte[] pack(ISOComponent m) throws ISOException {
        if (!(m instanceof ISODatasetField)) {
            throw new ISOException("Can't call ICC data packager on " + (m != null ? m.getClass().getName() : "null"));
        }
        ISODatasetField field = (ISODatasetField) m;
        Dataset dataset = field.getDataset(getFieldNumber());
        if (dataset == null) {
            if (field.getDatasets().isEmpty()) {
                return new byte[0];
            }
            dataset = field.getDatasets().get(0);
        }
        if (dataset.getFormat() != DatasetFormat.TLV) {
            throw new ISOException("ICC data is BER-TLV only");
        }
        return packDatasetContent(dataset);
    }

    /**
     * Unpacks raw ICC BER-TLV bytes into a synthetic dataset keyed by the outer field number.
     *
     * @param m destination dataset field
     * @param b BER-TLV bytes
     * @return number of bytes consumed
     * @throws ISOException on unpacking errors
     */
    @Override
    public int unpack(ISOComponent m, byte[] b) throws ISOException {
        if (!(m instanceof ISODatasetField)) {
            throw new ISOException("Can't call ICC data packager on " + (m != null ? m.getClass().getName() : "null"));
        }
        ISODataset dataset = unpackTLV(getFieldNumber(), b);
        ((ISODatasetField) m).addDataset(dataset);
        return b.length;
    }

    /**
     * Unpacks raw ICC BER-TLV bytes from a stream.
     *
     * @param m destination dataset field
     * @param in source stream
     * @throws IOException on stream errors
     * @throws ISOException on unpacking errors
     */
    @Override
    public void unpack(ISOComponent m, InputStream in) throws IOException, ISOException {
        unpack(m, in.readAllBytes());
    }
}
