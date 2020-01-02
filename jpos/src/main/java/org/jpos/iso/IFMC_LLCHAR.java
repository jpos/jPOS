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
 * Similar to Europay format, but instead of LLTT it's TTLL
 * <code>
 * Format TTLL....
 * Where 
 *       TT is the 2 digit field number (Tag)
 *       LL is the 2 digit field length
 *       .. is the field content   
 * </code>
 *
 * @author Alejandro Revilla
 * @author Robert Demski
 * @version $Id$
 * @see IFEP_LLCHAR
 */
public class IFMC_LLCHAR extends ISOTagStringFieldPackager {
    public IFMC_LLCHAR() {
        super(0, null, AsciiPrefixer.LL, NullPadder.INSTANCE,
                AsciiInterpreter.INSTANCE, AsciiPrefixer.LL);
    }
    /**
     * @param len - field len
     * @param description symbolic descrption
     */
    public IFMC_LLCHAR (int len, String description) {
        super(len, description, AsciiPrefixer.LL, NullPadder.INSTANCE,
                AsciiInterpreter.INSTANCE, AsciiPrefixer.LL);
    }
}
