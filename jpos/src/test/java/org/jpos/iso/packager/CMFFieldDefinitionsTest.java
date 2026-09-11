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

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jpos.iso.IFB_LLLBINARY;
import org.jpos.iso.IFB_LLLLBINARY;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CMFFieldDefinitionsTest {
    @ParameterizedTest
    @ValueSource(strings = {"cmf.xml", "cmfv3.xml", "cmf-858.xml"})
    void topLevelFieldIdsAreUnique(String resource) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        try (InputStream input = resource(resource)) {
            Element root = factory.newDocumentBuilder().parse(input).getDocumentElement();
            Set<String> ids = new HashSet<>();
            for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child instanceof Element field && field.hasAttribute("id"))
                    assertTrue(ids.add(field.getAttribute("id")),
                        resource + ": duplicate field " + field.getAttribute("id"));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"cmf.xml", "cmfv3.xml", "cmf-858.xml"})
    void reservedAndKeyManagementFieldsRoundTripAtTheirLimits(String resource) throws Exception {
        GenericPackager packager;
        try (InputStream input = resource(resource)) {
            packager = new GenericPackager(input);
        }
        ISOFieldPackager reserved = packager.getFieldPackager(91);
        ISOFieldPackager keys = packager.getFieldPackager(96);
        assertInstanceOf(IFB_LLLLBINARY.class, reserved);
        assertEquals(9999, reserved.getLength());
        assertInstanceOf(IFB_LLLBINARY.class, keys);
        assertEquals(999, keys.getLength());

        byte[] reservedData = new byte[9999];
        byte[] keyData = new byte[999];
        Arrays.fill(reservedData, (byte) 0xA5);
        Arrays.fill(keyData, (byte) 0x5A);
        assertArrayEquals(new byte[] {(byte) 0x99, (byte) 0x99},
            Arrays.copyOf(reserved.pack(new ISOBinaryField(91, reservedData)), 2));
        assertArrayEquals(new byte[] {0x09, (byte) 0x99},
            Arrays.copyOf(keys.pack(new ISOBinaryField(96, keyData)), 2));

        ISOMsg message = new ISOMsg();
        message.setPackager(packager);
        message.setMTI("1200");
        message.set(91, reservedData);
        message.set(96, keyData);
        byte[] packed = message.pack();
        ISOMsg unpacked = new ISOMsg();
        unpacked.setPackager(packager);
        assertEquals(packed.length, unpacked.unpack(packed));
        assertArrayEquals(reservedData, unpacked.getBytes(91));
        assertArrayEquals(keyData, unpacked.getBytes(96));
    }

    private InputStream resource(String name) {
        InputStream input = getClass().getResourceAsStream("/packager/" + name);
        assertNotNull(input, name);
        return input;
    }
}
