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

package org.jpos.tlv;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import java.util.List;
import java.util.Map;

/**
 * An ordered sequence of {@link TagValue} elements of type {@code T}.
 * @author Vishnu Pillai
 *         Date: 4/11/14

 * @param <T> the tag value type
 */
public interface TagSequence<T> extends TagValue<T> {

    /**
     * Returns all child tag values grouped by tag identifier.
     * @return map of tag identifier to list of TagValues
     */
    Map<String, List<TagValue<T>>> getChildren();

    /**
     * Adds a tag value to this sequence.
     * @param tagValue the tag value to add
     */
    void add(TagValue<T> tagValue);

    /**
     * Returns true if this sequence contains the given tag.
     * @param tag the tag identifier to look up
     * @return true if this sequence contains at least one value for the given tag
     */
    boolean hasTag(String tag);

    /**
     * Returns the first TagValue for the given tag.
     * @param tag the tag identifier
     * @return the first TagValue for the given tag, or null if absent
     */
    TagValue<T> getFirst(String tag);

    /**
     * Returns all TagValues for the given tag.
     * @param tag the tag identifier
     * @return list of TagValues for the given tag, or null if absent
     */
    List<TagValue<T>> get(String tag);

    /**
     * Returns all tag values grouped by tag identifier.
     * @return map of all tag identifier to list of TagValues
     */
    Map<String, List<TagValue<T>>> getAll();

    /**
     * Returns all tag values in insertion order.
     * @return ordered list of all TagValues in this sequence
     */
    List<TagValue<T>> getOrderedList();

    /**
     * Writes this sequence's tag values into the given ISOMsg.
     * @param isoMsg the ISOMsg to write tag values into
     * @throws ISOException on encoding error
     */
    void writeTo(ISOMsg isoMsg) throws ISOException;

    /**
     * Reads tag values from the given ISOMsg into this sequence.
     * @param isoMsg the ISOMsg to read tag values from
     * @throws ISOException on decoding error
     */
    void readFrom(ISOMsg isoMsg) throws ISOException;
}
