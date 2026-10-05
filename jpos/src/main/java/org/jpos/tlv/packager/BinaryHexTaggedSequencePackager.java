/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
 * Packager for tagged TLV sequences where the tag and length are encoded as binary hex.
 * @author Vishnu Pillai
 */
/** {@link TaggedSequencePackager} variant that uses binary hex encoding for tag-length-value fields. */
/**
 * {@link TaggedSequencePackager} variant that encodes tag-value data as binary hex.
 */
public class BinaryHexTaggedSequencePackager extends TaggedSequencePackager {

    /**
     * Default constructor.
     * @throws ISOException on configuration error
     */
    public BinaryHexTaggedSequencePackager() throws ISOException {
        super();
    }

    @Override
    protected ISOFieldPackager getTagPackager() {
        return new TagPackager(this.tag.length(), "Tag");
    }

    /** Field packager for TLV tag fields. */
    public static class TagPackager extends ISOFieldPackager {

        /**
         * Creates a TagPackager with the given length and description.
         * @param len tag field length
         * @param description field description
         */
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
            int packedLength = getPackedLength();
            for (int i = 0; i < tag.length(); i++) {
                if (Character.digit(tag.charAt(i), 16) < 0)
                    throw new ISOException("Invalid hexadecimal tag");
            }
            tagBytes = ISOUtil.hex2byte(tag);
            if (tagBytes.length > packedLength)
                throw new ISOException("Tag length " + tag.length() + " too long. Max: " + getLength());
            if (tagBytes.length != packedLength) {
                byte[] b = new byte[packedLength];
                System.arraycopy(tagBytes, 0, b, b.length - tagBytes.length, tagBytes.length);
                tagBytes = b;
            }
            return tagBytes;
        }

        @Override
        public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {
            int packedLength = getPackedLength();
            checkAvailable(b, offset, packedLength, "tag");
            byte[] tagBytes = new byte[packedLength];
            System.arraycopy(b, offset, tagBytes, 0, tagBytes.length);
            c.setValue(ISOUtil.byte2hex(tagBytes));
            return tagBytes.length;
        }

        private int getPackedLength() throws ISOException {
            int length = getLength();
            if (length < 0 || (length & 1) != 0)
                throw new ISOException("Invalid binary hexadecimal tag length " + length);
            return length / 2;
        }
    }
}
