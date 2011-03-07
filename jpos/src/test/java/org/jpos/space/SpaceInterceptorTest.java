package org.jpos.space;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SpaceInterceptorTest {

    @Test
    public void testConstructor() throws Throwable {
        Space sp = mock(Space.class);
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(sp);
        assertSame("spaceInterceptor.sp", sp, spaceInterceptor.sp);
    }

    @Test
    public void testInThrowsNullPointerException() throws Throwable {
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(null);
        try {
            spaceInterceptor.in("");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("spaceInterceptor.sp", spaceInterceptor.sp);
        }
    }

    @Test
    public void testOut() throws Throwable {
        TSpace sp = mock(TSpace.class);
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(sp);
        sp.out("testString", "1", 0L);
        spaceInterceptor.out("testString", "1", 0L);
        assertSame("spaceInterceptor.sp", sp, spaceInterceptor.sp);
        verify(sp, times(2)).out("testString", "1", 0L);
    }

    @Test
    public void testOutThrowsNullPointerException() throws Throwable {
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(null);
        try {
            spaceInterceptor.out(Integer.valueOf(-1), "1", 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("spaceInterceptor.sp", spaceInterceptor.sp);
        }
    }

    @Test
    public void testRdp() throws Throwable {
        Space sp = SpaceFactory.getSpace();
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(sp);
        Object result = spaceInterceptor.rdp(sp);
        assertNull("result", result);
        assertSame("spaceInterceptor.sp", sp, spaceInterceptor.sp);
    }

    @Test
    public void testRdpThrowsNullPointerException() throws Throwable {
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(null);
        try {
            spaceInterceptor.rdp("testString");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("spaceInterceptor.sp", spaceInterceptor.sp);
        }
    }

    @Test
    public void testRdThrowsNullPointerException() throws Throwable {
        SpaceInterceptor spaceInterceptor = new SpaceInterceptor(null);
        try {
            spaceInterceptor.rd("", -1L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("spaceInterceptor.sp", spaceInterceptor.sp);
        }
    }
}
