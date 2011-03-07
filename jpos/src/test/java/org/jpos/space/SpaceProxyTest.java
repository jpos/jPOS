package org.jpos.space;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class SpaceProxyTest {

    @Test
    public void testConstructorThrowsNullPointerException() throws Throwable {
        try {
            new SpaceProxy("Inval*d space: ");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
