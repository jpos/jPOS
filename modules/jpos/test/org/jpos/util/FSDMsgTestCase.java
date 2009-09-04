/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2009 Alejandro P. Revilla
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

import java.util.Arrays;

import junit.framework.TestCase;

import org.jpos.iso.FSDISOMsg;
import org.jpos.iso.ISOUtil;

public class FSDMsgTestCase extends TestCase {
    FSDMsg imsg;

    FSDMsg omsg;

    public void setUp() throws Exception {
        // Eclipse wants:-
//        imsg = new FSDMsg("file:../jpos6/modules/jpos/test/org/jpos/util/msg-");
//        omsg = new FSDMsg("file:../jpos6/modules/jpos/test/org/jpos/util/msg-");
        // Original
        imsg = new FSDMsg("file:../modules/jpos/test/org/jpos/util/msg-");
        omsg = new FSDMsg("file:../modules/jpos/test/org/jpos/util/msg-");
    }

    public void testLeadingBlanks() throws Exception {
        String value = "   123";
        String field = "testafs";
        imsg.set(field, value);
        assertEquals("Leading blanks", ISOUtil.hex2byte("2020203132331C"), imsg
                .pack().getBytes());

        omsg.unpack(imsg.pack().getBytes());

        assertEquals(value, omsg.get(field));
    }

    public void testTraillingBlanksDroppedwithFS() throws Exception {
        String value = "123   ";
        String field = "testafs";
        imsg.set(field, value);
        assertEquals("3132331C", ISOUtil.hexString(imsg
                .pack().getBytes()));

        omsg.unpack(imsg.pack().getBytes());

        assertEquals("123", omsg.get(field));
    }

    public void testMixedBlanksLeadingArePreserved() throws Exception {
        String value = "  123 ";
        String field = "testafs";
        imsg.set(field, value);
        assertEquals("Mixed blanks", ISOUtil.hex2byte("20203132331C"), imsg
                .pack().getBytes());

        omsg.unpack(imsg.pack().getBytes());

        assertEquals("  123", omsg.get(field));
    }

    public void testFinalField() throws Exception {
        String value1 = "  123 ";
        String field1 = "testafs";
        String value2 = "ABC";
        String field2 = "finalfield";
        imsg.set(field1, value1);
        imsg.set(field2, value2);
        assertEquals("20203132331C414243",ISOUtil.hexString(imsg.pack().getBytes()));

        omsg.unpack(imsg.pack().getBytes());
        assertEquals("  123", omsg.get(field1));
        assertEquals(value2, omsg.get(field2));
    }

    public void testDummySeparatorAlpha() throws Exception {
    
        // Eclipse wants:-
//        FSDMsg imsg = new FSDMsg("file:../jpos6/modules/jpos/test/org/jpos/util/msgDS-");
//        FSDMsg omsg = new FSDMsg("file:../jpos6/modules/jpos/test/org/jpos/util/msgDS-");
        // Original
        FSDMsg imsg = new FSDMsg("file:../test/org/jpos/util/msgDS-");
        FSDMsg omsg = new FSDMsg("file:../test/org/jpos/util/msgDS-");
        

        String macData = "AbCdEfGh";
        imsg.set("length", "8");
        imsg.set("alphavardata", macData);
        assertEquals("0008AbCdEfGh",imsg.pack());

        omsg.unpack(imsg.pack().getBytes());

        assertEquals("0008", omsg.get("length"));
        assertEquals(macData, omsg.get("alphavardata"));

        macData = "AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGh";
        imsg.set("length", "32");
        imsg.set("alphavardata", macData);
        assertEquals("Dummy separator long data",
                "0032AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGh", imsg.pack());

        omsg.unpack(imsg.pack().getBytes());

        assertEquals("0032", omsg.get("length"));
        assertEquals(macData, omsg.get("alphavardata"));

        imsg.set("length", "");
        imsg.set("alphavardata", "");
        assertEquals("Dummy separator no data", "0000", imsg.pack());

        imsg.set("length", "40"); // Too long data data will be silently
                                  // truncated,
        // not sure I like this behaviour!
        imsg.set("alphavardata", "AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGhXXXXXXXX");
        assertEquals("Dummy separator truncated data",
                "0040AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGh", imsg.pack());

        omsg.unpack(imsg.pack().getBytes());

        assertEquals("0040", omsg.get("length"));
        assertEquals("AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGh", omsg.get("alphavardata"));

    }

