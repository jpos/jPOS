/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.TestCase;

import org.jpos.iso.RightPadder;

/**
 * @author joconnor
 */
public class RightPadderTest extends TestCase {
    private RightPadder padder;
    
    protected void setUp() {
        padder = new RightPadder('0');
    }
    
    public void testPaddingNeeded() throws Exception {
        assertEquals("123000", padder.pad("123", 6));
    }

    public void testNoPaddingNeeded() throws Exception {
        assertEquals("123", padder.pad("123", 3));
    }

    public void testPadLengthTooShort() throws Exception {
        try {
            padder.pad("123", 2);
            fail("Padding a bigger string into a smaller buffer should throw an exception");
        } catch (Exception asIExpected) {
        }
    }

    public void testUnpad1() throws Exception {
        assertEquals("123", padder.unpad("123000"));
    }

    public void testUnpad2() throws Exception {
        assertEquals("123", padder.unpad("123"));
    }

    public void testUnpad3() throws Exception {
        assertEquals("1203", padder.unpad("1203000"));
    }

    public void testUnpadAllPadding() throws Exception {
        assertEquals("", padder.unpad("000"));
    }

    public void testReversability() throws Exception {
        String origin = "Abc";
        assertEquals(origin, padder.unpad(padder.pad(origin, 6)));
    }
}
