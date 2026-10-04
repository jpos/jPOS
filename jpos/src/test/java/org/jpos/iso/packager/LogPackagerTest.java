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

import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogPackagerTest {
    public static class ConfiguredXMLReader extends XMLFilterImpl {
        private static boolean created;

        public ConfiguredXMLReader() throws SAXException {
            super(XMLReaderFactory.createXMLReader());
            created = true;
        }
    }

    @Test
    public void testDefaultConstructor() {
        assertDoesNotThrow(LogPackager::new);
    }

    @Test
    public void testHonorsConfiguredSaxParser() throws Exception {
        String previousParser = System.getProperty("sax.parser");
        ConfiguredXMLReader.created = false;
        try {
            System.setProperty("sax.parser", ConfiguredXMLReader.class.getName());

            new LogPackager();

            assertTrue(ConfiguredXMLReader.created);
        } finally {
            if (previousParser == null)
                System.clearProperty("sax.parser");
            else
                System.setProperty("sax.parser", previousParser);
        }
    }

    @Test
    public void testRoundTrip() throws Exception {
        LogPackager packager = new LogPackager();
        ISOMsg source = new ISOMsg();
        source.setMTI("0800");
        source.set(11, "123456");
        source.set(new ISOBinaryField(52, ISOUtil.hex2byte("1122334455667788")));

        byte[] packed = packager.pack(source);
        ISOMsg result = new ISOMsg();
        assertEquals(packed.length, packager.unpack(result, packed));

        assertEquals("0800", result.getMTI());
        assertEquals("123456", result.getString(11));
        assertInstanceOf(ISOBinaryField.class, result.getComponent(52));
        assertEquals("1122334455667788", ISOUtil.hexString(result.getBytes(52)));
    }

    @Test
    public void testParsesLogMessage() throws Exception {
        String input = """
            <log realm="test" at="2026-10-04T12:00:00Z">
              <isomsg direction="incoming">
                <field id="0" value="0200"/>
                <field id="3" value="000000"/>
                <isomsg id="48">
                  <field id="1" value="nested"/>
                </isomsg>
              </isomsg>
            </log>
            """;
        LogPackager packager = new LogPackager();
        ISOMsg result = new ISOMsg();

        packager.unpack(result, input.getBytes(StandardCharsets.UTF_8));

        assertEquals("0200", result.getMTI());
        assertEquals("000000", result.getString(3));
        ISOMsg nested = assertInstanceOf(ISOMsg.class, result.getComponent(48));
        assertEquals("nested", nested.getString(1));
    }

    @Test
    public void testRejectsInternalDoctype() throws Exception {
        String input = """
            <!DOCTYPE log [<!ENTITY value "0800">]>
            <log><isomsg><field id="0" value="&value;"/></isomsg></log>
            """;

        ISOException e = assertThrows(ISOException.class, () -> unpack(input));

        assertTrue(e.getMessage().contains("DOCTYPE"));
    }

    @Test
    public void testRejectsExternalDoctypeWithoutAccessingIt() throws Exception {
        String input = """
            <!DOCTYPE log SYSTEM "urn:jpos:test:external-dtd">
            <log><isomsg><field id="0" value="0800"/></isomsg></log>
            """;

        ISOException e = assertThrows(ISOException.class, () -> unpack(input));

        assertTrue(e.getMessage().contains("DOCTYPE"));
    }

    @Test
    public void testAcceptsMaximumIsomsgNestingDepth() {
        assertDoesNotThrow(() -> unpack(nestedMessage(32)));
    }

    @Test
    public void testRejectsExcessiveIsomsgNestingDepth() {
        ISOException e = assertThrows(ISOException.class, () -> unpack(nestedMessage(33)));

        assertTrue(e.getMessage().contains("Maximum isomsg nesting depth exceeded"));
    }

    @Test
    public void testRejectsMalformedXml() {
        assertThrows(
            ISOException.class,
            () -> unpack("<log><isomsg><field id=\"0\" value=\"0800\"/></log>")
        );
    }

    @Test
    public void testRejectsRootlessField() {
        ISOException e = assertThrows(
            ISOException.class,
            () -> unpack("<log><field id=\"0\" value=\"0800\"/></log>")
        );

        assertTrue(e.getMessage().contains("field without isomsg"));
    }

    @Test
    public void testRejectsNonMessageTarget() throws Exception {
        LogPackager packager = new LogPackager();

        assertThrows(
            ISOException.class,
            () -> packager.unpack(new ISOField(), "<log/>".getBytes(StandardCharsets.UTF_8))
        );
    }

    @Test
    public void testReportsInputFailureAsISOException() throws Exception {
        LogPackager packager = new LogPackager();
        InputStream failingInput = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("test input failure");
            }
        };

        ISOException e = assertThrows(
            ISOException.class,
            () -> packager.unpack(new ISOMsg(), failingInput)
        );

        assertTrue(e.getMessage().contains("test input failure"));
    }

    private ISOMsg unpack(String input) throws Exception {
        LogPackager packager = new LogPackager();
        ISOMsg result = new ISOMsg();
        packager.unpack(result, input.getBytes(StandardCharsets.UTF_8));
        return result;
    }

    private String nestedMessage(int depth) {
        StringBuilder xml = new StringBuilder("<log><isomsg>");
        for (int i = 1; i < depth; i++)
            xml.append("<isomsg id=\"").append(i).append("\">");
        xml.append("</isomsg>".repeat(depth));
        return xml.append("</log>").toString();
    }
}
