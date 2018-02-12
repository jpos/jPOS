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

package org.jpos.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.jpos.security.jceadapter.JCEHandlerException;
import org.junit.Test;

public class SMExceptionTest {

    @Test
    public void testConstructor() throws Throwable {
        Exception e = new NumberFormatException("testSMExceptionParam1");
        SMException sMException = new SMException(e);
        assertEquals("sMException.getMessage()", "java.lang.NumberFormatException: testSMExceptionParam1", sMException.getMessage());
        assertNull("sMException.nested", sMException.nested);
        assertSame("sMException.getNested()", e, sMException.getNested());
    }

    @Test
    public void testConstructor1() throws Throwable {
        SMException sMException = new SMException();
        assertNull("sMException.nested", sMException.nested);
        assertNull("sMException.getNested()", sMException.getNested());
    }

    @Test
    public void testConstructor2() throws Throwable {
        Exception e = new IndexOutOfBoundsException("testSMExceptionParam1");
        SMException sMException = new SMException("testSMExceptions", e);
        assertEquals("sMException.getMessage()", "testSMExceptions", sMException.getMessage());
        assertNull("sMException.nested", sMException.nested);
        assertSame("sMException.getNested()", e, sMException.getNested());
    }

    @Test
    public void testConstructor3() throws Throwable {
        SMException sMException = new SMException("testSMExceptions");
        assertEquals("sMException.getMessage()", "testSMExceptions", sMException.getMessage());
        assertNull("sMException.nested", sMException.nested);
        assertNull("sMException.getNested()", sMException.getNested());
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new SMException((Exception) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetTagName() throws Throwable {
        SMException jCEHandlerException = new JCEHandlerException("testSMExceptions");
        String result = jCEHandlerException.getTagName();
        assertEquals("result", "security-module-exception", result);
    }
}
