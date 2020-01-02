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

import org.junit.jupiter.api.Test;

/**
 * @author joconnor
 */
public class IFA_AMOUNTTest {
    @Test
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "D123");
        IFA_AMOUNT packager = new IFA_AMOUNT(6, "Should be D00123");
        TestUtils.assertEquals(new byte[]{68, 48, 48, 49, 50, 51}, packager.pack(
                field));
    }

    @Test
    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{68, 48, 48, 49, 50, 51};
        IFA_AMOUNT packager = new IFA_AMOUNT(6, "Should be D00123");
        ISOField field = new ISOField();
        packager.unpack(field, raw, 0);
        assertEquals("D00123", (String) field.getValue());
    }

    @Test
    public void testReversability() throws Exception
    {
        String origin = "E0123456";
        ISOField f = new ISOField(12, origin);
        IFA_AMOUNT packager = new IFA_AMOUNT(8, "Should be E0123456");

        ISOField unpack = new ISOField();
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin, (String) unpack.getValue());
    }
}
