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

package org.jpos.q2.ui;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.jdom2.Element;
import org.junit.jupiter.api.Test;

public class UITest {

    @Test
    public void testConstructor() throws Throwable {
        UI uI = new UI();
        assertEquals("org.jpos.q2.ui.UI", uI.getLog().getRealm(), "uI.getLog().getRealm()");
        assertEquals(-1, uI.getState(), "uI.getState()");
        assertTrue(uI.isModified(), "uI.isModified()");
    }

    @Test
    public void testNewInstanceThrowsNullPointerException() throws Throwable {
        try {
            new UI().newInstance("testUIClazz");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.getFactory()\" because the return value of \"org.jpos.q2.QBeanSupport.getServer()\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testStartServiceThrowsNullPointerException() throws Throwable {
        UI uI = new UI();
        uI.setPersist(new Element("testUIName", "testUIPrefix", "testUIUri"));
        try {
            uI.startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.q2.Q2.getFactory()\" because the return value of \"org.jpos.q2.QBeanSupport.getServer()\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(uI.isModified(), "uI.isModified()");
            assertNull(uI.ui, "uI.ui");
        }
    }

    @Test
    public void testStartServiceThrowsNullPointerException1() throws Throwable {
        UI uI = new UI();
        try {
            uI.startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getAttributeValue(String, String)\" because \"config\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(uI.isModified(), "uI.isModified()");
            assertNull(uI.ui, "uI.ui");
        }
    }

    @Test
    public void testStopService() throws Throwable {
        UI uI = new UI();
        uI.setName("ui");
        uI.stopService();
        assertNull(uI.ui, "uI.ui");
    }
}
