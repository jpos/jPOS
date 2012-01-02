/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.iso.packager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.EmptyStackException;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.iso.IFA_AMOUNT;
import org.jpos.iso.IFA_BITMAP;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.Attributes2Impl;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

public class GenericPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        assertNull("genericPackager.getLogger()", genericPackager.getLogger());
        assertNull("genericPackager.getRealm()", genericPackager.getRealm());
    }

    @Test
    public void testConstructorThrowsISOException() throws Throwable {
        try {
            new GenericPackager("testGenericPackagerFilename");
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getNested().getClass()", FileNotFoundException.class, ex.getNested().getClass());
        }
    }

    @Test
    public void testConstructorThrowsISOException1() throws Throwable {
        try {
            new GenericPackager(new ByteArrayInputStream("".getBytes()));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getNested().getClass()", SAXParseException.class, ex.getNested().getClass());
            assertEquals("ex.getNested().getMessage()", "Premature end of file.", ex.getNested().getMessage());
        }
    }

    @Test
    public void testEmitBitMap() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        boolean result = genericPackager.emitBitMap();
        assertTrue("result", result);
    }

    @Test
    public void testGenericContentHandlerConstructor() throws Throwable {
        new GenericSubFieldPackager().new GenericContentHandler();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGenericContentHandlerEndDocument() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new X92GenericPackager().new GenericContentHandler();
        genericContentHandler.startDocument();
        genericContentHandler.endDocument();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGenericContentHandlerEndDocumentThrowsNullPointerException() throws Throwable {
        try {
            new GenericSubFieldPackager().new GenericContentHandler().endDocument();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericContentHandlerEndDocumentThrowsSAXException() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericPackager().new GenericContentHandler();
        genericContentHandler.startDocument();
        genericContentHandler.startElement("testGenericContentHandlerNamespaceURI", "isopackager", "testGenericContentHandlerQName",
                new AttributesImpl());
        try {
            genericContentHandler.endDocument();
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertEquals("ex.getMessage()", "Format error in XML Field Description File", ex.getMessage());
            assertNull("ex.getException()", ex.getException());
        }
    }

    @Test
    public void testGenericContentHandlerEndElement() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericPackager().new GenericContentHandler();
        genericContentHandler.startDocument();
        genericContentHandler.startElement("testGenericContentHandlerNamespaceURI", "isopackager", "testGenericContentHandlerQName",
                new AttributesImpl());
        genericContentHandler.endElement("testGenericContentHandlerNamespaceURI", "isopackager", "testGenericContentHandlerQName");
    }

    @Test
    public void testGenericContentHandlerEndElement1() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new X92GenericPackager().new GenericContentHandler();
        genericContentHandler.endElement("testGenericContentHandlerNamespaceURI", "testGenericContentHandlerLocalName",
                "testGenericContentHandlerQName");
    }

    @Test
    public void testGenericContentHandlerEndElementThrowsClassCastException() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericPackager().new GenericContentHandler();
        genericContentHandler.startDocument();
        Attributes atts = new AttributesImpl(new Attributes2Impl());
        genericContentHandler.startElement("testGenericContentHandlerNamespaceURI", "isopackager", "testGenericContentHandlerQName",
                atts);
        genericContentHandler.startElement("testGenericContentHandlerNamespaceURI1", "isopackager",
                "testGenericContentHandlerQName1", atts);
        try {
            genericContentHandler.endElement("testGenericContentHandlerNamespaceURI", "isofieldpackager",
                    "testGenericContentHandlerQName");
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals("ex.getClass()", ClassCastException.class, ex.getClass());
        }
    }

    @Test
    public void testGenericContentHandlerEndElementThrowsEmptyStackException() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericSubFieldPackager().new GenericContentHandler();
        genericContentHandler.startDocument();
        try {
            genericContentHandler.endElement("testGenericContentHandlerNamespaceURI", "isofieldpackager",
                    "testGenericContentHandlerQName");
            fail("Expected EmptyStackException to be thrown");
        } catch (EmptyStackException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericContentHandlerEndElementThrowsNullPointerException() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericPackager().new GenericContentHandler();
        try {
            genericContentHandler.endElement("testGenericContentHandlerNamespaceURI", "isopackager",
                    "testGenericContentHandlerQName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericContentHandlerEndElementThrowsNullPointerException1() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericPackager().new GenericContentHandler();
        try {
            genericContentHandler.endElement("testGenericContentHandlerNamespaceURI", null, "testGenericContentHandlerQName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericContentHandlerErrorThrowsNullPointerException() throws Throwable {
        try {
            new GenericSubFieldPackager().new GenericContentHandler().error(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericContentHandlerErrorThrowsSAXParseException() throws Throwable {
        SAXParseException ex2 = new SAXParseException("testGenericContentHandlerParam1", "testGenericContentHandlerParam2",
                "testGenericContentHandlerParam3", 100, 1000);
        try {
            new GenericSubFieldPackager().new GenericContentHandler().error(ex2);
            fail("Expected SAXParseException to be thrown");
        } catch (SAXParseException ex) {
            assertEquals("ex.getMessage()", "testGenericContentHandlerParam1", ex.getMessage());
            assertEquals("ex.getPublicId()", "testGenericContentHandlerParam2", ex.getPublicId());
            assertEquals("ex.getSystemId()", "testGenericContentHandlerParam3", ex.getSystemId());
            assertEquals("ex.getLineNumber()", 100, ex.getLineNumber());
            assertEquals("ex.getColumnNumber()", 1000, ex.getColumnNumber());
            assertNull("ex.getException()", ex.getException());
        }
    }

    @Test
    public void testGenericContentHandlerFatalErrorThrowsNullPointerException() throws Throwable {
        try {
            new GenericPackager().new GenericContentHandler().fatalError(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericContentHandlerFatalErrorThrowsSAXParseException() throws Throwable {
        SAXParseException ex2 = new SAXParseException("testGenericContentHandlerParam1", new LocatorImpl());
        try {
            new GenericPackager().new GenericContentHandler().fatalError(ex2);
            fail("Expected SAXParseException to be thrown");
        } catch (SAXParseException ex) {
            assertEquals("ex.getMessage()", "testGenericContentHandlerParam1", ex.getMessage());
            assertNull("ex.getPublicId()", ex.getPublicId());
            assertNull("ex.getSystemId()", ex.getSystemId());
            assertEquals("ex.getLineNumber()", 0, ex.getLineNumber());
            assertEquals("ex.getColumnNumber()", 0, ex.getColumnNumber());
            assertNull("ex.getException()", ex.getException());
        }
    }

    @Test
    public void testGenericContentHandlerStartDocument() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericPackager().new GenericContentHandler();
        genericContentHandler.startDocument();
    }

    @Test
    public void testGenericContentHandlerStartElement() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericPackager().new GenericContentHandler();
        genericContentHandler.startDocument();
        genericContentHandler.startElement("testGenericContentHandlerNamespaceURI", "isopackager", "testGenericContentHandlerQName",
                new AttributesImpl());
    }

    @Test
    public void testGenericContentHandlerStartElement1() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new X92GenericPackager().new GenericContentHandler();
        Attributes atts = new AttributesImpl();
        genericContentHandler.startElement("testGenericContentHandlerNamespaceURI", "testGenericContentHandlerLocalName",
                "testGenericContentHandlerQName", atts);
    }

    @Test
    public void testGenericContentHandlerStartElementThrowsSAXException() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericPackager().new GenericContentHandler();
        try {
            genericContentHandler.startElement("testGenericContentHandlerNamespaceURI", "testGenericContentHandlerLocalName",
                    "testGenericContentHandlerQName", null);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
        }
    }

    @Test
    public void testGenericContentHandlerStartElementThrowsSAXException1() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericValidatingPackager().new GenericContentHandler();
        Attributes atts = new AttributesImpl();
        try {
            genericContentHandler.startElement("testGenericContentHandlerNamespaceURI", "isofield",
                    "testGenericContentHandlerQName", atts);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
            assertEquals("(AttributesImpl) atts.getLength()", 0, ((AttributesImpl) atts).getLength());
        }
    }

    @Test
    public void testGenericContentHandlerStartElementThrowsSAXException2() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericSubFieldPackager().new GenericContentHandler();
        Attributes atts = new Attributes2Impl();
        try {
            genericContentHandler
                    .startElement("testGenericContentHandlerNamespaceURI", null, "testGenericContentHandlerQName", atts);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
        }
    }

    @Test
    public void testGenericContentHandlerStartElementThrowsSAXException3() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericSubFieldPackager().new GenericContentHandler();
        Attributes atts = new AttributesImpl();
        try {
            genericContentHandler.startElement("testGenericContentHandlerNamespaceURI", "isopackager",
                    "testGenericContentHandlerQName", atts);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
            assertEquals("(AttributesImpl) atts.getLength()", 0, ((AttributesImpl) atts).getLength());
        }
    }

    @Test
    public void testGenericContentHandlerStartElementThrowsSAXException4() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new X92GenericPackager().new GenericContentHandler();
        Attributes atts = new Attributes2Impl();
        try {
            genericContentHandler.startElement("testGenericContentHandlerNamespaceURI", "isofieldpackager",
                    "testGenericContentHandlerQName", atts);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertEquals("ex.getMessage()", "null", ex.getMessage());
            assertEquals("ex.getException().getMessage()", "null", ex.getException().getMessage());
        }
    }

    @Test
    public void testGetBitMapfieldPackager() throws Throwable {
        ISOFieldPackager iFA_AMOUNT = new IFA_AMOUNT();
        ISOFieldPackager[] fld = new ISOFieldPackager[2];
        fld[1] = iFA_AMOUNT;
        GenericPackager genericPackager = new GenericPackager();
        genericPackager.setFieldPackager(fld);
        ISOFieldPackager result = genericPackager.getBitMapfieldPackager();
        assertSame("result", iFA_AMOUNT, result);
    }

    @Test
    public void testGetBitMapfieldPackagerThrowsArrayIndexOutOfBoundsException() throws Throwable {
        ISOFieldPackager[] fld = new ISOFieldPackager[0];
        GenericPackager genericPackager = new GenericPackager();
        genericPackager.setFieldPackager(fld);
        try {
            genericPackager.getBitMapfieldPackager();
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            assertEquals("ex.getMessage()", "1", ex.getMessage());
        }
    }

    @Test
    public void testGetBitMapfieldPackagerThrowsNullPointerException() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        try {
            genericPackager.getBitMapfieldPackager();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetFirstField() throws Throwable {
        GenericPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[3];
        fld[1] = new IFA_BITMAP();
        genericValidatingPackager.setFieldPackager(fld);
        int result = genericValidatingPackager.getFirstField();
        assertEquals("result", 2, result);
    }

    @Test
    public void testGetFirstField1() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[3];
        genericPackager.setFieldPackager(fld);
        int result = genericPackager.getFirstField();
        assertEquals("result", 1, result);
    }

    @Test
    public void testGetFirstFieldThrowsArrayIndexOutOfBoundsException() throws Throwable {
        GenericPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[1];
        genericValidatingPackager.setFieldPackager(fld);
        assertEquals(0, genericValidatingPackager.getFirstField());
    }

    @Test
    public void testGetFirstFieldThrowsNullPointerException() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        try {
            genericPackager.getFirstField();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetMaxValidField() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        int result = genericPackager.getMaxValidField();
        assertEquals("result", 128, result);
    }

    @Test
    public void testReadFileThrowsISOException() throws Throwable {
        try {
            new GenericSubFieldPackager().readFile(new ByteArrayInputStream("".getBytes()));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getNested().getClass()", SAXParseException.class, ex.getNested().getClass());
            assertEquals("ex.getNested().getMessage()", "Premature end of file.", ex.getNested().getMessage());
        }
    }

    @Test
    public void testReadFileThrowsISOException1() throws Throwable {
        try {
            new GenericPackager().readFile("testGenericPackagerFilename");
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getNested().getClass()", FileNotFoundException.class, ex.getNested().getClass());
        }
    }

    @Ignore ("test failing")
    @Test
    public void testSetConfigurationThrowsConfigurationException() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        Configuration cfg = new SimpleConfiguration();
        try {
            genericPackager.setConfiguration(cfg);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertEquals(
                    "ex.getMessage()",
                    "org.jpos.iso.ISOException: java.lang.ClassNotFoundException: org.apache.crimson.parser.XMLReaderImpl (java.lang.ClassNotFoundException: org.apache.crimson.parser.XMLReaderImpl)",
                    ex.getMessage());
            assertEquals("ex.getNested().getMessage()", "java.lang.ClassNotFoundException: org.apache.crimson.parser.XMLReaderImpl",
                    ex.getNested().getMessage());
            assertEquals("(GenericValidatingPackager) genericValidatingPackager.getLogger().getName()", "",
                    genericPackager.getLogger().getName());
            assertEquals("(GenericValidatingPackager) genericValidatingPackager.getRealm()", "",
                    genericPackager.getRealm());
        }
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        GenericPackager genericSubFieldPackager = new GenericSubFieldPackager();
        Configuration cfg = new SubConfiguration();
        try {
            genericSubFieldPackager.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("(GenericSubFieldPackager) genericSubFieldPackager.getLogger()", genericSubFieldPackager.getLogger());
            assertNull("(GenericSubFieldPackager) genericSubFieldPackager.getRealm()", genericSubFieldPackager.getRealm());
        }
    }
}
