package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.jpos.util.Logger;
import org.junit.Test;

public class PostPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        PostPackager postPackager = new PostPackager();
        assertNotNull("postPackager.p127", postPackager.p127);
        assertNull("postPackager.getLogger()", postPackager.getLogger());
        assertEquals("postPackager.fld.length", 129, postPackager.fld.length);
        assertNull("postPackager.getRealm()", postPackager.getRealm());
    }

    @Test
    public void testPostPrivatePackagerConstructor() throws Throwable {
        PostPackager.PostPrivatePackager postPrivatePackager = new PostPackager().new PostPrivatePackager();
        assertNull("postPrivatePackager.getLogger()", postPrivatePackager.getLogger());
        assertEquals("postPrivatePackager.fld127.length", 26, postPrivatePackager.fld127.length);
        assertNull("postPrivatePackager.getRealm()", postPrivatePackager.getRealm());
    }

    @Test
    public void testSetLogger() throws Throwable {
        PostPackager postPackager = new PostPackager();
        Logger logger = Logger.getLogger("testPostPackagerName");
        postPackager.setLogger(logger, "testPostPackagerRealm");
        assertNotNull("postPackager.p127", postPackager.p127);
        assertSame("postPackager.getLogger()", logger, postPackager.getLogger());
        assertEquals("postPackager.getRealm()", "testPostPackagerRealm", postPackager.getRealm());
    }
}
