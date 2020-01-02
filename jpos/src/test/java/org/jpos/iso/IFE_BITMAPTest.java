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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

/**
 * @author ay
 */
public class IFE_BITMAPTest {
    
    IFE_BITMAP oneByte ;
    IFE_BITMAP twoBytes ;
    IFE_BITMAP threeBytes ;
    IFE_BITMAP fourBytes ;
    IFE_BITMAP eightBytes ;
    IFE_BITMAP sixteenBytes ;
    IFE_BITMAP thirtytwoBytes;
    IFE_BITMAP fortyeightBytes;
    byte[] DataWith2ndBitMapBitOn, DataWith2ndBitMapBitOff, thirtytwoByteBitMapIn48Bytes, sixteenByteBitMapIn48Bytes, testbytes, outBytes;
    String in;
    
    @BeforeEach
    public void setUp() {
    	oneByte = new IFE_BITMAP(1,"1 byte bitmap");
        twoBytes = new IFE_BITMAP(2,"2 byte bitmap");
        threeBytes = new IFE_BITMAP(3,"3 byte bitmap");
        fourBytes = new IFE_BITMAP(4,"4 byte bitmap");
        eightBytes = new IFE_BITMAP(8,"8 byte bitmap");
        sixteenBytes = new IFE_BITMAP(16,"16 byte bitmap");
        thirtytwoBytes = new IFE_BITMAP(32,"32 byte bitmap");
        fortyeightBytes = new IFE_BITMAP(48,"48 byte bitmap");
        
        DataWith2ndBitMapBitOn        = ISOUtil.asciiToEbcdic("8F81431FF12458F17F91421FF12418F18F81421FF12418F1".getBytes());
        DataWith2ndBitMapBitOff       = ISOUtil.asciiToEbcdic("7F81431FF12458F17F91421FF12418F18F81421FF12418F1".getBytes());
        thirtytwoByteBitMapIn48Bytes  = ISOUtil.asciiToEbcdic("8F81431FF12458F18F91421FF12418F18F81421FF12418F1".getBytes());
        sixteenByteBitMapIn48Bytes    = ISOUtil.asciiToEbcdic("7F81431FF12458F17F91421FF12418F18F81421FF12418F1".getBytes());
        in = ISOUtil.hexdump(DataWith2ndBitMapBitOn);

        
    }

    @Test
    public void test01ByteBitmapWithDataWith2ndBitMapBitOn() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = oneByte.unpack(c, DataWith2ndBitMapBitOn,0);
        assertEquals(2, consumed, "1 bytes bitmap: unpack of 1 byte bitmap should consume 2 bytes in characters");
        assertEquals(8, ((BitSet) c.getValue()).length() - 1, "1 bytes bitmap: 1 byte bitmap should result in a bitmap holding fields up to 8");

