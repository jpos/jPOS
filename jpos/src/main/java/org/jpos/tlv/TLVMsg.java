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

package org.jpos.tlv;

import org.jpos.iso.ISOUtil;

/**
 * @author bharavi
 */
public class TLVMsg {

    private int tag;
    protected byte[] value;

    /**
     * empty constructor
     */
    public TLVMsg() {

    }

    /**
     * constructs a TLV Message from tag and value
     * 
     * @param tag 
     * @param value 
     */
    public TLVMsg(int tag, byte[] value) {
        this.tag = tag;
        this.value = value;
    }

    /*
     * @return TAG 
     */
    public int getTag() {
        return tag;
    }

    /*
     * @return value 
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
    public void setValue(byte[] newValue) {
        this.value = newValue;
    }

    /*
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
        int ix = 0;
        int tmp = value.length;
        int bytes = 0;
        // if Length is greater less than 127
        //set the 8bit as 0 indicating next 7 bits is the length
        //of the message
        //if length is more than 127 then, set the first bit as 1 indicating
        //next 7 bits will indicate the length of following bytes used for
        // length

        while (tmp != 0) {
            tmp = tmp >> 8;
            bytes++;
        }

        /* If value can be encoded on one byte */
        if (bytes <= 1 && value.length <= 127) {
            byte[] rBytes = new byte[bytes];
            rBytes[0] = (byte) value.length;
            return rBytes;
        }
        //else {
        /* Value to be encoded on multiple bytes */
        //we need 1 byte to indicate the length
        byte[] rBytes = new byte[1 + bytes];
        rBytes[0] = (byte) (0x80 | bytes);

        int mask = 0xFF;
        tmp = value.length;
        while (ix < bytes) {
            /* Mask off 8 bits of the value at a time */
            rBytes[(bytes - ix)] = (byte) (tmp & mask);
            bytes--;
            /* Shift value right 8 bits, effectively removing them */
            tmp = (tmp >> 8);
        }
        //}
        return rBytes;
    }
    
    /*
     * @return value 
     */
    public String getStringValue() {
        return ISOUtil.hexString(value);
    }
}

