package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ISO93APackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        ISO93APackager iSO93APackager = new ISO93APackager();
        assertNull("iSO93APackager.getLogger()", iSO93APackager.getLogger());
        assertEquals("iSO93APackager.fld.length", 129, iSO93APackager.fld.length);
        assertNull("iSO93APackager.getRealm()", iSO93APackager.getRealm());
    }
}
