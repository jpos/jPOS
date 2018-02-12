/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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

import junit.framework.TestCase;

/**
 * @author joconnor
 */
public class IF_CHARTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "ABCD");
        IF_CHAR packager = new IF_CHAR(10, "Should be ABCD      ");
        TestUtils.assertEquals(new byte[]{65, 66, 67, 68, 32, 32, 32, 32, 32, 32}, packager.pack(
                field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{65, 66, 67, 68, 32, 32, 32, 32, 32, 32};
        IF_CHAR packager = new IF_CHAR(10, "Should be ABCD      ");
        ISOField field = new ISOField(12);
        packager.unpack(field, raw, 0);
        assertEquals("ABCD      ", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "Abc123:.-";
        ISOField f = new ISOField(12, origin);
        IF_CHAR packager = new IF_CHAR(10, "Should be ABCD      ");

        ISOField unpack = new ISOField(12);
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin + " ", (String) unpack.getValue());
    }
}
