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

package org.jpos.emv;

import org.jpos.iso.ISOException;
import org.jpos.tlv.TagSequenceBase;

/**
 * @author Vishnu Pillai
 */
public class EMVTagSequence extends TagSequenceBase {

    public EMVTagSequence() {
        super();
    }

    protected EMVTagSequence(String tag) {
        super(tag);
    }

    @Override
    protected EMVTagSequence createTagValueSequence(String tag) {
        return new EMVTagSequence(tag);
    }

    @Override
    protected EMVTag createLiteralTagValuePair(String tag, String value) throws ISOException {
        try {
            return new LiteralEMVTag(EMVStandardTagType.forHexCode(tag), value);
        } catch (UnknownTagNumberException e) {
            throw new ISOException(e);
        }
    }

    @Override
    protected EMVTag createBinaryTagValuePair(String tag, byte[] value) throws ISOException {
        try {
            return new BinaryEMVTag(EMVStandardTagType.forHexCode(tag), value);
        } catch (UnknownTagNumberException e) {
            throw new ISOException(e);
        }
    }
}
