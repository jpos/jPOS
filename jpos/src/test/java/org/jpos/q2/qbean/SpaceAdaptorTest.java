/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Set;

import javax.management.MalformedObjectNameException;

import org.jdom2.Element;
import org.junit.jupiter.api.Test;

public class SpaceAdaptorTest {

    @Test
    public void testConstructor() throws Throwable {
        SpaceAdaptor spaceAdaptor = new SpaceAdaptor();
        assertEquals("org.jpos.q2.qbean.SpaceAdaptor", spaceAdaptor.getLog().getRealm(), "spaceAdaptor.getLog().getRealm()");
        assertNull(spaceAdaptor.getKeys(), "spaceAdaptor.getKeys()");
        assertEquals(-1, spaceAdaptor.getState(), "spaceAdaptor.getState()");
        assertTrue(spaceAdaptor.isModified(), "spaceAdaptor.isModified()");
        assertNull(spaceAdaptor.getSpaceName(), "spaceAdaptor.getSpaceName()");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetKeys() throws Throwable {
        Set result = new SpaceAdaptor().getKeys();
        assertNull(result, "result");
    }

    @Test
    public void testSetSpaceNameThrowsNullPointerException() throws Throwable {
        SpaceAdaptor spaceAdaptor = new SpaceAdaptor();
        try {
            spaceAdaptor.setSpaceName("testSpaceAdaptorSpaceName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("testSpaceAdaptorSpaceName", spaceAdaptor.getSpaceName(), "spaceAdaptor.getSpaceName()");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildren(String)\" because the return value of \"org.jpos.q2.QBeanSupport.getPersist()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(spaceAdaptor.isModified(), "spaceAdaptor.isModified()");
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
            assertEquals(0, spaceAdaptor.getKeys().size(), "spaceAdaptor.getKeys().size()");
        }
    }

    @Test
    public void testStopServiceThrowsNullPointerException() throws Throwable {
        SpaceAdaptor spaceAdaptor = new SpaceAdaptor();
        try {
            spaceAdaptor.stopService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.getMBeanServer()\" because the return value of \"org.jpos.q2.qbean.SpaceAdaptor.getServer()\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
