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

package org.jpos.ui.factory;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URL;

import javax.swing.event.HyperlinkEvent;

import org.junit.jupiter.api.Test;

public class HtmlFactoryTest {

    @Test
    public void testConstructor() throws Throwable {
        new HtmlFactory();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testListenerConstructor() throws Throwable {
        new HtmlFactory.Listener();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testListenerHyperlinkUpdate() throws Throwable {
        new HtmlFactory.Listener().hyperlinkUpdate(new HyperlinkEvent("", HyperlinkEvent.EventType.ENTERED, new URL(
                "ftp://q:77s35Ms8!q@uxlmi.net:4622/9M/aQ2Jlp_vr.gtvf")));
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testListenerHyperlinkUpdateThrowsClassCastException() throws Throwable {
        try {
            new HtmlFactory.Listener().hyperlinkUpdate(new HyperlinkEvent("1", HyperlinkEvent.EventType.ACTIVATED, new URL(
                    "ftp://q:77s35Ms8!q@uxlmi.net:4622/9M/aQ2Jlp_vr.gtvf")));
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals(ClassCastException.class, ex.getClass(), "ex.getClass()");
        }
    }

    @Test
    public void testListenerHyperlinkUpdateThrowsNullPointerException() throws Throwable {
        try {
            new HtmlFactory.Listener().hyperlinkUpdate(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"javax.swing.event.HyperlinkEvent.getEventType()\" because \"e\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
