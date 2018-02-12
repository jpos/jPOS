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
public class IFA_LLBNUMTest extends TestCase
{
    public void testPack() throws Exception
    {
        ISOField field = new ISOField(12, "123");
        IFA_LLBNUM packager = new IFA_LLBNUM(10, "Should be 0x30340123", true);
        TestUtils.assertEquals(new byte[]{0x30, 0x34, 0x01, 0x23}, packager.pack(
                field));
    }

    public void testUnpack() throws Exception
    {
        byte[] raw = new byte[]{0x30, 0x34, 0x01, 0x23};
        IFA_LLBNUM packager = new IFA_LLBNUM(10, "Should be 0x30330123", true);
        ISOField field = new ISOField();
        packager.unpack(field, raw, 0);
        assertEquals("0123", (String) field.getValue());
    }

    public void testReversability() throws Exception
    {
        String origin = "123456789";
        ISOField f = new ISOField(12, origin);
        IFA_LLBNUM packager = new IFA_LLBNUM(10, "Should be 0x31301234567890", false);

        ISOField unpack = new ISOField();
        packager.unpack(unpack, packager.pack(f), 0);
        assertEquals(origin + "0", (String) unpack.getValue());
    }
}
