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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author: Nigel Smith (nsmith at moneyswitch.net)
 */
public class SignedEbcdicNumberInterpreterTest {

    private SignedEbcdicNumberInterpreter signedEbcdicNumberInterpreter;
    
    @BeforeEach
    public void setUp() throws Exception {
        signedEbcdicNumberInterpreter = new SignedEbcdicNumberInterpreter();
    }
    
    @Test
    public void testUninterpretNegative() throws Exception {
        byte[] rawData = new byte[] { (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0xf1, (byte) 0xf3, (byte) 0xf5, (byte) 0xf7, (byte) 0xf8, (byte) 0xd6, (byte) 0x12, (byte) 0x9a };
        int offset = 3;
        int length = 6;
        String expectedString = "-135786";
        
        assertEquals(expectedString, signedEbcdicNumberInterpreter.uninterpret(rawData, offset, length));
    }

    @Test
    public void testUninterpretPositive() throws Exception {
        byte[] rawData = new byte[] { (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0xf1, (byte) 0xf3, (byte) 0xf5, (byte) 0xf7, (byte) 0xf8, (byte) 0xc6, (byte) 0x12, (byte) 0x9a };
        int offset = 3;
        int length = 6;
        String expectedString = "135786";
        
        assertEquals(expectedString, signedEbcdicNumberInterpreter.uninterpret(rawData, offset, length));
    }
    
    @Test
    public void testUninterpretUnsigned() throws Exception {
        byte[] rawData = new byte[] { (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0xf1, (byte) 0xf3, (byte) 0xf5, (byte) 0xf7, (byte) 0xf8, (byte) 0xf6, (byte) 0x12, (byte) 0x9a };
        int offset = 3;
        int length = 6;
        String expectedString = "135786";
        
        assertEquals(expectedString, signedEbcdicNumberInterpreter.uninterpret(rawData, offset, length));
    }
    
    @Test
    public void testInterpretNegative() throws Exception {
        byte[] expectedRawData = new byte[] { (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0xf1, (byte) 0xf3, (byte) 0xf5, (byte) 0xf7, (byte) 0xf8, (byte) 0xd6, (byte) 0x12, (byte) 0x9a };
        byte[] rawData = new byte[] { (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x12, (byte) 0x9a };
        int offset = 3;
        String string = "-135786";
        
        signedEbcdicNumberInterpreter.interpret(string, rawData, offset);
        assertTrue(Arrays.equals(expectedRawData, rawData), "Expected " + ISOUtil.hexdump(expectedRawData) + " but was " + ISOUtil.hexdump(rawData));
    }
    
    @Test
    public void testInterpretPositive() throws Exception {
        byte[] expectedRawData = new byte[] { (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0xf1, (byte) 0xf3, (byte) 0xf5, (byte) 0xf7, (byte) 0xf8, (byte) 0xf6, (byte) 0x12, (byte) 0x9a };
        byte[] rawData = new byte[] { (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x12, (byte) 0x9a };
        int offset = 3;
        String string = "135786";
        
        signedEbcdicNumberInterpreter.interpret(string, rawData, offset);
        assertTrue(Arrays.equals(expectedRawData, rawData), "Expected " + ISOUtil.hexdump(expectedRawData) + " but was " + ISOUtil.hexdump(rawData));
    }
}

