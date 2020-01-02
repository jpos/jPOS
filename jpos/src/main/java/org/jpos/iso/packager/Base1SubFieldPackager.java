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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.util.Map;

/**
 * ISO 8583 v1987 BINARY Packager 
 * customized for VISA Base1 subfields
 * 
 *
 * @author <a href="mailto:eoin.flood@orbiscom.com">Eoin Flood</a>
 * @version $Id$
 * @see ISOPackager
 * @see ISOBasePackager
 * @see ISOComponent
 */

@SuppressWarnings("unchecked")
public class Base1SubFieldPackager extends ISOBasePackager
{
    // These methods are identical to ISOBasePackager
    // except that fld[1] has been replaced with fld[0]
    // and a secondard bitmap is not allowed

    protected boolean emitBitMap()
    {
        return fld[0] instanceof ISOBitMapPackager;
    }

    protected int getFirstField()
    {
        return fld[0] instanceof ISOBitMapPackager ? 1 : 0;
    }

    protected ISOFieldPackager getBitMapfieldPackager() 
    {
        return fld[0];
    }

    /**
     * Unpack a packed subfield into
     * its corresponding ISOComponent
     */

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
                    ISOComponent c = fld[i].createComponent(i);
                    consumed += fld[i].unpack (c, b, consumed);
                    m.set(c);
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
        finally 
        {
            Logger.log (evt);
        }
    }

    /**
     * Pack the subfield into a byte array
     */
    @Override
    public byte[] pack (ISOComponent m) throws ISOException {
        LogEvent evt = new LogEvent (this, "pack");
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(100))
        {
            ISOComponent c;
            Map fields = m.getChildren();

            if (emitBitMap()) 
            {
                // BITMAP (-1 in HashTable)
                c = (ISOComponent) fields.get (-1);
                byte[] b = getBitMapfieldPackager().pack(c);
                bout.write(b);
            }

            for (int i=getFirstField(); i<=m.getMaxField(); i++) 
            {
                if ((c = (ISOComponent) fields.get (i)) != null)
                {
                    try 
                    {
                        byte[] b = fld[i].pack(c);
                        bout.write(b);
                    } 
                    catch (Exception e) 
                    {
                        evt.addMessage ("error packing field "+i);
                        evt.addMessage (c);
                        evt.addMessage (e);
                        throw new ISOException (e);
                    }
                }
            }

            byte[] d = bout.toByteArray();
            if (logger != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.hexString (d));
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
}


