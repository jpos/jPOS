package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ISO87APackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        ISO87APackager iSO87APackager = new ISO87APackager();
        assertNull("iSO87APackager.getLogger()", iSO87APackager.getLogger());
        assertNull("iSO87APackager.getRealm()", iSO87APackager.getRealm());
        assertEquals("iSO87APackager.fld.length", 129, iSO87APackager.fld.length);
    }
}
