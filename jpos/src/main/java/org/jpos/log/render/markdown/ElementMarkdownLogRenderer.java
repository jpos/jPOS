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
