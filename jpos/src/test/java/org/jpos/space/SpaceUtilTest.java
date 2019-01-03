/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2019 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.space;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class SpaceUtilTest {

    @Test
    public void testConstructor() throws Throwable {
        new SpaceUtil();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testInpAll() throws Throwable {
        Object[] result = SpaceUtil.inpAll(new TSpace(), "testString");
        assertEquals("result.length", 0, result.length);
    }

    @Test
    public void testInpAll1() throws Throwable {
        Space sp = SpaceFactory.getSpace("testSpaceUtilSpaceUri");
        SpaceUtil.nextLong(sp, "");
        Object[] result = SpaceUtil.inpAll(sp, "");
        assertEquals("(TSpace) sp.entries.size()", 0, ((TSpace) sp).entries.size());
        assertFalse("(TSpace) sp.entries.containsKey(\"\")", ((TSpace) sp).entries.containsKey(""));
        assertEquals("result.length", 1, result.length);
        assertEquals("result[0]", 1L, result[0]);
    }

    @Test
    public void testInpAllThrowsNullPointerException() throws Throwable {
        try {
            SpaceUtil.inpAll(null, "");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testNextLong() throws Throwable {
        Space sp = new TSpace();
        long result = SpaceUtil.nextLong(sp, "");
        assertEquals("(TSpace) sp.entries.size()", 1, ((TSpace) sp).entries.size());
        assertEquals("result", 1L, result);
    }

    @Test
    public void testNextLongThrowsNullPointerException() throws Throwable {
        Space sp = new TSpace();
        try {
            SpaceUtil.nextLong(sp, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("ex.getMessage()", "key=null, value=1", ex.getMessage());
            assertTrue("(TSpace) sp.isEmpty()", ((TSpace) sp).isEmpty());
        }
    }

    @Test
    public void testNextLongThrowsNullPointerException1() throws Throwable {
        try {
            SpaceUtil.nextLong(null, "");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testWipe1() throws Throwable {
        SpaceUtil.wipe(SpaceFactory.getSpace(), "");
        assertTrue("Test completed without Exception", true);
        // dependencies on static and environment state led to removal of 1
        // assertion
    }

    @Test
    public void testWipeThrowsNullPointerException() throws Throwable {
        try {
            SpaceUtil.wipe(null, "");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
