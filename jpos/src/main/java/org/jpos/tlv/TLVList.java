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

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author bharavi
 */

public class TLVList implements Serializable, Loggeable {

    private static final long serialVersionUID = 6962311407331957465L;
    private List<TLVMsg> tags = new ArrayList<TLVMsg>();
    private int tagToFind = 0;
    private int indexLastOccurrence = -1;

    public TLVList() {
        super();
    }

    /**
     * unpack a message
     * @param buf - raw message
     */
    public void unpack(byte[] buf) throws ISOException {
        unpack(buf, 0);
    }

    /**
     * @return a list of tags.
     */
    public List<TLVMsg> getTags() {
        return tags;
    }

    /**
     * @return an enumeration of the List of tags.
     */
    public Enumeration<TLVMsg> elements() {
        return Collections.enumeration(tags);
    }

    /**
     * unpack a message with a starting offset
     * @param buf - raw message
     * @param offset theoffset
     * @throws org.jpos.iso.ISOException
     */
    public void unpack(byte[] buf, int offset) throws ISOException {
        ByteBuffer buffer=ByteBuffer.wrap(buf,offset,buf.length-offset);
        TLVMsg currentNode;
        while (hasNext(buffer)) {    
            currentNode = getTLVMsg(buffer);    // null is returned if no tag found (trailing padding)
            if (currentNode != null)
                append(currentNode);
        }
    }

    /**
     * Append TLVMsg to the TLVList
     */
    public void append(TLVMsg tlvToAppend) {
        tags.add(tlvToAppend);
    }
    
    /**
     * Append TLVMsg to the TLVList
     * @param tag tag id
     * @param value tag value
     */
    public void append(int tag, byte[] value) {
        append(new TLVMsg(tag, value));
    }
    
    /**
     * Append TLVMsg to the TLVList
     * @param tag id
     * @param value in hexadecimal character representation
     */
    public void append(int tag, String value) {
        append(new TLVMsg(tag, ISOUtil.hex2byte(value)));
    }

    /**
     * delete the specified TLV from the list using a Zero based index
     * @param index number
     */
    public void deleteByIndex(int index) {
        tags.remove(index);
    }

    /**
     * Delete the specified TLV from the list by tag value
     * @param tag id
     */
    public void deleteByTag(int tag) {
        List<TLVMsg> t = new ArrayList<TLVMsg>();
        for (TLVMsg tlv2 :tags ) {
            if (tlv2.getTag() == tag)
                t.add(tlv2);
        }
        tags.removeAll(t);
    }

    /**
     * searches the list for a specified tag and returns a TLV object
     * @param tag id
     * @return TLVMsg
     */
    public TLVMsg find(int tag) {
        tagToFind = tag;
        for (TLVMsg tlv :tags ) {
            if (tlv.getTag() == tag) {
                indexLastOccurrence = tags.indexOf(tlv);
                return tlv;
            }
        }
        indexLastOccurrence = -1;
        return null;
    }

    /**
     * searches the list for a specified tag and returns a zero based index for
     * that tag
     * @return index for a given {2code tag}
     */
    public int findIndex(int tag) {
        tagToFind = tag;
        for (TLVMsg tlv :tags ) {
            if (tlv.getTag() == tag) {
                indexLastOccurrence = tags.indexOf(tlv);
                return indexLastOccurrence;
            }
        }
        indexLastOccurrence = -1;
        return -1;
    }
    
    /**
     * Return the next TLVMsg of same TAG value
     * @return TLVMsg (return null if not found)
     */
    public TLVMsg findNextTLV() {

        for ( int i=indexLastOccurrence + 1 ; i < tags.size(); i++) {
            if (tags.get(i).getTag() == tagToFind) {
                indexLastOccurrence = i;
                return tags.get(i);
            }
        }
        return null;
    }

    /**
     * Returns a TLV object which represents the TLVMsg stored within the TLVList
     * at the given index
     * @param index number
     * @return TLVMsg
     */
    public TLVMsg index(int index) {
        return tags.get(index);
    }

