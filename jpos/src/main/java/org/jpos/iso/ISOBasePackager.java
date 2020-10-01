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

import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;

/**
 * provides base functionality for the actual packagers
 *
 * @author apr
 */
@SuppressWarnings ("unused")
public abstract class ISOBasePackager implements ISOPackager, LogSource {
    protected ISOFieldPackager[] fld;
    protected int thirdBitmapField= -999;       // for implementations where the tertiary bitmap is inside a Data Element

    protected Logger logger = null;
    protected boolean logFieldName= true;
    protected String realm = null;
    protected int headerLength = 0;

    public void setFieldPackager (ISOFieldPackager[] fld) {
        this.fld = fld;
    }

    public void setThirdBitmapField(int f) throws ISOException
    {
        if (f < 0 || f > 128)
            throw new ISOException("thirdBitmapField should be >= 0 and <= 128");
        thirdBitmapField= f;
    }
    public int getThirdBitmapField() { return thirdBitmapField; }

    /**
     * @return true if BitMap have to be emited
     */
    protected boolean emitBitMap () {
        return fld[1] instanceof ISOBitMapPackager;
    }

    /**
     * usually 2 for normal fields, 1 for bitmap-less or ANSI X9.2
     * @return first valid field
     */
    protected int getFirstField() {
        if (!(fld[0] instanceof ISOMsgFieldPackager) && fld.length > 1)
            return fld[1] instanceof ISOBitMapPackager ? 2 : 1;
        return 0;
    }

    /**
     * pack method that works in conjunction with {@link #unpack(ISOComponent, byte[])}.
     * <p>
     * Handles a tertiary bitmap possibly appearing in Data Element {@code thirdBitmapField}.<br>
     *
     * @param   m   the Component to pack
     * @return      Message image
     * @exception ISOException
     */
    @Override
    public byte[] pack (ISOComponent m) throws ISOException
    {
        LogEvent evt = null;
        if (logger != null)
            evt = new LogEvent (this, "pack");

        try {
            if (m.getComposite() != m)
                throw new ISOException ("Can't call packager on non Composite");

            ArrayList<byte[]> v = new ArrayList<byte[]>(128);
            byte[] b;
            byte[] hdr= null;
            int len = 0;

            Map fields = m.getChildren();
            ISOComponent c = (ISOComponent) fields.get (0);
            int first = getFirstField();


            // pre-read header, if it exists, and advance total len
            if (m instanceof ISOMsg && headerLength>0)
            {
            	hdr= ((ISOMsg) m).getHeader();
            	if (hdr != null)
            		len += hdr.length;
            }

            if (first > 0 && c != null) {
                b = fld[0].pack(c);
                len += b.length;
                v.add (b);
            }

            BitSet bmap12= null;                            // will store primary and secondary part of bitmap
            BitSet bmap3= null;                             // will store tertiary part of bitmap
            if (emitBitMap())
            {   // The ISOComponent stores a single bitmap in field -1, which could be up to
                // 192 bits long. If we have a thirdBitmapField, we may need to split the full
                // bitmap into 1 & 2 at the beginning (16 bytes), and 3rd inside the Data Element
                c = (ISOComponent) fields.get (-1);
                bmap12= (BitSet)c.getValue();               // the full bitmap (up to 192 bits long)

                if (thirdBitmapField >= 0 &&                // we may need to split it!
                    fld[thirdBitmapField] instanceof ISOBitMapPackager)
                {
                    if (bmap12.length() - 1 > 128)          // some bits are set in the high part (3rd bitmap)
                    {
                        bmap3= bmap12.get(128, 193);        // new bitmap, with the high 3rd bitmap (use 128 as dummy bit0)
                        bmap3.clear(0);                     // don't really need to clear dummy bit0 I guess...
                        bmap12.set(thirdBitmapField);       // indicate presence of field that will hold the 3rd bitmap
                        bmap12.clear(129, 193);             // clear high part, so that the field's pack() method will not use it

                        // Now create add-hoc ISOBitMap in position thirdBitmapField to hold 3rd bitmap
                        ISOBitMap bmField= new ISOBitMap(thirdBitmapField);
                        bmField.setValue(bmap3);
                        m.set(bmField);
                        fields.put(thirdBitmapField, bmField);    // fields is a clone of m's inner map, so we store it here as well

                        // bit65 should only be set if there's a data-containing DE-65 (which should't happen!)
                        bmap12.set(65, fields.get(65) == null ? false : true);
                    }
                    else
                    {   // else: No bits/fields above 128 in this message.
                        // In case there's an old (residual/garbage) field `thirdBitmapField` in the message
                        // we need to clear the bit and the data
                        m.unset(thirdBitmapField);                // remove from ISOMsg
                        bmap12.clear(thirdBitmapField);           // remove from inner bitmap
                        fields.remove(thirdBitmapField);          // remove from fields clone
                    }
                }
                // now will emit the 1st and 2nd bitmaps, and the loop below will take care of 3rd
                // when emitting field `thirdBitmapField`
                b = getBitMapfieldPackager().pack(c);
                len += b.length;
                v.add (b);
            }

            // if Field 1 is a BitMap then we are packing an
            // ISO-8583 message so next field is fld#2.
            // else we are packing an ANSI X9.2 message, first field is 1
            int tmpMaxField=Math.min (m.getMaxField(), (bmap3 != null || fld.length > 129) ? 192 : 128);

            for (int i=first; i<=tmpMaxField; i++) {
                if ((c=(ISOComponent) fields.get (i)) != null)
                {
                    try {
                        ISOFieldPackager fp = fld[i];
                        if (fp == null)
                            throw new ISOException ("null field "+i+" packager");
                        b = fp.pack(c);
                        len += b.length;
                        v.add (b);
                    } catch (ISOException e) {
                        if (evt != null) {
                            evt.addMessage ("error packing field "+i);
                            evt.addMessage (c);
                            evt.addMessage (e);
                        }
                        throw new ISOException("error packing field "+i, e);
                    }
                }
            }

// BBB This part not needed
//            if(m.getMaxField()>128 && fld.length > 128) {
//                for (int i=1; i<=64; i++) {
//                    if ((c = (ISOComponent)fields.get (i + 128)) != null)
//                    {
//                        try {
//                            b = fld[i+128].pack(c);
//                            len += b.length;
//                            v.add (b);
//                        } catch (ISOException e) {
//                            if (evt != null) {
//                                evt.addMessage ("error packing field "+(i+128));
//                                evt.addMessage (c);
//                                evt.addMessage (e);
//                            }
//                            throw e;
//                        }
//                    }
//                }
//            }

            int k = 0;
            byte[] d = new byte[len];

            // if ISOMsg insert header (we pre-read it at the beginning)
            if (hdr != null) {
                System.arraycopy(hdr, 0, d, k, hdr.length);
                k += hdr.length;
            }

            for (byte[] bb : v) {
                System.arraycopy(bb, 0, d, k, bb.length);
                k += bb.length;
            }
            if (evt != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.hexString (d));

            return d;
        } catch (ISOException e) {
            if (evt != null)
                evt.addMessage (e);
            throw e;
        } finally {
            if (evt != null)
                Logger.log(evt);
        }
    }

