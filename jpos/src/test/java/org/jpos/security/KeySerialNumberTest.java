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

package org.jpos.security;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class KeySerialNumberTest {

    @Test
    public void testConstructor() throws Throwable {
        KeySerialNumber keySerialNumber = new KeySerialNumber();
        assertNull(keySerialNumber.getBaseKeyID(), "keySerialNumber.getBaseKeyID()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        KeySerialNumber keySerialNumber = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter");
        assertEquals("testKeySerialNumberBaseKeyID", keySerialNumber.baseKeyID, "keySerialNumber.baseKeyID");
        assertEquals("testKeySerialNumberDeviceID", keySerialNumber.deviceID, "keySerialNumber.deviceID");
        assertEquals("testKeySerialNumberTransactionCounter",
                keySerialNumber.transactionCounter, "keySerialNumber.transactionCounter");
    }

    @Test
    public void testDump() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        Object[] objects = new Object[1];
        p.format("testKeySerialNumberParam1", objects);
        new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID", "testKeySerialNumberTransactionCounter")
                .dump(p, "testKeySerialNumberIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new KeySerialNumber().dump(null, "testKeySerialNumberIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.PrintStream.println(String)\" because \"p\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetBaseKeyID() throws Throwable {
        String result = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter").getBaseKeyID();
        assertEquals("testKeySerialNumberBaseKeyID", result, "result");
    }

    @Test
    public void testGetDeviceID() throws Throwable {
        String result = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter").getDeviceID();
        assertEquals("testKeySerialNumberDeviceID", result, "result");
    }

    @Test
    public void testGetTransactionCounter() throws Throwable {
        String result = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter").getTransactionCounter();
        assertEquals("testKeySerialNumberTransactionCounter", result, "result");
    }

    @Test
    public void testGetTransactionCounter1() throws Throwable {
        String result = new KeySerialNumber().getTransactionCounter();
        assertNull(result, "result");
    }

    @Test
    public void testSetBaseKeyID() throws Throwable {
        KeySerialNumber keySerialNumber = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter");
        keySerialNumber.setBaseKeyID("testKeySerialNumberBaseKeyID1");
        assertEquals("testKeySerialNumberBaseKeyID1", keySerialNumber.baseKeyID, "keySerialNumber.baseKeyID");
    }

    @Test
    public void testSetDeviceID() throws Throwable {
        KeySerialNumber keySerialNumber = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter");
        keySerialNumber.setDeviceID("testKeySerialNumberDeviceID1");
        assertEquals("testKeySerialNumberDeviceID1", keySerialNumber.deviceID, "keySerialNumber.deviceID");
    }

    @Test
    public void testSetTransactionCounter() throws Throwable {
        KeySerialNumber keySerialNumber = new KeySerialNumber("testKeySerialNumberBaseKeyID", "testKeySerialNumberDeviceID",
                "testKeySerialNumberTransactionCounter");
        keySerialNumber.setTransactionCounter("testKeySerialNumberTransactionCounter1");
        assertEquals("testKeySerialNumberTransactionCounter1",
                keySerialNumber.transactionCounter, "keySerialNumber.transactionCounter");
    }
}
