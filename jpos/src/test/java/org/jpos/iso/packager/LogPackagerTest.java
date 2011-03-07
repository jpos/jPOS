package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jpos.iso.ISOException;
import org.junit.Test;

public class LogPackagerTest {

    @Test
    public void testConstructorThrowsISOException() throws Throwable {
        try {
            new LogPackager();
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "java.lang.ClassNotFoundException: org.apache.crimson.parser.XMLReaderImpl",
                    ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }
}
