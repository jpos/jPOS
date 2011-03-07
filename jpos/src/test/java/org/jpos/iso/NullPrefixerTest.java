package org.jpos.iso;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NullPrefixerTest {

    @Test
    public void testDecodeLength() throws Throwable {
        byte[] b = new byte[2];
        int result = NullPrefixer.INSTANCE.decodeLength(b, 100);
        assertEquals("result", -1, result);
    }

    @Test
    public void testEncodeLength() throws Throwable {
        NullPrefixer INSTANCE = NullPrefixer.INSTANCE;
        byte[] b = new byte[2];
        INSTANCE.encodeLength(100, b);
        assertEquals("INSTANCE.getPackedLength()", 0, INSTANCE.getPackedLength());
    }

    @Test
    public void testGetPackedLength() throws Throwable {
        int result = NullPrefixer.INSTANCE.getPackedLength();
        assertEquals("result", 0, result);
    }
}
