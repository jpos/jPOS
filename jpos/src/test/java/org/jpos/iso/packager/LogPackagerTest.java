package org.jpos.iso.packager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.jpos.iso.ISOException;
import org.junit.Test;

public class LogPackagerTest {

    static void assertMatch(String message, String regexp, String value){
        Pattern p = Pattern.compile(regexp, Pattern.DOTALL);
        Matcher m = p.matcher(value);
        assert m.matches() :  message;
    }

    @Test
    public void testConstructorThrowsISOException() throws Throwable {
        try {
            new LogPackager();
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertMatch("ex.getMessage()", ".*java.lang.ClassNotFoundException: org.apache.crimson.parser.XMLReaderImpl",
                    ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }
}
