package org.jpos.iso;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ISOFormattableBinaryFieldPackagerTest {

    @Test
    public void packWithTagFirst() throws ISOException {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x30, 0x31});
        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(999, "Should be 1234",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LL, IsoFieldHeaderFormatter.TAG_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF1, (byte)0xF2,(byte)0xF0, (byte)0xF2, (byte)0x30, (byte)0x31}, packager.pack(field));
    }

    @Test
    public void unpackWithTagFirst() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF1, (byte)0xF2,(byte)0xF0, (byte)0xF2, (byte)0x30, (byte)0x31};

        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(999, "Should be 1234",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LL, IsoFieldHeaderFormatter.TAG_FIRST);

        ISOBinaryField isoField = new ISOBinaryField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        TestUtils.assertEquals (new byte[] {(byte)0x30, (byte)0x31}, (byte[]) isoField.getValue());
        assertEquals(12, isoField.fieldNumber);
    }

    @Test
    public void packWithLengthFirst() throws ISOException {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x30, 0x31});
        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(999, "Should be F0F0F4F1F23031",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF4, (byte)0xF1, (byte)0xF2, (byte)0x30, (byte)0x31}, packager.pack(field));
    }

    @Test
    public void unpackWithLengthFirst() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF4, (byte)0xF1, (byte)0xF2, (byte)0x30, (byte)0x31};

        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(999, "Should be 1234",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        ISOBinaryField isoField = new ISOBinaryField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        TestUtils.assertEquals(new byte[] {(byte)0x30, (byte)0x31}, (byte[])isoField.getValue());
        assertEquals(12, isoField.fieldNumber);
    }

    @Test
    public void packWithTagFirstZeroLenFixedLenField() throws ISOException {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x30, 0x31});
        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(2, "Should be F1F23031",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, NullPrefixer.INSTANCE, IsoFieldHeaderFormatter.TAG_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF1, (byte)0xF2, (byte)0x30, (byte)0x31}, packager.pack(field));
    }

    @Test
    public void unpackWithTagFirstZeroLenFixedLenField() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF1, (byte)0xF2, (byte)0x30, (byte)0x31};

        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(2, "Should be F1F23031",
                EbcdicPrefixer.LL, NullPadder.INSTANCE, LiteralBinaryInterpreter.INSTANCE, NullPrefixer.INSTANCE, IsoFieldHeaderFormatter.TAG_FIRST);

        ISOBinaryField isoField = new ISOBinaryField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        TestUtils.assertEquals(new byte[] {(byte)0x30, (byte)0x31}, (byte[]) isoField.getValue());
        assertEquals(12, isoField.fieldNumber);
    }

    @Test
    public void packWithLengthFirstLeftZeroPadder() throws ISOException {
        ISOBinaryField field = new ISOBinaryField(12, new byte[] {0x30, 0x31});
        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(10, "Should be F0F0F7F1F20000003031",
                EbcdicPrefixer.LL, LeftPadder.ZERO_PADDER, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        TestUtils.assertEquals(new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF7, (byte)0xF1, (byte)0xF2, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x30, (byte)0x31}, packager.pack(field));
    }

    @Test
    public void unpackWithLengthFirstLeftZeroPadder() throws ISOException {
        byte[] raw = new byte[] {(byte)0xF0, (byte)0xF0, (byte)0xF7, (byte)0xF1, (byte)0xF2, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x30, (byte)0x31};

        ISOFormattableBinaryFieldPackager packager = new ISOFormattableBinaryFieldPackager(10, "Should be F0F0F7F1F20000003031",
                EbcdicPrefixer.LL, LeftPadder.ZERO_PADDER, LiteralBinaryInterpreter.INSTANCE, EbcdicPrefixer.LLL, IsoFieldHeaderFormatter.LENGTH_FIRST);

        ISOBinaryField isoField = new ISOBinaryField();
        assertEquals(raw.length, packager.unpack(isoField, raw, 0));
        TestUtils.assertEquals(new byte[]{(byte)0x30, (byte)0x31}, (byte[]) isoField.getValue());
        assertEquals(12, isoField.fieldNumber);
    }

}
