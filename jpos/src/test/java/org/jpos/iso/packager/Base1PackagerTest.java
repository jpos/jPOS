package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class Base1PackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        Base1Packager base1Packager = new Base1Packager();
        assertNull("base1Packager.getLogger()", base1Packager.getLogger());
        assertEquals("base1Packager.base1Fld.length", 129, base1Packager.base1Fld.length);
        assertNull("base1Packager.getRealm()", base1Packager.getRealm());
    }

    @Test
    public void testF126PackagerConstructor() throws Throwable {
        Base1Packager.F126Packager f126Packager = new Base1Packager().new F126Packager();
        assertNull("f126Packager.getLogger()", f126Packager.getLogger());
        assertNull("f126Packager.getRealm()", f126Packager.getRealm());
        assertEquals("f126Packager.fld126.length", 11, f126Packager.fld126.length);
    }

    @Test
    public void testF127PackagerConstructor() throws Throwable {
        Base1Packager.F127Packager f127Packager = new Base1Packager().new F127Packager();
        assertNull("f127Packager.getLogger()", f127Packager.getLogger());
        assertNull("f127Packager.getRealm()", f127Packager.getRealm());
        assertEquals("f127Packager.fld127.length", 6, f127Packager.fld127.length);
    }
}
