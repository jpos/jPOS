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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see org.jpos.core.Configurable
 * @since jPOS 1.2
 */
public class SimpleLogListener implements LogListener {
    PrintStream p;

    public SimpleLogListener () {
        super();
        p = System.out;
    }
    public SimpleLogListener (PrintStream p) {
        this ();
        setPrintStream (p);
    }
    public synchronized void setPrintStream (PrintStream p) {
        this.p = p;
    }
    public synchronized void close() {
        if (p != null) {
            p.close();
            p = null;
        }
    }
    public synchronized LogEvent log (LogEvent ev) {
        if (p != null) {
            ev.dump (p, "");
            /*try {
                ev = printJsonLog(ev);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            p.flush();
        }
        return ev;
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private LogEvent printJsonLog(LogEvent ev) throws Exception{
        if (p != null) {
            JsonNode tag = JsonNodeFactory.instance.objectNode();

            JsonNode header = JsonNodeFactory.instance.objectNode();
            JsonNode log = JsonNodeFactory.instance.objectNode();

            ObjectNode headerNode = ((ObjectNode) header)
                    .put("id", 2016)
                    .put("realm", ev.getRealm())
                    .put("at", LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).format(formatter));
            headerNode.set(ev.getTag(),tag);

            ObjectNode logNode = ((ObjectNode) log);
            logNode.set("log",header);

            /*synchronized (ev.getPayLoad()) {
                for(Object o : ev.getPayLoad()) {
                    if (o instanceof SQLException) {
                        log = log.append("error", ConsoleLogUtil.processSQLException((SQLException)o));
                    }else if (o instanceof Throwable) {
                        log = log.append("error", ConsoleLogUtil.processThrowable((Throwable)o));
                    }else if (o instanceof Object[]) {
                        log = log.append("message", ConsoleLogUtil.processObjectArray((Object[])o));
                    }else if (o instanceof Element) {
                        try {
                            log = log.append("message", ConsoleLogUtil.processXML((Element)o));
                        } catch (JSONException ex) {
                            System.err.println("{\"log-name\":\"log-error\",\"realm\":\"log\",\"tag\":\"error\"," +
                                    "\"error\":[{\"message\":\"XML log cannot be converted into JSON, (\"" +
                                    ex.getMessage() + ") happened," +
                                    "\"stack-trace\":"+ConsoleLogUtil.parseStackTrace(ex.getStackTrace()).toString()+"}]}");
                        }
                    }else if(o instanceof ISOMsg){
                        try{
                            log = log.append("message", ISOLogUtil.getJSON((ISOMsg) o));
                        }catch(Exception ex){
                            System.err.println("{\"log-name\":\"log-error\",\"realm\":\"log\",\"tag\":\"error\"," +
                                    "\"error\":[{\"message\":\"ISOMsg log cannot be converted into JSON, (\"" +
                                    ex.getMessage() + ") happened," +
                                    "\"stack-trace\":"+ConsoleLogUtil.parseStackTrace(ex.getStackTrace()).toString()+"}]}");
                        }

                    }else if(o!=null) {
                        log = log.append("message", o.toString());
                    }else {
                        log = log.append("message", "null");
                    }
                }
            }*/

            p.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(log));
            p.flush();
        }
        return ev;
    }

}

