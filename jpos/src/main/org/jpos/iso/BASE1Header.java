package org.jpos.iso;

import org.jpos.iso.*;

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
 * </pre>
 *
 */
public class BASE1Header {
    public static final int LENGTH = 22;
    private byte[] header;

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
        this.header = header;
    }
    public byte[] getBytes() {
        return header;
    }
    public int getHLen() {
        return (int) (header[0] & 0xFF);
    }
    public void setHFormat(int hformat) {
        header[1] = (byte) hformat;
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
        len += LENGTH;
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
    public byte[] getSource() {
        byte[] b = new byte[3];
        System.arraycopy(header, 8, b, 0, 3);
        return b;
    }
    public byte[] getDestination() {
        byte[] b = new byte[3];
        System.arraycopy(header, 5, b, 0, 3);
        return b;
    }

    public void swapDirection() {
        byte[] source = new byte[3];
        System.arraycopy(header, 8, source, 0, 3);
        System.arraycopy(header, 5, header, 8, 3);
        System.arraycopy(source, 0, header, 5, 3);
    }
    public boolean isRejected() {
        return (header[16] & 0x80) == 0x80;
    }
    // first bit of fld 13 of header == 1 && len >=26
    // indica un nuevo header de 22 (Pagina 4-2 Vol-1 VIP System
    // Technical Reference General Requirements
};

