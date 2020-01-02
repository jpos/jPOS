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

package org.jpos.iso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author joconnor
 */
public class IF_NOPTest {
    @Test
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IF_NOP packager = new IF_NOP();
        assertTrue(packager.pack(field).length == 0);
    }

    @Test
    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{};
        IF_NOP packager = new IF_NOP();
        ISOField field = new ISOField(12);
        assertEquals(0, packager.unpack(field, raw, 0));
        assertNull(field.getValue());
    }
}
