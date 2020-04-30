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

package org.jpos.util;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;

import org.jpos.iso.FSDISOMsg;
import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class FSDMsgTestCase {
    private static final String SCHEMA_DIR_URL = "file:build/resources/test/org/jpos/util/";
    private static final String SCHEMA_JAR_URL = "jar:org/jpos/util/";

    FSDMsg imsg;

    FSDMsg omsg;

    @BeforeEach
    public void setUp() throws Exception {
        imsg = new FSDMsg(SCHEMA_DIR_URL + "msg-");
        omsg = new FSDMsg(SCHEMA_DIR_URL + "msg-");
    }

    @Test
    public void testLeadingBlanks() throws Exception {
        String value = "   123";
        String field = "testafs";
        imsg.set(field, value);
        assertEquals("Leading blanks", ISOUtil.hex2byte("2020203132331C"), imsg
                .pack().getBytes());

        omsg.unpack(imsg.pack().getBytes());

        Assertions.assertEquals(value, omsg.get(field));
    }

    @Test
    public void testTraillingBlanksDroppedwithFS() throws Exception {
        String value = "123   ";
        String field = "testafs";
        imsg.set(field, value);
        Assertions.assertEquals("3132331C", ISOUtil.hexString(imsg
                .pack().getBytes()));

        omsg.unpack(imsg.pack().getBytes());

        Assertions.assertEquals("123", omsg.get(field));
    }

    @Test
    public void testMixedBlanksLeadingArePreserved() throws Exception {
        String value = "  123 ";
        String field = "testafs";
        imsg.set(field, value);
        assertEquals("Mixed blanks", ISOUtil.hex2byte("20203132331C"), imsg
                .pack().getBytes());

        omsg.unpack(imsg.pack().getBytes());

        Assertions.assertEquals("  123", omsg.get(field));
    }

    @Test
    public void testFinalField() throws Exception {
        String value1 = "  123 ";
        String field1 = "testafs";
        String value2 = "ABC";
        String field2 = "finalfield";
        imsg.set(field1, value1);
        imsg.set(field2, value2);
        Assertions.assertEquals("20203132331C414243", ISOUtil.hexString(imsg.pack().getBytes()));

        omsg.unpack(imsg.pack().getBytes());
        Assertions.assertEquals("  123", omsg.get(field1));
        Assertions.assertEquals(value2, omsg.get(field2));
    }

    @Test
    public void testDummySeparatorAlpha() throws Exception {
        FSDMsg imsg = new FSDMsg(SCHEMA_DIR_URL + "msgDS-");
        FSDMsg omsg = new FSDMsg(SCHEMA_DIR_URL + "msgDS-");
        

        String macData = "AbCdEfGh";
        imsg.set("length", "8");
        imsg.set("alphavardata", macData);
        Assertions.assertEquals("0008AbCdEfGh", imsg.pack());

        omsg.unpack(imsg.pack().getBytes());

        Assertions.assertEquals("0008", omsg.get("length"));
        Assertions.assertEquals(macData, omsg.get("alphavardata"));

        macData = "AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGh";
        imsg.set("length", "32");
        imsg.set("alphavardata", macData);
        Assertions.assertEquals("0032AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGh",
                imsg.pack(), "Dummy separator long data");

        omsg.unpack(imsg.pack().getBytes());

        Assertions.assertEquals("0032", omsg.get("length"));
        Assertions.assertEquals(macData, omsg.get("alphavardata"));

        imsg.set("length", "");
        imsg.set("alphavardata", "");
        Assertions.assertEquals("0000", imsg.pack(), "Dummy separator no data");

        imsg.set("length", "40"); // Too long data data will be silently
                                  // truncated,
        // not sure I like this behaviour!
        imsg.set("alphavardata", "AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGhXXXXXXXX");
        Assertions.assertEquals("0040AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGh",
                imsg.pack(), "Dummy separator truncated data");

        omsg.unpack(imsg.pack().getBytes());

        Assertions.assertEquals("0040", omsg.get("length"));
        Assertions.assertEquals("AbCdEfGhAbCdEfGhAbCdEfGhAbCdEfGh", omsg.get("alphavardata"));

    }

    @Test
    public void testDummySeparatorBinary() throws Exception {
        FSDMsg imsg = new FSDMsg(SCHEMA_DIR_URL + "DSmsg-");
        FSDMsg omsg = new FSDMsg(SCHEMA_DIR_URL + "DSmsg-");
        

        String macData = "12345678123456781234567812345678";
        String binaryMacData = new String(ISOUtil.hex2byte(macData), ISOUtil.CHARSET);
        String id = "01";
        String binaryID = new String(ISOUtil.hex2byte(id),ISOUtil.CHARSET);
        
        imsg.set("id", id);
        imsg.set("content", macData);
                
        Assertions.assertEquals(binaryID + binaryMacData, imsg.pack());
        
        omsg.unpack(imsg.pack().getBytes());

        Assertions.assertEquals(id, omsg.get("id"));
        Assertions.assertEquals(macData, omsg.get("content"));

        macData = "1234567812345678123456781234567812345678123456781234567812345678";
        binaryMacData = new String(ISOUtil.hex2byte(macData), ISOUtil.CHARSET);
        imsg.set("id", id);
        imsg.set("content", macData);
        Assertions.assertEquals(binaryID + binaryMacData, imsg
                .pack(), "Dummy separator long data");

        imsg.set("id", id);
        imsg.set("content", "");
        Assertions.assertEquals(binaryID, imsg.pack(), "Dummy separator no data");
        
        omsg.unpack(imsg.pack().getBytes());

        Assertions.assertEquals(id, omsg.get("id"));
        Assertions.assertEquals("", omsg.get("content"));
        
        try {
            macData = "1234567890123456789012345678901234567890123456789012345678901234567890123456789099";

            imsg.set("id", id); 
            imsg.set("content", macData);

            imsg.pack();
            fail("Runtime Exception expected as field has overflowed.");

        } catch (RuntimeException e) {
            // Expected
        }

    }

    @Test
    public void testPackJarSchema() throws Exception {
        FSDMsg fsdm = new FSDMsg(SCHEMA_JAR_URL + "DSmsg-");

        String mac = "12345678123456781234567812345678";
        String packedMAC = new String(ISOUtil.hex2byte(mac), ISOUtil.CHARSET);
        String id = "01";
        String packedID = new String(ISOUtil.hex2byte(id),ISOUtil.CHARSET);

        fsdm.set("id", id);
        fsdm.set("content", mac);
        String s = fsdm.pack();
        Assertions.assertEquals(packedID + packedMAC, s);

    }

    @Test
    public void testLoadMissingDirSchema() throws Exception {
        FSDMsg fsdm = new FSDMsg(SCHEMA_DIR_URL + "DSmsgX-");

        String mac = "12345678123456781234567812345678";
        String id = "01";

        fsdm.set("id", id);
        fsdm.set("content", mac);
        try {
          fsdm.pack();
          fail("FileNotFoundException expected");
        } catch (FileNotFoundException ex) {}
    }

    @Test
    public void testLoadMissingJarSchema() throws Exception {
        FSDMsg fsdm = new FSDMsg(SCHEMA_JAR_URL + "DSmsgX-");

        String mac = "12345678123456781234567812345678";
        String id = "01";

        fsdm.set("id", id);
        fsdm.set("content", mac);
        try {
          fsdm.pack();
          fail("FileNotFoundException expected");
        } catch (FileNotFoundException ex) {}
    }

    @Test
    public void testDummySeparatorNumeric() throws Exception {
        FSDMsg m = new FSDMsg(SCHEMA_DIR_URL + "msgDS-");

    
        String macData = "12345678";
        m.set("length", "8");
        m.set("alphavardata", macData);
        Assertions.assertEquals("000812345678", m.pack());
    
        macData = "12345678123456781234567812345678";
        m.set("length", "32");
        m.set("alphavardata", macData);
        Assertions.assertEquals("003212345678123456781234567812345678",
                m.pack(), "Dummy separator long data");
    
        m.set("length", "");
        m.set("alphavardata", "");
        Assertions.assertEquals("0000", m.pack(), "Dummy separator no data");
    
        m.set("length", "40"); // Too long data data will be silently truncated,
                               // not sure I like this behaviour!
        m.set("alphavardata", "12345678123456781234567812345678XXXXXXXX");
        Assertions.assertEquals("004012345678123456781234567812345678",
                m.pack(), "Dummy separator truncated data");
    
    }
    @Test
    public void testClone () throws Exception {
        FSDMsg m0 = new FSDMsg(SCHEMA_DIR_URL + "msgDS-");
        
        m0.set ("alphavardata", "ABCDE");
        FSDMsg m1 = (FSDMsg) m0.clone();
        m1.set ("alphavardata", "12345"); 
        Assertions.assertEquals("ABCDE", m0.get("alphavardata"), "Original alphavardata");
        Assertions.assertEquals("12345", m1.get("alphavardata"), "Cloned alphavardata");

    }
    @Test
    public void testFSDISOMsgClone () throws Exception {
        FSDMsg m0 = new FSDMsg(SCHEMA_DIR_URL + "msgDS-");
      
        m0.set ("alphavardata", "ABCDE");
        FSDISOMsg iso0 = new FSDISOMsg (m0);
        FSDISOMsg iso1 = (FSDISOMsg) iso0.clone();
        FSDMsg m1 = iso1.getFSDMsg();
        m1.set ("alphavardata", "12345"); 
        Assertions.assertEquals("ABCDE", m0.get("alphavardata"), "Original alphavardata");
        Assertions.assertEquals("12345", m1.get("alphavardata"), "Cloned alphavardata");
    }
    @Test
    public void testFSDISOMsgPartialCloneAndMerge () throws Exception {
        FSDMsg m0 = new FSDMsg(SCHEMA_DIR_URL + "msgiso-");
        m0.set ("0", "0800");
        m0.set ("11", "000001");
        m0.set ("41", "29110001");
        m0.set ("70", "301");
        FSDISOMsg iso0 = new FSDISOMsg (m0);
        FSDISOMsg iso1 = (FSDISOMsg) iso0.clone(new int[] { 0, 11, 70 });
        FSDMsg m1 = iso1.getFSDMsg();

        Assertions.assertEquals("0800", m0.get("0"), "m0.0");
        Assertions.assertEquals("0800", m1.get("0"), "m1.0");
        m1.set ("0", "0810");
        Assertions.assertEquals("0800", m0.get("0"), "m0.0");
        Assertions.assertEquals("0810", m1.get("0"), "m1.0");
        assertNull(m1.get("41"), "m1.41 should be null");
        iso1.merge (iso0);
        Assertions.assertEquals("29110001", m1.get("41"), "m1.41");
    }
    public void assertEquals(String msg, byte[] b1, byte[] b2) {
        assertTrue(Arrays.equals(b1, b2), msg);
    }
    
    
    @Test
    public void testFSDMsgDefaultKey () throws Exception {
        FSDMsg m0 = new FSDMsg(SCHEMA_DIR_URL + "fsd-");
        FSDMsg m1 = new FSDMsg(SCHEMA_DIR_URL + "fsd-");
        
        FSDMsg u0 = new FSDMsg(SCHEMA_DIR_URL + "fsd-");
        FSDMsg u1 = new FSDMsg(SCHEMA_DIR_URL + "fsd-");
        
        m0.set("message-id","03");
        m0.set("x","X");
        m0.set("y","WHYWHY03");
        Assertions.assertEquals("000X0300WHYWHY03", m0.pack(), "Default defined - not used - pack");

        m1.set("message-id","99");
        m1.set("x","X");
        m1.set("z","DEFAULT");
        Assertions.assertEquals("000X99DEFAULT   ", m1.pack(), "Default defined - used - pack");

        u0.unpack(m0.packToBytes());
        Assertions.assertEquals("00WHYWHY03", u0.get("y"), "Default defined - not used - unpack");
        
        u1.unpack(m1.packToBytes());
        Assertions.assertEquals("DEFAULT   ", u1.get("z"), "Default defined - used - unpack");
    }

    @Test
    public void testFSDBinary() throws Exception {
        FSDMsg m0 = new FSDMsg(SCHEMA_DIR_URL + "fsd-");
        FSDMsg u0 = new FSDMsg(SCHEMA_DIR_URL + "fsd-");

        m0.set("message-id","04");
        m0.set("x","X");
        m0.set("y", ISOUtil.hexString("12345678".getBytes()));
        Assertions.assertEquals("000X0412345678", m0.pack());

        u0.unpack(m0.packToBytes());
        Assertions.assertEquals(ISOUtil.hexString("12345678".getBytes()), u0.get("y"));
    }
}
