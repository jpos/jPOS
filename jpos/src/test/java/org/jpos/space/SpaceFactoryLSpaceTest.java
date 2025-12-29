/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2025 jPOS Software SRL
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test SpaceFactory support for LSpace creation via 'lspace' URI prefix.
 */
public class SpaceFactoryLSpaceTest {

    @Test
    public void testCreateLSpaceViaFactory() {
        Space sp = SpaceFactory.getSpace("lspace:test");
        assertNotNull(sp, "Space should be created");
        assertTrue(sp instanceof LSpace, "Should be an instance of LSpace");
        assertTrue(sp instanceof LocalSpace, "Should implement LocalSpace");
    }

    @Test
    public void testLSpaceBasicOperations() {
        Space sp = SpaceFactory.getSpace("lspace:functional-test");

        // Test basic operations
        sp.out("testKey", "testValue");
        assertEquals("testValue", sp.rdp("testKey"));
        assertEquals("testValue", sp.inp("testKey"));
        assertNull(sp.rdp("testKey"));
    }

    @Test
    public void testDefaultSpaceIsTSpace() {
        // Default should still be TSpace
        Space sp = SpaceFactory.getSpace();
        assertNotNull(sp);
        assertTrue(sp instanceof TSpace, "Default space should be TSpace");
    }

    @Test
    public void testTSpacePrefixStillWorks() {
        Space sp = SpaceFactory.getSpace("tspace:legacy");
        assertNotNull(sp);
        assertTrue(sp instanceof TSpace, "tspace prefix should create TSpace");
    }
}
