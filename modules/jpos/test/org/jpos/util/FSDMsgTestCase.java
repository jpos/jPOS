/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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

package org.jpos.util;

import org.jpos.iso.ISOUtil;
import java.util.Arrays;
import junit.framework.*;

public class FSDMsgTestCase extends TestCase {
    FSDMsg msg;

    public void setUp() throws Exception {
        msg = new FSDMsg("file:../test/org/jpos/util/msg-");
    }

    public void testLeadingBlanks() throws Exception {
        msg.set("testafs", "   123");
        assertEquals("Leading blanks", ISOUtil.hex2byte("2020203132331C"), msg
                .pack().getBytes());
    }

    public void testTraillingBlanks() throws Exception {
        msg.set("testafs", "123   ");
        assertEquals("Trailing blanks", ISOUtil.hex2byte("3132331C"), msg
                .pack().getBytes());
    }

    public void testMixedBlanks() throws Exception {
        msg.set("testafs", "  123 ");
        assertEquals("Mixed blanks", ISOUtil.hex2byte("20203132331C"), msg
                .pack().getBytes());
    }

    public void testFinalField() throws Exception {
        msg.set("testafs", "  123 ");
        msg.set("finalfield", "ABC");
        assertEquals("Final Field", ISOUtil.hex2byte("20203132331C414243"), msg
                .pack().getBytes());

        FSDMsg m = new FSDMsg("file:../test/org/jpos/util/msg-");
        m.unpack(ISOUtil.hex2byte("20203132331C414243"));
        assertEquals("Final Field", ISOUtil.hex2byte("20203132331C414243"), m
                .pack().getBytes());
    }

    public void testDummySeparatorAlpha() throws Exception {
        FSDMsg m = new FSDMsg("file:../test/org/jpos/util/msgDS-");
        
        String macData = "AbCdEfGh";
        m.set("length", "8");
        m.set("alphavardata", macData);
        assertEquals("Dummy separator 1", "0008AbCdEfGh", m.pack());

        macData = "AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGh";
        m.set("length", "32");
        m.set("alphavardata", macData);
        assertEquals("Dummy separator long data",
                "0032AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGh", m.pack());

        m.set("length", "");
        m.set("alphavardata", "");
        assertEquals("Dummy separator no data", "0000", m.pack());

        m.set("length", "40"); // Too long data data will be silently truncated,
                               // not sure I like this behaviour!
        m.set("alphavardata", "AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGhXXXXXXXX");
        assertEquals("Dummy separator truncated data",
                "0040AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGh", m.pack());

    }

    public void testDummySeparatorBinary() throws Exception {
        FSDMsg m = new FSDMsg("file:../test/org/jpos/util/msgDS-");
        
        String macData = "12345678123456781234567812345678";
        String binaryMacData = new String(ISOUtil.hex2byte(macData),
                "ISO8859_1");
        m.set("length", "8");
        m.set("binaryvardata", macData);
        assertEquals("Dummy separator 1", "0008" + binaryMacData, m.pack());

        macData = "1234567812345678123456781234567812345678123456781234567812345678";
        binaryMacData = new String(ISOUtil.hex2byte(macData), "ISO8859_1");
        m.set("length", "32");
        m.set("binaryvardata", macData);
        assertEquals("Dummy separator long data", "0032" + binaryMacData, m
                .pack());

        m.set("length", "");
        m.set("binaryvardata", "");
        assertEquals("Dummy separator no data", "0000", m.pack());

        try {
            macData = "123456781234567812345678123456781234567812345678123456781234567812345678XXXXXXXX";

            binaryMacData = new String(ISOUtil.hex2byte(macData), "ISO8859_1");
            m.set("length", "40"); // Too long data data will be silently
                                   // truncated, not sure I like this behaviour!
            m.set("binaryvardata", macData);

            m.pack();
            fail("Runtime Exception expected as field has overflowed.");
            
        } catch (RuntimeException e) {
            // Expected
        }

    }
    
    public void testDummySeparatorNumeric() throws Exception {
        FSDMsg m = new FSDMsg("file:../test/org/jpos/util/msgDS-");
    
        String macData = "12345678";
        m.set("length", "8");
        m.set("alphavardata", macData);
        assertEquals("Dummy separator 1", "000812345678", m.pack());
    
        macData = "12345678123456781234567812345678";
        m.set("length", "32");
        m.set("alphavardata", macData);
        assertEquals("Dummy separator long data",
                "003212345678123456781234567812345678", m.pack());
    
        m.set("length", "");
        m.set("alphavardata", "");
        assertEquals("Dummy separator no data", "0000", m.pack());
    
        m.set("length", "40"); // Too long data data will be silently truncated,
                               // not sure I like this behaviour!
        m.set("alphavardata", "12345678123456781234567812345678XXXXXXXX");
        assertEquals("Dummy separator truncated data",
                "004012345678123456781234567812345678", m.pack());
    
    }

    public void assertEquals(String msg, byte[] b1, byte[] b2) {
        assertTrue(msg, Arrays.equals(b1, b2));
    }

    
}
