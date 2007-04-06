/*
 * Copyright (c) 2006 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.transaction;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Collections;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Externalizable;
import java.io.PrintStream;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jpos.util.Loggeable;
import org.jpos.util.LogEvent;
import org.jpos.util.Profiler;
import org.jpos.iso.ISOUtil;

public class Context implements Externalizable, Loggeable, Pausable {
    private transient Map map; // transient map
    private Map pmap;          // persistent (serializable) map

    public static String LOGEVT = "LOGEVT";
    public static String PROFILER = "PROFILER";
    public static String PAUSED_TRANSACTION = ":paused_transaction";

    public Context () {
        super ();
    }
    /**
     * puts an Object in the transient Map
     */
    public void put (Object key, Object value) {
        getMap().put (key, value);
    }
    /**
     * puts an Object in the transient Map
     */
    public void put (Object key, Object value, boolean persist) {
        getMap().put (key, value);
        if (persist && value instanceof Serializable)
            getPMap().put (key, value);
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
        return (String) getMap().get (key);
    }
    public String getString (Object key, Object defValue) {
        return (String) get (key, defValue);
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
                ((now = System.currentTimeMillis()) < end))
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
            Object k = in.readObject();
            Object v = in.readObject();
            map.put (k, v);
            pmap.put (k, v);
        }
    }
    /**
     * @return persistent map
     */
    private synchronized Map getPMap() {
        if (pmap == null)
            pmap = Collections.synchronizedMap (new LinkedHashMap ());
        return pmap;
    }
    /**
     * @return transient map
     */
    private synchronized Map getMap() {
        if (map == null)
            map = Collections.synchronizedMap (new LinkedHashMap ());
        return map;
    }
    private void dumpMap (PrintStream p, String indent) {
        if (map == null)
            return;

        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next ();
            if (pmap != null && pmap.containsKey(entry.getKey())) 
                p.print (indent + "<entry key='" + entry.getKey().toString() + "' p='true'>"); 
            else
                p.print (indent + "<entry key='" + entry.getKey().toString() + "'>"); 
            Object value = entry.getValue();
            if (value instanceof Loggeable) {
                p.println ("");
                ((Loggeable) value).dump (p, indent + " ");
                p.print (indent);
            } else if (value instanceof Element) {
                p.println ("");
                p.println (indent+ "<![CDATA[");
                XMLOutputter out = new XMLOutputter (Format.getPrettyFormat ());
                out.getFormat().setLineSeparator ("\n");
                try {
                    out.output ((Element) value, p);
                } catch (IOException ex) {
                    ex.printStackTrace (p);
                }
                p.println ("");
                p.println (indent + "]]>");
            } else if (value instanceof byte[]) {
                byte[] b = (byte[]) value;
                p.println ("");
                p.println (ISOUtil.hexdump (b));
            } else if (value != null) {
                p.print (value.toString ());
            } else {
                p.print ("nil");
            }
            p.println ("</entry>");
        }
    }
    /**
     * return a LogEvent used to store trace information
     * about this transaction.
     * If there's no LogEvent there, it creates one.
     * @return LogEvent
     */
    public LogEvent getLogEvent () {
        LogEvent evt = (LogEvent) get (LOGEVT);
        if (evt == null) {
            synchronized (this) {
                evt = (LogEvent) get (LOGEVT);
                if (evt == null) {
                    evt = new LogEvent ("log");
                    put (LOGEVT, evt);
                }
            }
        }
        return evt;
    }
    /**
     * return (or creates) a Profiler object
     * @return Profiler object
     */
    public Profiler getProfiler () {
        Profiler prof = (Profiler) get (PROFILER);
        if (prof == null) {
            synchronized (this) {
                prof = (Profiler) get (PROFILER);
                if (prof == null) {
                    prof = new Profiler();
                    put (PROFILER, prof);
                }
            }
        }
        return prof;
    }
    /**
     * adds a trace message
     * @msg trace information
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
        put (PAUSED_TRANSACTION, p);
    }
    public PausedTransaction getPausedTransaction() {
        return (PausedTransaction) get (PAUSED_TRANSACTION);

    }
    static final long serialVersionUID = 6056487212221438338L;
}

