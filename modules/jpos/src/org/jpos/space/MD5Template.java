/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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
import java.security.MessageDigest;
import java.util.Arrays;

import jdbm.helper.DefaultSerializer;

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
    public static byte[] digest (Object obj) {
        try {
            MessageDigest md = MessageDigest.getInstance ("MD5");
            return md.digest (DefaultSerializer.INSTANCE.serialize (obj));
        } catch (Exception e) {
            throw new SpaceError (e);
        }
    }
    public boolean equals (Object obj) {
        return Arrays.equals (digest (obj), digest);
    }
    public Object getKey () {
        return key;
    }
}

