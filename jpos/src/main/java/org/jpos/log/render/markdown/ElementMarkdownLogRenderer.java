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

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jpos.log.LogRenderer;
import java.io.IOException;
import java.io.PrintStream;

public final class ElementMarkdownLogRenderer implements LogRenderer<Element> {
    final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

    public ElementMarkdownLogRenderer() {
        out.getFormat().setLineSeparator("\n");
    }
    @Override
    public void render(Element o, PrintStream ps, String indent) {
        ps.println("```xml");

        try {
            out.output(o, ps);
        } catch (IOException ex) {
            ex.printStackTrace(ps);
        }
        ps.println("```");
    }
    public Class<?> clazz() {
        return Element.class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }
}
