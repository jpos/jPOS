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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.BitSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author joconnor
 */
public class IFB_BITMAPTest 
{
    
    IFB_BITMAP oneByte ;
    IFB_BITMAP eightBytes ;
    IFB_BITMAP sixteenBytes;
    IFB_BITMAP twentyfourBytes;
    byte[] inBytes, eightByteBitMapIn24Bytes, sixteenByteBitMapIn24Bytes, testbytes, outBytes;
    
    @BeforeEach
    public void setUp() {
        oneByte = new IFB_BITMAP(1,"1 byte bitmap");
        eightBytes = new IFB_BITMAP(8,"8 byte bitmap");
        sixteenBytes = new IFB_BITMAP(16,"16 byte bitmap");
        twentyfourBytes = new IFB_BITMAP(24,"24 byte bitmap");
        
        //                                             Next Bitmap?       Next Bitmap?
        //                                             V                  V
        inBytes =                    ISOUtil.hex2byte("8181421FF12418F1"+"8F81421FF12418F1"+"8F81421FF12418F1");
        eightByteBitMapIn24Bytes =   ISOUtil.hex2byte("7181421FF12418F1"+"1881421FF12418F1"+"8F81421FF12418F1");
        sixteenByteBitMapIn24Bytes = ISOUtil.hex2byte("8181421FF12418F1"+"1881421FF12418F1"+"8F81421FF12418F1");

        
    }
    
    @Test
    public void test01ByteBitmap() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = oneByte.unpack(c, inBytes,0);
        assertEquals(1,consumed, "1 byte should be consumed irrespective of 2nd, 3rd or any bitmap indicators");
        assertEquals(8,((BitSet)c.getValue()).length() - 1, "1 byte can only result in a bitmap holding fields up to 8");
        
