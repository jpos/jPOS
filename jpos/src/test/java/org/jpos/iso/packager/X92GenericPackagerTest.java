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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

import org.jpos.iso.ISOException;
import org.jpos.iso.X92_BITMAP;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

public class X92GenericPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        X92GenericPackager x92GenericPackager = new X92GenericPackager();
        assertNull(x92GenericPackager.getLogger(), "x92GenericPackager.getLogger()");
        assertNull(x92GenericPackager.getRealm(), "x92GenericPackager.getRealm()");
    }

    @Test
    public void testConstructorThrowsISOException() throws Throwable {
        byte[] bytes = new byte[1];
        try {
            new X92GenericPackager(new ByteArrayInputStream(bytes));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals(SAXParseException.class, ex.getNested().getClass(), "ex.getNested().getClass()");
            assertEquals("Content is not allowed in prolog.", ex.getNested().getMessage(), "ex.getNested().getMessage()");
        }
    }

    @Test
    public void testConstructorThrowsISOException1() throws Throwable {
        try {
            new X92GenericPackager("testX92GenericPackagerFilename");
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals(FileNotFoundException.class, ex.getNested().getClass(), "ex.getNested().getClass()");
        }
    }

    @Test
    public void testEmitBitMap() throws Throwable {
        X92GenericPackager x92GenericPackager = new X92GenericPackager();
        boolean result = x92GenericPackager.emitBitMap();
        assertTrue(result, "result");
    }

    @Test
    public void testGetBitMapfieldPackager() throws Throwable {
        X92GenericPackager x92GenericPackager = new X92GenericPackager();
        X92_BITMAP result = (X92_BITMAP) x92GenericPackager.getBitMapfieldPackager();
        assertEquals(4, result.getMaxPackedLength(), "result.getMaxPackedLength()");
    }

    @Test
    public void testGetMaxValidField() throws Throwable {
        X92GenericPackager x92GenericPackager = new X92GenericPackager();
        int result = x92GenericPackager.getMaxValidField();
        assertEquals(64, result, "result");
    }
}
