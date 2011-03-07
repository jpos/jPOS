package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ISO87APackagerBBitmapTest {

    @Test
    public void testConstructor() throws Throwable {
        ISO87APackagerBBitmap iSO87APackagerBBitmap = new ISO87APackagerBBitmap();
        assertEquals("iSO87APackagerBBitmap.fld.length", 129, iSO87APackagerBBitmap.fld.length);
        assertNull("iSO87APackagerBBitmap.getLogger()", iSO87APackagerBBitmap.getLogger());
        assertNull("iSO87APackagerBBitmap.getRealm()", iSO87APackagerBBitmap.getRealm());
    }
}
