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

import java.util.List;

/**
 * Represents one dataset instance inside an ISO 8583:2023 composite field.
 */
public interface Dataset {
    /**
     * Returns the dataset identifier as carried on the wire.
     *
     * @return dataset identifier in the range {@code 0x01} to {@code 0xFE}
     */
    int getIdentifier();

    /**
     * Returns the logical dataset encoding format.
     *
     * @return dataset format
     */
    DatasetFormat getFormat();

    /**
     * Returns all decoded elements in insertion order.
     *
     * @return immutable list of dataset elements
     */
    List<DatasetElement> getElements();

    /**
     * Returns all elements that match the supplied element identifier.
     *
     * @param id element identifier, either a TLV tag or DBM bit number
     * @return immutable list of matching elements
     */
    List<DatasetElement> getElements(int id);

    /**
     * Returns the first element that matches the supplied identifier.
     *
     * @param id element identifier, either a TLV tag or DBM bit number
     * @return matching element or {@code null} when absent
     */
    DatasetElement getElement(int id);
}
