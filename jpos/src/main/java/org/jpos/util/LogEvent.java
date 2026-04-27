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
 * A single structured log event that carries a tag, realm, payload items, and optionally a {@link Throwable}.
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
    private Map<String,String> tags;

    /**
     * Constructs an empty event with the given tag.
     *
     * @param tag log tag (level/event name)
     */
    public LogEvent (String tag) {
        super();
        this.tag = tag;
        createdAt = Instant.now();
        this.payLoad = Collections.synchronizedList (new ArrayList<>());
    }

    /** Default constructor; uses the {@code info} tag. */
    public LogEvent () {
        this("info");
    }
    /**
     * Constructs an event with the given tag and an initial payload entry.
     *
     * @param tag log tag (level/event name)
     * @param msg initial payload entry
     */
    public LogEvent (String tag, Object msg) {
        this (tag);
        addMessage(msg);
    }
    /**
     * Constructs an event tied to a {@link LogSource}.
     *
     * @param source source whose logger and realm govern this event
     * @param tag log tag (level/event name)
     */
    public LogEvent (LogSource source, String tag) {
        this (tag);
        this.source  = source;
        honorSourceLogger = true;
    }
    /**
     * Constructs an event tied to a {@link LogSource} with an initial payload entry.
     *
     * @param source source whose logger and realm govern this event
     * @param tag log tag (level/event name)
     * @param msg initial payload entry
     */
    public LogEvent (LogSource source, String tag, Object msg) {
        this (tag);
        this.source  = source;
        honorSourceLogger = true;
        addMessage(msg);
    }
    /**
     * Returns the log tag.
     *
     * @return the event tag
     */
    public String getTag() {
        return tag;
    }
    /**
     * Sets the log tag for this event.
     * @param tag the log tag to set
     */
    public void setTag (String tag) {
        this.tag = tag;
    }
    /**
     * Adds a message or object to this event's payload.
     * @param msg the message or object to add
     */
    public void addMessage (Object msg) {
        payLoad.add (msg);
        if (msg instanceof Throwable)
            hasException = true;
    }
    /**
     * Adds a message wrapped in an XML tag to this event's payload.
     * @param tagname the XML tag name to wrap the message in
     * @param message the message text
     */
    public void addMessage (String tagname, String message) {
        payLoad.add ("<"+tagname+">"+message+"</"+tagname+">");
    }
    /**
     * Returns the {@link LogSource} associated with this event, if any.
     *
     * @return the source, or {@code null} if not set
     */
    public LogSource getSource() {
        return source;
    }
    /**
     * Replaces the {@link LogSource} associated with this event.
     *
     * @param source new source (may be {@code null})
     */
    public void setSource(LogSource source) {
        this.source = source;
    }
    /**
     * Controls whether the XML wrapper is suppressed in log output.
     * @param noArmor if true, suppress the XML wrapper
     */
    public void setNoArmor (boolean noArmor) {
        this.noArmor = noArmor;
    }
    /**
     * Writes the log event header to the given PrintStream.
     * @param p the PrintStream to write the header to
     * @param indent the indentation prefix
     * @return the inner indentation string for nested content
     */
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
            String traceId = tags != null ? tags.get("trace-id") : null;
            if (traceId != null) {
                sb.append (String.format (" trace-id=\"%s\"", traceId));
            }
            sb.append ('>');
            p.println (sb);
        }
        return indent + "  ";
    }
    /**
     * Writes the log event trailer to the given PrintStream.
     * @param p the PrintStream to write the trailer to
     * @param indent the indentation prefix
     */
    protected void dumpTrailer (PrintStream p, String indent) {
        if (!noArmor)
            p.println (indent + "</log>");
    }
    /**
     * Dumps the full log event to the given PrintStream.
     * @param p     the PrintStream to dump to
     * @param outer the outer indentation string
     */
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
    /**
     * Returns the realm of the associated source, or empty when no source is set.
     *
     * @return the source's realm, or an empty string
     */
    public String getRealm() {
        return source != null ? source.getRealm() : "";
    }
    /**
     * Sets the {@code trace-id} tag explicitly.
     *
     * @param traceId trace identifier
     * @return this event for chaining
     */
    public LogEvent withTraceId (String traceId) {
        return withTag("trace-id", traceId);
    }
    /**
     * Sets the {@code trace-id} tag from a UUID (dashes stripped).
     *
     * @param uuid trace UUID
     * @return this event for chaining
     */
    public LogEvent withTraceId (UUID uuid) {
        return withTag("trace-id", uuid.toString().replace("-", ""));
    }
    /**
     * Adds or overwrites a tag on this event.
     *
     * @param key tag name
     * @param value tag value
     * @return this event for chaining
     */
    public LogEvent withTag(String key, String value) {
        if (tags == null)
            tags = new LinkedHashMap<>();
        tags.put(key, value);
        return this;
    }
    /**
     * Adds the supplied tags to this event.
     *
     * @param map tags to add (ignored if {@code null} or empty)
     * @return this event for chaining
     */
    public LogEvent withTags(Map<String,String> map) {
        if (map != null && !map.isEmpty()) {
            if (tags == null)
                tags = new LinkedHashMap<>();
            tags.putAll(map);
        }
        return this;
    }
    /**
     * Returns an unmodifiable view of this event's tags.
     *
     * @return event tags, or an empty map if none have been set
     */
    public Map<String,String> getTags() {
        return tags != null ? Collections.unmodifiableMap(tags) : Collections.emptyMap();
    }
    /**
     * Sets the {@link LogSource} associated with this event and returns it for chaining.
     *
     * @param source new source
     * @return this event for chaining
     */
    public LogEvent withSource (LogSource source) {
        setSource(source);
        return this;
    }
    /**
     * Appends a payload entry and returns this event for chaining.
     *
     * @param o payload entry
     * @return this event for chaining
     */
    public LogEvent add (Object o) {
        addMessage(o);
        return this;
    }
    /**
     * Ensures a {@code trace-id} tag is present, generating one if needed.
     *
     * @return this event for chaining
     */
    public LogEvent withTraceId () {
        getTraceId();
        return this;
    }
    /**
     * Returns the current trace-id, generating one if absent.
     *
     * @return the trace-id (never {@code null})
     */
    public String getTraceId() {
        synchronized(getPayLoad()) {
            String traceId = tags != null ? tags.get("trace-id") : null;
            if (traceId == null) {
                traceId = UUID.randomUUID().toString().replace("-","");
                withTag("trace-id", traceId);
            }
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
    /**
     * Renders this event to a string with the given indent prefix.
     *
     * @param indent indent prefix to apply to every emitted line
     * @return string rendering of this event
     */
    public String toString(String indent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream (baos);
        synchronized (getPayLoad()) {
            dump(p, indent);
        }
        return baos.toString();
    }
    /**
     * Renders this event to a string with no leading indent.
     *
     * @return string rendering of this event
     */
    public String toString() {
        return toString("");
    }

    /**
     * Indicates whether the payload contains a {@link Throwable}.
     *
     * @return {@code true} if any payload entry is a {@link Throwable}
     */
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

    /**
     * Returns the time at which this event was first dumped, capturing it on first call.
     *
     * @return the dump timestamp
     */
    public synchronized Instant getDumpedAt() {
        if (dumpedAt == null)
            dumpedAt = Instant.now();
        return dumpedAt;
    }
    /**
     * Returns the time at which this event was constructed.
     *
     * @return the creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
