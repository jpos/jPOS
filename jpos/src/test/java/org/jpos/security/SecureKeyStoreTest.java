package org.jpos.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

public class SecureKeyStoreTest {

    @Test
    public void testSecureKeyStoreExceptionConstructor() throws Throwable {
        SecureKeyStore.SecureKeyStoreException secureKeyStoreException = new SecureKeyStore.SecureKeyStoreException();
        assertNull("secureKeyStoreException.getNested()", secureKeyStoreException.getNested());
    }

    @Test
    public void testSecureKeyStoreExceptionConstructor1() throws Throwable {
        Exception nested = new SecureKeyStore.SecureKeyStoreException();
        SecureKeyStore.SecureKeyStoreException secureKeyStoreException = new SecureKeyStore.SecureKeyStoreException(
                "testSecureKeyStoreExceptionDetail", nested);
        assertEquals("secureKeyStoreException.getMessage()", "testSecureKeyStoreExceptionDetail",
                secureKeyStoreException.getMessage());
        assertSame("secureKeyStoreException.getNested()", nested, secureKeyStoreException.getNested());
    }

    @Test
    public void testSecureKeyStoreExceptionConstructor2() throws Throwable {
        SecureKeyStore.SecureKeyStoreException secureKeyStoreException = new SecureKeyStore.SecureKeyStoreException(
                "testSecureKeyStoreExceptionDetail");
        assertEquals("secureKeyStoreException.getMessage()", "testSecureKeyStoreExceptionDetail",
                secureKeyStoreException.getMessage());
        assertNull("secureKeyStoreException.getNested()", secureKeyStoreException.getNested());
    }

    @Test
    public void testSecureKeyStoreExceptionConstructor3() throws Throwable {
        Exception nested = new SecureKeyStore.SecureKeyStoreException();
        SecureKeyStore.SecureKeyStoreException secureKeyStoreException = new SecureKeyStore.SecureKeyStoreException(nested);
        assertEquals("secureKeyStoreException.getMessage()", "org.jpos.security.SecureKeyStore$SecureKeyStoreException",
                secureKeyStoreException.getMessage());
        assertSame("secureKeyStoreException.getNested()", nested, secureKeyStoreException.getNested());
    }

    @Test
    public void testSecureKeyStoreExceptionConstructorThrowsNullPointerException() throws Throwable {
        try {
            new SecureKeyStore.SecureKeyStoreException((Exception) null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
