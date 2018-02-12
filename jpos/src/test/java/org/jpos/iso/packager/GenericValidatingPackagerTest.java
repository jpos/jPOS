/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.jpos.iso.ISOBaseValidator;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldValidator;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOVMsg;
import org.jpos.iso.IVA_ALPHANUM;
import org.jpos.iso.IVA_ALPHANUMNOBLANK;
import org.jpos.iso.IVA_ALPHANUMNOZERO_NOBLANK;
import org.jpos.iso.validator.ISOVException;
import org.jpos.iso.validator.MSGTEST;
import org.jpos.iso.validator.MSGTEST02;
import org.jpos.iso.validator.TEST0100;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.Attributes2Impl;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

public class GenericValidatingPackagerTest {
    @Test
    public void testConstructor() throws Throwable {
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        assertEquals("genericValidatingPackager.bitmapField", 1, genericValidatingPackager.bitmapField);
        assertNull("genericValidatingPackager.getLogger()", genericValidatingPackager.getLogger());
        assertEquals("genericValidatingPackager.inc", 500, GenericValidatingPackager.inc);
        assertEquals("genericValidatingPackager.maxValidField", 128, genericValidatingPackager.maxValidField);
        assertNull("genericValidatingPackager.getRealm()", genericValidatingPackager.getRealm());
        assertTrue("genericValidatingPackager.emitBitmap", genericValidatingPackager.emitBitmap);
    }

