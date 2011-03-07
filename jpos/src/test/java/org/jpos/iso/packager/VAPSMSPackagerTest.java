package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class VAPSMSPackagerTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testConstructorThrowsIllegalArgumentException() throws Throwable {
        try {
            new VAPSMSPackager();
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "Length 255 too long for org.jpos.iso.IFB_LLCHAR", ex.getMessage());
        }
    }

}
