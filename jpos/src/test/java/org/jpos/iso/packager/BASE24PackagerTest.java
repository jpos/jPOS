package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class BASE24PackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        BASE24Packager bASE24Packager = new BASE24Packager();
        assertNull("bASE24Packager.getLogger()", bASE24Packager.getLogger());
        assertEquals("bASE24Packager.fld.length", 129, bASE24Packager.fld.length);
        assertNull("bASE24Packager.getRealm()", bASE24Packager.getRealm());
    }
}
