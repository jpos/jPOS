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

/*
 * vim:set ts=8 sw=4:
 */

/**
 * ISOFieldPackager EBCDIC variable len CHAR suitable for MasterCard subfield 112<br>
 * <code>
 * Format TTTLL....
 * Where TTT is the 3 digit field number (Tag)
 *       LLL is the 3 digit field length
 *       ... is the field content   
 * </code>
 * 
 * @author <a href="mailto:fernandoluizjr@gmail.com">Fernando Amaral</a>
 * @author Fernando Amaral
 * 
 * @version $Id: IFEPE_LLLCHAR.java 2706 2016-03-12 19:20:00Z apr $
 * @see ISOComponent
 */
public class IFEPE_LLLCHAR extends ISOTagStringFieldPackager {
    public IFEPE_LLLCHAR() {
        super(0, null, EbcdicPrefixer.LLL, NullPadder.INSTANCE,
                EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LLL);
    }

    /**
     * @param len
     *            - field len
     * @param description
     *            symbolic descrption
     */
    public IFEPE_LLLCHAR(int len, String description) {
        super(len, description, EbcdicPrefixer.LLL, NullPadder.INSTANCE,
                EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LLL);
    }

}
