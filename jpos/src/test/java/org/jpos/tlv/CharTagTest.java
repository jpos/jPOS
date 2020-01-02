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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the CharTag class.
 *
 * @author Robert Demski <drdemsey@gmail.com>
 */
public class CharTagTest {

    CharTag instance;
    CharTagMap tagMap;

    @BeforeEach
    public void setUp() {
        tagMap = CharTagMap.getInstance();
        instance = tagMap.createTLV("02", "valueXYZ");
    }

    @Test
    void testTag() {
        assertEquals("02", instance.getTagId());
        assertEquals("valueXYZ", instance.getValue());
        assertEquals("02008valueXYZ", instance.getTLV());
    }

    @Test
    void testTLVNullValue() {
        instance = tagMap.createTLV("02", null);
        assertEquals("02000", instance.getTLV());
    }

    @Test
    void testToString() {
        assertEquals("tag: 02, len: 8, value: valueXYZ", instance.toString());
    }

    @Test
    void testToStringNullValue() {
        instance = tagMap.createTLV("K7", null);
        assertEquals("tag: K7, len: 0", instance.toString());
    }

}
