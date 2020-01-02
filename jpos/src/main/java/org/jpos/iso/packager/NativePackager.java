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

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;

import java.io.*;

public class NativePackager implements ISOPackager {
    @Override
    public byte[] pack(ISOComponent c) throws ISOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (c instanceof ISOMsg) {
                ISOMsg m = (ISOMsg)c;
                ISOPackager p = m.getPackager();
                m.setPackager(null);
                ObjectOutputStream os = new ObjectOutputStream(baos);
                ((Externalizable)c).writeExternal(os);
                os.flush();
                m.setPackager(p);
            }
        } catch (IOException e) {
            throw new ISOException (e);
        }
        return baos.toByteArray();
    }

    @Override
    public int unpack(ISOComponent m, byte[] b) throws ISOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        if (m instanceof Externalizable) {
            try {
                unpack (m, bais);
            } catch (IOException e) {
                throw new ISOException (e);
            }
        }
        return b.length - bais.available();
    }

    @Override
    public void unpack(ISOComponent m, InputStream in) throws IOException, ISOException {
        try {
            if (m instanceof Externalizable) {
                ObjectInputStream is = new ObjectInputStream(in);
                ((Externalizable) m).readExternal(is);
            }
        } catch (Exception e) {
            throw new ISOException (e);
        }
    }

    @Override
    public String getDescription() {
        return getClass().getName();
    }

    @Override
    public String getFieldDescription(ISOComponent m, int fldNumber) {
        return null;
    }

    @Override
    public ISOMsg createISOMsg() {
        return new ISOMsg();
    }
}

