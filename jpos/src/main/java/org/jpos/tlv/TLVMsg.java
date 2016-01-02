/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2016 Alejandro P. Revilla
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

package org.jpos.tlv;

import java.io.PrintStream;
import java.math.BigInteger;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;

/**
 * @author bharavi
 */
public class TLVMsg implements Loggeable {
    private int tag;
    protected byte[] value;

    /**
     * empty constructor
     */
    public TLVMsg() {
        super();
    }

    /**
     * constructs a TLV Message from tag and value
     * 
     * @param tag id
     * @param value tag value
     */
    public TLVMsg(int tag, byte[] value) {
        this.tag = tag;
        this.value = value;
    }

    /**
     * @return tag
     */
    public int getTag() {
        return tag;
    }

    /**
     * @return tag value
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * @param tag of TLV Message
     */
    public void setTag(int tag) {
        this.tag = tag;
    }

    /**
     * @param value of TLV Message
     */
    public void setValue(byte[] value) {
        this.value = value;
    }

    /**
     * @return tag + length + value of the TLV Message
     */
    public byte[] getTLV() {
        String hexVal = Integer.toHexString(tag);
        byte[] bTag = ISOUtil.hex2byte(hexVal);
        byte[] bLen = getL();
        if (value != null) {
            int tLength = bTag.length + bLen.length + value.length;
            byte[] out = new byte[tLength];
            System.arraycopy(bTag, 0, out, 0, bTag.length);
            System.arraycopy(bLen, 0, out, bTag.length, bLen.length);
            System.arraycopy(value, 0, out, bTag.length + bLen.length,
                    value.length);
            return out;
        } else {//Length can be 0
            int tLength = bTag.length + bLen.length;
            byte[] out = new byte[tLength];
            System.arraycopy(bTag, 0, out, 0, bTag.length);
            System.arraycopy(bLen, 0, out, bTag.length, bLen.length);
            return out;

        }
    }

    /**
     * Value up to 127 can be encoded in single byte and multiple bytes are
     * required for length bigger than 127
     * 
     * @return encoded length
     */
    public byte[] getL() {

        if (value == null)
            return new byte[1];

        // if Length is less than 128
        // set the 8bit as 0 indicating next 7 bits is the length
        // of the message
        // if length is more than 127 then, set the first bit as 1 indicating
        // next 7 bits will indicate the length of following bytes used for
        // length

        BigInteger bi = BigInteger.valueOf(value.length);
        /* Value to be encoded on multiple bytes */
        byte[] rBytes = bi.toByteArray();
        /* If value can be encoded on one byte */
        if (value.length < 0x80)
          return rBytes;

        //we need 1 byte to indicate the length
        //for that is used sign byte (first 8-bits equals 0),
        //if it is not present it is added
        if ( rBytes[0] > 0 )
          rBytes = ISOUtil.concat(new byte[1], rBytes);
        rBytes[0] = (byte) (0x80 | rBytes.length-1);

        return rBytes;
    }
    
    /**
     * @return value 
     */
    public String getStringValue() {
        return ISOUtil.hexString(value);
    }

    @Override
    public String toString(){
        String t = Integer.toHexString(tag);
        if (t.length() % 2 > 0)
          t = "0"+t;
        return String.format("[tag: 0x%s, %s]", t
                ,value==null?null:getStringValue());
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.print (indent);
        p.print ("<tag id='");
        p.print (Integer.toHexString(getTag()));
        p.print ("' value='");
        p.print (ISOUtil.hexString(getValue()));
        p.println ("' />");
    }
}
