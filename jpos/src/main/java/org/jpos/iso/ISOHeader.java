/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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
 * @author Eoin.Flood@orbiscom.com
 */

public interface ISOHeader extends Cloneable,Serializable
{
    /**
     * Return this header as byte array.
     */
    byte[] pack();

    /**
     * Create a new ISOHeader from a byte array.
     *
     * @return The Number of bytes consumed.
     */
    int unpack(byte[] b);

    /**
     * Set the Destination address in this ISOHeader.
     */
    void setDestination(String dst);

    /**
     * Return the destination address in this ISOHeader.
     * returns null if there is no destination address
     */
    String getDestination();

    /**
     * Set the Source address in this ISOHeader.
     */
    void setSource(String src);

    /**
     * Return the source address in this ISOHeader.
     * returns null if there is no source address
     */
    String getSource();

    /**
     * return the number of bytes in this ISOHeader
     */
    int getLength();
    
    /**
     * Swap the source and destination addresses in this ISOHeader
     * (if they exist).
     */
    void swapDirection();

    /**
     * Allow object to be cloned.
     */
    Object clone();
}

