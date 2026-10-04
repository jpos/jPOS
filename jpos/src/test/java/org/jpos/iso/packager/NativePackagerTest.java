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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NativePackagerTest {
    ISOMsg m;
    NativePackager p;
    static final byte[] PACKED = ISOUtil.hex2byte("ACED0005778100FFFF460000000430383030460003000630303030303046000B000630303030303146002900083239313130303031420037000455AA11224D00007F460002000A54657374203132372E32460003000A54657374203132372E33460004000A54657374203132372E344D000005460001000C54657374203132372E352E31454545");

    @BeforeEach
    public void setup() throws ISOException, NoSuchFieldException {
        p = new NativePackager();
        m = new ISOMsg();
        m.set(0, "0800");
        m.set(3, "000000");
        m.set(11, "000001");
        m.set(41, "29110001");
        m.set(55, ISOUtil.hex2byte("55AA1122"));
        m.set("127.2", "Test 127.2");
        m.set("127.3", "Test 127.3");
        m.set("127.4", "Test 127.4");
        m.set("127.5.1", "Test 127.5.1");
        m.setPackager(p);
    }

    @Test
    public void testPack() throws ISOException {
        assertArrayEquals(PACKED, m.pack(), "Expected image");
    }

    @Test
    public void testUnpack() throws ISOException {
        ISOMsg m1 = new ISOMsg();
        m1.setPackager(p);
        m1.unpack(PACKED);
        assertEquals("0800", m1.getMTI());
        assertEquals("000000", m1.getString(3));
        assertEquals("000001", m1.getString(11));
        assertEquals("29110001", m1.getString(41));
        assertArrayEquals(ISOUtil.hex2byte("55AA1122"), m1.getBytes(55));
        assertEquals("Test 127.2", m1.getString("127.2"));
        assertEquals("Test 127.3", m1.getString("127.3"));
        assertEquals("Test 127.4", m1.getString("127.4"));
        assertEquals("Test 127.5.1", m1.getString("127.5.1"));
    }

    @Test
    public void testExactByteLimit() throws Exception {
        p.setConfiguration(configuration("deserialization-max-bytes", PACKED.length));

        ISOMsg result = unpack(PACKED);

        assertEquals("0800", result.getMTI());
    }

    @Test
    public void testByteLimitRejectsOneByteOver() throws Exception {
        p.setConfiguration(configuration("deserialization-max-bytes", PACKED.length - 1L));

        assertThrows(ISOException.class, () -> unpack(PACKED));
    }

    @Test
    public void testOversizedByteArrayIsRejectedBeforeStreamParsing() throws Exception {
        p.setConfiguration(configuration("deserialization-max-bytes", 4));

        ISOException exception = assertThrows(
            ISOException.class,
            () -> p.unpack(new ISOMsg(), new byte[5])
        );

        assertTrue(exception.getMessage().contains("maximum stream bytes"));
    }

    @Test
    public void testExactInputStreamLimitPreservesFollowingFrame() throws Exception {
        p.setConfiguration(configuration("deserialization-max-bytes", PACKED.length));
        byte[] frames = new byte[PACKED.length * 2];
        System.arraycopy(PACKED, 0, frames, 0, PACKED.length);
        System.arraycopy(PACKED, 0, frames, PACKED.length, PACKED.length);
        ByteArrayInputStream in = new ByteArrayInputStream(frames);

        ISOMsg first = new ISOMsg();
        p.unpack(first, in);
        assertEquals(PACKED.length, in.available());

        ISOMsg second = new ISOMsg();
        p.unpack(second, in);
        assertEquals(0, in.available());
        assertEquals("0800", first.getMTI());
        assertEquals("0800", second.getMTI());
    }

    @Test
    public void testConfiguredDepthLimit() throws Exception {
        byte[] packed = packObject(new Object[] { new Object[0] });
        p.setConfiguration(configuration("deserialization-max-depth", 1));

        assertThrows(ISOException.class, () -> p.unpack(new ObjectPayloadISOMsg(), packed));
    }

    @Test
    public void testConfiguredReferenceLimit() throws Exception {
        byte[] packed = packObject(new Object[] { new SerializableValue() });
        p.setConfiguration(configuration("deserialization-max-references", 1));

        assertThrows(ISOException.class, () -> p.unpack(new ObjectPayloadISOMsg(), packed));
    }

    @Test
    public void testConfiguredArrayLengthLimit() throws Exception {
        byte[] packed = packObject(new int[2]);
        p.setConfiguration(configuration("deserialization-max-array-length", 1));

        assertThrows(ISOException.class, () -> p.unpack(new ObjectPayloadISOMsg(), packed));
    }

    @Test
    public void testConfiguredLimitsMustBePositive() {
        String[] properties = {
            "deserialization-max-depth",
            "deserialization-max-references",
            "deserialization-max-array-length",
            "deserialization-max-bytes"
        };
        for (String property : properties) {
            assertThrows(ConfigurationException.class, () -> p.setConfiguration(configuration(property, 0)));
            assertThrows(ConfigurationException.class, () -> p.setConfiguration(configuration(property, -1)));
        }
    }

    @Test
    public void testConfiguredLimitsMustBeNumeric() {
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("deserialization-max-depth", "invalid");

        assertThrows(ConfigurationException.class, () -> p.setConfiguration(cfg));
    }

    @Test
    public void testPackagerMetadataIsRejectedByDefault() throws Exception {
        byte[] packed = packWithPackagerMetadata();

        assertThrows(ISOException.class, () -> unpack(packed));
    }

    @Test
    public void testPackagerMetadataCompatibilityOptIn() throws Exception {
        byte[] packed = packWithPackagerMetadata();
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("allow-packager-metadata", "true");
        p.setConfiguration(cfg);

        ISOMsg result = unpack(packed);

        assertEquals(ISO87APackager.class, result.getPackager().getClass());
        assertEquals("0800", result.getMTI());
    }

    private ISOMsg unpack(byte[] image) throws ISOException {
        ISOMsg result = new ISOMsg();
        result.setPackager(p);
        result.unpack(image);
        return result;
    }

    private byte[] packObject(Object value) throws ISOException {
        ObjectPayloadISOMsg message = new ObjectPayloadISOMsg(value);
        message.setPackager(p);
        return message.pack();
    }

    private byte[] packWithPackagerMetadata() throws Exception {
        ISOMsg message = new ISOMsg("0800");
        message.setPackager(new ISO87APackager());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            message.writeExternal(out);
        }
        return bytes.toByteArray();
    }

    private SimpleConfiguration configuration(String name, long value) {
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put(name, Long.toString(value));
        return cfg;
    }

    private static class ObjectPayloadISOMsg extends ISOMsg {
        private Object value;

        private ObjectPayloadISOMsg() { }

        private ObjectPayloadISOMsg(Object value) {
            this.value = value;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(value);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            value = in.readObject();
        }
    }

    private static class SerializableValue implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
