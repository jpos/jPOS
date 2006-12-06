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

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Vector;

import org.jpos.iso.ISOField;
import org.jpos.iso.ISOBitMap;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

/**
 * GenericSubFieldPackager
 * Used to pack composite SubFields from the GenericPackager
 *
 * @author Eoin Flood
 * @see GenericPackager
 *
 * This class is basically the same as Base1SubFieldPackager except that it extends
 * GenericPackager which means that parameters such as emitBitmap, maxvalidField and 
 * bitmapField can be specified in the GenericPackager xml config file.
 */

public class GenericSubFieldPackager extends GenericPackager 
{
    public GenericSubFieldPackager() throws ISOException
    {
        super();
    }

    public int unpack (ISOComponent m, byte[] b) throws ISOException 
    {
        LogEvent evt = new LogEvent (this, "unpack");
        try 
        {
            if (m.getComposite() != m) 
                throw new ISOException ("Can't call packager on non Composite");
            if (logger != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.hexString (b));

            int consumed=0;
            ISOBitMap bitmap = new ISOBitMap (-1);

            BitSet bmap = null;
            int maxField = fld.length;
            if (emitBitMap()) 
            {
                consumed += getBitMapfieldPackager().unpack(bitmap,b,consumed);
                bmap = (BitSet) bitmap.getValue();
                m.set (bitmap);
                maxField = bmap.size();
            }
            for (int i=getFirstField(); i<maxField && consumed < b.length; i++) 
            {
                if (bmap == null || bmap.get(i)) 
                {
                    if (fld[i] != null) {
                        ISOComponent c = fld[i].createComponent(i);
                        consumed += fld[i].unpack (c, b, consumed);
                        m.set(c);
                    }
                }
            }
            if (b.length != consumed) 
            {
                evt.addMessage (
                "WARNING: unpack len=" +b.length +" consumed=" +consumed);
            }
            return consumed;
        } 
        catch (ISOException e) 
        {
            evt.addMessage (e);
            throw e;
        } 
        catch (Exception e)
        {
            evt.addMessage (e);
            throw new ISOException (e);
        }
        finally 
        {
            Logger.log (evt);
        }
    }

    /**
     * Pack the subfield into a byte array
     */ 

    public byte[] pack (ISOComponent m) throws ISOException 
    {
        LogEvent evt = new LogEvent (this, "pack");
        try 
        {
            ISOComponent c;
            Vector v = new Vector();
            Hashtable fields = m.getChildren();
            int len = 0;
            byte[] b;

            if (emitBitMap()) 
            {
                // BITMAP (-1 in HashTable)
                c = (ISOComponent) fields.get (new Integer (-1));
                b = getBitMapfieldPackager().pack(c);
                len += b.length;
                v.addElement (b);
            }

            for (int i=getFirstField(); i<=m.getMaxField(); i++) 
            {
                c = (ISOComponent) fields.get (new Integer (i));
                if (c == null && !emitBitMap())
                    c = new ISOField (i, "");
                if (c != null) {
                    try 
                    {
                        b = fld[i].pack(c);
                        len += b.length;
                        v.addElement (b);
                    } 
                    catch (Exception e) 
                    {
                        evt.addMessage ("error packing subfield "+i);
                        evt.addMessage (c);
                        evt.addMessage (e);
                    }
                }
            }
            int k = 0;
            byte[] d = new byte[len];
            for (int i=0; i<v.size(); i++) 
            {
                b = (byte[]) v.elementAt(i);
                for (int j=0; j<b.length; j++)
                d[k++] = b[j];
            }
            if (logger != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.hexString (d));
            return d;
        } 
        catch (ISOException e) 
        {
            evt.addMessage (e);
            throw e;
        } 
        catch (Exception e)
        {
            evt.addMessage (e);
            throw new ISOException (e);
        }
        finally 
        {
            Logger.log(evt);
        }
    }
}


