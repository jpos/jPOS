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

package org.jpos.tlv.packager;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOUtil;

/**
 * @author Vishnu Pillai
 */
public class BinaryHexTaggedSequencePackager extends TaggedSequencePackager {

    public BinaryHexTaggedSequencePackager() throws ISOException {
        super();
    }

    @Override
    protected ISOFieldPackager getTagPackager() {
        return new TagPackager(this.tag.length(), "Tag");
    }

    public static class TagPackager extends ISOFieldPackager {

        public TagPackager(int len, String description) {
            super(len, description);
        }


        @Override
        public int getMaxPackedLength() {
            return getLength() / 2;
        }

        @Override
        public byte[] pack(ISOComponent c) throws ISOException {
            byte[] tagBytes;
            String tag = c.getValue().toString();
            tagBytes = ISOUtil.hex2byte(tag);
            if (tagBytes.length != getMaxPackedLength()) {
                byte[] b = new byte[getMaxPackedLength()];
                System.arraycopy(tagBytes, 0, b, b.length - tagBytes.length, tagBytes.length);
                tagBytes = b;
            }
            return tagBytes;
        }

        @Override
        public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
            byte[] tagBytes = new byte[getMaxPackedLength()];
            System.arraycopy(b, offset, tagBytes, 0, tagBytes.length);
            c.setValue(ISOUtil.byte2hex(tagBytes));
            return tagBytes.length;
        }
    }
}
