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

package org.jpos.q2;

import java.io.InputStream;
import java.io.OutputStream;

/** Q2 CLI command interface for external command execution. */
public interface Command {
    /**
     * Executes this command.
     * @param is input stream
     * @param os output stream
     * @param err error stream
     * @param strings command arguments
     * @throws Exception on execution failure
     */
    void exec(InputStream is, OutputStream os, OutputStream err, String[] strings) throws Exception;
}
