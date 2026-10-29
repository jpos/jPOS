/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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

package org.jpos.emv.cryptogram;

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.jpos.emv.cryptogram.CryptogramDataBuilder.PaddingMethod.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Elias Terranti
 */
public class CryptogramDataBuilderTest {

    byte[] r0;
    byte[] r1;
    byte[] r2;

    @BeforeEach
    public void setUp() {
        Random r = new Random();

        r0 = new byte[0];
        r1 = new byte[8];
        r2 = new byte[r.nextInt(256)];

        r.nextBytes(r0);
        r.nextBytes(r1);
        r.nextBytes(r2);
    }

    @Test
    void testPaddingMethod2() {
        int n = 8; // block size
        Arrays.asList(r0, r1, r2).forEach(b -> {

            byte[] b_padded = ISOUtil.hex2byte(ISO9797Method2.apply(ISOUtil.hexString(b)));

            assertEquals(0, b_padded.length % n);

            // if data is empty, padded data should be a block of 1 bit followed by 0s
            if (b.length == 0) {
                assertArrayEquals(ISOUtil.hex2byte("8000000000000000"), b_padded);
            }
            // if data length is multiple of n, data is padded with a new block of 1 bit followed by 0s
            if (b.length % n == 0 && b.length > 0) {
                assertArrayEquals(ISOUtil.concat(b, ISOUtil.hex2byte("8000000000000000")), b_padded);
            }

            // if data length is not multiple of n, data is padded  with 1 bit followed by 0s
            if (b.length % n > 0) {
                byte[] padding = Arrays.copyOfRange(b_padded, b.length, b_padded.length);
                assertEquals(((byte) 0x80), padding[0]);
                if (padding.length > 1) {
                    assertArrayEquals(ISOUtil.concat(new byte[]{(byte) 0x80}, new byte[n - b.length % n - 1]), padding);
                }
            }
        });
    }

    @Test
    void testPaddingMethod1() {
        int n = 8; // block size
        Arrays.asList(r0, r1, r2).forEach(b -> {
            byte[] b_padded = ISOUtil.hex2byte(ISO9797Method1.apply(ISOUtil.hexString(b)));

            assertEquals(0, b_padded.length % n);

            // if data is empty, padded data should be a block of 0s
            if (b.length == 0) {
                assertArrayEquals(new byte[n], b_padded);
            }

            // if data length  is multiple of n,  no padding is added
            if (b.length % n == 0 && b.length > 0) {
                assertArrayEquals(b, b_padded);
            }

            // if data length is not multiple of n, data is padded with 0s
            if (b.length % n > 0) {
                assertArrayEquals(ISOUtil.concat(b, new byte[n - b.length % n]), b_padded);
            }
        });
    }

    @Test
    void testPaddingMethod3() {
        int n = 8; // block size
        Arrays.asList(r0, r1, r2).forEach(b -> {
            byte[] b_padded = ISOUtil.hex2byte(ISO9797Method3.apply(ISOUtil.hexString(b)));

            assertEquals(0, b_padded.length % n);

            // if data is empty, padded data should be a 2 block of 0s
            if (b.length == 0) {
                assertArrayEquals(new byte[n * 2], b_padded);
            }

            // if data length is multiple of n, pad only length Block
            if (b.length % n == 0 && b.length > 0) {
                String pad = ISOUtil.byte2hex(b_padded).substring(0, 2 * (b_padded.length - b.length));
                assertArrayEquals(b, Arrays.copyOfRange(b_padded, b_padded.length - b.length, b_padded.length));
                assertArrayEquals(ISOUtil.int2byte(b.length), ISOUtil.hex2byte(ISOUtil.unPadLeft(pad, '0')));
            }

            // if data length is not multiple of n, data is right padded with 0s, then left padded with length Block
            if (b.length % n > 0) {
                byte[] rpad = new byte[n - b.length % n];
                byte[] lpad = Arrays.copyOfRange(b_padded, 0, b_padded.length - b.length - rpad.length);
                assertArrayEquals(ISOUtil.int2byte(b.length), ISOUtil.hex2byte(ISOUtil.unPadLeft(ISOUtil.byte2hex(lpad), '0')));
                assertArrayEquals(ISOUtil.concat(ISOUtil.concat(lpad, b), rpad), b_padded);
            }
        });
    }

}
