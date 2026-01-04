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

package org.jpos.log.render.txt;

import org.jpos.log.LogRenderer;
import org.jpos.log.evt.License;

import java.io.PrintStream;

public final class LicenseTxtLogRenderer implements LogRenderer<License> {
    public Class<?> clazz() {
        return License.class;
    }
    public Type type() {
        return Type.TXT;
    }

    @Override
    public void render(License l, PrintStream ps, String indent) {
        ps.println (l.license());
        ps.printf ("0x%x%n", l.status());
    }
}
