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

package org.jpos.iso;

/**
 * Binary prefixer whose encoded length includes the length prefix itself.
 */
public class SelfInclusiveBinaryPrefixer implements Prefixer {
    /** A self-inclusive, one-byte binary length prefixer. */
    public static final SelfInclusiveBinaryPrefixer B = new SelfInclusiveBinaryPrefixer(BinaryPrefixer.B);

    /** A self-inclusive, two-byte binary length prefixer. */
    public static final SelfInclusiveBinaryPrefixer BB = new SelfInclusiveBinaryPrefixer(BinaryPrefixer.BB);

    private final BinaryPrefixer prefixer;

    private SelfInclusiveBinaryPrefixer(BinaryPrefixer prefixer) {
        this.prefixer = prefixer;
    }

    @Override
    public void encodeLength(int length, byte[] b) throws ISOException {
        int inclusiveLength = length + getPackedLength();
        if (inclusiveLength > maxLength())
            throw new ISOException("invalid len " + length + " for a self-inclusive binary prefix");
        prefixer.encodeLength(inclusiveLength, b);
    }

    @Override
    public int decodeLength(byte[] b, int offset) throws ISOException {
        int inclusiveLength = prefixer.decodeLength(b, offset);
        if (inclusiveLength < getPackedLength())
            throw new ISOException("invalid self-inclusive binary prefix " + inclusiveLength);
        return inclusiveLength - getPackedLength();
    }

    @Override
    public int getPackedLength() {
        return prefixer.getPackedLength();
    }

    private int maxLength() {
        return getPackedLength() == 1 ? 0xFF : 0xFFFF;
    }
}
