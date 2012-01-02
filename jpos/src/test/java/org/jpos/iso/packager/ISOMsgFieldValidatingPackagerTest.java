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
import static org.junit.Assert.fail;

import org.jpos.iso.IFA_BINARY;
import org.jpos.iso.IFB_AMOUNT;
import org.jpos.iso.IFB_LLLCHAR;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.junit.Test;

public class ISOMsgFieldValidatingPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        ISOPackager msgPackager = new ISOBaseValidatingPackager();
        ISOMsgFieldValidatingPackager iSOMsgFieldValidatingPackager = new ISOMsgFieldValidatingPackager(new IFB_LLLCHAR(),
                msgPackager);
        assertNull("iSOMsgFieldValidatingPackager.getDescription()", iSOMsgFieldValidatingPackager.getDescription());
        assertEquals("iSOMsgFieldValidatingPackager.getMaxPackedLength()", -1, iSOMsgFieldValidatingPackager.getMaxPackedLength());
        assertEquals("iSOMsgFieldValidatingPackager.getLength()", -1, iSOMsgFieldValidatingPackager.getLength());
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new ISOMsgFieldValidatingPackager(null, new ISOBaseValidatingPackager());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testValidateWithNonISOMsgThrowsNullPointerException() throws Throwable {
        try {
            new ISOMsgFieldValidatingPackager(new IFA_BINARY(100, "testISOMsgFieldValidatingPackagerDescription"),
                    new ISOBaseValidatingPackager()).validate(new ISOField(100));
            fail("Expected ClassCastException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("ex.getClass()", NullPointerException.class, ex.getClass());
        }
    }

    @Test
    public void testValidateThrowsNullPointerException() throws Throwable {
        try {
            new ISOMsgFieldValidatingPackager(new IFB_AMOUNT(100, "testISOMsgFieldValidatingPackagerDescription", true),
                    new ISOBaseValidatingPackager()).validate(new ISOMsg("testISOMsgFieldValidatingPackagerMti"));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