    /**
     * pack the TLV message (BER-TLV Encoding)
     * @return the packed message
     */
    public byte[] pack() {
        ByteBuffer buffer=ByteBuffer.allocate(516);
        for ( TLVMsg tlv : tags)
          buffer.put(tlv.getTLV());
        byte[] b=new byte[buffer.position()];
        buffer.flip();
        buffer.get(b);
        return b;
 
    }

    /**
     * Read next TLV Message from stream and return it 
     * @param buffer the buffer
     * @return TLVMsg
     */
    private TLVMsg getTLVMsg(ByteBuffer buffer) throws ISOException {
        int tag = getTAG(buffer);  // tag = 0 if tag not found
        if (tag ==0)
            return null;

        // Get Length if buffer remains!
        if (!buffer.hasRemaining())
            throw new ISOException(String.format("BAD TLV FORMAT - tag (%x)"
                    + " without length or value",tag));

        int length = getValueLength(buffer);
        if(length >buffer.remaining())
            throw new ISOException(String.format("BAD TLV FORMAT - tag (%x)"
                    + " length (%d) exceeds available data.", tag, length));

        byte[] arrValue= new byte[length];
        buffer.get(arrValue);

        return getTLVMsg(tag, arrValue);
    }
   
    protected TLVMsg getTLVMsg(int tag, byte[] arrValue) {
        return new TLVMsg(tag,arrValue);
    }

    /**
     * Check Existance of next TLV Field
     * @param buffer  ByteBuffer containing TLV data
     */
    private  boolean hasNext(ByteBuffer buffer) {
        return buffer.hasRemaining();
    }
    
    /**
     * Return the next TAG
     * @return tag
     */
    private int getTAG(ByteBuffer buffer) {
        int b;
        int tag;
        b = buffer.get() & 0xff;
        // Skip padding chars
        if (b == 0xFF || b == 0x00) {
            do {
                if (hasNext(buffer)) {
                    b = buffer.get() & 0xff;
                } else {
                    break;
                }    
            } while (b == 0xFF || b == 0x00);
        }
        // Get first byte of Tag Identifier
        tag = b;
        // Get rest of Tag identifier if required
        if ((b & 0x1F) == 0x1F) {
            do {
                tag <<= 8;
                b = buffer.get();
                tag |= b & 0xFF;
                
            } while ((b & 0x80) == 0x80);
        }
        return tag;
    }
    
    /**
     * Read length bytes and return the int value
     * @param buffer buffer
     * @return value length
     */
    protected int getValueLength(ByteBuffer buffer) {
        byte b = buffer.get();
        int count = b & 0x7f;
        // check first byte for more bytes to follow
        if ((b & 0x80) == 0 || count == 0)
            return count;

        //fetch rest of bytes
        byte[] bb = new byte[count];
        buffer.get(bb);
        //adjust buffer if first bit is turn on
        //important for BigInteger reprsentation
        if ( (bb[0] & 0x80) > 0 )
            bb = ISOUtil.concat(new byte[1], bb);
        return new BigInteger(bb).intValue();
    }
    
    /**
     * searches the list for a specified tag and returns a hex String
     * @param tag id
     * @return hexString  
     */
    public String getString(int tag) {
        TLVMsg msg = find(tag);
        if (msg != null) {
            return msg.getStringValue();
        }
        else {
            return null;
        }
    }
    
    /**
     * searches the list for a specified tag and returns it raw
     * @param tag id
     * @return byte[]  
     */
    public byte[] getValue(int tag) {
        TLVMsg msg = find(tag);
        if (msg != null) {
            return msg.getValue();
        }
        else {
            return null;
        }
    }
    
    /**
     * searches the list for a specified tag and returns a boolean indicating presence
     * @return boolean
     */
    public boolean hasTag(int tag) {
        return findIndex(tag) > -1;
    }

    @Override
    public void dump(PrintStream p, String indent) {
        String inner = indent + "   ";
        p.println (indent + "<tlvlist>");
        for (TLVMsg msg : getTags())
            msg.dump (p, inner);
        p.println (indent + "</tlvlist>");
    }
}
