package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ISO87BPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        ISO87BPackager iSO87BPackager = new ISO87BPackager();
        assertNull("iSO87BPackager.getLogger()", iSO87BPackager.getLogger());
        assertEquals("iSO87BPackager.fld.length", 129, iSO87BPackager.fld.length);
        assertNull("iSO87BPackager.getRealm()", iSO87BPackager.getRealm());
    }
}
