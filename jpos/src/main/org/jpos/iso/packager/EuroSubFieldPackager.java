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

import java.util.Hashtable;

import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

/**
 * EuroPay SubField packager
 * @author Eoin Flood
 * @version $Revision$ $Date$
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 *
 * This packager is used by EuroPackager to package subfields
 * such as field 48.
 */
public class EuroSubFieldPackager extends ISOBasePackager
{ 
    /** 
     * Default constructor
     */
    public EuroSubFieldPackager()
    {
        super();
    }

    /**
     * Always return false
     */
    protected boolean emitBitMap()
    {
        return false;
    }

    public byte[] pack (ISOComponent c) throws ISOException
    {
        try
        {
            int len;
            Hashtable tab = c.getChildren();
            StringBuffer sb = new StringBuffer();

            // Handle first IF_CHAR field
            ISOField f0 = (ISOField) tab.get (new Integer(0));
            if (f0 != null) {
                String s = (String) f0.getValue();
                sb.append (s);
            }
            for (int i =1; i<fld.length; i++) 
            {
                Object obj = tab.get (new Integer(i));
                if (obj instanceof ISOField) {
                    ISOField f = (ISOField) obj;
                    sb.append (new String(fld[i].pack(f)));
                } 
            }
            return sb.toString().getBytes();
        }
        catch (Exception ex)
        {
            throw new ISOException (ex);
        }
    }

    public int unpack (ISOComponent m, byte[] b) throws ISOException
    {
        LogEvent evt = new LogEvent (this, "unpack");
        // Unpack the IF_CHAR field
        int consumed = 0;
        ISOComponent c;
        if (fld[0] != null) {
            c = fld[0].createComponent(0);
            consumed += fld[0].unpack (c, b, consumed);
            m.set(c);
        }

        // Now unpack the IFEP_LLCHAR fields
        for (int i=1; consumed < b.length ; i++) 
        {
            if (fld[i] == null)
                continue;
            c = fld[i].createComponent(i);
            consumed += fld[i].unpack (c, b, consumed);
            if (logger != null) 
            {
                evt.addMessage ("<unpack fld=\"" + i 
                    +"\" packager=\""
                    +fld[i].getClass().getName()+ "\">");
                    evt.addMessage ("  <value>" 
                    +c.getValue().toString()
                    + "</value>");
                evt.addMessage ("</unpack>");
            }
            m.set(c);
        }
        Logger.log (evt);
        return consumed;
    }
}

