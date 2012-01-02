/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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
    public byte[] pack();

    /**
     * Create a new ISOHeader from a byte array.
     *
     * @return The Number of bytes consumed.
     */
    public int unpack (byte[] b);

    /**
     * Set the Destination address in this ISOHeader.
     */
    public void setDestination(String dst);

    /**
     * Return the destination address in this ISOHeader.
     * returns null if there is no destination address
     */
    public String getDestination();

    /**
     * Set the Source address in this ISOHeader.
     */
    public void setSource(String src);

    /**
     * Return the source address in this ISOHeader.
     * returns null if there is no source address
     */
    public String getSource();

    /**
     * return the number of bytes in this ISOHeader
     */
    public int getLength();
    
    /**
     * Swap the source and destination addresses in this ISOHeader
     * (if they exist).
     */
    public void swapDirection();

    /**
     * Allow object to be cloned.
     */
    public Object clone();
}

