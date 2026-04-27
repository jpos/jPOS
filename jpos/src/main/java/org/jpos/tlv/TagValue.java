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

/**
 * A typed tag-value pair used in TLV (Tag-Length-Value) processing.
 * @author Vishnu Pillai

 * @param <T> the tag value type
 */
public interface TagValue<T> {

    /**
     * Returns the tag identifier.
     * @return tag string
     */
    String getTag();

    /**
     * Returns the tag value.
     * @return the value
     * @throws ISOException on error
     */
    T getValue() throws ISOException;

    /**
     * Returns {@code true} if this tag-value is composite (contains nested TLV data).
     * @return true if composite
     */
    boolean isComposite();

}
