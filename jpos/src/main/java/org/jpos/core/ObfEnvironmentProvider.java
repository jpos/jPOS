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

package org.jpos.core;

import org.jpos.iso.ISOUtil;
import org.jpos.security.SystemSeed;

import java.nio.ByteBuffer;
import java.util.Base64;

public class ObfEnvironmentProvider implements EnvironmentProvider {
    @Override
    public String prefix() {
        return "obf::";
    }

    @Override
    public String get(String config) {
        ByteBuffer buf = ByteBuffer.wrap(Base64.getDecoder().decode(config));
        int i = buf.getInt();
        byte[] b = new byte[buf.remaining()];
        buf.get(b);
        return new String(ISOUtil.xor(b, SystemSeed.getSeed(i, b.length)));
    }
}
