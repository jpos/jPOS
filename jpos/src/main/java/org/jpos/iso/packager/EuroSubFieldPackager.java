/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.xml.sax.Attributes;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
@SuppressWarnings("unchecked")
public class EuroSubFieldPackager extends ISOBasePackager implements GenericPackagerParams, ISOSubFieldPackager
{
    protected static Prefixer tagPrefixer = AsciiPrefixer.LL;

    // fieldId is read from "id" XML attribute (because this class is GenericPackagerParams).
    // Useful for cases where we embed an EuroSubFieldPackager inside a non-bitmapped
    // field (such as another EuroSubFieldPackager), and the wrapping packager needs to know
    // this object's field position (i.e., outer tag)
    protected Integer fieldId = -1;

    @Override
    public void setGenericPackagerParams(Attributes atts) {
        fieldId = Integer.parseInt(atts.getValue("id"));
    }

    @Override
    public int getFieldNumber() {
        return fieldId;
    }

    /**
     * Always return false
     */
    @Override
    protected boolean emitBitMap()
    {
        return false;
    }

    @Override
    public byte[] pack (ISOComponent c) throws ISOException {
        LogEvent evt = new LogEvent (this, "pack");
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(100)) {
            Map tab = c.getChildren();

            for (Entry ent : (Set<Entry>) tab.entrySet()) {
                Integer i = (Integer) ent.getKey();
                if (i < 0)
                    continue;
                if (fld[i] == null)
                    throw new ISOException ("Unsupported sub-field " + i + " packing field " + c.getKey());
                if (ent.getValue() instanceof ISOComponent)
                    try {
                        ISOComponent f = (ISOComponent) ent.getValue();
                        byte[] b = fld[i].pack(f);
                        bout.write(b);
                    } catch (Exception e) {
                        evt.addMessage ("error packing subfield "+i);
                        evt.addMessage (c);
                        evt.addMessage (e);
                        throw e;
                    }
            }

            byte[] d = bout.toByteArray();
            if (logger != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.hexString (d));
            return d;
        }
        catch (Exception ex)
        {
            throw new ISOException (ex);
        }
    }


    /**
     * This packager treats field 0 as a field that may or may not be present before the  TLV subelements.
     *
     * Certain types of messages for some 8583 specs that extend this class' behavior (e.g., the Mastercard implementation
     * in class {@link MasterCardEBCDICSubFieldPackager}) may not have field 0 present (the TCC in Mastercard's nomenclature).
     * So, if the corresponding isofield packager for field 0 doesn't fill the {@link ISOComponent}'s value,
     * we don't store anything as subfield 0 of m.
     */
    @Override
    public int unpack (ISOComponent m, byte[] b) throws ISOException
    {
        LogEvent evt = logger != null ? new LogEvent (this, "unpack") : null;
        int consumed = 0;
        ISOComponent c = null;

        // Unpack the fields
        while (consumed < b.length) {
            //If this is first iteration and there is a packager for SE 0 then i=0, i.e. use field packager for SE 0
            //Else determine current tag
            int i = c == null && fld[0] != null ? 0 : tagPrefixer.decodeLength(b, consumed);

            if (i >= fld.length || fld[i] == null)
                throw new ISOException("Unsupported sub-field " + i + " unpacking field " + m.getKey());

            c = fld[i].createComponent(i);
            consumed += fld[i].unpack (c, b, consumed);
            if (i != 0 || c.getValue() != null) {
                if (evt != null) {
                    fieldUnpackLogger(evt, i, c, fld[i], logFieldName);
                }

                m.set(c);
            }
            // else:
            // If it was field 0 (TCC) && nothing was stored in the component, we discard this component
        }

        if (logger != null && evt != null)
            Logger.log (evt);

        return consumed;
    }


    @Override
    public void setLogger (Logger logger, String realm) {
        super.setLogger (logger, realm);
        if (fld != null) {
            for (int i=0; i<fld.length; i++) {
                if (fld[i] instanceof ISOMsgFieldPackager) {
                    Object o = ((ISOMsgFieldPackager)fld[i]).getISOMsgPackager();
                    if (o instanceof LogSource) {
                        ((LogSource)o).setLogger (logger, realm + "-fld-" + i);
                    }
                }
            }
        }
    }

}

