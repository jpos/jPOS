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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.X92_BITMAP;
import org.jpos.util.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Base1SubFieldPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        Base1SubFieldPackager base1SubFieldPackager = new Base1SubFieldPackager();
        assertNull("base1SubFieldPackager.getLogger()", base1SubFieldPackager.getLogger());
        assertNull("base1SubFieldPackager.getRealm()", base1SubFieldPackager.getRealm());
    }

    @Test
    public void testEmitBitMap() throws Throwable {
        Base1SubFieldPackager base1SubFieldPackager = new Base1SubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[1];
        base1SubFieldPackager.setFieldPackager(fld);
        boolean result = base1SubFieldPackager.emitBitMap();
        assertFalse("result", result);
    }

    @Test
    public void testEmitBitMapThrowsArrayIndexOutOfBoundsException() throws Throwable {
        Base1SubFieldPackager f126Packager = new Base1Packager.F126Packager();
        ISOFieldPackager[] fld = new ISOFieldPackager[0];
        f126Packager.setFieldPackager(fld);

        try {
            f126Packager.emitBitMap();
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "0", ex.getMessage());

        }
    }

    @Test
    public void testGetBitMapfieldPackager() throws Throwable {
        Base1SubFieldPackager f126Packager = new Base1Packager.F126Packager();
        Base1_BITMAP126 result = (Base1_BITMAP126) f126Packager.getBitMapfieldPackager();
        assertEquals("result.getMaxPackedLength()", 2, result.getMaxPackedLength());
    }

    @Test
    public void testGetBitMapfieldPackagerThrowsNullPointerException() throws Throwable {
        Base1SubFieldPackager base1SubFieldPackager = new Base1SubFieldPackager();
        try {
            base1SubFieldPackager.getBitMapfieldPackager();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetFirstField() throws Throwable {
        Base1SubFieldPackager base1SubFieldPackager = new Base1SubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[2];
        fld[0] = new X92_BITMAP(100, "testBase1SubFieldPackagerDescription");
        base1SubFieldPackager.setFieldPackager(fld);
        int result = base1SubFieldPackager.getFirstField();
        assertEquals("result", 1, result);
    }

    @Test
    public void testGetFirstField1() throws Throwable {
        ISOFieldPackager[] fld = new ISOFieldPackager[1];
        Base1SubFieldPackager f126Packager = new Base1Packager.F126Packager();
        f126Packager.setFieldPackager(fld);
        int result = f126Packager.getFirstField();
        assertEquals("result", 0, result);
    }

    @Test
    public void testGetFirstFieldThrowsNullPointerException() throws Throwable {
        Base1SubFieldPackager base1SubFieldPackager = new Base1SubFieldPackager();
        try {
            base1SubFieldPackager.getFirstField();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testUnpack() throws Throwable {
        Base1SubFieldPackager base1SubFieldPackager = new Base1SubFieldPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[3];
        base1SubFieldPackager.setFieldPackager(fld);
        base1SubFieldPackager.setLogger(new Logger(), "testBase1SubFieldPackagerRealm");
        byte[] b = new byte[0];
        int result = base1SubFieldPackager.unpack(new ISOMsg(100), b);
        assertEquals("result", 0, result);
    }

}
