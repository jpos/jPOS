/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2015 Alejandro P. Revilla
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
import org.json.simple.JSONValue;

import java.io.*;
import java.util.*;

/**
 * packs/unpacks ISOMsgs into JSON representation
 *
 * @author apr@jpos.org
 * @see ISOPackager
 */

public class JSONPackager implements ISOPackager
{
    private ByteArrayOutputStream out;
    private PrintStream p;
    private ISOPackager packager;

    public JSONPackager() throws ISOException {
        super();
    }

    public JSONPackager(ISOPackager extpackager) throws ISOException {
        super();
        packager = extpackager;
    }

    public byte[] pack (ISOComponent m) throws ISOException {
        Map json = new LinkedHashMap();
        put (json, m, "");
        return JSONValue.toJSONString(json).getBytes(ISOUtil.CHARSET);
    }

    public synchronized int unpack (ISOComponent c, byte[] b) 
        throws ISOException
    {
        Map map = (Map) JSONValue.parse(new String(b));
        ISOMsg m = (ISOMsg) c;
        for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if (k.endsWith("b")) {
                m.set(k.substring(0, k.length()-1), ISOUtil.hex2byte(v));
            } else {
                m.set(k, v);
            }
        }
        return b.length;
    }

    public synchronized void unpack (ISOComponent c, InputStream in) 
        throws ISOException
    {
        throw new ISOException ("stream unpack not supported");
    }

    public String getFieldDescription(ISOComponent m, int fldNumber) {
        return "Field " + fldNumber;
    }
    public ISOMsg createISOMsg () {
        return new ISOMsg();
    }
    public String getDescription () {
        return getClass().getName();
    }

    private void put (Map map, ISOComponent c, String prefix) throws ISOException {
        if (c.getComposite() != null) {
            Map children = c.getChildren();
            for (Map.Entry entry : (Set<Map.Entry>) children.entrySet()) {
                ISOComponent cc = (ISOComponent) entry.getValue();
                put (map, cc, c.getFieldNumber() > 0 ? prefix + Integer.toString(c.getFieldNumber()) + "." : prefix);
            }
        }
        else if (c instanceof ISOField){
            if (packager==null){
                map.put(prefix + c.getKey(), ((ISOField)c).getValue());
            }else {
                Map field = new LinkedHashMap();
                if((((ISOBasePackager) packager).getFieldPackager((int) c.getKey()).getClass().getName()).contains("NUMERIC")) {
                    try {
                        field.put("value", Integer.valueOf((String) (((ISOField) c).getValue())));
                    }catch (NumberFormatException e){
                        field.put("value", ((ISOField) c).getValue());
                    }
                }else{
                    field.put("value", ((ISOField) c).getValue());
                }
                field.put("description",((ISOBasePackager) packager).getFieldPackager((int) c.getKey()).getDescription());
                field.put("class",((ISOBasePackager) packager).getFieldPackager((int) c.getKey()).getClass().getName());

                map.put(prefix + c.getKey(), field);
            }
        }else if (c instanceof ISOBinaryField)
            map.put(prefix + c.getKey() + "b", ISOUtil.hexString(((ISOBinaryField) c).getBytes()));
    }
}
