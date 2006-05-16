/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

/**
 * @author joconnor
 */
public class LeftPadderTest extends TestCase {
    private LeftPadder padder;
    
    protected void setUp() {
        padder = LeftPadder.ZERO_PADDER;
    }
    
    public void testPaddingNeeded() throws Exception {
        assertEquals("000123", padder.pad("123", 6));
    }

    public void testNoPaddingNeeded() throws Exception {
        assertEquals("123", padder.pad("123", 3));
    }

    public void testPadLengthTooShort() throws Exception {
        try {
            padder.pad("123", 2);
            fail("Padding a string longer than the available width should throw an exception");
        } catch (Exception asIExpected) {
        }
    }

    public void testUnpad() throws Exception {
        assertEquals("123", padder.unpad("000123"));
    }

    public void testUnpadAllPadding() throws Exception {
        assertEquals("", padder.unpad("000"));
    }

    public void testReversability() throws Exception {
        String origin = "Abc";
        assertEquals(origin, padder.unpad(padder.pad(origin, 6)));
    }
}
