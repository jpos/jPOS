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

import org.jpos.iso.packager.Base1_BITMAP126;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpecializedFixedFieldPackagerSecurityTest {
    @Test
    public void testAmountChecksAvailableDataAtNonzeroOffset() throws Exception {
        IFB_AMOUNT packager = new IFB_AMOUNT(6, "amount", true);
        ISOField field = new ISOField(4);
        byte[] data = new byte[] { 0x55, 'D', 0x00, 0x01, 0x23, 0x66 };

        assertEquals(4, packager.unpack(field, data, 1));
        assertEquals("D00123", field.getValue());
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(4), new byte[] { 'D', 0x00, 0x01 }, 0));
    }

    @Test
    public void testAmountRejectsMalformedBCDAndPadding() {
        IFB_AMOUNT packager = new IFB_AMOUNT(6, "amount", true);

        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(4), new byte[] { 'D', 0x00, 0x0A, 0x23 }, 0));
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOField(4), new byte[] { 'D', 0x10, 0x01, 0x23 }, 0));
    }

    @Test
    public void testX92BitmapChecksAndConsumesExactlyOneBitmap() throws Exception {
        X92_BITMAP packager = new X92_BITMAP(16, "bitmap");
        ISOBitMap field = new ISOBitMap(0);
        byte[] data = "xx0000000000000001yy".getBytes(StandardCharsets.US_ASCII);

        assertEquals(16, packager.unpack(field, data, 2));
        assertTrue(((BitSet) field.getValue()).get(64));
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBitMap(0), "000000000000000".getBytes(StandardCharsets.US_ASCII), 0));
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBitMap(0), "000000000000000G".getBytes(StandardCharsets.US_ASCII), 0));
    }

    @Test
    public void testX92BitmapStreamRequiresCompleteBitmap() throws Exception {
        X92_BITMAP packager = new X92_BITMAP(16, "bitmap");
        ISOBitMap field = new ISOBitMap(0);
        byte[] data = "0000000000000001z".getBytes(StandardCharsets.US_ASCII);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        packager.unpack(field, in);
        assertEquals(1, in.available());
        assertThrows(EOFException.class, () -> packager.unpack(new ISOBitMap(0),
          new ByteArrayInputStream(new byte[15])));
    }

    @Test
    public void testBase1BitmapChecksAndConsumesExactlyOneBitmap() throws Exception {
        Base1_BITMAP126 packager = new Base1_BITMAP126(16, "bitmap");
        ISOBitMap field = new ISOBitMap(0);
        byte[] data = new byte[11];
        data[2] = 0x40;

        assertEquals(8, packager.unpack(field, data, 2));
        assertTrue(((BitSet) field.getValue()).get(2));
        assertThrows(ISOException.class,
          () -> packager.unpack(new ISOBitMap(0), new byte[7], 0));
    }

    @Test
    public void testBase1BitmapStreamRequiresCompleteBitmap() throws Exception {
        Base1_BITMAP126 packager = new Base1_BITMAP126(16, "bitmap");
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[9]);

        packager.unpack(new ISOBitMap(0), in);
        assertEquals(1, in.available());
        assertThrows(EOFException.class, () -> packager.unpack(new ISOBitMap(0),
          new ByteArrayInputStream(new byte[7])));
    }
}
