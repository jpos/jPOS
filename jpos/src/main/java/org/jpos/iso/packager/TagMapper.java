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

/**
 * Maps between field IDs and tag names for tagged (TLV-style) ISO fields.
 */
public interface TagMapper {

    /**
     * Returns the tag string for the given field and sub-field number.
     * @param fieldNumber the parent field number
     * @param subFieldNumber the sub-field number
     * @return the tag string
     */
    String getTagForField(int fieldNumber, int subFieldNumber);

    /**
     * Returns the sub-field number for the given field number and tag string.
     * @param fieldNumber the parent field number
     * @param tag the tag string
     * @return the sub-field number, or {@code null} if not found
     */
    Integer getFieldNumberForTag(int fieldNumber, String tag);

}
