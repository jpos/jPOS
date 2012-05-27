/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class SpaceFactoryTest {

    @Test
    public void testConstructor() throws Throwable {
	new SpaceFactory();
	assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGetSpace2() throws Throwable {
	TSpace result = (TSpace) SpaceFactory.getSpace("testString");
	assertTrue("result.isEmpty()", result.isEmpty());
    }

    public void testGetSpaceThrowsNullPointerException1() throws Throwable {
	try {
	    SpaceFactory.getSpace("testSpaceFactoryScheme",
		    "testSpaceFactoryName", "testSpaceFactoryParam");
	    fail("Expected NullPointerException to be thrown");
	} catch (NullPointerException ex) {
	    assertNull("ex.getMessage()", ex.getMessage());
	}
    }

    @Test(expected = NullPointerException.class)
    public void testGetSpaceThrowsNullPointerException2() throws Throwable {
	SpaceFactory.getSpace("testSpaceFactoryScheme", "testSpaceFactoryName",
		null);
    }

    @Test
    public void testGetSpaceThrowsSpaceError() throws Throwable {
	try {
	    SpaceFactory.getSpace("spacelet", "testSpaceFactoryName",
		    "testSpaceFactoryParam");
	    fail("Expected SpaceError to be thrown");
	} catch (SpaceError ex) {
	    assertEquals(
		    "ex.getMessage()",
		    "spacelet:testSpaceFactoryName:testSpaceFactoryParam not found.",
		    ex.getMessage());
	}
    }
}
