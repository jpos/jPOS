/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.tlv;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Vector;

import org.jpos.iso.ISOException;

/**
 * @author bharavi
 */
public class TLVList {

    private Vector tags = new Vector();
    private int tagToFind = 0;
    private int indexLastOccurrence = -1;

    /**
     * empty constructor
     */
    public TLVList() {
     
       
    }

    /**
     * unpack a message
     * @param buf - raw message
     */
    public void unpack(byte[] buf) throws ISOException {
        ByteBuffer buffer=ByteBuffer.wrap(buf);
        TLVMsg currentNode;
        while (hasNext(buffer)) {    
            currentNode = getTLVMsg(buffer);    // null is returned if no tag found (trailing padding)
            if (currentNode != null) {
                append(currentNode);
            }
        }
    }
    
    /**
     * return an enumeration of the Vector of tags.
     */
    public Enumeration elements() {
        return tags.elements();
    }
    
    /**
     * unpack a message with a starting offset
     * @param buf - raw message
     */
    public void unpack(byte[] buf, int offset) throws ISOException {
        ByteBuffer buffer=ByteBuffer.wrap(buf,offset,buf.length-offset);
        TLVMsg currentNode;
        while (hasNext(buffer)) {    
            currentNode = getTLVMsg(buffer);
            append(currentNode);
        }
    }

    /**
     * Append TLVMsg to the TLVList
     * @param TLVMsg
     */
    public void append(TLVMsg tlvToAppend) {
        tags.add(tlvToAppend);
    }
    
    /**
     * Append TLVMsg to the TLVList
     * @param TAG
     * @param value
     */
    public void append(int tag, byte[] value) {
        append(new TLVMsg(tag, value));
    }

    /*
     *delete the specified TLV from the list using a Zero based index
     *@param index 
     */
    public void deleteByIndex(int index) {
        tags.remove(index);
    }

    /*
     * Delete the specified TLV from the list by tag value
     * @param TAG
     */
    public void deleteByTag(int tag) {
        int i = 0;
        TLVMsg tlv2;

        while (i < tags.size()) {
            tlv2 = (TLVMsg) (tags.elementAt(i));
            if (tlv2.getTag() == tag) {
                tags.removeElement(tlv2);
            } else
                i++;
        }
       }

    /*
     *searches the list for a specified tag and returns a TLV object
     *@return TLVMsg  
     */
    public TLVMsg find(int tag) {
        int i = 0;
        tagToFind = tag;
        while (i < tags.size()) {
            TLVMsg tlv = (TLVMsg) (tags.elementAt(i));
            if (tlv.getTag() == tag) {
                indexLastOccurrence = i;
                return (tlv);
            }
            i++;
        }
        indexLastOccurrence = -1;
        return null;
    }

    /*
     * searches the list for a specified tag and returns a zero based index for
     * that TAG
     * @return index for a given TAG
     */
    public int findIndex(int tag) {
        int i = 0;
        tagToFind = tag;
        while (i < tags.size()) {
            TLVMsg tlv = (TLVMsg) (tags.elementAt(i));
            if (tlv.getTag() == tag) {
                indexLastOccurrence = i;
                return i;
            }
            i++;
        }
        indexLastOccurrence = -1;
        return -1;
    }
    
    /*
     * Return the next TLVMsg of same TAG value
     * @return TLVMsg (return null if not found)
     */
    public TLVMsg findNextTLV() {
        int i = indexLastOccurrence + 1;

        while (i < tags.size()) {
            TLVMsg tlv = (TLVMsg) (tags.elementAt(i));
            if (tlv.getTag() == tagToFind) {
                indexLastOccurrence = i;
                return (tlv);
            }
            i++;
        }
        return null;
    }

    /*
     *Returns a TLV object which represents the TLVMsg stored within the TLVList
     *at the given index
     *@return TLVMsg 
     */
    public TLVMsg index(int index) {
        return (TLVMsg) tags.get(index);
    }

    /**
     * pack the TLV message (BER-TLV Encoding)
     * @return the packed message
     */
    public byte[] pack() {
        int i = 0;
        TLVMsg tlv;
        ByteBuffer buffer=ByteBuffer.allocate(400);
        while(i<tags.size()) {
            tlv=(TLVMsg)(tags.elementAt(i));
            buffer.put(tlv.getTLV());
            i++;
        }
        byte[] b=new byte[buffer.position()];
        buffer.flip();
        buffer.get(b);
        return b;
 
    }

    /*
     * Read next TLV Message from stream and return it 
     *@return TLVMsg 
     */
    private TLVMsg getTLVMsg(ByteBuffer buffer) throws ISOException {
        int tag = getTAG(buffer);  // tag = 0 if tag not found
        if (tag !=0) {
            byte[] arrValue=null;
            // Get Length if buffer remains!
            if (!buffer.hasRemaining()) {
                throw new ISOException("BAD TLV FORMAT - tag ("+Integer.toHexString(tag)+") without length or value");
            }
            else {
                int length = getValueLength(buffer);
               if(length >buffer.remaining()) throw new ISOException("BAD TLV FORMAT - tag ("+Integer.toHexString(tag)+") length ("+length+") exceeds available data.");
        
                if(length>0) {
                    arrValue= new byte[length];
                    buffer.get(arrValue);
                }
    
                TLVMsg tlv = new TLVMsg(tag, arrValue);
                return tlv;
            }
        }
        else {
            return null;
        }
    }
   
    /*
     * Check Existance of next TLV Field
     * @param buffer  ByteBuffer containing TLV data
     */
    private  boolean hasNext(ByteBuffer buffer) {
              return buffer.hasRemaining();
    }
    
    /* 
     * Return the next TAG
     * @return tag
     */
    private int getTAG(ByteBuffer buffer) {
        int b;
        int tag = 0x000000;
        b = buffer.get() & 0xff;
        // Skip padding chars
        if (b == 0xFF || b == 0x00) {
            do {
                b = buffer.get() & 0xff;    
            } while ((b == 0xFF || b == 0x00) && hasNext(buffer));
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
    
    /*
     * Read length bytes and return the int value
     * @return value length
     */
    private int getValueLength(ByteBuffer buffer) {
        int length = 0;
        int count = 0;
        byte b;
        b = buffer.get();
        count = b & 0xFF;
        if ((count & 0x80) == 0x80) {
            // check first byte for more bytes to follow
            count -= 0x80;
            for (length = 0; count > 0; count--) {
                length <<= 8;
                b = buffer.get();
                length |= b & 0xFF;
            }
        } else {
            length = count;
        }
        return length;
    }
      
     
}