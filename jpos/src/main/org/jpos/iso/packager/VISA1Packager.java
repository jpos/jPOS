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

package org.jpos.iso.packager;

import java.io.InputStream;
import java.util.Vector;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.VISA1ResponseFilter;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogSource;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 */
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
    protected int handleSpecialField35 (ISOMsg m, Vector v) 
        throws ISOException
    {
        int len = 0;
        byte[] entryMode = new byte[1];
        if (m.hasField (35)) {
            entryMode[0] = (byte) '\001';
            byte[] value = ((String)m.getValue(35)).getBytes();
            v.addElement (entryMode);
            v.addElement (value);
            v.addElement (FS);
            len += value.length+2;
        } else if (m.hasField (2) && m.hasField (14)) {
            entryMode[0] = (byte) '\000';
            String simulatedTrack2 = 
                (String) m.getValue(2) + "=" + (String) m.getValue(14);
            v.addElement (entryMode);
            v.addElement (simulatedTrack2.getBytes());
            v.addElement (FS);
            len += simulatedTrack2.length()+2;
        }
        return len;
    }
    public byte[] pack (ISOComponent c) throws ISOException
    {
        LogEvent evt = new LogEvent (this, "pack");
        try {
            if (!(c instanceof ISOMsg))
                throw new ISOException
                    ("Can't call VISA1 packager on non ISOMsg");
        
            ISOMsg m = (ISOMsg) c;

            int len  = 0;
            Vector v = new Vector();
            for (int i=0; i<sequence.length; i++) {
                int fld = sequence[i];
                if (fld == 35) 
                    len += handleSpecialField35 (m, v);
                else if (m.hasField(fld)) {
                    byte[] value;
                    if (fld == 4) {
                        long l = Long.parseLong (((String)m.getValue(4)));
                        value = ISOUtil.formatAmount (l,12).trim().getBytes();
                    }
                    else
                        value = ((String)m.getValue(fld)).getBytes();
                    v.addElement (value);
                    len += value.length;
                    if (i < (sequence.length-1)) {
                        v.addElement (FS);
                        len++;
                    }
                }
            }

            int k = 0;
            byte[] d = new byte[len];
            for (int i=0; i<v.size(); i++) {
                byte[] b = (byte[]) v.elementAt(i);
                for (int j=0; j<b.length; j++)
                    d[k++] = b[j];
            }
            if (logger != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.dumpString (d));
            return d;
        } catch (ISOException e) {
            evt.addMessage (e);
            throw e;
        } finally {
            Logger.log(evt);
        }
    }

    public String guessAutNumber (String s) {
        StringBuffer buf = new StringBuffer();
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
}
