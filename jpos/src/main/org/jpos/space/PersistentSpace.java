/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.space;

import java.io.*;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Comparator;
import java.lang.reflect.Field;

import org.jpos.space.*;

/**
 * Persistent Space implementation
 * @author Alejandro Revilla, modified by Kris Leite for Persistent
 * @version $Revision$ $Date$
 * @since 2.0
 */
public class PersistentSpace implements Space // PersistentSpaceMBean 
{
    protected Map map;
    static Space defaultSpace = null;
    static int cacheSize = 16;

    public PersistentSpace() {
        super();
        map = new HashMap ();
    }
    public synchronized void setCacheSize (int cacheSize) {
        this.cacheSize = cacheSize;
    }
    private Data initData (Object key) {
        Data data = null;
        String dir = "space";
        if (key instanceof String)
              dir = dir + "/" + key;
        map.put (key, (data = new Data (dir)));
        return data;
    }
    public void out (Object key, Object value) {
        List listeners;
        synchronized (this) {
            Data data = (Data) map.get (key);
            if (data == null) 
                data = initData (key);
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
    public LeasedReference out (Object id, Object value, long timeout) {
        LeasedReference ref = new LeasedReference (value, timeout);
        out (id, ref);
        return ref;
    }
    public synchronized Object rdp (Object key) {
        Data data  = (Data) map.get (key);
        if (data == null)
            data = initData (key);
        return data.get (key);
    }
    public synchronized Object inp (Object key) {
        Data data  = (Data) map.get (key);
        if (data == null)
            data = initData (key);
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
    public synchronized void addListener    (Object key, SpaceListener listener) {
        Data data = (Data) map.get (key);
        if (data == null)
            data = initData (key);
        data.addListener (listener);
    }
    public synchronized void removeListener (Object key, SpaceListener listener) {
        Data data = (Data) map.get (key);
        if (data != null)
            data.removeListener (listener);
    }

    protected static final class Data {
        LinkedList data;
        LinkedList stored;
        LinkedList listeners;
        File dir;

        protected Data (String store) {
            super();
            data = new LinkedList ();
            stored = new LinkedList ();
            listeners = null;
            dir = new File (store);
            dir.mkdirs ();
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File f, String name) {
                    return name.toUpperCase().startsWith("SPA");
                }
            };
            File file[] = dir.listFiles (filter);
            Arrays.sort (file);
            for (int i=0; file.length > i; i++) {
                stored.add(file[i].getAbsolutePath ());
                if (cacheSize > data.size()) {
                    Object value = readValue (file[i].getAbsolutePath ());
                    if (value != null)
                        data.add (value);
                }
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
            } catch (IOException e) { 
            } catch (ClassNotFoundException e) {
            } finally {
                try {
                    fin.close();
                } catch (IOException e) { }
            }
            return value;
        }
        protected void add (Object value) {
            File f = null;
            FileOutputStream fos = null;
            try {
                f = File.createTempFile("SPA","",dir);
                fos = new FileOutputStream (f);
                ObjectOutputStream fout = new ObjectOutputStream (
                                          new BufferedOutputStream (fos));
                fout.writeObject (value);
                fout.flush();
                fos.getFD().sync();
            } catch (IOException e) {
            } finally {
                try {
                    fos.close();
                } catch (IOException e) { };
            }
            stored.add(f.getAbsolutePath());
            /* fill cache */
            while (cacheSize > data.size ()) {
                if (data.size () > size ()) break;
                if ((data.size() + 1) == size ()) {
                    data.add (value);
                    break;
                } else {
                    Object obj = readValue ((String) stored.get (data.size () + 1));
                    data.add (obj);
                }
            }
        }
        protected Object get (Object value) {
            Object obj = null;
            while (size() > 0) {
                obj = getFirst();
                if (obj instanceof LeasedReference) {
                    obj = ((LeasedReference)obj).get ();
                    if (obj == null) {
                        data.removeFirst ();
                        File f = new File ((String) stored.removeFirst ());
                        f.delete ();
                        continue;
                    }
                }
                break;
            }
            return obj;
        }
        protected Object getFirst () {
            Object object = null;
            if (data.size() > 0) {
                object = data.getFirst ();
            } else if (size() > 0) {
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
    public static final Space getSpace () {
        if (defaultSpace == null) {
            synchronized (PersistentSpace.class) {
                if (defaultSpace == null)
                    defaultSpace = new PersistentSpace ();
            }
        }
        return defaultSpace;
    }
    public static final Space getSpace (String spaceName) {
        String key = "jpos:space/"+spaceName;
        Object obj = getSpace().rdp (key);
        Space sp   = getSpace();
        if (obj == null) {
            synchronized (TransientSpace.class) {
                obj = sp.rdp (key);
                if (obj == null) {
                    obj = new PersistentSpace ();
                    sp.out (key, obj);
                }
            }
        } 
        return (Space) obj;
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
        return (o != null) ? o.toString() : "null";
    }
    public int size (Object key) {
        Data data  = (Data) map.get (key);
        return data == null ? 0 : data.size ();
    }
}

