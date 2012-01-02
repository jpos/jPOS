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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.EmptyStackException;
import java.util.List;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Attributes2Impl;
import org.xml.sax.helpers.AttributesImpl;

public class XMLPackagerTest {
    private Attributes atts;
    private ISOMsg isoMsg;
    private XMLPackager xMLPackager;
    private Logger logger;

    byte[] hex2byte(String hexString) {
        return ISOUtil.hex2byte(hexString.replaceAll(" ", ""));
    }

    @Before
    public void onSetup() throws ISOException, NoSuchFieldException {
        // PrintStream p = new PrintStream(new ByteArrayOutputStream())
        xMLPackager = new XMLPackager();
        atts = new Attributes2Impl();
        logger = new Logger();
        logger.addListener(new SimpleLogListener());
        isoMsg = xMLPackager.createISOMsg();
        xMLPackager.setLogger(logger, xMLPackager.getClass().getName());
        isoMsg.setPackager(xMLPackager);
    }

    @Test
    public void testCharactersThrowsEmptyStackException() throws Throwable {
        char[] ch = new char[0];
        try {
            new XMLPackager().characters(ch, 100, 1000);
            fail("Expected EmptyStackException to be thrown");
        } catch (EmptyStackException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        assertThat(xMLPackager.realm, is(xMLPackager.getClass().getName()));
        assertThat(xMLPackager.logger, is(logger));
    }

    @Test
    public void testCreateISOMsg() throws Throwable {
        ISOMsg result = new XMLPackager().createISOMsg();
        assertEquals("result.getDirection()", 0, result.getDirection());
    }

    @Test
    public void testEndElementThrowsEmptyStackException() throws Throwable {
        try {
            xMLPackager.endElement("testXMLPackagerNs", "header", "testXMLPackagerQname");
            fail("Expected EmptyStackException to be thrown");
        } catch (EmptyStackException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEndElementThrowsEmptyStackException1() throws Throwable {
        try {
            xMLPackager.endElement("testXMLPackagerNs", "isomsg", "testXMLPackagerQname");
            fail("Expected EmptyStackException to be thrown");
        } catch (EmptyStackException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testEndElementThrowsNullPointerException() throws Throwable {
        try {
            xMLPackager.endElement("testXMLPackagerNs", null, "testXMLPackagerQname");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetFieldDescription() throws Throwable {
        ISOComponent m = new ISOMsg(100);
        String result = xMLPackager.getFieldDescription(m, 100);
        assertEquals("result", "<notavailable/>", result);
    }

    @Test
    public void testGetLogger() throws Throwable {
        Logger logger = Logger.getLogger("testXMLPackagerName");
        xMLPackager.setLogger(logger, "testXMLPackagerRealm");
        Logger result = xMLPackager.getLogger();
        assertSame("result", logger, result);
    }

    @Test
    public void testGetRealm() throws Throwable {
        String result = new XMLPackager().getRealm();
        assertNull("result", result);
    }

    @Test
    public void testGetRealm1() throws Throwable {
        xMLPackager.setLogger(Logger.getLogger("testXMLPackagerName"), "testXMLPackagerRealm");
        String result = xMLPackager.getRealm();
        assertEquals("result", "testXMLPackagerRealm", result);
    }

    @Test
    public void testPackException() {
        ISOField field = new ISOField();
        try {
            xMLPackager.pack(field);
            fail("Exception Expected - not an isomsg");
        } catch (ISOException e) {
            assertThat(e.getMessage(), is("cannot pack class org.jpos.iso.ISOField"));
        }
    }

    @Test
    public void testPack() throws IOException, ISOException, SAXException {
        isoMsg.setMTI("0800");
        isoMsg.set(7, "7654321");
        isoMsg.set(11, "12345678");
        isoMsg.set(12, "20110224112759");
        isoMsg.set(24, "");
        byte[] data = isoMsg.pack();
        // System.out.println(new String(data));
        String expected = "<isomsg><!-- org.jpos.iso.packager.XMLPackager --><field id=\"0\" value=\"0800\"/>"
                + "<field id=\"7\" value=\"7654321\"/><field id=\"11\" value=\"12345678\"/>"
                + "<field id=\"12\" value=\"20110224112759\"/><field id=\"24\" value=\"\"/></isomsg>";
        XMLUnit.setIgnoreWhitespace(true);
        // XMLAssert.assertXMLEqual(expected, new String(data));
        DetailedDiff myDiff = new DetailedDiff(XMLUnit.compareXML(expected, new String(data)));
        List allDifferences = myDiff.getAllDifferences();
        assertEquals(myDiff.toString(), 0, allDifferences.size());
    }

    @Test
    public void testUnpackBytes() throws IOException, ISOException {
        String input = "<isomsg><!-- org.jpos.iso.packager.XMLPackager --><field id=\"0\" value=\"0800\"/>"
                + "<field id=\"7\" value=\"7654321\"/><field id=\"11\" value=\"12345678\"/>"
                + "<field id=\"12\" value=\"20110224112759\"/><field id=\"24\" value=\"831\"/></isomsg>";
        isoMsg.setMTI("0800");
        isoMsg.set(7, "7654321");
        isoMsg.set(11, "12345678");
        isoMsg.set(12, "20110224112759");
        isoMsg.set(24, "");
        ISOMsg result = xMLPackager.createISOMsg();
        int consumedBytes = xMLPackager.unpack(result, input.getBytes());
        assertThat(consumedBytes, is(218));
        assertThat(result.getMTI(), is("0800"));
        assertThat(result.getString(7), is("7654321"));
        assertThat(result.getString(11), is("12345678"));
        assertThat(result.getString(12), is("20110224112759"));
        assertThat(result.getString(24), is("831"));
    }

    @Test
    public void testUnpackStream() throws IOException, ISOException {
        String input = "<isomsg><!-- org.jpos.iso.packager.XMLPackager --><field id=\"0\" value=\"0800\"/>"
                + "<field id=\"7\" value=\"7654321\"/><field id=\"11\" value=\"12345678\"/>"
                + "<field id=\"12\" value=\"20110224112759\"/><field id=\"24\" value=\"\"/></isomsg>";
        isoMsg.setMTI("0800");
        isoMsg.set(7, "7654321");
        isoMsg.set(11, "12345678");
        isoMsg.set(12, "20110224112759");
        isoMsg.set(24, "831");
        ISOMsg result = xMLPackager.createISOMsg();
        xMLPackager.unpack(result, new ByteArrayInputStream(input.getBytes()));
        assertThat(result.getMTI(), is("0800"));
        assertThat(result.getString(7), is("7654321"));
        assertThat(result.getString(11), is("12345678"));
        assertThat(result.getString(12), is("20110224112759"));
        assertThat(result.getString(24), is(""));
    }

    @Test
    public void testSetLogger() throws Throwable {
        Logger logger = Logger.getLogger("testXMLPackagerName");
        xMLPackager.setLogger(logger, "testXMLPackagerRealm");
        assertSame("xMLPackager.logger", logger, xMLPackager.logger);
        assertEquals("xMLPackager.realm", "testXMLPackagerRealm", xMLPackager.realm);
    }

    @Test
    public void testStartElement1() throws Throwable {
        xMLPackager.startElement("testXMLPackagerNs", "testXMLPackagerName", "testXMLPackagerQName", atts);
        assertEquals("(AttributesImpl) atts.getLength()", 0, ((AttributesImpl) atts).getLength());
    }

    @Test
    public void testStartElement2() throws Throwable {
        xMLPackager.startElement("testXMLPackagerNs", "header", "testXMLPackagerQName", atts);
    }

    @Test
    public void testStartElementThrowsEmptyStackException() throws Throwable {
        try {
            xMLPackager.startElement("testXMLPackagerNs", "field", "testXMLPackagerQName", atts);
            fail("Expected EmptyStackException to be thrown");
        } catch (EmptyStackException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStartElementThrowsNullPointerException() throws Throwable {
        Attributes atts = new Attributes2Impl();
        try {
            xMLPackager.startElement("testXMLPackagerNs", null, "testXMLPackagerQName", atts);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testStartElementThrowsNullPointerException1() throws Throwable {
        try {
            xMLPackager.startElement("testXMLPackagerNs", "testXMLPackagerName", "testXMLPackagerQName", null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
