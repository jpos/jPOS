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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.jpos.util.Logger;
import org.junit.jupiter.api.Test;

public class EuroPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        EuroPackager euroPackager = new EuroPackager();
        assertNull(euroPackager.getLogger(), "euroPackager.getLogger()");
        assertNotNull(euroPackager.f48Packager, "euroPackager.f48Packager");
        assertEquals(129, euroPackager.fld.length, "euroPackager.fld.length");
        assertNull(euroPackager.getRealm(), "euroPackager.getRealm()");
    }

    @Test
    public void testEuro48PackagerConstructor() throws Throwable {
        EuroPackager.Euro48Packager euro48Packager = new EuroPackager.Euro48Packager();
        assertNull(euro48Packager.getLogger(), "euro48Packager.getLogger()");
        assertNull(euro48Packager.getRealm(), "euro48Packager.getRealm()");
    }

    @Test
    public void testSetLogger() throws Throwable {
        EuroPackager euroPackager = new EuroPackager();
        Logger logger = Logger.getLogger("testEuroPackagerName");
        euroPackager.setLogger(logger, "testEuroPackagerRealm");
        assertSame(logger, euroPackager.getLogger(), "euroPackager.getLogger()");
        assertNotNull(euroPackager.f48Packager, "euroPackager.f48Packager");
        assertEquals("testEuroPackagerRealm", euroPackager.getRealm(), "euroPackager.getRealm()");
    }
}
