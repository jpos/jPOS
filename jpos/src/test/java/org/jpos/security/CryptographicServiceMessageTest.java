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
import java.util.NoSuchElementException;

import org.junit.Test;

public class CryptographicServiceMessageTest {

    @Test
    public void testAddField() throws Throwable {
        CryptographicServiceMessage cryptographicServiceMessage = new CryptographicServiceMessage();
        cryptographicServiceMessage.addField("testCryptographicServiceMessageTag", "testCryptographicServiceMessageContent");
        assertEquals("cryptographicServiceMessage.fields.size()", 1, cryptographicServiceMessage.fields.size());
        assertEquals("cryptographicServiceMessage.fields.get(\"TESTCRYPTOGRAPHICSERVICEMESSAGETAG\")",
                "testCryptographicServiceMessageContent",
                cryptographicServiceMessage.fields.get("TESTCRYPTOGRAPHICSERVICEMESSAGETAG"));
        assertEquals("cryptographicServiceMessage.orderedTags.size()", 1, cryptographicServiceMessage.orderedTags.size());
        assertEquals("cryptographicServiceMessage.orderedTags.get(0)", "TESTCRYPTOGRAPHICSERVICEMESSAGETAG",
                cryptographicServiceMessage.orderedTags.get(0));
    }

    @Test
    public void testAddFieldThrowsNullPointerException() throws Throwable {
        CryptographicServiceMessage cryptographicServiceMessage = new CryptographicServiceMessage();
        try {
            cryptographicServiceMessage.addField("testCryptographicServiceMessageTag", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("cryptographicServiceMessage.fields.size()", 0, cryptographicServiceMessage.fields.size());
            assertEquals("cryptographicServiceMessage.orderedTags.size()", 0, cryptographicServiceMessage.orderedTags.size());
        }
    }

    @Test
    public void testAddFieldThrowsNullPointerException1() throws Throwable {
        CryptographicServiceMessage cryptographicServiceMessage = new CryptographicServiceMessage();
        try {
            cryptographicServiceMessage.addField(null, "testCryptographicServiceMessageContent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("cryptographicServiceMessage.fields.size()", 0, cryptographicServiceMessage.fields.size());
            assertEquals("cryptographicServiceMessage.orderedTags.size()", 0, cryptographicServiceMessage.orderedTags.size());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        CryptographicServiceMessage cryptographicServiceMessage = new CryptographicServiceMessage();
        assertEquals("cryptographicServiceMessage.fields.size()", 0, cryptographicServiceMessage.fields.size());
        assertEquals("cryptographicServiceMessage.orderedTags.size()", 0, cryptographicServiceMessage.orderedTags.size());
    }

    @Test
    public void testConstructor1() throws Throwable {
        CryptographicServiceMessage cryptographicServiceMessage = new CryptographicServiceMessage(
                "testCryptographicServiceMessageMcl");
        assertEquals("cryptographicServiceMessage.fields.size()", 0, cryptographicServiceMessage.fields.size());
        assertEquals("cryptographicServiceMessage.orderedTags.size()", 0, cryptographicServiceMessage.orderedTags.size());
        assertEquals("cryptographicServiceMessage.mcl", "testCryptographicServiceMessageMcl", cryptographicServiceMessage.mcl);
    }

    @Test
    public void testDump() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        new CryptographicServiceMessage("testCryptographicServiceMessageMcl").dump(p, "testCryptographicServiceMessageIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDump1() throws Throwable {
        CryptographicServiceMessage cryptographicServiceMessage = new CryptographicServiceMessage(
                "testCryptographicServiceMessageMcl");
        cryptographicServiceMessage.addField("testCryptographicServiceMessageTag", "testCryptographicServiceMessageContent");
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        cryptographicServiceMessage.dump(p, "testCryptographicServiceMessageIndent");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        try {
            new CryptographicServiceMessage("testCryptographicServiceMessageMcl")
                    .dump(null, "testCryptographicServiceMessageIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetFieldContent() throws Throwable {
        String result = new CryptographicServiceMessage("testCryptographicServiceMessageMcl")
                .getFieldContent("testCryptographicServiceMessageTag");
        assertNull("result", result);
    }

    @Test
    public void testGetFieldContentThrowsNullPointerException() throws Throwable {
        try {
            new CryptographicServiceMessage("testCryptographicServiceMessageMcl").getFieldContent(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetMCL() throws Throwable {
        CryptographicServiceMessage cryptographicServiceMessage = new CryptographicServiceMessage();
        cryptographicServiceMessage.setMCL("testCryptographicServiceMessageMcl");
        String result = cryptographicServiceMessage.getMCL();
        assertEquals("result", "testCryptographicServiceMessageMcl", result);
    }

    @Test
    public void testGetMCL1() throws Throwable {
        String result = new CryptographicServiceMessage().getMCL();
        assertNull("result", result);
    }

    @Test
    public void testParse() throws Throwable {
        CryptographicServiceMessage result = CryptographicServiceMessage.parse("CSM(MCL/");
        assertEquals("result.getMCL()", "", result.getMCL());
    }

    @Test
    public void testParseThrowsNoSuchElementException() throws Throwable {
        try {
            CryptographicServiceMessage.parse("");
            fail("Expected NoSuchElementException to be thrown");
        } catch (NoSuchElementException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseThrowsNoSuchElementException1() throws Throwable {
        try {
            CryptographicServiceMessage.parse("CSM");
            fail("Expected NoSuchElementException to be thrown");
        } catch (NoSuchElementException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseThrowsNullPointerException() throws Throwable {
        try {
            CryptographicServiceMessage.parse(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testParseThrowsParsingException() throws Throwable {
        try {
            CryptographicServiceMessage.parse("CSM(MCL");
            fail("Expected ParsingException to be thrown");
        } catch (CryptographicServiceMessage.ParsingException ex) {
            assertEquals("ex.getMessage()", "Invalid field, doesn't have a tag: MCL", ex.getMessage());
        }
    }

    @Test
    public void testParseThrowsParsingException1() throws Throwable {
        try {
            CryptographicServiceMessage.parse("testCryptographicServiceMessageCsmString");
            fail("Expected ParsingException to be thrown");
        } catch (CryptographicServiceMessage.ParsingException ex) {
            assertEquals("ex.getMessage()",
                    "Invalid CSM, doesn't start with the \"CSM(\" tag: testCryptographicServiceMessageCsmString", ex.getMessage());
        }
    }

    @Test
    public void testParsingExceptionConstructor() throws Throwable {
        CryptographicServiceMessage.ParsingException parsingException = new CryptographicServiceMessage.ParsingException(
                "testParsingExceptionDetail");
        assertEquals("parsingException.getMessage()", "testParsingExceptionDetail", parsingException.getMessage());
    }

    @Test
    public void testParsingExceptionConstructor1() throws Throwable {
        new CryptographicServiceMessage.ParsingException();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testSetMCL() throws Throwable {
        CryptographicServiceMessage cryptographicServiceMessage = new CryptographicServiceMessage();
        cryptographicServiceMessage.setMCL("testCryptographicServiceMessageMcl");
        assertEquals("cryptographicServiceMessage.mcl", "testCryptographicServiceMessageMcl", cryptographicServiceMessage.mcl);
    }

    @Test
    public void testToString() throws Throwable {
        String result = new CryptographicServiceMessage("testCryptographicServiceMessageMcl").toString();
        assertEquals("result", "CSM(MCL/testCryptographicServiceMessageMcl )", result);
    }

    @Test
    public void testToString1() throws Throwable {
        CryptographicServiceMessage cryptographicServiceMessage = new CryptographicServiceMessage(
                "testCryptographicServiceMessageMcl");
        cryptographicServiceMessage.addField("testCryptographicServiceMessageTag", "testCryptographicServiceMessageContent");
        String result = cryptographicServiceMessage.toString();
        assertEquals(
                "result",
                "CSM(MCL/testCryptographicServiceMessageMcl TESTCRYPTOGRAPHICSERVICEMESSAGETAG/testCryptographicServiceMessageContent )",
                result);
    }
}
