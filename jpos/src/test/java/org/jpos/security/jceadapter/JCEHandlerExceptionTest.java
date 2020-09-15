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

package org.jpos.security.jceadapter;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class JCEHandlerExceptionTest {

    @Test
    public void testConstructor() throws Throwable {
        Exception e = new JCEHandlerException("testJCEHandlerExceptions");
        JCEHandlerException jCEHandlerException = new JCEHandlerException(e);
        assertEquals("org.jpos.security.jceadapter.JCEHandlerException: testJCEHandlerExceptions",
                jCEHandlerException.getMessage(), "jCEHandlerException.getMessage()");
        assertSame(e, jCEHandlerException.getNested(), "jCEHandlerException.getNested()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        JCEHandlerException jCEHandlerException = new JCEHandlerException("testJCEHandlerExceptions");
        assertEquals("testJCEHandlerExceptions", jCEHandlerException.getMessage(), "jCEHandlerException.getMessage()");
        assertNull(jCEHandlerException.getNested(), "jCEHandlerException.getNested()");
    }

    @Test
    public void testConstructor2() throws Throwable {
        Exception e = new Exception("testJCEHandlerExceptionParam1");
        JCEHandlerException jCEHandlerException = new JCEHandlerException("testJCEHandlerExceptions", e);
        assertEquals("testJCEHandlerExceptions", jCEHandlerException.getMessage(), "jCEHandlerException.getMessage()");
        assertSame(e, jCEHandlerException.getNested(), "jCEHandlerException.getNested()");
    }

    @Test
    public void testConstructor3() throws Throwable {
        JCEHandlerException jCEHandlerException = new JCEHandlerException();
        assertNull(jCEHandlerException.getNested(), "jCEHandlerException.getNested()");
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new JCEHandlerException((Exception) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.lang.Throwable.toString()\" because \"nested\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }
}
