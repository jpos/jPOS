/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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
 * EBCDIC version of IFMC_LLBINARY, where Tag (TT) and Length (LL) are EBCDIC.
 */
public class IFEPE_LLBINARY extends ISOTagBinaryFieldPackager {

    public IFEPE_LLBINARY() {
        super(0,null, EbcdicPrefixer.LL, NullPadder.INSTANCE,
                LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LL);
    }

    public IFEPE_LLBINARY(int len, String description) {
         super(len, description, EbcdicPrefixer.LL, NullPadder.INSTANCE,
                 LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LL);
    }
}
