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

import org.jpos.iso.ISOUtil;

import java.io.PrintStream;

public class Dumpable implements Loggeable {
    String name;
    byte[] payload;

    public Dumpable (String name, byte[] payload) {
        this.name = name;
        this.payload = payload;
    }
    public void dump(PrintStream p, String indent) {
        p.println (indent + "<" + name + ">");
        p.print (ISOUtil.hexdump (payload));
        p.println (indent + "</" + name + ">");
    }
}
