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

import org.jdom2.Element;
import org.junit.jupiter.api.Test;

public class SpaceProxyTest {

    @Test
    public void testConstructor() throws Throwable {
        SpaceProxyAdaptor spaceProxyAdaptor = new SpaceProxyAdaptor();
        assertNull(spaceProxyAdaptor.getSpaceName(), "spaceProxyAdaptor.getSpaceName()");
        assertEquals("org.jpos.q2.qbean.SpaceProxyAdaptor", spaceProxyAdaptor.getLog()
                .getRealm(), "spaceProxyAdaptor.getLog().getRealm()");
        assertEquals(-1, spaceProxyAdaptor.getState(), "spaceProxyAdaptor.getState()");
        assertTrue(spaceProxyAdaptor.isModified(), "spaceProxyAdaptor.isModified()");
    }

    @Test
    public void testGetKeysThrowsNullPointerException() throws Throwable {
        try {
            new SpaceProxyAdaptor().getKeys();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.space.SpaceProxy.getKeySet()\" because \"this.sp\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testSetSpaceName() throws Throwable {
        SpaceProxyAdaptor spaceProxyAdaptor = new SpaceProxyAdaptor();
        spaceProxyAdaptor.setPersist(new Element("testSpaceProxyAdaptorName"));
        spaceProxyAdaptor.setSpaceName("testSpaceProxyAdaptorSpaceName");
        assertEquals("testSpaceProxyAdaptorSpaceName", spaceProxyAdaptor.getSpaceName(), "spaceProxyAdaptor.getSpaceName()");
        assertTrue(spaceProxyAdaptor.isModified(), "spaceProxyAdaptor.isModified()");
    }

    @Test
    public void testSetSpaceNameThrowsNullPointerException() throws Throwable {
        SpaceProxyAdaptor spaceProxyAdaptor = new SpaceProxyAdaptor();
        try {
            spaceProxyAdaptor.setSpaceName("testSpaceProxyAdaptorSpaceName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("testSpaceProxyAdaptorSpaceName", spaceProxyAdaptor.getSpaceName(), "spaceProxyAdaptor.getSpaceName()");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChildren(String)\" because the return value of \"org.jpos.q2.QBeanSupport.getPersist()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(spaceProxyAdaptor.isModified(), "spaceProxyAdaptor.isModified()");
        }
    }

    @Test
    public void testStopServiceThrowsNullPointerException() throws Throwable {
        SpaceProxyAdaptor spaceProxyAdaptor = new SpaceProxyAdaptor();
        try {
            spaceProxyAdaptor.stopService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.space.SpaceProxy.shutdown()\" because \"this.sp\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
