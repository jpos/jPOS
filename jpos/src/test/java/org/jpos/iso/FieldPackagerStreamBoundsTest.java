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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldPackagerStreamBoundsTest {
    private static final Prefixer NEGATIVE_PREFIXER = new NegativePrefixer();

    @Test
    void filledStringPackagerRejectsNegativeLength() {
        ISOFilledStringFieldPackager packager = new ISOFilledStringFieldPackager(
          99, "test", NullPadder.INSTANCE, LiteralInterpreter.INSTANCE, NEGATIVE_PREFIXER
        );

        assertInvalidLength(packager, new ISOField(2), new byte[1]);
    }

    @Test
    void amountPackagerRejectsNegativeLength() {
        ISOAmountFieldPackager packager = new ISOAmountFieldPackager(
          99, "test", NullPadder.INSTANCE, LiteralInterpreter.INSTANCE, NEGATIVE_PREFIXER
        );

        assertInvalidLength(packager, new ISOField(2), new byte[1]);
    }

    @Test
    void taggedStringPackagerRejectsNegativeLength() {
        ISOTagStringFieldPackager packager = new ISOTagStringFieldPackager(
          99, "test", AsciiPrefixer.L, NullPadder.INSTANCE, LiteralInterpreter.INSTANCE,
          NEGATIVE_PREFIXER
        );

        assertInvalidLength(packager, new ISOField(), taggedOverflowLength());
    }

    @Test
    void taggedBinaryPackagerRejectsNegativeLength() {
        ISOTagBinaryFieldPackager packager = new ISOTagBinaryFieldPackager(
          99, "test", AsciiPrefixer.L, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE,
          NEGATIVE_PREFIXER
        );

        assertInvalidLength(packager, new ISOBinaryField(), taggedOverflowLength());
    }

    private void assertInvalidLength(ISOFieldPackager packager, ISOComponent field, byte[] input) {
        ISOException e = assertThrows(ISOException.class,
          () -> packager.unpack(field, new ByteArrayInputStream(input)));
        assertTrue(hasMessage(e, "Invalid field length -2"), () -> "Unexpected exception chain: " + exceptionChain(e));
    }

    private byte[] taggedOverflowLength() {
        return new byte[] { '1', 0 };
    }

    private boolean hasMessage(Throwable t, String message) {
        while (t != null) {
            if (message.equals(t.getMessage()))
                return true;
            t = t instanceof ISOException ? ((ISOException) t).getNested() : t.getCause();
        }
        return false;
    }

    private String exceptionChain(Throwable t) {
        StringBuilder chain = new StringBuilder();
        while (t != null) {
            if (!chain.isEmpty())
                chain.append(" -> ");
            chain.append(t.getClass().getSimpleName()).append(": ").append(t.getMessage());
            t = t instanceof ISOException ? ((ISOException) t).getNested() : t.getCause();
        }
        return chain.toString();
    }

    private static class NegativePrefixer implements Prefixer {
        @Override
        public void encodeLength(int length, byte[] b) {
        }

        @Override
        public int decodeLength(byte[] b, int offset) {
            return -2;
        }

        @Override
        public int getPackedLength() {
            return 1;
        }
    }
}
