package org.jpos.iso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class BinaryPrefixer2Test {

    @Test
    public void testConstructor() throws Throwable {
        BinaryPrefixer binaryPrefixer = new BinaryPrefixer(100);
        assertEquals("binaryPrefixer.getPackedLength()", 100, binaryPrefixer.getPackedLength());
    }

    @Test
    public void testDecodeLength() throws Throwable {
        byte[] b = new byte[2];
        b[1] = (byte) -1;
        int result = new BinaryPrefixer(1).decodeLength(b, 1);
        assertEquals("result", 255, result);
    }

    @Test
    public void testDecodeLength1() throws Throwable {
        byte[] b = new byte[3];
        int result = new BinaryPrefixer(0).decodeLength(b, 100);
        assertEquals("result", 0, result);
    }

    @Test
    public void testDecodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[3];
        try {
            new BinaryPrefixer(100).decodeLength(b, 0);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "3", ex.getMessage());
        }
    }

    @Test
    public void testDecodeLengthThrowsArrayIndexOutOfBoundsException1() throws Throwable {
        byte[] b = new byte[3];
        try {
            new BinaryPrefixer(100).decodeLength(b, 100);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "100", ex.getMessage());
        }
    }

    @Test
    public void testDecodeLengthThrowsNullPointerException() throws Throwable {
        try {
            new BinaryPrefixer(100).decodeLength((byte[]) null, 100);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEncodeLength() throws Throwable {
        byte[] b = new byte[4];
        new BinaryPrefixer(2).encodeLength(100, b);
        assertEquals("b[1]", (byte) 100, b[1]);
    }

    @Test
    public void testEncodeLength1() throws Throwable {
        byte[] b = new byte[0];
        new BinaryPrefixer(0).encodeLength(100, b);
        assertEquals("b.length", 0, b.length);
    }

    @Test
    public void testEncodeLengthThrowsArrayIndexOutOfBoundsException() throws Throwable {
        byte[] b = new byte[0];
        try {
            new BinaryPrefixer(1).encodeLength(100, b);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());
            assertEquals("b.length", 0, b.length);
        }
    }

    @Test
    public void testEncodeLengthThrowsNullPointerException() throws Throwable {
        try {
            new BinaryPrefixer(2).encodeLength(100, (byte[]) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = new BinaryPrefixer(100).getPackedLength();
        assertEquals("result", 100, result);
    }
}