    /**
     * @param   m   the Container of this message
     * @param   b   ISO message image
     * @return      consumed bytes
     * @exception ISOException
     */
    @Override
    public int unpack (ISOComponent m, byte[] b) throws ISOException {
        LogEvent evt = logger != null ? new LogEvent (this, "unpack") : null;
        int consumed = 0;

        try {
            if (m.getComposite() != m)
                throw new ISOException ("Can't call packager on non Composite");
            if (evt != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.hexString (b));


            // if ISOMsg and headerLength defined
            if (m instanceof ISOMsg /*&& ((ISOMsg) m).getHeader()==null*/ && headerLength>0)
            {
                byte[] h = new byte[headerLength];
                System.arraycopy(b, 0, h, 0, headerLength);
                ((ISOMsg) m).setHeader(h);
                consumed += headerLength;
            }

            if (!(fld[0] == null) && !(fld[0] instanceof ISOBitMapPackager))
            {
                ISOComponent mti = fld[0].createComponent(0);
                consumed  += fld[0].unpack(mti, b, consumed);
                m.set (mti);
            }

            BitSet bmap = null;
            int bmapBytes= 0;                                   // bitmap length in bytes (usually 8, 16, 24)
            int maxField= fld.length - 1;                       // array length counts position 0!

            if (emitBitMap()) {
                ISOBitMap bitmap = new ISOBitMap (-1);
                consumed += getBitMapfieldPackager().unpack(bitmap,b,consumed);
                bmap = (BitSet) bitmap.getValue();
                bmapBytes= (bmap.length()-1 + 63) >> 6 << 3;
                if (evt != null)
                    evt.addMessage ("<bitmap>"+bmap.toString()+"</bitmap>");
                m.set (bitmap);

                maxField = Math.min(maxField, bmap.length()-1); // bmap.length behaves similarly to fld.length
            }

            for (int i= getFirstField(); i <= maxField; i++) {
                try {
                    if (bmap == null && fld[i] == null)
                        continue;

                    // maxField is computed above as min(fld.length-1, bmap.length()-1), therefore
                    // "maxField > 128" means fld[] has packagers defined above 128, *and*
                    // the bitmap's length is greater than 128 (i.e., a contiguous tertiary bitmap exists).
                    // In this case, bit 65 simply indicates a 3rd bitmap contiguous to the 2nd one.
                    // Therefore, there MUST NOT be a DE-65 with data payload to read.
                    if (maxField > 128 && i==65)
                        continue;   // ignore extended bitmap

                    if (bmap == null || bmap.get(i)) {
                        if (fld[i] == null)
                            throw new ISOException ("field packager '" + i + "' is null");

                        ISOComponent c = fld[i].createComponent(i);
                        consumed += fld[i].unpack (c, b, consumed);
                        if (evt != null)
                            fieldUnpackLogger(evt, i, c, fld[i], logFieldName);
                        m.set(c);

                        if (i == thirdBitmapField && fld.length > 129 &&          // fld[128] is at pos 129
                            bmapBytes == 16 &&
                            fld[thirdBitmapField] instanceof ISOBitMapPackager)
                        {   // We have a weird case of tertiary bitmap implemented inside a Data Element
                            // instead of being contiguous to the primary and secondary bitmaps.
                            // If enter this "if" it's because we have a proper 16-byte bitmap (1st & 2nd),
                            // but are expecting more than 128 Data Elements according to fld[].
                            // Normally, these kind of ISO8583 implementations have the tertiary bitmap in DE-65,
                            // but sometimes they specify some other DE (given by thirdBitmapField).
                            // We also double check that the DE has been specified as an ISOBitMapPackager in fld[].
                            // By now, the tertiary bitmap has already been unpacked into field `thirdBitmapField`.
                            BitSet bs3rd= (BitSet)((ISOComponent)m.getChildren().get(thirdBitmapField)).getValue();
                            maxField= 128 + (bs3rd.length() - 1);                 // update loop end condition
                            for (int bit= 1; bit <= 64; bit++)
                                bmap.set(bit+128, bs3rd.get(bit));                // extend bmap with new bits above 128
                        }
                    }
                } catch (ISOException e) {
                    if (evt != null) {
                        evt.addMessage("error unpacking field " + i + " consumed=" + consumed);
                        evt.addMessage(e);
                    }
                    // jPOS-3
                    if (e.getNested() == null) {
                        e = new ISOException(
                            String.format("%s unpacking field=%d, consumed=%d",
                            e.getMessage(), i, consumed)
                        );
                    } else {
                        e = new ISOException(
                            String.format("%s (%s) unpacking field=%d, consumed=%d",
                            e.getMessage(), e.getNested().toString(), i, consumed)
                        );
                    }
                    throw e;
                }
            } // for each field

            if (evt != null && b.length != consumed) {
                evt.addMessage ("WARNING: unpack len=" +b.length +" consumed=" +consumed);
            }

            return consumed;
        } catch (ISOException e) {
            if (evt != null)
                evt.addMessage (e);
            throw e;
        } catch (Exception e) {
            if (evt != null)
                evt.addMessage (e);
            throw new ISOException (e.getMessage() + " consumed=" + consumed);
        } finally {
            if (evt != null)
                Logger.log (evt);
        }
    }

