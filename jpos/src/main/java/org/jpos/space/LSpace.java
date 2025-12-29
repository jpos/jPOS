/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2025 jPOS Software SRL
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 3.0
 */
@SuppressWarnings("unchecked")
public class LSpace<K,V> implements LocalSpace<K,V>, Loggeable, Runnable {
    private final ConcurrentHashMap<K, KeyEntry> entries;
    private volatile LocalSpace<K, SpaceListener<K,V>> sl;    // space listeners

    public static final long GCDELAY = 5*1000;
    private static final long GCLONG = 60_000L;
    private static final long NRD_RESOLUTION = 500L;
    private static final int MAX_ENTRIES_IN_DUMP = 1000;
    private static final long ONE_MILLION = 1_000_000L;         // multiplier millis --> nanos

    private final Set<K>[] expirables;
    private long lastLongGC = System.nanoTime();

    /**
     * Per-key synchronization and queue structure.
     * Each key gets its own KeyEntry with isolated lock and condition variables.
     */
    private static class KeyEntry {
        final ReentrantLock lock = new ReentrantLock();
        final Condition hasValue = lock.newCondition();   // signaled when value added
        final Condition isEmpty = lock.newCondition();     // signaled when queue becomes empty (for nrd)
        final LinkedList<Object> queue = new LinkedList<>();
        volatile boolean hasExpirable = false;
    }

    public LSpace() {
        super();
        this.entries = new ConcurrentHashMap<>(256);  // Initial capacity to reduce resizing
        this.expirables = new Set[] {
            ConcurrentHashMap.newKeySet(),
            ConcurrentHashMap.newKeySet()
        };
        SpaceFactory.getGCExecutor().scheduleAtFixedRate(this, GCDELAY, GCDELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void out(K key, V value) {
        var jfr = new SpaceEvent("out", "" + key);
        jfr.begin();

        if (key == null || value == null) {
            jfr.commit();
            throw new NullPointerException("key=" + key + ", value=" + value);
        }

        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());

        entry.lock.lock();
        try {
            boolean wasEmpty = entry.queue.isEmpty();
            entry.queue.addLast(value);
            if (wasEmpty)
                entry.hasValue.signalAll();  // Wake ALL readers (multiple rd() can read same value)
        } finally {
            entry.lock.unlock();
        }

        if (sl != null)
            notifyListeners(key, value);

        jfr.commit();
    }

    @Override
    public void out(K key, V value, long timeout) {
        var jfr = new SpaceEvent("out:tim", "" + key);
        jfr.begin();

        if (key == null || value == null) {
            jfr.commit();
            throw new NullPointerException("key=" + key + ", value=" + value);
        }

        Object v = value;
        if (timeout > 0) {
            v = new Expirable(value, System.nanoTime() + (timeout * ONE_MILLION));
        }

        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());

        entry.lock.lock();
        try {
            boolean wasEmpty = entry.queue.isEmpty();
            entry.queue.addLast(v);
            if (timeout > 0) {
                entry.hasExpirable = true;
                registerExpirable(key, timeout);
            }
            if (wasEmpty)
                entry.hasValue.signalAll();  // Wake ALL readers (multiple rd() can read same value)
        } finally {
            entry.lock.unlock();
        }

        if (sl != null)
            notifyListeners(key, value);

        jfr.commit();
    }

    @Override
    public void push(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException("key=" + key + ", value=" + value);

        var jfr = new SpaceEvent("push", "" + key);
        jfr.begin();

        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());

        entry.lock.lock();
        try {
            boolean wasEmpty = entry.queue.isEmpty();
            entry.queue.addFirst(value);  // LIFO behavior
            if (wasEmpty)
                entry.hasValue.signalAll();  // Wake ALL readers (multiple rd() can read same value)
        } finally {
            entry.lock.unlock();
        }

        if (sl != null)
            notifyListeners(key, value);

