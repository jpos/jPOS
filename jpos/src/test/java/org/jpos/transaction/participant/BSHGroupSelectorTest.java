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

package org.jpos.transaction.participant;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.EOFException;
import java.io.StreamCorruptedException;

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.junit.jupiter.api.Test;

public class BSHGroupSelectorTest {

    @Test
    public void testConstructor() throws Throwable {
        BSHGroupSelector bSHGroupSelector = new BSHGroupSelector();
        assertNull(bSHGroupSelector.getRealm(), "bSHGroupSelector.getRealm()");
        assertNull(bSHGroupSelector.getLogger(), "bSHGroupSelector.getLogger()");
    }

    @Test
    public void testDefaultSelect() throws Throwable {
        String result = new BSHGroupSelector().defaultSelect(100L, new StreamCorruptedException());
        assertEquals("", result, "result");
    }

    @Test
    public void testSelect() throws Throwable {
        String result = new BSHGroupSelector().select(100L, new EOFException());
        assertEquals("", result, "result");
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BSHGroupSelector bSHGroupSelector = new BSHGroupSelector();
        bSHGroupSelector.setConfiguration(new Element("testBSHGroupSelectorName", "testBSHGroupSelectorUri"));
        assertNull(bSHGroupSelector.prepareForAbortMethod, "bSHGroupSelector.prepareForAbortMethod");
        assertNull(bSHGroupSelector.selectMethod, "bSHGroupSelector.selectMethod");
        assertNull(bSHGroupSelector.commitMethod, "bSHGroupSelector.commitMethod");
        assertNull(bSHGroupSelector.abortMethod, "bSHGroupSelector.abortMethod");
        assertNull(bSHGroupSelector.prepareMethod, "bSHGroupSelector.prepareMethod");
        assertFalse(bSHGroupSelector.trace, "bSHGroupSelector.trace");
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException() throws Throwable {
        BSHGroupSelector bSHGroupSelector = new BSHGroupSelector();
        try {
            bSHGroupSelector.setConfiguration(null);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
                assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChild(String)\" because \"e\" is null", ex.getMessage(), "ex.getMessage()");
                assertEquals("Cannot invoke \"org.jdom2.Element.getChild(String)\" because \"e\" is null", ex.getNested().getMessage(), "ex.getNested().getMessage()");
            }
            assertNull(bSHGroupSelector.prepareForAbortMethod, "bSHGroupSelector.prepareForAbortMethod");
            assertNull(bSHGroupSelector.selectMethod, "bSHGroupSelector.selectMethod");
            assertNull(bSHGroupSelector.commitMethod, "bSHGroupSelector.commitMethod");
            assertNull(bSHGroupSelector.abortMethod, "bSHGroupSelector.abortMethod");
            assertNull(bSHGroupSelector.prepareMethod, "bSHGroupSelector.prepareMethod");
            assertFalse(bSHGroupSelector.trace, "bSHGroupSelector.trace");
        }
    }
}
