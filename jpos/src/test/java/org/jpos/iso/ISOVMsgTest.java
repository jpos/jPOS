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

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ListIterator;

import org.junit.jupiter.api.Test;

public class ISOVMsgTest {

    @Test
    public void testAddISOVError() throws Throwable {
        ISOVError FirstError = new ISOVError("testISOVMsgDescription");
        ISOVMsg iSOVMsg = new ISOVMsg(new ISOMsg(100), FirstError);
        boolean result = iSOVMsg.addISOVError(new ISOVError("testISOVMsgDescription1"));
        assertEquals(2, iSOVMsg.errors.size(), "iSOVMsg.errors.size()");
        assertSame(FirstError, iSOVMsg.errors.get(0), "iSOVMsg.errors.get(0)");
        assertTrue(result, "result");
    }

    @Test
    public void testConstructor() throws Throwable {
        ISOVMsg iSOVMsg = new ISOVMsg(new ISOMsg("testISOVMsgMti"));
        assertEquals(1, iSOVMsg.fields.size(), "iSOVMsg.fields.size()");
        assertEquals(0, iSOVMsg.direction, "iSOVMsg.direction");
        assertEquals(0, iSOVMsg.errors.size(), "iSOVMsg.errors.size()");
        assertNull(iSOVMsg.header, "iSOVMsg.header");
        assertEquals(0, iSOVMsg.maxField, "iSOVMsg.maxField");
        assertTrue(iSOVMsg.dirty, "iSOVMsg.dirty");
        assertNull(iSOVMsg.packager, "iSOVMsg.packager");
        assertEquals(-1, iSOVMsg.fieldNumber, "iSOVMsg.fieldNumber");
        assertTrue(iSOVMsg.maxFieldDirty, "iSOVMsg.maxFieldDirty");
    }

    @Test
    public void testConstructor1() throws Throwable {
        ISOVMsg iSOVMsg = new ISOVMsg(new ISOMsg("testISOVMsgMti"), new ISOVError("testISOVMsgDescription", "testISOVMsgRejectCode"));
        assertEquals(1, iSOVMsg.fields.size(), "iSOVMsg.fields.size()");
        assertEquals(0, iSOVMsg.direction, "iSOVMsg.direction");
        assertEquals(1, iSOVMsg.errors.size(), "iSOVMsg.errors.size()");
        assertNull(iSOVMsg.header, "iSOVMsg.header");
        assertEquals(0, iSOVMsg.maxField, "iSOVMsg.maxField");
        assertTrue(iSOVMsg.dirty, "iSOVMsg.dirty");
        assertNull(iSOVMsg.packager, "iSOVMsg.packager");
        assertEquals(-1, iSOVMsg.fieldNumber, "iSOVMsg.fieldNumber");
        assertTrue(iSOVMsg.maxFieldDirty, "iSOVMsg.maxFieldDirty");
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new ISOVMsg(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read field \"packager\" because \"Source\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException1() throws Throwable {
        try {
            new ISOVMsg(null, new ISOVError("testISOVMsgDescription"));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read field \"packager\" because \"Source\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testErrorListIterator() throws Throwable {
        ISOVMsg iSOVMsg = new ISOVMsg(new ISOMsg("testISOVMsgMti"));
        ListIterator<?> result = iSOVMsg.errorListIterator();
        assertFalse(result.hasNext(), "result.hasNext()");
        assertEquals(0, iSOVMsg.errors.size(), "iSOVMsg.errors.size()");
    }

    @Test
    public void testErrorListIterator1() throws Throwable {
        ISOVMsg iSOVMsg = new ISOVMsg(new ISOMsg("testISOVMsgMti"), new ISOVError("testISOVMsgDescription", "testISOVMsgRejectCode"));
        ListIterator<?> result = iSOVMsg.errorListIterator();
        assertTrue(result.hasNext(), "result.hasNext()");
        assertEquals(1, iSOVMsg.errors.size(), "iSOVMsg.errors.size()");
    }
}
