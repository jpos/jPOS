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
public class TTTDecimalTagMapperTest {

    Exception thrown;

    private TagMapper instance;

    private String resTag;

    private int resField;

    @BeforeEach
    public void setUp() {
        instance = new TTTDecimalTagMapper();
    }

    @Test
    public void testGetTagForFieldWith0() {
        resTag = instance.getTagForField(-1, 0);
        assertEquals("000", resTag);
    }

    @Test
    public void testGetTagForFieldWith3() {
        resTag = instance.getTagForField(Integer.MAX_VALUE, 3);
        assertEquals("003", resTag);
    }

    @Test
    public void testGetTagForFieldWith23() {
        resTag = instance.getTagForField(-1, 23);
        assertEquals("023", resTag);
    }

    @Test
    public void testGetTagForFieldWith934() {
        resTag = instance.getTagForField(0, 934);
        assertEquals("934", resTag);
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
            () -> instance.getTagForField(0, 47656)
        );
        assertEquals("The subtag id: 47656 of DE0 out of range"
                , StringUtils.left(thrown.getMessage(), 40)
        );
    }

    @Test
    public void testGetTagForFieldWithFirstIllegal() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getTagForField(0, 1000)
        );
        assertEquals("The subtag id: 1000 of DE0 is illegal", thrown.getMessage());
    }

    @Test
    public void testGetTagForFieldWith1036() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getTagForField(0, 1036)
        );
        assertEquals("The subtag id: 1036 of DE0 is illegal", thrown.getMessage());
    }

    @Test
    public void testGetTagForFieldWith1010() {
        resTag = instance.getTagForField(0, 1010);
        assertEquals("00A", resTag);
    }

    @Test
    public void testGetTagForFieldWith1011() {
        resTag = instance.getTagForField(0, 1011);
        assertEquals("00B", resTag);
    }

    @Test
    public void testGetTagForFieldWith1046() {
        resTag = instance.getTagForField(0, 1046);
        assertEquals("01A", resTag);
    }

    @Test
    public void testGetTagForFieldWith47655() {
        resTag = instance.getTagForField(0, 47655);
        assertEquals("ZZZ", resTag);
    }

    @Test
    public void testGetFieldNumberForTagWith003() {
        resField = instance.getFieldNumberForTag(Integer.MAX_VALUE, "003");
        assertEquals(3, resField);
    }

    @Test
    public void testGetFieldNumberForTagWith023() {
        resField = instance.getFieldNumberForTag(Integer.MAX_VALUE, "023");
        assertEquals(23, resField);
    }

    @Test
    public void testGetFieldNumberForTagWith934() {
        resField = instance.getFieldNumberForTag(Integer.MIN_VALUE, "934");
        assertEquals(934, resField);
    }

    @Test
    public void testGetFieldNumberForTagWith00A() {
        resField = instance.getFieldNumberForTag(0, "00A");
        assertEquals(1010, resField);
    }

    @Test
    public void testGetFieldNumberForTagWith90Z() {
        resField = instance.getFieldNumberForTag(0, "90Z");
        assertEquals(12699, resField);
    }

    @Test
    public void testGetFieldNumberForTagWithZZZ() {
        resField = instance.getFieldNumberForTag(0, "ZZZ");
        assertEquals(47655, resField);
    }

    @Test
    public void testGetFieldNumberForTagWithOtherChars() {
        thrown = assertThrows(NumberFormatException.class,
            () -> instance.getFieldNumberForTag(48, "#3!")
        );
        assertEquals("The subtag '#3!' of DE48 cannot be converted"
                , StringUtils.left(thrown.getMessage(), 44)
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
            () -> instance.getFieldNumberForTag(62, "1005")
        );
        assertEquals("The subtag id: '1005' length of DE62 must be 3"
                , thrown.getMessage()
        );
    }

    @Test
    public void testGetFieldNumberForTagWithMinusOne() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getFieldNumberForTag(62, "-01")
        );
        assertEquals("The subtag id: -01 of DE62 cannot be negative"
                , thrown.getMessage()
        );
    }

    @Test
    public void testGetFieldNumberForTagWithMinus0A() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getFieldNumberForTag(62, "-0A")
        );
        assertEquals("The subtag id: -0A of DE62 cannot be negative"
                , thrown.getMessage()
        );
    }

    @Test
    public void testGetFieldNumberForTagMinusWithLowerLetter() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getFieldNumberForTag(48, "1cA")
        );
        assertEquals("The subtag '1cA' of DE48 cannot be converted"
                , StringUtils.left(thrown.getMessage(), 44)
        );
    }

    @Test
    public void testGetFieldNumberForTagWithLettersOtherChars() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.getFieldNumberForTag(48, "1#A")
        );
        assertEquals("The subtag '1#A' of DE48 cannot be converted"
                , StringUtils.left(thrown.getMessage(), 44)
        );
    }

}
