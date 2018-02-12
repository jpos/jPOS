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

package org.jpos.iso.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.core.Configuration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.junit.Test;

public class TEST01001Test {

    @Test
    public void testConstructor() throws Throwable {
        TEST0100 tEST0100 = new TEST0100(true);
        assertNull("tEST0100.getRealm()", tEST0100.getRealm());
        assertTrue("tEST0100.breakOnError()", tEST0100.breakOnError());
        assertNull("tEST0100.getLogger()", tEST0100.getLogger());
    }

    @Test
    public void testConstructor1() throws Throwable {
        TEST0100 tEST0100 = new TEST0100();
        assertNull("tEST0100.getRealm()", tEST0100.getRealm());
        assertFalse("tEST0100.breakOnError()", tEST0100.breakOnError());
        assertNull("tEST0100.getLogger()", tEST0100.getLogger());
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        TEST0100 tEST0100 = new TEST0100();
        Configuration cfg = new SubConfiguration();
        try {
            tEST0100.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testValidateThrowsClassCastException() throws Throwable {
        TEST0100 tEST0100 = new TEST0100();
        try {
            tEST0100.validate(new ISOField());
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals("ex.getClass()", ClassCastException.class, ex.getClass());
        }
    }

    @Test
    public void testValidateThrowsISOException() throws Throwable {
        TEST0100 tEST0100 = new TEST0100();
        try {
            tEST0100.validate(new ISOMsg());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "MTI not available", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testValidateThrowsNullPointerException() throws Throwable {
        TEST0100 tEST0100 = new TEST0100(true);
        try {
            tEST0100.validate(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
