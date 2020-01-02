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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Robert Demski <drdemsey@gmail.com>
 */
public class TTDecimalTagMapperTest {

    Exception thrown;

    private TagMapper instance;

    private String resTag;

    private int resField;

    @BeforeEach
    public void setUp() {
        instance = new TTDecimalTagMapper();
    }

    @Test
    public void testGetTagForFieldWith0() {
        resTag = instance.getTagForField(-1, 0);
        assertEquals("00", resTag);
    }

    @Test
    public void testGetTagForFieldWith3() {
        resTag = instance.getTagForField(Integer.MAX_VALUE, 3);
        assertEquals("03", resTag);
    }

    @Test
    public void testGetTagForFieldWith23() {
        resTag = instance.getTagForField(-1, 23);
        assertEquals("23", resTag);
    }

    @Test
    public void testGetTagForFieldWith934() {
        resTag = instance.getTagForField(0, 98);
        assertEquals("98", resTag);
    }

    @Test
    public void testGetTagForFieldWithMinusOne() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getTagForField(0, -1)
        );
        assertEquals("The subtag id: -1 of DE0 out of range"
                , StringUtils.left(thrown.getMessage(), 37)
        );
    }

    @Test
    public void testGetTagForFieldWithOutOfRange() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getTagForField(0, 1396)
        );
        assertEquals("The subtag id: 1396 of DE0 out of range"
                , StringUtils.left(thrown.getMessage(), 39)
        );
    }

    @Test
    public void testGetTagForFieldWithFirstIllegal() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getTagForField(0, 100)
        );
        assertEquals("The subtag id: 100 of DE0 is illegal", thrown.getMessage());
    }

    @Test
    public void testGetTagForFieldWith1036() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getTagForField(0, 109)
        );
        assertEquals("The subtag id: 109 of DE0 is illegal", thrown.getMessage());
    }

    @Test
    public void testGetTagForFieldWith110() {
        resTag = instance.getTagForField(0, 110);
        assertEquals("0A", resTag);
    }

    @Test
    public void testGetTagForFieldWith111() {
        resTag = instance.getTagForField(0, 111);
        assertEquals("0B", resTag);
    }

    @Test
    public void testGetTagForFieldWith146() {
        resTag = instance.getTagForField(0, 146);
        assertEquals("1A", resTag);
    }

    @Test
    public void testGetTagForFieldWith1395() {
        resTag = instance.getTagForField(0, 1395);
        assertEquals("ZZ", resTag);
    }

    @Test
    public void testGetFieldNumberForTagWith03() {
        resField = instance.getFieldNumberForTag(Integer.MAX_VALUE, "03");
        assertEquals(3, resField);
    }

    @Test
    public void testGetFieldNumberForTagWith23() {
        resField = instance.getFieldNumberForTag(Integer.MAX_VALUE, "23");
        assertEquals(23, resField);
    }

    @Test
    public void testGetFieldNumberForTagWith98() {
        resField = instance.getFieldNumberForTag(Integer.MIN_VALUE, "98");
        assertEquals(98, resField);
    }

    @Test
    public void testGetFieldNumberForTagWith0A() {
        resField = instance.getFieldNumberForTag(0, "0A");
        assertEquals(110, resField);
    }

    @Test
    public void testGetFieldNumberForTagWith9Z() {
        resField = instance.getFieldNumberForTag(0, "9Z");
        assertEquals(459, resField);
    }

    @Test
    public void testGetFieldNumberForTagWithZZ() {
        resField = instance.getFieldNumberForTag(0, "ZZ");
        assertEquals(1395, resField);
    }

    @Test
    public void testGetFieldNumberForTagWithOtherChars() {
        thrown = assertThrows(NumberFormatException.class,
            () -> instance.getFieldNumberForTag(48, "3!")
        );
        assertEquals("The subtag '3!' of DE48 cannot be converted"
                , StringUtils.left(thrown.getMessage(), 43)
        );
    }

    @Test
    public void testGetFieldNumberForTagWithNull() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getFieldNumberForTag(48, null)
        );
        assertEquals("The subtag id of DE48 cannot be null", thrown.getMessage());
    }

    @Test
    public void testGetFieldNumberForTagWithInvalidSize() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getFieldNumberForTag(62, "105")
        );
        assertEquals("The subtag id: '105' length of DE62 must be 2"
                , thrown.getMessage()
        );
    }

    @Test
    public void testGetFieldNumberForTagWithMinusOne() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getFieldNumberForTag(62, "-1")
        );
        assertEquals("The subtag id: -1 of DE62 cannot be negative"
                , thrown.getMessage()
        );
    }

    @Test
    public void testGetFieldNumberForTagWithMinusA() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getFieldNumberForTag(62, "-A")
        );
        assertEquals("The subtag id: -A of DE62 cannot be negative"
                , thrown.getMessage()
        );
    }

    @Test
    public void testGetFieldNumberForTagMinusWithLowerLetter() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getFieldNumberForTag(48, "1c")
        );
        assertEquals("The subtag '1c' of DE48 cannot be converted"
                , StringUtils.left(thrown.getMessage(), 43)
        );
    }

    @Test
    public void testGetFieldNumberForTagWithLettersOtherChars() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getFieldNumberForTag(48, "#A")
        );
        assertEquals("The subtag '#A' of DE48 cannot be converted"
                , StringUtils.left(thrown.getMessage(), 43)
        );
    }

}
