/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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
import static  org.jpos.transaction.TransactionManager.Trace;

import java.io.PrintStream;

public final class TransactionManagerTraceArrayMarkdownLogRenderer implements LogRenderer<Trace[]> {
    @Override
    public void render(Trace[] traces, PrintStream ps, String indent) {
        ps.println ("```mermaid");
        ps.println ("gitGraph");
        for (int i=0; i<traces.length; i++) {
            ps.println (traces[i]);
        }
        ps.println ("```");
    }
    public Class<?> clazz() {
        return Trace[].class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }
}
