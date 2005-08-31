/*
 * Copyright (c) 2004 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package tlv;

import iso.TestUtils;
import junit.framework.TestCase;


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
	 byte[] data=ISOUtil.hex2byte("8407A00000009602008407A00000009602018407A00000009602029F70020001");

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

    public void testPack() {
        TLVList tlv=new TLVList();
        tlv.append(0x84,aid1);
        tlv.append(0x84,aid2);
        tlv.append(0x84,aid3);
        tlv.append(0x9F70,atc);
        TestUtils.assertEquals(data, tlv.pack());
    }
}