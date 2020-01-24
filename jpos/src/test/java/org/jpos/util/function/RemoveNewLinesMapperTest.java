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
package org.jpos.util.function;

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RemoveNewLinesMapperTest {
    private final byte[] UNIX = new byte[]{'\n'};
    private final byte[] WINDOWS = new byte[]{'\r', '\n'};

    @Test
    void testRemovalWithoutSpaceConsolidation() {
        RemoveNewLinesMapper mapper = new RemoveNewLinesMapper();
        mapper.NEWLINE_SEPARATORS = UNIX;
        mapper.newLineAtEnd = false;
        byte[] b = {'\n', '\n', '\n', ' ', ' ', ' ', 'a', 'b', '\n', 'c', ' ', 'd', ' ', '\n', ' ', ' '};
        byte[] r = mapper.apply(b);
        assertArrayEquals(new byte[]{' ', ' ', ' ', 'a', 'b', 'c', ' ', 'd', ' ', ' ', ' '}, r);
        b = new byte[]{'\r', '\n', '\r', '\n', ' ', ' ', ' ', 'a', 'b', '\r', '\n', 'c', ' ', 'd', ' ', '\r', '\n', ' ', ' '};

        mapper.NEWLINE_SEPARATORS = WINDOWS;
        r = mapper.apply(b);
        assertArrayEquals(new byte[]{' ', ' ', ' ', 'a', 'b', 'c', ' ', 'd', ' ', ' ', ' '}, r);
    }

    @Test
    void testRemovalWithSpaceConsolidation() {
        RemoveNewLinesMapper mapper = new RemoveNewLinesMapper();
        mapper.NEWLINE_SEPARATORS = UNIX;
        mapper.newLineAtEnd = false;
        mapper.combineSpaces = true;
        byte[] b = { ' ', ' ', ' ', 'a', ' ', ' ', 'b', '\n', 'c', ' ', 'd', ' ', '\n', ' ', ' '};
        byte[] r = mapper.apply(b);
        assertArrayEquals(new byte[]{' ', 'a', ' ', 'b', 'c', ' ', 'd', ' ', ' '}, r);

        mapper.NEWLINE_SEPARATORS = WINDOWS;
        b = new byte[] {' ', ' ', ' ', 'a', ' ', ' ', 'b', '\r', '\n', 'c', ' ', 'd', ' ', '\r', '\n', ' ', ' '};
        r = mapper.apply(b);
        assertArrayEquals(new byte[]{' ', 'a', ' ', 'b', 'c', ' ', 'd', ' ', ' '}, r);
    }

    @Test
    void testValuesChangedByConfiguration() throws ConfigurationException {
        RemoveNewLinesMapper mapper = new RemoveNewLinesMapper();
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("combine-spaces", "true");
        cfg.put("newline-at-end", "false");
        mapper.setConfiguration(cfg);
        assertTrue(mapper.combineSpaces);
        assertFalse(mapper.newLineAtEnd);
    }

    @Test
    void testDefaultValues() throws ConfigurationException {
        RemoveNewLinesMapper mapper = new RemoveNewLinesMapper();
        assertFalse(mapper.combineSpaces);
        assertTrue(mapper.newLineAtEnd);
        SimpleConfiguration cfg = new SimpleConfiguration();
        mapper.setConfiguration(cfg);
        assertFalse(mapper.combineSpaces);
        assertTrue(mapper.newLineAtEnd);
    }

    @Test
    void testAddNewLineAtEnd() {
        RemoveNewLinesMapper mapper = new RemoveNewLinesMapper();
        mapper.NEWLINE_SEPARATORS = UNIX;
        mapper.newLineAtEnd = true;
        byte[] b = {'a', 'b', '\n', 'c', 'd'};
        byte[] r = mapper.apply(b);
        assertArrayEquals(new byte[]{'a','b','c','d','\n'}, r);
        byte[] c = {'a', 'b', '\n', 'c', '\n'};
        r = mapper.apply(c);
        assertArrayEquals(new byte[]{'a', 'b', 'c', '\n'}, r);

        mapper.NEWLINE_SEPARATORS = WINDOWS;
        b = new byte[]{'a', 'b', '\r','\n', 'c', 'd'};
        r = mapper.apply(b);
        assertArrayEquals(new byte[]{'a','b','c','d','\r','\n'}, r);
        c = new byte[]{'a', 'b', '\r', '\n', 'c', '\r', '\n'};
        r = mapper.apply(c);
        assertArrayEquals(new byte[]{'a', 'b', 'c', '\r', '\n'}, r);
    }
}
