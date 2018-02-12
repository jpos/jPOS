/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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

package org.jpos.transaction;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Caller;
import org.jpos.util.LogEvent;
import org.jpos.util.Loggeable;
import org.jpos.util.Profiler;
import org.jpos.rc.Result;

import java.io.*;
import java.util.*;

import static org.jpos.transaction.ContextConstants.*;

public class Context implements Externalizable, Loggeable, Pausable, Cloneable {
    private static final long serialVersionUID = -764389940148411076L;
    private transient Map<Object,Object> map; // transient map
    private Map<Object,Object> pmap;          // persistent (serializable) map
    private long timeout;
    private boolean resumeOnPause = false;
    private transient boolean trace = false;

    public Context () {
        super ();
    }

    /**
     * puts an Object in the transient Map
     */
    public void put (Object key, Object value) {
        if (trace) {
            getProfiler().checkPoint(
                String.format("%s='%s' [%s]", getKeyName(key), value, Caller.info(1))
            );
        }
        getMap().put (key, value);
        synchronized (this) {
            notifyAll();
        }
    }
    /**
     * puts an Object in the transient Map
     */
    public void put (Object key, Object value, boolean persist) {
        if (trace) {
            getProfiler().checkPoint(
                String.format("%s(P)='%s' [%s]", getKeyName(key), value, Caller.info(1))
            );
        }
        if (persist && value instanceof Serializable)
            getPMap().put (key, value);
        getMap().put(key, value);
    }

    /**
     * Persists a transient entry
     * @param key the key
     */
    public void persist (Object key) {
        Object value = get(key);
        if (value instanceof Serializable)
            getPMap().put (key, value);
    }

