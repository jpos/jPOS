package org.jpos.util.log.event;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jpos.util.Loggeable;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Created by erlangga on 2019-10-12.
 */
public class XMLLogEvent implements BaseLogEvent {

    @Override
    public String dumpHeader(PrintStream p, String indent, String realm, Instant dumpedAt, Instant createdAt, boolean noArmor) {
        if (noArmor) {
            p.println("");
        } else {
            if (dumpedAt == null)
                dumpedAt = Instant.now();
            StringBuilder sb = new StringBuilder(indent);
            sb.append ("<log realm=\"");
            sb.append (realm);
            sb.append("\" at=\"");
            sb.append(LocalDateTime.ofInstant(dumpedAt, ZoneId.systemDefault()));
            sb.append ('"');
            long elapsed = Duration.between(createdAt, dumpedAt).toMillis();
            if (elapsed > 0) {
                sb.append (" lifespan=\"");
                sb.append (elapsed);
                sb.append ("ms\"");
            }
            sb.append ('>');
            p.println (sb.toString());
        }
        return indent + "  ";
    }

    @Override
    public void dumpTrailer(PrintStream p, String indent, boolean noArmor) {
        if (!noArmor)
            p.println (indent + "</log>");
    }

    @Override
    public void dump(PrintStream p, String outer, String realm, Instant dumpedAt, Instant createdAt, List<Object> payLoad, boolean noArmor, String tag) {
        try {
            String indent = dumpHeader (p, outer, realm,dumpedAt,createdAt, noArmor);
            if (payLoad.isEmpty()) {
                if (tag != null)
                    p.println (indent + "<" + tag + "/>");
            }
            else {
                String newIndent;
                if (tag != null) {
                    if (!tag.isEmpty())
                        p.println (indent + "<" + tag + ">");
                    newIndent = indent + "  ";
                }
                else
                    newIndent = "";
                synchronized (payLoad) {
                    for (Object o : payLoad) {
                        if (o instanceof Loggeable)
                            ((Loggeable) o).dump(p, newIndent);
                        else if (o instanceof SQLException) {
                            SQLException e = (SQLException) o;
                            p.println(newIndent + "<SQLException>"
                                    + e.getMessage() + "</SQLException>");
                            p.println(newIndent + "<SQLState>"
                                    + e.getSQLState() + "</SQLState>");
                            p.println(newIndent + "<VendorError>"
                                    + e.getErrorCode() + "</VendorError>");
                            ((Throwable) o).printStackTrace(p);
                        } else if (o instanceof Throwable) {
                            p.println(newIndent + "<exception name=\""
                                    + ((Throwable) o).getMessage() + "\">");
                            p.print(newIndent);
                            ((Throwable) o).printStackTrace(p);
                            p.println(newIndent + "</exception>");
                        } else if (o instanceof Object[]) {
                            Object[] oa = (Object[]) o;
                            p.print(newIndent + "[");
                            for (int j = 0; j < oa.length; j++) {
                                if (j > 0)
                                    p.print(",");
                                p.print(oa[j].toString());
                            }
                            p.println("]");
                        } else if (o instanceof Element) {
                            p.println("");
                            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
                            out.getFormat().setLineSeparator("\n");
                            try {
                                out.output((Element) o, p);
                            } catch (IOException ex) {
                                ex.printStackTrace(p);
                            }
                            p.println("");
                        } else if (o != null) {
                            p.println(newIndent + o.toString());
                        } else {
                            p.println(newIndent + "null");
                        }
                    }
                }
                if (tag != null && !tag.isEmpty())
                    p.println (indent + "</" + tag + ">");
            }
        } catch (Throwable t) {
            t.printStackTrace(p);

        } finally {
            dumpTrailer (p, outer, noArmor);
        }
    }
}
