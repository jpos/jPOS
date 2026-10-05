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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TerminatedCharacterPackagerSecurityTest {
    @Test
    void terminatedFieldPackingEnforcesItsWireContract() throws Exception {
        IF_TCHAR packager = new IF_TCHAR(3, "terminated", "::");

        TestUtils.assertEquals(bytes("ABC::"), packager.pack(new ISOField(2, "ABC")));
        assertThrows(ISOException.class, () -> packager.pack(new ISOField(2, "ABCD")));
        assertThrows(ISOException.class, () -> packager.pack(new ISOField(2, "A::")));

        packager.setToken(null);
        assertThrows(ISOException.class, () -> packager.pack(new ISOField(2, "ABC")));
        packager.setToken("");
        assertThrows(ISOException.class, () -> packager.pack(new ISOField(2, "ABC")));
    }

    @Test
    void terminatedFieldHonorsOffsetAndMaximumLength() throws Exception {
        IF_TCHAR packager = new IF_TCHAR(3, "terminated", "|");
        ISOField field = new ISOField(2);

        assertEquals(4, packager.unpack(field, bytes("xxABC|tail"), 2));
        assertEquals("ABC", field.getValue());
        assertEquals(3, packager.getLength());
        assertEquals(1, packager.unpack(field, bytes("|tail"), 0));
        assertEquals("", field.getValue());

        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("ABC"), 0));
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("ABCD|"), 0));
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("ABC|"), 5));
    }

    @Test
    void terminatedFieldRequiresTheCompleteConfiguredToken() throws Exception {
        IF_TCHAR packager = new IF_TCHAR(3, "terminated", "::");
        ISOField field = new ISOField(2);

        assertEquals(3, packager.unpack(field, bytes("A::tail"), 0));
        assertEquals("A", field.getValue());
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("ABC:"), 0));

        packager.setToken(null);
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("ABC"), 0));
        packager.setToken("");
        assertThrows(ISOException.class, () -> packager.unpack(field, bytes("ABC"), 0));
    }

    @Test
    void terminatedStreamStopsAtTheTerminator() throws Exception {
        IF_TCHAR packager = new IF_TCHAR(3, "terminated", "::");
        ISOField field = new ISOField(2);
        ByteArrayInputStream in = stream("ABC::tail");

        packager.unpack(field, in);

        assertEquals("ABC", field.getValue());
        assertEquals('t', in.read());
        assertThrows(EOFException.class, () -> packager.unpack(field, stream("ABC:")));
        assertThrows(ISOException.class, () -> packager.unpack(field, stream("ABCD::")));
    }

    @SuppressWarnings("deprecation")
    @Test
    void fixedEbcdicFieldRequiresItsConfiguredBytes() throws Exception {
        IF_ECHAR packager = new IF_ECHAR(3, "EBCDIC");
        ISOField field = new ISOField(2);
        byte[] encoded = ISOUtil.asciiToEbcdic("xABCz");

        assertEquals(3, packager.unpack(field, encoded, 1));
        assertEquals("ABC", field.getValue());
        assertThrows(ISOException.class, () -> packager.unpack(field, encoded, 3));

        assertThrows(EOFException.class, () -> packager.unpack(field,
          new ByteArrayInputStream(ISOUtil.asciiToEbcdic("AB"))));
    }

    private static byte[] bytes(String value) {
        return value.getBytes(ISOUtil.CHARSET);
    }

    private static ByteArrayInputStream stream(String value) {
        return new ByteArrayInputStream(bytes(value));
    }
}
