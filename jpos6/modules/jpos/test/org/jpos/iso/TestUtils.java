/*
 * Copyright (c) 2006 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import junit.framework.Assert;

/**
 * @author joconnor
 */
public class TestUtils {
    /** Tests for equality of array elements */
    public static void assertEquals(Object[] expected, Object[] was) {
        Assert.assertEquals("Wrong size array", expected.length, was.length);
        for (int i= 0; i < expected.length; i++) {
            Assert.assertEquals("Non-equal objects at index " + i, expected[i], was[i]);
        }
    }

    /** Tests for equality of array elements */
    public static void assertEquals(byte[] expected, byte[] was) {
        if (expected.length != was.length) {
            for (int i = 0; i < expected.length && i < was.length; i++) {
                Assert.assertEquals("Arrays different lengths. Non-equal objects at index " + i, expected[i], was[i]);
            }
            // Following always fails, but gives a nice error message
            Assert.assertEquals("Wrong size array", expected.length, was.length);
        } else {
            for (int i= 0; i < expected.length; i++) {
                Assert.assertEquals("Non-equal objects at index " + i, expected[i], was[i]);
            }
        }
    }
}
