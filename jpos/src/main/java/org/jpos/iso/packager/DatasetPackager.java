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
import org.jpos.iso.DatasetElement;
import org.jpos.iso.DatasetFormat;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISODataset;
import org.jpos.iso.ISODatasetField;
import org.jpos.iso.ISODatasetPackager;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;
import org.jpos.tlv.TLVMsg;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.xml.sax.Attributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Packager for ISO 8583:2023 composite fields that contain one or more datasets.
 */
public class DatasetPackager extends GenericPackager implements ISODatasetPackager {
    private static final int TLV_DATASET_MAX_IDENTIFIER = 0x70;
    private static final int DATASET_ENVELOPE_SIZE = 3;
    private static final int DBM_INITIAL_BITS = 16;
    private static final int DBM_CONTINUATION_BITS = 8;

    private int fieldId;

    /**
     * Creates an empty dataset packager.
     *
     * @throws ISOException on packager initialization errors
     */
    public DatasetPackager() throws ISOException {
        super();
    }

    /**
     * Returns the outer ISO field number this dataset packager is bound to.
     *
     * @return outer field number, or {@code 0} when not initialized from XML
     */
    @Override
    public int getFieldNumber() {
        return fieldId;
    }

    /**
     * Captures the outer field id declared in the XML packager definition.
     *
     * @param atts XML attributes for the current field packager
     */
    @Override
    public void setGenericPackagerParams(Attributes atts) {
        super.setGenericPackagerParams(atts);
        fieldId = Integer.parseInt(atts.getValue("id"));
    }

