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

package org.jpos.iso.packager;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.util.Serializer;

import java.io.*;

/** {@link ISOPackager} that round-trips messages via Java serialization. */
public class NativePackager implements ISOPackager, Configurable {
    private static final Serializer.DeserializationLimits DEFAULT_DESERIALIZATION_LIMITS =
        new Serializer.DeserializationLimits(32, 50_000, 1_000_000, 16L * 1024 * 1024);

    private Serializer.DeserializationLimits deserializationLimits = DEFAULT_DESERIALIZATION_LIMITS;

    /** Default constructor using the standard native deserialization limits. */
    public NativePackager() {}

    /**
     * Configures the resource limits applied while unpacking native messages.
     *
     * @param cfg configuration containing optional deserialization limit properties
     * @throws ConfigurationException if a configured limit is not a positive integer
     */
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        try {
            Serializer.DeserializationLimits limits = new Serializer.DeserializationLimits(
                cfg.getLong("deserialization-max-depth", DEFAULT_DESERIALIZATION_LIMITS.maxDepth()),
                cfg.getLong("deserialization-max-references", DEFAULT_DESERIALIZATION_LIMITS.maxReferences()),
                cfg.getLong("deserialization-max-array-length", DEFAULT_DESERIALIZATION_LIMITS.maxArrayLength()),
                cfg.getLong("deserialization-max-bytes", DEFAULT_DESERIALIZATION_LIMITS.maxStreamBytes())
            );
            if (limits.maxDepth() == 0 || limits.maxReferences() == 0
              || limits.maxArrayLength() == 0 || limits.maxStreamBytes() == 0)
                throw new IllegalArgumentException("Deserialization limits must be positive");
            deserializationLimits = limits;
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Invalid NativePackager deserialization limits", e);
        }
    }

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
        if (b.length > deserializationLimits.maxStreamBytes())
            throw new ISOException("Serialized input exceeds maximum stream bytes");
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
                // Graph limits apply when ObjectInputStream resolves objects; the hard byte cap
                // also covers the primitive data read directly by ISOMsg.readExternal.
                ObjectInputStream is = Serializer.createLimitedAllowListObjectInputStream(
                    in, deserializationLimits, "org.jpos.iso."
                );
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
