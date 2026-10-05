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
import org.jpos.iso.ISOUtil;
import org.jpos.iso.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IF_FSTCHARSecurityTest {
    @Test
    void packingEnforcesTheTerminatedFieldContract() throws Exception {
        IF_FSTCHAR packager = packager(3);

        TestUtils.assertEquals(bytes("|"), packager.pack(new ISOField(1, "")));
        TestUtils.assertEquals(bytes("ABC|"), packager.pack(new ISOField(1, "ABC")));
        assertThrows(ISOException.class, () -> packager.pack(new ISOField(1, "ABCD")));
        assertThrows(ISOException.class, () -> packager.pack(new ISOField(1, "A|B")));
        assertThrows(IllegalArgumentException.class, () -> packager.setToken("\u20ac"));
        assertThrows(ISOException.class,
          () -> new IF_FSTCHAR().pack(new ISOField(1, "A")));

        packager.setToken("\u00e9");
        TestUtils.assertEquals(bytes("A\u00e9"), packager.pack(new ISOField(1, "A")));
    }

    @Test
    void byteArrayUnpackHonorsOffsetAndAvailableInput() throws Exception {
        IF_FSTCHAR packager = packager(3);
        ISOField field = new ISOField(1);

        assertEquals(4, packager.unpack(field, bytes("xxABC|tail"), 2));
        assertEquals("ABC", field.getValue());
        assertEquals(1, packager.unpack(field, bytes("|tail"), 0));
        assertEquals("", field.getValue());
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("ABC"), 0));
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("ABCD|"), 0));
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("ABC|"), -1));
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("ABC|"), 5));
    }

    @Test
    void streamUnpackDoesNotDependOnAvailableAndStopsAtTerminator() throws Exception {
        IF_FSTCHAR packager = packager(3);
        ISOField field = new ISOField(1);
        ByteArrayInputStream in = unavailable(bytes("ABC|tail"));

        packager.unpack(field, in);

        assertEquals("ABC", field.getValue());
        assertEquals('t', in.read());
        assertThrows(EOFException.class,
          () -> packager.unpack(field, unavailable(bytes("ABC"))));
        assertThrows(ISOException.class,
          () -> packager.unpack(field, unavailable(bytes("ABCD|"))));

        packager.setToken("\u00e9");
        packager.unpack(field, unavailable(bytes("A\u00e9")));
        assertEquals("A", field.getValue());
    }

    private static IF_FSTCHAR packager(int length) {
        IF_FSTCHAR packager = new IF_FSTCHAR(length, "terminated");
        packager.setToken("|");
        return packager;
    }

    private static byte[] bytes(String value) {
        return value.getBytes(ISOUtil.CHARSET);
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