        jfr.commit();
    }

    @Override
    public void push(K key, V value, long timeout) {
        if (key == null || value == null)
            throw new NullPointerException("key=" + key + ", value=" + value);

        var jfr = new SpaceEvent("push:tim", "" + key);
        jfr.begin();

        Object v = value;
        if (timeout > 0) {
            v = new Expirable(value, System.nanoTime() + (timeout * ONE_MILLION));
        }

        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());

        entry.lock.lock();
        try {
            boolean wasEmpty = entry.queue.isEmpty();
            entry.queue.addFirst(v);  // LIFO behavior
            if (timeout > 0) {
                entry.hasExpirable = true;
                registerExpirable(key, timeout);
            }
            if (wasEmpty)
                entry.hasValue.signalAll();  // Wake ALL readers (multiple rd() can read same value)
        } finally {
            entry.lock.unlock();
        }

        if (sl != null)
            notifyListeners(key, value);

        jfr.commit();
    }

    @Override
    public void put(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException("key=" + key + ", value=" + value);

        var jfr = new SpaceEvent("put", "" + key);
        jfr.begin();

        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());

        entry.lock.lock();
        try {
            entry.queue.clear();
            entry.queue.add(value);
            entry.hasExpirable = false;
            entry.hasValue.signalAll();  // Wake ALL readers since queue was replaced
        } finally {
            entry.lock.unlock();
        }

        if (sl != null)
            notifyListeners(key, value);

        jfr.commit();
    }

    @Override
    public void put(K key, V value, long timeout) {
        if (key == null || value == null)
            throw new NullPointerException("key=" + key + ", value=" + value);

        var jfr = new SpaceEvent("put:tim", "" + key);
        jfr.begin();

        Object v = value;
        if (timeout > 0) {
            v = new Expirable(value, System.nanoTime() + (timeout * ONE_MILLION));
        }

        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());

        entry.lock.lock();
        try {
            entry.queue.clear();
            entry.queue.add(v);
            if (timeout > 0) {
                entry.hasExpirable = true;
                registerExpirable(key, timeout);
            } else {
                entry.hasExpirable = false;
            }
            entry.hasValue.signalAll();  // Wake ALL readers since queue was replaced
        } finally {
            entry.lock.unlock();
        }

        if (sl != null)
            notifyListeners(key, value);

        jfr.commit();
    }

    @Override
    public V rdp(Object key) {
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
        if (key instanceof Template)
            return inTemplate((Template) key);
        return inKey((K) key);
    }

    private V inKey(K key) {
        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());
        V result = null;

        entry.lock.lock();
        try {
            while (result == null) {
                result = (V) getHead(entry, key, true);
                if (result != null)
                    break;

                try {
                    entry.hasValue.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // Cleanup empty entry
            if (entry.queue.isEmpty()) {
                entries.remove(key, entry);
                entry.isEmpty.signalAll();
            }
        } finally {
            entry.lock.unlock();
        }

        return result;
    }

    private V inTemplate(Template tmpl) {
        K key = (K) tmpl.getKey();
        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());
        V result = null;

        entry.lock.lock();
        try {
            while (result == null) {
                result = (V) getObject(entry, key, tmpl, true);
                if (result != null)
                    break;

                try {
                    entry.hasValue.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (entry.queue.isEmpty()) {
                entries.remove(key, entry);
                entry.isEmpty.signalAll();
            }
        } finally {
            entry.lock.unlock();
        }

        return result;
    }

    @Override
    public V in(Object key, long timeout) {
        if (key instanceof Template)
            return inTemplate((Template) key, timeout);
        return inKey((K) key, timeout);
    }

    private V inKey(K key, long timeout) {
        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());
        V result = null;

        long now = System.nanoTime();
        long deadline = now + timeout * ONE_MILLION;

        entry.lock.lock();
        try {
            while (result == null) {
                result = (V) getHead(entry, key, true);
                if (result != null)
                    break;

                long remaining = deadline - System.nanoTime();
                if (remaining <= 0)
                    break;

                try {
                    entry.hasValue.awaitNanos(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (entry.queue.isEmpty()) {
                entries.remove(key, entry);
                entry.isEmpty.signalAll();
            }
        } finally {
            entry.lock.unlock();
        }

        return result;
    }

    private V inTemplate(Template tmpl, long timeout) {
        K key = (K) tmpl.getKey();
        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());
        V result = null;

        long now = System.nanoTime();
        long deadline = now + timeout * ONE_MILLION;

        entry.lock.lock();
        try {
            while (result == null) {
                result = (V) getObject(entry, key, tmpl, true);
                if (result != null)
                    break;

                long remaining = deadline - System.nanoTime();
                if (remaining <= 0)
                    break;

                try {
                    entry.hasValue.awaitNanos(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (entry.queue.isEmpty()) {
                entries.remove(key, entry);
                entry.isEmpty.signalAll();
            }
        } finally {
            entry.lock.unlock();
        }

        return result;
    }

    @Override
    public V rd(Object key) {
        if (key instanceof Template)
            return rdTemplate((Template) key);
        return rdKey((K) key);
    }

    private V rdKey(K key) {
        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());
        V result = null;

        entry.lock.lock();
        try {
            while (result == null) {
                result = (V) getHead(entry, key, false);
                if (result != null)
                    break;

                try {
                    entry.hasValue.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            entry.lock.unlock();
        }

        return result;
    }

    private V rdTemplate(Template tmpl) {
        K key = (K) tmpl.getKey();
        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());
        V result = null;

        entry.lock.lock();
        try {
            while (result == null) {
                result = (V) getObject(entry, key, tmpl, false);
                if (result != null)
                    break;

                try {
                    entry.hasValue.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            entry.lock.unlock();
        }

        return result;
    }

    @Override
    public V rd(Object key, long timeout) {
        if (key instanceof Template)
            return rdTemplate((Template) key, timeout);
        return rdKey((K) key, timeout);
    }

    private V rdKey(K key, long timeout) {
        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());
        V result = null;

        long now = System.nanoTime();
        long deadline = now + timeout * ONE_MILLION;

        entry.lock.lock();
        try {
            while (result == null) {
                result = (V) getHead(entry, key, false);
                if (result != null)
                    break;

                long remaining = deadline - System.nanoTime();
                if (remaining <= 0)
                    break;

                try {
                    entry.hasValue.awaitNanos(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            entry.lock.unlock();
        }

        return result;
    }

    private V rdTemplate(Template tmpl, long timeout) {
        K key = (K) tmpl.getKey();
        KeyEntry entry = entries.computeIfAbsent(key, k -> new KeyEntry());
        V result = null;

        long now = System.nanoTime();
        long deadline = now + timeout * ONE_MILLION;

        entry.lock.lock();
        try {
            while (result == null) {
                result = (V) getObject(entry, key, tmpl, false);
                if (result != null)
                    break;

                long remaining = deadline - System.nanoTime();
                if (remaining <= 0)
                    break;

                try {
                    entry.hasValue.awaitNanos(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            entry.lock.unlock();
        }

        return result;
    }

    @Override
    public void nrd(Object key) {
        K k = (K) key;
        while (true) {
            KeyEntry entry = entries.get(k);

            if (entry == null)
                return;  // Key not present

            entry.lock.lock();
            try {
                Object obj = getHead(entry, k, false);
                if (obj == null)
                    return;  // Queue is empty

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
    }

    @Override
    public V nrd(Object key, long timeout) {
        K k = (K) key;
        long now = System.nanoTime();
        long deadline = now + timeout * ONE_MILLION;

        while (true) {
            KeyEntry entry = entries.get(k);

            if (entry == null)
                return null;  // Key not present

            entry.lock.lock();
            try {
                V obj = (V) getHead(entry, k, false);
                if (obj == null)
                    return null;  // Queue is empty

                long remaining = deadline - System.nanoTime();
                if (remaining <= 0)
                    return obj;  // Timeout, return current value

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
    }

    @Override
    public boolean existAny(K[] keys) {
        for (K key : keys) {
            if (rdp(key) != null)
                return true;
        }
        return false;
    }

    @Override
    public boolean existAny(K[] keys, long timeout) {
        long now = System.nanoTime();
        long deadline = now + timeout * ONE_MILLION;
        long pollInterval = 10 * ONE_MILLION;  // 10ms in nanos

        while (true) {
            // Check all keys
            for (K key : keys) {
                if (rdp(key) != null)
                    return true;
            }

            // Check timeout
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0)
                return false;

            // Sleep briefly
            long sleepTime = Math.min(pollInterval, remaining);
            LockSupport.parkNanos(sleepTime);
        }
    }

    @Override
    public void run() {
        try {
            gc();
        } catch (Exception e) {
            e.printStackTrace(); // this should never happen
        }
    }

    public void gc() {
        gc(0);
        if (System.nanoTime() - lastLongGC > GCLONG * ONE_MILLION) {
            gc(1);
            lastLongGC = System.nanoTime();
        }
    }

    private void gc(int generation) {
        var jfr = new SpaceEvent("gc", Integer.toString(generation));
        jfr.begin();

        // Snapshot keys to check (avoid concurrent modification)
        Set<K> keysToCheck = new HashSet<>(expirables[generation]);
        expirables[generation].clear();

        // Check each key (no global lock)
        for (K key : keysToCheck) {
            KeyEntry entry = entries.get(key);

            if (entry == null)
                continue;  // Already removed

            entry.lock.lock();
            try {
                boolean stillHasExpirable = false;

                Iterator<Object> iterator = entry.queue.iterator();
                while (iterator.hasNext()) {
                    Object obj = iterator.next();

                    if (obj instanceof Expirable) {
                        Object value = ((Expirable) obj).getValue();

                        if (value == null) {  // Expired
                            iterator.remove();
                        } else {
                            stillHasExpirable = true;
                        }
                    }
                }

                entry.hasExpirable = stillHasExpirable;

                if (stillHasExpirable) {
                    expirables[generation].add(key);
                }

                if (entry.queue.isEmpty()) {
                    entries.remove(key, entry);
                    entry.isEmpty.signalAll();
                } else {
                    // Wake readers since queue may have changed
                    entry.hasValue.signalAll();
                }
            } finally {
                entry.lock.unlock();
            }

            Thread.yield();  // Be nice to other threads
        }

        // Cleanup listener space
        if (sl != null && sl.getKeySet().isEmpty()) {
            sl = null;
        }

        jfr.commit();
    }

    @Override
    public int size(Object key) {
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
        getSL().out((K) key, listener);
    }

    @Override
    public void addListener(Object key, SpaceListener listener, long timeout) {
        getSL().out((K) key, listener, timeout);
    }

    @Override
    public void removeListener(Object key, SpaceListener listener) {
        if (sl != null) {
            sl.inp((K) new ObjectTemplate(key, listener));
        }
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public Set<K> getKeySet() {
        return new HashSet<>(entries.keySet());
    }

    public String getKeysAsString() {
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
        var jfr = new SpaceEvent("dump", "");
        jfr.begin();

        Object[] keys;
        int size = entries.size();
        if (size > MAX_ENTRIES_IN_DUMP * 100) {
            p.printf("%sWARNING - space too big, size=%d%n", indent, size);
            jfr.commit();
            return;
        }

        keys = entries.keySet().toArray();

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
        var jfr = new SpaceEvent("notify", "" + key);
        jfr.begin();

        if (sl == null) {
            jfr.commit();
            return;
        }

        // Get snapshot of listeners - sl is LSpace so access via package-private entries
        Object[] listeners = null;
        LSpace<K, SpaceListener<K,V>> lsl = (LSpace<K, SpaceListener<K,V>>) sl;
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
     * Non-standard method (required for space replication) - use with care
     * @return underlying entry map
     */
    public Map getEntries() {
        // For compatibility - return a snapshot as Map<K, List>
        Map<K, List> result = new HashMap<>();
        for (Map.Entry<K, KeyEntry> e : entries.entrySet()) {
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
     * Non-standard method (required for space replication) - use with care
     * @param entries underlying entry map
     */
    public void setEntries(Map entries) {
        this.entries.clear();
        for (Object o : entries.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            K key = (K) e.getKey();
            List list = (List) e.getValue();
            KeyEntry entry = this.entries.computeIfAbsent(key, k -> new KeyEntry());
            entry.lock.lock();
            try {
                entry.queue.clear();
                entry.queue.addAll(list);
            } finally {
                entry.lock.unlock();
            }
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * Get head of queue (non-blocking version for rdp/inp).
     * Does not require lock to be held.
     */
    private Object getHeadNonBlocking(K key, boolean remove) {
        KeyEntry entry = entries.get(key);
        if (entry == null)
            return null;

        Object result = null;
        entry.lock.lock();
        try {
            result = getHead(entry, key, remove);

            if (remove && entry.queue.isEmpty()) {
                entries.remove(key, entry);
                entry.isEmpty.signalAll();
            }
        } finally {
            entry.lock.unlock();
        }

        return result;
    }

    /**
     * Get head of queue.
     * MUST be called with entry.lock held!
     */
    private Object getHead(KeyEntry entry, K key, boolean remove) {
        Object result = null;
        boolean wasExpirable = false;

        // Consume expired entries at head
        while (result == null && !entry.queue.isEmpty()) {
            Object obj = entry.queue.getFirst();

            if (obj instanceof Expirable) {
                Object value = ((Expirable) obj).getValue();
                wasExpirable = true;

                if (value == null) {  // Expired
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

        // Update hasExpirable flag
        if (entry.queue.isEmpty()) {
            entry.hasExpirable = false;
            if (wasExpirable)
                unregisterExpirable(key);
        }

        return result;
    }

    /**
     * Get object matching template (non-blocking version for rdp/inp).
     */
    private Object getObjectNonBlocking(Template tmpl, boolean remove) {
        K key = (K) tmpl.getKey();
        KeyEntry entry = entries.get(key);
        if (entry == null)
            return null;

        Object result = null;
        entry.lock.lock();
        try {
            result = getObject(entry, key, tmpl, remove);

            if (remove && entry.queue.isEmpty()) {
                entries.remove(key, entry);
                entry.isEmpty.signalAll();
            }
        } finally {
            entry.lock.unlock();
        }

        return result;
    }

    /**
     * Get object matching template.
     * MUST be called with entry.lock held!
     */
    private Object getObject(KeyEntry entry, K key, Template tmpl, boolean remove) {
        Object result = null;
        Iterator<Object> iterator = entry.queue.iterator();
        boolean wasExpirable = false;

        while (iterator.hasNext()) {
            Object obj = iterator.next();

            if (obj instanceof Expirable) {
                Object value = ((Expirable) obj).getValue();

                if (value == null) {  // Expired
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

    private LocalSpace<K, SpaceListener<K,V>> getSL() {
        if (sl == null) {
            synchronized (this) {
                if (sl == null)
                    sl = new LSpace<>();
            }
        }
        return sl;
    }

    private void registerExpirable(K k, long t) {
        expirables[t > GCLONG ? 1 : 0].add(k);
    }

    private void unregisterExpirable(K k) {
        expirables[0].remove(k);
        expirables[1].remove(k);
    }

    /**
     * Expirable wrapper for values with timeout.
     */
    static class Expirable implements Comparable, Serializable {
        private static final long serialVersionUID = 0xA7F22BF5;

        Object value;

        /**
         * When to expire, in the future, as given by monotonic System.nanoTime().
         * IMPORTANT: always use a nanosec offset from System.nanoTime()!
         */
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
            return diff > 0 ? 1 :
                   diff < 0 ? -1 :
                   0;
        }
    }
}
