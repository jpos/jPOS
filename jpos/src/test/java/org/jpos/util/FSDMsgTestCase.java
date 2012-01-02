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

package org.jpos.util;

import junit.framework.TestCase;
import org.jpos.iso.FSDISOMsg;
import org.jpos.iso.ISOUtil;

import java.util.Arrays;

public class FSDMsgTestCase extends TestCase {
    private static final String SCHEMA_DIR_URL = "file:target/test-classes/org/jpos/util/";
    FSDMsg imsg;

    FSDMsg omsg;

    public void setUp() throws Exception {
        imsg = new FSDMsg(SCHEMA_DIR_URL + "msg-");
        omsg = new FSDMsg(SCHEMA_DIR_URL + "msg-");
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
        FSDMsg imsg = new FSDMsg(SCHEMA_DIR_URL + "msgDS-");
        FSDMsg omsg = new FSDMsg(SCHEMA_DIR_URL + "msgDS-");
        

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
        FSDMsg imsg = new FSDMsg(SCHEMA_DIR_URL + "DSmsg-");
        FSDMsg omsg = new FSDMsg(SCHEMA_DIR_URL + "DSmsg-");
        

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
        FSDMsg m = new FSDMsg(SCHEMA_DIR_URL + "msgDS-");

    
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
        FSDMsg m0 = new FSDMsg(SCHEMA_DIR_URL + "msgDS-");
        
        m0.set ("alphavardata", "ABCDE");
        FSDMsg m1 = (FSDMsg) m0.clone();
        m1.set ("alphavardata", "12345"); 
        assertEquals ("Original alphavardata", "ABCDE", m0.get ("alphavardata"));
        assertEquals ("Cloned alphavardata", "12345", m1.get ("alphavardata"));

    }
    public void testFSDISOMsgClone () throws Exception {
        FSDMsg m0 = new FSDMsg(SCHEMA_DIR_URL + "msgDS-");
      
        m0.set ("alphavardata", "ABCDE");
        FSDISOMsg iso0 = new FSDISOMsg (m0);
        FSDISOMsg iso1 = (FSDISOMsg) iso0.clone();
        FSDMsg m1 = iso1.getFSDMsg();
        m1.set ("alphavardata", "12345"); 
        assertEquals ("Original alphavardata", "ABCDE", m0.get ("alphavardata"));
        assertEquals ("Cloned alphavardata", "12345", m1.get ("alphavardata"));
    }
    public void testFSDISOMsgPartialCloneAndMerge () throws Exception {
        FSDMsg m0 = new FSDMsg(SCHEMA_DIR_URL + "msgiso-");
        m0.set ("0", "0800");
        m0.set ("11", "000001");
        m0.set ("41", "29110001");
        m0.set ("70", "301");
        FSDISOMsg iso0 = new FSDISOMsg (m0);
        FSDISOMsg iso1 = (FSDISOMsg) iso0.clone(new int[] { 0, 11, 70 });
        FSDMsg m1 = iso1.getFSDMsg();

        assertEquals ("m0.0", "0800", m0.get ("0"));
        assertEquals ("m1.0", "0800", m1.get ("0"));
        m1.set ("0", "0810");
        assertEquals ("m0.0", "0800", m0.get ("0"));
        assertEquals ("m1.0", "0810", m1.get ("0"));
        assertNull ("m1.41 should be null", m1.get ("41"));
        iso1.merge (iso0);
        assertEquals ("m1.41", "29110001", m1.get ("41"));
    }
    public void assertEquals(String msg, byte[] b1, byte[] b2) {
        assertTrue(msg, Arrays.equals(b1, b2));
    }
    
    
    public void testFSDMsgDefaultKey () throws Exception {
        FSDMsg m0 = new FSDMsg(SCHEMA_DIR_URL + "fsd-");
        FSDMsg m1 = new FSDMsg(SCHEMA_DIR_URL + "fsd-");
        
        FSDMsg u0 = new FSDMsg(SCHEMA_DIR_URL + "fsd-");
        FSDMsg u1 = new FSDMsg(SCHEMA_DIR_URL + "fsd-");
        
        m0.set("message-id","03");
        m0.set("x","X");
        m0.set("y","WHYWHY03");
        assertEquals ("Default defined - not used - pack", "000X0300WHYWHY03", m0.pack() );

        m1.set("message-id","99");
        m1.set("x","X");
        m1.set("z","DEFAULT");
        assertEquals ("Default defined - used - pack", "000X99DEFAULT   ", m1.pack() );

        u0.unpack(m0.packToBytes());
        assertEquals ("Default defined - not used - unpack", "00WHYWHY03", u0.get("y") );
        
        u1.unpack(m1.packToBytes());
        assertEquals ("Default defined - used - unpack", "DEFAULT   ", u1.get("z") );

    }
    
    
}
