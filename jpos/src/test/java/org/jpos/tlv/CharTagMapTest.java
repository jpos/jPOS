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

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the CharTagMap class.
 *
 * @author Robert Demski <drdemsey@gmail.com>
 */
public class CharTagMapTest {

    Exception thrown;

    CharTagMap instance;

    @BeforeEach
    public void setUp() {
        instance = CharTagMap.getInstance();
    }

    @Test
    public void testCreateTLV() {
        CharTag tag = instance.createTLV("X7", "test");

        assertAll(
            () -> assertEquals("X7", tag.getTagId()),
            () -> assertEquals("test", tag.getValue()),
            () -> assertNull(instance.getTagValue("X7")),
            () -> assertNull(instance.get("X7"))
        );
    }

    @Test
    public void testCreateTLVWithNullValue() {
        CharTag tag = instance.createTLV("X7", null);

        assertAll(
            () -> assertEquals("X7", tag.getTagId()),
            () -> assertNull(tag.getValue()),
            () -> assertNull(instance.get("X7"))
        );
    }

    @Test
    public void testCreateTLVWithTagIdNull() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.createTLV(null, "test")
        );
        assertEquals("Tag identifier have to be specified", thrown.getMessage());
    }

    @Test
    public void testCreateTLVWithTagIdInvalidSize() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.createTLV("XYZ", "test")
        );
        assertEquals("Invalid tag 'XYZ' size: expected 2, but got 3", thrown.getMessage());
    }

    @Test
    public void testCreateTLVWithValueExceededMaxSize() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.createTLV("XY", ISOUtil.zeropad("", 1000))
        );
        assertEquals(
            "The value size 1000 of the tag 'XY' has exceeded the maximum allowable value 999"
            , thrown.getMessage()
        );
    }

    @Test
    public void testUnpackEmptyTag() {
        instance.unpack("02000");

        assertAll(
            () -> assertEquals(1, instance.size()),
            () -> assertEquals("02", instance.get("02").getTagId()),
            () -> assertEquals("", instance.get("02").getValue())
        );
    }

    @Test
    public void testUnpackOneTag() {
        instance.unpack("02008ValueXyZ");

        assertAll(
            () -> assertEquals(1, instance.size()),
            () -> assertEquals("02", instance.get("02").getTagId()),
            () -> assertEquals("ValueXyZ", instance.get("02").getValue())
        );
    }

    @Test
    public void testUnpackOneTagAndMissing() {
        instance.unpack("02008ValueXyZ");

        assertAll(
            () -> assertEquals(1, instance.size()),
            () -> assertEquals("02", instance.get("02").getTagId()),
            () -> assertEquals("ValueXyZ", instance.get("02").getValue()),
            () -> assertNull(instance.get("03"))
        );
    }

    @Test
    public void testUnpackTwoTag() {
        instance.unpack("02008ValueXyZ03007ValueAb");

        assertAll(
            () -> assertEquals(2, instance.size()),
            () -> assertEquals("02", instance.get("02").getTagId()),
            () -> assertEquals("ValueXyZ", instance.get("02").getValue()),
            () -> assertEquals("03", instance.get("03").getTagId()),
            () -> assertEquals("ValueAb", instance.get("03").getValue())
        );
    }

    @Test
    public void testUnpackTwoTagReverted() {
        instance.unpack("03007ValueAb02008ValueXyZ");

        assertAll(
            () -> assertEquals(2, instance.size()),
            () -> assertEquals("02", instance.get("02").getTagId()),
            () -> assertEquals("ValueXyZ", instance.get("02").getValue()),
            () -> assertEquals("03", instance.get("03").getTagId()),
            () -> assertEquals("ValueAb", instance.get("03").getValue())
        );
    }

    @Test
    public void testUnpackTwoTagShortTag() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.unpack("03008wartosc20")
        );
    }

    @Test
    public void testUnpackTwoTagShortLength() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.unpack("0400")
        );
    }

    @Test
    public void testUnpackRevertedTwoTagShortValue() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.unpack("04008wa")
        );
    }

    @Test
    public void testUnpackNull() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.unpack(null)
        );
    }

}
