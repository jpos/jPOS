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
import java.util.BitSet;
import java.util.Map;
import org.xml.sax.Attributes;

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

public class GenericSubFieldPackager extends GenericPackager implements ISOSubFieldPackager
{

    private Integer fieldId = 0;

    public GenericSubFieldPackager() throws ISOException
    {
        super();
    }

    @Override
    public int getFieldNumber() {
      return fieldId;
    }

    @Override
    protected void setGenericPackagerParams(Attributes atts) {
        super.setGenericPackagerParams(atts);
        fieldId = Integer.parseInt(atts.getValue("id"));
    }

    @Override
    public int unpack (ISOComponent m, byte[] b) throws ISOException 
    {
        LogEvent evt = new LogEvent (this, "unpack");
        try 
        {
            if (m.getComposite() != m) 
                throw new ISOException ("Can't call packager on non Composite");
            if (b.length == 0)
                return 0; // nothing to do
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
                if ((bmap == null || bmap.get(i)) && i<fld.length && fld[i] != null) {
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
    @Override
    public byte[] pack(ISOComponent m) throws ISOException
    {
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
                c = (ISOComponent) fields.get (i);
                if (c == null && !emitBitMap())
                    c = new ISOField (i, "");
                if (c != null) {
                    try 
                    {
                        byte[] b = fld[i].pack(c);
                        bout.write(b);
                    } 
                    catch (Exception e) 
                    {
                        evt.addMessage ("error packing subfield "+i);
                        evt.addMessage (c);
                        evt.addMessage (e);
                        throw e;
                    }
                }
            }

            byte[] d = bout.toByteArray();
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