        outBytes = oneByte.pack(c);
        assertEquals(2, outBytes.length, "1 bytes bitmap: pack of 1 byte bitmap must produce a result 2 bytes long");
        assertEquals(ISOUtil.hexString(DataWith2ndBitMapBitOn, 0, 2), ISOUtil.hexString(outBytes), "1 bytes bitmap: pack of unpacked value should be the same as original");
    }
    @Test
    public void test02ByteBitmapWithDataWith2ndBitMapBitOn() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = twoBytes.unpack(c, DataWith2ndBitMapBitOn,0);
        assertEquals(4, consumed, "2 bytes bitmap: unpack of 2 bytew bitmap should consume 4 bytes in characters");
        assertEquals(16, ((BitSet) c.getValue()).length() - 1, "2 bytes bitmap: 2 bytes bitmap should result in a bitmap holding fields up to 16");

        outBytes = twoBytes.pack(c);
        assertEquals(4, outBytes.length, "2 bytes bitmap: pack of 2 byte bitmap must produce a result 4 bytes long");
        assertEquals(ISOUtil.hexString(DataWith2ndBitMapBitOn, 0, 4), ISOUtil.hexString(outBytes), "2 bytes bitmap: pack of unpacked value should be the same as original");
        
        try {
            outBytes = oneByte.pack(c);
            fail("2 bytes bitmap: pack of 16 bits bitmap by 1 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 8 in the 1 bytes available.", e.getMessage());
        }
    }
    @Test
    public void test03ByteBitmapWithDataWith2ndBitMapBitOn() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = threeBytes.unpack(c, DataWith2ndBitMapBitOn,0);
        assertEquals(6, consumed, "3 bytes bitmap: unpack of 3 bytew bitmap should consume 6 bytes in characters");
        assertEquals(24, ((BitSet) c.getValue()).length() - 1, "3 bytes bitmap: 3 bytes bitmap should result in a bitmap holding fields up to 24");

        outBytes = threeBytes.pack(c);
        assertEquals(6, outBytes.length, "3 bytes bitmap:pack of 3 byte bitmap must produce a result 6 bytes long");
        assertEquals(ISOUtil.hexString(DataWith2ndBitMapBitOn, 0, 6), ISOUtil.hexString(outBytes), "3 bytes bitmap: pack of unpacked value should be the same as original");
        
        try {
            outBytes = oneByte.pack(c);
            fail("3 bytes bitmap: pack of 24 bits bitmap by 1 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 8 in the 1 bytes available.", e.getMessage());
        }
        
        try {
            outBytes = twoBytes.pack(c);
            fail("3 bytes bitmap: pack of 24 bits bitmap by 2 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 16 in the 2 bytes available.", e.getMessage());
        }
                
    }    
    @Test
    public void test04ByteBitmapWithDataWith2ndBitMapBitOn() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = fourBytes.unpack(c, DataWith2ndBitMapBitOn,0);
        assertEquals(8, consumed, "4 bytes bitmap: unpack of 4 bytes bitmap should consume 8 bytes in characters");
        assertEquals(32, ((BitSet) c.getValue()).length() - 1, "4 bytes bitmap: 4 bytes bitmap should result in a bitmap holding fields up to 32");

        outBytes = fourBytes.pack(c);
        assertEquals(8, outBytes.length, "4 bytes bitmap: pack of 4 byte bitmap must produce a result 8 bytes long");
        assertEquals(ISOUtil.hexString(DataWith2ndBitMapBitOn, 0, 8), ISOUtil.hexString(outBytes), "4 bytes bitmap: pack of unpacked value should be the same as original");
        
        try {
            outBytes = oneByte.pack(c);
            fail("4 bytes bitmap: pack of 32 bits bitmap by 1 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 8 in the 1 bytes available.", e.getMessage());
        }
        
        try {
            outBytes = twoBytes.pack(c);
            fail("4 bytes bitmap: pack of 32 bits bitmap by 2 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 16 in the 2 bytes available.", e.getMessage());
        }
        
        try {
            outBytes = threeBytes.pack(c);
            fail("4 bytes bitmap: pack of 32 bits bitmap by 3 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 24 in the 3 bytes available.", e.getMessage());
        }
    }
 
    @Test
    public void test08ByteBitmapWithDataWith2ndBitMapBitOn() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = eightBytes.unpack(c, DataWith2ndBitMapBitOn,0);
        assertEquals(16, consumed, "8 bytes bitmap: unpack of 8 bytes bitmap should consume 16 bytes in characters");
        assertEquals(64, ((BitSet) c.getValue()).length() - 1, "8 bytes bitmap: 8 bytes bitmap should result in a bitmap holding fields up to 64");

        outBytes = eightBytes.pack(c);
        assertEquals(16, outBytes.length, "8 bytes bitmap: pack of 8 bytes bitmap must produce a result 16 bytes long");
        assertEquals(ISOUtil.hexString(DataWith2ndBitMapBitOn, 0, 16), ISOUtil.hexString(outBytes), "8 bytes bitmap: pack of upacked value should be the same as original");
        
        try {
            outBytes = oneByte.pack(c);
            fail("8 bytes bitmap: pack of 64 bits bitmap by 1 byte packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 8 in the 1 bytes available.", e.getMessage());
        }
        
        try {
            outBytes = twoBytes.pack(c);
            fail("8 bytes bitmap: pack of 64 bits bitmap by 2 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 16 in the 2 bytes available.", e.getMessage());
        }
        
        try {
            outBytes = threeBytes.pack(c);
            fail("8 bytes bitmap: pack of 64 bits bitmap by 3 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 24 in the 3 bytes available.", e.getMessage());
        }
        
        try {
            outBytes = fourBytes.pack(c);
            fail("8 bytes bitmap: pack of 64 bits bitmap by 4 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 32 in the 4 bytes available.", e.getMessage());
        }
        
    }
    
    @Test
    public void test16ByteBitmapWithDataWith2ndBitMapBitOn() throws Exception
    {
        ISOComponent c = new ISOBitMap(1);
        int consumed = sixteenBytes.unpack(c, DataWith2ndBitMapBitOn, 0);
        assertEquals(32, consumed, "16 bytes bitmap: unpack of 8 bytes bitmap should consume 32 bytes in characters");
        assertEquals(128, ((BitSet) c.getValue()).length() - 1, "16 bytes bitmap: 16 bytes bitmap should result in a bitmap holding fields up to 128");

        outBytes = sixteenBytes.pack(c);
        assertEquals(32, outBytes.length, "16 bytes bitmap: pack of 16 bytes bitmap must produce a result 32 bytes long");
        assertEquals(ISOUtil.hexString(DataWith2ndBitMapBitOn, 0, 32), ISOUtil.hexString(outBytes), "16 bytes bitmap: pack of upacked value should be the same as original");
        
        try {
            outBytes = oneByte.pack(c);
            fail("16 bytes bitmap: pack of 64 bits bitmap by 1 byte packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 8 in the 1 bytes available.", e.getMessage());
        }
        
        try {
            outBytes = twoBytes.pack(c);
            fail("16 bytes bitmap: pack of 64 bits bitmap by 2 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 16 in the 2 bytes available.", e.getMessage());
        }
        
        try {
            outBytes = threeBytes.pack(c);
            fail("16 bytes bitmap: pack of 64 bits bitmap by 3 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 24 in the 3 bytes available.", e.getMessage());
        }
        
        try {
            outBytes = fourBytes.pack(c);
            fail("16 bytes bitmap: pack of 64 bits bitmap by 4 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 32 in the 4 bytes available.", e.getMessage());
        }
        try {
            outBytes = eightBytes.pack(c);
            fail("16 bytes bitmap: pack of 64 bits bitmap by 4 bytes packager should result in ISOException");
        } catch (Exception e) {
            // expected.
            assertEquals("Bitmap can only hold bits numbered up to 64 in the 8 bytes available.", e.getMessage());
        }
    }
    
    @Test
    public void test16ByteBitmapWithDataWith2ndBitMapBitOFF() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = sixteenBytes.unpack(c, DataWith2ndBitMapBitOff,0);
        assertEquals(16, consumed, "16 bytes bitmap with data with 2nd bit map bit off: unpack of 8 bytes bitmap should consume 32 bytes in characters as the 2nd bitmap indicator is off");
        assertEquals(64, ((BitSet) c.getValue()).length() - 1, "16 bytes bitmap with data with 2nd bit map bit off: 16 byte bitmap with 2nd bitmap idicator is off should have a maximum field of 64");
        
        outBytes = sixteenBytes.pack(c);
        assertEquals(16, outBytes.length, "16 bytes bitmap with data with 2nd bit map bit off: pack of 64 bits bitmap with 16 bytes packager must produce a result 16 bytes long");
        assertEquals(ISOUtil.hexString(DataWith2ndBitMapBitOff, 0, 16), ISOUtil.hexString(outBytes), "16 bytes bitmap with data with 2nd bit map bit off: pack of upacked value should be the same as original");
       
    }
