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

package org.jpos.util;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jpos.jfr.LogEventDump;
import org.jpos.log.LogRenderer;
import org.jpos.log.LogRendererRegistry;
import org.jpos.log.evt.SysInfo;
import org.jpos.log.render.txt.SysInfoTxtLogRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author @apr
 */
public class LogEvent {
    private LogSource source;
    private String tag;
    private final List<Object> payLoad;
    private Instant createdAt;
    private Instant dumpedAt;
    private boolean honorSourceLogger;
    private boolean noArmor;
    private boolean hasException;
    private String traceId;

    public LogEvent (String tag) {
        super();
        this.tag = tag;
        createdAt = Instant.now();
        this.payLoad = Collections.synchronizedList (new ArrayList<>());
    }

    public LogEvent () {
        this("info");
    }
    public LogEvent (String tag, Object msg) {
        this (tag);
        addMessage(msg);
    }
    public LogEvent (LogSource source, String tag) {
        this (tag);
        this.source  = source;
        honorSourceLogger = true;
    }
    public LogEvent (LogSource source, String tag, Object msg) {
        this (tag);
        this.source  = source;
        honorSourceLogger = true;
        addMessage(msg);
    }
    public String getTag() {
        return tag;
    }
    public void setTag (String tag) {
        this.tag = tag;
    }
    public void addMessage (Object msg) {
        payLoad.add (msg);
        if (msg instanceof Throwable)
            hasException = true;
    }
    public void addMessage (String tagname, String message) {
        payLoad.add ("<"+tagname+">"+message+"</"+tagname+">");
    }
    public LogSource getSource() {
        return source;
    }
    public void setSource(LogSource source) {
        this.source = source;
    }
    public void setNoArmor (boolean noArmor) {
        this.noArmor = noArmor;
    }
    protected String dumpHeader (PrintStream p, String indent) {
        if (noArmor) {
            p.println("");
        } else {
            dumpedAt = getDumpedAt();
            StringBuilder sb = new StringBuilder(indent);
            sb.append ("<log realm=\"");
            sb.append (getRealm());
            sb.append("\" at=\"");
            sb.append(LocalDateTime.ofInstant(dumpedAt, ZoneId.systemDefault()));
            sb.append ('"');
            long elapsed = Duration.between(createdAt, dumpedAt).toMillis();
            if (elapsed > 0) {
                sb.append (" lifespan=\"");
                sb.append (elapsed);
                sb.append ("ms\"");
            }
            if (traceId != null) {
                sb.append (String.format (" trace-id=\"%s\"", traceId));
            }
            sb.append ('>');
            p.println (sb);
        }
        return indent + "  ";
    }
    protected void dumpTrailer (PrintStream p, String indent) {
        if (!noArmor)
            p.println (indent + "</log>");
    }
    public void dump (PrintStream p, String outer) {
        var jfr = new LogEventDump();
        jfr.begin();
        try {
            String indent = dumpHeader (p, outer);
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
                            LogRenderer<Object> renderer = LogRendererRegistry.getRenderer(o.getClass(), LogRenderer.Type.TXT);
                            if (renderer != null)
                                renderer.render(o, p, newIndent);
                            else
                                p.println(newIndent + o);
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
            dumpTrailer (p, outer);
            jfr.commit();
        }
    }
    public String getRealm() {
        return source != null ? source.getRealm() : "";
    }
    public LogEvent withTraceId (String traceId) {
        this.traceId = traceId;
        return this;
    }
    public LogEvent withTraceId (UUID uuid) {
        this.traceId = uuid.toString().replace("-", "");
        return this;
    }
    public LogEvent withSource (LogSource source) {
        setSource(source);
        return this;
    }
    public LogEvent add (Object o) {
        addMessage(o);
        return this;
    }
    public LogEvent withTraceId () {
        getTraceId();
        return this;
    }
    public String getTraceId() {
        synchronized(getPayLoad()) {
            if (traceId == null)
                traceId = UUID.randomUUID().toString().replace("-","");
            return traceId;
        }
    }

    /**
     * WARNING: payLoad is a SynchronizedList. If you intend to get a reference
     * to it in order to iterate over the list, you need to synchronize on the
     * returned object.
     *
     * <pre>
     *     synchronized (evt.getPayLoad()) {
     *        Iterator iter = evt.getPayLoad().iterator();
     *        while (iter.hasNext()) {
     *            ...
     *            ...
     *
     *        }
     *     }
     * </pre>
     * @return payLoad, which is a SynchronizedList
     */
    public List<Object> getPayLoad() {
        return payLoad;
    }
    public String toString(String indent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream (baos);
        synchronized (getPayLoad()) {
            dump(p, indent);
        }
        return baos.toString();
    }
    public String toString() {
        return toString("");
    }

    public boolean hasException() {
        return hasException;
    }
    /**
     * This is a hack for backward compatibility after accepting PR67
     * @see <a href="https://github.com/jpos/jPOS/pull/67">PR67</a>
     * @return true if ISOSource has been set
     */
    public boolean isHonorSourceLogger() {
        return honorSourceLogger;
    }

    public synchronized Instant getDumpedAt() {
        if (dumpedAt == null)
            dumpedAt = Instant.now();
        return dumpedAt;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