        outBytes = oneByte.pack(c);
        assertEquals(ISOUtil.hexString(inBytes,0,1),ISOUtil.hexString(outBytes), "1 byte bitmap pack should reflect unpack");
    }
    
    @Disabled("test currently failing - debug action required TODO: CCB")
    @Test public void test08ByteBitmap() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = eightBytes.unpack(c, inBytes,0);
        assertEquals(8,consumed, "8 bytes should be consumed irrespective of 2nd and 3rd bitmap indicators");
        assertEquals(64,((BitSet)c.getValue()).length() - 1, "8 byte can only result in a bitmap holding fields up to 64");
        
        outBytes = eightBytes.pack(c);
        assertEquals(ISOUtil.hexString(inBytes,0,8),ISOUtil.hexString(outBytes), "8 byte bitmap pack should reflect unpack");
        
        try {
            outBytes = oneByte.pack(c);
            fail("Pack of bitmap with fields outside of 1 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 8 in the 1 bytes available.",e.getMessage());
        }
    }
    
    @Test public void test16ByteBitmapWithOnly8BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = sixteenBytes.unpack(c, eightByteBitMapIn24Bytes,0);
        assertEquals(8,consumed, "8 bytes should be consumed as the 2nd bitmap indicator if off");
        assertEquals(64,((BitSet)c.getValue()).length() - 1, "16 byte bitmap with just 8 bytes used should have a maximum field of ");
        
        outBytes = sixteenBytes.pack(c);
        assertEquals(ISOUtil.hexString(eightByteBitMapIn24Bytes,0,8),ISOUtil.hexString(outBytes), "16 Byte (8 bytes used) bitmap pack should reflect unpack");
        
    }
    
    @Disabled("test currently failing - debug action required TODO: CCB")
    @Test public void test16ByteBitmap() throws Exception
    {
        ISOComponent c = new ISOBitMap(1);
        int consumed = sixteenBytes.unpack(c, inBytes,0);
        assertEquals(16,consumed, "16 Bytes should be consumed irrespective of 3rd bitmap indicators");
        assertEquals(128,((BitSet)c.getValue()).length() - 1, "16 byte can only result in a bitmap holding fields up to 128");
        
        outBytes = sixteenBytes.pack(c);
        assertEquals(ISOUtil.hexString(inBytes,0,16),ISOUtil.hexString(outBytes), "16 byte bitmap pack should reflect unpack");
        
        try {
            outBytes = eightBytes.pack(c);
            fail("Pack of bitmap with fields outside of 8 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 64 in the 8 bytes available.",e.getMessage());
        }
        
        try {
            outBytes = oneByte.pack(c);
            fail("Pack of bitmap with fields outside of 1 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 8 in the 1 bytes available.",e.getMessage());
        }
    }
    
    @Test public void test24ByteBitmapWithOnly8BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = twentyfourBytes.unpack(c, eightByteBitMapIn24Bytes,0);
        assertEquals(8,consumed, "8 bytes should be consumed as the 2nd bitmap indicator if off");
        assertEquals(64,((BitSet)c.getValue()).length() - 1, "24 byte bitmap with just 8 bytes used should have a maximum field of ");
        
        outBytes = twentyfourBytes.pack(c);
        assertEquals(ISOUtil.hexString(eightByteBitMapIn24Bytes,0,8),ISOUtil.hexString(outBytes), "24 Byte (8 bytes used) bitmap pack should reflect unpack");
        
    }
    
    @Test
    public void test24ByteBitmapWithOnly16BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = twentyfourBytes.unpack(c, sixteenByteBitMapIn24Bytes,0);
        assertEquals(16,consumed, "16 bytes should be consumed as the 2nd bitmap indicator is on, 3rd is off");
        assertEquals(128,((BitSet)c.getValue()).length() - 1, "24 byte bitmap with just 16 bytes used should have a maximum field of ");
        
        outBytes = twentyfourBytes.pack(c);
        assertEquals(ISOUtil.hexString(sixteenByteBitMapIn24Bytes,0,16),ISOUtil.hexString(outBytes), "24 Byte (16 bytes used) bitmap pack should reflect unpack");
        
    }

    @Disabled("test currently failing - debug action required TODO: CCB")
    @Test public void test24ByteBitmap() throws Exception
    {
        ISOComponent c = new ISOBitMap(1);
        int consumed = twentyfourBytes.unpack(c, inBytes,0);
        assertEquals(24,consumed, "All 24 Bytes should be consumed");
        assertEquals(192,((BitSet)c.getValue()).length() - 1, "24 byte can only result in a bitmap holding fields up to 192");

        outBytes = twentyfourBytes.pack(c);
        assertEquals(ISOUtil.hexString(inBytes,0,24),ISOUtil.hexString(outBytes), "24 byte bitmap pack should reflect unpack");
        
        try {
            outBytes = sixteenBytes.pack(c);
            fail("Pack of bitmap with fields outside of 16 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 128 in the 16 bytes available.",e.getMessage());
        }
        
        try {
            outBytes = eightBytes.pack(c);
            fail("Pack of bitmap with fields outside of 8 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 64 in the 8 bytes available.",e.getMessage());
        }
        
        try {
            outBytes = oneByte.pack(c);
            fail("Pack of bitmap with fields outside of 1 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 8 in the 1 bytes available.",e.getMessage());
        }
    }

    @Test public void testThirdBitmapPack() throws Exception {
        byte[] b = ISOUtil.hex2byte("F23C04800AE00000800000000000010863BC780000000010");
        BitSet bs1 = ISOUtil.byte2BitSet(b, 0, 192);
        ISOBitMap bmap = new ISOBitMap(-1);
        bmap.setValue(bs1);
        IFB_BITMAP ifb = new IFB_BITMAP(24, "BITMAP");
        byte[] b1 = ifb.pack(bmap);
        assertEquals (ISOUtil.hexString(b), ISOUtil.hexString(b1), "Pack should be equal to unpack");
    }
}
