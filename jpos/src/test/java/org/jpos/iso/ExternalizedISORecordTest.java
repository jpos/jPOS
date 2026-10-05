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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExternalizedISORecordTest {
    @Test
    public void testReadHeaderRejectsNegativeLengthWithoutChangingHeader() throws Exception {
        ISOMsg msg = new ISOMsg();
        byte[] original = { 0x01, 0x02 };
        msg.setHeader(original);

        try (ObjectInputStream in = input(out -> out.writeShort(-1))) {
            assertThrows(InvalidObjectException.class, () -> msg.readHeader(in));
        }
        assertArrayEquals(original, msg.getHeader());
    }

    @Test
    public void testReadHeaderRejectsPrematureEofWithoutChangingHeader() throws Exception {
        ISOMsg msg = new ISOMsg();
        byte[] original = { 0x01, 0x02 };
        msg.setHeader(original);

        try (ObjectInputStream in = input(out -> {
            out.writeShort(2);
            out.writeByte(0x03);
        })) {
            assertThrows(EOFException.class, () -> msg.readHeader(in));
        }
        assertArrayEquals(original, msg.getHeader());
    }

    @Test
    public void testReadHeaderAcceptsEmptyHeader() throws Exception {
        ISOMsg msg = new ISOMsg();
        msg.setHeader(new byte[] { 0x01 });

        try (ObjectInputStream in = input(out -> out.writeShort(0))) {
            msg.readHeader(in);
        }
        assertArrayEquals(new byte[0], msg.getHeader());
    }

    @Test
    public void testBinaryFieldRejectsNegativeLengthWithoutChangingState() throws Exception {
        byte[] original = { 0x01, 0x02 };
        ISOBinaryField field = new ISOBinaryField(7, original);

        try (ObjectInputStream in = input(out -> {
            out.writeShort(8);
            out.writeShort(-1);
        })) {
            assertThrows(InvalidObjectException.class, () -> field.readExternal(in));
        }
        assertEquals(7, field.getFieldNumber());
        assertArrayEquals(original, field.getBytes());
    }

    @Test
    public void testBinaryFieldRejectsPrematureEofWithoutChangingState() throws Exception {
        byte[] original = { 0x01, 0x02 };
        ISOBinaryField field = new ISOBinaryField(7, original);

        try (ObjectInputStream in = input(out -> {
            out.writeShort(8);
            out.writeShort(2);
            out.writeByte(0x03);
        })) {
            assertThrows(EOFException.class, () -> field.readExternal(in));
        }
        assertEquals(7, field.getFieldNumber());
        assertArrayEquals(original, field.getBytes());
    }

    @Test
    public void testBinaryFieldReadsCompleteState() throws Exception {
        ISOBinaryField field = new ISOBinaryField(7, new byte[] { 0x01 });

        try (ObjectInputStream in = input(out -> {
            out.writeShort(42);
            out.writeShort(3);
            out.write(new byte[] { 0x02, 0x03, 0x04 });
        })) {
            field.readExternal(in);
        }
        assertEquals(42, field.getFieldNumber());
        assertArrayEquals(new byte[] { 0x02, 0x03, 0x04 }, field.getBytes());
    }

    private static ObjectInputStream input(OutputWriter writer) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            writer.write(out);
        }
        return new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
    }

    @FunctionalInterface
    private interface OutputWriter {
        void write(ObjectOutputStream out) throws IOException;
    }
}
