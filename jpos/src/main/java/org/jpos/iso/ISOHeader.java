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

import java.io.Serializable;

/**
 * Represents the optional header portion of an ISO-8583 message frame.
 * @author Eoin.Flood@orbiscom.com
 */

public interface ISOHeader extends Cloneable,Serializable
{
    /**
     * Packs this header into a byte array.
     * @return this header serialised as a byte array
     */
    byte[] pack();

    /**
     * Unpacks the header from a raw byte array.
     * @param b raw bytes to parse
     * @return the number of bytes consumed
     */
    int unpack(byte[] b);

    /**
     * Sets the destination address in this ISOHeader.
     * @param dst the destination address
     */
    void setDestination(String dst);

    /**
     * Returns the destination address in this ISOHeader.
     * @return the destination address, or null if not set
     */
    String getDestination();

    /**
     * Sets the source address in this ISOHeader.
     * @param src the source address
     */
    void setSource(String src);

    /**
     * Returns the source address in this ISOHeader.
     * @return the source address, or null if not set
     */
    String getSource();

    /**
     * Returns the number of bytes in this ISOHeader when packed.
     * @return header length in bytes
     */
    int getLength();

    /**
     * Swaps the source and destination addresses in this ISOHeader (if they exist).
     */
    void swapDirection();

    /**
     * Returns a clone of this ISOHeader.
     * @return cloned ISOHeader instance
     */
    Object clone();
}
