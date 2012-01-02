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

import org.junit.Ignore;
import static java.lang.String.format;
import static org.mockito.BDDMockito.*;
import static org.junit.Assume.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.mockito.*;
import org.mockito.runners.*;
import org.junit.*;
import org.junit.runner.*;
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
    
    @Before
    public void setUp() {
        twoBytes = new IFA_BITMAP(2,"2 byte bitmap");
        sixteenBytes = new IFA_BITMAP(16,"16 byte bitmap");
        thirtytwoBytes = new IFA_BITMAP(32,"32 byte bitmap");
        fortyeightBytes = new IFA_BITMAP(48,"48 byte bitmap");
        
        inBytes                       = "8F81421FF12418F18F81421FF12418F18F81421FF12418F1".getBytes();
        sixteenByteBitMapIn32Bytes    = "7F81421FF12418F18F81421FF12418F18F81421FF12418F1".getBytes();
        thirtytwoByteBitMapIn48Bytes  = "8F81421FF12418F17F81421FF12418F18F81421FF12418F1".getBytes();
        sixteenByteBitMapIn48Bytes    = "7F81421FF12418F17F81421FF12418F18F81421FF12418F1".getBytes();
        in = ISOUtil.hexdump(inBytes);
        
    }
    
    @Ignore("test currently failing - debug action required TODO: CCB")
    @Test public void test02ByteBitmap() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = twoBytes.unpack(c, inBytes,0);
        assertEquals("2 characters should be consumed irrespective of 2nd, 3rd or any bitmap indicators - actually consumes: "+consumed,2,consumed);
        assertEquals("2 characters can only result in a bitmap holding fields up to 8",8,((BitSet)c.getValue()).length() - 1);

        outBytes = twoBytes.pack(c);
        assertEquals("Pack of two bytes must produce a result 2 bytes long",2,outBytes.length);
        assertEquals("2 byte bitmap pack should reflect unpack", ISOUtil.hexString(inBytes,0,2),ISOUtil.hexString(outBytes));
    }
    
    @Ignore("test currently failing - debug action required TODO: CCB")
    @Test public void test16ByteBitmap() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = sixteenBytes.unpack(c, inBytes,0);
        assertEquals("16 characters should be consumed irrespective of 2nd and 3rd bitmap indicators",16,consumed);
        assertEquals("16 characters can only result in a bitmap holding fields up to 64",64,((BitSet)c.getValue()).length() - 1);

        outBytes = sixteenBytes.pack(c);
        assertEquals("Pack of sixteen bytes must produce a result 16 bytes long",16,outBytes.length);
        assertEquals("16 byte bitmap pack should reflect unpack", ISOUtil.hexString(inBytes,0,16),ISOUtil.hexString(outBytes));
        
        try {
            outBytes = twoBytes.pack(c);
            fail("Pack of bitmap with fields outside of 2 byte range should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold fields numbered up to 8 in the 2 bytes available.",e.getMessage());
        }
    }
    
    @Ignore("test currently failing - debug action required TODO: CCB")
    @Test public void test32ByteBitmap() throws Exception
    {
        ISOComponent c = new ISOBitMap(1);
        int consumed = thirtytwoBytes.unpack(c, inBytes,0);
        assertEquals("32 characters should be consumed irrespective of 3rd bitmap indicators",32,consumed);
        assertEquals("32 characters can only result in a bitmap holding fields up to 128",128,((BitSet)c.getValue()).length() - 1);

        outBytes = thirtytwoBytes.pack(c);
        assertEquals("Pack of thirty two bytes must produce a result 32 bytes long",32,outBytes.length);
        assertEquals("32 byte bitmap pack should reflect unpack",ISOUtil.hexString(inBytes,0,32),ISOUtil.hexString(outBytes));
        
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
    
    @Test public void test32LByteBitmapWithOnly16BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = thirtytwoBytes.unpack(c, sixteenByteBitMapIn32Bytes,0);
        assertEquals("16 bytes should be consumed as the 2nd bitmap indicator is off",16,consumed);
        assertEquals("32 byte bitmap with just 16 bytes used should have a maximum field of ",64,((BitSet)c.getValue()).length() - 1);
        
        outBytes = sixteenBytes.pack(c);
        assertEquals("32 Byte (16 bytes used) bitmap pack should reflect unpack",ISOUtil.hexString(sixteenByteBitMapIn32Bytes,0,16),ISOUtil.hexString(outBytes));
        
    }
    
    @Ignore("test currently failing - debug action required TODO: CCB")
    @Test public void test48ByteBitmap() throws Exception
    {
        ISOComponent c = new ISOBitMap(1);
        int consumed = fortyeightBytes.unpack(c, inBytes,0);
        assertEquals("All 48 Bytes should be consumed",48,consumed);
        assertEquals("48 characters can only result in a bitmap holding fields up to 192",192,((BitSet)c.getValue()).length() - 1);

        outBytes = fortyeightBytes.pack(c);
        assertEquals("Pack of forty eight bytes must produce a result 48 bytes long",48,outBytes.length);
        assertEquals("48 byte bitmap pack should reflect unpack",ISOUtil.hexString(inBytes,0,48),ISOUtil.hexString(outBytes));
        
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
    
    @Ignore("test currently failing - debug action required TODO: CCB")
    @Test public void test48ByteBitmapWithOnly32BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = fortyeightBytes.unpack(c, thirtytwoByteBitMapIn48Bytes,0);
        assertEquals("32 bytes should be consumed as the 2nd bitmap indicator is on, 3rd is off ",32,consumed);
        assertEquals("48 byte bitmap with just 32 bytes used should have a maximum field of ",128,((BitSet)c.getValue()).length() - 1);
        
        outBytes = fortyeightBytes.pack(c);
        assertEquals("48 Byte (32 bytes used) bitmap pack should reflect unpack",ISOUtil.hexString(thirtytwoByteBitMapIn48Bytes,0,32),ISOUtil.hexString(outBytes));
        
    }
    @Test public void test48ByteBitmapWithOnly16BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = fortyeightBytes.unpack(c, sixteenByteBitMapIn48Bytes,0);
        assertEquals("16 bytes should be consumed as the 2nd bitmap indicator is off",16,consumed);
        assertEquals("48 byte bitmap with just 16 bytes used should have a maximum field of ",64,((BitSet)c.getValue()).length() - 1);
        
        outBytes = fortyeightBytes.pack(c);
        assertEquals("48 Byte (16 bytes used) bitmap pack should reflect unpack",ISOUtil.hexString(sixteenByteBitMapIn48Bytes,0,16),ISOUtil.hexString(outBytes));
        
    }

}
