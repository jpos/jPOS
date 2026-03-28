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

/**
 * Mutable dataset implementation used by {@link ISODatasetField}.
 */
public class ISODataset implements Dataset {
    private final int identifier;
    private final DatasetFormat format;
    private final List<DatasetElement> elements = new ArrayList<>();

    /**
     * Creates an empty dataset.
     *
     * @param identifier dataset identifier
     * @param format dataset format
     */
    public ISODataset(int identifier, DatasetFormat format) {
        if (identifier < 0x01 || identifier > 0xFE) {
            throw new IllegalArgumentException("dataset identifier out of range");
        }
        this.identifier = identifier;
        this.format = format;
    }

    @Override
    public int getIdentifier() {
        return identifier;
    }

    @Override
    public DatasetFormat getFormat() {
        return format;
    }

    /**
     * Appends an element without replacing existing entries with the same id.
     *
     * @param element element to append
     */
    public void addElement(DatasetElement element) {
        elements.add(element);
    }

    /**
     * Replaces any existing elements with the same id and then appends the supplied element.
     *
     * @param element element to store
     */
    public void putElement(DatasetElement element) {
        elements.removeIf(existing -> existing.getId() == element.getId());
        addElement(element);
    }

    /**
     * Appends a primitive element.
     *
     * @param id element identifier
     * @param component backing component
     */
    public void addElement(int id, ISOComponent component) {
        addElement(new DatasetElement(id, component));
    }

    /**
     * Replaces any existing element with the same id.
     *
     * @param id element identifier
     * @param component backing component
     */
    public void putElement(int id, ISOComponent component) {
        putElement(new DatasetElement(id, component));
    }

    /**
     * Appends an element and records its constructed TLV flag.
     *
     * @param id element identifier
     * @param component backing component
     * @param constructed whether the tag is constructed
     */
    public void addElement(int id, ISOComponent component, boolean constructed) {
        addElement(new DatasetElement(id, component, constructed));
    }

    /**
     * Replaces any existing element with the same id and records its constructed TLV flag.
     *
     * @param id element identifier
     * @param component backing component
     * @param constructed whether the tag is constructed
     */
    public void putElement(int id, ISOComponent component, boolean constructed) {
        putElement(new DatasetElement(id, component, constructed));
    }

    /**
     * Removes all elements that match the supplied identifier.
     *
     * @param id element identifier to remove
     */
    public void removeElement(int id) {
        elements.removeIf(existing -> existing.getId() == id);
    }

    /**
     * Indicates whether the dataset contains any elements.
     *
     * @return {@code true} when empty
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Stores a character element and returns this dataset for fluent chaining.
     *
     * @param id element identifier
     * @param value element value
     * @return this dataset
     */
    public ISODataset with(int id, String value) {
        ISOField field = new ISOField(id, value);
        putElement(id, field);
        return this;
    }

    /**
     * Stores a binary element and returns this dataset for fluent chaining.
     *
     * @param id element identifier
     * @param value element value
     * @return this dataset
     */
    public ISODataset with(int id, byte[] value) {
        ISOBinaryField field = new ISOBinaryField(id, value);
        putElement(id, field);
        return this;
    }

    /**
     * Stores an ISO component and returns this dataset for fluent chaining.
     *
     * @param id element identifier
     * @param component backing component
     * @return this dataset
     */
    public ISODataset with(int id, ISOComponent component) {
        component.setFieldNumber(id);
        putElement(id, component);
        return this;
    }

    /**
     * Stores an ISO component and its constructed TLV flag, returning this dataset for fluent chaining.
     *
     * @param id element identifier
     * @param component backing component
     * @param constructed whether the tag is constructed
     * @return this dataset
     */
    public ISODataset with(int id, ISOComponent component, boolean constructed) {
        component.setFieldNumber(id);
        putElement(id, component, constructed);
        return this;
    }

    @Override
    public List<DatasetElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public List<DatasetElement> getElements(int id) {
        List<DatasetElement> matches = new ArrayList<>();
        for (DatasetElement element : elements) {
            if (element.getId() == id) {
                matches.add(element);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    @Override
    public DatasetElement getElement(int id) {
        for (DatasetElement element : elements) {
            if (element.getId() == id) {
                return element;
            }
        }
        return null;
    }

    public ISOComponent getComponent(int id) {
        DatasetElement element = getElement(id);
        return element != null ? element.getComponent() : null;
    }

    /**
     * Returns the logical value of the first matching element.
     *
     * @param id element identifier
     * @return element value or {@code null}
     * @throws ISOException on component access errors
     */
    public Object getValue(int id) throws ISOException {
        DatasetElement element = getElement(id);
        return element != null ? element.getValue() : null;
    }

    /**
     * Returns the bytes of the first matching element.
     *
     * @param id element identifier
     * @return element bytes or {@code null}
     * @throws ISOException on component access errors
     */
    public byte[] getBytes(int id) throws ISOException {
        DatasetElement element = getElement(id);
        return element != null ? element.getBytes() : null;
    }
}
