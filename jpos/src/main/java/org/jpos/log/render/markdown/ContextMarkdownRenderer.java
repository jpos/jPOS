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
import org.jpos.log.LogRendererRegistry;
import org.jpos.transaction.Context;

import java.io.PrintStream;
import java.util.Map;

public final class ContextMarkdownRenderer implements LogRenderer<Context> {
    @Override
    public void render(Context ctx, PrintStream ps, String indent) {
        Map<Object,Object> map = ctx.getMapClone();
        map.forEach((key, value) -> formatEntry(key.toString(), value, ps));
    }

    public Class<?> clazz() {
        return Context.class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }
    
    private void formatEntry (String key, Object value, PrintStream ps) {
        LogRenderer<Object> renderer = LogRendererRegistry.getRenderer(value.getClass(), Type.MARKDOWN);
        if (renderer != null) {
            ps.printf ("#### %s%n", key);
            // ps.printf ("> %s%n%n", Caller.shortClassName(renderer.getClass().getCanonicalName()));
            renderer.render (value, ps, "");
        } else {
            ps.printf ("No renderer could be found for class %s%n", value.getClass());
            ps.printf ("#### %s%n```%n", key);
            ps.println(value);
            ps.println("```");
        }
    }
}
