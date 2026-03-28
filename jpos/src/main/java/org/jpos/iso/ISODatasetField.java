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

import org.jpos.iso.packager.XMLPackager;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composite ISO field that holds one or more datasets.
 */
public class ISODatasetField extends ISOComponent {
    private int fieldNumber;
    private final List<Dataset> datasets = new ArrayList<>();

    /**
     * Creates an unbound dataset field.
     */
    public ISODatasetField() {
        this(-1);
    }

    /**
     * Creates a dataset field bound to an outer field number.
     *
     * @param fieldNumber outer field number
     */
    public ISODatasetField(int fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    /**
     * Appends a dataset to this field.
     *
     * @param dataset dataset to add
     */
    public void addDataset(Dataset dataset) {
        datasets.add(dataset);
    }

    /**
     * Removes a dataset instance from this field.
     *
     * @param dataset dataset to remove
     */
    public void removeDataset(Dataset dataset) {
        datasets.remove(dataset);
    }

    /**
     * Indicates whether this field still contains datasets.
     *
     * @return {@code true} when at least one dataset is present
     */
    public boolean hasDatasets() {
        return !datasets.isEmpty();
    }

    /**
     * Returns all datasets in insertion order.
     *
     * @return immutable list of datasets
     */
    public List<Dataset> getDatasets() {
        return Collections.unmodifiableList(datasets);
    }

    /**
     * Returns all datasets that match the supplied identifier.
     *
     * @param identifier dataset identifier
     * @return immutable list of matching datasets
     */
    public List<Dataset> getDatasets(int identifier) {
        List<Dataset> matches = new ArrayList<>();
        for (Dataset dataset : datasets) {
            if (dataset.getIdentifier() == identifier) {
                matches.add(dataset);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    /**
     * Returns the first dataset that matches the supplied identifier.
     *
     * @param identifier dataset identifier
     * @return matching dataset or {@code null}
     */
    public Dataset getDataset(int identifier) {
        for (Dataset dataset : datasets) {
            if (dataset.getIdentifier() == identifier) {
                return dataset;
            }
        }
        return null;
    }

    /**
     * Returns the component stored under the given dataset and element identifiers.
     *
     * @param datasetId dataset identifier
     * @param elementId element identifier
     * @return matching component or {@code null}
     */
    public ISOComponent get(int datasetId, int elementId) {
        Dataset dataset = getDataset(datasetId);
        if (dataset instanceof ISODataset) {
            return ((ISODataset) dataset).getComponent(elementId);
        }
        DatasetElement element = dataset != null ? dataset.getElement(elementId) : null;
        return element != null ? element.getComponent() : null;
    }

    /**
     * Returns the logical value stored under the given dataset and element identifiers.
     *
     * @param datasetId dataset identifier
     * @param elementId element identifier
     * @return element value or {@code null}
     * @throws ISOException on component access errors
     */
    public Object getValue(int datasetId, int elementId) throws ISOException {
        ISOComponent component = get(datasetId, elementId);
        return component != null ? component.getValue() : null;
    }

    /**
     * Returns the bytes stored under the given dataset and element identifiers.
     *
     * @param datasetId dataset identifier
     * @param elementId element identifier
     * @return element bytes or {@code null}
     * @throws ISOException on component access errors
     */
    public byte[] getBytes(int datasetId, int elementId) throws ISOException {
        ISOComponent component = get(datasetId, elementId);
        return component != null ? component.getBytes() : null;
    }

    /**
     * Returns this composite field.
     *
     * @return this field
     */
    @Override
    public ISOComponent getComposite() {
        return this;
    }

    /**
     * Returns the outer ISO field number.
     *
     * @return outer field number
     */
    @Override
    public Object getKey() {
        return fieldNumber;
    }

    /**
     * Returns the datasets carried by this field.
     *
     * @return dataset list
     */
    @Override
    public Object getValue() {
        return getDatasets();
    }

    /**
     * Dataset fields do not expose their bytes directly and must be packed via a
     * {@link DatasetFieldPackager}.
     *
     * @return never returns normally
     * @throws ISOException always
     */
    @Override
    public byte[] getBytes() throws ISOException {
        throw new ISOException("Dataset fields must be packed via DatasetFieldPackager");
    }

    /**
     * Sets the outer ISO field number.
     *
     * @param fieldNumber outer field number
     */
    @Override
    public void setFieldNumber(int fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    /**
     * Returns the outer ISO field number.
     *
     * @return outer field number
     */
    @Override
    public int getFieldNumber() {
        return fieldNumber;
    }

    /**
     * Replaces the datasets held by this field.
     *
     * @param obj either a {@link Dataset} or a {@link java.util.List} of datasets
     * @throws ISOException when the supplied value type is unsupported
     */
    @Override
    public void setValue(Object obj) throws ISOException {
        datasets.clear();
        if (obj instanceof Dataset) {
            datasets.add((Dataset) obj);
        } else if (obj instanceof List<?>) {
            for (Object item : (List<?>) obj) {
                if (!(item instanceof Dataset)) {
                    throw new ISOException("Invalid dataset list entry " + item);
                }
                datasets.add((Dataset) item);
            }
        } else if (obj != null) {
            throw new ISOException("Unsupported dataset field value " + obj.getClass().getName());
        }
    }

    /**
     * Dataset fields must be packed through their field packager.
     *
     * @return never returns normally
     * @throws ISOException always
     */
    @Override
    public byte[] pack() throws ISOException {
        throw new ISOException("Not available on Dataset field");
    }

    /**
     * Dataset fields must be unpacked through their field packager.
     *
     * @param b source buffer
     * @return never returns normally
     * @throws ISOException always
     */
    @Override
    public int unpack(byte[] b) throws ISOException {
        throw new ISOException("Not available on Dataset field");
    }

    /**
     * Dataset fields must be unpacked through their field packager.
     *
     * @param in source stream
     * @throws ISOException always
     */
    @Override
    public void unpack(InputStream in) throws ISOException {
        throw new ISOException("Not available on Dataset field");
    }

    /**
     * Dumps the field as dataset-aware XML.
     *
     * @param p destination stream
     * @param indent indentation prefix
     */
    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + "<" + XMLPackager.ISOFIELD_TAG + " " + XMLPackager.ID_ATTR + "=\"" + fieldNumber + "\" type=\"dataset\">");
        String innerIndent = indent + "  ";
        for (Dataset dataset : datasets) {
            p.println(innerIndent + "<dataset id=\"" + String.format("%02X", dataset.getIdentifier()) + "\" format=\"" + dataset.getFormat() + "\">");
            String datasetIndent = innerIndent + "  ";
            for (DatasetElement element : dataset.getElements()) {
                try {
                    String elementId = dataset.getFormat() == DatasetFormat.TLV
                      ? String.format("0x%X", element.getId())
                      : Integer.toString(element.getId());
                    p.println(datasetIndent
                      + "<element id=\""
                      + elementId
                      + "\""
                      + (element.isConstructed() ? " constructed=\"true\"" : "")
                      + " value=\""
                      + ISOUtil.hexString(element.getBytes())
                      + "\"/>");
                } catch (ISOException e) {
                    p.println(datasetIndent + "<element id=\"" + element.getId() + "\" error=\"" + ISOUtil.normalize(e.getMessage()) + "\"/>");
                }
            }
            p.println(innerIndent + "</dataset>");
        }
        p.println(indent + "</" + XMLPackager.ISOFIELD_TAG + ">");
    }
}
