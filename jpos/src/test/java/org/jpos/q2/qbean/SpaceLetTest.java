package org.jpos.q2.qbean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.jdom.Element;
import org.jpos.space.SpaceError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

;

@RunWith(MockitoJUnitRunner.class)
public class SpaceLetTest {

    @Test
    public void testConstructor() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        assertNotNull("java default constructor", spaceLet);
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testInp() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        Element persist = mock(Element.class);
        spaceLet.setPersist(persist);
        spaceLet.initService();
        Object result = spaceLet.inp(Integer.valueOf(0));
        assertNull(result);
    }

    @Test
    public void testInpThrowsSpaceError() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.inp("testString");
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test(expected = SpaceError.class)
    public void testInThrowsSpaceError() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        spaceLet.in("");
    }

    @Test
    public void testInThrowsSpaceError1() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.in("", 100L);
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testOutThrowsSpaceError1() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.out("testString", "testString");
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testOutThrowsSpaceError3() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.out("testString", "", 100L);
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testRdpThrowsSpaceError() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.rdp("");
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testRdThrowsSpaceError() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.rd(Integer.valueOf(0));
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testRdThrowsSpaceError1() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.rd(";1", 100L);
            fail("Expected SpaceError to be thrown");
        } catch (SpaceError ex) {
            // expected
        }
    }

    @Test
    public void testStartServiceThrowsNullPointerException() throws Throwable {
        SpaceLet spaceLet = new SpaceLet();
        try {
            spaceLet.startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testStopServiceThrowsNullPointerException() throws Throwable {
        try {
            new SpaceLet().stopService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            // expected
        }
    }
}
