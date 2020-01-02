/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

import java.util.*;

/**
 * Transient Space implementation
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 2.0
 */
@SuppressWarnings("unchecked")
public class TransientSpace implements LocalSpace, TransientSpaceMBean {
    protected Map map;
    static LocalSpace defaultSpace = new TransientSpace ();

    public TransientSpace () {
        super();
        map = new HashMap ();
    }
    public void out (Object key, Object value) {
        List listeners;
        synchronized (this) {
            Data data = (Data) map.get (key);
            if (data == null) 
                map.put (key, data = new Data ());
            data.add (value);
            this.notifyAll ();
            listeners = data.getListeners();
        }
        if (listeners != null) {
            Iterator iter = listeners.iterator();
            while (iter.hasNext()) {
                ((SpaceListener)iter.next()).notify (key, value);
            }
        }
    }
    public void out (Object id, Object value, long timeout) {
        LeasedReference ref = new LeasedReference (value, timeout);
        out (id, ref);
    }
    public synchronized Object rdp (Object key) {
        Object obj = null;
        Data data  = (Data) map.get (key);
        if (data != null) {
            return data.get (key);
        }
        return obj;
    }
    public synchronized Object inp (Object key) {
        Object obj = null;
        Data data  = (Data) map.get (key);
        if (data != null) {
            obj = data.remove ();
            if (data.isEmpty ())
                map.remove (key);
        }
        return obj;
    }
    public synchronized Object in (Object key) {
        Object obj;
        while ((obj = inp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException e) { }
        }
        return obj;
    }
    public synchronized Object in  (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = inp (key)) == null &&
                (now = System.currentTimeMillis()) < end)
        {
            try {
                this.wait (end - now);
            } catch (InterruptedException e) { }
        }
        return obj;
    }
    public synchronized Object rd  (Object key) {
        Object obj;
        while ((obj = rdp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException e) { }
        }
        return obj;
    }
    public synchronized Object rd  (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = rdp (key)) == null &&
                (now = System.currentTimeMillis()) < end)
        {
            try {
                this.wait (end - now);
            } catch (InterruptedException e) { }
        }
        return obj;
    }
    public void put (Object key, Object value) {
        throw new SpaceError ("Unsupported operation");
    }
    public void put (Object key, Object value, long timeout) {
        throw new SpaceError ("Unsupported operation");
    }
    public synchronized void addListener    (Object key, SpaceListener listener) {
        Data data = (Data) map.get (key);
        if (data == null) 
            map.put (key, data = new Data());
        data.addListener (listener);
    }
    public synchronized void addListener 
        (Object key, SpaceListener listener, long timeout) 
    {
        // Not properly implemented, use new TSpace class instead
        addListener (key, listener);
    }
    public synchronized void removeListener (Object key, SpaceListener listener) {
        Data data = (Data) map.get (key);
        if (data != null)
            data.removeListener (listener);
    }

    @SuppressWarnings("unchecked")
    protected static final class Data {
        LinkedList data;
        LinkedList listeners;

        protected Data () {
            super();
            data = new LinkedList ();
            listeners = null;
        }
        protected Data (Object value) {
            this ();
            add (value);
        }
        protected void add (Object value) {
            data.add (value);
        }
        protected Object get (Object value) {
            Object obj = null;
            while (size() > 0) {
                obj = data.getFirst();
                if (obj instanceof LeasedReference) {
                    obj = ((LeasedReference)obj).get ();
                    if (obj == null) {
                        data.removeFirst ();
                        continue;
                    }
                }
                break;
            }
            return obj;
        }
        protected int size () {
            return data.size ();
        }
        protected Object remove () {
            Object obj = null;
            while (size() > 0) {
                obj = data.removeFirst();
                if (obj instanceof LeasedReference) {
                    LeasedReference ref = (LeasedReference) obj;
                    obj = ref.get ();
                    if (obj == null) {
                        continue;
                    }
                    ref.discard ();
                }
                break;
            }
            return obj;
        }
        protected boolean isEmpty () {
            return data.isEmpty () && listeners == null;
        }
        protected void addListener (SpaceListener l) {
            if (listeners == null)
                listeners = new LinkedList ();
            listeners.add (l);
        }
        protected void removeListener (SpaceListener l) {
            if (listeners != null) {
                listeners.remove (l);
                if (listeners.isEmpty ())
                    listeners = null;
            }
        }
        protected List getListeners () {
            return listeners;
        }
    }
    public static LocalSpace getSpace () {
        return defaultSpace;
    }
    public static LocalSpace getSpace (String spaceName) {
        String key = "jpos:space/"+spaceName;
        Object obj = getSpace().rdp (key);
        Space sp   = getSpace();
        if (obj == null) {
            synchronized (TransientSpace.class) {
                obj = sp.rdp (key);
                if (obj == null) {
                    obj = new TransientSpace ();
                    sp.out (key, obj);
                }
            }
        } 
        return (LocalSpace) obj;
    }
    /**
     * @return set of keys present in the Space
     */
    public Set getKeySet () {
        Set keySet;
        synchronized (this) {
            keySet = map.keySet();
        }
        return keySet;
    }
    public String getKeys () {
        StringBuffer sb = new StringBuffer ();
        Iterator iter = map.keySet().iterator ();
        boolean first = true;
        while (iter.hasNext()) {
            if (!first)
                sb.append (' ');
            else
                first = false;
            sb.append (iter.next().toString ());
        }
        return sb.toString ();
    }

    /**
     * same as Space.out (key,value)
     * @param key Key
     * @param value value
     */
    public void write (String key, String value) {
        out (key, value);
    }

    /**
     * same as (String) Space.rdp (key)
     * @param key Key
     * @return value.toString()
     */
    public String read (String key) {
        Object o = rdp (key);
        return o != null ? o.toString() : "null";
    }
    public int size (Object key) {
        Data data  = (Data) map.get (key);
        return data == null ? 0 : data.size ();
    }
    public void push (Object id, Object value) {
        throw new SpaceError ("Unsupported operation");
    }
    public void push (Object id, Object value, long timeout) {
        throw new SpaceError ("Unsupported operation");
    }
    public boolean existAny (Object[] keys) {
        throw new SpaceError ("Unsupported operation");
    }
    public boolean existAny (Object[] keys, long timeout) {
        throw new SpaceError ("Unsupported operation");
    }
    public void nrd(Object key) {
        throw new SpaceError("Not implemented");
    }
    public Object nrd(Object key, long timeout) {
        throw new SpaceError("Not implemented");
    }
}

