/*
 * Copyright (c) 2006 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.AsciiHexInterpreter;


/**
 * @author joconnor
 */
public class AsciiHexInterpreterTest extends TestCase {
    private AsciiHexInterpreter inter;

    /*
	 * @see TestCase#setUp()
	 */
    protected void setUp() throws Exception {
        inter = AsciiHexInterpreter.INSTANCE;
    }

    public void testInterpret() {
        byte[] data = new byte[] {(byte)0xFF, (byte)0x12};
        byte[] b = new byte[4];
        inter.interpret(data, b, 0);
        TestUtils.assertEquals(new byte[] {0x46, 0x46, 0x31, 0x32}, b);
    }

    public void testUninterpret() {
        byte[] data = new byte[] {(byte)0xFF, (byte)0x12};
        byte[] b = new byte[] {0x46, 0x46, 0x31, 0x32};
        TestUtils.assertEquals(data, inter.uninterpret(b, 0, 2));
    }

    public void testGetPackedLength() {
        assertEquals(6, inter.getPackedLength(3));
    }
    
    public void testReversability() {
        byte data[] = new byte[] {0x01, 0x23, 0x45, 0x67, (byte)0x89,
                (byte)0xAB, (byte)0xCD, (byte)0xEF};
        byte[] b = new byte[inter.getPackedLength(data.length)];
        inter.interpret(data, b, 0);
        
        TestUtils.assertEquals(data, inter.uninterpret(b, 0, data.length));
    }
}