    /**
     * Packs a dataset field payload, including each dataset envelope.
     *
     * @param m dataset field component
     * @return packed payload bytes
     * @throws ISOException on packing errors
     */
    @Override
    public byte[] pack(ISOComponent m) throws ISOException {
        if (!(m instanceof ISODatasetField)) {
            throw new ISOException("Can't call dataset packager on " + (m != null ? m.getClass().getName() : "null"));
        }
        LogEvent evt = new LogEvent(this, "pack");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(128)) {
            ISODatasetField field = (ISODatasetField) m;
            for (Dataset dataset : field.getDatasets()) {
                byte[] content = packDatasetContent(dataset);
                if (content.length == 0) {
                    throw new ISOException(String.format("Dataset %02X cannot be empty", dataset.getIdentifier()));
                }
                if (content.length > 0xFFFF) {
                    throw new ISOException(String.format("Dataset %02X too long: %d", dataset.getIdentifier(), content.length));
                }
                out.write(dataset.getIdentifier() & 0xFF);
                out.write((content.length >> 8) & 0xFF);
                out.write(content.length & 0xFF);
                out.write(content);
            }
            byte[] packed = out.toByteArray();
            if (logger != null) {
                evt.addMessage(ISOUtil.hexString(packed));
            }
            return packed;
        } catch (ISOException e) {
            evt.addMessage(e);
            throw e;
        } catch (Exception e) {
            evt.addMessage(e);
            throw new ISOException(e);
        } finally {
            Logger.log(evt);
        }
    }

    /**
     * Unpacks one or more datasets from a field payload.
     *
     * @param m destination dataset field
     * @param b payload bytes
     * @return number of bytes consumed
     * @throws ISOException on unpacking errors
     */
    @Override
    public int unpack(ISOComponent m, byte[] b) throws ISOException {
        if (!(m instanceof ISODatasetField)) {
            throw new ISOException("Can't call dataset packager on " + (m != null ? m.getClass().getName() : "null"));
        }
        LogEvent evt = new LogEvent(this, "unpack");
        try {
            ISODatasetField field = (ISODatasetField) m;
            int consumed = 0;
            while (consumed < b.length) {
                if (b.length - consumed < DATASET_ENVELOPE_SIZE) {
                    throw new ISOException("Truncated dataset envelope");
                }
                int identifier = b[consumed] & 0xFF;
                int length = ((b[consumed + 1] & 0xFF) << 8) | (b[consumed + 2] & 0xFF);
                consumed += DATASET_ENVELOPE_SIZE;
                if (length <= 0) {
                    throw new ISOException(String.format("Dataset %02X has invalid length %d", identifier, length));
                }
                if (consumed + length > b.length) {
                    throw new ISOException(String.format("Dataset %02X overruns composite field", identifier));
                }
                byte[] content = Arrays.copyOfRange(b, consumed, consumed + length);
                field.addDataset(unpackDataset(identifier, resolveDatasetFormat(identifier), content));
                consumed += length;
            }
            if (logger != null) {
                evt.addMessage(ISOUtil.hexString(b));
            }
            return consumed;
        } catch (ISOException e) {
            evt.addMessage(e);
            throw e;
        } catch (Exception e) {
            evt.addMessage(e);
            throw new ISOException(e);
        } finally {
            Logger.log(evt);
        }
    }

    /**
     * Unpacks one or more datasets from a stream-backed payload.
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

    /**
     * Resolves the format implied by a dataset identifier.
     *
     * @param identifier dataset identifier
     * @return inferred dataset format
     */
    protected DatasetFormat resolveDatasetFormat(int identifier) {
        return identifier <= TLV_DATASET_MAX_IDENTIFIER ? DatasetFormat.TLV : DatasetFormat.DBM;
    }

    /**
     * Packs the inner dataset payload without the outer dataset envelope.
     *
     * @param dataset dataset to encode
     * @return encoded dataset payload
     * @throws ISOException on packing errors
     */
    protected byte[] packDatasetContent(Dataset dataset) throws ISOException {
        if (dataset.getFormat() == DatasetFormat.TLV) {
            return packTLV(dataset);
        }
        return packDBM(dataset);
    }

    /**
     * Decodes a dataset payload without the outer dataset envelope.
     *
     * @param identifier dataset identifier
     * @param format dataset format
     * @param content dataset payload bytes
     * @return decoded dataset
     * @throws ISOException on decoding errors
     */
    protected Dataset unpackDataset(int identifier, DatasetFormat format, byte[] content) throws ISOException {
        return format == DatasetFormat.TLV
          ? unpackTLV(identifier, content)
          : unpackDBM(identifier, content);
    }

    /**
     * Decodes a TLV dataset payload.
     *
     * @param identifier dataset identifier
     * @param content TLV payload bytes
     * @return decoded dataset
     * @throws ISOException on decoding errors
     */
    protected ISODataset unpackTLV(int identifier, byte[] content) throws ISOException {
        TLVList tlv = new TLVList();
        try {
            tlv.unpack(content);
        } catch (RuntimeException e) {
            throw new ISOException(String.format("Invalid TLV dataset %02X", identifier), e);
        }
        ISODataset dataset = new ISODataset(identifier, DatasetFormat.TLV);
        for (TLVMsg tag : tlv.getTags()) {
            ISOBinaryField field = new ISOBinaryField(tag.getTag(), tag.getValue());
            dataset.addElement(tag.getTag(), field, isConstructedTag(tag.getTag()));
        }
        return dataset;
    }

    /**
     * Encodes a TLV dataset payload.
     *
     * @param dataset dataset to encode
     * @return TLV payload bytes
     * @throws ISOException on encoding errors
     */
    protected byte[] packTLV(Dataset dataset) throws ISOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(128)) {
            for (DatasetElement element : dataset.getElements()) {
                TLVMsg tlv = new TLVMsg(element.getId(), element.getBytes());
                out.write(tlv.getTLV());
            }
            return out.toByteArray();
        } catch (IllegalArgumentException e) {
            throw new ISOException(String.format("Unable to pack TLV dataset %02X", dataset.getIdentifier()), e);
        } catch (IOException e) {
            throw new ISOException(e);
        }
    }

    /**
     * Decodes a DBM dataset payload.
     *
     * @param identifier dataset identifier
     * @param content DBM payload bytes
     * @return decoded dataset
     * @throws ISOException on decoding errors
     */
    protected ISODataset unpackDBM(int identifier, byte[] content) throws ISOException {
        DBMBitmap dbm = unpackDBMBitmap(content);
        if (dbm.tlvContinuation) {
            throw new ISOException(String.format("Dataset %02X uses DBM TLV continuation, which is not supported yet", identifier));
        }
        ISODataset dataset = new ISODataset(identifier, DatasetFormat.DBM);
        int consumed = dbm.consumed;
        for (int elementId : dbm.elements) {
            if (elementId >= fld.length || fld[elementId] == null) {
                throw new ISOException(String.format("No packager defined for dataset %02X element %d", identifier, elementId));
            }
            ISOComponent component = fld[elementId].createComponent(elementId);
            consumed += fld[elementId].unpack(component, content, consumed);
            dataset.addElement(elementId, component);
        }
        if (consumed != content.length) {
            throw new ISOException(String.format("Dataset %02X content mismatch: consumed=%d len=%d", identifier, consumed, content.length));
        }
        return dataset;
    }

    /**
     * Encodes a DBM dataset payload.
     *
     * @param dataset dataset to encode
     * @return DBM payload bytes
     * @throws ISOException on encoding errors
     */
    protected byte[] packDBM(Dataset dataset) throws ISOException {
        List<DatasetElement> elements = dataset.getElements();
        if (elements.isEmpty()) {
            return new byte[0];
        }
        validateDBMElements(dataset);
        byte[] bitmap = packDBMBitmap(elements);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(128)) {
            out.write(bitmap);
            for (int elementId : sortedElementIds(elements)) {
                DatasetElement element = dataset.getElement(elementId);
                if (elementId >= fld.length || fld[elementId] == null) {
                    throw new ISOException(String.format("No packager defined for dataset %02X element %d", dataset.getIdentifier(), elementId));
                }
                out.write(fld[elementId].pack(element.getComponent()));
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new ISOException(e);
        }
    }

    private void validateDBMElements(Dataset dataset) throws ISOException {
        Set<Integer> seen = new TreeSet<>();
        for (DatasetElement element : dataset.getElements()) {
            if (!seen.add(element.getId())) {
                throw new ISOException(String.format("DBM dataset %02X contains duplicate element %d", dataset.getIdentifier(), element.getId()));
            }
        }
    }

    private int[] sortedElementIds(List<DatasetElement> elements) {
        return elements.stream().mapToInt(DatasetElement::getId).sorted().toArray();
    }

    private DBMBitmap unpackDBMBitmap(byte[] content) throws ISOException {
        if (content.length < 2) {
            throw new ISOException("DBM content too short");
        }
        List<byte[]> words = new ArrayList<>();
        int offset = 0;
        byte[] first = Arrays.copyOfRange(content, offset, offset + 2);
        words.add(first);
        offset += 2;

        boolean continuation = isBitSet(first[0], 1);
        while (continuation) {
            if (offset >= content.length) {
                throw new ISOException("Truncated DBM continuation");
            }
            byte[] next = new byte[] { content[offset] };
            words.add(next);
            offset++;
            continuation = isBitSet(next[0], 1);
        }

        List<Integer> elements = new ArrayList<>();
        int elementId = 1;
        for (int i = 0; i < words.size(); i++) {
            byte[] word = words.get(i);
            boolean last = i == words.size() - 1;
            int size = i == 0 ? DBM_INITIAL_BITS : DBM_CONTINUATION_BITS;
            int lastUsableBit = last ? size - 1 : size;
            int startBit = 2;
            for (int bit = startBit; bit <= lastUsableBit; bit++) {
                int byteIndex = (bit - 1) / 8;
                int bitInByte = ((bit - 1) % 8) + 1;
                if (isBitSet(word[byteIndex], bitInByte)) {
                    elements.add(elementId);
                }
                elementId++;
            }
        }

        boolean tlvContinuation = isBitSet(words.get(words.size() - 1)[words.get(words.size() - 1).length - 1], 8);
        return new DBMBitmap(elements, tlvContinuation, offset);
    }

    private byte[] packDBMBitmap(List<DatasetElement> elements) {
        int highestElement = elements.stream().mapToInt(DatasetElement::getId).max().orElse(0);
        int extraWords = 0;
        while (capacity(extraWords) < highestElement) {
            extraWords++;
        }
        byte[] bitmap = new byte[2 + extraWords];
        if (extraWords > 0) {
            setBit(bitmap, 1);
            for (int i = 2; i < bitmap.length - 1; i++) {
                bitmap[i] |= (byte) 0x80;
            }
        }
        for (DatasetElement element : elements) {
            setElementBit(bitmap, element.getId(), extraWords);
        }
        return bitmap;
    }

    private int capacity(int extraWords) {
        int capacity = extraWords == 0 ? 14 : 15;
        if (extraWords > 0) {
            capacity += (extraWords - 1) * 7;
            capacity += 6;
        }
        return capacity;
    }

    private void setElementBit(byte[] bitmap, int elementId, int extraWords) {
        if (extraWords == 0) {
            if (elementId < 1 || elementId > 14) {
                throw new IllegalArgumentException("DBM element out of range for single-word bitmap: " + elementId);
            }
            setBit(bitmap, elementId + 1);
            return;
        }
        if (elementId <= 15) {
            setBit(bitmap, elementId + 1);
            return;
        }
        int remaining = elementId - 15;
        for (int word = 1; word <= extraWords; word++) {
            int wordCapacity = word < extraWords ? 7 : 6;
            if (remaining <= wordCapacity) {
                bitmap[word + 1] |= (byte) (0x80 >> remaining);
                return;
            }
            remaining -= wordCapacity;
        }
        throw new IllegalArgumentException("DBM element out of range: " + elementId);
    }

    private void setBit(byte[] bitmap, int bitNumber) {
        int byteIndex = (bitNumber - 1) / 8;
        int shift = 7 - ((bitNumber - 1) % 8);
        bitmap[byteIndex] |= (byte) (1 << shift);
    }

    private boolean isBitSet(byte value, int bitNumber) {
        return ((value >> (8 - bitNumber)) & 0x01) == 0x01;
    }

    private boolean isConstructedTag(int tag) {
        String hexTag = Integer.toHexString(tag);
        if ((hexTag.length() & 0x01) == 1) {
            hexTag = '0' + hexTag;
        }
        byte[] tagBytes = ISOUtil.hex2byte(hexTag);
        return (tagBytes[0] & 0x20) == 0x20;
    }

    private static class DBMBitmap {
        private final List<Integer> elements;
        private final boolean tlvContinuation;
        private final int consumed;

        private DBMBitmap(List<Integer> elements, boolean tlvContinuation, int consumed) {
            this.elements = elements;
            this.tlvContinuation = tlvContinuation;
            this.consumed = consumed;
        }
    }
}
