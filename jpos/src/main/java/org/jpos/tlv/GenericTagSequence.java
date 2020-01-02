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

package org.jpos.tlv;

import org.jpos.iso.ISOException;

/**
 * @author Vishnu Pillai
 */
public class GenericTagSequence extends TagSequenceBase {

    public GenericTagSequence() {
        super();
    }

    protected GenericTagSequence(String tag) {
        super(tag);
    }


    protected TagSequence createTagValueSequence(String tag) {
        return new GenericTagSequence(tag);
    }

    protected TagValue createLiteralTagValuePair(String tag, String value) throws ISOException {
        return new LiteralTagValue(tag, value);
    }

    protected TagValue createBinaryTagValuePair(String tag, byte[] value) throws ISOException {
        return new BinaryTagValue(tag, value);
    }

}
