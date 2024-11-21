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

package org.jpos.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jpos.iso.ISOUtil;
import org.jpos.util.*;
import org.jpos.rc.Result;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.jpos.transaction.ContextConstants.*;

public class Context implements Externalizable, Loggeable, Cloneable, Pausable {
    @Serial
    private static final long serialVersionUID = 2604524947983441462L;
    private transient Map<Object,Object> map; // transient map
    private Map<Object,Object> pmap;          // persistent (serializable) map
    private transient boolean trace = false;
    private CompletableFuture<Integer> pausedFuture;
    private long timeout;
    private final Lock lock = new ReentrantLock();
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
     * Get object instance from transaction context.
     *
     * @param <T> desired type of object instance
     * @param key the key of object instance
     * @return object instance if exist in context or {@code null} otherwise
     */
    public <T> T get(Object key) {
        @SuppressWarnings("unchecked")
        T obj = (T) getMap().get(key);
        return obj;
    }

    /**
     * Check if key present
     * @param key the key
     * @return true if present
     */
    public boolean hasKey(Object key) {
        return getMap().containsKey(key);
    }

    /**
     * Determines whether the specified keys are all present in the map.
     * This method accepts a variable number of key arguments and supports
     * both Object[] and String keys. When the key is a String, it can contain
     * multiple keys separated by a '|' character, and the method will return
     * true if any of those keys is present in the map. The method does not
     * support nested arrays of keys.
     *
     * @param keys A variable-length array of keys to check for in the map.
     *             These keys can be of any Object type or String containing
     *             multiple keys separated by '|'.
     * @return true if all specified keys (or any of the '|' separated keys
     *         within a String key) are present in the map, false otherwise.
     */
    public boolean hasKeys(Object... keys) {
        Map<Object,Object> m = getMap();
        return Arrays.stream(keys)
          .flatMap(obj -> obj instanceof Object[] ? Arrays.stream((Object[]) obj) : Stream.of(obj))
          .allMatch(key -> {
              if (key == null) {
                  return m.containsKey(null);  // Explicit null check
              }
              String s = (key instanceof String a ? a : key.toString()).strip();
              if (s.contains("|")) {
                  return Arrays.stream(s.split("\\|"))
                    .map(String::strip)
                    .anyMatch(m::containsKey);
              }
              return m.containsKey(s);
        });
    }

    /**
     * Returns a comma-separated string of keys that are not present in the map.
     * This method accepts a variable number of key arguments and supports
     * both Object[] and String keys. When the key is a String, it can contain
     * multiple keys separated by a '|' character, and the method will return
     * the keys not present in the map. The method does not support nested arrays of keys.
     *
     * @param keys A variable-length array of keys to check for their absence in the map.
     *             These keys can be of any Object type or String containing
     *             multiple keys separated by '|'.
     * @return A comma-separated string of keys that are not present in the map.
     *         If all the specified keys (or any of the '|' separated keys within
     *         a String key) are present in the map, an empty string is returned.
     */
    public String keysNotPresent (Object... keys) {
        Map<Object, Object> m = getMap();
        StringJoiner notFoundKeys = new StringJoiner(",");

        Arrays.stream(keys)
          .flatMap(obj -> obj instanceof Object[] ? Arrays.stream((Object[]) obj) : Stream.of(obj))
          .forEach(key -> {
              boolean keyPresent;

              if (key instanceof String s) {
                  s = s.strip();
                  if (s.contains("|")) {
                      keyPresent = Arrays.stream(s.split("\\|"))
                        .map(String::strip)
                        .anyMatch(m::containsKey);
                  } else {
                      keyPresent = m.containsKey(s);
                  }
              } else {
                  keyPresent = m.containsKey(key);
              }

              if (!keyPresent) {
                  notFoundKeys.add(key.toString().strip());
              }
          });

        return notFoundKeys.toString();
    }


    /**
     * Check key exists present persisted map
     * @param key the key
     * @return true if present
     */
    public boolean hasPersistedKey(Object key) {
        return getPMap().containsKey(key);
    }

