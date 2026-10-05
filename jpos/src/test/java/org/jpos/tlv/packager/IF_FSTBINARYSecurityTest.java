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

import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IF_FSTBINARYSecurityTest {
    private static final byte TERMINATOR = (byte) 0xFE;

    @Test
    void packingPreservesThePayloadAndAppendsTheTerminator() throws Exception {
        IF_FSTBINARY packager = packager(3);

        assertArrayEquals(bytes(TERMINATOR), packager.pack(new ISOBinaryField(1, bytes())));
        assertArrayEquals(bytes(0x00, 0x80, 0x7F, 0xFE),
          packager.pack(new ISOBinaryField(1, bytes(0x00, 0x80, 0x7F))));
        assertThrows(ISOException.class,
          () -> packager.pack(new ISOBinaryField(1, bytes(1, 2, 3, 4))));
        assertThrows(ISOException.class,
          () -> packager.pack(new ISOBinaryField(1, bytes(1, 0xFE, 2))));
        assertThrows(ISOException.class,
          () -> new IF_FSTBINARY().pack(new ISOBinaryField(1, bytes(1))));
        assertThrows(IllegalArgumentException.class, () -> packager.setToken("-1"));
    }

    @Test
    void byteArrayUnpackHonorsOffsetAndAvailableInput() throws Exception {
        IF_FSTBINARY packager = packager(3);
        ISOBinaryField field = new ISOBinaryField(1);

        byte[] image = bytes(9, 9, 0x00, 0x80, 0x7F, 0xFE, 8);
        assertEquals(4, packager.unpack(field, image, 2));
        assertArrayEquals(bytes(0x00, 0x80, 0x7F), field.getBytes());
        assertEquals(1, packager.unpack(field, bytes(0xFE, 8), 0));
        assertArrayEquals(bytes(), field.getBytes());
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes(1, 2, 3), 0));
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes(1, 2, 3, 4, 0xFE), 0));
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes(0xFE), -1));
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes(0xFE), 2));

        ISOField characterField = new ISOField(1);
        assertEquals(3, packager.unpack(characterField, bytes(0x00, 0x80, 0xFE), 0));
        assertEquals(new String(bytes(0x00, 0x80), ISOUtil.CHARSET), characterField.getValue());
        assertArrayEquals(bytes(0x00, 0x80), characterField.getBytes());
    }

    @Test
    void streamRoundTripDoesNotDependOnAvailableAndPreservesTrailingData() throws Exception {
        IF_FSTBINARY packager = packager(3);
        byte[] value = bytes(0x00, 0x80, 0x7F);
        byte[] packed = packager.pack(new ISOBinaryField(1, value));
        ByteArrayInputStream in = unavailable(bytes(0x00, 0x80, 0x7F, 0xFE, 0x55));
        ISOBinaryField field = new ISOBinaryField(1);

        packager.unpack(field, in);

        assertArrayEquals(value, field.getBytes());
        assertArrayEquals(value, unpack(packager, packed));
        assertEquals(0x55, in.read());
        assertThrows(EOFException.class,
          () -> packager.unpack(field, unavailable(bytes(1, 2, 3))));
        assertThrows(ISOException.class,
          () -> packager.unpack(field, unavailable(bytes(1, 2, 3, 4, 0xFE))));
    }

    private static byte[] unpack(IF_FSTBINARY packager, byte[] packed) throws Exception {
        ISOBinaryField field = new ISOBinaryField(1);
        assertEquals(packed.length, packager.unpack(field, packed, 0));
        return field.getBytes();
    }

    private static IF_FSTBINARY packager(int length) {
        IF_FSTBINARY packager = new IF_FSTBINARY(length, "terminated binary");
        packager.setToken("FE");
        return packager;
    }

    private static byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++)
            bytes[i] = (byte) values[i];
        return bytes;
    }

    private static ByteArrayInputStream unavailable(byte[] value) {
        return new ByteArrayInputStream(value) {
            @Override
            public int available() {
                return 0;
            }
        };
    }
}
