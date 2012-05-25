/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import org.jpos.util.DefaultTimer;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.util.*;

/**
 * TSpace implementation
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since !.4.9
 */
public class TSpace<K,V> extends TimerTask implements LocalSpace<K,V>, Loggeable {
    protected Map entries;
    protected TSpace sl;    // space listeners
    public static final long GCDELAY = 60*1000;
    private Set[] expirables;
    private static final long GCLONG = GCDELAY * 5;
    private long lastLongGC = System.currentTimeMillis();

    public TSpace () {
        super();
        entries = new HashMap ();
        expirables = new Set[] { new HashSet<K>(), new HashSet<K>() };
        DefaultTimer.getTimer().schedule (this, GCDELAY, GCDELAY);
    }
    public void out (K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);
        synchronized(this) {
            getList (key).add (value);
            this.notifyAll ();
        }
        if (sl != null)
            notifyListeners(key, value);
    }
    public void out (K key, V value, long timeout) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);
        Object v = value;
        if (timeout > 0) {
            v = new Expirable (value, System.currentTimeMillis() + timeout);
        }
        synchronized (this) {
            getList (key).add (v);
            this.notifyAll ();
            if (timeout > 0) {
                registerExpirable(key, timeout);
            }
        }
        if (sl != null)
            notifyListeners(key, value);
    }
    public synchronized V rdp (Object key) {
        if (key instanceof Template)
            return (V) getObject ((Template) key, false);
        return (V) getHead (key, false);
    }
    public synchronized V inp (Object key) {
        if (key instanceof Template)
            return (V) getObject ((Template) key, true);
        return (V) getHead (key, true);
    }
    public synchronized V in (Object key) {
        Object obj;
        while ((obj = inp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException e) { }
        }
        return (V) obj;
    }
    public synchronized V in  (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = inp (key)) == null && 
                ((now = System.currentTimeMillis()) < end))
        {
            try {
                this.wait (end - now);
            } catch (InterruptedException e) { }
        }
        return (V) obj;
    }
    public synchronized V rd  (Object key) {
        Object obj;
        while ((obj = rdp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException e) { }
        }
        return (V) obj;
    }
    public synchronized V rd  (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = rdp (key)) == null && 
                ((now = System.currentTimeMillis()) < end))
        {
            try {
                this.wait (end - now);
            } catch (InterruptedException e) { }
        }
        return (V) obj;
    }
    public void run () {
        try {
            gc();
        } catch (Exception e) {
            e.printStackTrace(); // this should never happen
        }
    }
    public void gc () {
        gc(0);
        if (System.currentTimeMillis() - lastLongGC > GCLONG) {
            gc(1);
            lastLongGC = System.currentTimeMillis();
        }
    }
    private void gc (int generation) {
        Set<K> exps = expirables[generation];
        synchronized (this) {
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
    }

    public synchronized int size (Object key) {
        int size = 0;
        List l = (List) entries.get (key);
        if (l != null) 
            size = l.size();
        return size;
    }
    public synchronized void addListener (Object key, SpaceListener listener) {
        getSL().out (key, listener);
    }
    public synchronized void addListener 
        (Object key, SpaceListener listener, long timeout) 
    {
        getSL().out (key, listener, timeout);
    }
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
    public Set getKeySet() {
        return entries.keySet();
    }
    public String getKeysAsString () {
        StringBuffer sb = new StringBuffer();
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
    public void dump(PrintStream p, String indent) {
        Object[] keys;
        synchronized (this) {
            keys = entries.keySet().toArray();
        }
        for (int i=0; i<keys.length; i++) {
            p.printf ("%s<key count='%d'>%s</key>\n", indent, size(keys[i]), keys[i]);
        }
        p.println(indent+"<keycount>"+(keys.length-1)+"</keycount>");
        int exp0, exp1;
        synchronized (this) {
            exp0 = expirables[0].size();
            exp1 = expirables[1].size();
        }
        p.println(String.format("%s<gcinfo>%d,%d</gcinfo>\n", indent, exp0, exp1));
    }
    public void notifyListeners (Object key, Object value) {
        Object[] listeners = null;
        synchronized (this) {
            if (sl == null)
                return;
            List l = (List) sl.entries.get (key);
            if (l != null)
                listeners = l.toArray();
        }
        if (listeners != null) {
            for (int i=0; i<listeners.length; i++) {
                Object o = listeners[i];
                if (o instanceof Expirable)
                    o = ((Expirable)o).getValue();
                if (o instanceof SpaceListener)
                    ((SpaceListener) o).notify (key, value);
            }
        }
    }
    public void push (K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);
        synchronized(this) {
            getList (key).add (0, value);
            this.notifyAll ();
        }
        if (sl != null)
            notifyListeners(key, value);
    }

    public void push (K key, V value, long timeout) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);
        Object v = value;
        if (timeout > 0) {
            v = new Expirable (value, System.currentTimeMillis() + timeout);
        }
        synchronized (this) {
            getList (key).add (0, v);
            this.notifyAll ();
            if (timeout > 0) {
                registerExpirable(key, timeout);
            }
        }
        if (sl != null)
            notifyListeners(key, value);
    }

    public void put (K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);

        synchronized (this) {
            List l = new LinkedList();
            l.add (value);
            entries.put (key, l);
            this.notifyAll ();
        }
        if (sl != null)
            notifyListeners(key, value);
    }
    public void put (K key, V value, long timeout) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);
        Object v = value;
        if (timeout > 0) {
            v = new Expirable (value, System.currentTimeMillis() + timeout);
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
    }
    public boolean existAny (K[] keys) {
        for (int i=0; i<keys.length; i++) {
            if (rdp (keys[i]) != null)
                return true;
        }
        return false;
    }
    public boolean existAny (K[] keys, long timeout) {
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while (((now = System.currentTimeMillis()) < end)) {
            if (existAny (keys))
                return true;
            synchronized (this) {
                try {
                    wait (end - now);
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
        if (obj != null && remove) {
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
        List l = (List) entries.get (tmpl.getKey());
        if (l == null)
            return obj;

        Iterator iter = l.iterator();
        while (iter.hasNext()) {
            obj = iter.next();
            if (obj instanceof Expirable) {
                obj = ((Expirable) obj).getValue();
                if (obj == null) {
                    iter.remove();
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
    static class Expirable implements Comparable {
        Object value;
        long expires;

        public Expirable (Object value, long expires) {
            super();
            this.value = value;
            this.expires = expires;
        }
        public boolean isExpired () {
            return expires < System.currentTimeMillis ();
        }
        public String toString() {
            return getClass().getName() 
                + "@" + Integer.toHexString(hashCode())
                + ",value=" + value.toString()
                + ",expired=" + isExpired ();
        }
        public Object getValue() {
            return isExpired() ? null : value;
        }
        public int compareTo (Object obj) {
            Expirable other = (Expirable) obj;
            long otherExpires = other.expires;
            if (otherExpires == expires)
                return 0;
            else if (expires < otherExpires)
                return -1;
            else 
                return 1;
        }
    }
}
