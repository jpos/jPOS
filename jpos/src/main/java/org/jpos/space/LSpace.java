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

import org.jpos.jfr.SpaceEvent;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.io.Serializable;
import java.lang.ref.Cleaner;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LSpace (Loom-optimized Space) implementation with per-key locking for Virtual Thread efficiency.
 *
 * <p>This implementation addresses the thundering herd problem in traditional Space implementations
 * by using per-key {@link ReentrantLock} and {@link Condition} objects instead of global synchronization.
 * With Virtual Threads (Project Loom), this prevents thousands of threads from being
 * unnecessarily woken up when only one is relevant.</p>
 *
 * <p>Key features:
 * <ul>
 *   <li>Per-key isolation: Each key has its own lock and condition variables</li>
 *   <li>Targeted wakeups: Only threads waiting on a specific key are signaled</li>
 *   <li>Virtual Thread optimized: Scales efficiently with thousands of concurrent threads</li>
 *   <li>Full LocalSpace compatibility: Drop-in replacement with same API and behavior</li>
 *   <li>JFR instrumentation: All operations emit SpaceEvent for monitoring</li>
 * </ul>
 * </p>
 *
 * Concurrency notes (core safety invariants):
 * - Never remove a KeyEntry from entries while threads are waiting on that entry's hasValue Condition,
 *   otherwise those waiters can be stranded forever (future out() creates a new KeyEntry+Condition).
 * - Blocking rd()/in() must never return null unless interrupted (or timed out for timed variants).
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 3.0
 */
@SuppressWarnings("unchecked")
public class LSpace<K,V> implements LocalSpace<K,V>, Loggeable, Runnable, AutoCloseable {
    private final ConcurrentHashMap<K, KeyEntry> entries;
    private volatile LocalSpace<K, SpaceListener<K,V>> sl;
    private final ScheduledFuture<?> gcFuture;
    private final Object[] expLocks = new Object[] { new Object(), new Object() };

    public static final long GCDELAY = 5 * 1000;
    private static final long GCLONG = 60_000L;
    private static final long NRD_RESOLUTION = 500L;
    private static final int MAX_ENTRIES_IN_DUMP = 1000;

    private static final long ONE_MILLION = 1_000_000L; // millis -> nanos
    private static final long NO_TIMEOUT = -1L;

    private final Set<K>[] expirables;
    private long lastLongGC = System.nanoTime();
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private static final Cleaner CLEANER = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final CleaningState cleaningState;

    /**
     * Per-key synchronization and queue structure.
     */
    private static class KeyEntry {
        final ReentrantLock lock = new ReentrantLock();
        final Condition hasValue = lock.newCondition();   // signaled when value added
        final Condition isEmpty = lock.newCondition();    // signaled when queue becomes empty (for nrd)
        final LinkedList<Object> queue = new LinkedList<>();
        volatile boolean hasExpirable = false;

        KeyEntry(Object key) {/* key ignored, but could be used for debugging */}
    }


    public LSpace() {
        super();
        this.entries = new ConcurrentHashMap<>(256);
        this.expirables = new Set[] {
          ConcurrentHashMap.newKeySet(),
          ConcurrentHashMap.newKeySet()
        };
        this.gcFuture = SpaceFactory.getGCExecutor().scheduleAtFixedRate(this, GCDELAY, GCDELAY, TimeUnit.MILLISECONDS);
        this.cleaningState = new CleaningState(gcFuture, entries, expirables);
        this.cleanable = CLEANER.register(this, cleaningState);
    }

    // -------------------------
    // JFR tagging helper (patch)
    // -------------------------
    private static String jfrTag(Object keyOrTemplate) {
        if (keyOrTemplate instanceof Template) {
            Object k = ((Template) keyOrTemplate).getKey();
            return "" + k;
        }
        return "" + keyOrTemplate;
    }

    @Override
    public void out(K key, V value) {
        out(key, value, NO_TIMEOUT);
    }

