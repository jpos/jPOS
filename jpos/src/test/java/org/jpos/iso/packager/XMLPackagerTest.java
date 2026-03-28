/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.String;
import java.util.EmptyStackException;
import java.util.List;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISODataset;
import org.jpos.iso.ISODatasetField;
import org.jpos.iso.DatasetFormat;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Attributes2Impl;

public class XMLPackagerTest {
    private Attributes atts;
    private ISOMsg isoMsg;
    private XMLPackager xMLPackager;
    private Logger logger;

    byte[] hex2byte(String hexString) {
        return ISOUtil.hex2byte(hexString.replaceAll(" ", ""));
    }

    @BeforeEach
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
            assertNull(ex.getMessage(), "ex.getMessage()");
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
        assertEquals(0, result.getDirection(), "result.getDirection()");
    }

    @Test
    public void testEndElementThrowsEmptyStackException() throws Throwable {
        try {
            xMLPackager.endElement("testXMLPackagerNs", "header", "testXMLPackagerQname");
            fail("Expected EmptyStackException to be thrown");
        } catch (EmptyStackException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testEndElementThrowsEmptyStackException1() throws Throwable {
        try {
            xMLPackager.endElement("testXMLPackagerNs", "isomsg", "testXMLPackagerQname");
            fail("Expected EmptyStackException to be thrown");
        } catch (EmptyStackException ex) {
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testEndElementThrowsNullPointerException() throws Throwable {
        try {
            xMLPackager.endElement("testXMLPackagerNs", null, "testXMLPackagerQname");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.equals(Object)\" because \"name\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetFieldDescription() throws Throwable {
        ISOComponent m = new ISOMsg(100);
        String result = xMLPackager.getFieldDescription(m, 100);
        // old version
        // assertEquals("<notavailable/>", result, "result");

        // new version, inspired in XML2003Packager
        assertEquals("Data element "+100, result, "result");
    }

    @Test
    public void testGetLogger() throws Throwable {
        Logger logger = Logger.getLogger("testXMLPackagerName");
        xMLPackager.setLogger(logger, "testXMLPackagerRealm");
        Logger result = xMLPackager.getLogger();
        assertSame(logger, result, "result");
    }

    @Test
    public void testGetRealm() throws Throwable {
        String result = new XMLPackager().getRealm();
        assertNull(result, "result");
    }

    @Test
    public void testGetRealm1() throws Throwable {
        xMLPackager.setLogger(Logger.getLogger("testXMLPackagerName"), "testXMLPackagerRealm");
        String result = xMLPackager.getRealm();
        assertEquals("testXMLPackagerRealm", result, "result");
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
        String expected = """
                <isomsg>
                  <field id="0" value="0800"/>
                  <field id="7" value="7654321"/>
                  <field id="11" value="12345678"/>
                  <field id="12" value="20110224112759"/>
                  <field id="24" value=""/>
                </isomsg>
                """;
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        DetailedDiff myDiff = new DetailedDiff(XMLUnit.compareXML(expected, new String(data)));
        List allDifferences = myDiff.getAllDifferences();
        assertEquals(0, allDifferences.size(), myDiff.toString());
    }

    @Test
    public void testPackDatasetFieldUsesHexTLVElementIds() throws Exception {
        ISODatasetField field55 = new ISODatasetField(55);
        ISODataset dataset = new ISODataset(0x37, DatasetFormat.TLV);
        dataset.addElement(0x9F26, new ISOBinaryField(0x9F26, hex2byte("1122334455667788")));
        field55.addDataset(dataset);
        isoMsg.set(field55);

        String xml = new String(xMLPackager.pack(isoMsg));

        assertThat(xml.contains("<element id=\"0x9F26\" value=\"1122334455667788\"/>"), is(true));
    }

    @Test
    public void testUnpackBytes() throws IOException, ISOException {
        String input = """
                <isomsg>
                  <header>686561646572</header>
                  <field id="0" value="0800"/>
                  <field id="7" value="7654321"/>
                  <field id="11" value="12345678"/>
                  <field id="12" value="20110224112759"/>
                  <field id="24" value="831"/>
                </isomsg>
                """;
        isoMsg.setHeader("header".getBytes());
        isoMsg.setMTI("0800");
        isoMsg.set(7, "7654321");
        isoMsg.set(11, "12345678");
        isoMsg.set(12, "20110224112759");
        isoMsg.set(24, "");
        ISOMsg result = xMLPackager.createISOMsg();
        int consumedBytes = xMLPackager.unpack(result, input.getBytes());
        assertThat(consumedBytes, is(input.getBytes().length));
        assertThat(result.getHeader(), is("header".getBytes()));
        assertThat(result.getMTI(), is("0800"));
        assertThat(result.getString(7), is("7654321"));
        assertThat(result.getString(11), is("12345678"));
        assertThat(result.getString(12), is("20110224112759"));
        assertThat(result.getString(24), is("831"));
    }

    @Test
    public void testUnpackLargeXmlBytes() throws IOException, ISOException {
        String veryLongXml = "<large-xml><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element><element><nested-element>Some very very long text</nested-element></element></large-xml>";
        String input = """
                <isomsg>
                  <field id="0" value="0800"/>
                  <field id="1"><![CDATA[%s]]></field>
                </isomsg>
                """.formatted(veryLongXml);
        isoMsg.setHeader("header".getBytes());
        ISOMsg result = xMLPackager.createISOMsg();
        int consumedBytes = xMLPackager.unpack(result, input.getBytes());
        assertThat(result.getString(1), is(veryLongXml));
    }

    @Test
    public void testUnpackStream() throws IOException, ISOException {
        String input = """
                <isomsg>
                  <header>686561646572</header>
                  <field id="0" value="0800"/>
                  <field id="7" value="7654321"/>
                  <field id="11" value="12345678"/>
                  <field id="12" value="20110224112759"/>
                  <field id="24" value=""/>
                </isomsg>
                """;
        isoMsg.setHeader("header".getBytes());
        isoMsg.setMTI("0800");
        isoMsg.set(7, "7654321");
        isoMsg.set(11, "12345678");
        isoMsg.set(12, "20110224112759");
        isoMsg.set(24, "831");
        ISOMsg result = xMLPackager.createISOMsg();
        xMLPackager.unpack(result, new ByteArrayInputStream(input.getBytes()));
        assertThat(result.getHeader(), is("header".getBytes()));
        assertThat(result.getMTI(), is("0800"));
        assertThat(result.getString(7), is("7654321"));
        assertThat(result.getString(11), is("12345678"));
        assertThat(result.getString(12), is("20110224112759"));
        assertThat(result.getString(24), is(""));
    }

    @Test
    public void testXML2003PackagerUnpacksDatasetXml() throws Exception {
        XML2003Packager packager = new XML2003Packager();
        ISOMsg result = packager.createISOMsg();
        String input = """
                <isomsg>
                  <field id="0" value="0100"/>
                  <field id="55" type="dataset">
                    <dataset id="37" format="TLV">
                      <element id="0x9F26" value="1122334455667788"/>
                      <element id="0x9F10" value="06011203A0B800"/>
                    </dataset>
                  </field>
                  <field id="49" type="dataset">
                    <dataset id="71" format="DBM">
                      <element id="1" value="31"/>
                      <element id="2" value="31323334"/>
                    </dataset>
                  </field>
                </isomsg>
                """;

        packager.unpack(result, input.getBytes());

        assertEquals("0100", result.getMTI());
        assertThat(result.getComponent(55) instanceof ISODatasetField, is(true));
        assertThat(result.getComponent(49) instanceof ISODatasetField, is(true));
        ISODataset field55 = (ISODataset) ((ISODatasetField) result.getComponent(55)).getDataset(0x37);
        ISODataset field49 = (ISODataset) ((ISODatasetField) result.getComponent(49)).getDataset(0x71);
        assertThat(ISOUtil.hexString(field55.getBytes(0x9F26)), is("1122334455667788"));
        assertThat(ISOUtil.hexString(field55.getBytes(0x9F10)), is("06011203A0B800"));
        assertThat(ISOUtil.hexString(field49.getBytes(1)), is("31"));
        assertThat(ISOUtil.hexString(field49.getBytes(2)), is("31323334"));
    }

    @Test
    public void testSetLogger() throws Throwable {
        Logger logger = Logger.getLogger("testXMLPackagerName");
        xMLPackager.setLogger(logger, "testXMLPackagerRealm");
        assertSame(logger, xMLPackager.logger, "xMLPackager.logger");
        assertEquals("testXMLPackagerRealm", xMLPackager.realm, "xMLPackager.realm");
    }

    @Test
    public void testStartElement1() throws Throwable {
        xMLPackager.startElement("testXMLPackagerNs", "testXMLPackagerName", "testXMLPackagerQName", atts);
        assertEquals(0, atts.getLength(), "(AttributesImpl) atts.getLength()");
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
            assertNull(ex.getMessage(), "ex.getMessage()");
        }
    }

    @Test
    public void testStartElementThrowsNullPointerException() throws Throwable {
        Attributes atts = new Attributes2Impl();
        try {
            xMLPackager.startElement("testXMLPackagerNs", null, "testXMLPackagerQName", atts);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.equals(Object)\" because \"name\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testStartElementThrowsNullPointerException1() throws Throwable {
        xMLPackager.startElement("testXMLPackagerNs", "testXMLPackagerName", "testXMLPackagerQName", null);
    }
}
