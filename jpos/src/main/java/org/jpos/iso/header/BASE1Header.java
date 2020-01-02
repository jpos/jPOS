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
    public BASE1Header(String source, String destination, int format) {
        super();
        header = new byte[LENGTH];
        header[0] = LENGTH; // hlen
        setHFormat(1);
        setFormat(format);
        setSource(source);
        setDestination(destination);
    }
    public BASE1Header(byte[] header) {
        super(header);
    }

    public int getHLen() {
        return header[0] & 0xFF;
    }
    public void setHFormat(int hformat) {
        header[1] = (byte) hformat;
    }
    public int getFormat() {
        return header[2] & 0xFF;
    }
    public void setRtCtl(int i) {
        header[11] = (byte) i;
    }
    public void setFlags(int i) {
        header[12] = (byte) (i >> 8 & 0xFF);
        header[13] = (byte) (i & 0xFF);
    }
    public void setStatus(int i) {
        header[14] = (byte) (i >> 16 & 0xFF);
        header[15] = (byte) (i >> 8 & 0xFF);
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
        header[3]  = (byte) (len >> 8 & 0xff);
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
        return getLength() >= 26 && (header[22] & 0x80) == 0x80;
    }
	
    /**
     * Gets the BASE 1 Reject Code.
     * 
     * @return If the message is a reject return the Reject Code Otherwise return "" 
     */ 
    public String getRejectCode() {
        return isRejected() ? ISOUtil.bcd2str (this.header, 24, 4, false) : "";
    }

    /*
     * parse header contributed by santhoshvee@yahoo.co.uk in jpos-dev mailing list
     */
     public String formatHeader() {
         String h = ISOUtil.hexString(this.header);
         String lf = System.getProperty("line.separator");
         StringBuffer d = new StringBuffer();
         d.append(lf);
         d.append("[H 01] "); d.append(h.substring(0, 2));   d.append(lf);
         d.append("[H 02] "); d.append(h.substring(2, 4));   d.append(lf);
         d.append("[H 03] "); d.append(h.substring(4, 6));   d.append(lf);
         d.append("[H 04] "); d.append(h.substring(6, 10));  d.append(lf);
         d.append("[H 05] "); d.append(h.substring(10, 16)); d.append(lf);
         d.append("[H 06] "); d.append(h.substring(16, 22)); d.append(lf);
         d.append("[H 07] "); d.append(h.substring(22, 24)); d.append(lf);
         d.append("[H 08] "); d.append(h.substring(24, 28)); d.append(lf);
         d.append("[H 09] "); d.append(h.substring(28, 34)); d.append(lf);
         d.append("[H 10] "); d.append(h.substring(34, 36)); d.append(lf);
         d.append("[H 11] "); d.append(h.substring(36, 42)); d.append(lf);
         d.append("[H 12] "); d.append(h.substring(42, 44)); d.append(lf);
         if (isRejected()) {
             d.append("[H 13] "); d.append(h.substring(44, 46)); d.append(lf);
             d.append("[H 14] "); d.append(h.substring(46, 48)); d.append(lf);
             
         }
         return d.toString();
     }

}
