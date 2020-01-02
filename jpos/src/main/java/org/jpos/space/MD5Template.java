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

package org.jpos.space;

import java.io.Serializable;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

import org.jpos.iso.ISOUtil;

public class MD5Template implements Template, Serializable  {
    private static final long serialVersionUID = -1204861759575740048L;
    byte[] digest;
    Object key;

    public MD5Template (Object key, Object value) {
        super ();
        this.key    = key;
        this.digest = digest (value);
    }
    public MD5Template (Object key, byte[] digest) {
        super ();
        this.key = key;
        this.digest = digest;
    }
    public byte[] digest (Object obj) {
        try {
            MessageDigest md = MessageDigest.getInstance ("MD5");
            return md.digest (serialize (obj));
        } catch (Exception e) {
            throw new SpaceError (e);
        }
    }
    @Override
    public boolean equals (Object obj) {
        return Arrays.equals (digest (obj), digest);
    }
    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42;
    }
    public Object getKey () {
        return key;
    }
    public byte[] getDigest () {
        return digest;
    }
    public String getDigestAsString () {
        return ISOUtil.hexString (digest);
    }
    public String toString () {
        StringBuilder sb = new StringBuilder();
        sb.append ("key='");
        sb.append (key);
        sb.append ("', digest=");
        sb.append (getDigestAsString ());
        return sb.toString();        
    }
    public static byte[] serialize (Object obj) throws IOException {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream (baos);
        oos.writeObject (obj);
        oos.close();

        return baos.toByteArray();
    }
}
