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

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Robert Demski <drdemsey@gmail.com>
 */
public class CharTagMapBuilderTest {

    Exception thrown;

    CharTagMapBuilder instance;

    @BeforeEach
    public void setUp() {
        instance = new CharTagMapBuilder();
    }

    @Test
    public void testBuildDefault() {
        CharTagMap tm = instance.build();

        tm.unpack("XY019The quick brown foxKV007Foo Bar");
        assertEquals("The quick brown fox", tm.getTagValue("XY"));
        assertEquals("Foo Bar", tm.getTagValue("KV"));

        String result = tm.pack();
        tm.clear();
        tm.unpack(result);
        assertEquals("The quick brown fox", tm.getTagValue("XY"));
        assertEquals("Foo Bar", tm.getTagValue("KV"));
    }

    @Test
    public void testBuild() {
        CharTagMap tm = instance.withTagSize(3)
                .withLengthSize(2)
                .build();

        tm.unpack("XYZ19The quick brown foxKV107Foo Bar");
        assertEquals("The quick brown fox", tm.getTagValue("XYZ"));
        assertEquals("Foo Bar", tm.getTagValue("KV1"));

        String result = tm.pack();
        tm.clear();
        tm.unpack(result);
        assertEquals("The quick brown fox", tm.getTagValue("XYZ"));
        assertEquals("Foo Bar", tm.getTagValue("KV1"));
    }

    @Test
    public void testBuildSwapped() {
        CharTagMap tm = instance
                .withTagLengthSwap(true)
                .build();

        tm.unpack("019XYThe quick brown fox007KVFoo Bar");
        assertEquals("The quick brown fox", tm.getTagValue("XY"));
        assertEquals("Foo Bar", tm.getTagValue("KV"));

        String result = tm.pack();
        tm.clear();
        tm.unpack(result);
        assertEquals("The quick brown fox", tm.getTagValue("XY"));
        assertEquals("Foo Bar", tm.getTagValue("KV"));
    }

    @Test
    public void testBuildLongTagSize() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.withTagSize(5)
                          .build()
        );
    }

    @Test
    public void testBuildZeroTagSize() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.withTagSize(0)
                          .build()
        );
    }

    @Test
    public void testBuildNegativeTagSize() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.withTagSize(-1)
                          .build()
        );
    }

    @Test
    public void testBuildLongLengthSize() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.withLengthSize(6)
                          .build()
        );
    }

    @Test
    public void testBuildZeroLengthSize() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.withLengthSize(0)
                          .build()
        );
    }

    @Test
    public void testBuildNegativeLengthSize() {
        thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.withLengthSize(-1)
                          .build()
        );
    }

}