    public void testDummySeparatorBinary() throws Exception {
        // Eclipse wants:-
//        FSDMsg imsg = new FSDMsg("file:../jpos6/modules/jpos/test/org/jpos/util/DSmsg-");
//        FSDMsg omsg = new FSDMsg("file:../jpos6/modules/jpos/test/org/jpos/util/DSmsg-");
        // Original
        FSDMsg imsg = new FSDMsg("file:../test/org/jpos/util/DSmsg-");
        FSDMsg omsg = new FSDMsg("file:../test/org/jpos/util/DSmsg-");
        

        String macData = "12345678123456781234567812345678";
        String binaryMacData = new String(ISOUtil.hex2byte(macData),
                "ISO8859_1");
        String id = "01";
        String binaryID = new String(ISOUtil.hex2byte(id),"ISO8859_1");
        
        imsg.set("id", id);
        imsg.set("content", macData);
                
        assertEquals(binaryID + binaryMacData, imsg.pack());
        
        omsg.unpack(imsg.pack().getBytes());

        assertEquals(id, omsg.get("id"));
        assertEquals(macData, omsg.get("content"));

        macData = "1234567812345678123456781234567812345678123456781234567812345678";
        binaryMacData = new String(ISOUtil.hex2byte(macData), "ISO8859_1");
        imsg.set("id", id);
        imsg.set("content", macData);
        assertEquals("Dummy separator long data", binaryID + binaryMacData, imsg
                .pack());

        imsg.set("id", id);
        imsg.set("content", "");
        assertEquals("Dummy separator no data", binaryID, imsg.pack());
        
        omsg.unpack(imsg.pack().getBytes());

        assertEquals(id, omsg.get("id"));
        assertEquals("", omsg.get("content"));
        
        try {
            macData = "1234567890123456789012345678901234567890123456789012345678901234567890123456789099";

            binaryMacData = new String(ISOUtil.hex2byte(macData), "ISO8859_1");
            imsg.set("id", id); 
            imsg.set("content", macData);

            imsg.pack();
            fail("Runtime Exception expected as field has overflowed.");

        } catch (RuntimeException e) {
            // Expected
        }

    }

    
    public void testDummySeparatorNumeric() throws Exception {
        // Eclipse wants:-
//        FSDMsg m = new FSDMsg("file:../jpos6/modules/jpos/test/org/jpos/util/msgDS-");
        // Original
        FSDMsg m = new FSDMsg("file:../test/org/jpos/util/msgDS-");

    
        String macData = "12345678";
        m.set("length", "8");
        m.set("alphavardata", macData);
        assertEquals("000812345678", m.pack());
    
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
    public void testClone () throws Exception {
        // Eclipse wants:-
//        FSDMsg m0 = new FSDMsg("file:../jpos6/modules/jpos/test/org/jpos/util/msgDS-");
        // Original
        FSDMsg m0 = new FSDMsg("file:../test/org/jpos/util/msgDS-");
        
        m0.set ("alphavardata", "ABCDE");
        FSDMsg m1 = (FSDMsg) m0.clone();
        m1.set ("alphavardata", "12345"); 
        assertEquals ("Original alphavardata", "ABCDE", m0.get ("alphavardata"));
        assertEquals ("Cloned alphavardata", "12345", m1.get ("alphavardata"));

    }
    public void testFSDISOMsgClone () throws Exception {
        // Eclipse wants:-
//        FSDMsg m0 = new FSDMsg("file:../jpos6/modules/jpos/test/org/jpos/util/msgDS-");
        // Original
        FSDMsg m0 = new FSDMsg("file:../test/org/jpos/util/msgDS-");
      
        m0.set ("alphavardata", "ABCDE");
        FSDISOMsg iso0 = new FSDISOMsg (m0);
        FSDISOMsg iso1 = (FSDISOMsg) iso0.clone();
        FSDMsg m1 = iso1.getFSDMsg();
        m1.set ("alphavardata", "12345"); 
        assertEquals ("Original alphavardata", "ABCDE", m0.get ("alphavardata"));
        assertEquals ("Cloned alphavardata", "12345", m1.get ("alphavardata"));
    }
    public void assertEquals(String msg, byte[] b1, byte[] b2) {
        assertTrue(msg, Arrays.equals(b1, b2));
    }
}
