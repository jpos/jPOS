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

package org.jpos.iso.packager;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 */
public class TaggedFieldPackagerBaseTest {

    private static final Path PACKAGERS_LOCATION = FileSystems
            .getDefault()
            .getPath("build/resources/test/org/jpos/iso/packagers");

    private static final Path ISO83TLVPACKAGER   = PACKAGERS_LOCATION.resolve("ISO93TLVPackager.xml");

    private static final String MESSAGE_MTI1     = "1100";

    /**
     * The first bitmap with DE2, DE48 and DE60 set.
     */
    private static final byte[] MESSAGE_BITMAP1  = ISOUtil.hex2byte("4000000000010010");

    private static final String MESSAGE_VALUE1   = "06123456022A10748TagA1A30748TagA3012A100760TagA1";

    /**
     * Unsorted DE48 tags.
     * <p>
     * The A3 <i>(id 3)</i> before A1 <i>(id 1)</i>.
     */
    private static final String MESSAGE_VALUE2   = "06123456022A30748TagA3A10748TagA1012A100760TagA1";

    /**
     * Undefined DE48 tag.
     * <p>
     * After A1 <i>(id 1)</i> followed by undefined in packager A2 <i>(id 2)</i>.
     */
    private static final String MESSAGE_VALUE3   = "06123456022A10748TagA1A20748TagA2012A100760TagA1";

    /**
     * Undefined DE48 tag with followed tag.
     * <p>
     * Undefined in packager A2 <i>(id 2)</i> and following A3 <i>(id 3)</i>.
     */
    private static final String MESSAGE_VALUE4   = "06123456022A20748TagA2A30748TagA3012A100760TagA1";

    private static final String REPR_MESSAGE1    = MESSAGE_MTI1 + toString(MESSAGE_BITMAP1) + MESSAGE_VALUE1;

    /**
     * Unsorted DE48 tags message representation.
     */
    private static final String REPR_MESSAGE2    = MESSAGE_MTI1 + toString(MESSAGE_BITMAP1) + MESSAGE_VALUE2;

    /**
     * Undefined DE48 tag message representation.
     */
    private static final String REPR_MESSAGE3    = MESSAGE_MTI1 + toString(MESSAGE_BITMAP1) + MESSAGE_VALUE3;

    private static final String REPR_MESSAGE4    = MESSAGE_MTI1 + toString(MESSAGE_BITMAP1) + MESSAGE_VALUE4;

    private static ISOPackager packager;

    @BeforeAll
    static void beforeClass() throws Throwable {
        packager = new GenericPackager(new FileInputStream(ISO83TLVPACKAGER.toFile()));
    }

    private static String toString(byte[] b) {
        return new String(b, ISOUtil.CHARSET);
    }

    private static byte[] toBytes(String str) {
        return str.getBytes(ISOUtil.CHARSET);
    }

    @Test
    public void testPack() throws Exception {
        ISOMsg msg = new ISOMsg(MESSAGE_MTI1);
        msg.set(2, "123456");

        msg.set("48.1", "48TagA1");
        msg.set("48.3", "48TagA3");

        msg.set("60.1", "60TagA1");

        msg.setPackager(packager);

        byte[] packed = msg.pack();

        String packedAscii = toString(packed);
        assertAll(
            () -> assertEquals(REPR_MESSAGE1.length(), packed.length),
            () -> assertEquals(MESSAGE_MTI1, StringUtils.left(packedAscii, 4)),
            () -> assertEquals(MESSAGE_VALUE1, StringUtils.right(packedAscii, 48))
        );
    }

    @Test
    public void testPackWithUndefied() throws Exception {
        ISOMsg msg = new ISOMsg(MESSAGE_MTI1);
        msg.set(2, "123456");

        msg.set("48.1", "48TagA1");
        msg.set("48.2", "48TagA2"); // undefined in packager
        msg.set("48.3", "48TagA3");

        msg.set("60.1", "60TagA1");

        msg.setPackager(packager);
        String expected = "06123456033A10748TagA1A20748TagA2A30748TagA3012A100760TagA1";
        int expectedLength = MESSAGE_MTI1.length() + MESSAGE_BITMAP1.length + expected.length();

        byte[] packed = msg.pack();

        String packedAscii = toString(packed);
        assertAll(
            () -> assertEquals(expectedLength, packed.length),
            () -> assertEquals(MESSAGE_MTI1, StringUtils.left(packedAscii, 4)),
            () -> assertEquals(expected, StringUtils.right(packedAscii, expected.length()))
        );
    }

    @Test
    public void testPackWithUndefiedBinary() throws Exception {
        ISOMsg msg = new ISOMsg(MESSAGE_MTI1);
        msg.set(2, "123456");

        msg.set("48.1", "48TagA1");
        msg.set("48.2", "48TagA2".getBytes(ISOUtil.CHARSET)); // undefined in packager
        msg.set("48.3", "48TagA3");

        msg.set("60.1", "60TagA1");

        msg.setPackager(packager);

        byte[] packed = msg.pack();

        String packedAscii = toString(packed);
        assertAll(
            () -> assertEquals(REPR_MESSAGE1.length(), packed.length),
            () -> assertEquals(MESSAGE_MTI1, StringUtils.left(packedAscii, 4)),
            () -> assertEquals(MESSAGE_VALUE1, StringUtils.right(packedAscii, 48))
        );
    }

