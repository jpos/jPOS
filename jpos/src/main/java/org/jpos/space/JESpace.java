/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import com.sleepycat.je.*;
import com.sleepycat.persist.EntityStore; 
import com.sleepycat.persist.StoreConfig; 
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;
import com.sleepycat.persist.model.Relationship;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.jpos.iso.ISOUtil;
import org.jpos.util.Log;
import org.jpos.util.Loggeable;

/**
 * BerkeleyDB Jave Edition based persistent space implementation
 *
 * @author Alejandro Revilla
 * @since 1.6.5

 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings("unchecked")
public class JESpace<K,V> extends Log implements LocalSpace<K,V>, PersistentSpace, Loggeable, Runnable {
    /** BerkeleyDB JE environment instance. */
    Environment dbe = null;
    /** BerkeleyDB JE entity store. */
    EntityStore store = null;
    /** Primary index for Ref entities. */
    PrimaryIndex<Long, Ref> pIndex = null;
    /** Primary index for GCRef entities. */
    PrimaryIndex<Long,GCRef> gcpIndex = null;
    /** Secondary index for Ref entities by key. */
    SecondaryIndex<String,Long, Ref> sIndex = null;
    /** Secondary index for GCRef entities by expiration time. */
    SecondaryIndex<Long,Long,GCRef> gcsIndex = null;
    /** Semaphore used to prevent concurrent GC runs. */
    Semaphore gcSem = new Semaphore(1);
    /** Local space used to manage space listeners. */
    LocalSpace<Object,SpaceListener> sl;
    /** Resolution in milliseconds for non-blocking read polling. */
    private static final long NRD_RESOLUTION = 500L;
    /** Delay in milliseconds between GC runs. */
    public static final long GC_DELAY = 15*1000L;
    /** Default transaction timeout in milliseconds. */
    public static final long DEFAULT_TXN_TIMEOUT = 30*1000L;
    /** Default lock timeout in milliseconds. */
    public static final long DEFAULT_LOCK_TIMEOUT = 120*1000L;
    /** Future handle for the scheduled GC task. */
    private Future gcTask;

    /** Registry mapping space names to their JESpace instances. */
    static final Map<String,Space> spaceRegistrar = 
        new HashMap<String,Space> ();

    /**
     * Constructs a JESpace with the given name and path/parameter string.
     *
     * @param name   the space name (also used as the entity store name)
     * @param params comma-separated parameters; first element is the directory path
     * @throws SpaceError if the BerkeleyDB environment or store cannot be opened
     */
    public JESpace(String name, String params) throws SpaceError {
        super();
        try {
            EnvironmentConfig envConfig = new EnvironmentConfig();
            StoreConfig storeConfig = new StoreConfig();
            String[] p = ISOUtil.commaDecode(params);
            String path = p[0];
            envConfig.setAllowCreate (true);
            envConfig.setTransactional(true);
            envConfig.setLockTimeout(getParam("lock.timeout", p, DEFAULT_LOCK_TIMEOUT), TimeUnit.MILLISECONDS);
            envConfig.setTxnTimeout(getParam("txn.timeout", p, DEFAULT_TXN_TIMEOUT), TimeUnit.MILLISECONDS);
            storeConfig.setAllowCreate (true);
            storeConfig.setTransactional (true);

            File dir = new File(path);
            dir.mkdirs();

            dbe = new Environment (dir, envConfig);
            store = new EntityStore (dbe, name, storeConfig);
            pIndex = store.getPrimaryIndex (Long.class, Ref.class);
            gcpIndex = store.getPrimaryIndex (Long.class, GCRef.class);
            sIndex = store.getSecondaryIndex (pIndex, String.class, "key");
            gcsIndex = store.getSecondaryIndex (gcpIndex, Long.class, "expires");
            gcTask = SpaceFactory.getGCExecutor().scheduleAtFixedRate(this, GC_DELAY, GC_DELAY, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new SpaceError (e);
        }
    }

    public void out (K key, V value) {
        out (key, value, 0L);
    }
    public void out (K key, V value, long timeout) {
        Transaction txn = null;
        try {
            txn = dbe.beginTransaction (null, null);
            Ref ref = new Ref(key.toString(), value, timeout);
            pIndex.put (ref);
            if (timeout > 0L)
                gcpIndex.putNoReturn (
                    new GCRef (ref.getId(), ref.getExpiration())
                );
            txn.commit();
            txn = null;
            synchronized (this) {
                notifyAll ();
            }
            if (sl != null)
                notifyListeners(key, value);
        } catch (Exception e) {
            throw new SpaceError (e);
        } finally {
            if (txn != null)
                abort (txn);
        }
    }
    public void push (K key, V value, long timeout) {
        Transaction txn = null;
        try {
            txn = dbe.beginTransaction (null, null);
            Ref ref = new Ref(key.toString(), value, timeout);
            pIndex.put (ref);
            pIndex.delete (ref.getId());
            ref.reverseId();
            pIndex.put (ref);
            txn.commit();
            txn = null;
            synchronized (this) {
                notifyAll ();
            }
            if (sl != null)
                notifyListeners(key, value);
        } catch (Exception e) {
            throw new SpaceError (e);
        } finally {
            if (txn != null)
                abort (txn);
        }
    }
    public void push (K key, V value) {
        push (key, value, 0L);
    }
    @SuppressWarnings("unchecked")
    public V rdp (Object key) {
        try {
            return (V) getObject (key, false);
        } catch (DatabaseException e) {
            throw new SpaceError (e);
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized V in (Object key) {
        Object obj;
        while ((obj = inp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException ignored) { }
        }
        return (V) obj;
    }
    @SuppressWarnings("unchecked")
    public synchronized V in (Object key, long timeout) {
        Object obj;
        Instant now = Instant.now();
        long duration;
        while ((obj = inp (key)) == null &&
                (duration = Duration.between(now, Instant.now()).toMillis()) < timeout)
        {
            try {
                this.wait (timeout - duration);
            } catch (InterruptedException ignored) { }
        }
        return (V) obj;
    }

    @SuppressWarnings("unchecked")
    public synchronized V rd  (Object key) {
        Object obj;
        while ((obj = rdp (key)) == null) {
            try {
                this.wait ();
            } catch (InterruptedException ignored) { }
        }
        return (V) obj;
    }
    @SuppressWarnings("unchecked")
    public synchronized V rd  (Object key, long timeout) {
        Object obj;
        Instant now = Instant.now();
        long duration;
        while ((obj = rdp (key)) == null &&
                (duration = Duration.between(now, Instant.now()).toMillis()) < timeout)
        {
            try {
                this.wait (timeout - duration);
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
        Instant now = Instant.now();
        long duration;
        while ((obj = rdp (key)) != null &&
                (duration = Duration.between(now, Instant.now()).toMillis()) < timeout)
        {
            try {
                this.wait (Math.min(NRD_RESOLUTION, timeout - duration));
            } catch (InterruptedException ignored) { }
        }
        return (V) obj;
    }
    @SuppressWarnings("unchecked")
    public V inp (Object key) {
        try {
            return (V) getObject (key, true);
        } catch (DatabaseException e) {
            throw new SpaceError (e);
        }
    }

    public boolean existAny (Object[] keys) {
        for (Object key : keys) {
            if (rdp(key) != null) {
                return true;
            }
        }
        return false;
    }
    public boolean existAny (Object[] keys, long timeout) {
        Instant now = Instant.now();
        long duration;
        while ((duration = Duration.between(now, Instant.now()).toMillis()) < timeout) {
            if (existAny (keys))
                return true;
            synchronized (this) {
                try {
                    wait (timeout - duration);
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
    /** Removes all existing entries for the key then writes a single entry (head-of-queue replacement).
     * @param key the entry key
     * @param value the new value
     */
    public synchronized void put (K key, V value) {
        while (inp (key) != null)
            ; // NOPMD
        out (key, value);
    }
    /** Runs a garbage-collection pass removing expired entries from the BDB JE store.
     * @throws DatabaseException on BDB error
     */
    public void gc () throws DatabaseException {
        Transaction txn = null;
        EntityCursor<GCRef> cursor = null;
        try {
            if (!gcSem.tryAcquire())
                return;
            txn = dbe.beginTransaction (null, null);
            cursor = gcsIndex.entities (
                txn, 0L, true, Instant.now().toEpochMilli(), false, null
            );
            for (GCRef gcRef: cursor) {
                pIndex.delete (gcRef.getId());
                cursor.delete ();
            }
            cursor.close();
            cursor = null;
            txn.commit();
            txn = null;
            if (sl != null) {
                synchronized (this) {
                    if (sl != null && sl.getKeySet().isEmpty())
                        sl = null;
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
            if (txn != null)
                abort (txn);
            gcSem.release();
        }
    }
    public void run() {
        try {
            gc();
        } catch (Exception e) {
            warn(e);
        }
    }
    public void close () throws DatabaseException {
        gcSem.acquireUninterruptibly();
        gcTask.cancel(false);
        while (!gcTask.isDone()) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException ignored) { }
        }
        store.close ();
        dbe.close();
    }

    /** Returns (or creates) the named JESpace stored at the given path.
     * @param name space name
     * @param path filesystem path for BDB JE environment
     * @return the JESpace instance
     */
    public synchronized static JESpace getSpace (String name, String path)
    {
        JESpace sp = (JESpace) spaceRegistrar.get (name);
        if (sp == null) {
            sp = new JESpace(name, path);
            spaceRegistrar.put (name, sp);
        }
        return sp;
    }
    /** Returns (or creates) the named JESpace using the name as the storage path.
     * @param name space name and storage path
     * @return the JESpace instance
     */
    public static JESpace getSpace (String name) {
        return getSpace (name, name);        
    }
    private Object getObject (Object key, boolean remove) throws DatabaseException {
        Transaction txn = null;
        EntityCursor<Ref> cursor = null;
        Template tmpl = null;
        if (key instanceof Template) {
            tmpl = (Template) key;
            key  = tmpl.getKey();
        }
        try {
            txn = dbe.beginTransaction (null, null);
            cursor = sIndex.subIndex(key.toString()).entities(txn, null);
            for (Ref ref : cursor) {
                if (ref.isActive()) {
                    if (tmpl != null && !tmpl.equals (ref.getValue()))
                        continue;
                    if (remove) {
                        cursor.delete();
                        if (ref.hasExpiration()) 
                            gcpIndex.delete (txn, ref.getId());
                    }
                    cursor.close(); cursor = null;
                    txn.commit(); txn = null;
                    return ref.getValue();
                }
                else {
                    cursor.delete();
                    if (ref.hasExpiration()) 
                        gcpIndex.delete (txn, ref.getId());
                }
            }
            cursor.close(); cursor = null;
            txn.commit(); txn = null;
            return null;
        } finally {
            if (cursor != null)
                cursor.close ();
            if (txn != null)
                txn.abort();
        }
    }
    private void abort (Transaction txn) throws SpaceError {
        try {
            txn.abort();
        } catch (DatabaseException e) {
            throw new SpaceError (e);
        }
    }

    private LocalSpace<Object,SpaceListener> getSL() {
        synchronized (this) {
            if (sl == null)
                sl = new TSpace<Object,SpaceListener>();
        }
        return sl;
    }

    private void notifyListeners (Object key, Object value) {
        Set<SpaceListener> listeners = new HashSet<SpaceListener>();
        synchronized (this) {
            if (sl == null)
                return;
            SpaceListener s = null;
            while ((s = sl.inp(key)) != null)
                listeners.add(s);
            for (SpaceListener spl: listeners)
                sl.out(key, spl);
        }
        for (SpaceListener spl: listeners)
            spl.notify (key, value);
    }

    public synchronized void addListener(Object key, SpaceListener listener) {
        getSL().out (key, listener);
    }

    public synchronized void addListener(Object key, SpaceListener listener, long timeout) {
        getSL().out (key, listener);
    }

    public synchronized void removeListener(Object key, SpaceListener listener) {
        if (sl != null)
            sl.inp (new ObjectTemplate (key, listener));
    }

    public Set getKeySet() {
        Set res = new HashSet();
        Transaction txn = null;
        EntityCursor<Ref> cursor = null;
        try {
            txn = dbe.beginTransaction (null, null);
            cursor = sIndex.entities(txn, null);
            for (Ref ref : cursor)
                res.add(ref.getKey());
            cursor.close();
            cursor = null;
            txn.commit();
            txn = null;
        } catch (IllegalStateException ex) {
            warn (ex);
        } finally {
            if (cursor != null)
                cursor.close ();
            if (txn != null)
                txn.abort();
        }

        return res;
    }

  public int size(Object key) {
      Transaction txn = null;
      EntityCursor<Ref> cursor = null;
      try {
          txn = dbe.beginTransaction (null, null);
          cursor = sIndex.subIndex(key.toString()).entities(txn, null);
          int keyCount = 0;
          for (Ref ref : cursor)
              if (ref.isActive())
                  keyCount++;
          cursor.close();
          cursor = null;
          txn.commit();
          txn = null;
          return keyCount;
      } catch (IllegalStateException e) {
          return -1;
      } finally {
          if (cursor != null)
              cursor.close ();
          if (txn != null)
              txn.abort();
      }
  }

    /**
     * Persistent entity representing a single space entry (key/value with optional expiration).
     */
    @Entity
    public static class Ref {
        @PrimaryKey(sequence="Ref.id")
        /** Auto-generated primary key for this Ref. */
        private long id;

        @SecondaryKey(relate= Relationship.MANY_TO_ONE)
        /** The space key associated with this Ref. */
        private String key;

        /** Expiration timestamp in epoch milliseconds, or 0 if no expiration. */
        private long expires;
        /** The serialized or native value stored in the space. */
        private Object value;

        /** Default constructor required by BerkeleyDB JE. */
        public Ref () {
            super();
        }

        /**
         * Constructs a Ref for the given key, value and timeout.
         *
         * @param key     the space key
         * @param value   the value to store
         * @param timeout timeout in milliseconds, or 0 for no expiration
         */
        public Ref (String key, Object value, long timeout) {
            this.key = key;
            this.value =  serialize (value);
            if (timeout > 0L)
                this.expires = Instant.now().toEpochMilli() + timeout;
        }

        /**
         * Returns the primary key id of this Ref.
         *
         * @return the primary key
         */
        public long getId() {
            return id;
        }

        /**
         * Negates the id to push this entry to the front of an ordered scan (push semantics).
         */
        public void reverseId() {
            this.id = -this.id;
        }

        /**
         * Returns {@code true} if this Ref has passed its expiration time.
         *
         * @return {@code true} if expired
         */
        public boolean isExpired () {
            return expires > 0L && expires < Instant.now().toEpochMilli();
        }

        /**
         * Returns {@code true} if this Ref has not yet expired.
         *
         * @return {@code true} if still active
         */
        public boolean isActive () {
            return !isExpired();
        }

        /**
         * Returns the space key of this Ref.
         *
         * @return the key string
         */
        public Object getKey () {
            return key;
        }

        /**
         * Returns the deserialized value of this Ref.
         *
         * @return the stored value
         */
        public Object getValue () {
            return deserialize(value);
        }

        /**
         * Returns the expiration timestamp in epoch milliseconds.
         *
         * @return expiration time, or 0 if no expiration is set
         */
        public long getExpiration () {
            return expires;
        }

        /**
         * Returns {@code true} if this Ref has an expiration set.
         *
         * @return {@code true} if an expiration time is set
         */
        public boolean hasExpiration () {
            return expires > 0L;
        }
        private boolean isPersistent (Class c) {
            return
                c.isPrimitive() ||
                c.isAnnotationPresent(Entity.class) ||
                c.isAnnotationPresent(Persistent.class);
        }
        private Object serialize (Object obj) {
            Class cls = obj.getClass();
            if (isPersistent (cls))
                return obj;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream os = new ObjectOutputStream(baos);
                os.writeObject(obj);
                obj = baos.toByteArray();
            } catch (IOException e) {
                throw new SpaceError (e);
            }
            return obj;
        }
        private Object deserialize (Object obj) {
            Class cls = obj.getClass();
            if (isPersistent (cls))
                return obj;

            ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) obj);
            try {
                ObjectInputStream is = org.jpos.util.Serializer.createSafeObjectInputStream(bais);
                return is.readObject();
            } catch (Exception e) {
                throw new SpaceError (e);
            }

        }
    }
    
    public void dump(PrintStream p, String indent) {
        Transaction txn = null;
        EntityCursor<Ref> cursor = null;
        int count = 0;
        try {
            txn = dbe.beginTransaction (null, null);
            cursor = sIndex.entities(txn, null);
            String key = null;
            int keyCount = 0;
            for (Ref ref : cursor) {
                if (ref.getKey().equals(key)) {
                    keyCount++;
                } else {
                    if (key != null) {
                        dumpKey (p, indent, key, keyCount);
                        count++;
                    }
                    keyCount = 1;
                    key = ref.getKey().toString();
                }
            }
            if (key != null) {
                dumpKey (p, indent, key, keyCount);
                count++;
            }
            p.println(indent+"<keycount>"+count+"</keycount>");
            cursor.close(); cursor = null;
            txn.commit(); txn = null;
        } catch (IllegalStateException e) {
            //Empty Cursor
            p.println(indent+"<keycount>0</keycount>");
        } finally {
            if (cursor != null)
                cursor.close ();
            if (txn != null)
                txn.abort();
        }
    }

    private void dumpKey (PrintStream p, String indent, String key, int count) {
        if (count > 0)
            p.printf ("%s<key size='%d'>%s</key>\n", indent, count, key);
        else
            p.printf ("%s<key>%s</key>\n", indent, key);
    }

    private long getParam (String name, String[] params, long defaultValue) {
        for (String s : params) {
            if (s.contains(name)) {
                int pos = s.indexOf('=');
                if (pos >=0 && s.length() > pos)
                    return Long.valueOf(s.substring(pos+1).trim());
            }
        }
        return defaultValue;
    }

    /**
     * Persistent entity used by the garbage collector to track expiring Ref entries.
     */
    @Entity
    public static class GCRef {
        @PrimaryKey
        /** The id of the corresponding Ref entry to be garbage collected. */
        private long id;

        @SecondaryKey(relate=Relationship.MANY_TO_ONE)
        /** Expiration timestamp in epoch milliseconds used to order GC candidates. */
        private long expires;

        /** Default constructor required by BerkeleyDB JE. */
        public GCRef () {
            super();
        }

        /**
         * Constructs a GCRef for the given Ref id and expiration time.
         *
         * @param id      the id of the Ref to be collected
         * @param expires the expiration timestamp in epoch milliseconds
         */
        public GCRef (long id, long expires) {
            this.id = id;
            this.expires = expires;
        }

        /**
         * Returns the id of the corresponding Ref entry.
         *
         * @return the Ref primary key
         */
        public long getId() {
            return id;
        }

        /**
         * Returns {@code true} if the expiration time has passed.
         *
         * @return {@code true} if this GCRef is expired
         */
        public boolean isExpired () {
            return expires > 0L && expires < Instant.now().toEpochMilli();
        }

        /**
         * Returns the expiration timestamp in epoch milliseconds.
         *
         * @return expiration time
         */
        public long getExpiration () {
            return expires;
        }
    }
}
