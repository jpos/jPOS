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

package org.jpos.security;

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class SystemSeedTest {
    @Test
    public void testWellKnownSeeds() throws Throwable {
        assertEquals("371B4CF737319AC15BD6B18DE9E8B8537D2D2CC6323B4E728C8A06C654FBBA06",
                ISOUtil.hexString(ISOUtil.xor(SystemSeed.getSeed(16,32), SystemSeed.getSeed(4064-32))), "Invalid seed 0");
        assertEquals("6835BEFB07E7965940A99D46C7FAC87561DCB1FD7BF4932DB5ACC1E0529B1AE891B0368E14D5F9BFFC74E380426C3A7B",
                ISOUtil.hexString(ISOUtil.xor(SystemSeed.getSeed(0,1024), SystemSeed.getSeed(2048,48))), "Invalid seed 1");
        assertEquals("6835BEFB07E7965940A99D46C7FAC87561DCB1FD7BF4932DB5ACC1E0529B1AE891B0368E14D5F9BFFC74E380426C3A7B",
          ISOUtil.hexString(ISOUtil.xor(SystemSeed.getSeed(0,1024), SystemSeed.getSeed(6144,48))), "Invalid seed 3");

         assertEquals(ISOUtil.hexString(new byte[8192]), ISOUtil.hexString(ISOUtil.xor(SystemSeed.getSeed(1,8192), SystemSeed.getSeed(4097,16384))), "Invalid seed 3");

    }
}
