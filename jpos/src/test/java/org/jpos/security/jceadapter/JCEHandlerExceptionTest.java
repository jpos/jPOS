package org.jpos.security.jceadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

public class JCEHandlerExceptionTest {

    @Test
    public void testConstructor() throws Throwable {
        Exception e = new JCEHandlerException("testJCEHandlerExceptions");
        JCEHandlerException jCEHandlerException = new JCEHandlerException(e);
        assertEquals("jCEHandlerException.getMessage()",
                "org.jpos.security.jceadapter.JCEHandlerException: testJCEHandlerExceptions", jCEHandlerException.getMessage());
        assertSame("jCEHandlerException.getNested()", e, jCEHandlerException.getNested());
    }

    @Test
    public void testConstructor1() throws Throwable {
        JCEHandlerException jCEHandlerException = new JCEHandlerException("testJCEHandlerExceptions");
        assertEquals("jCEHandlerException.getMessage()", "testJCEHandlerExceptions", jCEHandlerException.getMessage());
        assertNull("jCEHandlerException.getNested()", jCEHandlerException.getNested());
    }

    @Test
    public void testConstructor2() throws Throwable {
        Exception e = new Exception("testJCEHandlerExceptionParam1");
        JCEHandlerException jCEHandlerException = new JCEHandlerException("testJCEHandlerExceptions", e);
        assertEquals("jCEHandlerException.getMessage()", "testJCEHandlerExceptions", jCEHandlerException.getMessage());
        assertSame("jCEHandlerException.getNested()", e, jCEHandlerException.getNested());
    }

    @Test
    public void testConstructor3() throws Throwable {
        JCEHandlerException jCEHandlerException = new JCEHandlerException();
        assertNull("jCEHandlerException.getNested()", jCEHandlerException.getNested());
    }

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new JCEHandlerException((Exception) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
