/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.AsciiPrefixer;

/**
 * Tests the ASCII length Prefixer.
 * @author jonathan.oconnor@xcom
 */
public class AsciiPrefixerTest extends TestCase
{
    public void testEncode() throws Exception
    {
        byte[] b = new byte[2];
        AsciiPrefixer.LL.encodeLength(21, b);
        TestUtils.assertEquals(new byte[]{0x32, 0x31}, b);
    }

    public void testEncodeShortLength() throws Exception
    {
        byte[] b = new byte[2];
        AsciiPrefixer.LL.encodeLength(3, b);
        TestUtils.assertEquals(new byte[]{0x30, 0x33}, b);
    }

    public void testDecode() throws Exception
    {
        byte[] b = new byte[]{0x32, 0x35};
        assertEquals(25, AsciiPrefixer.LL.decodeLength(b, 0));
    }

    public void testReversability() throws Exception
    {
        int len = 3;
        byte[] b = new byte[2];
        AsciiPrefixer.LL.encodeLength(len, b);
        assertEquals(len, AsciiPrefixer.LL.decodeLength(b, 0));
    }
}
