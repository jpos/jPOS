/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

/**
 * Tests the EBCDIC length Prefixer.
 * @author jonathan.oconnor@xcom.de
 */
public class EbcdicPrefixerTest extends TestCase
{
    public void testEncode() throws Exception
    {
        byte[] b = new byte[2];
        EbcdicPrefixer.LL.encodeLength(21, b);
        TestUtils.assertEquals(new byte[]{(byte)0xF2, (byte)0xF1}, b);
    }

    public void testDecode() throws Exception
    {
        byte[] b = new byte[]{(byte)0xF2, (byte)0xF5};
        assertEquals(25, EbcdicPrefixer.LL.decodeLength(b, 0));
    }

    public void testReversability() throws Exception
    {
        int len = 3;
        byte[] b = new byte[2];
        EbcdicPrefixer.LL.encodeLength(len, b);
        assertEquals(len, EbcdicPrefixer.LL.decodeLength(b, 0));
    }
}
