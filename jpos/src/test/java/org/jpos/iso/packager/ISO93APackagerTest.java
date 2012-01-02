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

import org.junit.Test;

public class ISO93APackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        ISO93APackager iSO93APackager = new ISO93APackager();
        assertNull("iSO93APackager.getLogger()", iSO93APackager.getLogger());
        assertEquals("iSO93APackager.fld.length", 129, iSO93APackager.fld.length);
        assertNull("iSO93APackager.getRealm()", iSO93APackager.getRealm());
    }
}
