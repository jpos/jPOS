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

import opencard.opt.util.TLV;
import opencard.opt.util.Tag;

import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

/**
 * ICC TLV SubField packager
 * @author Bharavi
 * @version 1.0
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 * @deprecated use IFB_LLLTLVBINARY and TLVField.java instead
 *
 * This packager is used by TLVPackager to package subfields
 * such as field 55.
 */
public class TLVSubFieldPackager extends ISOBasePackager
{

//Find a way to pass these tags dynamically
   private byte   tg[][]=   {
            {(byte)0x9F,(byte)0x01}, //Tag value
            {(byte)0x9F,(byte)0x02}, //Tag value
            {(byte)0x9F,(byte)0x03}, //Tag value
            {(byte)0x9F,(byte)0x04}, //Tag value
            };


   //++++++++++++++++

    /**
     * Default constructor
     */
    public TLVSubFieldPackager()
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
        try {

            int len;
            Hashtable tab = c.getChildren();

            StringBuffer sb = new StringBuffer();
            byte [] bt0 = {0x00};
            Tag t0=new Tag(bt0);
            TLV tlv=null;

            for (int i =0; i<fld.length; i++) {
                ISOBinaryField f = (ISOBinaryField) tab.get (new Integer(i));

                if (f != null) {
                    if(tlv==null) {
                        tlv =
                            new TLV(t0,new TLV (
                              new Tag  (tg[i]),fld[i].pack(f))
                            );
                    }
                    else
                    {
                        tlv.add(new TLV(new Tag  (tg[i]),fld[i].pack(f)));
                    }
                }
            }
            byte [] out=tlv.toBinary();
            return out;
        }
        catch (Exception ex) {
            // ex.printStackTrace();
            throw new ISOException (ex);
        }
    }

    public int unpack (ISOComponent m, byte[] b) throws ISOException
    {

        LogEvent evt = new LogEvent (this, "unpack");
        // Unpack the IF_CHAR field
        int consumed = 0;
        // Now unpack the IFEP_LLCHAR fields
        int maxField = fld.length;
        TLV temp =  null;
        TLV tlv=new TLV(b);
        for (int i=getFirstField()-1; i<maxField; i++)
        {
            ISOComponent c = fld[i].createComponent(i);
            TLV t=tlv.findTag(new Tag  (tg[i]),temp);

            if(t!=null)
            {
                temp=t;
                byte val[]=t.valueAsByteArray();
                consumed += fld[i].unpack (c,val,0);
                m.set(c);
            }
        }
        Logger.log (evt);
        return consumed;
    }
}

