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
 * Tests the EBCDIC length Prefixer.
 * @author jonathan.oconnor@xcom.de
 */
public class EbcdicPrefixerTest {
    @Test
    public void testEncode() throws Exception
    {
        byte[] b = new byte[2];
        EbcdicPrefixer.LL.encodeLength(21, b);
        TestUtils.assertEquals(new byte[]{(byte)0xF2, (byte)0xF1}, b);
    }

    @Test
    public void testDecode() throws Exception
    {
        byte[] b = new byte[]{(byte)0xF2, (byte)0xF5};
        assertEquals(25, EbcdicPrefixer.LL.decodeLength(b, 0));
    }

    @Test
    public void testReversability() throws Exception
    {
        int len = 3;
        byte[] b = new byte[2];
        EbcdicPrefixer.LL.encodeLength(len, b);
        assertEquals(len, EbcdicPrefixer.LL.decodeLength(b, 0));
    }
}
