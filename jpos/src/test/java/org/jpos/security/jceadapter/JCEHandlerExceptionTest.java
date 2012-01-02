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

package org.jpos.security.jceadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

public class JCEHandlerExceptionTest {

    @Test
    public void testConstructor() throws Throwable {
        Exception e = new JCEHandlerException("testJCEHandlerExceptions");
        JCEHandlerException jCEHandlerException = new JCEHandlerException(e);
        assertEquals("jCEHandlerException.getMessage()",
                "org.jpos.security.jceadapter.JCEHandlerException: testJCEHandlerExceptions", jCEHandlerException.getMessage());
        assertSame("jCEHandlerException.getNested()", e, jCEHandlerException.getNested());
    }

    @Test
    public void testConstructor1() throws Throwable {
        JCEHandlerException jCEHandlerException = new JCEHandlerException("testJCEHandlerExceptions");
        assertEquals("jCEHandlerException.getMessage()", "testJCEHandlerExceptions", jCEHandlerException.getMessage());
        assertNull("jCEHandlerException.getNested()", jCEHandlerException.getNested());
    }

    @Test
    public void testConstructor2() throws Throwable {
        Exception e = new Exception("testJCEHandlerExceptionParam1");
        JCEHandlerException jCEHandlerException = new JCEHandlerException("testJCEHandlerExceptions", e);
        assertEquals("jCEHandlerException.getMessage()", "testJCEHandlerExceptions", jCEHandlerException.getMessage());
        assertSame("jCEHandlerException.getNested()", e, jCEHandlerException.getNested());
    }

    @Test
    public void testConstructor3() throws Throwable {
        JCEHandlerException jCEHandlerException = new JCEHandlerException();
        assertNull("jCEHandlerException.getNested()", jCEHandlerException.getNested());
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new JCEHandlerException((Exception) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
