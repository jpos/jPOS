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

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.helper.FastIterator;
import jdbm.helper.Serializer;
import jdbm.htree.HTree;
import org.jpos.util.DefaultTimer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

/**
 * JDBM based persistent space implementation
 *
 * @author Alejandro Revilla
 * @author Kris Leite
 * @version $Revision$ $Date$
 * @since 1.4.7
 */
@SuppressWarnings("unchecked")
public class JDBMSpace<K,V> extends TimerTask implements Space<K,V> {
    protected HTree htree;
    protected RecordManager recman;
    protected static final Serializer refSerializer = new Ref ();
    protected static final Map<String,Space> spaceRegistrar = new HashMap<String,Space> ();
    protected boolean autoCommit = true;
    protected String name;
    public static final long GCDELAY = 5*60*1000;
    private static final long NRD_RESOLUTION = 500L;

    /**
     * protected constructor.
     * @param name Space Name
     * @param filename underlying JDBM filename
     */
    protected JDBMSpace (String name, String filename) {
        super();
        this.name = name;
        try {
            Properties props = new Properties();
            props.put (RecordManagerOptions.CACHE_SIZE, "512");
            recman = RecordManagerFactory.createRecordManager (filename, props);
            long recid = recman.getNamedObject ("space");
            if (recid != 0) {
                htree = HTree.load (recman, recid);
            } else {
                htree = HTree.createInstance (recman);
                recman.setNamedObject ("space", htree.getRecid());
            }
            recman.commit ();
        } catch (IOException e) {
            throw new SpaceError (e);
        }
        DefaultTimer.getTimer().schedule (this, GCDELAY, GCDELAY);
    }
    /**
     * @return reference to default JDBMSpace
     */
    public static JDBMSpace getSpace() {
        return getSpace ("space");
    }
    /**
     * creates a named JDBMSpace 
     * (filename used for storage is the same as the given name)
     * @param name the Space name
     * @return reference to named JDBMSpace
     */
    public static JDBMSpace getSpace(String name) {
        return getSpace(name, name);
    }
    /**
     * creates a named JDBMSpace
     * @param name the Space name
     * @param filename the storage file name
     * @return reference to named JDBMSpace
     */
    public synchronized static JDBMSpace
        getSpace (String name, String filename) 
    {
        JDBMSpace sp = (JDBMSpace) spaceRegistrar.get (name);
        if (sp == null) {
            sp = new JDBMSpace (name, filename);
            spaceRegistrar.put (name, sp);
        }
        return sp;
    }
    /**
     * Use with utmost care and at your own risk.
     *
     * If you are to perform several operations on the space you
     * should synchronize on the space, i.e:
     * <pre>
     *   synchronized (sp) {
     *     sp.setAutoCommit (false);
     *     sp.out (..., ...)
     *     sp.out (..., ...)
     *     ...
     *     ...
     *     sp.inp (...);
     *     sp.commit ();    // or sp.rollback ();
     *     sp.setAutoCommit (true);
     *   }
     * </pre>
     * @param b true or false
     */
    public void setAutoCommit (boolean b) {
        this.autoCommit = b;
    }
    /**
     * force commit
     */
    public void commit () {
        try {
            recman.commit ();
            this.notifyAll ();
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    /**
     * force rollback
     */
    public void rollback () {
        try {
            recman.rollback ();
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    /**
     * close this space - use with care
     */
    public void close () {
        synchronized (JDBMSpace.class) {
            spaceRegistrar.remove (name);
        }
        synchronized (this) {
            try {
                recman.close ();
                recman = null;
            } catch (IOException e) {
                throw new SpaceError (e);
            }
        }
    }
    /**
     * Write a new entry into the Space
     * @param key Entry's key
     * @param value Object value
     */
    public void out (K key, V value) {
        out (key, value, -1);
    }
    /**
     * Write a new entry into the Space
     * The entry will timeout after the specified period
     * @param key Entry's key
     * @param value Object value
     * @param timeout entry timeout in millis
     */
    public void out (K key, V value, long timeout) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);
        try {
            synchronized (this) {
                long recid = recman.insert (value);

                long expiration = timeout == -1 ? Long.MAX_VALUE :
                        System.currentTimeMillis() + timeout;
                Ref dataRef = new Ref (recid, expiration);
                long dataRefRecId = recman.insert (dataRef, refSerializer);

                Head head = (Head) htree.get (key);
                if (head == null) {
                    head = new Head ();
                    head.first = dataRefRecId;
                    head.last  = dataRefRecId;
                    head.count = 1;
                } else {
                    long previousLast = head.last;
                    Ref lastRef   = 
                        (Ref) recman.fetch (previousLast, refSerializer);
                    lastRef.next      = dataRefRecId;
                    head.last         = dataRefRecId;
                    head.count++;
                    recman.update (previousLast, lastRef, refSerializer);
                }
                htree.put (key, head);
                if (autoCommit) {
                    recman.commit ();
                    this.notifyAll ();
                }
            }
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    public void push (K key, V value) {
        push (key, value, -1);
    }
    /**
     * Write a new entry into the Space at the head of a queue
     * The entry will timeout after the specified period
     * @param key Entry's key
     * @param value Object value
     * @param timeout entry timeout in millis
     */
    public void push (Object key, Object value, long timeout) {
        if (key == null || value == null)
            throw new NullPointerException ("key=" + key + ", value=" + value);
        try {
            synchronized (this) {
                long recid = recman.insert (value);
                long expiration = timeout == -1 ? Long.MAX_VALUE :
                        System.currentTimeMillis() + timeout;
                Ref dataRef = new Ref (recid, expiration);

                Head head = (Head) htree.get (key);
                if (head == null) {
                    head = new Head ();
                    head.first = head.last = recman.insert (dataRef, refSerializer);
                } else {
                    dataRef.next = head.first;
                    head.first   = recman.insert (dataRef, refSerializer);
                }
                head.count++;
                htree.put (key, head);
                if (autoCommit) {
                    recman.commit ();
                    this.notifyAll ();
                }
            }
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    /**
     * Read probe reads an entry from the space if one exists, 
     * return null otherwise.
     * @param key Entry's key
     * @return value or null
     */
    public synchronized V rdp (Object key) {
        try {
            if (key instanceof Template) 
                return (V) getObject ((Template) key, false);

            Object obj = null;
            Ref ref = getFirst (key, false);
            if (ref != null) 
                obj = recman.fetch (ref.recid);
            if (autoCommit)
                recman.commit ();
            return (V) obj;
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }

    /**
     * In probe takes an entry from the space if one exists, 
     * return null otherwise.
     * @param key Entry's key
     * @return value or null
     */
    public synchronized V inp (Object key) {
        try {
            if (key instanceof Template) 
                return (V) getObject ((Template) key, true);

            Object obj = null;
            Ref ref = getFirst (key, true);
            if (ref != null) {
                obj = recman.fetch (ref.recid);
                recman.delete (ref.recid);
            }
            if (autoCommit)
                recman.commit ();
            return (V) obj;
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    public synchronized V in (Object key) {
        Object obj;
        while ((obj = inp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException ignored) { }
        }
        return (V) obj;
    }
    /**
     * Take an entry from the space, waiting forever until one exists.
     * @param key Entry's key
     * @return value
     */
    public synchronized V in (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = inp (key)) == null &&
                (now = System.currentTimeMillis()) < end)
        {
            try {
                this.wait (end - now);
            } catch (InterruptedException ignored) { }
        }
        return (V) obj;
    }

    /**
     * Read an entry from the space, waiting forever until one exists.
     * @param key Entry's key
     * @return value
     */
    public synchronized V rd  (Object key) {
        Object obj;
        while ((obj = rdp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException ignored) { }
        }
        return (V) obj;
    }

    /**
     * Read an entry from the space, waiting a limited amount of time
     * until one exists.
     * @param key Entry's key
     * @param timeout millis to wait
     * @return value or null
     */
    public synchronized V rd  (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = rdp (key)) == null &&
                (now = System.currentTimeMillis()) < end)
        {
            try {
                this.wait (end - now);
            } catch (InterruptedException ignored) { }
        }
        return (V) obj;
    }
    public synchronized void nrd  (Object key) {
        while (rdp (key) != null) {
            try {
                this.wait (NRD_RESOLUTION);
            } catch (InterruptedException ignored) { }
        }
    }
    public synchronized V nrd  (Object key, long timeout) {
        Object obj;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((obj = rdp (key)) != null &&
                (now = System.currentTimeMillis()) < end)
        {
            try {
                this.wait (Math.min(NRD_RESOLUTION, end - now));
            } catch (InterruptedException ignored) { }
        }
        return (V) obj;
    }

    /**
     * @param key the Key
     * @return aproximately queue size
     */
    public long size (Object key) {
        try {
            Head head = (Head) htree.get (key);
            return head != null ? head.count : 0;
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    public boolean existAny (Object[] keys) {
        for (Object key : keys) {
            if (rdp(key) != null)
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
                } catch (InterruptedException ignored) { }
            }
        }
        return false;
    }
    public synchronized void put (K key, V value, long timeout) {
        while (inp (key) != null)
            ; // NOPMD
        out (key, value, timeout);
    }
    public synchronized void put (K key, V value) {
        while (inp (key) != null)
            ; // NOPMD
        out (key, value);
    }
    private void purge (Object key) throws IOException {
        Head head = (Head) htree.get (key);
        Ref previousRef = null;
        if (head != null) {
            for (long recid = head.first; recid >= 0; ) {
                Ref r = (Ref) recman.fetch (recid, refSerializer);
                if (r.isExpired ()) {
                    recman.delete (r.recid);
                    recman.delete (recid);
                    head.count--;
                    if (previousRef == null) {
                        head.first = r.next;
                    } else {
                        previousRef.next = r.next;
                        recman.update (
                            head.last, previousRef, refSerializer
                        );
                    }
                } else {
                    previousRef   = r;
                    head.last     = recid;
                }
                recid = r.next;
            }
            if (head.first == -1)  {
                htree.remove (key);
            }
            else {
                htree.put (key, head);
            }
        }
    }
    public void run () {
        try {
            gc();
        } catch (Exception e) {
            e.printStackTrace(); // this should never happen
        }
    }
    /**
     * garbage collector.
     * removes expired entries
     */
    public void gc () {
        final String GCKEY = "GC$" + Integer.toString (hashCode());
        final long TIMEOUT = 24 * 3600 * 1000;
        Object obj;
        try {
            synchronized (this) {
                // avoid concurrent gc
                if (rdp (GCKEY) != null) 
                    return;
                ((Space)this).out (GCKEY, Boolean.TRUE, TIMEOUT);  
            }
            FastIterator iter = htree.keys ();

            try {
                while ( (obj = iter.next()) != null) {
                    ((Space)this).out (GCKEY, obj, TIMEOUT);
                    Thread.yield ();
                }
            } catch (ConcurrentModificationException e) {
                // ignore, we may have better luck on next try
            }
            while ( (obj = inp (GCKEY)) != null) {
                synchronized (this) {
                    purge (obj);
                    recman.commit ();
                }
                Thread.yield ();
            }    
        } catch (IOException e) {
            throw new SpaceError (e);
        }
    }
    public String getKeys () {
        StringBuilder sb = new StringBuilder();
        try {
            FastIterator iter = htree.keys ();
            Object obj;
            while ( (obj = iter.next()) != null) {
                if (sb.length() > 0)
                    sb.append (' ');
                sb.append (obj.toString());
            }
        } catch (IOException e) {
            throw new SpaceError (e);
        }
        return sb.toString();
    }
    
    private Ref getFirst (Object key, boolean remove) throws IOException {
        Head head = (Head) htree.get (key);
        Ref ref = null;
        if (head != null) {
            long recid;
            for (recid = head.first; recid >= 0; ) {
                Ref r = (Ref) recman.fetch (recid, refSerializer);
                if (r.isExpired ()) {
                    recman.delete (r.recid);
                    recman.delete (recid);
                    recid = r.next;
                    head.count--;
                } else  {
                    ref = r;
                    if (remove) {
                        recman.delete (recid);
                        recid = ref.next;
                        head.count--;
                    }
                    break;
                }
            } 
            if (head.first != recid) {
                if (recid < 0)
                    htree.remove (key);
                else {
                    head.first = recid;
                    htree.put (key, head);
                }
            }
        }
        return ref;
    }
    private void unlinkRef
        (long recid, Head head, Ref r, Ref previousRef, long previousRecId) 
        throws IOException
    {
        recman.delete (r.recid);
        recman.delete (recid);
        head.count--;
        if (previousRef == null)
            head.first = r.next;
        else {
            previousRef.next = r.next;
            recman.update (
                previousRecId, previousRef, refSerializer
            );
        }
    }
    private Object getObject (Template tmpl, boolean remove) 
        throws IOException 
    {
        Object obj = null;
        Object key = tmpl.getKey();
        Head head = (Head) htree.get (key);
        Ref previousRef = null;
        long previousRecId = 0;
        int unlinkCount = 0;
        if (head != null) {
            for (long recid = head.first; recid >= 0; ) {
                Ref r = (Ref) recman.fetch (recid, refSerializer);
                if (r.isExpired ()) {
                    unlinkRef (recid, head, r, previousRef, previousRecId);
                    unlinkCount++;
                } else  {
                    Object o = recman.fetch (r.recid);
                    if (o != null && tmpl.equals(o)) {
                        obj = o;
                        if (remove) {
                            unlinkRef (
                                recid, head, r, previousRef, previousRecId
                            );
                            unlinkCount++;
                        }
                        break;
                    }
                    previousRef = r;
                    previousRecId = recid;
                }
                recid = r.next;
            } 
            if (unlinkCount > 0) {
                if (head.first == -1)  {
                    htree.remove (key);
                }
                else {
                    htree.put (key, head);
                }
            }
        }
        return obj;
    }
    static class Head implements Externalizable {
        public long first;
        public long last;
        public long count;
        static final long serialVersionUID = 2L;

        public Head () {
            super ();
            first = -1;
            last  = -1;
        }
        public void writeExternal (ObjectOutput out) throws IOException {
            out.writeLong (first);
            out.writeLong (last);
            out.writeLong (count);
        }
        public void readExternal (ObjectInput in) throws IOException {
            first = in.readLong ();
            last  = in.readLong ();
            count = in.readLong ();
        }
        public String toString() {
            return getClass().getName() 
                + "@" + Integer.toHexString(hashCode())
                + ":[first=" + first 
                + ",last=" + last
                + "]";
        }
    }
    static class Ref implements Serializer {
        long recid;
        long expires;
        long next;
        static final long serialVersionUID = 1L;

        public Ref () {
            super();
        }
        public Ref (long recid, long expires) {
            super();
            this.recid   = recid;
            this.expires = expires;
            this.next    = -1;
        }

        public boolean isExpired () {
            return expires < System.currentTimeMillis ();
        }
        public String toString() {
            return getClass().getName() 
                + "@" + Integer.toHexString(hashCode())
                + ":[recid=" + recid
                + ",next=" + next
                + ",expired=" + isExpired ()
                + "]";
        }
        public byte[] serialize (Object obj) 
            throws IOException
        {
            Ref d = (Ref) obj;

            byte[] buf = new byte [24];
            putLong (buf, 0, d.recid);
            putLong (buf, 8, d.next);
            putLong (buf,16, d.expires);
            return buf;
        }
        public Object deserialize (byte[] serialized) 
            throws IOException
        {
            Ref d = new Ref ();
            d.recid   = getLong (serialized,  0);
            d.next    = getLong (serialized,  8);
            d.expires = getLong (serialized, 16);
            return d;
        }
    }
    static void putLong (byte[] b, int off, long val) {
        b[off+7] = (byte) val;
        b[off+6] = (byte) (val >>>  8);
        b[off+5] = (byte) (val >>> 16);
        b[off+4] = (byte) (val >>> 24);
        b[off+3] = (byte) (val >>> 32);
        b[off+2] = (byte) (val >>> 40);
        b[off+1] = (byte) (val >>> 48);
        b[off] = (byte) (val >>> 56);
    }
    static long getLong (byte[] b, int off) {
        return (b[off+7] & 0xFFL) +
               ((b[off+6] & 0xFFL) << 8) +
               ((b[off+5] & 0xFFL) << 16) +
               ((b[off+4] & 0xFFL) << 24) +
               ((b[off+3] & 0xFFL) << 32) +
               ((b[off+2] & 0xFFL) << 40) +
               ((b[off+1] & 0xFFL) << 48) +
               ((b[off] & 0xFFL) << 56);
    }
}
