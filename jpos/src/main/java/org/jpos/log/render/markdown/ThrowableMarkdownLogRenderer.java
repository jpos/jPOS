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

package org.jpos.log.render.markdown;

import org.jpos.log.LogRenderer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class ThrowableMarkdownLogRenderer implements LogRenderer<Throwable> {
    @Override
    public void render(Throwable t, PrintStream ps, String indent) {
        ps.println(stackTrace(indent+" ", t));
    }
    public Class<?> clazz() {
        return Throwable.class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }

    private String stackTrace(String indent, Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        t.printStackTrace(ps);
        return indent(indent, baos.toString());
    }
}
