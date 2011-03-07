package org.jpos.space;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SpaceErrorTest {

    @Test
    public void testConstructor() throws Throwable {
        Throwable cause = new UnknownError();
        SpaceError spaceError = new SpaceError("testSpaceErrorMessage", cause);
        assertEquals("spaceError.getMessage()", "testSpaceErrorMessage", spaceError.getMessage());
        assertSame("spaceError.getCause()", cause, spaceError.getCause());
    }

    @Test
    public void testConstructor1() throws Throwable {
        Throwable cause = new SpaceError("testSpaceErrorMessage");
        SpaceError spaceError = new SpaceError(cause);
        assertEquals("spaceError.getMessage()", "org.jpos.space.SpaceError: testSpaceErrorMessage", spaceError.getMessage());
        assertSame("spaceError.getCause()", cause, spaceError.getCause());
    }

    @Test
    public void testConstructor2() throws Throwable {
        SpaceError spaceError = new SpaceError("testSpaceErrorMessage");
        assertEquals("spaceError.getMessage()", "testSpaceErrorMessage", spaceError.getMessage());
    }

    @Test
    public void testConstructor3() throws Throwable {
        new SpaceError();
        assertTrue("Test completed without Exception", true);
    }
}
