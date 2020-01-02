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
import org.jpos.util.Profiler;

/**
 * BerkeleyDB Jave Edition based persistent space implementation
 *
 * @author Alejandro Revilla
 * @since 1.6.5
 */
@SuppressWarnings("unchecked")
public class JESpace<K,V> extends Log implements LocalSpace<K,V>, Loggeable, Runnable {
    Environment dbe = null;
    EntityStore store = null;
    PrimaryIndex<Long, Ref> pIndex = null;
    PrimaryIndex<Long,GCRef> gcpIndex = null;
    SecondaryIndex<String,Long, Ref> sIndex = null;
    SecondaryIndex<Long,Long,GCRef> gcsIndex = null;
    Semaphore gcSem = new Semaphore(1);
    LocalSpace<Object,SpaceListener> sl;
    private static final long NRD_RESOLUTION = 500L;
    public static final long GC_DELAY = 15*1000L;
    public static final long DEFAULT_TXN_TIMEOUT = 30*1000L;
    public static final long DEFAULT_LOCK_TIMEOUT = 120*1000L;
    private Future gcTask;

    static final Map<String,Space> spaceRegistrar = 
        new HashMap<String,Space> ();

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
    public void gc () throws DatabaseException {
        Transaction txn = null;
        EntityCursor<GCRef> cursor = null;
        try {
            if (!gcSem.tryAcquire())
                return;
            txn = dbe.beginTransaction (null, null);
            cursor = gcsIndex.entities (
                txn, 0L, true, System.currentTimeMillis(), false, null
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

    public synchronized static JESpace getSpace (String name, String path)
    {
        JESpace sp = (JESpace) spaceRegistrar.get (name);
        if (sp == null) {
            sp = new JESpace(name, path);
            spaceRegistrar.put (name, sp);
        }
        return sp;
    }
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

    @Entity
    public static class Ref {
        @PrimaryKey(sequence="Ref.id")
        private long id;

        @SecondaryKey(relate= Relationship.MANY_TO_ONE)
        private String key;

        private long expires;
        private Object value;

        public Ref () {
            super();
        }
        public Ref (String key, Object value, long timeout) {
            this.key = key;
            this.value =  serialize (value);
            if (timeout > 0L)
                this.expires = System.currentTimeMillis() + timeout;
        }
        public long getId() {
            return id;
        }
        public void reverseId() {
            this.id = -this.id;
        }
        public boolean isExpired () {
            return expires > 0L && expires < System.currentTimeMillis ();
        }
        public boolean isActive () {
            return !isExpired();
        }
        public Object getKey () {
            return key;
        }
        public Object getValue () {
            return deserialize(value);
        }
        public long getExpiration () {
            return expires;
        }
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
                ObjectInputStream is = new ObjectInputStream (bais);
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

    @Entity
    public static class GCRef {
        @PrimaryKey
        private long id;

        @SecondaryKey(relate=Relationship.MANY_TO_ONE)
        private long expires;
        public GCRef () {
            super();
        }
        public GCRef (long id, long expires) {
            this.id = id;
            this.expires = expires;
        }
        public long getId() {
            return id;
        }
        public boolean isExpired () {
            return expires > 0L && expires < System.currentTimeMillis ();
        }
        public long getExpiration () {
            return expires;
        }
    }
}
