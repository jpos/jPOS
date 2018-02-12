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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.InvalidParameterException;

import org.junit.Test;

public class EncryptedPINTest {

    @Test
    public void testConstructor() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN();
        assertNull("encryptedPIN.getAccountNumber()", encryptedPIN.getAccountNumber());
    }

    @Test
    public void testConstructor1() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, "13CharactersX");
        assertEquals("encryptedPIN.accountNumber", "13Characters", encryptedPIN.accountNumber);
        assertEquals("encryptedPIN.pinBlockFormat", (byte) 0, encryptedPIN.pinBlockFormat);
        assertEquals("encryptedPIN.pinBlock.length", 17, encryptedPIN.pinBlock.length);
    }

    @Test
    public void testConstructor2() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, "12Characters", false);
        assertEquals("encryptedPIN.accountNumber", "12Characters", encryptedPIN.accountNumber);
        assertEquals("encryptedPIN.pinBlockFormat", (byte) 0, encryptedPIN.pinBlockFormat);
        assertEquals("encryptedPIN.pinBlock.length", 17, encryptedPIN.pinBlock.length);
    }

    @Test
    public void testConstructor3() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, "11Character");
        assertEquals("encryptedPIN.accountNumber", "0011Characte", encryptedPIN.accountNumber);
        assertEquals("encryptedPIN.pinBlockFormat", (byte) 0, encryptedPIN.pinBlockFormat);
        assertEquals("encryptedPIN.pinBlock.length", 17, encryptedPIN.pinBlock.length);
    }

    @Test
    public void testConstructor4() throws Throwable {
        byte[] pinBlock = new byte[1];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "13CharactersX");
        assertEquals("encryptedPIN.accountNumber", "13Characters", encryptedPIN.accountNumber);
        assertEquals("encryptedPIN.pinBlockFormat", (byte) 0, encryptedPIN.pinBlockFormat);
        assertSame("encryptedPIN.pinBlock", pinBlock, encryptedPIN.pinBlock);
    }

    @Test
    public void testConstructor5() throws Throwable {
        byte[] pinBlock = new byte[0];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "12Characters", false);
        assertEquals("encryptedPIN.accountNumber", "12Characters", encryptedPIN.accountNumber);
        assertEquals("encryptedPIN.pinBlockFormat", (byte) 0, encryptedPIN.pinBlockFormat);
        assertSame("encryptedPIN.pinBlock", pinBlock, encryptedPIN.pinBlock);
    }

    @Test
    public void testConstructor6() throws Throwable {
        byte[] pinBlock = new byte[1];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "11Character");
        assertEquals("encryptedPIN.accountNumber", "0011Characte", encryptedPIN.accountNumber);
        assertEquals("encryptedPIN.pinBlockFormat", (byte) 0, encryptedPIN.pinBlockFormat);
        assertSame("encryptedPIN.pinBlock", pinBlock, encryptedPIN.pinBlock);
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException1() throws Throwable {
        byte[] pinBlock = new byte[3];
        try {
            new EncryptedPIN(pinBlock, (byte) 0, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
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
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new EncryptedPIN().dump(null, "testEncryptedPINIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
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
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testExtractAccountNumberPart() throws Throwable {
        String result = EncryptedPIN.extractAccountNumberPart("11Character");
        assertEquals("result", "0011Characte", result);
    }

    @Test
    public void testExtractAccountNumberPart1() throws Throwable {
        String result = EncryptedPIN.extractAccountNumberPart("13CharactersX");
        assertEquals("result", "13Characters", result);
    }

    @Test
    public void testExtractAccountNumberPart2() throws Throwable {
        String result = EncryptedPIN.extractAccountNumberPart("12Characters");
        assertEquals("result", "012Character", result);
    }

    @Test
    public void testExtractAccountNumberPartThrowsNullPointerException() throws Throwable {
        try {
            EncryptedPIN.extractAccountNumberPart(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetAccountNumber() throws Throwable {
        byte[] pinBlock = new byte[0];
        String result = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber").getAccountNumber();
        assertEquals("result", "AccountNumbe", result);
    }

    @Test
    public void testGetAccountNumber1() throws Throwable {
        String result = new EncryptedPIN().getAccountNumber();
        assertNull("result", result);
    }

    @Test
    public void testGetPINBlock() throws Throwable {
        byte[] pinBlock = new byte[2];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber");
        byte[] pinBlock2 = new byte[3];
        encryptedPIN.setPINBlock(pinBlock2);
        byte[] result = encryptedPIN.getPINBlock();
        assertSame("result", pinBlock2, result);
        assertEquals("pinBlock2[0]", (byte) 0, pinBlock2[0]);
    }

    @Test
    public void testGetPINBlock1() throws Throwable {
        byte[] pinBlock = new byte[3];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber");
        byte[] pinBlock2 = new byte[0];
        encryptedPIN.setPINBlock(pinBlock2);
        byte[] result = encryptedPIN.getPINBlock();
        assertSame("result", pinBlock2, result);
    }

    @Test
    public void testGetPINBlockFormat() throws Throwable {
        byte[] pinBlock = new byte[2];
        byte result = new EncryptedPIN(pinBlock, (byte) 100, "testEncryptedPINAccountNumber").getPINBlockFormat();
        assertEquals("result", (byte) 100, result);
    }

    @Test
    public void testGetPINBlockFormat1() throws Throwable {
        byte[] pinBlock = new byte[2];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber");
        encryptedPIN.setPINBlockFormat((byte) 0);
        byte result = encryptedPIN.getPINBlockFormat();
        assertEquals("result", (byte) 0, result);
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
        assertEquals("encryptedPIN.accountNumber", "12Characters", encryptedPIN.accountNumber);
    }

    @Test
    public void test11Chars() throws Throwable {
        byte[] pinBlock = new byte[3];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "11Character");
        assertEquals("encryptedPIN.accountNumber", "0011Characte", encryptedPIN.accountNumber);
    }

    @Test
    public void testSetAccountNumberThrowsNullPointerException() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, "testEncryptedPINAccountNumber");
        try {
            encryptedPIN.setAccountNumber(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("encryptedPIN.accountNumber", "AccountNumbe", encryptedPIN.accountNumber);
        }
    }

    @Test
    public void testSetPINBlock() throws Throwable {
        byte[] pinBlock = new byte[3];
        EncryptedPIN encryptedPIN = new EncryptedPIN(pinBlock, (byte) 0, "testEncryptedPINAccountNumber");
        byte[] pinBlock2 = new byte[0];
        encryptedPIN.setPINBlock(pinBlock2);
        assertSame("encryptedPIN.pinBlock", pinBlock2, encryptedPIN.pinBlock);
    }

    @Test
    public void testSetPINBlockFormat() throws Throwable {
        EncryptedPIN encryptedPIN = new EncryptedPIN("testEncryptedPINPinBlockHexString1", (byte) 0, "testEncryptedPINAccountNumber");
        encryptedPIN.setPINBlockFormat((byte) 100);
        assertEquals("encryptedPIN.pinBlockFormat", (byte) 100, encryptedPIN.pinBlockFormat);
    }
}
