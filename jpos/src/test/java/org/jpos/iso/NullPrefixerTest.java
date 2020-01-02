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

package org.jpos.iso;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NullPrefixerTest {

    @Test
    public void testDecodeLength() throws Throwable {
        byte[] b = new byte[2];
        int result = NullPrefixer.INSTANCE.decodeLength(b, 100);
        assertEquals(-1, result, "result");
    }

    @Test
    public void testEncodeLength() throws Throwable {
        NullPrefixer INSTANCE = NullPrefixer.INSTANCE;
        byte[] b = new byte[2];
        INSTANCE.encodeLength(100, b);
        assertEquals(0, INSTANCE.getPackedLength(), "INSTANCE.getPackedLength()");
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = NullPrefixer.INSTANCE.getPackedLength();
        assertEquals(0, result, "result");
    }
}
