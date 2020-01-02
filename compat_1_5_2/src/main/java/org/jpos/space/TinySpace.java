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

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Tiny Space implementation
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 1.4.7
 * @deprecated org.jpos.space.TSpace is the new default lightweight space
 */
@SuppressWarnings("unchecked")
public class TinySpace implements Space, Serializable {
    protected Map map = new HashMap ();
    private static final long serialVersionUID = -5216796586015661708L;

    public TinySpace () {
        super();
    }
    public void out (Object key, Object value) {
        synchronized (this) {
            Object v = map.get (key);
            if (v == null)
                map.put (key, value);
            else if (v instanceof Data)
                ((Data)v).add (value);
            else {
                Data data = new Data();
                data.add (v);
                data.add (value);
                map.put (key, data);
            }
            this.notifyAll ();
        }
    }
    public void out (Object id, Object value, long timeout) {
        out (id, new LeasedReference (value, timeout));
    }
    public synchronized Object rdp (Object key) {
        Object obj = map.get (key);
        if (obj instanceof Data) 
            obj = ((Data) obj).get (key);
        else if (obj instanceof LeasedReference) {
            obj = ((LeasedReference)obj).get ();
        }
        return obj;
    }
    public void nrd(Object key) {
        throw new SpaceError("Not implemented");
    }
    public Object nrd(Object key, long timeout) {
        throw new SpaceError("Not implemented");
    }
    public synchronized Object inp (Object key) {
        Object obj = map.get (key);
        if (obj instanceof Data) {
            Data data = (Data) obj;
            obj = data.remove ();
            if (data.isEmpty ())
                map.remove (key);
        } else if (obj != null) {
            if (obj instanceof LeasedReference) {
                obj = ((LeasedReference)obj).get ();
            }
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
    public boolean existAny (Object[] keys) {
        for (int i=0; i<keys.length; i++) {
            if (rdp (keys[i]) != null)
                return true;
        }
        return false;
    }
    public boolean existAny (Object[] keys, long timeout) {
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((now = System.currentTimeMillis()) < end) {
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
    public void push (Object id, Object value) {
        throw new SpaceError ("Unsupported operation");
    }
    public void push (Object id, Object value, long timeout) {
        throw new SpaceError ("Unsupported operation");
    }
    public void put (Object key, Object value) {
        throw new SpaceError ("Unsupported operation");
    }
    public void put (Object key, Object value, long timeout) {
        throw new SpaceError ("Unsupported operation");
    }
    @SuppressWarnings("unchecked")
    protected static final class Data {
        LinkedList data;

        protected Data () {
            super();
            data = new LinkedList ();
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
            return data.isEmpty ();
        }
    }
}

