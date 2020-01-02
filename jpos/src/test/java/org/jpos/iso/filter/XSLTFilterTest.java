/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.channel.PADChannel;
import org.jpos.iso.packager.CTCSubFieldPackager;
import org.jpos.util.LogEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class XSLTFilterTest {

    @Test
    public void testConstructor() throws Throwable {
        XSLTFilter xSLTFilter = new XSLTFilter();
        assertTrue(xSLTFilter.reread, "xSLTFilter.reread");
        assertNull(xSLTFilter.packager.getRealm(), "xSLTFilter.packager.getRealm()");
        assertNull(xSLTFilter.tfactory.getURIResolver(), "xSLTFilter.tfactory.getURIResolver()");
        assertNull(xSLTFilter.transformer, "xSLTFilter.transformer");
    }

    @Disabled("test fails, exception is not raised at construction time")
    @Test
    public void testConstructorThrowsISOException() throws Throwable {
        try {
            new XSLTFilter("testXSLTFilterXsltfile", true);
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertTrue(ex.getMessage().startsWith("javax.xml.transform.TransformerConfigurationException:"));
        }
    }

    @Test
    public void testFilterThrowsVetoException1() throws Throwable {
        XSLTFilter xSLTFilter = new XSLTFilter();
        try {
            xSLTFilter.filter(new PADChannel(new CTCSubFieldPackager()), null, new LogEvent("testXSLTFilterTag"));
            fail("Expected VetoException to be thrown");
        } catch (ISOFilter.VetoException ex) {
            assertEquals("java.lang.NullPointerException", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested().getMessage(), "ex.getNested().getMessage()");
            assertNull(xSLTFilter.tfactory.getURIResolver(), "xSLTFilter.tfactory.getURIResolver()");
            assertNull(xSLTFilter.transformer, "xSLTFilter.transformer");
        }
    }

}
