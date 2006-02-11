/*
 * Copyright (c) 2004 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package tlv;

import iso.TestUtils;
import junit.framework.TestCase;


import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;
import org.jpos.tlv.TLVMsg;

/**
 * @author Bharavi
 */
public class TestTLVMsg extends TestCase {
	 byte[] aid1={(byte)0xA0,(byte)0x00,0x00,0x00,(byte)0x96,0x02,0x00};
	 byte[] aid2={(byte)0xA0,(byte)0x00,0x00,0x00,(byte)0x96,0x02,0x01};
	 byte[] aid3={(byte)0xA0,(byte)0x00,0x00,0x00,(byte)0x96,0x02,0x02};
	 byte[] atc={(byte)0x00,(byte)0x01};
     byte[] mac=ISOUtil.hex2byte("0ED701005000522400000000158501AA");
     
	 byte[] data=ISOUtil.hex2byte("8407A00000009602008407A00000009602018407A00000009602029F70020001");
     byte[] dataAtOffset=ISOUtil.hex2byte("000000008407A00000009602008407A00000009602018407A00000009602029F70020001");
     byte[] data2=ISOUtil.hex2byte("0100168407A00000009602008407A00000009602018407A00000009602029F70020001");
     byte[] dataLong=ISOUtil.hex2byte("6481945A13303030353232343030303030303030313538355F3401019F2608C63CEB0B838CE5E09F360200E59F3704045680009F5604000000109F90010B50494E20556E626C6F636B9F6008452278451F42E4697F3B0F9F5D01019F5E080000000000000001580200E09F33030010409F3501119F9002100ED701005000522400000000158501AA9F50054D434849509F510430343030");
     byte[] dataLongTag=ISOUtil.hex2byte("5A13303132333435363738393031323334353637385F3401019F2608C63CEB0B838CE5E09F360200E59F3704045680009F5604000000109F90010B50494E20556E626C6F636B9F6008452278451F42E4697F3B0F9F5D01019F5E080000000000000001580200E09F33030010409F3501119F9002100ED701005000522400000000158501AA9F50054D434849509F510430343030");
     byte[] dataLongAtOffset=ISOUtil.hex2byte("01006481945A13303030353232343030303030303030313538355F3401019F2608C63CEB0B838CE5E09F360200E59F3704045680009F5604000000109F90010B50494E20556E626C6F636B9F6008452278451F42E4697F3B0F9F5D01019F5E080000000000000001580200E09F33030010409F3501119F9002100ED701005000522400000000158501AA9F50054D434849509F510430343030");
     

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {

    }

    public void testUnpack() throws Exception {

		TLVList tlvList=new TLVList();
		tlvList.unpack(data);

		TLVMsg  m1=tlvList.find(0x84);
		TLVMsg  m2=tlvList.findNextTLV();
		TLVMsg  m3=tlvList.findNextTLV();
		TLVMsg  m4=tlvList.find(0x9F70);

        TestUtils.assertEquals(aid1,m1.getValue());
        TestUtils.assertEquals(aid2,m2.getValue());
        TestUtils.assertEquals(aid3,m3.getValue());
        TestUtils.assertEquals(atc, m4.getValue());
    }
    
    public void testUnpackWithOffset() throws Exception {

        TLVList tlvList=new TLVList();
        tlvList.unpack(data2,3);

        TLVMsg  m1=tlvList.find(0x84);
        TLVMsg  m2=tlvList.findNextTLV();
        TLVMsg  m3=tlvList.findNextTLV();
        TLVMsg  m4=tlvList.find(0x9F70);

        TestUtils.assertEquals(aid1,m1.getValue());
        TestUtils.assertEquals(aid2,m2.getValue());
        TestUtils.assertEquals(aid3,m3.getValue());
        TestUtils.assertEquals(atc, m4.getValue());
    }

    public void testPack() {
        TLVList tlv=new TLVList();
        tlv.append(0x84,aid1);
        tlv.append(0x84,aid2);
        tlv.append(0x84,aid3);
        tlv.append(0x9F70,atc);
        TestUtils.assertEquals(data, tlv.pack());
    }
    
    public void testUnpackLong() {
        TLVList tlv = new TLVList();
        try {
            tlv.unpack(dataLong);
        }
        catch (ISOException e) {
            fail("TLVList.unpack should work with long length indicator");
        }

    }
    
    public void testUnpackWithOffsetLong() {
        TLVList tlv = new TLVList();
        try {
            tlv.unpack(dataLongAtOffset,2);
        }
        catch (Exception e) {
            fail("TLVList.unpack should work from an offset and long length indicator");
        }
    }
    
    public void testUnpackLongTag() {
        TLVList tlv = new TLVList();
        try {
            tlv.unpack(dataLongTag);
        }
        catch (ISOException e) {
            fail("TLVList.unpack should work on data with long tag lengths");
        }
        TLVMsg macTLVMsg = tlv.find(0x9F9002);
        assertNotNull(macTLVMsg);
        assertEquals(macTLVMsg.getTag(),0x9f9002);
        TestUtils.assertEquals(mac,macTLVMsg.getValue());
        
        
    }
}