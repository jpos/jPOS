package org.jpos.space;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class SpaceTapTest {

    @Test
    public void testClose() throws Throwable {
        SpaceTap spaceTap = new SpaceTap(new TSpace(), "\u0001 ", Integer.valueOf(1), 100L);
        spaceTap.close();
        assertNull("spaceTap.ssp", spaceTap.ssp);
    }

    @Test
    public void testClose1() throws Throwable {
        LocalSpace ssp = new TSpace();
        SpaceTap spaceTap = new SpaceTap(ssp, ssp, new Object(), "", 100L);
        spaceTap.close();
        spaceTap.close();
        assertNull("spaceTap.ssp", spaceTap.ssp);
    }

    @Test
    public void testConstructor() throws Throwable {
        LocalSpace ssp = new TSpace();
        Object key = new Object();
        SpaceTap spaceTap = new SpaceTap(ssp, ssp, key, "", 100L);
        assertFalse("(TSpace) ssp.sl.isEmpty()", ((TSpace) ssp).sl.isEmpty());
        assertSame("spaceTap.dsp", ssp, spaceTap.dsp);
        assertSame("spaceTap.key", key, spaceTap.key);
        assertEquals("spaceTap.tapKey", "", spaceTap.tapKey);
        assertEquals("spaceTap.tapTimeout", 100L, spaceTap.tapTimeout);
        assertSame("spaceTap.ssp", ssp, spaceTap.ssp);
    }

    @Test
    public void testConstructorThrowsIllegalArgumentException() throws Throwable {
        LocalSpace ssp = new TSpace();
        Long key = Long.valueOf(100L);
        try {
            new SpaceTap(ssp, ssp, key, key, 100L);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "Possible deadlock - key equals tap-key within same space", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsIllegalArgumentException1() throws Throwable {
        LocalSpace sp = new TSpace();
        try {
            new SpaceTap(sp, "testString", "testString", 100L);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("ex.getMessage()", "Possible deadlock - key equals tap-key within same space", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException2() throws Throwable {
        LocalSpace ssp = new TSpace();
        try {
            new SpaceTap(ssp, ssp, null, "", 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructorThrowsNullPointerException3() throws Throwable {
        try {
            new SpaceTap(null, "", "testString", 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testPlaceholder() {
        assertTrue(true);
    }

}
