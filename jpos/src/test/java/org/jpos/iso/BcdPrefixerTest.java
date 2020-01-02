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
public class BcdPrefixerTest {
    @Test
    public void testEncode() throws Exception
    {
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(21, b);
        TestUtils.assertEquals(new byte[]{0x21}, b);
    }

    @Test
    public void testEncodeShortLength() throws Exception
    {
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(3, b);
        TestUtils.assertEquals(new byte[]{0x03}, b);
    }

    @Test
    public void testEncodeLLL() throws Exception
    {
        byte[] b = new byte[2];
        BcdPrefixer.LLL.encodeLength(321, b);
        TestUtils.assertEquals(new byte[]{0x03, 0x21}, b);
    }

    @Test
    public void testEncodeLLLShortLength() throws Exception
    {
        byte[] b = new byte[2];
        BcdPrefixer.LLL.encodeLength(3, b);
        TestUtils.assertEquals(new byte[]{0x00, 0x03}, b);
    }

    @Test
    public void testEncode99() throws Exception
    {
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(99, b);
        TestUtils.assertEquals(new byte[]{(byte)0x99}, b);
    }

    @Test
    public void testDecode() throws Exception
    {
        byte[] b = new byte[]{0x25};
        assertEquals(25, BcdPrefixer.LL.decodeLength(b, 0));
    }

    @Test
    public void testDecode19() throws Exception
    {
        byte[] b = new byte[]{0x19};
        assertEquals(19, BcdPrefixer.LL.decodeLength(b, 0));
    }

    @Test
    public void testDecode99() throws Exception
    {
        byte[] b = new byte[]{(byte)0x99};
        assertEquals(99, BcdPrefixer.LL.decodeLength(b, 0));
    }

    @Test
    public void testReversability() throws Exception
    {
        int len = 3;
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(len, b);
        assertEquals(len, BcdPrefixer.LL.decodeLength(b, 0));
    }
}
