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

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author joconnor
 */
public class IFA_BITMAPTest 
{
    
    IFA_BITMAP twoBytes ;
    IFA_BITMAP sixteenBytes ;
    IFA_BITMAP thirtytwoBytes;
    IFA_BITMAP fortyeightBytes;
    byte[] inBytes, sixteenByteBitMapIn32Bytes, thirtytwoByteBitMapIn48Bytes, sixteenByteBitMapIn48Bytes, testbytes, outBytes;
    String in;
    
    @BeforeEach
    public void setUp() {
        twoBytes = new IFA_BITMAP(2,"2 byte bitmap");
        sixteenBytes = new IFA_BITMAP(16,"16 byte bitmap");
        thirtytwoBytes = new IFA_BITMAP(32,"32 byte bitmap");
        fortyeightBytes = new IFA_BITMAP(48,"48 byte bitmap");
        
        inBytes                       = "8F81421FF12418F18F81421FF12418F18081421FF12418F1".getBytes();
        sixteenByteBitMapIn32Bytes    = "7F81421FF12418F18F81421FF12418F18081421FF12418F1".getBytes();
        thirtytwoByteBitMapIn48Bytes  = "8F81421FF12418F17F81421FF12418F18081421FF12418F1".getBytes();
        sixteenByteBitMapIn48Bytes    = "7F81421FF12418F17F81421FF12418F18081421FF12418F1".getBytes();
        in = ISOUtil.hexdump(inBytes);
        
    }
    
    @Disabled("test currently failing - debug action required TODO: CCB")
    @Test
    public void test02ByteBitmap() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = twoBytes.unpack(c, inBytes,0);
        assertEquals(2,consumed, "2 characters should be consumed irrespective of 2nd, 3rd or any bitmap indicators - actually consumes: "+consumed);
        assertEquals(8,((BitSet)c.getValue()).length() - 1, "2 characters can only result in a bitmap holding fields up to 8");