    public void unpack (ISOComponent m, InputStream in)
        throws IOException, ISOException
    {
        LogEvent evt = logger != null ? new LogEvent (this, "unpack") : null;
        try {
            if (m.getComposite() != m)
                throw new ISOException ("Can't call packager on non Composite");

            // if ISOMsg and headerLength defined
            if (m instanceof ISOMsg && ((ISOMsg) m).getHeader()==null && headerLength>0)
            {
            	byte[] h = new byte[headerLength];
            	in.read(h, 0, headerLength);
            	((ISOMsg) m).setHeader(h);
            }


            if (!(fld[0] instanceof ISOMsgFieldPackager) &&
                !(fld[0] instanceof ISOBitMapPackager))
            {
                ISOComponent mti = fld[0].createComponent(0);
                fld[0].unpack(mti, in);
                m.set (mti);
            }

            BitSet bmap = null;
            int maxField = fld.length;
            if (emitBitMap()) {
                ISOBitMap bitmap = new ISOBitMap (-1);
                getBitMapfieldPackager().unpack(bitmap, in);
                bmap = (BitSet) bitmap.getValue();
                if (evt != null)
                    evt.addMessage ("<bitmap>"+bmap.toString()+"</bitmap>");
                m.set (bitmap);
                maxField = Math.min(maxField, bmap.size());
            }

            for (int i=getFirstField(); i<maxField; i++) {
                if (bmap == null && fld[i] == null)
                    continue;

                if (bmap == null || bmap.get(i)) {
                    if (fld[i] == null)
                        throw new ISOException ("field packager '" + i + "' is null");

                    ISOComponent c = fld[i].createComponent(i);
                    fld[i].unpack (c, in);
                    if (evt != null)
                        fieldUnpackLogger(evt, i, c, fld[i], logFieldName);
                    m.set(c);
                }
            }
            if (bmap != null && bmap.get(65) && fld.length > 128 &&
                fld[65] instanceof ISOBitMapPackager)
            {
                bmap= (BitSet) ((ISOComponent) m.getChildren().get(65)).getValue();
                for (int i=1; i<64; i++) {
                    if (bmap == null || bmap.get(i)) {
                        ISOComponent c = fld[i+128].createComponent(i);
                        fld[i+128].unpack (c, in);
                        if (evt != null)
                            fieldUnpackLogger(evt, i+128, c, fld[i+128], logFieldName);
                        m.set(c);
                    }
                }
            }
        } catch (ISOException e) {
            if (evt != null)
                evt.addMessage (e);
            throw e;
        } catch (EOFException e) {
            throw e;
        } catch (Exception e) {
            if (evt != null)
                evt.addMessage (e);
            throw new ISOException (e);
        } finally {
            if (evt != null)
                Logger.log (evt);
        }
    }


