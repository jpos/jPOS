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

package org.jpos.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class KeySerialNumberTest {

    @Test
    public void testConstructor() throws Throwable {
        KeySerialNumber keySerialNumber = new KeySerialNumber();
        assertNull("keySerialNumber.getBaseKeyID()", keySerialNumber.getBaseKeyID());
    }

    @Test
    public void testConstructor1() throws Throwable {
        KeySerialNumber keySerialNumber = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter");
        assertEquals("keySerialNumber.baseKeyID", "testKeySerialNumberBaseKeyID", keySerialNumber.baseKeyID);
        assertEquals("keySerialNumber.deviceID", "testKeySerialNumberDeviceID", keySerialNumber.deviceID);
        assertEquals("keySerialNumber.transactionCounter", "testKeySerialNumberTransactionCounter",
                keySerialNumber.transactionCounter);
    }

    @Test
    public void testDump() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        Object[] objects = new Object[1];
        p.format("testKeySerialNumberParam1", objects);
        new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID", "testKeySerialNumberTransactionCounter")
                .dump(p, "testKeySerialNumberIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new KeySerialNumber().dump(null, "testKeySerialNumberIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetBaseKeyID() throws Throwable {
        String result = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter").getBaseKeyID();
        assertEquals("result", "testKeySerialNumberBaseKeyID", result);
    }

    @Test
    public void testGetDeviceID() throws Throwable {
        String result = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter").getDeviceID();
        assertEquals("result", "testKeySerialNumberDeviceID", result);
    }

    @Test
    public void testGetTransactionCounter() throws Throwable {
        String result = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter").getTransactionCounter();
        assertEquals("result", "testKeySerialNumberTransactionCounter", result);
    }

    @Test
    public void testGetTransactionCounter1() throws Throwable {
        String result = new KeySerialNumber().getTransactionCounter();
        assertNull("result", result);
    }

    @Test
    public void testSetBaseKeyID() throws Throwable {
        KeySerialNumber keySerialNumber = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter");
        keySerialNumber.setBaseKeyID("testKeySerialNumberBaseKeyID1");
        assertEquals("keySerialNumber.baseKeyID", "testKeySerialNumberBaseKeyID1", keySerialNumber.baseKeyID);
    }

    @Test
    public void testSetDeviceID() throws Throwable {
        KeySerialNumber keySerialNumber = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter");
        keySerialNumber.setDeviceID("testKeySerialNumberDeviceID1");
        assertEquals("keySerialNumber.deviceID", "testKeySerialNumberDeviceID1", keySerialNumber.deviceID);
    }

    @Test
    public void testSetTransactionCounter() throws Throwable {
        KeySerialNumber keySerialNumber = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter");
        keySerialNumber.setTransactionCounter("testKeySerialNumberTransactionCounter1");
        assertEquals("keySerialNumber.transactionCounter", "testKeySerialNumberTransactionCounter1",
                keySerialNumber.transactionCounter);
    }
}
