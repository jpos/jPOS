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
import java.io.EOFException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BitmapInputValidationTest {
    private static final byte[] ASCII_PRIMARY = "4000000000000000".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] ASCII_EXTENDED =
        "80000000000000004000000000000000".getBytes(StandardCharsets.US_ASCII);

    @Test
    public void testAsciiBitmapHonorsOffsetAndPrimaryBoundary() throws Exception {
        IFA_BITMAP packager = new IFA_BITMAP(16, "Bitmap");
        byte[] image = ISOUtil.concat(new byte[] { 0x00 }, ISOUtil.concat(ASCII_PRIMARY, new byte[] { 0x00 }));
        ISOBitMap bitmap = new ISOBitMap(1);

        assertEquals(16, packager.unpack(bitmap, image, 1));
        assertTrue(((java.util.BitSet) bitmap.getValue()).get(2));
        assertFalse(((java.util.BitSet) bitmap.getValue()).get(1));
    }

    @Test
    public void testAsciiBitmapRequiresSelectedSecondary() {
        IFA_BITMAP packager = new IFA_BITMAP(16, "Bitmap");
        ISOBitMap bitmap = new ISOBitMap(1);
        byte[] truncated = "8000000000000000400000000000000".getBytes(StandardCharsets.US_ASCII);

        assertThrows(ISOException.class,
            () -> packager.unpack(bitmap, truncated, 0));
        assertThrows(EOFException.class,
            () -> packager.unpack(bitmap, new ByteArrayInputStream(truncated)));
    }

    @Test
    public void testAsciiBitmapAcceptsExactSecondaryBoundary() throws Exception {
        IFA_BITMAP packager = new IFA_BITMAP(16, "Bitmap");
        ISOBitMap bitmap = new ISOBitMap(1);

        assertEquals(32, packager.unpack(bitmap, ASCII_EXTENDED, 0));
        assertTrue(((java.util.BitSet) bitmap.getValue()).get(1));
        assertTrue(((java.util.BitSet) bitmap.getValue()).get(66));
    }

    @Test
    public void testAsciiBitmapRejectsMalformedHex() {
        IFA_BITMAP packager = new IFA_BITMAP(8, "Bitmap");
        byte[] malformed = "400000000000000G".getBytes(StandardCharsets.US_ASCII);
        byte[] image = ISOUtil.concat(new byte[] { 0x00 }, malformed);

        assertThrows(ISOException.class,
            () -> packager.unpack(new ISOBitMap(1), image, 1));
        assertThrows(ISOException.class,
            () -> packager.unpack(new ISOBitMap(1), new ByteArrayInputStream(malformed)));
    }

    @Test
    public void testBinaryBitmapRequiresSelectedSecondary() {
        IFB_BITMAP packager = new IFB_BITMAP(16, "Bitmap");
        byte[] truncated = ISOUtil.hex2byte("800000000000000040000000000000");

        assertThrows(ISOException.class,
            () -> packager.unpack(new ISOBitMap(1), truncated, 0));
        assertThrows(EOFException.class,
            () -> packager.unpack(new ISOBitMap(1), new ByteArrayInputStream(truncated)));
    }

    @Test
    public void testBinaryBitmapHonorsOffsetAndExactSecondaryBoundary() throws Exception {
        IFB_BITMAP packager = new IFB_BITMAP(16, "Bitmap");
        byte[] encoded = ISOUtil.hex2byte("80000000000000004000000000000000");
        byte[] image = ISOUtil.concat(new byte[] { 0x00 }, ISOUtil.concat(encoded, new byte[] { 0x00 }));
        ISOBitMap bitmap = new ISOBitMap(1);

        assertEquals(16, packager.unpack(bitmap, image, 1));
        assertTrue(((java.util.BitSet) bitmap.getValue()).get(1));
        assertTrue(((java.util.BitSet) bitmap.getValue()).get(66));
    }

    @Test
    public void testEbcdicBitmapRequiresSelectedSecondary() {
        IFE_BITMAP packager = new IFE_BITMAP(16, "Bitmap");
        byte[] truncated = ISOUtil.asciiToEbcdic(
            "8000000000000000400000000000000".getBytes(StandardCharsets.US_ASCII));

        assertThrows(ISOException.class,
            () -> packager.unpack(new ISOBitMap(1), truncated, 0));
        assertThrows(EOFException.class,
            () -> packager.unpack(new ISOBitMap(1), new ByteArrayInputStream(truncated)));
    }

    @Test
    public void testEbcdicBitmapHonorsOffsetAndExactSecondaryBoundary() throws Exception {
        IFE_BITMAP packager = new IFE_BITMAP(16, "Bitmap");
        byte[] encoded = ISOUtil.asciiToEbcdic(ASCII_EXTENDED);
        byte[] image = ISOUtil.concat(new byte[] { 0x00 }, ISOUtil.concat(encoded, new byte[] { 0x00 }));
        ISOBitMap bitmap = new ISOBitMap(1);

        assertEquals(32, packager.unpack(bitmap, image, 1));
        assertTrue(((java.util.BitSet) bitmap.getValue()).get(1));
        assertTrue(((java.util.BitSet) bitmap.getValue()).get(66));
    }

    @Test
    public void testEbcdicBitmapRejectsMalformedHex() {
        IFE_BITMAP packager = new IFE_BITMAP(8, "Bitmap");
        byte[] malformed = ISOUtil.asciiToEbcdic(
            "400000000000000G".getBytes(StandardCharsets.US_ASCII));
        byte[] image = ISOUtil.concat(new byte[] { 0x00 }, malformed);

        assertThrows(ISOException.class,
            () -> packager.unpack(new ISOBitMap(1), image, 1));
        assertThrows(ISOException.class,
            () -> packager.unpack(new ISOBitMap(1), new ByteArrayInputStream(malformed)));
    }

    @Test
    public void testTertiaryBitmapStreamBoundaries() throws Exception {
        byte[] ascii = (
            "8000000000000000" +
            "8000000000000000" +
            "4000000000000000").getBytes(StandardCharsets.US_ASCII);
        byte[] binary = ISOUtil.hex2byte(new String(ascii, StandardCharsets.US_ASCII));
        byte[] ebcdic = ISOUtil.asciiToEbcdic(ascii);

        ByteArrayInputStream asciiStream = withTrailingByte(ascii);
        ISOBitMap asciiBitmap = new ISOBitMap(1);
        new IFA_BITMAP(24, "Bitmap").unpack(asciiBitmap, asciiStream);
        assertEquals(1, asciiStream.available());
        assertTrue(((java.util.BitSet) asciiBitmap.getValue()).get(130));

        ByteArrayInputStream binaryStream = withTrailingByte(binary);
        ISOBitMap binaryBitmap = new ISOBitMap(1);
        new IFB_BITMAP(24, "Bitmap").unpack(binaryBitmap, binaryStream);
        assertEquals(1, binaryStream.available());
        assertTrue(((java.util.BitSet) binaryBitmap.getValue()).get(130));

        ByteArrayInputStream ebcdicStream = withTrailingByte(ebcdic);
        ISOBitMap ebcdicBitmap = new ISOBitMap(1);
        new IFE_BITMAP(24, "Bitmap").unpack(ebcdicBitmap, ebcdicStream);
        assertEquals(1, ebcdicStream.available());
        assertTrue(((java.util.BitSet) ebcdicBitmap.getValue()).get(130));
    }

    private static ByteArrayInputStream withTrailingByte(byte[] encoded) {
        return new ByteArrayInputStream(ISOUtil.concat(encoded, new byte[] { 0x00 }));
    }
}
