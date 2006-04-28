/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.BcdPrefixer;

/**
 * Tests the ASCII length Prefixer.
 * @author jonathan.oconnor@xcom
 */
public class BcdPrefixerTest extends TestCase
{
    public void testEncode() throws Exception
    {
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(21, b);
        TestUtils.assertEquals(new byte[]{0x21}, b);
    }

    public void testEncodeShortLength() throws Exception
    {
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(3, b);
        TestUtils.assertEquals(new byte[]{0x03}, b);
    }

    public void testEncodeLLL() throws Exception
    {
        byte[] b = new byte[2];
        BcdPrefixer.LLL.encodeLength(321, b);
        TestUtils.assertEquals(new byte[]{0x03, 0x21}, b);
    }

    public void testEncodeLLLShortLength() throws Exception
    {
        byte[] b = new byte[2];
        BcdPrefixer.LLL.encodeLength(3, b);
        TestUtils.assertEquals(new byte[]{0x00, 0x03}, b);
    }

    public void testEncode99() throws Exception
    {
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(99, b);
        TestUtils.assertEquals(new byte[]{(byte)0x99}, b);
    }

    public void testDecode() throws Exception
    {
        byte[] b = new byte[]{0x25};
        assertEquals(25, BcdPrefixer.LL.decodeLength(b, 0));
    }

    public void testDecode19() throws Exception
    {
        byte[] b = new byte[]{0x19};
        assertEquals(19, BcdPrefixer.LL.decodeLength(b, 0));
    }

    public void testDecode99() throws Exception
    {
        byte[] b = new byte[]{(byte)0x99};
        assertEquals(99, BcdPrefixer.LL.decodeLength(b, 0));
    }

    public void testReversability() throws Exception
    {
        int len = 3;
        byte[] b = new byte[1];
        BcdPrefixer.LL.encodeLength(len, b);
        assertEquals(len, BcdPrefixer.LL.decodeLength(b, 0));
    }
}
