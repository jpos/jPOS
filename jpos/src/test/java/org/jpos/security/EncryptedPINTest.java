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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.InvalidParameterException;

import org.junit.jupiter.api.Test;

public class EncryptedPINTest {

    @Test
    public void testConstructor() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN();
        assertNull(encryptedPIN.getAccountNumber(), "encryptedPIN.getAccountNumber()");
    }

    @Test
    public void testConstructor1() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, "13CharactersX");
        assertEquals("13Characters", encryptedPIN.accountNumber, "encryptedPIN.accountNumber");
        assertEquals((byte) 0, encryptedPIN.pinBlockFormat, "encryptedPIN.pinBlockFormat");
        assertEquals(17, encryptedPIN.pinBlock.length, "encryptedPIN.pinBlock.length");
    }

    @Test
    public void testConstructor2() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, "12Characters", false);
        assertEquals("12Characters", encryptedPIN.accountNumber, "encryptedPIN.accountNumber");
        assertEquals((byte) 0, encryptedPIN.pinBlockFormat, "encryptedPIN.pinBlockFormat");
        assertEquals(17, encryptedPIN.pinBlock.length, "encryptedPIN.pinBlock.length");
    }

    @Test
    public void testConstructor3() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, "11Character");
        assertEquals("0011Characte", encryptedPIN.accountNumber, "encryptedPIN.accountNumber");
        assertEquals((byte) 0, encryptedPIN.pinBlockFormat, "encryptedPIN.pinBlockFormat");
        assertEquals(17, encryptedPIN.pinBlock.length, "encryptedPIN.pinBlock.length");
    }

    @Test
    public void testConstructor4() throws Throwable {
        byte[] pinBlock = new byte[1];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "13CharactersX");
        assertEquals("13Characters", encryptedPIN.accountNumber, "encryptedPIN.accountNumber");
        assertEquals((byte) 0, encryptedPIN.pinBlockFormat, "encryptedPIN.pinBlockFormat");
        assertSame(pinBlock, encryptedPIN.pinBlock, "encryptedPIN.pinBlock");
    }

    @Test
    public void testConstructor5() throws Throwable {
        byte[] pinBlock = new byte[0];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "12Characters", false);
        assertEquals("12Characters", encryptedPIN.accountNumber, "encryptedPIN.accountNumber");
        assertEquals((byte) 0, encryptedPIN.pinBlockFormat, "encryptedPIN.pinBlockFormat");
        assertSame(pinBlock, encryptedPIN.pinBlock, "encryptedPIN.pinBlock");
    }

    @Test
    public void testConstructor6() throws Throwable {
        byte[] pinBlock = new byte[1];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "11Character");
        assertEquals("0011Characte", encryptedPIN.accountNumber, "encryptedPIN.accountNumber");
        assertEquals((byte) 0, encryptedPIN.pinBlockFormat, "encryptedPIN.pinBlockFormat");
        assertSame(pinBlock, encryptedPIN.pinBlock, "encryptedPIN.pinBlock");
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"s\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException1() throws Throwable {
        byte[] pinBlock = new byte[3];
        try {
            new EncryptedPIN(pinBlock, (byte) 0, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"s\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testConstructorUnevenNumberChars() throws Throwable {
        new EncryptedPIN("testEncryptedPINPinBlockHexString", (byte) 0, "testEncryptedPINAccountNumber");
    }

    @Test
    public void testDump() throws Throwable {
        byte[] pinBlock = new byte[0];
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-16BE");
        new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber").dump(p, "testEncryptedPINIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new EncryptedPIN().dump(null, "testEncryptedPINIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.io.PrintStream.print(String)\" because \"p\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testDumpThrowsNullPointerException1() throws Throwable {
        byte[] pinBlock = new byte[2];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber");
        encryptedPIN.setPINBlock(null);
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-16BE");
        try {
            encryptedPIN.dump(p, "testEncryptedPINIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read the array length because \"b\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testExtractAccountNumberPart() throws Throwable {
        String result = EncryptedPIN.extractAccountNumberPart("11Character");
        assertEquals("0011Characte", result, "result");
    }

    @Test
    public void testExtractAccountNumberPart1() throws Throwable {
        String result = EncryptedPIN.extractAccountNumberPart("13CharactersX");
        assertEquals("13Characters", result, "result");
    }

    @Test
    public void testExtractAccountNumberPart2() throws Throwable {
        String result = EncryptedPIN.extractAccountNumberPart("12Characters");
        assertEquals("012Character", result, "result");
    }

    @Test
    public void testExtractAccountNumberPartThrowsNullPointerException() throws Throwable {
        try {
            EncryptedPIN.extractAccountNumberPart(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"s\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetAccountNumber() throws Throwable {
        byte[] pinBlock = new byte[0];
        String result = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber").getAccountNumber();
        assertEquals("AccountNumbe", result, "result");
    }

    @Test
    public void testGetAccountNumber1() throws Throwable {
        String result = new EncryptedPIN().getAccountNumber();
        assertNull(result, "result");
    }

    @Test
    public void testGetPINBlock() throws Throwable {
        byte[] pinBlock = new byte[2];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber");
        byte[] pinBlock2 = new byte[3];
        encryptedPIN.setPINBlock(pinBlock2);
        byte[] result = encryptedPIN.getPINBlock();
        assertSame(pinBlock2, result, "result");
        assertEquals((byte) 0, pinBlock2[0], "pinBlock2[0]");
    }

    @Test
    public void testGetPINBlock1() throws Throwable {
        byte[] pinBlock = new byte[3];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber");
        byte[] pinBlock2 = new byte[0];
        encryptedPIN.setPINBlock(pinBlock2);
        byte[] result = encryptedPIN.getPINBlock();
        assertSame(pinBlock2, result, "result");
    }

    @Test
    public void testGetPINBlockFormat() throws Throwable {
        byte[] pinBlock = new byte[2];
        byte result = new EncryptedPIN(pinBlock, (byte) 100, "testEncryptedPINAccountNumber").getPINBlockFormat();
        assertEquals((byte) 100, result, "result");
    }

    @Test
    public void testGetPINBlockFormat1() throws Throwable {
        byte[] pinBlock = new byte[2];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber");
        encryptedPIN.setPINBlockFormat((byte) 0);
        byte result = encryptedPIN.getPINBlockFormat();
        assertEquals((byte) 0, result, "result");
    }

    @Test
    public void testSetAccountNumber() throws Throwable {
        byte[] pinBlock = new byte[3];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber");
        try {
            encryptedPIN.setAccountNumber("13CharactersX");
            fail("Expected InvalidParameterException");
        } catch (InvalidParameterException ignored) { }
    }

    @Test
    public void testSetAccountNumber1() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, "testEncryptedPINAccountNumber");
        encryptedPIN.setAccountNumber("12Characters");
        assertEquals("12Characters", encryptedPIN.accountNumber, "encryptedPIN.accountNumber");
    }

    @Test
    public void test11Chars() throws Throwable {
        byte[] pinBlock = new byte[3];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "11Character");
        assertEquals("0011Characte", encryptedPIN.accountNumber, "encryptedPIN.accountNumber");
    }

    @Test
    public void testSetAccountNumberThrowsNullPointerException() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, "testEncryptedPINAccountNumber");
        try {
            encryptedPIN.setAccountNumber(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"extractedAccountNumber\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals("AccountNumbe", encryptedPIN.accountNumber, "encryptedPIN.accountNumber");
        }
    }

    @Test
    public void testSetPINBlock() throws Throwable {
        byte[] pinBlock = new byte[3];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber");
        byte[] pinBlock2 = new byte[0];
        encryptedPIN.setPINBlock(pinBlock2);
        assertSame(pinBlock2, encryptedPIN.pinBlock, "encryptedPIN.pinBlock");
    }

    @Test
    public void testSetPINBlockFormat() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, "testEncryptedPINAccountNumber");
        encryptedPIN.setPINBlockFormat((byte) 100);
        assertEquals((byte) 100, encryptedPIN.pinBlockFormat, "encryptedPIN.pinBlockFormat");
    }
}
