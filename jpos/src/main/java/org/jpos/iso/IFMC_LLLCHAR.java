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

/**
 * Similar to Europay format, but instead of LLTT it's TTLLL
 * <code>
 * Format TTLLL....
 * Where 
 *       TT  is the 2 digit field number (Tag)
 *       LLL is the 3 digit field length
 *       ..  is the field content
 * </code>
 *
 * @author Alejandro Revilla
 * @author Robert Demski
 * @version $Id: IFMC_LLLCHAR.java 2854 2010-01-02 10:34:31Z apr $
 * @see IFEP_LLCHAR
 */
public class IFMC_LLLCHAR extends ISOTagStringFieldPackager {
    public IFMC_LLLCHAR() {
        super(0, null, AsciiPrefixer.LL, NullPadder.INSTANCE,
                AsciiInterpreter.INSTANCE, AsciiPrefixer.LLL);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFMC_LLLCHAR (int len, String description) {
        super(len, description, AsciiPrefixer.LL, NullPadder.INSTANCE,
                AsciiInterpreter.INSTANCE, AsciiPrefixer.LLL);
    }
}
