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

package org.jpos.jfr;

import jdk.jfr.*;
/**
 * JFR event recorded around individual jPOS space operations, threshold-filtered
 * to avoid storms (default 10 ms).
 */
@Category("jPOS")
@Name("jpos.Space")
@StackTrace
@Threshold("10 ms")
public class SpaceEvent extends Event {
    /** Operation name (e.g. {@code in}, {@code out}, {@code rdp}). */
    @Name("op")
    public String operation;

    /** Space key associated with the operation. */
    @Name("key")
    public String key;

    /**
     * Constructs a SpaceEvent describing the given operation and key.
     *
     * @param operation operation name
     * @param key space key
     */
    public SpaceEvent(String operation, String key) {
        this.operation = operation;
        this.key = key;
    }
}
