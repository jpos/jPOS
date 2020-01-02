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

package org.jpos.util;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Serializer {
    public static byte[] serialize (Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(baos);
        os.writeObject(obj);
        return baos.toByteArray();
    }
    public static Object deserialize (byte[] b) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        ObjectInputStream is = new ObjectInputStream(bais);
        return is.readObject();
    }
    @SuppressWarnings("unchecked")
    public static <T> T deserialize (byte[] b, Class<T> clazz) throws IOException, ClassNotFoundException {
        return (T) deserialize(b);
    }
    @SuppressWarnings("unchecked")
    public static <T> T serializeDeserialize (T obj) throws IOException, ClassNotFoundException {
        return (T) deserialize (serialize(obj));
    }

    public static byte[] serializeStringMap (Map<String,String> m)
      throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream     oos = new ObjectOutputStream (baos);
        Set s = m.entrySet();
        oos.writeInt (s.size());
        for (Object value : s) {
            Map.Entry entry = (Map.Entry) value;
            oos.writeObject(entry.getKey());
            oos.writeObject(entry.getValue());
        }
        oos.close();
        return baos.toByteArray();
    }
    public static Map<String,String> deserializeStringMap (byte[] buf)
      throws ClassNotFoundException, IOException
    {
        ByteArrayInputStream  bais = new ByteArrayInputStream (buf);
        ObjectInputStream     ois  = new ObjectInputStream( bais );
        Map<String,String> m = new HashMap<>();
        int size = ois.readInt();
        for (int i=0; i<size; i++) {
            m.put (
              (String) ois.readObject(),
              (String) ois.readObject()
            );
        }
        return m;
    }
}