    @Ignore("test fails - GenericValidatingPackager to be deprecated")
    @Test
    public void testConstructorThrowsISOException() throws Throwable {
        try {
            new GenericValidatingPackager("testGenericValidatingPackagerFileName");
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "java.lang.ClassNotFoundException: org.apache.crimson.parser.XMLReaderImpl",
                    ex.getMessage());
            assertEquals("ex.getNested().getMessage()", "SAX2 driver class org.apache.crimson.parser.XMLReaderImpl not found", ex
                    .getNested().getMessage());
        }
    }

    @Ignore("test fails - GenericValidatingPackager to be deprecated")
    @Test
    public void testConstructorThrowsISOException1() throws Throwable {
        try {
            new GenericValidatingPackager(new ByteArrayInputStream("x".getBytes()));
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "org.xml.sax.SAXParseException: Content is not allowed in prolog.", ex.getMessage());
            assertEquals("ex.getNested().getMessage()", "Content is not allowed in prolog.", ex.getNested().getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerConstructor() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        assertEquals("genericValidatorContentHandler.VALIDATOR_INDEX", -3, GenericValidatingPackager.GenericValidatorContentHandler.VALIDATOR_INDEX);
    }

    @Test
    public void testGenericValidatorContentHandlerEndDocument() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        genericValidatorContentHandler.startDocument();
        genericValidatorContentHandler.endDocument();
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testGenericValidatorContentHandlerEndDocumentThrowsNullPointerException() throws Throwable {
        try {
            new GenericValidatingPackager().new GenericValidatorContentHandler().endDocument();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerEndDocumentThrowsSAXException() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        genericValidatorContentHandler.startDocument();
        genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isopackager",
                "testGenericValidatorContentHandlerQName", new AttributesImpl());
        try {
            genericValidatorContentHandler.endDocument();
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getException()", ex.getException());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerEndElement() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        genericValidatorContentHandler.startDocument();
        genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isopackager",
                "testGenericValidatorContentHandlerQName", new AttributesImpl());
        genericValidatorContentHandler.endElement("testGenericValidatorContentHandlerNamespaceURI", "isopackager",
                "testGenericValidatorContentHandlerQName");
    }

    @Test
    public void testGenericValidatorContentHandlerEndElement1() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        genericValidatorContentHandler.endElement("testGenericValidatorContentHandlerNamespaceURI",
                "testGenericValidatorContentHandlerLocalName", "testGenericValidatorContentHandlerQName");
    }

    @Test
    public void testGenericValidatorContentHandlerEndElementThrowsClassCastException() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        genericValidatorContentHandler.startDocument();
        genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isopackager",
                "testGenericValidatorContentHandlerQName", new AttributesImpl());
        try {
            genericValidatorContentHandler.endElement("testGenericValidatorContentHandlerNamespaceURI", "isofieldvalidator",
                    "testGenericValidatorContentHandlerQName");
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals("ex.getClass()", ClassCastException.class, ex.getClass());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerEndElementThrowsEmptyStackException() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        genericValidatorContentHandler.startDocument();
        try {
            genericValidatorContentHandler.endElement("testGenericValidatorContentHandlerNamespaceURI", "isopackager",
                    "testGenericValidatorContentHandlerQName");
            fail("Expected EmptyStackException to be thrown");
        } catch (EmptyStackException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerEndElementThrowsNullPointerException() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        try {
            genericValidatorContentHandler.endElement("testGenericValidatorContentHandlerNamespaceURI", "isovalidator",
                    "testGenericValidatorContentHandlerQName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerEndElementThrowsNullPointerException1() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        try {
            genericValidatorContentHandler.endElement("testGenericValidatorContentHandlerNamespaceURI", "isofieldpackager",
                    "testGenericValidatorContentHandlerQName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerEndElementThrowsNullPointerException2() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        try {
            genericValidatorContentHandler.endElement("testGenericValidatorContentHandlerNamespaceURI", null,
                    "testGenericValidatorContentHandlerQName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerErrorThrowsNullPointerException() throws Throwable {
        try {
            new GenericValidatingPackager().new GenericValidatorContentHandler().error(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerErrorThrowsSAXParseException() throws Throwable {
        SAXParseException ex2 = new SAXParseException("testGenericValidatorContentHandlerParam1", new LocatorImpl());
        try {
            new GenericValidatingPackager().new GenericValidatorContentHandler().error(ex2);
            fail("Expected SAXParseException to be thrown");
        } catch (SAXParseException ex) {
            assertEquals("ex.getMessage()", "testGenericValidatorContentHandlerParam1", ex.getMessage());
            assertNull("ex.getPublicId()", ex.getPublicId());
            assertNull("ex.getSystemId()", ex.getSystemId());
            assertEquals("ex.getLineNumber()", 0, ex.getLineNumber());
            assertEquals("ex.getColumnNumber()", 0, ex.getColumnNumber());
            assertNull("ex.getException()", ex.getException());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerFatalErrorThrowsNullPointerException() throws Throwable {
        try {
            new GenericValidatingPackager().new GenericValidatorContentHandler().fatalError(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenericValidatorContentHandlerMakeFieldValidatorArray1() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Hashtable tab = new Properties();
        tab.put(Integer.valueOf(-4), new IVA_ALPHANUMNOBLANK(true, "testGenericValidatorContentHandlerDescription"));
        ISOFieldValidator[] result = genericValidatorContentHandler.makeFieldValidatorArray(tab);
        assertEquals("result.length", 1, result.length);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenericValidatorContentHandlerMakeFieldValidatorArray2() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Hashtable tab = new Hashtable(100);
        tab.put(Integer.valueOf(-2), new IVA_ALPHANUMNOBLANK("testGenericValidatorContentHandlerDescription"));
        tab.put(Integer.valueOf(-4), new IVA_ALPHANUMNOBLANK(100, 1000, "testGenericValidatorContentHandlerDescription"));
        ISOFieldValidator[] result = genericValidatorContentHandler.makeFieldValidatorArray(tab);
        assertEquals("result.length", 2, result.length);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenericValidatorContentHandlerMakeFieldValidatorArray4() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        ISOFieldValidator[] result = genericValidatorContentHandler.makeFieldValidatorArray(new Hashtable(100));
        assertEquals("result.length", 0, result.length);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenericValidatorContentHandlerMakeFieldValidatorArrayThrowsClassCastException1() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Map hashMap = new HashMap();
        hashMap.put("", "testString");
        Hashtable tab = new Hashtable(hashMap);
        tab.put(Integer.valueOf(100), new IVA_ALPHANUMNOZERO_NOBLANK(true, "testGenericValidatorContentHandlerDescription"));
        try {
            genericValidatorContentHandler.makeFieldValidatorArray(tab);
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals("ex.getClass()", ClassCastException.class, ex.getClass());
            assertEquals("tab.size()", 2, tab.size());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenericValidatorContentHandlerMakeFieldValidatorArrayThrowsClassCastException2() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Hashtable tab = new Hashtable(100);
        tab.put("testString", new Object());
        try {
            genericValidatorContentHandler.makeFieldValidatorArray(tab);
            fail("Expected ClassCastException to be thrown");
        } catch (ClassCastException ex) {
            assertEquals("ex.getClass()", ClassCastException.class, ex.getClass());
            assertEquals("tab.size()", 1, tab.size());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerMakeFieldValidatorArrayThrowsNullPointerException() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        try {
            genericValidatorContentHandler.makeFieldValidatorArray(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGenericValidatorContentHandlerMakeMsgValidatorArray() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Vector vector = new Vector();
        vector.add(null);
        Hashtable tab = new Hashtable(100);
        tab.put(Integer.valueOf(-3), vector);
        ISOBaseValidator[] result = genericValidatorContentHandler.makeMsgValidatorArray(tab);
        assertEquals("result.length", 1, result.length);
        assertNull("result[0]", result[0]);
    }

    @Test
    public void testGenericValidatorContentHandlerMakeMsgValidatorArrayThrowsNullPointerException() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        try {
            genericValidatorContentHandler.makeMsgValidatorArray(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartDocument() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        genericValidatorContentHandler.startDocument();
    }

    @Test
    public void testGenericValidatorContentHandlerStartElement() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        genericValidatorContentHandler.startDocument();
        genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isopackager",
                "testGenericValidatorContentHandlerQName", new AttributesImpl());
    }

    @Test
    public void testGenericValidatorContentHandlerStartElement1() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Attributes atts = new Attributes2Impl();
        genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI",
                "testGenericValidatorContentHandlerLocalName", "testGenericValidatorContentHandlerQName", atts);
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Attributes atts = new AttributesImpl();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isofieldpackager",
                    "testGenericValidatorContentHandlerQName", atts);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertEquals("ex.getMessage()", "null", ex.getMessage());
            assertEquals("ex.getException().getMessage()", "null", ex.getException().getMessage());
            assertEquals("(AttributesImpl) atts.getLength()", 0, atts.getLength());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException1() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Attributes atts = new AttributesImpl();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isofieldvalidator",
                    "testGenericValidatorContentHandlerQName", atts);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
            assertEquals("(AttributesImpl) atts.getLength()", 0, atts.getLength());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException10() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Attributes atts = new Attributes2Impl();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isopackager",
                    "testGenericValidatorContentHandlerQName", atts);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException11() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isofieldvalidator",
                    "testGenericValidatorContentHandlerQName", null);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException2() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Attributes atts = new Attributes2Impl();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", null,
                    "testGenericValidatorContentHandlerQName", atts);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException3() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isofieldpackager",
                    "testGenericValidatorContentHandlerQName", null);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException4() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isovalidator",
                    "testGenericValidatorContentHandlerQName", null);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException5() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Attributes atts = new AttributesImpl();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isofield",
                    "testGenericValidatorContentHandlerQName", atts);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
            assertEquals("(AttributesImpl) atts.getLength()", 0, atts.getLength());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException6() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isofield",
                    "testGenericValidatorContentHandlerQName", null);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException7() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Attributes atts = new AttributesImpl();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isovalidator",
                    "testGenericValidatorContentHandlerQName", atts);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
            assertEquals("(AttributesImpl) atts.getLength()", 0, atts.getLength());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException8() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        genericValidatorContentHandler.startDocument();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "isopackager",
                    "testGenericValidatorContentHandlerQName", null);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
        }
    }

    @Test
    public void testGenericValidatorContentHandlerStartElementThrowsSAXException9() throws Throwable {
        GenericValidatingPackager.GenericValidatorContentHandler genericValidatorContentHandler = new GenericValidatingPackager().new GenericValidatorContentHandler();
        Attributes atts = new AttributesImpl();
        try {
            genericValidatorContentHandler.startElement("testGenericValidatorContentHandlerNamespaceURI", "property",
                    "testGenericValidatorContentHandlerQName", atts);
            fail("Expected SAXException to be thrown");
        } catch (SAXException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getException().getMessage()", ex.getException().getMessage());
            assertEquals("(AttributesImpl) atts.getLength()", 0, atts.getLength());
        }
    }

    @Ignore("test fails - GenericValidatingPackager is an unmaintained and going to be deprecated")
    @Test
    public void testReadFileThrowsISOException() throws Throwable {
        try {
            new GenericValidatingPackager().readFile("testGenericValidatingPackagerFilename");
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "java.lang.ClassNotFoundException: org.apache.crimson.parser.XMLReaderImpl",
                    ex.getMessage());
            assertEquals("ex.getNested().getMessage()", "SAX2 driver class org.apache.crimson.parser.XMLReaderImpl not found", ex
                    .getNested().getMessage());
        }
    }

    @Test
    public void testSetFieldValidator() throws Throwable {
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOFieldValidator[] fvlds = new ISOFieldValidator[1];
        genericValidatingPackager.setFieldValidator(fvlds);
        assertSame("genericValidatingPackager.fvlds", fvlds, genericValidatingPackager.fvlds);
    }

    @Test
    public void testSetGenericPackagerParams() throws Throwable {
        Attributes atts = new AttributesImpl();
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        genericValidatingPackager.setGenericPackagerParams(atts);
        assertEquals("genericValidatingPackager.bitmapField", 1, genericValidatingPackager.bitmapField);
        assertEquals("genericValidatingPackager.maxValidField", 128, genericValidatingPackager.maxValidField);
        assertTrue("genericValidatingPackager.emitBitmap", genericValidatingPackager.emitBitmap);
        assertEquals("(AttributesImpl) atts.getLength()", 0, atts.getLength());
    }

    @Test
    public void testSetGenericPackagerParamsThrowsNullPointerException() throws Throwable {
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        try {
            genericValidatingPackager.setGenericPackagerParams(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertEquals("genericValidatingPackager.bitmapField", 1, genericValidatingPackager.bitmapField);
            assertEquals("genericValidatingPackager.maxValidField", 128, genericValidatingPackager.maxValidField);
            assertTrue("genericValidatingPackager.emitBitmap", genericValidatingPackager.emitBitmap);
        }
    }

    @Test
    public void testSetMsgValidator() throws Throwable {
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[3];
        genericValidatingPackager.setMsgValidator(msgVlds);
        assertSame("genericValidatingPackager.mvlds", msgVlds, genericValidatingPackager.mvlds);
    }

    @Test
    public void testValidate1() throws Throwable {
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOFieldValidator[] fvlds = new ISOFieldValidator[2];
        fvlds[0] = new ISOFieldValidator();
        fvlds[1] = new IVA_ALPHANUMNOZERO_NOBLANK();
        genericValidatingPackager.setFieldValidator(fvlds);
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[0] = new MSGTEST();
        msgVlds[1] = new ISOBaseValidator();
        genericValidatingPackager.setMsgValidator(msgVlds);
        ISOMsg m = new ISOMsg("testGenericValidatingPackagerMti");
        m.setMTI("testGenericValidatingPackagerMti");
        ISOVMsg result = (ISOVMsg) genericValidatingPackager.validate(m);
        assertNotNull("result", result);
    }

    @Test
    public void testValidate6() throws Throwable {
        ISOBaseValidator mSGTEST = new MSGTEST(false);
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOBaseValidator mSGTEST2 = new MSGTEST(false);
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[3];
        msgVlds[0] = mSGTEST2;
        msgVlds[1] = mSGTEST;
        msgVlds[2] = mSGTEST2;
        genericValidatingPackager.setMsgValidator(msgVlds);
        ISOFieldValidator[] fvlds = new ISOFieldValidator[1];
        fvlds[0] = new IVA_ALPHANUM("testGenericValidatingPackagerDescription");
        genericValidatingPackager.setFieldValidator(fvlds);
        ISOVMsg result = (ISOVMsg) genericValidatingPackager.validate(new ISOMsg(100));
        assertNotNull("result", result);
    }

    @Test
    public void testValidateThrowsISOException() throws Throwable {
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOFieldValidator[] fvlds = new ISOFieldValidator[1];
        fvlds[0] = new IVA_ALPHANUMNOBLANK("testGenericValidatingPackagerDescription");
        genericValidatingPackager.setFieldValidator(fvlds);
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[3];
        msgVlds[0] = new TEST0100();
        genericValidatingPackager.setMsgValidator(msgVlds);
        try {
            genericValidatingPackager.validate(new ISOMsg());
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertEquals("ex.getMessage()", "MTI not available", ex.getMessage());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testValidateThrowsISOVException() throws Throwable {
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[3];
        msgVlds[0] = new MSGTEST(false);
        msgVlds[1] = new MSGTEST(true);
        genericValidatingPackager.setMsgValidator(msgVlds);
        ISOFieldValidator[] fvlds = new ISOFieldValidator[1];
        fvlds[0] = new IVA_ALPHANUM("testGenericValidatingPackagerDescription");
        genericValidatingPackager.setFieldValidator(fvlds);
        try {
            genericValidatingPackager.validate(new ISOMsg(100));
            fail("Expected ISOVException to be thrown");
        } catch (ISOVException ex) {
            assertEquals("ex.getMessage()", "Error on msg. ", ex.getMessage());
            assertFalse("ex.treated()", ex.treated());
            assertNotNull("ex.getErrComponent()", ex.getErrComponent());
            assertNull("ex.getNested()", ex.getNested());
        }
    }

    @Test
    public void testValidateThrowsNullPointerException() throws Throwable {
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOFieldValidator[] fvlds = new ISOFieldValidator[0];
        genericValidatingPackager.setFieldValidator(fvlds);
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[0] = new ISOBaseValidator();
        msgVlds[1] = new MSGTEST02();
        genericValidatingPackager.setMsgValidator(msgVlds);
        try {
            genericValidatingPackager.validate(new ISOMsg());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testValidateThrowsNullPointerException1() throws Throwable {
        try {
            new GenericValidatingPackager().validate(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testValidateThrowsNullPointerException3() throws Throwable {
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[0] = new MSGTEST02();
        genericValidatingPackager.setMsgValidator(msgVlds);
        ISOFieldValidator[] fvlds = new ISOFieldValidator[2];
        fvlds[0] = new IVA_ALPHANUMNOBLANK(100, "testGenericValidatingPackagerDescription");
        genericValidatingPackager.setFieldValidator(fvlds);
        try {
            genericValidatingPackager.validate(new ISOMsg());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testValidateThrowsNullPointerException4() throws Throwable {
        GenericValidatingPackager genericValidatingPackager = new GenericValidatingPackager();
        ISOFieldValidator[] fvlds = new ISOFieldValidator[0];
        genericValidatingPackager.setFieldValidator(fvlds);
        ISOBaseValidator[] msgVlds = new ISOBaseValidator[2];
        msgVlds[1] = new MSGTEST02();
        genericValidatingPackager.setMsgValidator(msgVlds);
        try {
            genericValidatingPackager.validate(new ISOMsg());
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
