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

    protected Logger logger = null;
    protected String realm = null;
    protected int headerLength = 0;
    
    public void setFieldPackager (ISOFieldPackager[] fld) {
        this.fld = fld;
    }
    /**
     * @return true if BitMap have to be emited
     */
    protected boolean emitBitMap () {
        return (fld[1] instanceof ISOBitMapPackager);
    }
    /**
     * usually 2 for normal fields, 1 for bitmap-less
     * or ANSI X9.2 
     * @return first valid field
     */
    protected int getFirstField() {
        if ((!(fld[0] instanceof ISOMsgFieldPackager) ) && fld.length > 1)
            return (fld[1] instanceof ISOBitMapPackager) ? 2 : 1;
        return 0;
    }
    /**
     * @param   m   the Component to pack
     * @return      Message image
     * @exception ISOException
     */
    public byte[] pack (ISOComponent m) throws ISOException {
        LogEvent evt = null;
        if (logger != null)
            evt = new LogEvent (this, "pack");
        try {
            if (m.getComposite() != m) 
                throw new ISOException ("Can't call packager on non Composite");

            ISOComponent c;
            ArrayList<byte[]> v = new ArrayList<byte[]>(128);
            Map fields = m.getChildren();
            int len = 0;
            int first = getFirstField();

            c = (ISOComponent) fields.get (0);
            byte[] b;

            if (m instanceof ISOMsg && headerLength>0) 
            {
            	byte[] h = ((ISOMsg) m).getHeader();
            	if (h != null) 
            		len += h.length;
            }
            
            if (first > 0 && c != null) {
                b = fld[0].pack(c);
                len += b.length;
                v.add (b);
            }

            if (emitBitMap()) {
                // BITMAP (-1 in HashTable)
                c = (ISOComponent) fields.get (-1);
                b = getBitMapfieldPackager().pack(c);
                len += b.length;
                v.add (b);
            }

            // if Field 1 is a BitMap then we are packing an
            // ISO-8583 message so next field is fld#2.
            // else we are packing an ANSI X9.2 message, first field is 1
            int tmpMaxField=Math.min (m.getMaxField(), 128);

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
                        throw e;
                    }
                }
            }
        
            if(m.getMaxField()>128 && fld.length > 128) {
                for (int i=1; i<=64; i++) {
                    if ((c = (ISOComponent) 
                        fields.get (i + 128)) != null)
                    {
                        try {
                            b = fld[i+128].pack(c);
                            len += b.length;
                            v.add (b);
                        } catch (ISOException e) {
                            if (evt != null) {
                                evt.addMessage ("error packing field "+(i+128));
                                evt.addMessage (c);
                                evt.addMessage (e);
                            }
                            throw e;
                        }
                    }
                }
            }

            int k = 0;
            byte[] d = new byte[len];
            
            // if ISOMsg insert header 
            if (m instanceof ISOMsg && headerLength>0) 
            {
            	byte[] h = ((ISOMsg) m).getHeader();
            	if (h != null) {
                    System.arraycopy(h, 0, d, k, h.length);
                    k += h.length;
                }
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
    public int unpack (ISOComponent m, byte[] b) throws ISOException {
        LogEvent evt = new LogEvent (this, "unpack");
        int consumed = 0;

        try {
            if (m.getComposite() != m) 
                throw new ISOException ("Can't call packager on non Composite");
            if (logger != null)  // save a few CPU cycle if no logger available
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
            int maxField = fld.length;
            if (emitBitMap()) {
                ISOBitMap bitmap = new ISOBitMap (-1);
                consumed += getBitMapfieldPackager().unpack(bitmap,b,consumed);
                bmap = (BitSet) bitmap.getValue();
                if (logger != null)
                    evt.addMessage ("<bitmap>"+bmap.toString()+"</bitmap>");
                m.set (bitmap);
                maxField = Math.min(maxField, bmap.size());
            }
            for (int i=getFirstField(); i<maxField; i++) {
                try {
                    if (bmap == null && fld[i] == null)
                        continue;
                    if (maxField > 128 && i==65)
                        continue;   // ignore extended bitmap

                    if (bmap == null || bmap.get(i)) {
                        if (fld[i] == null)
                            throw new ISOException ("field packager '" + i + "' is null");

                        ISOComponent c = fld[i].createComponent(i);
                        consumed += fld[i].unpack (c, b, consumed);
                        if (logger != null) {
                            evt.addMessage ("<unpack fld=\"" + i 
                                +"\" packager=\""
                                +fld[i].getClass().getName()+ "\">");
                            if (c.getValue() instanceof ISOMsg)
                                evt.addMessage (c.getValue());
                            else if (c.getValue() instanceof byte[]) {
                                evt.addMessage ("  <value type='binary'>" 
                                    +ISOUtil.hexString((byte[]) c.getValue())
                                    + "</value>");
                            }
                            else {
                                evt.addMessage ("  <value>" 
                                    +c.getValue()
                                    + "</value>");
                            }
                            evt.addMessage ("</unpack>");
                        }
                        m.set(c);
                    }
                } catch (ISOException e) {
                    evt.addMessage (
                        "error unpacking field " + i + " consumed=" + consumed
                    );
                    evt.addMessage (e);
                    // jPOS-3
                    e = new ISOException (
                        String.format ("%s (%s) unpacking field=%d, consumed=%d",
                        e.getMessage(), e.getNested().toString(), i, consumed)
                    );
                    throw e;
                }
            }
            if (b.length != consumed) {
                evt.addMessage (
                    "WARNING: unpack len=" +b.length +" consumed=" +consumed
                );
            }
            return consumed;
        } catch (ISOException e) {
            evt.addMessage (e);
            throw e;
        } catch (Exception e) {
            evt.addMessage (e);
            throw new ISOException (e.getMessage() + " consumed=" + consumed);
        } finally {
            Logger.log (evt);
        }
    }
    public void unpack (ISOComponent m, InputStream in) 
        throws IOException, ISOException 
    {
        LogEvent evt = new LogEvent (this, "unpack");
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
                if (logger != null)
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
                    if (logger != null) {
                        evt.addMessage ("<unpack fld=\"" + i 
                            +"\" packager=\""
                            +fld[i].getClass().getName()+ "\">");
                        if (c.getValue() instanceof ISOMsg)
                            evt.addMessage (c.getValue());
                        else
                            evt.addMessage ("  <value>" 
                                +c.getValue().toString()
                                + "</value>");
                        evt.addMessage ("</unpack>");
                    }
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
                        if (logger != null) {
                            evt.addMessage ("<unpack fld=\"" + i+128
                                +"\" packager=\""
                                +fld[i+128].getClass().getName()+ "\">");
                            evt.addMessage ("  <value>" 
                                +c.getValue().toString()
                                + "</value>");
                            evt.addMessage ("</unpack>");
                        }
                        m.set(c);
                    }
                }
            }
        } catch (ISOException e) {
            evt.addMessage (e);
            throw e;
        } catch (EOFException e) {
            throw e;
        } catch (Exception e) {
            evt.addMessage (e);
            throw new ISOException (e);
        } finally {
            Logger.log (evt);
        }
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
