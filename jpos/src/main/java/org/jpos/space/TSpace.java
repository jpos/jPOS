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

package org.jpos.space;
import org.jpos.jfr.SpaceEvent;
import org.jpos.util.Loggeable;
import java.io.PrintStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * TSpace implementation
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since !.4.9
 */

@SuppressWarnings("unchecked")
public class TSpace<K,V> implements LocalSpace<K,V>, Loggeable, Runnable {
    protected Map entries;
    protected TSpace sl;    // space listeners
    public static final long GCDELAY = 5*1000;
    private static final long GCLONG = 60_000L;
    private static final long NRD_RESOLUTION = 500L;
    private static final int MAX_ENTRIES_IN_DUMP = 1000;
    private static final long ONE_MILLION = 1_000_000L;         // multiplier millis --> nanos
    private final Set[] expirables;
    private long lastLongGC = System.nanoTime();

    public TSpace () {
        super();
        entries = new HashMap ();
        expirables = new Set[] { new HashSet<K>(), new HashSet<K>() };
        SpaceFactory.getGCExecutor().scheduleAtFixedRate(this, GCDELAY, GCDELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void out (K key, V value) {
        var jfr = new SpaceEvent("out", "" + key);
        jfr.begin();
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);
        synchronized(this) {
            List l = getList(key);
            l.add (value);
            if (l.size() == 1)
                this.notifyAll ();
        }
        if (sl != null)
            notifyListeners(key, value);
        jfr.commit();
    }

    @Override
    public void out (K key, V value, long timeout) {
        var jfr = new SpaceEvent("out:tim", "" + key);
        jfr.begin();

        if (key == null || value == null) {
            jfr.commit();
            throw new NullPointerException("key=" + key + ", value=" + value);
        }
        Object v = value;
        if (timeout > 0) {
            v = new Expirable (value, System.nanoTime() + (timeout * ONE_MILLION));
        }
        synchronized (this) {
            List l = getList(key);
            l.add(v);
            if (l.size() == 1)
                this.notifyAll ();
            if (timeout > 0) {
                registerExpirable(key, timeout);
            }
        }
        if (sl != null)
            notifyListeners(key, value);
        jfr.commit();
    }

    @Override
    public synchronized V rdp (Object key) {
        var jfr = new SpaceEvent("rdp", "" + key);
        jfr.begin();
        try {
            if (key instanceof Template)
                return (V) getObject ((Template) key, false);
            return (V) getHead (key, false);
        } finally {
            jfr.commit();
        }
    }

    @Override
    public synchronized V inp (Object key) {
        var jfr = new SpaceEvent("inp", "" + key);
        jfr.begin();
        try {
            if (key instanceof Template)
                return (V) getObject ((Template) key, true);
            return (V) getHead (key, true);
        } finally {
            jfr.commit();
        }
    }

    @Override
    public synchronized V in (Object key) {
        Object obj;
        while ((obj = inp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException e) { }
        }
        return (V) obj;
    }

    @Override
    public synchronized V in  (Object key, long timeout) {
        V obj;
        long now = System.nanoTime();
        long to = now + timeout * ONE_MILLION;
        long waitFor;
        while ( (obj = inp (key)) == null &&
                (waitFor = (to - System.nanoTime())) >= 0 )
        {
            try {
                this.wait(Math.max(waitFor / ONE_MILLION, 1L));
            } catch (InterruptedException e) { }
        }
        return obj;
    }

    @Override
    public synchronized V rd  (Object key) {
        Object obj;
        while ((obj = rdp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException e) { }
        }
        return (V) obj;
    }

    @Override
    public synchronized V rd  (Object key, long timeout) {
        V obj;
        long now = System.nanoTime();
        long to = now + (timeout * ONE_MILLION);
        long waitFor;
        while ( (obj = rdp (key)) == null &&
                (waitFor = (to - System.nanoTime())) >= 0 )
        {
            try {
                this.wait(Math.max(waitFor / ONE_MILLION, 1L));
            } catch (InterruptedException e) { }
        }
        return obj;
    }

    @Override
    public synchronized void nrd  (Object key) {
        while (rdp (key) != null) {
            try {
                this.wait (NRD_RESOLUTION);
            } catch (InterruptedException ignored) { }
        }
    }

    @Override
    public synchronized V nrd  (Object key, long timeout) {
        V obj;
        long now = System.nanoTime();
        long to = now + (timeout * ONE_MILLION);
        long waitFor;
        while ( (obj = rdp (key)) != null &&
                (waitFor = (to - System.nanoTime())) >= 0 )
        {
            try {
                this.wait(Math.min(NRD_RESOLUTION,
                                   Math.max(waitFor / ONE_MILLION, 1L)));
            } catch (InterruptedException ignored) { }
        }
        return obj;
    }

    @Override
    public void run () {
        try {
            gc();
        } catch (Exception e) {
            e.printStackTrace(); // this should never happen
        }
    }

    public void gc () {
        gc(0);
        if (System.nanoTime() - lastLongGC > GCLONG*ONE_MILLION) {
            gc(1);
            lastLongGC = System.nanoTime();
        }
    }

    private void gc (int generation) {
        var jfr = new SpaceEvent("gc", Integer.toString(generation));
        jfr.begin();

        Set<K> exps;
        synchronized (this) {
            exps = expirables[generation];
            expirables[generation] = new HashSet<K>();
        }
        for (K k : exps) {
            if (rdp(k) != null) {
                synchronized (this) {
                    expirables[generation].add(k);
                }
            }
            Thread.yield ();
        }
        if (sl != null) {
            synchronized (this) {
                if (sl != null && sl.isEmpty())
                    sl = null;
            }
        }
        jfr.commit();
    }

    @Override
    public synchronized int size (Object key) {
        var jfr = new SpaceEvent("size", "" + key);
        jfr.begin();

        int size = 0;
        List l = (List) entries.get (key);
        if (l != null) 
            size = l.size();
        jfr.commit();
        return size;
    }

    @Override
    public synchronized void addListener (Object key, SpaceListener listener) {
        getSL().out (key, listener);
    }

    @Override
    public synchronized void addListener 
        (Object key, SpaceListener listener, long timeout) 
    {
        getSL().out (key, listener, timeout);
    }

    @Override
    public synchronized void removeListener 
        (Object key, SpaceListener listener) 
    {
        if (sl != null) {
            sl.inp (new ObjectTemplate (key, listener));
        }
    }
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public synchronized Set<K> getKeySet() {
        return new HashSet<K>(entries.keySet());
    }

    public String getKeysAsString () {
        StringBuilder sb = new StringBuilder();
        Object[] keys;
        synchronized (this) {
            keys = entries.keySet().toArray();
        }
        for (int i=0; i<keys.length; i++) {
            if (i > 0)
                sb.append (' ');
            sb.append (keys[i]);
        }
        return sb.toString();
    }

    @Override
    public void dump(PrintStream p, String indent) {
        var jfr = new SpaceEvent("dump", "");
        jfr.begin();

        Object[] keys;
        int size = entries.size();
        if (size > MAX_ENTRIES_IN_DUMP * 100) {
            p.printf ("%sWARNING - space too big, size=%d%n", indent, size);
            jfr.commit();
            return;
        }
        synchronized (this) {
            keys = entries.keySet().toArray();
        }
        int i=0;
        for (Object key : keys) {
            p.printf("%s<key count='%d'>%s</key>%n", indent, size(key), key);
            if (i++ > MAX_ENTRIES_IN_DUMP) {
                p.printf ("%s...%n", indent);
                p.printf ("%s...%n", indent);
                break;
            }
        }
        p.printf("%s key-count: %d%n", indent, keys.length);
        int exp0, exp1;
        synchronized (this) {
            exp0 = expirables[0].size();
            exp1 = expirables[1].size();
        }
        p.printf("%s    gcinfo: %d,%d%n", indent, exp0, exp1);
        jfr.commit();
    }

    public void notifyListeners (Object key, Object value) {
        var jfr = new SpaceEvent("notify", "" + key);
        jfr.begin();

        Object[] listeners = null;
        synchronized (this) {
            if (sl == null)
                return;
            List l = (List) sl.entries.get (key);
            if (l != null)
                listeners = l.toArray();
        }
        if (listeners != null) {
            for (Object listener : listeners) {
                Object o = listener;
                if (o instanceof Expirable)
                    o = ((Expirable) o).getValue();
                if (o instanceof SpaceListener)
                    ((SpaceListener) o).notify(key, value);
            }
        }
        jfr.commit();
    }

    @Override
    public void push (K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);
        var jfr = new SpaceEvent("push", "" + key);
        jfr.begin();
        synchronized(this) {
            List l = getList(key);
            boolean wasEmpty = l.isEmpty();
            l.add (0, value);
            if (wasEmpty)
                this.notifyAll ();
        }
        if (sl != null)
            notifyListeners(key, value);
        jfr.commit();
    }

