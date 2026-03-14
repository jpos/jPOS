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

import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mapdb.Atomic;

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MapDB based persistent space implementation.
 *
 * <p>This implementation borrows the per-key locking model introduced by {@link LSpace}
 * while persisting queue state in a MapDB store. Each key keeps its own lock/conditions so
 * Virtual Threads can block efficiently without waking unrelated waiters, and queue contents
 * survive JVM restarts thanks to MapDB.</p>

 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MDBSpace<K,V> implements LocalSpace<K,V>, PersistentSpace, Loggeable, Runnable {
    private static final ConcurrentHashMap<String, MDBSpace<?,?>> REGISTRY = new ConcurrentHashMap<>();

    private static final long GC_DELAY = 15_000L;
    private static final long NRD_RESOLUTION = 500L;
    private static final long NO_TIMEOUT = -1L;

    private final DB db;
    private final HTreeMap<K, Head> heads;
    private final HTreeMap<Long, Ref> refs;
    private final Atomic.Long sequence;
    private final ConcurrentHashMap<K, KeyMonitor> monitors = new ConcurrentHashMap<>();
    private final ScheduledFuture<?> gcFuture;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final String name;

    private volatile LocalSpace<K, SpaceListener<K,V>> sl;

    private MDBSpace(String name, String params) {
        this.name = name;
        String[] tokens = ISOUtil.commaDecode(params != null ? params : name);
        if (tokens.length == 0)
            throw new SpaceError("Invalid MapDB space parameters for " + name);
        File dbFile = new File(tokens[0]);
        File parent = dbFile.getParentFile();
        if (parent != null)
            parent.mkdirs();
        Map<String,String> options = parseOptions(tokens);
        DBMaker.Maker maker = DBMaker.fileDB(dbFile)
            .fileMmapEnableIfSupported()
            .closeOnJvmShutdown();
        if ("true".equalsIgnoreCase(options.get("readonly")))
            maker.readOnly();
        if ("true".equalsIgnoreCase(options.get("transactionEnable")))
            maker.transactionEnable();
        this.db = maker.make();
        this.heads = db.hashMap(name + ".heads", Serializer.JAVA, Serializer.JAVA).createOrOpen();
        this.refs = db.hashMap(name + ".refs", Serializer.LONG, Serializer.JAVA).createOrOpen();
        this.sequence = db.atomicLong(name + ".seq").createOrOpen();
        this.gcFuture = SpaceFactory.getGCExecutor().scheduleAtFixedRate(this, GC_DELAY, GC_DELAY, TimeUnit.MILLISECONDS);
    }

    public static synchronized <K,V> MDBSpace<K,V> getSpace(String name) {
        return getSpace(name, name);
    }

    public static synchronized <K,V> MDBSpace<K,V> getSpace(String name, String params) {
        MDBSpace<?,?> space = REGISTRY.get(name);
        if (space == null) {
            space = new MDBSpace<>(name, params);
            REGISTRY.put(name, space);
        }
        return (MDBSpace<K,V>) space;
    }

    @Override
    public void out(K key, V value) {
        out(key, value, 0L);
    }

    @Override
    public void out(K key, V value, long timeout) {
        enqueue(key, value, timeout, AppendMode.TAIL);
    }

    @Override
    public void push(K key, V value) {
        push(key, value, 0L);
    }

    @Override
    public void push(K key, V value, long timeout) {
        enqueue(key, value, timeout, AppendMode.HEAD);
    }

    @Override
    public void put(K key, V value) {
        put(key, value, 0L);
    }

    @Override
    public void put(K key, V value, long timeout) {
        enqueue(key, value, timeout, AppendMode.REPLACE);
    }

    @Override
    public V rdp(Object key) {
        ensureOpen();
        if (key instanceof Template)
            return (V) fetchTemplate((Template) key, false);
        return (V) fetchHead((K) key, false);
    }

    @Override
    public V inp(Object key) {
        ensureOpen();
        if (key instanceof Template)
            return (V) fetchTemplate((Template) key, true);
        return (V) fetchHead((K) key, true);
    }

    @Override
    public V in(Object key) {
        return awaitValue(key, true, NO_TIMEOUT);
    }

    @Override
    public V in(Object key, long timeout) {
        return awaitValue(key, true, timeout);
    }

    @Override
    public V rd(Object key) {
        return awaitValue(key, false, NO_TIMEOUT);
    }

    @Override
    public V rd(Object key, long timeout) {
        return awaitValue(key, false, timeout);
    }

    @Override
    public void nrd(Object key) {
        nrd(key, NO_TIMEOUT);
    }

    @Override
    public V nrd(Object key, long timeout) {
        ensureOpen();
        K k = (K) key;
        KeyMonitor monitor = monitorFor(k);
        monitor.lock.lock();
        try {
            long nanos = timeout == NO_TIMEOUT ? NO_TIMEOUT : TimeUnit.MILLISECONDS.toNanos(timeout);
            while (queueHasValues(k)) {
                if (timeout == NO_TIMEOUT) {
                    monitor.isEmpty.await(NRD_RESOLUTION, TimeUnit.MILLISECONDS);
                } else {
                    if (nanos <= 0)
                        break;
                    nanos = monitor.isEmpty.awaitNanos(Math.min(TimeUnit.MILLISECONDS.toNanos(NRD_RESOLUTION), nanos));
                }
            }
            boolean hasValues = queueHasValues(k);
            if (timeout != NO_TIMEOUT && hasValues && nanos <= 0) {
                FetchResult<V> peek = fetchHeadLocked(k, monitor, false);
                if (peek.mutated)
                    commitQuietly();
                return peek.value;
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            monitor.lock.unlock();
        }
    }

    @Override
    public boolean existAny(Object[] keys) {
        ensureOpen();
        for (Object key : keys) {
            if (rdp(key) != null)
                return true;
        }
        return false;
    }

    @Override
    public boolean existAny(Object[] keys, long timeout) {
        ensureOpen();
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
        while (timeout == NO_TIMEOUT || System.nanoTime() < deadline) {
            if (existAny(keys))
                return true;
            long remaining = deadline - System.nanoTime();
            if (timeout != NO_TIMEOUT && remaining <= 0)
                break;
            try {
                Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(Math.max(remaining, 1L)), NRD_RESOLUTION));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return false;
    }

    @Override
    public int size(Object key) {
        ensureOpen();
        Head head = heads.get((K) key);
        return head != null ? (int) head.count : 0;
    }

    @Override
    public Set<K> getKeySet() {
        ensureOpen();
        return new HashSet<>(heads.keySet());
    }

    @Override
    public void addListener(Object key, SpaceListener listener) {
        ensureOpen();
        getSL().out((K) key, listener);
    }

    @Override
    public void addListener(Object key, SpaceListener listener, long timeout) {
        ensureOpen();
        getSL().out((K) key, listener, timeout);
    }

    @Override
    public void removeListener(Object key, SpaceListener listener) {
        ensureOpen();
        if (sl != null)
            sl.inp((K) new ObjectTemplate(key, listener));
    }

    @Override
    public void dump(PrintStream p, String indent) {
        ensureOpen();
        p.printf("%s<mdb-space name=\"%s\" key-count=\"%d\"/>%n", indent, name, heads.size());
        int shown = 0;
        for (K key : heads.keySet()) {
            if (shown++ > 50) {
                p.printf("%s...%n", indent);
                break;
            }
            p.printf("%s<key size=\"%d\">%s</key>%n", indent, size(key), key);
        }
    }

    @Override
    public void run() {
        try {
            gc();
        } catch (Exception e) {
            throw new SpaceError(e);
        }
    }

    public void gc() {
        if (closed.get())
            return;
        boolean mutated = false;
        for (K key : new ArrayList<>(heads.keySet())) {
            KeyMonitor monitor = monitorFor(key);
            monitor.lock.lock();
            try {
                mutated |= purgeExpiredLocked(key, monitor);
            } finally {
                monitor.lock.unlock();
            }
        }
        if (mutated)
            commitQuietly();
        if (sl != null && sl.getKeySet().isEmpty())
            sl = null;
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true))
            return;
        if (gcFuture != null)
            gcFuture.cancel(false);
        REGISTRY.remove(name);
        try {
            db.close();
        } catch (RuntimeException e) {
            throw new SpaceError(e);
        }
    }

    private void enqueue(K key, V value, long timeout, AppendMode mode) {
        ensureOpen();
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        KeyMonitor monitor = monitorFor(key);
        boolean mutated = false;
        monitor.lock.lock();
        try {
            Head head = heads.get(key);
            if (mode == AppendMode.REPLACE && head != null) {
                deleteChain(head.first);
                head = null;
            }
            Ref ref = new Ref(sequence.incrementAndGet(), value, timeout);
            if (head != null && mode == AppendMode.HEAD) {
                ref.next = head.first;
            }
            refs.put(ref.id, ref);
            if (head == null) {
                head = Head.single(ref.id);
            } else if (mode == AppendMode.HEAD) {
                head.first = ref.id;
                if (head.last < 0)
                    head.last = ref.id;
                head.count++;
            } else {
                if (head.last >= 0) {
                    Ref last = refs.get(head.last);
                    if (last != null) {
                        last.next = ref.id;
                        refs.put(last.id, last);
                    }
                }
                if (head.first < 0)
                    head.first = ref.id;
                head.last = ref.id;
                head.count++;
            }
            heads.put(key, head);
            monitor.hasValue.signalAll();
            mutated = true;
        } finally {
            monitor.lock.unlock();
        }
        if (mutated)
            commitQuietly();
        notifyListeners(key, value);
    }

    private V fetchHead(K key, boolean remove) {
        KeyMonitor monitor = monitorFor(key);
        monitor.lock.lock();
        try {
            FetchResult<V> result = fetchHeadLocked(key, monitor, remove);
            if (result.mutated)
                commitQuietly();
            return result.value;
        } finally {
            monitor.lock.unlock();
        }
    }

    private V fetchTemplate(Template tmpl, boolean remove) {
        K key = (K) tmpl.getKey();
        KeyMonitor monitor = monitorFor(key);
        monitor.lock.lock();
        try {
            FetchResult<V> result = fetchTemplateLocked(key, monitor, tmpl, remove);
            if (result.mutated)
                commitQuietly();
            return result.value;
        } finally {
            monitor.lock.unlock();
        }
    }

    private V awaitValue(Object keyOrTemplate, boolean remove, long timeout) {
        ensureOpen();
        Template tmpl = keyOrTemplate instanceof Template ? (Template) keyOrTemplate : null;
        K key = (K) (tmpl != null ? tmpl.getKey() : keyOrTemplate);
        KeyMonitor monitor = monitorFor(key);
        monitor.lock.lock();
        try {
            boolean timed = timeout != NO_TIMEOUT;
            long nanos = timed ? TimeUnit.MILLISECONDS.toNanos(timeout) : 0L;
            while (true) {
                FetchResult<V> result = tmpl != null
                    ? fetchTemplateLocked(key, monitor, tmpl, remove)
                    : fetchHeadLocked(key, monitor, remove);
                if (result.value != null) {
                    if (result.mutated)
                        commitQuietly();
                    return result.value;
                }
                if (!timed) {
                    monitor.hasValue.await();
                } else {
                    if (nanos <= 0)
                        return null;
                    nanos = monitor.hasValue.awaitNanos(nanos);
                    if (nanos <= 0)
                        return null;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            monitor.lock.unlock();
        }
    }

    private FetchResult<V> fetchHeadLocked(K key, KeyMonitor monitor, boolean remove) {
        boolean mutated = false;
        Head head = heads.get(key);
        while (head != null && head.first >= 0) {
            Ref ref = refs.get(head.first);
            if (ref == null || ref.isExpired()) {
                mutated |= unlinkFirst(key, head, ref);
                head = heads.get(key);
                continue;
            }
            V value = (V) ref.value;
            if (remove) {
                mutated |= unlinkFirst(key, head, ref);
                monitor.isEmpty.signalAll();
            }
            return new FetchResult<>(value, mutated);
        }
        if (head != null && head.count == 0) {
            heads.remove(key);
            mutated = true;
        }
        monitor.isEmpty.signalAll();
        maybeCleanupMonitor(key, monitor);
        return new FetchResult<>(null, mutated);
    }

    private FetchResult<V> fetchTemplateLocked(K key, KeyMonitor monitor, Template tmpl, boolean remove) {
        boolean mutated = false;
        Head head = heads.get(key);
        if (head == null || head.first < 0)
            return new FetchResult<>(null, false);
        long prevId = -1;
        Ref prev = null;
        long currentId = head.first;
        while (currentId >= 0) {
            Ref current = refs.get(currentId);
            if (current == null || current.isExpired()) {
                mutated |= unlinkCurrent(key, head, currentId, prevId, prev);
                head = heads.get(key);
                if (head == null || head.count == 0) {
                    monitor.isEmpty.signalAll();
                    maybeCleanupMonitor(key, monitor);
                    break;
                }
                if (prevId < 0)
                    currentId = head.first;
                else if (prev != null)
                    currentId = prev.next;
                continue;
            }
            Object value = current.value;
            if (tmpl.equals(value)) {
                if (remove) {
                    mutated |= unlinkCurrent(key, head, currentId, prevId, prev);
                    monitor.isEmpty.signalAll();
                }
                return new FetchResult<>((V) value, mutated);
            }
            prevId = currentId;
            prev = current;
            currentId = current.next;
        }
        return new FetchResult<>(null, mutated);
    }

    private boolean purgeExpiredLocked(K key, KeyMonitor monitor) {
        Head head = heads.get(key);
        if (head == null || head.first < 0)
            return false;
        boolean mutated = false;
        long prevId = -1;
        Ref prev = null;
        long currentId = head.first;
        while (currentId >= 0) {
            Ref current = refs.get(currentId);
            if (current == null || current.isExpired()) {
                mutated |= unlinkCurrent(key, head, currentId, prevId, prev);
                head = heads.get(key);
                if (head == null || head.count == 0) {
                    monitor.isEmpty.signalAll();
                    break;
                }
                if (prevId < 0)
                    currentId = head.first;
                else if (prev != null)
                    currentId = prev.next;
                continue;
            }
            prevId = currentId;
            prev = current;
            currentId = current.next;
        }
        if (head == null || head.count == 0)
            maybeCleanupMonitor(key, monitor);
        return mutated;
    }

    private boolean unlinkFirst(K key, Head head, Ref ref) {
        if (ref == null) {
            heads.remove(key);
            return true;
        }
        refs.remove(ref.id);
        head.first = ref.next;
        head.count = Math.max(0, head.count - 1);
        if (head.first < 0) {
            head.last = -1;
            heads.remove(key);
        } else {
            heads.put(key, head);
        }
        return true;
    }

    private boolean unlinkCurrent(K key, Head head, long currentId, long prevId, Ref prev) {
        Ref current = refs.remove(currentId);
        long nextId = current != null ? current.next : -1;
        if (prevId < 0) {
            head.first = nextId;
        } else if (prev != null) {
            prev.next = nextId;
            refs.put(prevId, prev);
        }
        if (nextId < 0)
            head.last = prevId;
        head.count = Math.max(0, head.count - 1);
        if (head.count <= 0) {
            heads.remove(key);
        } else {
            heads.put(key, head);
        }
        return true;
    }

    private void deleteChain(long recid) {
        long current = recid;
        while (current >= 0) {
            Ref ref = refs.remove(current);
            if (ref == null)
                break;
            current = ref.next;
        }
    }

    private boolean queueHasValues(K key) {
        Head head = heads.get(key);
        return head != null && head.count > 0;
    }

    private void notifyListeners(K key, V value) {
        LocalSpace<K, SpaceListener<K,V>> local = sl;
        if (local == null)
            return;
        List<SpaceListener<K,V>> listeners = new ArrayList<>();
        SpaceListener<K,V> listener;
        while ((listener = local.inp(key)) != null)
            listeners.add(listener);
        for (SpaceListener<K,V> l : listeners) {
            l.notify(key, value);
            local.out(key, l);
        }
    }

    private LocalSpace<K, SpaceListener<K,V>> getSL() {
        LocalSpace<K, SpaceListener<K,V>> local = sl;
        if (local == null) {
            synchronized (this) {
                if (sl == null)
                    sl = new LSpace<>();
                local = sl;
            }
        }
        return local;
    }

    private KeyMonitor monitorFor(K key) {
        return monitors.computeIfAbsent(key, k -> new KeyMonitor());
    }

    private void maybeCleanupMonitor(K key, KeyMonitor monitor) {
        if (monitor.lock.hasWaiters(monitor.hasValue))
            return;
        if (queueHasValues(key))
            return;
        monitors.remove(key, monitor);
    }

    private void ensureOpen() {
        if (closed.get())
            throw new IllegalStateException("MDBSpace is closed");
    }

    private Map<String,String> parseOptions(String[] tokens) {
        Map<String,String> opts = new HashMap<>();
        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i];
            int idx = token.indexOf('=');
            if (idx > 0)
                opts.put(token.substring(0, idx).trim(), token.substring(idx + 1).trim());
        }
        return opts;
    }

    private void commitQuietly() {
        try {
            db.commit();
        } catch (RuntimeException e) {
            throw new SpaceError(e);
        }
    }

    private enum AppendMode {
        HEAD,
        TAIL,
        REPLACE
    }

    private static final class Head implements Serializable {
        long first;
        long last;
        long count;

        private Head(long id) {
            this.first = id;
            this.last = id;
            this.count = 1L;
        }

        static Head single(long id) {
            return new Head(id);
        }
    }

    private static final class Ref implements Serializable {
        final long id;
        long next = -1L;
        final long expiresAt;
        final Object value;

        Ref(long id, Object value, long timeout) {
            this.id = id;
            this.value = value;
            if (timeout > 0) {
                this.expiresAt = Instant.now().toEpochMilli() + timeout;
            } else {
                this.expiresAt = Long.MAX_VALUE;
            }
        }

        boolean isExpired() {
            return expiresAt != Long.MAX_VALUE && Instant.now().toEpochMilli() > expiresAt;
        }
    }

    private static final class KeyMonitor {
        final ReentrantLock lock = new ReentrantLock();
        final Condition hasValue = lock.newCondition();
        final Condition isEmpty = lock.newCondition();
    }

    private static final class FetchResult<V> {
        final V value;
        final boolean mutated;

        FetchResult(V value, boolean mutated) {
            this.value = value;
            this.mutated = mutated;
        }
    }
}
