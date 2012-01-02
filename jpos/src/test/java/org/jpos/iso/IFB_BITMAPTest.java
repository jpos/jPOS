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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.BitSet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
    
    @Before
    public void setUp() {
        oneByte = new IFB_BITMAP(1,"1 byte bitmap");
        eightBytes = new IFB_BITMAP(8,"8 byte bitmap");
        sixteenBytes = new IFB_BITMAP(16,"16 byte bitmap");
        twentyfourBytes = new IFB_BITMAP(24,"24 byte bitmap");
        
        //                                             Next Bitmap?    Next Bitmap?
        //                                             V               V               
        inBytes =                    ISOUtil.hex2byte("8181421FF12418F18F81421FF12418F18F81421FF12418F1");
        eightByteBitMapIn24Bytes =   ISOUtil.hex2byte("7181421FF12418F11881421FF12418F18F81421FF12418F1");
        sixteenByteBitMapIn24Bytes = ISOUtil.hex2byte("8181421FF12418F11881421FF12418F18F81421FF12418F1");

        
    }
    
    @Test public void test01ByteBitmap() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = oneByte.unpack(c, inBytes,0);
        assertEquals("1 byte should be consumed irrespective of 2nd, 3rd or any bitmap indicators",1,consumed);
        assertEquals("1 byte can only result in a bitmap holding fields up to 8",8,((BitSet)c.getValue()).length() - 1);
        
        outBytes = oneByte.pack(c);
        assertEquals("1 byte bitmap pack should reflect unpack",ISOUtil.hexString(inBytes,0,1),ISOUtil.hexString(outBytes));
    }
    
    @Ignore("test currently failing - debug action required TODO: CCB")
    @Test public void test08ByteBitmap() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = eightBytes.unpack(c, inBytes,0);
        assertEquals("8 bytes should be consumed irrespective of 2nd and 3rd bitmap indicators",8,consumed);
        assertEquals("8 byte can only result in a bitmap holding fields up to 64",64,((BitSet)c.getValue()).length() - 1);
        
        outBytes = eightBytes.pack(c);
        assertEquals("8 byte bitmap pack should reflect unpack",ISOUtil.hexString(inBytes,0,8),ISOUtil.hexString(outBytes));
        
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
        assertEquals("8 bytes should be consumed as the 2nd bitmap indicator if off",8,consumed);
        assertEquals("16 byte bitmap with just 8 bytes used should have a maximum field of ",64,((BitSet)c.getValue()).length() - 1);
        
        outBytes = sixteenBytes.pack(c);
        assertEquals("16 Byte (8 bytes used) bitmap pack should reflect unpack",ISOUtil.hexString(eightByteBitMapIn24Bytes,0,8),ISOUtil.hexString(outBytes));
        
    }
    
    @Ignore("test currently failing - debug action required TODO: CCB")
    @Test public void test16ByteBitmap() throws Exception
    {
        ISOComponent c = new ISOBitMap(1);
        int consumed = sixteenBytes.unpack(c, inBytes,0);
        assertEquals("16 Bytes should be consumed irrespective of 3rd bitmap indicators",16,consumed);
        assertEquals("16 byte can only result in a bitmap holding fields up to 128",128,((BitSet)c.getValue()).length() - 1);
        
        outBytes = sixteenBytes.pack(c);
        assertEquals("16 byte bitmap pack should reflect unpack",ISOUtil.hexString(inBytes,0,16),ISOUtil.hexString(outBytes));
        
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
        assertEquals("8 bytes should be consumed as the 2nd bitmap indicator if off",8,consumed);
        assertEquals("24 byte bitmap with just 8 bytes used should have a maximum field of ",64,((BitSet)c.getValue()).length() - 1);
        
        outBytes = twentyfourBytes.pack(c);
        assertEquals("24 Byte (8 bytes used) bitmap pack should reflect unpack",ISOUtil.hexString(eightByteBitMapIn24Bytes,0,8),ISOUtil.hexString(outBytes));
        
    }
    
    @Test public void test24ByteBitmapWithOnly16BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = twentyfourBytes.unpack(c, sixteenByteBitMapIn24Bytes,0);
        assertEquals("16 bytes should be consumed as the 2nd bitmap indicator is on, 3rd is off",16,consumed);
        assertEquals("24 byte bitmap with just 16 bytes used should have a maximum field of ",128,((BitSet)c.getValue()).length() - 1);
        
        outBytes = twentyfourBytes.pack(c);
        assertEquals("24 Byte (16 bytes used) bitmap pack should reflect unpack",ISOUtil.hexString(sixteenByteBitMapIn24Bytes,0,16),ISOUtil.hexString(outBytes));
        
    }

    @Ignore("test currently failing - debug action required TODO: CCB")
    @Test public void test24ByteBitmap() throws Exception
    {
        ISOComponent c = new ISOBitMap(1);
        int consumed = twentyfourBytes.unpack(c, inBytes,0);
        assertEquals("All 24 Bytes should be consumed",24,consumed);
        assertEquals("24 byte can only result in a bitmap holding fields up to 192",192,((BitSet)c.getValue()).length() - 1);

        outBytes = twentyfourBytes.pack(c);
        assertEquals("24 byte bitmap pack should reflect unpack",ISOUtil.hexString(inBytes,0,24),ISOUtil.hexString(outBytes));
        
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

}
