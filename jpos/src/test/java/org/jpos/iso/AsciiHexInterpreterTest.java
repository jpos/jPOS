/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import junit.framework.TestCase;


/**
 * @author joconnor
 */
public class AsciiHexInterpreterTest extends TestCase {
    private AsciiHexInterpreter inter;

    /*
	 * @see TestCase#setUp()
	 */
    protected void setUp() throws Exception {
        inter = AsciiHexInterpreter.INSTANCE;
    }

    public void testInterpret() throws Exception {
        byte[] data = new byte[] {(byte)0xFF, (byte)0x12};
        byte[] b = new byte[4];
        inter.interpret(data, b, 0);
        TestUtils.assertEquals(new byte[] {0x46, 0x46, 0x31, 0x32}, b);
    }

    public void testUninterpret() throws Exception {
        byte[] data = new byte[] {(byte)0xFF, (byte)0x12};
        byte[] b = new byte[] {0x46, 0x46, 0x31, 0x32};
        TestUtils.assertEquals(data, inter.uninterpret(b, 0, 2));
    }

    public void testGetPackedLength() {
        assertEquals(6, inter.getPackedLength(3));
    }
    
    public void testReversability() throws Exception {
        byte data[] = new byte[] {0x01, 0x23, 0x45, 0x67, (byte)0x89,
                (byte)0xAB, (byte)0xCD, (byte)0xEF};
        byte[] b = new byte[inter.getPackedLength(data.length)];
        inter.interpret(data, b, 0);
        
        TestUtils.assertEquals(data, inter.uninterpret(b, 0, data.length));
    }
}
