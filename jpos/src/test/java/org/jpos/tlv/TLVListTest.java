/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.tlv;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList.TLVListBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TLVListTest {

    static final String EXCEPT_MSG_EXCEEDS_AVAL = "BAD TLV FORMAT: tag (%x) length (%d) exceeds available data";

    static final String EXCEPT_MSG_WITHOUT_LEN  = "BAD TLV FORMAT: tag (%x) without length or value";

    static final TLVListBuilder BUILDER_DEFAULT = TLVListBuilder.createInstance();

    static final TLVListBuilder BUILDER_FT1     = TLVListBuilder.createInstance()
            .fixedTagSize(1);

    static final TLVListBuilder BUILDER_FT2     = TLVListBuilder.createInstance()
            .fixedTagSize(2);

    static final TLVListBuilder BUILDER_FL1     = TLVListBuilder.createInstance()
            .fixedLengthSize(1);

    static final TLVListBuilder BUILDER_FL2     = TLVListBuilder.createInstance()
            .fixedLengthSize(2);

    static final TLVListBuilder BUILDER_FT1FL2  = TLVListBuilder.createInstance()
            .fixedTagSize(1)
            .fixedLengthSize(2);

    static final int TEST_TAG1      = 0x64;
    static final int TEST_TAG2      = 0x46;
    static final int TEST_TAG3      = 0x1fe8;

    TLVList instance;

    @BeforeEach
    public void beforeTest() {
        instance = BUILDER_DEFAULT.build();
    }

    @Test
    public void testBuilderFixedTagSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            TLVListBuilder.createInstance().fixedTagSize(0);
        });
    }

    @Test
    public void testBuilderFixedLengthSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            TLVListBuilder.createInstance().fixedLengthSize(0);
        });
    }

    @Test
    public void testBuilderFixedLengthSize2() {
        assertThrows(IllegalArgumentException.class, () -> {
            TLVListBuilder.createInstance().fixedLengthSize(5);
        });
    }

    @Test
    public void testAppend() {
        instance.append(TEST_TAG1, new byte[3]);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testAppend1() {
        instance.append(instance.createTLVMsg(TEST_TAG1, new byte[0]));
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testAppendThrowsNPE() {
        assertThrows(NullPointerException.class, () -> {
            instance.append(TEST_TAG1, new byte[2]);
            try {
                instance.append(null);
            } catch (RuntimeException ex) {
                assertFalse(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testAppendMinTagBelow() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance.append(0x00, new byte[0]);
        });
    }

    @Test
    public void testAppendMinTagAbove() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance.append(0xff, new byte[0]);
        });
    }

    @Test
    public void testAppendTwoBytesTag() {
        byte[] expected = ISOUtil.hex2byte("37D47C");
        instance.append(0x5f37, expected);
        byte[] result = instance.pack();
        assertEquals(6, result.length);
        assertArrayEquals(ISOUtil.hex2byte("5F37"), Arrays.copyOf(result, 2));
        assertArrayEquals(expected, Arrays.copyOfRange(result, 3, 6));
    }

    @Test
    public void testAppendThreeBytesTag() {
        byte[] expected = ISOUtil.hex2byte("37D47C");
        instance.append(0xbf5f37, expected);
        byte[] result = instance.pack();
        assertEquals(7, result.length);
        assertArrayEquals(ISOUtil.hex2byte("BF5F37"), Arrays.copyOf(result, 3));
        assertArrayEquals(expected, Arrays.copyOfRange(result, 4, 7));
    }

    @Test
    public void testAppendInvalidByteLow() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance.append(0x3f, new byte[0]);
        });
    }

    @Test
    public void testAppendInvalidByteHigh() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance.append(0x9f, new byte[0]);
        });
    }

    @Test
    public void testAppendFixedTagOneMinTag() {
        instance = BUILDER_FT1.build();
        instance.append(0x00, new byte[0]);
    }

    @Test
    public void testAppendFixedTagOneMinTagBelow() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance = BUILDER_FT1.build();
            instance.append(-0x01, new byte[0]);
        });
    }

    @Test
    public void testAppendFixedTagOneMaxTag() {
        instance = BUILDER_FT1.build();
        instance.append(0xff, new byte[0]);
    }

    @Test
    public void testAppendFixedTagOneMaxTagAbove() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance = BUILDER_FT1.build();
            instance.append(0x100, new byte[0]);
        });
    }

    @Test
    public void testAppendFixedTagOneByteHigh() {
        instance = BUILDER_FT1.build();
        instance.append(0x9f, new byte[0]);
    }

    @Test
    public void testAppendFixedLengthOneNull() {
        instance = BUILDER_FL1.build();
        instance.append(TEST_TAG1, (byte[]) null);
    }

    @Test
    public void testAppendFixedLengthOneAbove() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance = BUILDER_FL1.build();
            instance.append(TEST_TAG1, new byte[0x100]);
        });
    }

    @Test
    public void testAppendInvalidTwoBytesTagLow() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance.append(0x6f37, new byte[0]);
        });
    }

    @Test
    public void testAppendInvalidTwoBytesTagHigh() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance.append(0xaf37, new byte[0]);
        });
    }

    @Test
    public void testAppendInvalidTwoBytesTagHighEndZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance.append(0xbf00, new byte[0]);
        });
    }

    @Test
    public void testAppendInvalidThreeBytesTagHigh() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance.append(0xbf4f37, new byte[0]);
        });
    }

    @Test
    public void testAppendInvalidThreeBytesTagLow() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance.append(0x3f5fbf, new byte[0]);
        });
    }

    @Test
    public void testAppendInvalidThreeBytesTagHighEndFF() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance.append(0x3fff04, new byte[0]);
        });
    }

    @Test
    public void testConstructor() {
        instance = new TLVList();
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testDeleteByIndex() {
        instance.append(TEST_TAG1, new byte[0]);
        instance.deleteByIndex(0);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testDeleteByIndexThrowsIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            instance.deleteByIndex(TEST_TAG1);
        });
    }

    @Test
    public void testDeleteByTag() {
        instance.append(TEST_TAG1, new byte[1]);
        instance.append(TEST_TAG2, new byte[0]);
        instance.deleteByTag(TEST_TAG1);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testDeleteByTag1() {
        instance.append(TEST_TAG1, new byte[0]);
        instance.deleteByTag(TEST_TAG3);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testDeleteByTag2() {
        instance.deleteByTag(TEST_TAG1);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testDeleteByTag3() {
        instance.append(TEST_TAG2, new byte[0]);
        instance.deleteByTag(TEST_TAG2);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testTags() {
        List<TLVMsg> result = instance.getTags();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFind() {
        instance.append(TEST_TAG1, new byte[1]);
        TLVMsg expected = instance.createTLVMsg(0x07, null);
        instance.append(expected);
        TLVMsg result = instance.find(0x07);
        assertSame(expected, result);
    }

    @Test
    public void testFind1() {
        TLVMsg expected = instance.createTLVMsg(TEST_TAG1, null);
        instance.append(expected);
        TLVMsg result = instance.find(TEST_TAG1);
        assertSame(expected, result);
    }

    @Test
    public void testFind2() {
        instance.append(TEST_TAG3, new byte[0]);
        TLVMsg result = instance.find(TEST_TAG1);
        assertNull(result);
    }

    @Test
    public void testFind3() {
        TLVMsg result = instance.find(TEST_TAG1);
        assertNull(result);
    }

    @Test
    public void testFindIndex() {
        instance.append(TEST_TAG1, new byte[2]);
        instance.append(TEST_TAG2, new byte[2]);
        int result = instance.findIndex(TEST_TAG2);
        assertEquals(1, result);
    }

    @Test
    public void testFindIndex1() {
        instance.append(TEST_TAG3, new byte[3]);
        int result = instance.findIndex(TEST_TAG1);
        assertEquals(-1, result);
    }

    @Test
    public void testFindIndex2() {
        instance.append(instance.createTLVMsg(TEST_TAG1, null));
        int result = instance.findIndex(TEST_TAG1);
        assertEquals(0, result);
    }

    @Test
    public void testFindNextTLV() {
        instance.findIndex(TEST_TAG1);
        instance.append(TEST_TAG3, new byte[0]);
        TLVMsg result = instance.findNextTLV();
        assertNull(result);
    }

    @Test
    public void testFindNextTLV1() {
        instance.append(TEST_TAG1, new byte[2]);
        instance.append(TEST_TAG2, new byte[2]);
        TLVMsg expected = instance.createTLVMsg(TEST_TAG1, null);
        instance.append(expected);
        assertEquals(0, instance.findIndex(TEST_TAG1));
        TLVMsg result = instance.findNextTLV();
        assertSame(expected, result);
    }

    @Test
    public void testFindNextTLV2() {
        instance.append(TEST_TAG1, new byte[2]);
        instance.append(TEST_TAG2, new byte[2]);
        TLVMsg expected = instance.createTLVMsg(TEST_TAG1, null);
        instance.append(expected);
        instance.find(TEST_TAG1);
        TLVMsg result = instance.findNextTLV();
        assertSame(expected, result);
    }

    @Test
    public void testFindNextTLVThrowsIllegalStateExeption1() {
        assertThrows(IllegalStateException.class, () -> {
            instance.append(TEST_TAG1, new byte[3]);
            instance.findNextTLV();
        });
    }

    @Test
    public void testFindNextTLVThrowsIllegalStateExeption2() {
        assertThrows(IllegalStateException.class, () -> {
            instance.findNextTLV();
        });
    }

    @Test
    public void testIndex() {
        TLVMsg expected = instance.createTLVMsg(TEST_TAG1, null);
        instance.append(expected);
        TLVMsg result = instance.index(0);
        assertSame(expected, result);
    }

    @Test
    public void testIndex1() {
        instance.append(instance.createTLVMsg(TEST_TAG1, "testString".getBytes()));
        instance.append(TEST_TAG1, new byte[1]);
        instance.append(instance.createTLVMsg(0x0b, null));
        instance.append(TEST_TAG3, new byte[3]);
        instance.append(TEST_TAG2, new byte[1]);
        instance.append(0x0f, new byte[0]);
        instance.deleteByIndex(0);
        instance.append(instance.createTLVMsg(0x0c, null));
        instance.append(1, new byte[3]);
        instance.append(0x0a, new byte[0]);
        instance.append(instance.createTLVMsg(0x0d, null));
        instance.append(0x3f10, new byte[2]);
        instance.append(0x1f7fa0, new byte[1]);
        TLVMsg result = instance.index(10);
        assertEquals(0x1f7fa0, result.getTag());
    }

    @Test
    public void testIndexThrowsIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            instance.index(TEST_TAG1);
        });
    }

    @Test
    public void testPack() {
        byte[] result = instance.pack();
        assertEquals(0, result.length);
    }

    @Test
    public void testPackFixedTagSizeTwo() {
        instance = BUILDER_FT2.build();
        instance.append(TEST_TAG1, ISOUtil.hex2byte("F12E3D"));
        byte[] result = instance.pack();
        assertArrayEquals(ISOUtil.hex2byte("006403"), Arrays.copyOf(result, 3));
    }

    @Test
    public void testPackFixedLengthSizeTwo() {
        instance = BUILDER_FL2.build();
        instance.append(TEST_TAG1, ISOUtil.hex2byte("F12E3D"));
        byte[] result = instance.pack();
        assertArrayEquals(ISOUtil.hex2byte("640003"), Arrays.copyOf(result, 3));
    }

    @Test
    public void testPackFixedTagSizeOneAndFixedLengthSizeTwo() {
        instance = BUILDER_FT1FL2.build();
        instance.append(0x85, new byte[0x84]);
        byte[] result = instance.pack();
        assertEquals(0x87, result.length);
        assertArrayEquals(ISOUtil.hex2byte("850084"), Arrays.copyOf(result, 3));
    }

    @Test
    public void testPackFixedLengthSizeOne() {
        instance = BUILDER_FL1.build();
        instance.append(TEST_TAG1, new byte[0x84]);
        byte[] result = instance.pack();
        assertEquals(0x86, result.length);
        assertArrayEquals(ISOUtil.hex2byte("6484"), Arrays.copyOf(result, 2));
    }

    @Test
    public void testUnpack() {
        byte[] buf = ISOUtil.hex2byte("030100");
        instance.unpack(buf, 0);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x03, tm.getTag());
        assertArrayEquals(ISOUtil.hex2byte("00"), tm.getValue());
    }

    @Test
    public void testUnpack1() {
        byte[] buf = ISOUtil.hex2byte("000100");
        instance.unpack(buf, 0);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x01, tm.getTag());
        assertArrayEquals(new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack10() {
        byte[] buf = ISOUtil.hex2byte("2080");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x20, tm.getTag());
        assertArrayEquals(new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack11() {
        byte[] buf = ISOUtil.hex2byte("030100");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x03, tm.getTag());
        assertArrayEquals(ISOUtil.hex2byte("00"), tm.getValue());
    }

    @Test
    public void testUnpack12() {
        byte[] buf = ISOUtil.hex2byte("ff00");
        instance.unpack(buf);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack2() {
        byte[] buf = ISOUtil.hex2byte("000100");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x01, tm.getTag());
        assertArrayEquals(new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack3() {
        byte[] buf = ISOUtil.hex2byte("0100");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x01, tm.getTag());
        assertArrayEquals(new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack4() {
        byte[] buf = new byte[0];
        instance.unpack(buf);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack5() {
        byte[] buf = new byte[0];
        instance.unpack(buf, 0);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack6() {
        byte[] buf = ISOUtil.hex2byte("6000");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack7() {
        byte[] buf = new byte[2];
        instance.unpack(buf);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack8() {
        byte[] buf = new byte[3];
        instance.unpack(buf);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack9() {
        byte[] buf = ISOUtil.hex2byte("1e00");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpackWith0x00Padding() {
        byte[] buf = ISOUtil.hex2byte("fe0000");
        instance.unpack(buf, 0);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpackWith0x00Padding1() {
        byte[] buf = ISOUtil.hex2byte("878000");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpackFixedTagOne() {
        instance = BUILDER_FT1.build();
        instance.unpack(ISOUtil.hex2byte("0003112233"));

        assertEquals(1, instance.getTags().size());
        TLVMsg res = instance.find(0);
        assertArrayEquals(ISOUtil.hex2byte("03"), res.getL());
        assertArrayEquals(ISOUtil.hex2byte("112233"), res.getValue());
    }

    @Test
    public void testUnpackFixeTagOneWith0x00Padding1() {
        assertThrows(IllegalArgumentException.class, () -> {
            instance = BUILDER_FT1.build();
            instance.unpack(ISOUtil.hex2byte("000003112233"));
        });
    }

    @Test
    public void testUnpackFixedLengthTwo() {
        instance = BUILDER_FL2.build();
        instance.unpack(ISOUtil.hex2byte("070003112233"));

        assertEquals(1, instance.getTags().size());
        TLVMsg res = instance.find(7);
        assertArrayEquals(ISOUtil.hex2byte("0003"), res.getL());
        assertArrayEquals(ISOUtil.hex2byte("112233"), res.getValue());
    }

    @Test
    public void testUnpackFixedLengthTwoWith0x00Padding1() {
        instance = BUILDER_FL2.build();
        instance.unpack(ISOUtil.hex2byte("0007000311223300"));

        assertEquals(1, instance.getTags().size());
        TLVMsg res = instance.find(7);
        assertArrayEquals(ISOUtil.hex2byte("0003"), res.getL());
        assertArrayEquals(ISOUtil.hex2byte("112233"), res.getValue());
    }

    @Test
    public void testUnpackInvalidLengthThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("14830000");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackInvalidTagThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("007f");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            byte[] buf = new byte[3];
            try {
                instance.unpack(buf, 100);
            } catch (IndexOutOfBoundsException ex) {
                assertNull(ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("00ff80");
            try {
                instance.unpack(buf, 0);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0x80), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException1() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("001e");
            try {
                instance.unpack(buf, 0);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0x1e), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException10() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("7f0007");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_EXCEEDS_AVAL, 0x7f00, 0x07), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException11() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("ff1e");
            try {
                instance.unpack(buf, 0);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0x1e), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException12() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("fe2000");
            try {
                instance.unpack(buf, 0);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_EXCEEDS_AVAL, 0xfe, 0x20), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException13() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("000008");
            try {
                instance.unpack(buf, 0);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0x08), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException14() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("7f00");
            try {
                instance.unpack(buf, 0);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0x7f00), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException15() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("f8");
            try {
                instance.unpack(buf, 0);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0xf8), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException16() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("fe81ed");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_EXCEEDS_AVAL, 0xfe, 0xed), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException17() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("000001");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0x01), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException18() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("878009");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0x09), ex.getMessage());
                assertFalse(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException2() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("0001");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0x01), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException3() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("fe00ed");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0xed), ex.getMessage());
                assertFalse(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException4() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("00fe");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0xfe), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsISOException5() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("fe7600");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_EXCEEDS_AVAL, 0xfe, 0x76), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException6() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("ff1e");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0x1e), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException7() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("01");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0x01), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException8() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("7f00");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_WITHOUT_LEN, 0x7f00), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsIllegalArgumentException9() {
        assertThrows(IllegalArgumentException.class, () -> {
            byte[] buf = ISOUtil.hex2byte("00f801");
            try {
                instance.unpack(buf);
            } catch (IllegalArgumentException ex) {
                assertEquals(String.format(EXCEPT_MSG_EXCEEDS_AVAL, 0xf8, 0x01), ex.getMessage());
                assertTrue(instance.getTags().isEmpty());
                throw ex;
            }
        });
    }

    @Test
    public void testUnpackThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            instance.unpack(null, 100);
        });
    }

    @Test
    public void testUnpackThrowsNullPointerException1() {
        assertThrows(NullPointerException.class, () -> {
            instance.unpack(null);
        });
    }

}