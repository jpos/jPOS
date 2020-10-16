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

package org.jpos.q2.cli;

import org.jpos.iso.ISOUtil;
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.security.SystemSeed;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Obfuscate a secret
 */
public class OBF implements CLICommand {
    @Override
    public void exec(CLIContext cli, String[] args) throws Exception {
        if (args.length != 2) {
            usage(cli);
            return;
        }
        cli.println (String.format("obf::%s", obf(args[1])));
    }
    public void usage(CLIContext cli) {
        cli.println("Usage: obf \"secret\"");
    }

    private String obf(String s) {
        byte[] b = s.getBytes();
        int i = Math.abs(new SecureRandom().nextInt());
        b = ISOUtil.xor(b, SystemSeed.getSeed(i, b.length));
        ByteBuffer buf = ByteBuffer.allocate(b.length + 4);
        buf.putInt(i);
        buf.put(b);
        return Base64.getEncoder().encodeToString(buf.array());
    }
}
