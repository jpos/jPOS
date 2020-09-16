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

package org.jpos.iso.packager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
        assertNull(genericPackager.getLogger(), "genericPackager.getLogger()");
        assertNull(genericPackager.getRealm(), "genericPackager.getRealm()");
    }

    @Test
    public void testConstructorThrowsISOException() throws Throwable {
        try {
            new GenericPackager("testGenericPackagerFilename");
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals(FileNotFoundException.class, ex.getNested().getClass(), "ex.getNested().getClass()");
        }
    }

    @Test
    public void testConstructorThrowsISOException1() throws Throwable {
        try {
            new GenericPackager(new ByteArrayInputStream("".getBytes()));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals(SAXParseException.class, ex.getNested().getClass(), "ex.getNested().getClass()");
            assertEquals("Premature end of file.", ex.getNested().getMessage(), "ex.getNested().getMessage()");
        }
    }

    @Test
    public void testEmitBitMap() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        boolean result = genericPackager.emitBitMap();
        assertTrue(result, "result");
    }

    @Test
    public void testGenericContentHandlerConstructor() throws Throwable {
        new GenericSubFieldPackager().new GenericContentHandler();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testGenericContentHandlerEndDocument() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new X92GenericPackager().new GenericContentHandler();
        genericContentHandler.startDocument();
        genericContentHandler.endDocument();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testGenericContentHandlerEndDocumentThrowsNullPointerException() throws Throwable {
        try {
            new GenericSubFieldPackager().new GenericContentHandler().endDocument();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Stack.isEmpty()\" because \"this.fieldStack\" is null", ex.getMessage(), "ex.getMessage()");
            }
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
            assertEquals("Format error in XML Field Description File", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getException(), "ex.getException()");
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
            assertEquals(ClassCastException.class, ex.getClass(), "ex.getClass()");
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
            assertNull(ex.getMessage(), "ex.getMessage()");
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Stack.pop()\" because \"this.fieldStack\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGenericContentHandlerEndElementThrowsNullPointerException1() throws Throwable {
        GenericPackager.GenericContentHandler genericContentHandler = new GenericPackager().new GenericContentHandler();
        try {
            genericContentHandler.endElement("testGenericContentHandlerNamespaceURI", null, "testGenericContentHandlerQName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.equals(Object)\" because \"localName\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGenericContentHandlerErrorThrowsNullPointerException() throws Throwable {
        try {
            new GenericSubFieldPackager().new GenericContentHandler().error(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot throw exception because \"ex\" is null", ex.getMessage(), "ex.getMessage()");
            }
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
            assertEquals("testGenericContentHandlerParam1", ex.getMessage(), "ex.getMessage()");
            assertEquals("testGenericContentHandlerParam2", ex.getPublicId(), "ex.getPublicId()");
            assertEquals("testGenericContentHandlerParam3", ex.getSystemId(), "ex.getSystemId()");
            assertEquals(100, ex.getLineNumber(), "ex.getLineNumber()");
            assertEquals(1000, ex.getColumnNumber(), "ex.getColumnNumber()");
            assertNull(ex.getException(), "ex.getException()");
        }
    }

    @Test
    public void testGenericContentHandlerFatalErrorThrowsNullPointerException() throws Throwable {
        try {
            new GenericPackager().new GenericContentHandler().fatalError(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot throw exception because \"ex\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGenericContentHandlerFatalErrorThrowsSAXParseException() throws Throwable {
        SAXParseException ex2 = new SAXParseException("testGenericContentHandlerParam1", new LocatorImpl());
        try {
            new GenericPackager().new GenericContentHandler().fatalError(ex2);
            fail("Expected SAXParseException to be thrown");
        } catch (SAXParseException ex) {
            assertEquals("testGenericContentHandlerParam1", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getPublicId(), "ex.getPublicId()");
            assertNull(ex.getSystemId(), "ex.getSystemId()");
            assertEquals(0, ex.getLineNumber(), "ex.getLineNumber()");
            assertEquals(0, ex.getColumnNumber(), "ex.getColumnNumber()");
            assertNull(ex.getException(), "ex.getException()");
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("java.lang.NullPointerException", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("java.lang.NullPointerException: Cannot invoke \"org.xml.sax.Attributes.getValue(String)\" because \"atts\" is null", ex.getMessage(), "ex.getMessage()");
            }
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getException().getMessage(), "ex.getException().getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.xml.sax.Attributes.getValue(String)\" because \"atts\" is null", ex.getException().getMessage(), "ex.getException().getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("java.lang.NullPointerException", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(ex.getException().getMessage(), "ex.getException().getMessage()");
            assertEquals(0, atts.getLength(), "(AttributesImpl) atts.getLength()");
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("java.lang.NullPointerException", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("java.lang.NullPointerException: Cannot invoke \"String.equals(Object)\" because \"localName\" is null", ex.getMessage(), "ex.getMessage()");
            }
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getException().getMessage(), "ex.getException().getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.equals(Object)\" because \"localName\" is null", ex.getException().getMessage(), "ex.getException().getMessage()");
            }
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else if (isJavaVersionAtMost(JAVA_14)) {
                assertEquals("java.lang.NullPointerException", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("java.lang.NullPointerException: Cannot invoke \"java.util.Stack.push(Object)\" because \"this.fieldStack\" is null", ex.getMessage(), "ex.getMessage()");
            }
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getException().getMessage(), "ex.getException().getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Stack.push(Object)\" because \"this.fieldStack\" is null", ex.getException().getMessage(), "ex.getException().getMessage()");
            }
            assertEquals(0, atts.getLength(), "(AttributesImpl) atts.getLength()");
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("null", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("java.lang.NumberFormatException: null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals("null", ex.getException().getMessage(), "ex.getException().getMessage()");
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
        assertSame(iFA_AMOUNT, result, "result");
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
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("1", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 1 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetBitMapfieldPackagerThrowsNullPointerException() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        try {
            genericPackager.getBitMapfieldPackager();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot load from object array because \"this.fld\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetFirstField() throws Throwable {
        GenericPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[3];
        fld[1] = new IFA_BITMAP();
        genericValidatingPackager.setFieldPackager(fld);
        int result = genericValidatingPackager.getFirstField();
        assertEquals(2, result, "result");
    }

    @Test
    public void testGetFirstField1() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        ISOFieldPackager[] fld = new ISOFieldPackager[3];
        genericPackager.setFieldPackager(fld);
        int result = genericPackager.getFirstField();
        assertEquals(1, result, "result");
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot load from object array because \"this.fld\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetMaxValidField() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        int result = genericPackager.getMaxValidField();
        assertEquals(128, result, "result");
    }

    @Test
    public void testReadFileThrowsISOException() throws Throwable {
        try {
            new GenericSubFieldPackager().readFile(new ByteArrayInputStream("".getBytes()));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals(SAXParseException.class, ex.getNested().getClass(), "ex.getNested().getClass()");
            assertEquals("Premature end of file.", ex.getNested().getMessage(), "ex.getNested().getMessage()");
        }
    }

    @Test
    public void testReadFileThrowsISOException1() throws Throwable {
        try {
            new GenericPackager().readFile("testGenericPackagerFilename");
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals(FileNotFoundException.class, ex.getNested().getClass(), "ex.getNested().getClass()");
        }
    }

    @Disabled("test failing")
    @Test
    public void testSetConfigurationThrowsConfigurationException() throws Throwable {
        GenericPackager genericPackager = new GenericPackager();
        Configuration cfg = new SimpleConfiguration();
        try {
            genericPackager.setConfiguration(cfg);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertEquals(
                    "org.jpos.iso.ISOException: java.lang.ClassNotFoundException: org.apache.crimson.parser.XMLReaderImpl (java.lang.ClassNotFoundException: org.apache.crimson.parser.XMLReaderImpl)",
                    ex.getMessage(), "ex.getMessage()");
            assertEquals("java.lang.ClassNotFoundException: org.apache.crimson.parser.XMLReaderImpl",
                    ex.getNested().getMessage(), "ex.getNested().getMessage()");
            assertEquals("", genericPackager.getLogger().getName(),
                    "(GenericValidatingPackager) genericValidatingPackager.getLogger().getName()");
            assertEquals("", genericPackager.getRealm(),
                    "(GenericValidatingPackager) genericValidatingPackager.getRealm()");
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
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.get(String, String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(genericSubFieldPackager.getLogger(), "(GenericSubFieldPackager) genericSubFieldPackager.getLogger()");
            assertNull(genericSubFieldPackager.getRealm(), "(GenericSubFieldPackager) genericSubFieldPackager.getRealm()");
        }
    }
}
