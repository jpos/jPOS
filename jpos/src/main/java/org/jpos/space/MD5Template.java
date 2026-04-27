/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

/**
 * Space {@link Template} that compares entries by an MD5 digest of their
 * serialized form rather than by reference equality.
 */
public class MD5Template implements Template, Serializable  {
    private static final long serialVersionUID = -1204861759575740048L;
    /** Digest of the serialized comparison value. */
    byte[] digest;
    /** Key associated with this template. */
    Object key;

    /**
     * Constructs a template that matches entries whose serialized form digests to {@code value}.
     *
     * @param key entry key
     * @param value reference value to digest
     */
    public MD5Template (Object key, Object value) {
        super ();
        this.key    = key;
        this.digest = digest (value);
    }
    /**
     * Constructs a template directly from a precomputed digest.
     *
     * @param key entry key
     * @param digest precomputed MD5 digest
     */
    public MD5Template (Object key, byte[] digest) {
        super ();
        this.key = key;
        this.digest = digest;
    }
    /**
     * Computes the MD5 digest of {@code obj}'s serialized form.
     *
     * @param obj object to digest
     * @return the MD5 digest bytes
     */
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
    /**
     * Returns the key this template is registered under.
     *
     * @return the entry key
     */
    public Object getKey () {
        return key;
    }
    /**
     * Returns the digest used for comparison.
     *
     * @return the MD5 digest bytes
     */
    public byte[] getDigest () {
        return digest;
    }
    /**
     * Returns the digest as a hex-encoded string.
     *
     * @return the digest hex string
     */
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
    /**
     * Serializes {@code obj} via {@link ObjectOutputStream}, returning the resulting bytes.
     *
     * @param obj object to serialize
     * @return the serialized byte array
     * @throws IOException if writing fails
     */
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
