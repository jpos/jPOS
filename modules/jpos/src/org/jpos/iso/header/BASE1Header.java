/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.iso.header;

import org.jpos.iso.ISOUtil;

/*
 * BASE1 Header
 * <pre>
 *   0 hlen;         Fld  1: Header Length        1B      (Byte     0)
 *   1 hformat;      Fld  2: Header Format        8N,bit  (Byte     1)
 *   2 format;       Fld  3: Text Format          1B      (Byte     2)
 *   3 len[2];       Fld  4: Total Message Length 2B      (Byte  3- 4)
 *   5 dstId[3];     Fld  5: Destination Id       6N,BCD  (Byte  5- 7)
 *   8 srcId[3];     Fld  6: Source Id            6N,BCD  (Byte  8-10)
 *  11 rtCtl;        Fld  7: Round-Trip Ctrl Info 8N,bit  (Byte    11)
 *  12 flags[2];     Fld  8: BASE I Flags        16N,bit  (Byte 12-13)
 *  14 status[3];    Fld  9: Message Status Flags 24bits  (Byte 14-16)
 *  17 batchNbr;     Fld 10: Batch Number        1B       (Byte    17)
 *  18 reserved[3];  Fld 11: Reserved            3B       (Byte 18-20)
 *  21 userInfo;     Fld 12: User Info           1B       (Byte    21)
 *  The following fields are only presend in a reject message
 *  22 bitmap;       Fld 13: Bitmap              2B       (Byte 22-23)
 *  24 rejectdata;   Fld 14: Reject Data Group   2B       (Byte 24-25)
 * </pre>
 *
 */
public class BASE1Header extends BaseHeader {

    private static final long serialVersionUID = 6466427524726021374L;
    public static final int LENGTH = 22;

    public BASE1Header() {
        this("000000", "000000");
    }
    public BASE1Header(String source, String destination) {
        super();
        header = new byte[LENGTH];
        header[0] = LENGTH; // hlen
        setHFormat(1);
        setFormat(2);
        setSource(source);
        setDestination(destination);
    }
    public BASE1Header(byte[] header) {
        super(header);
    }
    public int unpack(byte[] header) {
        this.header = header;
        return header.length;
    }
    public int getHLen() {
        return (int) (header[0] & 0xFF);
    }
    public void setHFormat(int hformat) {
        header[1] = (byte) hformat;
    }
    public int getFormat() {
        return (int) (header[2] & 0xFF);
    }
    public void setRtCtl(int i) {
        header[11] = (byte) i;
    }
    public void setFlags(int i) {
        header[12] = (byte) ((i >> 8) & 0xFF);
        header[13] = (byte) (i & 0xFF);
    }
    public void setStatus(int i) {
        header[14] = (byte) ((i >> 16) & 0xFF);
        header[15] = (byte) ((i >> 8) & 0xFF);
        header[16] = (byte) (i & 0xFF);
    }
    public void setBatchNumber(int i) {
        header[17] = (byte) (i & 0xFF);
    }
    public void setFormat(int format) {
        header[2] = (byte) format;
    }
    public void setLen(int len) {
        len += header.length;
        header[3]  = (byte) ((len >> 8) & 0xff);
        header[4]  = (byte) (len        & 0xff);
    }
    public void setDestination(String dest) {
        byte[] d = ISOUtil.str2bcd(dest, true);
        System.arraycopy(d, 0, header, 5, 3);
    }
    public void setSource(String src) {
        byte[] d = ISOUtil.str2bcd(src, true);
        System.arraycopy(d, 0, header, 8, 3);
    }
    public String getSource() {
        return ISOUtil.bcd2str (this.header, 8, 6, false);
    }
    public String getDestination() {
        return ISOUtil.bcd2str (this.header, 5, 6, false);
    }
    public void swapDirection() {
        if (header != null && header.length >= LENGTH) {
            byte[] source = new byte[3];
            System.arraycopy(header, 8, source, 0, 3);
            System.arraycopy(header, 5, header, 8, 3);
            System.arraycopy(source, 0, header, 5, 3);
        }
    }
    public boolean isRejected() {
        // Header length must be 26 or gerater
        // And header field 13 bit 1 must be 1 (field 13 starts at byte 22)
        return (getLength() >= 26) && ((header[22] & 0x80) == 0x80);
    }
	
    /**
     * Gets the BASE 1 Reject Code.
     * 
     * @return If the message is a reject return the Reject Code Otherwise return "" 
     */ 
    public String getRejectCode() {
        return isRejected() ? ISOUtil.bcd2str (this.header, 24, 4, false) : "";
    }
}