    /**
     * Move entry to new key name
     * @param from key
     * @param to key
     * @return the entry's value (could be null if 'from' key not present)
     */
    public synchronized <T> T move(Object from, Object to) {
        T obj = get(from);
        if (obj != null) {
            put(to, obj, hasPersistedKey(from));
            remove(from);
        }
        return obj;
    }

    /**
     * Get object instance from transaction context.
     *
     * @param <T> desired type of object instance
     * @param key the key of object instance
     * @param defValue default value returned if there is no value in context
     * @return object instance if exist in context or {@code defValue} otherwise
     */
    public <T> T get(Object key, T defValue) {
        @SuppressWarnings("unchecked")
        T obj = (T) getMap().get(key);
        return obj != null ? obj : defValue;
    }

    /**
     * Transient remove
     */
    public synchronized <T> T remove(Object key) {
        getPMap().remove(key);
        @SuppressWarnings("unchecked")
        T obj = (T) getMap().remove(key);
        return obj;
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
    @SuppressWarnings("unchecked")
    public synchronized <T> T get (Object key, long timeout) {
        T obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = (T) map.get (key)) == null &&
                (now = System.currentTimeMillis()) < end)
        {
            try {
                if (end > now)
                    this.wait (end - now);
            } catch (InterruptedException ignored) { }
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

    /**
     * Creates a copy of the current Context object.
     * <p>
     * This method clones the Context object, creating new synchronized map containers
     * that are independent of the original. However, the keys and values themselves
     * are <b>not cloned</b> - both Context instances will share references to the same
     * key/value objects. Structural changes (add/remove operations) to one Context's
     * maps will not affect the other, but modifications to mutable key/value objects
     * will be visible in both Contexts.
     * </p>
     * <p>
     * The cloned Context preserves the thread-safety characteristics through
     * {@code Collections.synchronizedMap} wrappers.
     * </p>
     *
     * @return a copy of the current Context object with independent map containers
     * @throws AssertionError if cloning is not supported, which should not happen
     */
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

    /**
     * Creates a clone of the current Context instance, including only the specified keys.
     * This method accepts a variable number of key arguments and supports both
     * Object[] and String keys. When the key is a String, it can contain multiple
     * keys separated by a '|' character. The method does not support nested arrays of keys.
     *
     * @param keys A variable-length array of keys to include in the cloned context.
     *             These keys can be of any Object type or String containing multiple
     *             keys separated by '|'.
     * @return A cloned Context instance containing only the specified keys and
     *         their associated values from the original context. If none of the
     *         specified keys are present in the original context, an empty Context
     *         instance is returned.
     */
    public Context clone(Object... keys) {
        Context clonedContext = new Context();
        Map<Object, Object> m = getMap();
        Map<Object, Object> pm = getPMap();
        Arrays.stream(keys)
          .flatMap(obj -> obj instanceof Object[] ? Arrays.stream((Object[]) obj) : Stream.of(obj))
          .flatMap(obj -> {
              if (obj == null) {
                  return Stream.of((Object) null);  // Handle null as a key
              } else if (obj instanceof String s) {
                  s = s.strip();
                  return Arrays.stream(s.split("\\|"))
                    .map(String::strip)
                    .map(str -> (Object) str);  // Split strings into keys
              } else {
                  String s = obj.toString().strip();  // Convert non-String to String
                  return Arrays.stream(s.split("\\|"))
                    .map(String::strip)
                    .map(str -> (Object) str);
              }
          })
          .filter(m::containsKey)  // Check if the key exists in the map
          .forEachOrdered(key -> clonedContext.put(key, m.get(key), pm.containsKey(key)));
        return clonedContext;
    }

    /**
     * Merges the entries from the provided Context object into the current Context.
     * <p>
     * This method iterates over the entries in the given Context object 'c' and adds or updates
     * the entries in the current Context. If an entry already exists in the current Context,
     * its value will be updated. If an entry is marked as persisted in the given Context object,
     * it will also be marked as persisted in the current Context.
     * </p>
     *
     * @param c the Context object whose entries should be merged into the current Context
     */
    public void merge(Context c) {
        if (c != null) {
            c.getMap().forEach((key, value) -> put(key, value, c.hasPersistedKey(key)));
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Context context = (Context) o;
        return trace == context.trace &&
          Objects.equals(map, context.map) &&
          Objects.equals(pmap, context.pmap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, pmap, trace);
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

    @JsonIgnore
    public Map<Object,Object> getMapClone() {
        Map<Object,Object> cloned = Collections.synchronizedMap (new LinkedHashMap<>());
        synchronized(getMap()) {
            cloned.putAll(map);
        }
        return cloned;
    }

    protected void dumpMap (PrintStream p, String indent) {
        if (map != null) {
            getMapClone().entrySet().forEach(e -> dumpEntry(p, indent, e));
        }
    }

    protected void dumpEntry (PrintStream p, String indent, Map.Entry<Object,Object> entry) {
        String key = getKeyName(entry.getKey());
        if (key.startsWith(".") || key.startsWith("*"))
            return; // see jPOS-63

        p.printf("%s%s%s: ", indent, key, pmap != null && pmap.containsKey(key) ? "(P)" : "");
        Object value = entry.getValue();
        if (value instanceof Loggeable) {
            p.println();
            try {
                ((Loggeable) value).dump(p, indent + " ");
            } catch (Exception ex) {
                ex.printStackTrace(p);
            }
            p.print(indent);
        } else if (value instanceof Element) {
            p.println();
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            out.getFormat().setLineSeparator(System.lineSeparator());
            try {
                out.output((Element) value, p);
            } catch (IOException ex) {
                ex.printStackTrace(p);
            }
            p.println();
        } else if (value instanceof byte[] b) {
            p.println();
            p.println(ISOUtil.hexdump(b));
            p.print(indent);
        }
        else if (value instanceof short[]) {
            p.print (Arrays.toString((short[]) value));
        } else if (value instanceof int[]) {
            p.print(Arrays.toString((int[]) value));
        } else if (value instanceof long[]) {
            p.print(Arrays.toString((long[]) value));
        } else if (value instanceof Object[]) {
            p.print (ISOUtil.normalize(Arrays.toString((Object[]) value), true));
        }
        else if (value instanceof LogEvent) {
            ((LogEvent) value).dump(p, indent);
            p.print(indent);
        } else if (value != null) {
            try {
                LogUtil.dump(p, indent, value.toString());
            } catch (Exception ex) {
                ex.printStackTrace(p);
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
        LogEvent evt = get (LOGEVT.toString());
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
        Profiler prof = get (PROFILER.toString());
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
        Result result = get (RESULT.toString());
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
        if (msg != getMap()) // prevent recursive call to dump (and StackOverflow)
            getLogEvent().addMessage (msg);
    }
    /**
     * add a checkpoint to the profiler
     */
    public void checkPoint (String detail) {
        getProfiler().checkPoint (detail);
    }

    public boolean isTrace() {
        return trace;
    }
    public void setTrace(boolean trace) {
        if (trace)
            getProfiler();
        this.trace = trace;
    }

    @Override
    public Future<Integer> pause() {
        try {
            lock.lock();
            if (pausedFuture == null)
                pausedFuture = new CompletableFuture<>();
            else if (!pausedFuture.isDone())
                throw new IllegalStateException("already paused");
        } finally {
            lock.unlock();
        }
        return pausedFuture;
    }

    @Override
    public void resume(int result) {
        try {
            lock.lock();
            if (pausedFuture == null)
                pausedFuture = new CompletableFuture<>();
            pausedFuture.complete(result);
        } finally {
            lock.unlock();
        }
    }
    @Override
    public void reset () {
        try {
            lock.lock();
            pausedFuture = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    private String getKeyName(Object keyObject) {
        return keyObject instanceof String ? (String) keyObject :
          Caller.shortClassName(keyObject.getClass().getName())+"."+ keyObject;
    }
}
