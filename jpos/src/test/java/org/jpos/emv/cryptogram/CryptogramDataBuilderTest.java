package org.jpos.emv.cryptogram;

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.jpos.emv.cryptogram.CryptogramDataBuilder.ISO9797Method2;
import static org.jpos.emv.cryptogram.CryptogramDataBuilder.ISO9797Method1;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Elias Terranti
 */
public class CryptogramDataBuilderTest {

    Random r;
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
                byte[] padding = Arrays.copyOfRange(b_padded,  b.length, b_padded.length);
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

            // if data length is not multiple of n, data is padded  with 0s
            if (b.length % n > 0) {
                assertArrayEquals(ISOUtil.concat(b, new byte[n - b.length % n]), b_padded);
            }
        });
    }

}
