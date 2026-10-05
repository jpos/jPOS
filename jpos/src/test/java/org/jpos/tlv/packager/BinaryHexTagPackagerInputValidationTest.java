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

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BinaryHexTagPackagerInputValidationTest {
    @Test
    void rejectsTruncatedTagAtNonzeroOffset() {
        BinaryHexTaggedSequencePackager.TagPackager packager = packager(4);

        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(1), new byte[] { 0x00, 0x12 }, 1));
    }

    @Test
    void unpacksTagAtNonzeroOffset() throws ISOException {
        BinaryHexTaggedSequencePackager.TagPackager packager = packager(4);
        ISOField field = new ISOField(1);

        assertEquals(2, packager.unpack(field, new byte[] { 0x00, 0x12, 0x34, 0x00 }, 1));
        assertEquals("1234", field.getValue());
    }

    @Test
    void preservesShortTagLeftPadding() throws ISOException {
        BinaryHexTaggedSequencePackager.TagPackager packager = packager(4);

        assertArrayEquals(new byte[] { 0x00, 0x0A }, packager.pack(new ISOField(1, "A")));
    }

    @Test
    void rejectsOversizedAndMalformedTags() {
        BinaryHexTaggedSequencePackager.TagPackager packager = packager(4);

        assertThrows(ISOException.class, () -> packager.pack(new ISOField(1, "010203")));
        assertThrows(ISOException.class, () -> packager.pack(new ISOField(1, "12G4")));
    }

    @Test
    void rejectsOddAndNegativeConfiguredLengths() {
        BinaryHexTaggedSequencePackager.TagPackager odd = packager(3);
        BinaryHexTaggedSequencePackager.TagPackager negative = packager(-2);

        assertThrows(ISOException.class, () -> odd.pack(new ISOField(1, "12")));
        assertThrows(ISOException.class,
          () -> odd.unpack(new ISOField(1), new byte[] { 0x12, 0x34 }, 0));
        assertThrows(ISOException.class, () -> negative.pack(new ISOField(1, "12")));
        assertThrows(ISOException.class,
          () -> negative.unpack(new ISOField(1), new byte[] { 0x12 }, 0));
    }

    private static BinaryHexTaggedSequencePackager.TagPackager packager(int length) {
        return new BinaryHexTaggedSequencePackager.TagPackager(length, "Tag");
    }
}
