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

package org.jpos.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SerializerTest {
    private Calendar cal;

    @BeforeEach
    public void setUp() throws Exception {
        cal = new GregorianCalendar(2012, Calendar.MAY, 31, 23, 47, 58);
        cal.set(Calendar.MILLISECOND, 56);
    }

    @Test
    public void testSSerializeAndDeserialize() throws Exception {
        byte[] pickled = Serializer.serialize(cal);
        assertNotNull(pickled);
        Calendar reconstituted = (Calendar) Serializer.deserialize(pickled);
        assertNotNull(reconstituted);
        assertEquals(cal, reconstituted);
        assertNotSame(cal, reconstituted);
    }

    @Test
    public void testStringMapSerializer() throws Exception {
        Map<String,String> smap = new HashMap<>();
        smap.put ("A", "ABC");
        smap.put ("B", "CBA");
        byte[] b = Serializer.serializeStringMap(smap);
        Map<String,String> smap2 = Serializer.deserializeStringMap(b);
        assertEquals(smap, smap2);
    }

    @Test
    public void testDepthLimitAtBoundary() throws Exception {
        byte[] atLimit = Serializer.serialize(new Link(new Link(null)));
        byte[] overLimit = Serializer.serialize(new Link(new Link(new Link(null))));
        Serializer.DeserializationLimits limits = limits(2, 20, 10, Math.max(atLimit.length, overLimit.length));

        assertNotNull(Serializer.deserializeWithLimits(atLimit, limits));
        assertThrows(IOException.class, () -> Serializer.deserializeWithLimits(overLimit, limits));
    }

    @Test
    public void testReferenceLimitAtBoundary() throws Exception {
        byte[] atLimit = Serializer.serialize(new AllowedValue[] { new AllowedValue() });
        byte[] overLimit = Serializer.serialize(new AllowedValue[] { new AllowedValue(), new AllowedValue() });
        Serializer.DeserializationLimits limits = limits(10, 3, 10, Math.max(atLimit.length, overLimit.length));

        assertNotNull(Serializer.deserializeWithLimits(atLimit, limits));
        assertThrows(IOException.class, () -> Serializer.deserializeWithLimits(overLimit, limits));
    }

    @Test
    public void testArrayLengthLimitAtBoundary() throws Exception {
        byte[] atLimit = Serializer.serialize(new int[3]);
        byte[] overLimit = Serializer.serialize(new int[4]);
        Serializer.DeserializationLimits limits = limits(10, 20, 3, Math.max(atLimit.length, overLimit.length));

        assertNotNull(Serializer.deserializeWithLimits(atLimit, limits));
        assertThrows(IOException.class, () -> Serializer.deserializeWithLimits(overLimit, limits));
    }

    @Test
    public void testByteArrayLengthIsCheckedBeforeDeserialization() throws Exception {
        byte[] serialized = Serializer.serialize("bounded");

        assertEquals("bounded", Serializer.deserializeWithLimits(serialized, limits(10, 20, 10, serialized.length)));
        assertThrows(
          IOException.class,
          () -> Serializer.deserializeWithLimits(serialized, limits(10, 20, 10, serialized.length - 1))
        );
    }

    @Test
    public void testInputStreamByteLimitIncludesStreamHeader() throws Exception {
        byte[] serialized = Serializer.serialize("bounded");
        Serializer.DeserializationLimits exact = limits(10, 20, 10, serialized.length);
        Serializer.DeserializationLimits shortByOne = limits(10, 20, 10, serialized.length - 1);

        try (ObjectInputStream in = Serializer.createSafeObjectInputStream(new ByteArrayInputStream(serialized), exact)) {
            assertEquals("bounded", in.readObject());
        }
        CountingInputStream counted = new CountingInputStream(serialized);
        assertThrows(IOException.class, () -> {
            try (ObjectInputStream in = Serializer.createSafeObjectInputStream(
              counted, shortByOne
            )) {
                in.readObject();
            }
        });
        assertEquals(serialized.length - 1, counted.count);
    }

    @Test
    public void testAllowListChecksArrayComponentType() throws Exception {
        byte[] serialized = Serializer.serialize(new AllowedValue[] { new AllowedValue() });
        Serializer.DeserializationLimits limits = limits(10, 20, 10, serialized.length);

        assertThrows(IOException.class, () -> {
            try (ObjectInputStream in = Serializer.createLimitedAllowListObjectInputStream(
              new ByteArrayInputStream(serialized), limits
            )) {
                in.readObject();
            }
        });
        try (ObjectInputStream in = Serializer.createLimitedAllowListObjectInputStream(
          new ByteArrayInputStream(serialized), limits, AllowedValue.class.getName()
        )) {
            assertEquals(1, ((AllowedValue[]) in.readObject()).length);
        }
    }

    @Test
    public void testSafeFilterChecksRejectedArrayComponentType() throws Exception {
        byte[] serialized = Serializer.serialize(new javax.management.BadAttributeValueExpException[0]);

        assertThrows(IOException.class, () -> Serializer.deserialize(serialized));
    }

    @Test
    public void testTypedDeserializeChecksRequestedType() throws Exception {
        byte[] serialized = Serializer.serialize("value");

        assertEquals("value", Serializer.deserialize(serialized, String.class));
        assertThrows(ClassCastException.class, () -> Serializer.deserialize(serialized, Integer.class));
        assertThrows(NullPointerException.class, () -> Serializer.deserialize(serialized, null));
    }

    @Test
    public void testLimitsRejectNegativeValues() {
        assertThrows(
          IllegalArgumentException.class,
          () -> new Serializer.DeserializationLimits(-1, 1, 1, 1)
        );
    }

    private Serializer.DeserializationLimits limits(long depth, long references, long arrayLength, long bytes) {
        return new Serializer.DeserializationLimits(depth, references, arrayLength, bytes);
    }

    private static class Link implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Link next;

        private Link(Link next) {
            this.next = next;
        }
    }

    private static class AllowedValue implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    private static class CountingInputStream extends InputStream {
        private final ByteArrayInputStream delegate;
        private int count;

        private CountingInputStream(byte[] data) {
            delegate = new ByteArrayInputStream(data);
        }

        @Override
        public int read() {
            int value = delegate.read();
            if (value >= 0)
                count++;
            return value;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            int read = delegate.read(b, off, len);
            if (read > 0)
                count += read;
            return read;
        }
    }
}