    /**
     * Internal helper logging function.
     * Assumes evt is not null.
     */
    private static void fieldUnpackLogger(LogEvent evt, int fldno, ISOComponent c, ISOFieldPackager fld, boolean logFieldName) throws ISOException
    {
        evt.addMessage ("<unpack fld=\""+fldno
            +"\" packager=\""+fld.getClass().getName()+ "\">");

        if (logFieldName)
            evt.addMessage("  <!-- "+fld.getDescription()+" -->");

        if (c.getValue() instanceof ISOMsg)
            evt.addMessage (c.getValue());
        else if (c.getValue() instanceof byte[]) {
            evt.addMessage ("  <value type='binary'>"
                +ISOUtil.hexString((byte[]) c.getValue())
                + "</value>");
        }
        else {
            evt.addMessage ("  <value>"+c.getValue()+"</value>");
        }
        evt.addMessage ("</unpack>");
    }


    /**
     * @param   m   the Container (i.e. an ISOMsg)
     * @param   fldNumber the Field Number
     * @return  Field Description
     */
    public String getFieldDescription(ISOComponent m, int fldNumber) {
        return fld[fldNumber].getDescription();
    }
    /**
     * @param   fldNumber the Field Number
     * @return  Field Packager for this field
     */
    public ISOFieldPackager getFieldPackager (int fldNumber) {
        return fld != null && fldNumber < fld.length ? fld[fldNumber] : null;
    }
    /**
     * @param   fldNumber the Field Number
     * @param   fieldPackager the Field Packager
     */
    public void setFieldPackager
        (int fldNumber, ISOFieldPackager fieldPackager)
    {
        fld[fldNumber] = fieldPackager;
    }
    public ISOMsg createISOMsg () {
        return new ISOMsg();
    }
    /**
     * @return 128 for ISO-8583, should return 64 for ANSI X9.2
     */
    protected int getMaxValidField() {
        return 128;
    }
    /**
     * @return suitable ISOFieldPackager for Bitmap
     */
    protected ISOFieldPackager getBitMapfieldPackager() {
        return fld[1];
    }
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    public String getRealm () {
        return realm;
    }
    public Logger getLogger() {
        return logger;
    }
    public int getHeaderLength ()
    {
    	return headerLength;
    }
    public void setHeaderLength(int len)
    {
    	headerLength = len;
    }
    public String getDescription () {
        return getClass().getName();
    }
}
