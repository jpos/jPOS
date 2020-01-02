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

package org.jpos.security;

import org.bouncycastle.util.encoders.Base64;
import org.jpos.iso.ISOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public class SystemSeed {
    public static byte[] getSeed (int l) {
        return getSeed(0, l);
    }
    public static byte[] getSeed (int offset, int l) {
        ByteBuffer buf = ByteBuffer.allocate(l);
        while (buf.hasRemaining()) {
            offset = offset % seed.length;
            int i = Math.min(l-buf.position(), seed.length - offset);
            buf.put(seed, offset, i);
            offset += i;
        }
        return buf.array();
    }

    private static final byte[] seed;
    static {
        try {
            byte[] _s0 = get("/META-INF/q2/.seed");
            byte[] _s1 = get("/META-INF/.seed");
            seed = _s1 == null ? _s0 : ISOUtil.xor(_s0, _s1);
            if (seed == null || seed.length < 16)
                throw new IllegalArgumentException ("Invalid seed");
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid system configuration");
        }
    }

    private static byte[] get (String path) throws IOException {
        InputStream is = SystemSeed.class.getResourceAsStream(path);
        if (is != null) {
            try {
                byte[] b = new byte[is.available()];
                is.read(b);
                return Base64.decode(b);
            } finally {
                is.close();
            }
        }
        return null;
    }
}
