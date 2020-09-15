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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import org.jpos.security.CryptographicServiceMessage.ParsingException;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

public class CryptographicServiceMessageTest {

    Exception thrown;

    CryptographicServiceMessage instance;

    @BeforeEach
    void setUp() {
        instance = new CryptographicServiceMessage();
    }

    @Test
    void testAddField() {
        instance.addField("testTag", "testContent");
        assertEquals(1, instance.fields.size());
        assertEquals("testContent", instance.fields.get("TESTTAG"));
        assertEquals(1, instance.orderedTags.size());
        assertEquals("TESTTAG", instance.orderedTags.get(0));
    }

    @Test
    void testAddFieldThrowsNullPointerException() {
        thrown = assertThrows(NullPointerException.class,
            () -> instance.addField("testTag", null)
        );
        assertEquals("The content is required", thrown.getMessage());
        assertEquals(0, instance.fields.size());
        assertEquals(0, instance.orderedTags.size());
    }

    @Test
    void testAddFieldThrowsNullPointerException1() {
        thrown = assertThrows(NullPointerException.class,
            () -> instance.addField(null, "testContent")
        );
        assertEquals("The tag is required", thrown.getMessage());
        assertEquals(0, instance.fields.size());
        assertEquals(0, instance.orderedTags.size());
    }

    @Test
    void testConstructor() {
        assertEquals(0, instance.fields.size());
        assertEquals(0, instance.orderedTags.size());
    }

    @Test
    void testConstructor1() {
        instance.setMCL("testMcl");
        assertEquals(0, instance.fields.size());
        assertEquals(0, instance.orderedTags.size());
        assertEquals("testMcl", instance.mcl);
    }

    @Test
    void testDump() throws Throwable {
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        instance.setMCL("testMcl");
        instance.dump(p, "testIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    void testDump1() throws Throwable {
        instance.setMCL("testMcl");
        instance.addField("testTag", "testContent");
        PrintStream p = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        instance.dump(p, "testIndent");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    void testDumpThrowsNullPointerException() {
        instance.setMCL("testMcl");
        thrown = assertThrows(NullPointerException.class,
            () -> instance.dump(null, "testIndent")
        );
        if (isJavaVersionAtMost(JAVA_14)) {
            assertNull(thrown.getMessage());
        } else {
            assertEquals("Cannot invoke \"java.io.PrintStream.print(String)\" because \"p\" is null", thrown.getMessage());
        }
    }

    @Test
    void testGetFieldContent() {
        instance.setMCL("testMcl");
        String result = instance.getFieldContent("testTag");
        assertNull(result, "result");
    }

    @Test
    void testGetFieldContentThrowsNullPointerException() {
        instance.setMCL("testMcl");
        thrown = assertThrows(NullPointerException.class,
            () -> instance.getFieldContent(null)
        );
        if (isJavaVersionAtMost(JAVA_14)) {
            assertNull(thrown.getMessage());
        } else {
            assertEquals("Cannot invoke \"String.toUpperCase()\" because \"tag\" is null", thrown.getMessage());
        }
    }

    @Test
    void testGetMCL() {
        instance.setMCL("testCryptographicServiceMessageMcl");
        String result = instance.getMCL();
        assertEquals("testCryptographicServiceMessageMcl", result, "result");
    }

    @Test
    void testGetMCL1() {
        String result = instance.getMCL();
        assertNull(result, "result");
    }

    @Test
    void testParse() throws Throwable {
        CryptographicServiceMessage result = CryptographicServiceMessage.parse("CSM(MCL/");
        assertEquals("", result.getMCL());
    }

    @Test
    void testParseThrowsNoSuchElementException() {
        thrown = assertThrows(NoSuchElementException.class,
            () -> CryptographicServiceMessage.parse("")
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testParseThrowsNoSuchElementException1() {
        thrown = assertThrows(NoSuchElementException.class,
            () -> CryptographicServiceMessage.parse("CSM")
        );
        assertNull(thrown.getMessage());
    }

    @Test
    void testParseThrowsNullPointerException() {
        thrown = assertThrows(NullPointerException.class,
            () -> CryptographicServiceMessage.parse(null)
        );
        if (isJavaVersionAtMost(JAVA_14)) {
            assertNull(thrown.getMessage());
        } else {
            assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", thrown.getMessage());
        }
    }

    @Test
    void testParseThrowsParsingException() {
        thrown = assertThrows(ParsingException.class,
            () -> CryptographicServiceMessage.parse("CSM(MCL")
        );
        assertEquals("Invalid field, doesn't have a tag: MCL", thrown.getMessage());
    }

    @Test
    void testParseThrowsParsingException1() {
        thrown = assertThrows(ParsingException.class,
            () -> CryptographicServiceMessage.parse("testCsmString")
        );
        assertEquals("Invalid CSM, doesn't start with the \"CSM(\" tag: testCsmString"
                , thrown.getMessage()
        );
    }

    @Test
    void testParsingExceptionConstructor() {
        ParsingException pex = new ParsingException("testExceptionDetail");
        assertEquals("testExceptionDetail", pex.getMessage());
    }

    @Test
    void testParsingExceptionConstructor1() {
        new ParsingException();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    void testSetMCL() {
        instance.setMCL("testMcl");
        assertEquals("testMcl", instance.mcl);
    }

    @Test
    void testToString() {
        instance.setMCL("testMcl");
        String result = instance.toString();
        assertEquals("CSM(MCL/testMcl )", result);
    }

    @Test
    void testToString1() {
        instance = new CryptographicServiceMessage("testMcl");
        instance.addField("testTag", "testContent");
        String result = instance.toString();
        assertEquals("CSM(MCL/testMcl TESTTAG/testContent )", result);
    }

}