    @Override
    public void out(K key, V value, long timeout) {
        ensureOpen();
        var jfr = new SpaceEvent("out:tim", "" + key);
        jfr.begin();
        try {
            if (key == null || value == null) {
                throw new NullPointerException("key=" + key + ", value=" + value);
            }
    
            Object v = value;
            if (timeout > 0)
                v = new Expirable(value, System.nanoTime() + (timeout * ONE_MILLION));
    
            while (true) {
                KeyEntry entry = entries.computeIfAbsent(key, KeyEntry()::new);
    
                entry.lock.lock();
                try {
                    if (entries.get(key) != entry)
                        continue;
    
                    boolean wasEmpty = entry.queue.isEmpty();
                    entry.queue.addLast(v);
    
                    if (timeout > 0) {
                        entry.hasExpirable = true;
                        registerExpirable(key, timeout);
                    }
                    if (wasEmpty)
                        entry.hasValue.signalAll();
    
                    break;
                } finally {
                    entry.lock.unlock();
                }
            }
    
            if (sl != null)
                notifyListeners(key, value);
        } finally {
            jfr.commit();
        }
    }

    @Override
    public void push(K key, V value) {
        push(key, value, NO_TIMEOUT);
    }

    @Override
    public void push(K key, V value, long timeout) {
        ensureOpen();
        var jfr = new SpaceEvent("push:tim", "" + key);
        jfr.begin();
        try {
            if (key == null || value == null)
                throw new NullPointerException("key=" + key + ", value=" + value);
    
            Object v = value;
            if (timeout > 0)
                v = new Expirable(value, System.nanoTime() + (timeout * ONE_MILLION));
    
            while (true) {
                KeyEntry entry = entries.computeIfAbsent(key, KeyEntry::new);
    
                entry.lock.lock();
                try {
                    if (entries.get(key) != entry)
                        continue;
    
                    boolean wasEmpty = entry.queue.isEmpty();
                    entry.queue.addFirst(v);
    
                    if (timeout > 0) {
                        entry.hasExpirable = true;
                        registerExpirable(key, timeout);
                    }
                    if (wasEmpty)
                        entry.hasValue.signalAll();
    
                    break;
                } finally {
                    entry.lock.unlock();
                }
            }
    
            if (sl != null)
                notifyListeners(key, value);
        } finally {
            jfr.commit();
        }
    }

    @Override
    public void put(K key, V value) {
        put(key, value, NO_TIMEOUT);
    }

    @Override
    public void put(K key, V value, long timeout) {
        ensureOpen();
        var jfr = new SpaceEvent("put:tim", "" + key);
        jfr.begin();
        try {
            if (key == null || value == null)
                throw new NullPointerException("key=" + key + ", value=" + value);

            Object v = value;
            if (timeout > 0)
                v = new Expirable(value, System.nanoTime() + (timeout * ONE_MILLION));

            while (true) {
                KeyEntry entry = entries.computeIfAbsent(key, KeyEntry::new);

                entry.lock.lock();
                try {
                    if (entries.get(key) != entry) {
                        continue;
                    }

                    entry.queue.clear();
                    entry.queue.add(v);

                    if (timeout > 0) {
                        entry.hasExpirable = true;
                        registerExpirable(key, timeout);
                    } else {
                        entry.hasExpirable = false;
                        unregisterExpirable(key);
                    }

                    entry.hasValue.signalAll();
                    break;
                } finally {
                    entry.lock.unlock();
                }
            }

            if (sl != null)
                notifyListeners(key, value);
        } finally {
            jfr.commit();
        }
    }


    @Override
    public V rdp(Object key) {
        ensureOpen();
        var jfr = new SpaceEvent("rdp", "" + key);
        jfr.begin();
        try {
            if (key instanceof Template)
                return (V) getObjectNonBlocking((Template) key, false);
            return (V) getHeadNonBlocking((K) key, false);
        } finally {
            jfr.commit();
        }
    }

    @Override
    public V inp(Object key) {
        ensureOpen();
        var jfr = new SpaceEvent("inp", "" + key);
        jfr.begin();
        try {
            if (key instanceof Template)
                return (V) getObjectNonBlocking((Template) key, true);
            return (V) getHeadNonBlocking((K) key, true);
        } finally {
            jfr.commit();
        }
    }

    @Override
    public V in(Object key) {
        ensureOpen();
        String op = key instanceof Template ? "in:tmpl" : "in";
        var jfr = new SpaceEvent(op, jfrTag(key));
        jfr.begin();
        try {
            if (key instanceof Template)
                return inTemplate((Template) key);
            return inKey((K) key);
        } finally {
            jfr.commit();
        }
    }

