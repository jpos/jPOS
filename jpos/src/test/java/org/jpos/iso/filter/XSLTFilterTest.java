package org.jpos.iso.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.channel.PADChannel;
import org.jpos.iso.packager.CTCSubFieldPackager;
import org.jpos.util.LogEvent;
import org.junit.Ignore;
import org.junit.Test;

public class XSLTFilterTest {

    @Test
    public void testConstructor() throws Throwable {
        XSLTFilter xSLTFilter = new XSLTFilter();
        assertTrue("xSLTFilter.reread", xSLTFilter.reread);
        assertNull("xSLTFilter.packager.getRealm()", xSLTFilter.packager.getRealm());
        assertNull("xSLTFilter.tfactory.getURIResolver()", xSLTFilter.tfactory.getURIResolver());
        assertNull("xSLTFilter.transformer", xSLTFilter.transformer);
    }

    @Ignore("test fails, exception is not raised at construction time")
    @Test
    public void testConstructorThrowsISOException() throws Throwable {
        try {
            new XSLTFilter("testXSLTFilterXsltfile", true);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            junitx.framework.StringAssert
                    .assertStartsWith("javax.xml.transform.TransformerConfigurationException:", ex.getMessage());
        }
    }

    @Test
    public void testFilterThrowsVetoException1() throws Throwable {
        XSLTFilter xSLTFilter = new XSLTFilter();
        try {
            xSLTFilter.filter(new PADChannel(new CTCSubFieldPackager()), null, new LogEvent("testXSLTFilterTag"));
            fail("Expected VetoException to be thrown");
        } catch (ISOFilter.VetoException ex) {
            assertEquals("ex.getMessage()", "java.lang.NullPointerException", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
            assertNull("xSLTFilter.tfactory.getURIResolver()", xSLTFilter.tfactory.getURIResolver());
            assertNull("xSLTFilter.transformer", xSLTFilter.transformer);
        }
    }

}
