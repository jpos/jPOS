package org.jpos.space;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class SpaceFactoryTest {

    @Test
    public void testConstructor() throws Throwable {
        new SpaceFactory();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGetSpace2() throws Throwable {
        TSpace result = (TSpace) SpaceFactory.getSpace("testString");
        assertTrue("result.isEmpty()", result.isEmpty());
    }

    // @Test public void testGetSpace4() throws Throwable {
    // TSpace result = (TSpace) SpaceFactory.getSpace("");
    // assertTrue("result.isEmpty()", result.isEmpty());
    // }
    //
    // @Test public void testGetSpace5() throws Throwable {
    // TSpace result = (TSpace) SpaceFactory.getSpace(null);
    // assertTrue("result.isEmpty()", result.isEmpty());
    // }
    //
    //
    // @Test public void testGetSpace7() throws Throwable {
    // TSpace result = (TSpace) SpaceFactory.getSpace();
    // assertTrue("result.isEmpty()", result.isEmpty());
    // }

    public void testGetSpaceThrowsNullPointerException1() throws Throwable {
        try {
            SpaceFactory.getSpace("testSpaceFactoryScheme", "testSpaceFactoryName", "testSpaceFactoryParam");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetSpaceThrowsNullPointerException2() throws Throwable {
        try {
            SpaceFactory.getSpace("testSpaceFactoryScheme", "testSpaceFactoryName", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetSpaceThrowsSpaceError() throws Throwable {
        try {
            SpaceFactory.getSpace("spacelet", "testSpaceFactoryName", "testSpaceFactoryParam");
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            assertEquals("ex.getMessage()", "spacelet:testSpaceFactoryName:testSpaceFactoryParam not found.", ex.getMessage());
        }
    }
}
