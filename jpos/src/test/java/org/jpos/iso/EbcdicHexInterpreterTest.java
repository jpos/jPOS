package org.jpos.iso;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class EbcdicHexInterpreterTest {
    EbcdicHexInterpreter ebcdicHexInterpreter;
    byte[] hexEbcdicData;
    byte[] binaryData;

    @Before
    public void setUp() throws Exception {
        ebcdicHexInterpreter = EbcdicHexInterpreter.INSTANCE;
        binaryData = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz^{}[]\"='@:|,%_<>?!*&".getBytes();

        hexEbcdicData = new byte[] { (byte) 0xF3, (byte) 0xF1, (byte) 0xF3, (byte) 0xF2, (byte) 0xF3, (byte) 0xF3, (byte) 0xF3,
                (byte) 0xF4, (byte) 0xF3, (byte) 0xF5, (byte) 0xF3, (byte) 0xF6, (byte) 0xF3, (byte) 0xF7, (byte) 0xF3, (byte) 0xF8,
                (byte) 0xF3, (byte) 0xF9, (byte) 0xF3, (byte) 0xF0, (byte) 0xF4, (byte) 0xF1, (byte) 0xF4, (byte) 0xF2, (byte) 0xF4,
                (byte) 0xF3, (byte) 0xF4, (byte) 0xF4, (byte) 0xF4, (byte) 0xF5, (byte) 0xF4, (byte) 0xF6, (byte) 0xF4, (byte) 0xF7,
                (byte) 0xF4, (byte) 0xF8, (byte) 0xF4, (byte) 0xF9, (byte) 0xF4, (byte) 0xC1, (byte) 0xF4, (byte) 0xC2, (byte) 0xF4,
                (byte) 0xC3, (byte) 0xF4, (byte) 0xC4, (byte) 0xF4, (byte) 0xC5, (byte) 0xF4, (byte) 0xC6, (byte) 0xF5, (byte) 0xF0,
                (byte) 0xF5, (byte) 0xF1, (byte) 0xF5, (byte) 0xF2, (byte) 0xF5, (byte) 0xF3, (byte) 0xF5, (byte) 0xF4, (byte) 0xF5,
                (byte) 0xF5, (byte) 0xF5, (byte) 0xF6, (byte) 0xF5, (byte) 0xF7, (byte) 0xF5, (byte) 0xF8, (byte) 0xF5, (byte) 0xF9,
                (byte) 0xF5, (byte) 0xC1, (byte) 0xF6, (byte) 0xF1, (byte) 0xF6, (byte) 0xF2, (byte) 0xF6, (byte) 0xF3, (byte) 0xF6,
                (byte) 0xF4, (byte) 0xF6, (byte) 0xF5, (byte) 0xF6, (byte) 0xF6, (byte) 0xF6, (byte) 0xF7, (byte) 0xF6, (byte) 0xF8,
                (byte) 0xF6, (byte) 0xF9, (byte) 0xF6, (byte) 0xC1, (byte) 0xF6, (byte) 0xC2, (byte) 0xF6, (byte) 0xC3, (byte) 0xF6,
                (byte) 0xC4, (byte) 0xF6, (byte) 0xC5, (byte) 0xF6, (byte) 0xC6, (byte) 0xF7, (byte) 0xF0, (byte) 0xF7, (byte) 0xF1,
                (byte) 0xF7, (byte) 0xF2, (byte) 0xF7, (byte) 0xF3, (byte) 0xF7, (byte) 0xF4, (byte) 0xF7, (byte) 0xF5, (byte) 0xF7,
                (byte) 0xF6, (byte) 0xF7, (byte) 0xF7, (byte) 0xF7, (byte) 0xF8, (byte) 0xF7, (byte) 0xF9, (byte) 0xF7, (byte) 0xC1,
                (byte) 0xF5, (byte) 0xC5, (byte) 0xF7, (byte) 0xC2, (byte) 0xF7, (byte) 0xC4, (byte) 0xF5, (byte) 0xC2, (byte) 0xF5,
                (byte) 0xC4, (byte) 0xF2, (byte) 0xF2, (byte) 0xF3, (byte) 0xC4, (byte) 0xF2, (byte) 0xF7, (byte) 0xF4, (byte) 0xF0,
                (byte) 0xF3, (byte) 0xC1, (byte) 0xF7, (byte) 0xC3, (byte) 0xF2, (byte) 0xC3, (byte) 0xF2, (byte) 0xF5, (byte) 0xF5,
                (byte) 0xC6, (byte) 0xF3, (byte) 0xC3, (byte) 0xF3, (byte) 0xC5, (byte) 0xF3, (byte) 0xC6, (byte) 0xF2, (byte) 0xF1,
                (byte) 0xF2, (byte) 0xC1, (byte) 0xF2, (byte) 0xF6,

        };
    }

    @Test
    public void testInterpret() {
        int offset = 0;
        byte[] result = new byte[binaryData.length * 2];
        ebcdicHexInterpreter.interpret(binaryData, result, 0);
        assertThat(result, is(hexEbcdicData));
    }

    @Test
    public void testUninterpret() {
        int offset = 0;
        int length = 80;
        byte[] result = ebcdicHexInterpreter.uninterpret(hexEbcdicData, offset, length);
        assertThat(result, is(new byte[] { -5, -4, -3, -2, -1, -48, -47, -46, -45, -6, -5, -4, -3, -2, -1, -32, -31, -30, -29, -21,
                -20, -19, -18, -17, -16, -6, -5, -4, -3, -2, -1, -16, -15, -14, -13, -5, 59, 60, 61, 62, 63, 64, 65, 66, 67, 11, 12,
                13, 14, 15, 16, 58, 59, 60, 61, 62, 63, 80, 81, 82, 83, 27, -1, 28, 30, -4, -2, -4, -34, -63, -6, -37, 29, -51, -1,
                -16, -35, -33, -48, -5 }));
    }

}
