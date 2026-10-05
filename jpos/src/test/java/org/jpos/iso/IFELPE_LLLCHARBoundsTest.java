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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IFELPE_LLLCHARBoundsTest {
    @Test
    void unpacksExactMaximum() throws Exception {
        IFELPE_LLLCHAR packager = new IFELPE_LLLCHAR(3, "test");
        ISOField field = new ISOField();
        byte[] packed = ISOUtil.asciiToEbcdic("00512abc");

        assertEquals(packed.length, packager.unpack(field, packed, 0));
        assertEquals(12, field.getKey());
        assertEquals("abc", field.getValue());
    }

    @Test
    void packsExactMaximumAndReportsActualMaximum() throws Exception {
        IFELPE_LLLCHAR packager = new IFELPE_LLLCHAR(3, "test");

        byte[] packed = packager.pack(new ISOField(12, "abc"));

        assertEquals(8, packed.length);
        assertEquals(8, packager.getMaxPackedLength());
    }

    @Test
    void rejectsPackingAboveConfiguredAndWireMaximums() {
        IFELPE_LLLCHAR configuredMaximum = new IFELPE_LLLCHAR(2, "test");
        IFELPE_LLLCHAR wireMaximum = new IFELPE_LLLCHAR(999, "test");

        assertThrows(ISOException.class,
          () -> configuredMaximum.pack(new ISOField(12, "abc")));
        assertThrows(ISOException.class,
          () -> wireMaximum.pack(new ISOField(12, "a".repeat(998))));
    }

    @Test
    void rejectsTruncatedHeader() {
        IFELPE_LLLCHAR packager = new IFELPE_LLLCHAR(3, "test");

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), ISOUtil.asciiToEbcdic("0051"), 0));

        assertEquals("Insufficient data for field header", e.getMessage());
    }

    @Test
    void rejectsMalformedHeader() {
        IFELPE_LLLCHAR packager = new IFELPE_LLLCHAR(3, "test");

        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), ISOUtil.asciiToEbcdic("00A12abc"), 0));
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), ISOUtil.asciiToEbcdic("0051Aabc"), 0));
    }

    @Test
    void rejectsLengthShorterThanTag() {
        IFELPE_LLLCHAR packager = new IFELPE_LLLCHAR(3, "test");

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), ISOUtil.asciiToEbcdic("00112"), 0));

        assertEquals("Invalid self-inclusive field length 1", e.getMessage());
    }

    @Test
    void rejectsLengthAboveConfiguredMaximum() {
        IFELPE_LLLCHAR packager = new IFELPE_LLLCHAR(2, "test");

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), ISOUtil.asciiToEbcdic("00512abc"), 0));

        assertEquals("Field length 3 too long. Max: 2", e.getMessage());
    }

    @Test
    void rejectsTruncatedValue() {
        IFELPE_LLLCHAR packager = new IFELPE_LLLCHAR(3, "test");

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), ISOUtil.asciiToEbcdic("00512ab"), 0));

        assertEquals("Insufficient data for field value", e.getMessage());
    }

    @Test
    void streamRejectsLengthBeforeReadingValue() {
        IFELPE_LLLCHAR packager = new IFELPE_LLLCHAR(2, "test");
        ByteArrayInputStream in = new ByteArrayInputStream(ISOUtil.asciiToEbcdic("00512abc"));

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), in));

        assertEquals("Field length 3 too long. Max: 2", e.getMessage());
        assertEquals(3, in.available());
    }

    @Test
    void streamRejectsMalformedTag() {
        IFELPE_LLLCHAR packager = new IFELPE_LLLCHAR(3, "test");

        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(),
            new ByteArrayInputStream(ISOUtil.asciiToEbcdic("0051Aabc"))));
    }

    @Test
    void streamDecodesExactMaximum() throws Exception {
        IFELPE_LLLCHAR packager = new IFELPE_LLLCHAR(3, "test");
        ISOField field = new ISOField();

        packager.unpack(field, new ByteArrayInputStream(ISOUtil.asciiToEbcdic("00512abc")));

        assertEquals(12, field.getKey());
        assertEquals("abc", field.getValue());
    }
}