    /**
     * Evicts a persistent entry
     * @param key the key
     */
    public void evict (Object key) {
        getPMap().remove (key);
    }
    /**
     * Get
     */
    public Object get (Object key) {
        return getMap().get (key);
    }
    public Object get (Object key, Object defValue) {
        Object obj = getMap().get (key);
        return obj != null ? obj : defValue;
    }
    /**
     * Transient remove
     */
    public synchronized Object remove (Object key) {
        getPMap().remove (key);
        return getMap().remove (key);
    }
    public String getString (Object key) {
        Object obj = getMap().get (key);
        if (obj instanceof String)
            return (String) obj;
        else if (obj != null)
            return obj.toString();
        return null;
    }
    public String getString (Object key, String defValue) {
        Object obj = getMap().get (key);
        if (obj instanceof String)
            return (String) obj;
        else if (obj != null)
            return obj.toString();
        return defValue;
    }
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.println (indent + "<context>");
        dumpMap (p, inner);
        p.println (indent + "</context>");
    }
    /**
     * persistent get with timeout
     * @param key the key
     * @param timeout timeout
     * @return object (null on timeout)
     */
    public synchronized Object get (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = map.get (key)) == null &&
                (now = System.currentTimeMillis()) < end)
        {
            try {
                this.wait (end - now);
            } catch (InterruptedException e) { }
        }
        return obj;
    }
    public void writeExternal (ObjectOutput out) throws IOException {
        out.writeByte (0);  // reserved for future expansion (version id)
        Set s = getPMap().entrySet();
        out.writeInt (s.size());
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }
    public void readExternal  (ObjectInput in) 
        throws IOException, ClassNotFoundException
    {
        in.readByte();  // ignore version for now
        getMap();       // force creation of map
        getPMap();      // and pmap
        int size = in.readInt();
        for (int i=0; i<size; i++) {
            String k = (String) in.readObject();
            Object v = in.readObject();
            map.put (k, v);
            pmap.put (k, v);
        }
    }
    @Override
    public Context clone() {
        try {
            Context context = (Context) super.clone();
            if (map != null) {
                context.map = Collections.synchronizedMap (new LinkedHashMap<>());
                context.map.putAll(map);
            }
            if (pmap != null) {
                context.pmap = Collections.synchronizedMap (new LinkedHashMap<>());
                context.pmap.putAll(pmap);
            }
            return context;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Should not happen
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Context context = (Context) o;
        return timeout == context.timeout &&
          resumeOnPause == context.resumeOnPause &&
          trace == context.trace &&
          Objects.equals(map, context.map) &&
          Objects.equals(pmap, context.pmap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, pmap, timeout, resumeOnPause, trace);
    }

    /**
     * @return persistent map
     */
    private synchronized Map<Object,Object> getPMap() {
        if (pmap == null)
            pmap = Collections.synchronizedMap (new LinkedHashMap<> ());
        return pmap;
    }
    /**
     * @return transient map
     */
    public synchronized Map<Object,Object> getMap() {
        if (map == null)
            map = Collections.synchronizedMap (new LinkedHashMap<>());
        return map;
    }
    protected void dumpMap (PrintStream p, String indent) {
        if (map == null)
            return;

        for (Map.Entry<Object,Object> entry : map.entrySet()) {
            dumpEntry(p, indent, entry);
        }
    }

    protected void dumpEntry (PrintStream p, String indent, Map.Entry<Object,Object> entry) {
        String key = getKeyName(entry.getKey());
        if (key.startsWith(".") || key.startsWith("*"))
            return; // see jPOS-63

        p.printf("%s%s%s: ", indent, key, pmap != null && pmap.containsKey(key) ? "(P)" : "");
        Object value = entry.getValue();
        if (value instanceof Loggeable) {
            p.println("");
            ((Loggeable) value).dump(p, indent + " ");
            p.print(indent);
        } else if (value instanceof Element) {
            p.println("");
            p.println(indent + "<![CDATA[");
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            out.getFormat().setLineSeparator(System.lineSeparator());
            try {
                out.output((Element) value, p);
            } catch (IOException ex) {
                ex.printStackTrace(p);
            }
            p.println("");
            p.println(indent + "]]>");
        } else if (value instanceof byte[]) {
            byte[] b = (byte[]) value;
            p.println("");
            p.println(ISOUtil.hexdump(b));
            p.print(indent);
        } else if (value instanceof LogEvent) {
            ((LogEvent) value).dump(p, indent);
            p.print(indent);
        } else if (value != null) {
            try {
                p.print(ISOUtil.normalize(value.toString(), true));
            } catch (Exception e) {
                p.println(e.getMessage());
                p.print(indent);
            }
        }
        p.println();
    }
    /**
     * return a LogEvent used to store trace information
     * about this transaction.
     * If there's no LogEvent there, it creates one.
     * @return LogEvent
     */
    synchronized public LogEvent getLogEvent () {
        LogEvent evt = (LogEvent) get (LOGEVT.toString());
        if (evt == null) {
            evt = new LogEvent ();
            evt.setNoArmor(true);
            put (LOGEVT.toString(), evt);
        }
        return evt;
    }
    /**
     * return (or creates) a Profiler object
     * @return Profiler object
     */
    synchronized public Profiler getProfiler () {
        Profiler prof = (Profiler) get (PROFILER.toString());
        if (prof == null) {
            prof = new Profiler();
            put (PROFILER.toString(), prof);
        }
        return prof;
    }

    /**
     * return (or creates) a Resultr object
     * @return Profiler object
     */
    synchronized public Result getResult () {
        Result result = (Result) get (RESULT.toString());
        if (result == null) {
            result = new Result();
            put (RESULT.toString(), result);
        }
        return result;
    }

    /**
     * adds a trace message
     * @param msg trace information
     */
    public void log (Object msg) {
        getLogEvent().addMessage (msg);
    }
    /**
     * add a checkpoint to the profiler
     */
    public void checkPoint (String detail) {
        getProfiler().checkPoint (detail);
    }
    public void setPausedTransaction (PausedTransaction p) {
        put (PAUSED_TRANSACTION.toString(), p);
        synchronized (this) {
            if (resumeOnPause) {
                resume();
            }
        }
    }
    public PausedTransaction getPausedTransaction() {
        return (PausedTransaction) get (PAUSED_TRANSACTION.toString());
    }
    public void setTimeout (long timeout) {
        this.timeout = timeout;
    }
    public long getTimeout () {
        return timeout;
    }
    public synchronized void resume() {
        PausedTransaction pt = getPausedTransaction();
        if (pt != null && !pt.isResumed()) {
            pt.setResumed (true);
            pt.getTransactionManager().push (this);
        } else {
            resumeOnPause = true;
        }
    }
    public boolean isTrace() {
        return trace;
    }
    public void setTrace(boolean trace) {
        if (trace)
            getProfiler();
        this.trace = trace;
    }

    private String getKeyName(Object keyObject) {
        return keyObject instanceof String ? (String) keyObject :
          Caller.shortClassName(keyObject.getClass().getName())+"."+keyObject.toString();
    }
}
