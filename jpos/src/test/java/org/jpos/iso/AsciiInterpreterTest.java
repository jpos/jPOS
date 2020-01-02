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

package org.jpos.iso;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * @author joconnor
 */
public class AsciiInterpreterTest {
    private Interpreter inter;

    /*
	 * @see TestCase#setUp()
	 */
    @BeforeEach
    public void setUp() throws Exception {
        inter = AsciiInterpreter.INSTANCE;
    }

    @Test
    public void testInterpret() throws Exception {
        byte[] b = new byte[3];
        inter.interpret("123", b, 0);
        TestUtils.assertEquals(new byte[] {49, 50, 51}, b);
    }

    @Test
    public void testUninterpret() throws Exception {
        byte[] b = new byte[] {49, 50, 51};
        assertEquals("123", inter.uninterpret(b, 0, 3));
    }

    @Test
    public void testGetPackedLength() throws Exception {
        assertEquals(3, inter.getPackedLength(3));
    }
    
    @Test
    public void testReversability() throws Exception {
        String origin = "Abc123:.-";
        byte[] b = new byte[origin.length()];
        inter.interpret(origin, b, 0);
        
        assertEquals(origin, inter.uninterpret(b, 0, b.length));
    }
}