/*    
    public void test48ByteBitmap() throws Exception
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
    
    public void test48ByteBitmapWithOnly32BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = fortyeightBytes.unpack(c, thirtytwoByteBitMapIn48Bytes,0);
        assertEquals("32 bytes should be consumed as the 2nd bitmap indicator is on, 3rd is off ",32,consumed);
        assertEquals("48 byte bitmap with just 32 bytes used should have a maximum field of ",128,((BitSet)c.getValue()).length() - 1);
        
        outBytes = fortyeightBytes.pack(c);
        assertEquals("48 Byte (32 bytes used) bitmap pack should reflect unpack",ISOUtil.hexString(thirtytwoByteBitMapIn48Bytes,0,32),ISOUtil.hexString(outBytes));
        
    }
    public void test48ByteBitmapWithOnly16BytesUsed() throws Exception
    {

        ISOComponent c = new ISOBitMap(1);
        int consumed = fortyeightBytes.unpack(c, sixteenByteBitMapIn48Bytes,0);
        assertEquals("16 bytes should be consumed as the 2nd bitmap indicator is off",16,consumed);
        assertEquals("48 byte bitmap with just 16 bytes used should have a maximum field of ",64,((BitSet)c.getValue()).length() - 1);
        
        outBytes = fortyeightBytes.pack(c);
        assertEquals("48 Byte (16 bytes used) bitmap pack should reflect unpack",ISOUtil.hexString(sixteenByteBitMapIn48Bytes,0,16),ISOUtil.hexString(outBytes));
        
    } */

}
