package org.jpos.annotation.resolvers.parameters;

import static org.jpos.transaction.ContextConstants.RESULT;

import java.util.Map;
import java.util.Set;

import org.jpos.rc.Result;
import org.jpos.transaction.Context;
import org.jpos.transaction.PausedTransaction;
import org.jpos.util.LogEvent;
import org.jpos.util.Profiler;

class ContextView extends Context {
    private final Context ctx;
    private final Set<String> readWrite;
    private final Set<String> readOnly;
    private final Set<String> writeOnly;
    
    enum ACCESS {
        READ_ONLY(true, false), WRITE_ONLY(false, true), READ_WRITE(true, true);

        final boolean canRead;
        final boolean canWrite;
        
        ACCESS(boolean canRead, boolean canWrite) {
            this.canRead = canRead;
            this.canWrite = canWrite;
        }
        
        boolean hasAccess(ContextView ctx, Object key) {
            boolean hasAccess = false;
            if (canRead) {
                hasAccess |= ctx.readWrite.contains(key) || ctx.readOnly.contains(key);
            }
            if (canWrite) {
                hasAccess = ctx.readWrite.contains(key) || ctx.writeOnly.contains(key);
            }
            return hasAccess;
        }
    }
        
    public ContextView(Context ctx, Set<String> readWrite, Set<String> readOnly, Set<String> writeOnly) {
        this.ctx = ctx;
        this.readWrite = readWrite;
        this.readOnly = readOnly;
        this.writeOnly = writeOnly;
    }
    
    void validateKey(Object key, ACCESS access) {
        if (!access.hasAccess(this, key)) {
            throw new IllegalArgumentException(String.format("Can not access key %s, allowed readKeys are %s and allowed write keys are %s", key, readOnly, writeOnly));
        }
    }
    
    /**
     * puts an Object in the transient Map
     */
    @Override
    public void put (Object key, Object value) {
        validateKey(key, ACCESS.WRITE_ONLY);
        ctx.put(key, value);
    }
    /**
     * puts an Object in the transient Map
     */
    @Override
    public void put (Object key, Object value, boolean persist) {
        validateKey(key, ACCESS.WRITE_ONLY);
        ctx.put(key, value, persist);
    }

    /**
     * Persists a transient entry
     * @param key the key
     */
    @Override
    public void persist (Object key) {
        ctx.persist(key);
    }

    /**
     * Evicts a persistent entry
     * @param key the key
     */
    @Override
    public void evict (Object key) {
        ctx.evict(key);
    }

    /**
     * Get object instance from transaction context.
     *
     * @param <T> desired type of object instance
     * @param key the key of object instance
     * @return object instance if exist in context or {@code null} otherwise
     */
    @Override
    public <T> T get(Object key) {
        validateKey(key, ACCESS.READ_ONLY);
        return ctx.get(key);
    }

    /**
     * Check if key present
     * @param key the key
     * @return true if present
     */
    @Override
    public boolean hasKey(Object key) {
        validateKey(key, ACCESS.READ_WRITE);
        return ctx.hasKey(key);
    }

    /**
     * Check key exists present persisted map
     * @param key the key
     * @return true if present
     */
    @Override
    public boolean hasPersistedKey(Object key) {
        validateKey(key, ACCESS.READ_WRITE);
        return ctx.hasPersistedKey(key);
    }

    /**
     * Move entry to new key name
     * @param from key
     * @param to key
     * @return the entry's value (could be null if 'from' key not present)
     */
    @Override
    public synchronized <T> T move(Object from, Object to) {
        validateKey(from, ACCESS.WRITE_ONLY);
        validateKey(to, ACCESS.WRITE_ONLY);
        return ctx.move(from, to);
    }

    /**
     * Get object instance from transaction context.
     *
     * @param <T> desired type of object instance
     * @param key the key of object instance
     * @param defValue default value returned if there is no value in context
     * @return object instance if exist in context or {@code defValue} otherwise
     */
    @Override
    public <T> T get(Object key, T defValue) {
        validateKey(key, ACCESS.READ_ONLY);
        return ctx.get(key, defValue);
    }

    /**
     * Transient remove
     */
    @Override
    public synchronized <T> T remove(Object key) {
        validateKey(key, ACCESS.WRITE_ONLY);
        return ctx.remove(key);
    }

    @Override
    public String getString (Object key) {
        validateKey(key, ACCESS.READ_ONLY);
        return ctx.getString(key);
    }
    
    @Override
    public String getString (Object key, String defValue) {
        validateKey(key, ACCESS.READ_ONLY);
        return ctx.getString(key, defValue);
    }
    
    /**
     * persistent get with timeout
     * @param key the key
     * @param timeout timeout
     * @return object (null on timeout)
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> T get (Object key, long timeout) {
        validateKey(key, ACCESS.READ_ONLY);
        return ctx.get(key, timeout);
    }
    
    @Override
    public Context clone() {
        return new ContextView(ctx.clone(), readWrite, readOnly, writeOnly);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ContextView) {
            return ctx.equals(((ContextView)o).ctx);
        } else {
            return ctx.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return ctx.hashCode();
    }

    /**
     * @return transient map
     */
    @Override
    public synchronized Map<Object,Object> getMap() {
        throw new UnsupportedOperationException("getMap is not suported on a view");
    }

    /**
     * return a LogEvent used to store trace information
     * about this transaction.
     * If there's no LogEvent there, it creates one.
     * @return LogEvent
     */
    @Override
    public synchronized LogEvent getLogEvent () {
        return ctx.getLogEvent();
    }
    
    /**
     * return (or creates) a Profiler object
     * @return Profiler object
     */
    @Override
    public synchronized Profiler getProfiler () {
        return ctx.getProfiler();
    }

    /**
     * return (or creates) a Resultr object
     * @return Profiler object
     */
    @Override
    public synchronized Result getResult () {
        validateKey(RESULT.toString(), ACCESS.READ_WRITE);
        return ctx.getResult();
    }

    /**
     * adds a trace message
     * @param msg trace information
     */
    @Override
    public void log (Object msg) {
        ctx.log(msg);
    }
    
    /**
     * add a checkpoint to the profiler
     */
    @Override
    public void checkPoint (String detail) {
        ctx.checkPoint (detail);
    }
    
    @Override
    public void setPausedTransaction (PausedTransaction p) {
        ctx.setPausedTransaction(p);
    }
    
    @Override
    public PausedTransaction getPausedTransaction() {
        return ctx.getPausedTransaction();
    }
    
    @Override
    public PausedTransaction getPausedTransaction(long timeout) {
        return ctx.getPausedTransaction(timeout);
    }
    
    @Override
    public void setTimeout (long timeout) {
        ctx.setTimeout(timeout);
    }
    
    @Override
    public long getTimeout () {
        return ctx.getTimeout();
    }
    
    @Override
    public synchronized void resume() {
        ctx.resume();
    }
    
    @Override
    public boolean isTrace() {
        return ctx.isTrace();
    }
    
    @Override
    public void setTrace(boolean trace) {
        ctx.setTrace(trace);
    }
}