    @Test
    public void testPackWithUndefiedAndUnmapped() throws Exception {
        ISOMsg msg = new ISOMsg(MESSAGE_MTI1);
        msg.set(2, "123456");

        msg.set("48.0", "48TagA0"); // undefined in packager and unmapped
        msg.set("48.1", "48TagA1");
        msg.set("48.3", "48TagA3");

        msg.set("60.1", "60TagA1");

        msg.setPackager(packager);

        byte[] packed = msg.pack();

        String packedAscii = toString(packed);
        assertAll(
            () -> assertEquals(REPR_MESSAGE1.length(), packed.length),
            () -> assertEquals(MESSAGE_MTI1, StringUtils.left(packedAscii, 4)),
            () -> assertEquals(MESSAGE_VALUE1, StringUtils.right(packedAscii, 48))
        );
    }

    @Test
    public void testUnpack() throws Exception {
        ISOMsg msg = new ISOMsg();
        packager.unpack(msg, toBytes(REPR_MESSAGE1));

        assertAll(
            () -> assertEquals("1100", msg.getString(0)),
            () -> assertEquals("123456", msg.getString(2)),
            () -> assertEquals("48TagA1", msg.getString("48.1")),
            () -> assertEquals("48TagA3", msg.getString("48.3")),
            () -> assertEquals("60TagA1", msg.getString("60.1"))
        );
    }

    @Test
    public void testUnpackUnsortedTags() throws Exception {
        ISOMsg msg = new ISOMsg();
        packager.unpack(msg, toBytes(REPR_MESSAGE2));

        assertAll(
            () -> assertEquals("1100", msg.getString(0)),
            () -> assertEquals("123456", msg.getString(2)),
            () -> assertEquals("48TagA1", msg.getString("48.1")),
            () -> assertEquals("48TagA3", msg.getString("48.3")),
            () -> assertEquals("60TagA1", msg.getString("60.1"))
        );
    }

    @Test
    public void testUnpackUndefinedTag() throws Exception {
        ISOMsg msg = new ISOMsg();
        packager.unpack(msg, toBytes(REPR_MESSAGE3));

        assertAll(
            () -> assertEquals("1100", msg.getString(0)),
            () -> assertEquals("123456", msg.getString(2)),
            () -> assertEquals("48TagA1", msg.getString("48.1")),
            () -> assertEquals("48TagA2", msg.getString("48.2")),
            () -> assertEquals("60TagA1", msg.getString("60.1"))
        );
    }

    @Test
    public void testUnpackUndefinedAndUnmappedTag() throws Exception {
        ISOMsg msg = new ISOMsg();
        String msgValue = "06123456022A00748TagA0A30748TagA3012A100760TagA1";
        String reprMessage = MESSAGE_MTI1 + toString(MESSAGE_BITMAP1) + msgValue;
        packager.unpack(msg, toBytes(reprMessage));

        assertAll(
            () -> assertEquals("1100", msg.getString(0)),
            () -> assertEquals("123456", msg.getString(2)),
            () -> assertNull(msg.getString("48.0")),
            () -> assertNull(msg.getString("48.1")),
            () -> assertNull(msg.getString("48.2")),
            () -> assertEquals("48TagA3", msg.getString("48.3")),
            () -> assertEquals("60TagA1", msg.getString("60.1"))
        );
    }

    @Test
    public void testUnpackUndefinedTagWithFollowing() throws Exception {
        ISOMsg msg = new ISOMsg();
        packager.unpack(msg, toBytes(REPR_MESSAGE4));

        assertAll("Should unpack DE48.3 (A3) and any other standard elements",
            () -> assertEquals("1100", msg.getString(0)),
            () -> assertEquals("123456", msg.getString(2)),
            () -> assertEquals("48TagA2", msg.getString("48.2")),
            () -> assertEquals("48TagA3", msg.getString("48.3")),
            () -> assertEquals("60TagA1", msg.getString("60.1"))
        );
    }

    public static class TagMapperImpl implements TagMapper {

        private static final Map<String, Integer> MAP_TAG_MUMBER = new HashMap<>();
        private static final Map<String, String> MAP_NUMBER_TAG = new HashMap<>();

        static {
            MAP_TAG_MUMBER.put("48.A1", 1);
            MAP_TAG_MUMBER.put("48.A2", 2);
            MAP_TAG_MUMBER.put("48.A3", 3);
            MAP_TAG_MUMBER.put("60.A1", 1);

            MAP_NUMBER_TAG.put("48.1", "A1");
            MAP_NUMBER_TAG.put("48.2", "A2");
            MAP_NUMBER_TAG.put("48.3", "A3");
            MAP_NUMBER_TAG.put("60.1", "A1");
        }

        @Override
        public String getTagForField(int fieldNumber, int subFieldNumber) {
            return MAP_NUMBER_TAG.get(fieldNumber + "." + subFieldNumber);
        }

        @Override
        public Integer getFieldNumberForTag(int fieldNumber, String tag) {
            return MAP_TAG_MUMBER.get(fieldNumber + "." + tag);
        }
    }

}
