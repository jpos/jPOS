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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transient Space implementation
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 2.0
 * @jmx:mbean description "TransientSpace"
 */
public class TransientSpace implements LocalSpace, TransientSpaceMBean {
    protected Map map;
    static LocalSpace defaultSpace = new TransientSpace ();

    /**
     * @jmx:managed-constructor description="Default Constructor"
     */
    public TransientSpace () {
        super();
        map = new HashMap ();
    }
    public void out (Object key, Object value) {
        List listeners;
        synchronized (this) {
            Data data = (Data) map.get (key);
            if (data == null) 
                map.put (key, (data = new Data ()));
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
            map.put (key, (data = new Data()));
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
    public static final LocalSpace getSpace () {
        return defaultSpace;
    }
    public static final LocalSpace getSpace (String spaceName) {
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
     * @jmx:managed-attribute description="Keys in Space"
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
     * @jmx:managed-operation description="Write value to key"
     * @jmx:managed-operation-parameter position="0" name="key" description="Space Key"
     * @jmx:managed-operation-parameter position="1" name="value" description="Value to write"
     */
    public void write (String key, String value) {
        out (key, value);
    }

    /**
     * same as (String) Space.rdp (key)
     * @param key Key
     * @return value.toString()
     * @jmx:managed-operation description="Read value from key"
     * @jmx:managed-operation-parameter position="0" name="key" description="Space Key"
     */
    public String read (String key) {
        Object o = rdp (key);
        return (o != null) ? o.toString() : "null";
    }
    public int size (Object key) {
        Data data  = (Data) map.get (key);
        return data == null ? 0 : data.size ();
    }
}

