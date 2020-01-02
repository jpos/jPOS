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

package org.jpos.iso.packager;

import org.jpos.iso.*;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 */
@SuppressWarnings("unchecked")
public class VISA1Packager
    extends SimpleLogSource implements ISOPackager, VISA1ResponseFilter
{
    public static final byte[] FS = { (byte)'\034' };
    int[] sequence;
    int respField;
    String badResultCode;
    String okPattern;
    VISA1ResponseFilter filter;

    /**
     * @param sequence array of fields that go to VISA1 request
     * @param respField where to put response
     * @param badResultCode (i.e. "05")
     * @param okPattern (i.e. "AUT. ")
     */
    public VISA1Packager 
        (int[] sequence, int respField, String badResultCode, String okPattern)
    { 
        super();
        this.sequence      = sequence;
        this.respField     = respField;
        this.badResultCode = badResultCode;
        this.okPattern     = okPattern;
        setVISA1ResponseFilter (this);
    }
    public void setVISA1ResponseFilter (VISA1ResponseFilter filter) {
        this.filter = filter;
    }

    protected int handleSpecialField35 (ISOMsg m, ByteArrayOutputStream bout)
        throws ISOException, IOException
    {
        int len = 0;
        byte[] entryMode = new byte[1];
        if (m.hasField (35)) {
            entryMode[0] = (byte) '\001';
            byte[] value = m.getString(35).getBytes();
            bout.write(entryMode);
            bout.write(value);
            bout.write(FS);
            len += value.length+2;
        } else if (m.hasField (2) && m.hasField (14)) {
            entryMode[0] = (byte) '\000';
            String simulatedTrack2 = m.getString(2) + "=" + m.getString(14);
            bout.write(entryMode);
            bout.write(simulatedTrack2.getBytes());
            bout.write(FS);
            len += simulatedTrack2.length()+2;
        }
        return len;
    }

    @Override
    public byte[] pack (ISOComponent c) throws ISOException
    {
        LogEvent evt = new LogEvent (this, "pack");
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(100))
        {
            if (!(c instanceof ISOMsg))
                throw new ISOException
                    ("Can't call VISA1 packager on non ISOMsg");

            ISOMsg m = (ISOMsg) c;

            for (int i=0; i<sequence.length; i++) {
                int fld = sequence[i];
                if (fld == 35)
                    handleSpecialField35(m, bout);
                else if (m.hasField(fld)) {
                    byte[] value;
                    if (fld == 4) {
                        long amt = Long.valueOf(m.getString(4));
                        value = ISOUtil.formatAmount (amt,12).trim().getBytes();
                    }
                    else
                        value = m.getString(fld).getBytes();
                    bout.write(value);
                    if (i < sequence.length-1) {
                        bout.write(FS);
                    }
                }
            }

            byte[] d = bout.toByteArray();
            if (logger != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.dumpString (d));
            return d;
        } catch (ISOException ex) {
            evt.addMessage(ex);
            throw ex;
        } catch (IOException ex) {
            evt.addMessage(ex);
            throw new ISOException(ex);
        } finally {
            Logger.log(evt);
        }
    }

    public String guessAutNumber (String s) {
        StringBuilder buf = new StringBuilder();
        for (int i=0; i<s.length(); i++)
            if (Character.isDigit(s.charAt(i))) 
                buf.append (s.charAt(i));
        if (buf.length() == 0)
            return null;

        while (buf.length() > 6)
            buf.deleteCharAt(0);
        while (buf.length() < 6)
            buf.insert(0, "0");
        
        return buf.toString();
    }

    @Override
    public int unpack (ISOComponent m, byte[] b) throws ISOException
    {
        String response = new String (b);
        m.set (new ISOField (respField, response));
        m.set (new ISOField (39, badResultCode));
        if (response.startsWith (okPattern)) {
            String autNumber = filter.guessAutNumber (response);
            if (autNumber != null) {
                m.set (new ISOField (39, "00"));
                m.set (new ISOField (38, autNumber));
            }
        }
        return b.length;
    }
    public void unpack (ISOComponent m, InputStream in) throws ISOException {
        throw new ISOException ("not implemented");
    }
    public String getFieldDescription(ISOComponent m, int fldNumber)
    {
        return "VISA 1 fld "+fldNumber;
    }
    public String getDescription () {
        return getClass().getName();
    }    
    public ISOMsg createISOMsg() {
        return new ISOMsg();
    }
}
