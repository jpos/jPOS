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

package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jpos.iso.X92_BITMAP;
import org.junit.Test;

public class X92PackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        X92Packager x92Packager = new X92Packager();
        assertEquals("x92Packager.fld.length", 65, x92Packager.fld.length);
        assertNull("x92Packager.getLogger()", x92Packager.getLogger());
        assertNull("x92Packager.getRealm()", x92Packager.getRealm());
        assertEquals("x92Packager.bitMapPackager.getDescription()", "X9.2 BIT MAP", x92Packager.bitMapPackager.getDescription());
    }

    @Test
    public void testEmitBitMap() throws Throwable {
        X92Packager x92Packager = new X92Packager();
        boolean result = x92Packager.emitBitMap();
        assertTrue("result", result);
    }

    @Test
    public void testGetBitMapfieldPackager() throws Throwable {
        X92Packager x92Packager = new X92Packager();
        X92_BITMAP result = (X92_BITMAP) x92Packager.getBitMapfieldPackager();
        assertEquals("result.getMaxPackedLength()", 4, result.getMaxPackedLength());
    }

    @Test
    public void testGetMaxValidField() throws Throwable {
        X92Packager x92Packager = new X92Packager();
        int result = x92Packager.getMaxValidField();
        assertEquals("result", 64, result);
    }
}
