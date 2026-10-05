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
import static org.junit.jupiter.api.Assertions.assertTrue;

class IFIPM_LLLCHARTest {
    @Test
    void unpacksExactMaximum() throws Exception {
        IFIPM_LLLCHAR packager = new IFIPM_LLLCHAR(3, "test");
        ISOField field = new ISOField();

        assertEquals(10, packager.unpack(field, "0012003abc".getBytes(ISOUtil.CHARSET), 0));
        assertEquals(12, field.getKey());
        assertEquals("abc", field.getValue());
    }

    @Test
    void rejectsTruncatedHeader() {
        IFIPM_LLLCHAR packager = new IFIPM_LLLCHAR(3, "test");

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), "001200".getBytes(ISOUtil.CHARSET), 0));

        assertEquals("Insufficient data for field header", e.getMessage());
    }

    @Test
    void rejectsMalformedHeader() {
        IFIPM_LLLCHAR packager = new IFIPM_LLLCHAR(3, "test");

        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), "00A2003abc".getBytes(ISOUtil.CHARSET), 0));
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), "00120A3abc".getBytes(ISOUtil.CHARSET), 0));
    }

    @Test
    void rejectsLengthAboveConfiguredMaximum() {
        IFIPM_LLLCHAR packager = new IFIPM_LLLCHAR(2, "test");

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), "0012003abc".getBytes(ISOUtil.CHARSET), 0));

        assertEquals("Field length 3 too long. Max: 2", e.getMessage());
    }

    @Test
    void rejectsTruncatedValue() {
        IFIPM_LLLCHAR packager = new IFIPM_LLLCHAR(3, "test");

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), "0012003ab".getBytes(ISOUtil.CHARSET), 0));

        assertEquals("Insufficient data for field value", e.getMessage());
    }

    @Test
    void streamRejectsLengthBeforeReadingValue() {
        IFIPM_LLLCHAR packager = new IFIPM_LLLCHAR(2, "test");
        ByteArrayInputStream in = new ByteArrayInputStream("0012003abc".getBytes(ISOUtil.CHARSET));

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), in));

        assertEquals("Field length 3 too long. Max: 2", e.getMessage());
        assertEquals(3, in.available());
    }

    @Test
    void streamRejectsMalformedHeader() {
        IFIPM_LLLCHAR packager = new IFIPM_LLLCHAR(3, "test");

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(),
            new ByteArrayInputStream("00120A3abc".getBytes(ISOUtil.CHARSET))));

        assertTrue(e.getMessage().contains("Expected digit"));
    }
}
