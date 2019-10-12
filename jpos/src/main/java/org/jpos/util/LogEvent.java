/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2019 jPOS Software SRL
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
import org.jpos.util.log.event.BaseLogEvent;
import org.jpos.util.log.event.LogEventFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.jpos.util.log.format.XML.XML_LABEL;

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
    private BaseLogEvent baseLogEvent;

    public LogEvent (String tag) {
        super();
        this.tag = tag;
        createdAt = Instant.now();
        this.payLoad = Collections.synchronizedList (new ArrayList<>());
        this.baseLogEvent = LogEventFactory.getLogEvent(XML_LABEL);
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
    public BaseLogEvent getBaseLogEvent() {
        return baseLogEvent;
    }
    public void setBaseLogEvent(BaseLogEvent baseLogEvent) {
        this.baseLogEvent = baseLogEvent;
    }
    protected String dumpHeader (PrintStream p, String indent) {
        return baseLogEvent.dumpHeader(p,indent,getRealm(),dumpedAt,createdAt,noArmor);
    }
    protected void dumpTrailer (PrintStream p, String indent) {
        baseLogEvent.dumpTrailer(p,indent,noArmor);
    }

    public void dump (PrintStream p, String outer) {
        baseLogEvent.dump(p,outer,getRealm(),dumpedAt,createdAt,payLoad,noArmor,tag);
    }
    public String getRealm() {
        return source != null ? source.getRealm() : "";
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

    /**
     * This is a hack for backward compatibility after accepting PR67
     * @see <a href="https://github.com/jpos/jPOS/pull/67">PR67</a>
     * @return true if ISOSource has been set
     */
    public boolean isHonorSourceLogger() {
        return honorSourceLogger;
    }
}
