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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldPackagerBoundsTest {
    @Test
    void stringPackagerRejectsTruncatedPrefix() {
        ISOStringFieldPackager packager = new ISOStringFieldPackager(
          99, "test", NullPadder.INSTANCE, LiteralInterpreter.INSTANCE, AsciiPrefixer.LL
        );

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(2), new byte[] { '0' }, 0));

        assertTrue(hasMessage(e, "Insufficient data for length prefix"));
    }

    @Test
    void stringPackagerRejectsTruncatedValue() {
        ISOStringFieldPackager packager = new ISOStringFieldPackager(
          99, "test", NullPadder.INSTANCE, LiteralInterpreter.INSTANCE, AsciiPrefixer.LL
        );

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(2), "05abc".getBytes(ISOUtil.CHARSET), 0));

        assertTrue(hasMessage(e, "Insufficient data for field value"));
    }

    @Test
    void binaryPackagerRejectsHugeLengthBeforeAllocation() {
        ISOBinaryFieldPackager packager = new ISOBinaryFieldPackager(
          Integer.MAX_VALUE, "test", LiteralBinaryInterpreter.INSTANCE, new BinaryPrefixer(4)
        );

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBinaryField(2),
            new byte[] { 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 0));

        assertTrue(hasMessage(e, "Insufficient data for field value"));
    }

    @Test
    void binaryPackagerRejectsLengthAboveIntegerMaximum() {
        ISOBinaryFieldPackager packager = new ISOBinaryFieldPackager(
          0, "test", LiteralBinaryInterpreter.INSTANCE, new BinaryPrefixer(4)
        );

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBinaryField(2), new byte[] { (byte) 0x80, 0, 0, 0 }, 0));

        assertTrue(hasMessage(e, "Length prefix exceeds Integer.MAX_VALUE"));
    }

    @Test
    void binaryPackagerDoesNotTreatOverflowedMinusOneAsMissingPrefix() {
        ISOBinaryFieldPackager packager = new ISOBinaryFieldPackager(
          8, "test", LiteralBinaryInterpreter.INSTANCE, new BinaryPrefixer(4)
        );

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBinaryField(2),
            new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 0));

        assertTrue(hasMessage(e, "Length prefix exceeds Integer.MAX_VALUE"));
    }

    @Test
    void basePackagersAcceptExactAvailableValue() throws ISOException {
        ISOStringFieldPackager stringPackager = new ISOStringFieldPackager(
          99, "test", NullPadder.INSTANCE, LiteralInterpreter.INSTANCE, AsciiPrefixer.LL
        );
        ISOField stringField = new ISOField(2);
        assertEquals(5, stringPackager.unpack(stringField, "03abc".getBytes(ISOUtil.CHARSET), 0));
        assertEquals("abc", stringField.getValue());

        ISOBinaryFieldPackager binaryPackager = new ISOBinaryFieldPackager(
          99, "test", LiteralBinaryInterpreter.INSTANCE, BinaryPrefixer.B
        );
        ISOBinaryField binaryField = new ISOBinaryField(2);
        assertEquals(4, binaryPackager.unpack(binaryField, new byte[] { 3, 1, 2, 3 }, 0));
        TestUtils.assertEquals(new byte[] { 1, 2, 3 }, binaryField.getBytes());
    }

    @Test
    void formattableStringPackagerRejectsTruncatedHeader() {
        ISOFormattableStringFieldPackager packager = new ISOFormattableStringFieldPackager(
          99, "test", AsciiPrefixer.LL, NullPadder.INSTANCE, LiteralInterpreter.INSTANCE,
          AsciiPrefixer.LL, IsoFieldHeaderFormatter.TAG_FIRST
        );

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), "120".getBytes(ISOUtil.CHARSET), 0));

        assertTrue(hasMessage(e, "Insufficient data for field header"));
    }

    @Test
    void formattableBinaryPackagerRejectsTruncatedValue() {
        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(
          99, "test", AsciiPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE,
          AsciiPrefixer.LL, IsoFieldHeaderFormatter.TAG_FIRST
        );

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBinaryField(), "1203ab".getBytes(ISOUtil.CHARSET), 0));

        assertTrue(hasMessage(e, "Insufficient data for field value"));
    }

    @Test
    void lengthFirstPackagerRejectsLengthShorterThanTag() {
        ISOFormattableStringFieldPackager packager = new ISOFormattableStringFieldPackager(
          99, "test", AsciiPrefixer.LL, NullPadder.INSTANCE, LiteralInterpreter.INSTANCE,
          AsciiPrefixer.LL, IsoFieldHeaderFormatter.LENGTH_FIRST
        );

        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(), "0112".getBytes(ISOUtil.CHARSET), 0));

        assertTrue(hasMessage(e, "Invalid self-inclusive field length 1"));
    }

    @Test
    void streamPackagersRejectLengthBeforeReadingValue() {
        ISOStringFieldPackager stringPackager = new ISOStringFieldPackager(
          3, "test", NullPadder.INSTANCE, LiteralInterpreter.INSTANCE, AsciiPrefixer.LL
        );
        ISOException stringError = assertThrows(ISOException.class,
          () -> stringPackager.unpack(new ISOField(2),
            new ByteArrayInputStream("99".getBytes(ISOUtil.CHARSET))));
        assertTrue(hasMessage(stringError, "Field length 99 too long. Max: 3"));

        ISOBinaryFieldPackager binaryPackager = new ISOBinaryFieldPackager(
          3, "test", LiteralBinaryInterpreter.INSTANCE, new BinaryPrefixer(4)
        );
        ISOException binaryError = assertThrows(ISOException.class,
          () -> binaryPackager.unpack(new ISOBinaryField(2),
            new ByteArrayInputStream(new byte[] { 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF })));
        assertTrue(hasMessage(binaryError, "Field length 2147483647 too long. Max: 3"));
    }

    @Test
    void formattableStreamPackagersRejectLengthBeforeReadingValue() {
        ISOFormattableStringFieldPackager stringPackager = new ISOFormattableStringFieldPackager(
          3, "test", AsciiPrefixer.LL, NullPadder.INSTANCE, LiteralInterpreter.INSTANCE,
          AsciiPrefixer.LL, IsoFieldHeaderFormatter.TAG_FIRST
        );
        ISOException stringError = assertThrows(ISOException.class,
          () -> stringPackager.unpack(new ISOField(),
            new ByteArrayInputStream("1299".getBytes(ISOUtil.CHARSET))));
        assertTrue(hasMessage(stringError, "Field length 99 too long. Max: 3"));

        ISOFormattableBinaryFieldPackager binaryPackager = new ISOFormattableBinaryFieldPackager(
          3, "test", AsciiPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE,
          AsciiPrefixer.LL, IsoFieldHeaderFormatter.LENGTH_FIRST
        );
        ISOException binaryError = assertThrows(ISOException.class,
          () -> binaryPackager.unpack(new ISOBinaryField(),
            new ByteArrayInputStream("9912".getBytes(ISOUtil.CHARSET))));
        assertTrue(hasMessage(binaryError, "Field length 97 too long. Max: 3"));
    }

    @Test
    void formattableStreamPackagerSupportsFixedLengthWithoutReadingAPrefix() throws Exception {
        ISOFormattableStringFieldPackager packager = new ISOFormattableStringFieldPackager(
          3, "test", AsciiPrefixer.LL, NullPadder.INSTANCE, LiteralInterpreter.INSTANCE,
          NullPrefixer.INSTANCE, IsoFieldHeaderFormatter.TAG_FIRST
        );
        ISOField field = new ISOField();

        packager.unpack(field, new ByteArrayInputStream("12abc".getBytes(ISOUtil.CHARSET)));

        assertEquals(12, field.getKey());
        assertEquals("abc", field.getValue());
    }

    private boolean hasMessage(Throwable t, String message) {
        while (t != null) {
            if (message.equals(t.getMessage()))
                return true;
            t = t instanceof ISOException ? ((ISOException) t).getNested() : t.getCause();
        }
        return false;
    }
}