    @Override
    public void push (K key, V value, long timeout) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);
        var jfr = new SpaceEvent("push:tim", "" + key);
        jfr.begin();
        Object v = value;
        if (timeout > 0) {
            v = new Expirable (value, System.nanoTime() + (timeout * ONE_MILLION));
        }
        synchronized (this) {
            List l = getList(key);
            boolean wasEmpty = l.isEmpty();
            l.add (0, v);
            if (wasEmpty)
                this.notifyAll ();
            if (timeout > 0) {
                registerExpirable(key, timeout);
            }
        }
        if (sl != null)
            notifyListeners(key, value);
        jfr.commit();
    }

    @Override
    public void put (K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);

        var jfr = new SpaceEvent("put", "" + key);
        jfr.begin();
        synchronized (this) {
            List l = new LinkedList();
            l.add (value);
            entries.put (key, l);
            this.notifyAll ();
        }
        if (sl != null)
            notifyListeners(key, value);
        jfr.commit();
    }

    @Override
    public void put (K key, V value, long timeout) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);
        var jfr = new SpaceEvent("put:tim", "" + key);
        jfr.begin();

        Object v = value;
        if (timeout > 0) {
            v = new Expirable (value, System.nanoTime() + (timeout * ONE_MILLION));
        }
        synchronized (this) {
            List l = new LinkedList();
            l.add (v);
            entries.put (key, l);
            this.notifyAll ();
            if (timeout > 0) {
                registerExpirable(key, timeout);
            }
        }
        if (sl != null)
            notifyListeners(key, value);
        jfr.commit();
    }

    @Override
    public boolean existAny (K[] keys) {
        for (K key : keys) {
            if (rdp(key) != null)
                return true;
        }
        return false;
    }

    @Override
    public boolean existAny (K[] keys, long timeout) {
        long now = System.nanoTime();
        long to = now + (timeout * ONE_MILLION);
        long waitFor;
        while ((waitFor = (to - System.nanoTime())) >= 0) {
            if (existAny (keys))
                return true;
            synchronized (this) {
                try {
                    this.wait(Math.max(waitFor / ONE_MILLION, 1L));
                } catch (InterruptedException e) { }
            }
        }
        return false;
    }

    /**
     * unstandard method (required for space replication) - use with care
     * @return underlying entry map
     */
    public Map getEntries () {
        return entries;
    }

    /**
     * unstandard method (required for space replication) - use with care
     * @param entries underlying entry map
     */
    public void setEntries (Map entries) {
        this.entries = entries;
    }

    private List getList (Object key) {
        List l = (List) entries.get (key);
        if (l == null) 
            entries.put (key, l = new LinkedList());
        return l;
    }

    private Object getHead (Object key, boolean remove) {
        Object obj = null;
        List l = (List) entries.get (key);
        boolean wasExpirable = false;
        while (obj == null && l != null && l.size() > 0) {
            obj = l.get(0);
            if (obj instanceof Expirable) { 
                obj = ((Expirable) obj).getValue();
                wasExpirable = true;
            }
            if (obj == null) {
                l.remove (0);
                if (l.isEmpty()) {
                    entries.remove (key);
                }
            }
        }
        if (l != null) {
            if (remove && obj != null)
                l.remove (0);
            if (l.isEmpty()) {
                entries.remove (key);
                if (wasExpirable)
                    unregisterExpirable(key);
            }
        }
        return obj;
    }

    private Object getObject (Template tmpl, boolean remove) {
        Object obj = null;
        Object key = tmpl.getKey();
        List l = (List) entries.get (key);
        if (l != null) {
            Iterator iter = l.iterator();
            boolean wasExpirable = false;
            while (iter.hasNext()) {
                obj = iter.next();
                if (obj instanceof Expirable) {
                    obj = ((Expirable) obj).getValue();
                    if (obj == null) {
                        iter.remove();
                        wasExpirable = true;
                        continue;
                    }
                }
                if (tmpl.equals (obj)) {
                    if (remove)
                        iter.remove();
                    break;
                } else
                    obj = null;
            }
            if (l.isEmpty()) {
                entries.remove (key);
                if (wasExpirable)
                    unregisterExpirable(key);
            }
        }
        return obj;
    }

    private TSpace getSL() {
        synchronized (this) {
            if (sl == null)
                sl = new TSpace();
        }
        return sl;
    }

    private void registerExpirable(K k, long t) {
        expirables[t > GCLONG ? 1 : 0].add(k);
    }

    private void unregisterExpirable(Object k) {
        for (Set<K> s : expirables)
            s.remove(k);
    }

    static class Expirable implements Comparable, Serializable {

        private static final long serialVersionUID = 0xA7F22BF5;

        Object value;

        /**
         *  When to expire, in the future, as given by monotonic System.nanoTime().<br>
         *  IMPORTANT: always use a nanosec offset from System.nanoTime()!
         */
        long expires;

        Expirable (Object value, long expires) {
            super();
            this.value = value;
            this.expires = expires;
        }

        boolean isExpired () {
            return (System.nanoTime() - expires) > 0;
        }

        @Override
        public String toString() {
            return getClass().getName() 
                + "@" + Integer.toHexString(hashCode())
                + ",value=" + value.toString()
                + ",expired=" + isExpired ();
        }

        Object getValue() {
            return isExpired() ? null : value;
        }

        @Override
        public int compareTo (Object other) {
            long diff = this.expires - ((Expirable)other).expires;
            return  diff > 0 ?  1 :
                    diff < 0 ? -1 :
                    0;
        }
    }
}