    @Override
    public V in(Object key, long timeout) {
        ensureOpen();
        String op = key instanceof Template ? "in:tim:tmpl" : "in:tim";
        var jfr = new SpaceEvent(op, jfrTag(key));
        jfr.begin();
        try {
            if (key instanceof Template)
                return inTemplate((Template) key, timeout);
            return inKey((K) key, timeout);
        } finally {
            jfr.commit();
        }
    }

    @Override
    public V rd(Object key) {
        ensureOpen();
        String op = key instanceof Template ? "rd:tmpl" : "rd";
        var jfr = new SpaceEvent(op, jfrTag(key));
        jfr.begin();
        try {
            if (key instanceof Template)
                return rdTemplate((Template) key);
            return rdKey((K) key);
        } finally {
            jfr.commit();
        }
    }

    @Override
    public V rd(Object key, long timeout) {
        ensureOpen();
        String op = key instanceof Template ? "rd:tim:tmpl" : "rd:tim";
        var jfr = new SpaceEvent(op, jfrTag(key));
        jfr.begin();
        try {
            if (key instanceof Template)
                return rdTemplate((Template) key, timeout);
            return rdKey((K) key, timeout);
        } finally {
            jfr.commit();
        }
    }

    @Override
    public void nrd(Object key) {
        ensureOpen();
        var jfr = new SpaceEvent("nrd", "" + key);
        jfr.begin();
        try {
            K k = (K) key;
            while (true) {
                KeyEntry entry = entries.get(k);
                if (entry == null)
                    return;

                entry.lock.lock();
                try {
                    Object obj = getHead(entry, k, false);
                    if (obj == null) {
                        postFetchHousekeeping(k, entry);
                        return;
                    }
                    try {
                        entry.isEmpty.await(NRD_RESOLUTION, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } finally {
                    entry.lock.unlock();
                }
            }
        } finally {
            jfr.commit();
        }
    }

    @Override
    public V nrd(Object key, long timeout) {
        ensureOpen();
        var jfr = new SpaceEvent("nrd:tim", "" + key);
        jfr.begin();
        try {
            K k = (K) key;
            long deadline = System.nanoTime() + timeout * ONE_MILLION;

            while (true) {
                KeyEntry entry = entries.get(k);
                if (entry == null)
                    return null;

                entry.lock.lock();
                try {
                    V obj = (V) getHead(entry, k, false);
                    if (obj == null) {
                        postFetchHousekeeping(k, entry);
                        return null;
                    }
                    long remaining = deadline - System.nanoTime();
                    if (remaining <= 0)
                        return obj;

                    long waitTime = Math.min(NRD_RESOLUTION * ONE_MILLION, remaining);
                    try {
                        entry.isEmpty.awaitNanos(waitTime);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        return obj;
                    }
                } finally {
                    entry.lock.unlock();
                }
            }
        } finally {
            jfr.commit();
        }
    }

    @Override
    public boolean existAny(K[] keys) {
        ensureOpen();
        for (K key : keys) {
            if (rdp(key) != null)
                return true;
        }
        return false;
    }

    @Override
    public boolean existAny(K[] keys, long timeout) {
        ensureOpen();
        var jfr = new SpaceEvent("existAny:tim", Integer.toString(keys != null ? keys.length : 0));
        jfr.begin();
        try {
            long deadline = System.nanoTime() + timeout * ONE_MILLION;
            long pollInterval = 10 * ONE_MILLION;

            while (true) {
                for (K key : keys) {
                    if (rdp(key) != null)
                        return true;
                }

                long remaining = deadline - System.nanoTime();
                if (remaining <= 0)
                    return false;

                LockSupport.parkNanos(Math.min(pollInterval, remaining));
            }
        } finally {
            jfr.commit();
        }
    }

    @Override
    public void run() {
        // Scheduler ticks may race with close(); treat closed as a no-op.
        if (closed.get())
            return;
        try {
            gc();
        } catch (Exception e) {
            e.printStackTrace(); // should never happen
        }
    }

    public void gc() {
        // Avoid work after close if a scheduled tick slips through.
        if (closed.get())
            return;

        gc(0);
        if (System.nanoTime() - lastLongGC > GCLONG * ONE_MILLION) {
            gc(1);
            lastLongGC = System.nanoTime();
        }
    }

    private void gc(int generation) {
        // gc() already guards closed; keep gc(int) lean.
        var jfr = new SpaceEvent("gc", Integer.toString(generation));
        jfr.begin();

        Set<K> keysToCheck;
        synchronized (expLocks[generation]) {
            keysToCheck = new HashSet<>(expirables[generation]);
            expirables[generation].clear();
        }
        for (K key : keysToCheck) {
            KeyEntry entry = entries.get(key);
            if (entry == null)
                continue;

            entry.lock.lock();
            try {
                boolean stillHasExpirable = false;
                boolean sawAnyExpirable = false;

                Iterator<Object> iterator = entry.queue.iterator();
                while (iterator.hasNext()) {
                    Object obj = iterator.next();
                    if (obj instanceof Expirable) {
                        sawAnyExpirable = true;
                        Object value = ((Expirable) obj).getValue();
                        if (value == null) {
                            iterator.remove();
                        } else {
                            stillHasExpirable = true;
                        }
                    }
                }

                entry.hasExpirable = stillHasExpirable;

                if (stillHasExpirable) {
                    synchronized (expLocks[generation]) {
                        expirables[generation].add(key);
                    }
                    // Queue might have changed (expired items removed), wake any rd/in waiters.
                    entry.hasValue.signalAll();
                } else {
                    // No longer has expirables anywhere; ensure we don't keep the key in either generation set.
                    if (sawAnyExpirable)
                        unregisterExpirable(key);
                }

                // Apply the same safe empty-entry policy used everywhere else.
                if (entry.queue.isEmpty()) {
                    postFetchHousekeeping(key, entry);
                }
            } finally {
                entry.lock.unlock();
            }

            Thread.yield();
        }

        if (sl != null && sl.getKeySet().isEmpty()) {
            sl = null;
        }

        jfr.commit();
    }

    @Override
    public int size(Object key) {
        ensureOpen();
        var jfr = new SpaceEvent("size", "" + key);
        jfr.begin();

        int size = 0;
        KeyEntry entry = entries.get((K) key);
        if (entry != null) {
            entry.lock.lock();
            try {
                size = entry.queue.size();
            } finally {
                entry.lock.unlock();
            }
        }

        jfr.commit();
        return size;
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
        if (sl != null) {
            sl.inp((K) new ObjectTemplate(key, listener));
        }
    }

    public boolean isEmpty() {
        ensureOpen();
        return entries.isEmpty();
    }

    @Override
    public Set<K> getKeySet() {
        ensureOpen();
        return new HashSet<>(entries.keySet());
    }

    public String getKeysAsString() {
        ensureOpen();
        StringBuilder sb = new StringBuilder();
        Object[] keys = entries.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            if (i > 0)
                sb.append(' ');
            sb.append(keys[i]);
        }
        return sb.toString();
    }

    @Override
    public void dump(PrintStream p, String indent) {
        ensureOpen();
        var jfr = new SpaceEvent("dump", "");
        jfr.begin();

        int size = entries.size();
        if (size > MAX_ENTRIES_IN_DUMP * 100) {
            p.printf("%sWARNING - space too big, size=%d%n", indent, size);
            jfr.commit();
            return;
        }

        Object[] keys = entries.keySet().toArray();

        int i = 0;
        for (Object key : keys) {
            p.printf("%s<key count='%d'>%s</key>%n", indent, size(key), key);
            if (i++ > MAX_ENTRIES_IN_DUMP) {
                p.printf("%s...%n", indent);
                p.printf("%s...%n", indent);
                break;
            }
        }
        p.printf("%s key-count: %d%n", indent, keys.length);

        int exp0 = expirables[0].size();
        int exp1 = expirables[1].size();
        p.printf("%s    gcinfo: %d,%d%n", indent, exp0, exp1);

        jfr.commit();
    }

    public void notifyListeners(Object key, Object value) {
        ensureOpen();
        var jfr = new SpaceEvent("notify", "" + key);
        jfr.begin();
        LocalSpace<K, SpaceListener<K,V>> localSl = sl;  // Capture volatile read once
        if (localSl == null) {
            jfr.commit();
            return;
        }
        Object[] listeners = null;
        LSpace<K, SpaceListener<K,V>> lsl = (LSpace<K, SpaceListener<K,V>>) localSl;
        KeyEntry slEntry = lsl.entries.get((K) key);
        if (slEntry != null) {
            slEntry.lock.lock();
            try {
                listeners = slEntry.queue.toArray();
            } finally {
                slEntry.lock.unlock();
            }
        }

        if (listeners != null) {
            for (Object listener : listeners) {
                Object o = listener;
                if (o instanceof Expirable)
                    o = ((Expirable) o).getValue();
                if (o instanceof SpaceListener)
                    ((SpaceListener) o).notify(key, value);
            }
        }

        jfr.commit();
    }

    /**
     * Non-standard method (required for space replication) - use with care.
     */
    public Map getEntries() {
        ensureOpen();
        Map<K, List> result = new HashMap<>();
        for (var e : entries.entrySet()) {
            KeyEntry entry = e.getValue();
            entry.lock.lock();
            try {
                result.put(e.getKey(), new LinkedList<>(entry.queue));
            } finally {
                entry.lock.unlock();
            }
        }
        return result;
    }

    /**
     * Non-standard method (required for space replication) - use with care.
     */
    public void setEntries(Map entries) {
        ensureOpen();
        this.entries.clear();
        for (var e : (Set<Map.Entry>)entries.entrySet()) {
            K key =  (K)e.getKey();
            List<V> list = (List<V>)e.getValue();
            KeyEntry entry = this.entries.computeIfAbsent(key, KeyEntry::new);
            entry.lock.lock();
            try {
                entry.queue.clear();
                entry.queue.addAll(list);
                // Conservatively: if replication injects Expirables, caller should also registerExpirable appropriately.
                // We do not attempt to infer expirables here.
            } finally {
                entry.lock.unlock();
            }
        }
    }

    /**
     * Cancels the periodic GC task so this instance can be garbage-collected.
     * Safe to call multiple times.
     */
    public void close() {
        if (!closed.compareAndSet(false, true))
            return;

        if (gcFuture != null) {
            gcFuture.cancel(false);
        }
        // If sl is an LSpace, allow it to release resources as well.
        LocalSpace<K, SpaceListener<K,V>> s = sl;
        if (s instanceof LSpace<?,?>) {
            ((LSpace<?,?>) s).close();
        }
        sl = null;
        entries.clear();
        expirables[0].clear();
        expirables[1].clear();
        cleanable.clean(); // Eager cleanup
    }

    // ========== Blocking (deduplicated) ==========

    private V inKey(K key) {
        return awaitValue(key, entry -> getHead(entry, key, true), NO_TIMEOUT);
    }

    private V inKey(K key, long timeout) {
        return awaitValue(key, entry -> getHead(entry, key, true), timeout);
    }

    private V rdKey(K key) {
        return awaitValue(key, entry -> getHead(entry, key, false), NO_TIMEOUT);
    }

    private V rdKey(K key, long timeout) {
        return awaitValue(key, entry -> getHead(entry, key, false), timeout);
    }

    private V inTemplate(Template tmpl) {
        K key = (K) tmpl.getKey();
        return awaitValue(key, entry -> getObject(entry, key, tmpl, true), NO_TIMEOUT);
    }

    private V inTemplate(Template tmpl, long timeout) {
        K key = (K) tmpl.getKey();
        return awaitValue(key, entry -> getObject(entry, key, tmpl, true), timeout);
    }

    private V rdTemplate(Template tmpl) {
        K key = (K) tmpl.getKey();
        return awaitValue(key, entry -> getObject(entry, key, tmpl, false), NO_TIMEOUT);
    }

    private V rdTemplate(Template tmpl, long timeout) {
        K key = (K) tmpl.getKey();
        return awaitValue(key, entry -> getObject(entry, key, tmpl, false), timeout);
    }

    // ========== Non-blocking helpers ==========

    /**
     * Get head of queue (non-blocking version for rdp/inp).
     * Uses safe empty-entry cleanup via postFetchHousekeeping to avoid orphaning waiters.
     */
    private Object getHeadNonBlocking(K key, boolean remove) {
        KeyEntry entry = entries.get(key);
        if (entry == null)
            return null;

        entry.lock.lock();
        try {
            if (entries.get(key) != entry)
                return null;

            Object result = getHead(entry, key, remove);

            if (remove) {
                // If remove emptied the queue, postFetchHousekeeping will handle safe removal/signals.
                postFetchHousekeeping(key, entry);
            }
            return result;
        } finally {
            entry.lock.unlock();
        }
    }

    /**
     * Get object matching template (non-blocking version for rdp/inp).
     * Uses safe empty-entry cleanup via postFetchHousekeeping to avoid orphaning waiters.
     */
    private Object getObjectNonBlocking(Template tmpl, boolean remove) {
        K key = (K) tmpl.getKey();
        KeyEntry entry = entries.get(key);
        if (entry == null)
            return null;

        entry.lock.lock();
        try {
            if (entries.get(key) != entry)
                return null;

            Object result = getObject(entry, key, tmpl, remove);

            if (remove) {
                postFetchHousekeeping(key, entry);
            }
            return result;
        } finally {
            entry.lock.unlock();
        }
    }

    /**
     * Get head of queue.
     * MUST be called with entry.lock held.
     */
    private Object getHead(KeyEntry entry, K key, boolean remove) {
        Object result = null;
        boolean wasExpirable = false;

        while (result == null && !entry.queue.isEmpty()) {
            Object obj = entry.queue.getFirst();

            if (obj instanceof Expirable) {
                Object value = ((Expirable) obj).getValue();
                wasExpirable = true;

                if (value == null) {
                    entry.queue.removeFirst();
                    continue;
                } else {
                    result = value;
                }
            } else {
                result = obj;
            }

            if (remove && result != null) {
                entry.queue.removeFirst();
            }
        }

        if (entry.queue.isEmpty()) {
            entry.hasExpirable = false;
            if (wasExpirable)
                unregisterExpirable(key);
        }

        return result;
    }

    /**
     * Get object matching template.
     * MUST be called with entry.lock held.
     */
    private Object getObject(KeyEntry entry, K key, Template tmpl, boolean remove) {
        Object result = null;
        Iterator<Object> iterator = entry.queue.iterator();
        boolean wasExpirable = false;

        while (iterator.hasNext()) {
            Object obj = iterator.next();

            if (obj instanceof Expirable) {
                Object value = ((Expirable) obj).getValue();
                if (value == null) {
                    iterator.remove();
                    wasExpirable = true;
                    continue;
                } else {
                    obj = value;
                }
            }

            if (tmpl.equals(obj)) {
                result = obj;
                if (remove)
                    iterator.remove();
                break;
            }
        }

        if (entry.queue.isEmpty()) {
            entry.hasExpirable = false;
            if (wasExpirable)
                unregisterExpirable(key);
        }

        return result;
    }

    private void ensureOpen() {
        if (closed.get())
            throw new IllegalStateException("LSpace is closed");
    }

    // ========== Listener-space helpers ==========

    private LocalSpace<K, SpaceListener<K,V>> getSL() {
        ensureOpen();
        if (sl == null) {
            synchronized (this) {
                ensureOpen();
                if (sl == null) {
                    sl = new LSpace<>();
                    cleaningState.sl = (AutoCloseable) sl;
                }
            }
        }
        return sl;
    }

    private void registerExpirable(K k, long t) {
        int g = (t > GCLONG) ? 1 : 0;
        synchronized (expLocks[g]) {
            expirables[g].add(k);
        }
    }

    private void unregisterExpirable(K k) {
        synchronized (expLocks[0]) {
            synchronized (expLocks[1]) {
                expirables[0].remove(k);
                expirables[1].remove(k);
            }
        }
    }

    // ========== Blocking core (shared) ==========

    @FunctionalInterface
    private interface Fetcher {
        Object fetch(KeyEntry entry);
    }

    /**
     * Common blocking wait-loop for rd/in operations (key or template).
     *
     * Ensures:
     * - No premature null returns due to entry replacement (retries outer loop).
     * - Timed variants remove empty computeIfAbsent-created entries on timeout/interrupt (postFetchHousekeeping).
     * - Does not orphan waiters because postFetchHousekeeping refuses to remove if hasValue waiters exist.
     */
    @SuppressWarnings("unchecked")
    private V awaitValue(K key, Fetcher fetcher, long timeoutMillis) {
        ensureOpen();

        final boolean timed = timeoutMillis != NO_TIMEOUT;
        final long deadlineNanos = timed ? System.nanoTime() + timeoutMillis * ONE_MILLION : 0L;

        for (;;) {
            final KeyEntry entry = entries.computeIfAbsent(key, KeyEntry::new);

            entry.lock.lock();
            try {
                if (entries.get(key) != entry)
                    continue;

                for (;;) {
                    Object obj = fetcher.fetch(entry);
                    if (obj != null) {
                        postFetchHousekeeping(key, entry);
                        return (V) obj;
                    }

                    if (!timed) {
                        try {
                            entry.hasValue.await();
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            // Avoid leaking an empty entry created by computeIfAbsent for a waiter that got interrupted.
                            postFetchHousekeeping(key, entry);
                            break;
                        }
                    } else {
                        try {
                            long remaining = entry.hasValue.awaitNanos(deadlineNanos - System.nanoTime());
                            if (remaining <= 0) {
                                // Avoid leaking empty entries created by computeIfAbsent when timing out.
                                postFetchHousekeeping(key, entry);
                                return null;
                            }
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            postFetchHousekeeping(key, entry);
                            return null;
                        }
                    }

                    // If someone removed/replaced the entry, restart outer loop to bind to the current entry.
                    if (entries.get(key) != entry)
                        break;
                } // inner loop
            } finally {
                entry.lock.unlock();
            }
        } // outer loop
    }

    /**
     * Housekeeping that must run under entry.lock:
     * - Wake nrd waiters when queue becomes empty.
     * - Remove entry only if queue is empty AND no waiters are parked on hasValue (prevents orphaning).
     */
    private void postFetchHousekeeping(K key, KeyEntry entry) {
        if (!entry.queue.isEmpty())
            return;

        // Always wake nrd waiters when empty.
        entry.isEmpty.signalAll();

        // Remove only when safe (no hasValue waiters).
        if (entries.get(key) == entry && !entry.lock.hasWaiters(entry.hasValue)) {
            entries.remove(key, entry);
        }
    }

    /**
     * Expirable wrapper for values with timeout.
     */
    static class Expirable implements Comparable, Serializable {
        private static final long serialVersionUID = 0xA7F22BF5;

        Object value;
        long expires;

        Expirable(Object value, long expires) {
            super();
            this.value = value;
            this.expires = expires;
        }

        boolean isExpired() {
            return (System.nanoTime() - expires) > 0;
        }

        @Override
        public String toString() {
            return getClass().getName()
              + "@" + Integer.toHexString(hashCode())
              + ",value=" + value.toString()
              + ",expired=" + isExpired();
        }

        Object getValue() {
            return isExpired() ? null : value;
        }

        @Override
        public int compareTo(Object other) {
            long diff = this.expires - ((Expirable) other).expires;
            return diff > 0 ? 1 : diff < 0 ? -1 : 0;
        }
    }

    private static final class CleaningState implements Runnable {
        private final ScheduledFuture<?> gcFuture;
        private final ConcurrentHashMap<?,?> entries;
        private final Set<?>[] expirables;

        // We keep a reference to sl so we can cancel its scheduler too.
        // This does not introduce a new retention path; it already hangs off the parent space.
        private volatile AutoCloseable sl; // store as AutoCloseable to avoid generics pain

        private final AtomicBoolean cleaned = new AtomicBoolean(false);

        private CleaningState(ScheduledFuture<?> gcFuture,
                              ConcurrentHashMap<?,?> entries,
                              Set<?>[] expirables) {
            this.gcFuture = gcFuture;
            this.entries = entries;
            this.expirables = expirables;
        }

        @Override
        public void run() {
            if (!cleaned.compareAndSet(false, true))
                return;

            try {
                if (gcFuture != null)
                    gcFuture.cancel(false);
            } catch (Throwable ignored) { }

            // Best-effort close of the listener space.
            AutoCloseable s = sl;
            if (s != null) {
                try {
                    s.close();
                } catch (Throwable ignored) { }
                sl = null;
            }

            try {
                entries.clear();
            } catch (Throwable ignored) { }

            try {
                expirables[0].clear();
            } catch (Throwable ignored) { }
            try {
                expirables[1].clear();
            } catch (Throwable ignored) { }
        }
    }
}
