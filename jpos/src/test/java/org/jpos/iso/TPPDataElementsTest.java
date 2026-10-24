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

package org.jpos.iso;

import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TPPDataElementsTest {
    private byte[] TPPDE =
        ISOUtil.hex2byte(
            ISOUtil.hexString(("tpp-card-token                          ").getBytes()) // Card Token
            + "00000000000000022220" // Transaction ID
            + "00000000000000011110" // Transaction GroupID
        );

    @Test
    public void testTPPDataElements() throws ISOException {
        ISOPackager p = new GenericPackager("jar:packager/cmf.xml");
        ISOMsg m = new ISOMsg("2100");
        m.set("113.69", "tpp-card-token");
        m.set("113.70", "2222");
        m.set("113.71", "1111");
        m.setPackager(p);

        assertEquals(ISOUtil.hexString(TPPDE), ISOUtil.hexString(m.pack()).substring(72));

        assertEquals(m.getString("113.69"), "tpp-card-token");
        assertEquals(m.getString("113.70"), "2222");
        assertEquals(m.getString("113.71"), "1111");
    }

    @Test
    public void testExtendedAuthorizationIndicator() throws ISOException {
        assertExtendedAuthorizationIndicator("jar:packager/cmf.xml");
        assertExtendedAuthorizationIndicator("jar:packager/cmfv3.xml");
    }

    private void assertExtendedAuthorizationIndicator(String packagerResource) throws ISOException {
        ISOPackager p = new GenericPackager(packagerResource);
        ISOMsg m = new ISOMsg("2100");
        m.setPackager(p);
        m.set("113.34", "Y");

        byte[] packed = m.pack();

        ISOMsg unpacked = new ISOMsg();
        unpacked.setPackager(p);
        unpacked.unpack(packed);

        assertEquals("Y", unpacked.getString("113.34"));
    }

    @Test
    public void testCryptoCardIndicator() throws ISOException {
        assertPackUnpackPreservesFieldValue("jar:packager/cmf.xml", "113.72", "C");
        assertPackUnpackPreservesFieldValue("jar:packager/cmfv3.xml", "113.72", "C");
    }

    @Test
    public void testAgeVerificationData() throws ISOException {
        String value = "00100321D00200318D"; // two TLV records: tag 001, len 003, value 21D; tag 002, len 003, value 18D
        assertPackUnpackPreservesFieldValue("jar:packager/cmf.xml", "113.73", value);
        assertPackUnpackPreservesFieldValue("jar:packager/cmfv3.xml", "113.73", value);
    }

    @Test
    public void testW3CTraceContextWireFormatAndRoundTrip() throws ISOException {
        GenericPackager packager = new GenericPackager("jar:packager/cmfv3.xml");
        ISOMsgFieldPackager privateFields = (ISOMsgFieldPackager) packager.getFieldPackager(113);
        ISOFieldPackager traceContext = ((ISOBasePackager) privateFields.getISOMsgPackager()).getFieldPackager(78);
        assertInstanceOf(IFB_LLLCHAR.class, traceContext);
        assertEquals(255, traceContext.getLength());

        String traceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";
        for (String value : new String[] {traceparent, "a".repeat(255)}) {
            byte[] encoded = traceContext.pack(new ISOField(78, value));
            byte[] prefix = value.length() == 55
                ? new byte[] {0x00, 0x55} : new byte[] {0x02, 0x55};
            assertArrayEquals(prefix, java.util.Arrays.copyOf(encoded, 2));
            assertArrayEquals(value.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                java.util.Arrays.copyOfRange(encoded, 2, encoded.length));
            assertPackUnpackPreservesFieldValue("jar:packager/cmfv3.xml", "113.78", value);
        }
        assertThrows(ISOException.class,
            () -> traceContext.pack(new ISOField(78, "a".repeat(256))));
    }

    private void assertPackUnpackPreservesFieldValue(String packagerResource, String fieldPath, String value) throws ISOException {
        ISOPackager p = new GenericPackager(packagerResource);
        ISOMsg m = new ISOMsg("2100");
        m.setPackager(p);
        m.set(fieldPath, value);

        byte[] packed = m.pack();

        ISOMsg unpacked = new ISOMsg();
        unpacked.setPackager(p);
        unpacked.unpack(packed);

        assertEquals(value, unpacked.getString(fieldPath));
    }
}
