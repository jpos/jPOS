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
 * Binary version of IFMC_LLCHAR
 * 
 * @author Robert Demski
 * @see IFMC_LLCHAR 
 */
public class IFMC_LLBINARY extends ISOTagBinaryFieldPackager {

    public IFMC_LLBINARY() {
        super(0,null, AsciiPrefixer.LL, NullPadder.INSTANCE,
                LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LL);
    }

    public IFMC_LLBINARY (int len, String description) {
         super(len, description, AsciiPrefixer.LL, NullPadder.INSTANCE,
                 LiteralBinaryInterpreter.INSTANCE, AsciiPrefixer.LL);
    }
}
