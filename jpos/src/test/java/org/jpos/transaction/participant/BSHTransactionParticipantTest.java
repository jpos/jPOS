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

package org.jpos.transaction.participant;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.CharConversionException;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.NotActiveException;
import java.io.UnsupportedEncodingException;

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.util.LogEvent;
import org.junit.jupiter.api.Test;

import bsh.EvalError;
import bsh.ParseException;

public class BSHTransactionParticipantTest {
    @Test
    public void testAbort() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.abort(100L, new EOFException());
        assertNull(bSHTransactionParticipant.abortMethod, "bSHTransactionParticipant.abortMethod");
        assertFalse(bSHTransactionParticipant.trace, "bSHTransactionParticipant.trace");
    }

    @Test
    public void testCommit() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.commit(100L, Boolean.TRUE);
        assertNull(bSHTransactionParticipant.commitMethod, "bSHTransactionParticipant.commitMethod");
        assertFalse(bSHTransactionParticipant.trace, "bSHTransactionParticipant.trace");
    }

    @Test
    public void testConstructor() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        assertNull(bSHTransactionParticipant.getRealm(), "bSHTransactionParticipant.getRealm()");
        assertNull(bSHTransactionParticipant.getLogger(), "bSHTransactionParticipant.getLogger()");
    }

    @Test
    public void testDefaultAbort() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.defaultAbort(100L, new File("testBSHTransactionParticipantParam1"), new LogEvent());
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDefaultCommit() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.defaultCommit(100L, Long.valueOf(65L), new LogEvent("testBSHTransactionParticipantTag"));
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testDefaultPrepare() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        int result = bSHTransactionParticipant.defaultPrepare(100L, new CharConversionException(), new LogEvent());
        assertEquals(129, result, "result");
    }

    @Test
    public void testExecuteMethod() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        Object result = bSHTransactionParticipant.executeMethod(new BSHMethod("testBSHTransactionParticipantBshData", false), 100L,
                new StringBuffer(), new LogEvent("testBSHTransactionParticipantTag", Integer.valueOf(0)),
                "testBSHTransactionParticipantResultName");
        assertNull(result, "result");
    }

    @Test
    public void testExecuteMethod2() throws Throwable {
        BSHTransactionParticipant bSHGroupSelector = new BSHGroupSelector();
        Boolean result = (Boolean) bSHGroupSelector.executeMethod(new BSHMethod("testBSHTransactionParticipantBshData", false),
                100L, new UnsupportedEncodingException(), new LogEvent("testBSHTransactionParticipantTag"), "bsh.evalOnly");
        assertTrue(result.booleanValue(), "result");
    }

    @Test
    public void testExecuteMethodThrowsEvalError() throws Throwable {
        BSHTransactionParticipant bSHGroupSelector = new BSHGroupSelector();
        try {
            bSHGroupSelector.executeMethod(new BSHMethod("testBSHTransactionParticipantBshData", false), 100L,
                    new CharConversionException(), new LogEvent(), "sh.evalOnly");
            fail("Expected EvalError to be thrown");
        } catch (EvalError ex) {
            assertEquals("Class or variable not found: sh.evalOnly", ex.getMessage(), "ex.getMessage()");
            assertEquals("Class or variable not found: sh.evalOnly", ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testExecuteMethodThrowsFileNotFoundException() throws Throwable {
        BSHTransactionParticipant source = new BSHTransactionParticipant();
        try {
            source.executeMethod(new BSHMethod("testBSHTransactionParticipantBshData", true), 100L, new CharConversionException(),
                    new LogEvent(source, "testBSHTransactionParticipantTag"), "testBSHTransactionParticipantResultName");
            fail("Expected FileNotFoundException to be thrown");
        } catch (FileNotFoundException ex) {
            assertEquals(FileNotFoundException.class, ex.getClass(), "ex.getClass()");
        }
    }

    @Test
    public void testExecuteMethodThrowsNullPointerException() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        try {
            bSHTransactionParticipant.executeMethod(null, 100L, new StringBuffer(), new LogEvent("testBSHTransactionParticipantTag",
                    Integer.valueOf(0)), "testBSHTransactionParticipantResultName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.transaction.participant.BSHMethod.execute(java.util.Map, String)\" because \"m\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testExecuteMethodThrowsParseException() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        try {
            bSHTransactionParticipant.executeMethod(new BSHMethod("\u000E\u0019\u0003/\u0008Tz<|p", false), 100L,
                    new CharConversionException(), new LogEvent("testBSHTransactionParticipantTag", new Object()),
                    "testBSHTransactionParticipantResultName");
            fail("Expected ParseException to be thrown");
        } catch (ParseException ex) {
            assertThat(ex.getMessage(), allOf(notNullValue(), containsString("line 1, column 4")));
        }
    }

    @Test
    public void testPrepare() throws Throwable {
        int result = new BSHTransactionParticipant().prepare(100L, new NotActiveException());
        assertEquals(129, result, "result");
    }

    @Test
    public void testPrepareForAbort() throws Throwable {
        int result = new BSHTransactionParticipant().prepareForAbort(100L, Boolean.FALSE);
        assertEquals(128, result, "result");
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.setConfiguration(new Element("testBSHTransactionParticipantName",
                "testBSHTransactionParticipantUri"));
        assertNull(bSHTransactionParticipant.prepareForAbortMethod, "bSHTransactionParticipant.prepareForAbortMethod");
        assertNull(bSHTransactionParticipant.abortMethod, "bSHTransactionParticipant.abortMethod");
        assertNull(bSHTransactionParticipant.commitMethod, "bSHTransactionParticipant.commitMethod");
        assertNull(bSHTransactionParticipant.prepareMethod, "bSHTransactionParticipant.prepareMethod");
        assertFalse(bSHTransactionParticipant.trace, "bSHTransactionParticipant.trace");
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException1() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        try {
            bSHTransactionParticipant.setConfiguration(null);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
                assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jdom2.Element.getChild(String)\" because \"e\" is null", ex.getMessage(), "ex.getMessage()");
                assertEquals("Cannot invoke \"org.jdom2.Element.getChild(String)\" because \"e\" is null", ex.getNested().getMessage(), "ex.getNested().getMessage()");
            }
            assertNull(bSHTransactionParticipant.prepareForAbortMethod, "bSHTransactionParticipant.prepareForAbortMethod");
            assertNull(bSHTransactionParticipant.abortMethod, "bSHTransactionParticipant.abortMethod");
            assertNull(bSHTransactionParticipant.commitMethod, "bSHTransactionParticipant.commitMethod");
            assertNull(bSHTransactionParticipant.prepareMethod, "bSHTransactionParticipant.prepareMethod");
            assertFalse(bSHTransactionParticipant.trace, "bSHTransactionParticipant.trace");
        }
    }
}
