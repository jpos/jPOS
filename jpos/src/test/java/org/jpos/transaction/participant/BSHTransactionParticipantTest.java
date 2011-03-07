package org.jpos.transaction.participant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.CharConversionException;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.NotActiveException;
import java.io.UnsupportedEncodingException;

import org.jdom.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.util.LogEvent;
import org.junit.Test;

import bsh.EvalError;
import bsh.ParseException;

public class BSHTransactionParticipantTest {

    @Test
    public void testAbort() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.abort(100L, new EOFException());
        assertNull("bSHTransactionParticipant.abortMethod", bSHTransactionParticipant.abortMethod);
        assertFalse("bSHTransactionParticipant.trace", bSHTransactionParticipant.trace);
    }

    @Test
    public void testCommit() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.commit(100L, Boolean.TRUE);
        assertNull("bSHTransactionParticipant.commitMethod", bSHTransactionParticipant.commitMethod);
        assertFalse("bSHTransactionParticipant.trace", bSHTransactionParticipant.trace);
    }

    @Test
    public void testConstructor() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        assertNull("bSHTransactionParticipant.getRealm()", bSHTransactionParticipant.getRealm());
        assertNull("bSHTransactionParticipant.getLogger()", bSHTransactionParticipant.getLogger());
    }

    @Test
    public void testDefaultAbort() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.defaultAbort(100L, new File("testBSHTransactionParticipantParam1"), new LogEvent());
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDefaultCommit() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.defaultCommit(100L, Long.valueOf(65L), new LogEvent("testBSHTransactionParticipantTag"));
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testDefaultPrepare() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        int result = bSHTransactionParticipant.defaultPrepare(100L, new CharConversionException(), new LogEvent());
        assertEquals("result", 129, result);
    }

    @Test
    public void testExecuteMethod() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        Object result = bSHTransactionParticipant.executeMethod(new BSHMethod("testBSHTransactionParticipantBshData", false), 100L,
                new StringBuffer(), new LogEvent("testBSHTransactionParticipantTag", Integer.valueOf(0)),
                "testBSHTransactionParticipantResultName");
        assertNull("result", result);
    }

    @Test
    public void testExecuteMethod2() throws Throwable {
        BSHTransactionParticipant bSHGroupSelector = new BSHGroupSelector();
        Boolean result = (Boolean) bSHGroupSelector.executeMethod(new BSHMethod("testBSHTransactionParticipantBshData", false),
                100L, new UnsupportedEncodingException(), new LogEvent("testBSHTransactionParticipantTag"), "bsh.evalOnly");
        assertTrue("result", result.booleanValue());
    }

    @Test
    public void testExecuteMethodThrowsEvalError() throws Throwable {
        BSHTransactionParticipant bSHGroupSelector = new BSHGroupSelector();
        try {
            bSHGroupSelector.executeMethod(new BSHMethod("testBSHTransactionParticipantBshData", false), 100L,
                    new CharConversionException(), new LogEvent(), "sh.evalOnly");
            fail("Expected EvalError to be thrown");
        } catch (EvalError ex) {
            assertEquals("ex.getMessage()", "Class or variable not found: sh.evalOnly", ex.getMessage());
            assertEquals("ex.getMessage()", "Class or variable not found: sh.evalOnly", ex.getMessage());
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
            assertEquals("ex.getClass()", FileNotFoundException.class, ex.getClass());
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
            assertNull("ex.getMessage()", ex.getMessage());
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
            assertEquals("ex.getMessage()", "Parse error at line 1, column 4.  Encountered: /", ex.getMessage());
            assertEquals("ex.getErrorSourceFile()", "inline evaluation of: ``\u000E\u0019\u0003/\u0008Tz<|p;''",
                    ex.getErrorSourceFile());
            assertEquals("ex.toString()", "Parse error at line 1, column 4.  Encountered: /", ex.toString());
            assertNull("ex.currentToken", ex.currentToken);
            assertNull("ex.expectedTokenSequences", ex.expectedTokenSequences);
            assertNull("ex.tokenImage", ex.tokenImage);
            assertEquals("ex.getMessage()", "Parse error at line 1, column 4.  Encountered: /", ex.getMessage());
        }
    }

    @Test
    public void testPrepare() throws Throwable {
        int result = new BSHTransactionParticipant().prepare(100L, new NotActiveException());
        assertEquals("result", 129, result);
    }

    @Test
    public void testPrepareForAbort() throws Throwable {
        int result = new BSHTransactionParticipant().prepareForAbort(100L, Boolean.FALSE);
        assertEquals("result", 128, result);
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.setConfiguration(new Element("testBSHTransactionParticipantName",
                "testBSHTransactionParticipantUri"));
        assertNull("bSHTransactionParticipant.prepareForAbortMethod", bSHTransactionParticipant.prepareForAbortMethod);
        assertNull("bSHTransactionParticipant.abortMethod", bSHTransactionParticipant.abortMethod);
        assertNull("bSHTransactionParticipant.commitMethod", bSHTransactionParticipant.commitMethod);
        assertNull("bSHTransactionParticipant.prepareMethod", bSHTransactionParticipant.prepareMethod);
        assertFalse("bSHTransactionParticipant.trace", bSHTransactionParticipant.trace);
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException1() throws Throwable {
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        try {
            bSHTransactionParticipant.setConfiguration(null);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
            assertNull("bSHTransactionParticipant.prepareForAbortMethod", bSHTransactionParticipant.prepareForAbortMethod);
            assertNull("bSHTransactionParticipant.abortMethod", bSHTransactionParticipant.abortMethod);
            assertNull("bSHTransactionParticipant.commitMethod", bSHTransactionParticipant.commitMethod);
            assertNull("bSHTransactionParticipant.prepareMethod", bSHTransactionParticipant.prepareMethod);
            assertFalse("bSHTransactionParticipant.trace", bSHTransactionParticipant.trace);
        }
    }
}
