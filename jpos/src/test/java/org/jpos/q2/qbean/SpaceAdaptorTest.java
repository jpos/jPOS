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

package org.jpos.q2.qbean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import javax.management.MalformedObjectNameException;

import org.jdom.Element;
import org.junit.Test;

public class SpaceAdaptorTest {

    @Test
    public void testConstructor() throws Throwable {
        SpaceAdaptor spaceAdaptor = new SpaceAdaptor();
        assertEquals("spaceAdaptor.getLog().getRealm()", "org.jpos.q2.qbean.SpaceAdaptor", spaceAdaptor.getLog().getRealm());
        assertNull("spaceAdaptor.getKeys()", spaceAdaptor.getKeys());
        assertEquals("spaceAdaptor.getState()", -1, spaceAdaptor.getState());
        assertTrue("spaceAdaptor.isModified()", spaceAdaptor.isModified());
        assertNull("spaceAdaptor.getSpaceName()", spaceAdaptor.getSpaceName());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetKeys() throws Throwable {
        Set result = new SpaceAdaptor().getKeys();
        assertNull("result", result);
    }

    @Test
    public void testSetSpaceNameThrowsNullPointerException() throws Throwable {
        SpaceAdaptor spaceAdaptor = new SpaceAdaptor();
        try {
            spaceAdaptor.setSpaceName("testSpaceAdaptorSpaceName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("spaceAdaptor.getSpaceName()", "testSpaceAdaptorSpaceName", spaceAdaptor.getSpaceName());
            assertNull("ex.getMessage()", ex.getMessage());
            assertFalse("spaceAdaptor.isModified()", spaceAdaptor.isModified());
        }
    }

    @Test
    public void testStartServiceThrowsMalformedObjectNameException() throws Throwable {
        SpaceAdaptor spaceAdaptor = new SpaceAdaptor();
        spaceAdaptor.setPersist(new Element("testSpaceAdaptorName", "testSpaceAdaptorUri"));
        spaceAdaptor.setSpaceName("testSpaceAdaptor\nSpaceName");
        try {
            spaceAdaptor.startService();
            fail("Expected MalformedObjectNameException to be thrown");
        } catch (MalformedObjectNameException ex) {
            assertEquals("spaceAdaptor.getKeys().size()", 0, spaceAdaptor.getKeys().size());
        }
    }

    @Test
    public void testStopServiceThrowsNullPointerException() throws Throwable {
        SpaceAdaptor spaceAdaptor = new SpaceAdaptor();
        try {
            spaceAdaptor.stopService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
