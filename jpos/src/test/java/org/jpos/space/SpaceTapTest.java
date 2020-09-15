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

package org.jpos.space;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class SpaceTapTest {

    @Test
    public void testClose() throws Throwable {
        SpaceTap spaceTap = new SpaceTap(new TSpace(), "\u0001 ", Integer.valueOf(1), 100L);
        spaceTap.close();
        assertNull(spaceTap.ssp, "spaceTap.ssp");
    }

    @Test
    public void testClose1() throws Throwable {
        LocalSpace ssp = new TSpace();
        SpaceTap spaceTap = new SpaceTap(ssp, ssp, new Object(), "", 100L);
        spaceTap.close();
        spaceTap.close();
        assertNull(spaceTap.ssp, "spaceTap.ssp");
    }

    @Test
    public void testConstructor() throws Throwable {
        LocalSpace ssp = new TSpace();
        Object key = new Object();
        SpaceTap spaceTap = new SpaceTap(ssp, ssp, key, "", 100L);
        assertFalse(((TSpace) ssp).sl.isEmpty(), "(TSpace) ssp.sl.isEmpty()");
        assertSame(ssp, spaceTap.dsp, "spaceTap.dsp");
        assertSame(key, spaceTap.key, "spaceTap.key");
        assertEquals("", spaceTap.tapKey, "spaceTap.tapKey");
        assertEquals(100L, spaceTap.tapTimeout, "spaceTap.tapTimeout");
        assertSame(ssp, spaceTap.ssp, "spaceTap.ssp");
    }

    @Test
    public void testConstructorThrowsIllegalArgumentException() throws Throwable {
        LocalSpace ssp = new TSpace();
        Long key = Long.valueOf(100L);
        try {
            new SpaceTap(ssp, ssp, key, key, 100L);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("Possible deadlock - key equals tap-key within same space", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testConstructorThrowsIllegalArgumentException1() throws Throwable {
        LocalSpace sp = new TSpace();
        try {
            new SpaceTap(sp, "testString", "testString", 100L);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("Possible deadlock - key equals tap-key within same space", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException2() throws Throwable {
        LocalSpace ssp = new TSpace();
        try {
            new SpaceTap(ssp, ssp, null, "", 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"Object.equals(Object)\" because \"key\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException3() throws Throwable {
        try {
            new SpaceTap(null, "", "testString", 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.space.LocalSpace.addListener(Object, org.jpos.space.SpaceListener)\" because \"ssp\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testPlaceholder() {
        assertTrue(true);
    }

}
