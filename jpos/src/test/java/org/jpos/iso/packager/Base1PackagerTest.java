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

package org.jpos.iso.packager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class Base1PackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        Base1Packager base1Packager = new Base1Packager();
        assertNull(base1Packager.getLogger(), "base1Packager.getLogger()");
        assertEquals(129, base1Packager.base1Fld.length, "base1Packager.base1Fld.length");
        assertNull(base1Packager.getRealm(), "base1Packager.getRealm()");
    }

    @Test
    public void testF126PackagerConstructor() throws Throwable {
        Base1Packager.F126Packager f126Packager = new Base1Packager.F126Packager();
        assertNull(f126Packager.getLogger(), "f126Packager.getLogger()");
        assertNull(f126Packager.getRealm(), "f126Packager.getRealm()");
        assertEquals(11, f126Packager.fld126.length, "f126Packager.fld126.length");
    }

    @Test
    public void testF127PackagerConstructor() throws Throwable {
        Base1Packager.F127Packager f127Packager = new Base1Packager.F127Packager();
        assertNull(f127Packager.getLogger(), "f127Packager.getLogger()");
        assertNull(f127Packager.getRealm(), "f127Packager.getRealm()");
        assertEquals(6, f127Packager.fld127.length, "f127Packager.fld127.length");
    }
}