        outBytes = twoBytes.pack(c);
        assertEquals(2,outBytes.length, "Pack of two bytes must produce a result 2 bytes long");
        assertEquals(ISOUtil.hexString(inBytes,0,2),ISOUtil.hexString(outBytes), "2 byte bitmap pack should reflect unpack");
    }
    
    @Disabled("test currently failing - debug action required TODO: CCB")
    @Test
    public void test16ByteBitmap() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = sixteenBytes.unpack(c, inBytes,0);
        assertEquals(16,consumed, "16 characters should be consumed irrespective of 2nd and 3rd bitmap indicators");
        assertEquals(64,((BitSet)c.getValue()).length() - 1, "16 characters can only result in a bitmap holding fields up to 64");

        outBytes = sixteenBytes.pack(c);
        assertEquals(16,outBytes.length, "Pack of sixteen bytes must produce a result 16 bytes long");
        assertEquals(ISOUtil.hexString(inBytes,0,16),ISOUtil.hexString(outBytes), "16 byte bitmap pack should reflect unpack");
        
        try {
            outBytes = twoBytes.pack(c);
            fail("Pack of bitmap with fields outside of 2 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 8 in the 2 bytes available.",e.getMessage());
        }
    }
    
    @Disabled("test currently failing - debug action required TODO: CCB")
    @Test
    public void test32ByteBitmap() throws Exception
    {
        ISOComponent c = new ISOBitMap(1);
        int consumed = thirtytwoBytes.unpack(c, inBytes,0);
        assertEquals(32,consumed, "32 characters should be consumed irrespective of 3rd bitmap indicators");
        assertEquals(128,((BitSet)c.getValue()).length() - 1, "32 characters can only result in a bitmap holding fields up to 128");

        outBytes = thirtytwoBytes.pack(c);
        assertEquals(32,outBytes.length, "Pack of thirty two bytes must produce a result 32 bytes long");
        assertEquals(ISOUtil.hexString(inBytes,0,32),ISOUtil.hexString(outBytes), "32 byte bitmap pack should reflect unpack");
        
        try {
            outBytes = sixteenBytes.pack(c);
            fail("Pack of bitmap with fields outside of 16 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 64 in the 16 bytes available.",e.getMessage());
        }
        
        try {
            outBytes = twoBytes.pack(c);
            fail("Pack of bitmap with fields outside of 2 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 8 in the 2 bytes available.",e.getMessage());
        }
    }
    
    @Test
    public void test32LByteBitmapWithOnly16BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = thirtytwoBytes.unpack(c, sixteenByteBitMapIn32Bytes,0);
        assertEquals(16,consumed, "16 bytes should be consumed as the 2nd bitmap indicator is off");
        assertEquals(64,((BitSet)c.getValue()).length() - 1, "32 byte bitmap with just 16 bytes used should have a maximum field of ");
        
        outBytes = sixteenBytes.pack(c);
        assertEquals(ISOUtil.hexString(sixteenByteBitMapIn32Bytes,0,16),ISOUtil.hexString(outBytes), "32 Byte (16 bytes used) bitmap pack should reflect unpack");
        
    }
    
    @Disabled("test currently failing - debug action required TODO: CCB")
    @Test
    public void test48ByteBitmap() throws Exception
    {
        ISOComponent c = new ISOBitMap(1);
        int consumed = fortyeightBytes.unpack(c, inBytes,0);
        assertEquals(48,consumed, "All 48 Bytes should be consumed");
        assertEquals(192,((BitSet)c.getValue()).length() - 1, "48 characters can only result in a bitmap holding fields up to 192");

        outBytes = fortyeightBytes.pack(c);
        assertEquals(48,outBytes.length, "Pack of forty eight bytes must produce a result 48 bytes long");
        assertEquals(ISOUtil.hexString(inBytes,0,48),ISOUtil.hexString(outBytes), "48 byte bitmap pack should reflect unpack");
        
        try {
            outBytes = thirtytwoBytes.pack(c);
            fail("Pack of bitmap with fields outside of 32 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 128 in the 32 bytes available.",e.getMessage());
        }
        
        try {
            outBytes = thirtytwoBytes.pack(c);
            fail("Pack of bitmap with fields outside of 32 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 128 in the 32 bytes available.",e.getMessage());
        }
        
        try {
            outBytes = sixteenBytes.pack(c);
            fail("Pack of bitmap with fields outside of 16 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 64 in the 16 bytes available.",e.getMessage());
        }
        
        try {
            outBytes = twoBytes.pack(c);
            fail("Pack of bitmap with fields outside of 2 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 8 in the 2 bytes available.",e.getMessage());
        }
    }
    
    @Disabled("test currently failing - debug action required TODO: CCB")
    @Test
    public void test48ByteBitmapWithOnly32BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = fortyeightBytes.unpack(c, thirtytwoByteBitMapIn48Bytes,0);
        assertEquals(32,consumed, "32 bytes should be consumed as the 2nd bitmap indicator is on, 3rd is off ");
        assertEquals(128,((BitSet)c.getValue()).length() - 1, "48 byte bitmap with just 32 bytes used should have a maximum field of ");
        
        outBytes = fortyeightBytes.pack(c);
        assertEquals(ISOUtil.hexString(thirtytwoByteBitMapIn48Bytes,0,32),ISOUtil.hexString(outBytes), "48 Byte (32 bytes used) bitmap pack should reflect unpack");
        
    }
    @Test
    public void test48ByteBitmapWithOnly16BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = fortyeightBytes.unpack(c, sixteenByteBitMapIn48Bytes,0);
        assertEquals(16,consumed, "16 bytes should be consumed as the 2nd bitmap indicator is off");
        assertEquals(64,((BitSet)c.getValue()).length() - 1, "48 byte bitmap with just 16 bytes used should have a maximum field of ");
        
        outBytes = fortyeightBytes.pack(c);
        assertEquals(ISOUtil.hexString(sixteenByteBitMapIn48Bytes,0,16),ISOUtil.hexString(outBytes), "48 Byte (16 bytes used) bitmap pack should reflect unpack");
        
    }
    @Test
    public void testThirdBitmapPack() throws Exception {
        byte[] b = ISOUtil.hex2byte("F23C04800AE00000"+"8000000000000108"+"63BC780000000010");
        BitSet bs1 = ISOUtil.byte2BitSet(b, 0, 192);
        ISOBitMap bmap = new ISOBitMap(-1);
        bmap.setValue(bs1);
        IFA_BITMAP ifa = new IFA_BITMAP(24, "BITMAP");
        byte[] b1 = ifa.pack(bmap);
        assertEquals (ISOUtil.hexString(b), new String(b1), "Pack should be equal to unpack");
    }
}
