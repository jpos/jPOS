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

/**
 * Tests the ASCII length Prefixer.
 * @author jonathan.oconnor@xcom
 */
public class AsciiPrefixerTest {
    @Test
    public void testEncode() throws Exception
    {
        byte[] b = new byte[2];
        AsciiPrefixer.LL.encodeLength(21, b);
        TestUtils.assertEquals(new byte[]{0x32, 0x31}, b);
    }

    @Test
    public void testEncodeShortLength() throws Exception
    {
        byte[] b = new byte[2];
        AsciiPrefixer.LL.encodeLength(3, b);
        TestUtils.assertEquals(new byte[]{0x30, 0x33}, b);
    }

    @Test
    public void testDecode() throws Exception
    {
        byte[] b = new byte[]{0x32, 0x35};
        assertEquals(25, AsciiPrefixer.LL.decodeLength(b, 0));
    }

    @Test
    public void testReversability() throws Exception
    {
        int len = 3;
        byte[] b = new byte[2];
        AsciiPrefixer.LL.encodeLength(len, b);
        assertEquals(len, AsciiPrefixer.LL.decodeLength(b, 0));
    }
}
