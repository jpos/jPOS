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

import java.io.*;
import java.util.*;

/**
 * Persistent Space implementation
 * @author Alejandro Revilla, modified by Kris Leite for Persistent
 * @version $Revision$ $Date$
 * @since 2.0
 */
@SuppressWarnings("unchecked")
public class PersistentSpace implements LocalSpace // PersistentSpaceMBean {
{
    protected Map map;
    static LocalSpace defaultSpace = new PersistentSpace ();
    static int cacheSize = 16;

    public PersistentSpace() {
        super();
        map = new HashMap ();
    }
    public synchronized void setCacheSize (int cacheSize) {
        PersistentSpace.cacheSize = cacheSize;
    }
    public void out (Object key, Object value) {
        List listeners;
        synchronized (this) {
            Data data = (Data) map.get (key);
            if (data == null) 
                map.put (key, data = new Data (key));
            data.add (value);
            listeners = data.getListeners();
            this.notifyAll ();
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
    public synchronized int size (Object key) {
        Data data  = (Data) map.get (key);
        if (data == null)
            map.put (key, data = new Data (key));
        return data.size ();
    }
    public synchronized Object rdp (Object key) {
        Data data  = (Data) map.get (key);
        if (data == null)
            map.put (key, data = new Data (key));
        return data.get (key);
    }
    public synchronized Object inp (Object key) {
        Data data  = (Data) map.get (key);
        if (data == null)
            map.put (key, data = new Data (key));
        return data.remove ();
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
             map.put (key, data = new Data (key));
        data.addListener (listener);
    }
    public synchronized void addListener 
        (Object key, SpaceListener listener, long timeout) {
        // #FIXME# not implemented 
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
        LinkedList stored;
        LinkedList listeners;
        File dir;

        protected Data (Object name) {
            super();
            data = new LinkedList ();
            stored = new LinkedList ();
            listeners = null;
            String n = "space" + File.separatorChar;
                if (name instanceof String)
                    n += name;
                else
                    n += "H" + Integer.toString(name.hashCode());
            dir = new File (n);
            dir.mkdirs ();
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File f, String name) {
                    return name.toUpperCase().startsWith("S");
                }
            };
            File file[] = dir.listFiles (filter);
            Arrays.sort (file);
            for (int i=0; file.length > i; i++) {
                if (cacheSize > data.size()) {
                    Object value = readValue (file[i].getAbsolutePath ());
                    if (value == null) {
                        new File (file[i].getAbsolutePath ()).delete();
                    } else {
                        stored.add(file[i].getAbsolutePath ());
                        data.add (value);
                    }
                } else
                    stored.add(file[i].getAbsolutePath ());
            }
        }
        private Object readValue (String f) {
            Object value = null;
            ObjectInputStream fin = null;
            try {
                fin = new ObjectInputStream (
                      new BufferedInputStream (
                      new FileInputStream (f)));
                value = fin.readObject ();
            } catch (Exception e) { 
                throw new SpaceError (e);
            } finally {
                if (fin != null)
                    try {
                        fin.close();
                    } catch (Exception e) { 
                        throw new SpaceError (e);
                    }
            }
            return value;
        }
        
        protected File createTempFile (String prefex, File dir) {
            long t = System.currentTimeMillis();
            File f = null;
            do {
                String fn = prefex + Long.toHexString(t);
                t = t + 1;
                f = new File (dir, fn);
            } while (f.exists());
            return f;
        }
        
        protected void add (Object value) {
            File f = null;
            FileOutputStream fos = null;
            try {
                f = createTempFile ("S", dir);
                fos = new FileOutputStream (f);
                ObjectOutputStream fout = new ObjectOutputStream (
                                          new BufferedOutputStream (fos));
                fout.writeObject (value);
                fout.flush();
                fos.getFD().sync();
            } catch (Exception e) {
                throw new SpaceError (e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e) { 
                        throw new SpaceError (e);
                    }
                }
            }
            stored.add(f.getAbsolutePath());
            /* fill cache */
            if (cacheSize > data.size ())
                if (data.size() + 1 == stored.size ())
                    data.add (value);
        }
        protected Object get (Object value) {
            Object obj = null;
            while (size() > 0) {
                obj = getFirst();
                if (obj instanceof LeasedReference)
                    obj = ((LeasedReference)obj).get ();
                if (obj == null) {
                    data.removeFirst ();
                    File f = new File ((String) stored.removeFirst ());
                    f.delete ();
                    continue;
                }
                break;
            }
            return obj;
        }
        protected Object getFirst () {
            Object object = null;
            if (data.size() > 0) {
                object = data.getFirst ();
            } else if (stored.size() > 0) {
                object = readValue ((String) stored.getFirst ());
            }
            return object;
        }
        protected int size () {
            return stored.size ();
        }
        protected Object remove () {
            Object obj = null;
            File f = null;
            while (size() > 0) {
                obj = getFirst ();
                if (data.size () > 0) 
                    data.removeFirst();
                f = new File ((String) stored.removeFirst ());
                f.delete ();
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
            return stored.isEmpty () && listeners == null;
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
        String key = "jpos:pSpace/"+spaceName;
        Space sp   = TransientSpace.getSpace();
        Object obj = sp.rdp (key);
        if (obj == null) {
            synchronized (PersistentSpace.class) {
                obj = sp.rdp (key);
                if (obj == null) {
                    obj = new PersistentSpace ();
                    sp.out (key, obj);
                }
            }
        } 
        return (LocalSpace) obj;
    }
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

    public void write (String key, String value) {
        out (key, value);
    }

    public String read (String key) {
        Object o = inp (key);
        return o != null ? o.toString() : "null";
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
