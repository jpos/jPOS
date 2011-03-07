package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ISO93BPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        ISO93BPackager iSO93BPackager = new ISO93BPackager();
        assertNull("iSO93BPackager.getLogger()", iSO93BPackager.getLogger());
        assertNull("iSO93BPackager.getRealm()", iSO93BPackager.getRealm());
        assertEquals("iSO93BPackager.fld.length", 129, iSO93BPackager.fld.length);
    }
}
