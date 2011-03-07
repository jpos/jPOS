package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.jpos.util.Logger;
import org.junit.Test;

public class EuroPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        EuroPackager euroPackager = new EuroPackager();
        assertNull("euroPackager.getLogger()", euroPackager.getLogger());
        assertNotNull("euroPackager.f48Packager", euroPackager.f48Packager);
        assertEquals("euroPackager.fld.length", 129, euroPackager.fld.length);
        assertNull("euroPackager.getRealm()", euroPackager.getRealm());
    }

    @Test
    public void testEuro48PackagerConstructor() throws Throwable {
        EuroPackager.Euro48Packager euro48Packager = new EuroPackager().new Euro48Packager();
        assertNull("euro48Packager.getLogger()", euro48Packager.getLogger());
        assertNull("euro48Packager.getRealm()", euro48Packager.getRealm());
    }

    @Test
    public void testSetLogger() throws Throwable {
        EuroPackager euroPackager = new EuroPackager();
        Logger logger = Logger.getLogger("testEuroPackagerName");
        euroPackager.setLogger(logger, "testEuroPackagerRealm");
        assertSame("euroPackager.getLogger()", logger, euroPackager.getLogger());
        assertNotNull("euroPackager.f48Packager", euroPackager.f48Packager);
        assertEquals("euroPackager.getRealm()", "testEuroPackagerRealm", euroPackager.getRealm());
    }
}
