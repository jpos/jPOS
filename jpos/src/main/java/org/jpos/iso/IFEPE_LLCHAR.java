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
 * ISOFieldPackager EBCDIC variable len CHAR suitable for MasterCard subfield 48<br>
 * <code>
 * Format TTLL....
 * Where TT is the 2 digit field number (Tag)
 *       LL is the 2 digit field length
 *       ... is the field content   
 * </code>
 * 
 * @author <a href="mailto:eoin.flood@orbiscom.com">Eoin Flood</a>
 * @author <a href="mailto:marksalter@talktalk.net">Mark Salter</a>
 * @author Robert Demski
 * 
 * @version $Id: IFEP_LLCHAR.java 2706 2009-03-05 11:24:43Z apr $
 * @see ISOComponent
 */
public class IFEPE_LLCHAR extends ISOTagStringFieldPackager {
    public IFEPE_LLCHAR() {
        super(0, null, EbcdicPrefixer.LL, NullPadder.INSTANCE,
                EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LL);
    }

    /**
     * @param len
     *            - field len
     * @param description
     *            symbolic descrption
     */
    public IFEPE_LLCHAR(int len, String description) {
        super(len, description, EbcdicPrefixer.LL, NullPadder.INSTANCE,
                EbcdicInterpreter.INSTANCE, EbcdicPrefixer.LL);
    }

}
