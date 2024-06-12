package org.jpos.log.render.markdown;

import org.jpos.iso.ISOUtil;
import org.jpos.log.LogRenderer;

import java.io.PrintStream;

public final class ByteArrayMarkdownLogRenderer implements LogRenderer<byte[]> {
    @Override
    public void render(byte[] b, PrintStream ps, String indent) {
        if (b.length > 16) {
            ps.printf ("```%n%s%n```%n", indent(indent, ISOUtil.hexdump(b)));
        } else {
            ps.printf ("`%s`%n", indent(indent,ISOUtil.hexString(b)));
        }
    }
    public Class<?> clazz() {
        return byte[].class;
    }
    public Type type() {
        return Type.MARKDOWN;
    }
}
