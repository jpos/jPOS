/*
 * Copyright (c) 2003 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.space;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.Serializable;

/**
 * Tiny Space implementation
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 1.4.7
 */
public class TinySpace implements Space, Serializable {
    protected Map map = new HashMap ();

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
                ((now = System.currentTimeMillis()) < end))
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
                ((now = System.currentTimeMillis()) < end))
        {
            try {
                this.wait (end - now);
            } catch (InterruptedException e) { }
        }
        return obj;
    }
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

