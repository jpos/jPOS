/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2015 Alejandro P. Revilla
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

package org.jpos.transaction;

import org.jdom.Element;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.space.*;
import org.jpos.util.*;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.jpos.iso.ISOUtil;

@SuppressWarnings("unchecked unused")
public class TransactionManager 
    extends QBeanSupport 
    implements Runnable, TransactionConstants, TransactionManagerMBean
{
    public static final String  HEAD       = "$HEAD";
    public static final String  TAIL       = "$TAIL";
    public static final String  CONTEXT    = "$CONTEXT.";
    public static final String  STATE      = "$STATE.";
    public static final String  GROUPS     = "$GROUPS.";
    public static final String  TAILLOCK   = "$TAILLOCK";
    public static final String  RETRY_QUEUE = "$RETRY_QUEUE";
    public static final Integer PREPARING  = 0;
    public static final Integer COMMITTING = 1;
    public static final Integer DONE       = 2;
    public static final String  DEFAULT_GROUP = "";
    public static final long    MAX_PARTICIPANTS = 1000;  // loop prevention
    public static final long    MAX_WAIT = 15000L;
    public static final long    TIMER_PURGE_INTERVAL = 1000L;
    protected Map<String,List<TransactionParticipant>> groups;
    private static final ThreadLocal<Serializable> tlContext = new ThreadLocal<Serializable>();
    private static final ThreadLocal<Long> tlId = new ThreadLocal<Long>();

    Space sp;
    Space psp;
    Space isp; // input space
    String queue;
    String tailLock;
    List<Thread> threads;
    final List<TransactionStatusListener> statusListeners = new ArrayList<TransactionStatusListener>();
    boolean hasStatusListeners;
    boolean debug;
    boolean profiler;
    boolean doRecover;
    boolean callSelectorOnAbort;
    int sessions;
    int maxSessions;
    int threshold;
    int maxActiveSessions;
    AtomicInteger activeSessions = new AtomicInteger();
    long head, tail;
    long retryInterval = 5000L;
    long retryTimeout  = 60000L;
    long pauseTimeout  = 0L;
    Runnable retryTask = null;
    TPS tps;
    final Timer timer = DefaultTimer.getTimer();

    @Override
    public void initService () throws ConfigurationException {
        queue = cfg.get ("queue", null);
        if (queue == null)
            throw new ConfigurationException ("queue property not specified");
        sp   = SpaceFactory.getSpace (cfg.get ("space"));
        isp  = SpaceFactory.getSpace (cfg.get ("input-space", cfg.get ("space")));
        psp  = SpaceFactory.getSpace (cfg.get ("persistent-space", this.toString()));
        tail = initCounter (TAIL, cfg.getLong ("initial-tail", 1));
        head = Math.max (initCounter (HEAD, tail), tail);
        initTailLock ();

        groups = new HashMap<String,List<TransactionParticipant>>();
        initParticipants (getPersist());
        initStatusListeners (getPersist());
    }

    @Override
    public void startService () throws Exception {
        NameRegistrar.register(getName(), this);
        recover ();
        threads = Collections.synchronizedList(new ArrayList(maxSessions));
        if (tps != null)
            tps.stop();
        tps = new TPS (cfg.getBoolean ("auto-update-tps", true));
        for (int i=0; i<sessions; i++) {
            new Thread(this).start();
        }
        if (psp.rdp (RETRY_QUEUE) != null)
            checkRetryTask();
    }

    @Override
    public void stopService () throws Exception {
        NameRegistrar.unregister (getName ());

        Thread[] tt = threads.toArray(new Thread[threads.size()]);
        for (int i=0; i < tt.length; i++) {
            isp.out(queue, Boolean.FALSE, 60 * 1000);
        }
        for (Thread thread : tt) {
            try {
                thread.join (60*1000);
                threads.remove(thread);
            } catch (InterruptedException e) {
                getLog().warn ("Session " + thread.getName() +" does not respond - attempting to interrupt");
                thread.interrupt();
            }
        }
        tps.stop();
    }
    public void queue (Serializable context) {
        isp.out(queue, context);
    }
    public void push (Serializable context) {
        isp.push (queue, context);
    }
    @SuppressWarnings("unused")
    public String getQueueName() {
        return queue;
    }
    public Space getSpace() {
        return sp;
    }
    public Space getInputSpace() {
        return isp;
    }
    public Space getPersistentSpace() {
        return psp;
    }

    @Override
    public void run () {
        long id = 0;
        int session = 0; // FIXME
        List<TransactionParticipant> members = null;
        Iterator<TransactionParticipant> iter = null;
        PausedTransaction pt;
        boolean abort = false;
        LogEvent evt = null;
        Profiler prof = null;
        long startTime = 0L;
        boolean paused;
        Thread thread = Thread.currentThread();
        if (threads.size() < maxSessions) {
            threads.add(thread);
            session = threads.indexOf(thread);
            activeSessions.incrementAndGet();
        } else {
            getLog().warn ("Max sessions reached, new session not created");
            return;
        }
        getLog().info ("start " + thread);
        while (running()) {
            Serializable context = null;
            prof = null;
            paused = false;
            thread.setName (getName() + "-" + session + ":idle");
            try {
                if (hasStatusListeners)
                    notifyStatusListeners (session, TransactionStatusEvent.State.READY, id, "", null);

                Object obj = isp.in (queue, MAX_WAIT);
                if (obj == Boolean.FALSE)
                    continue;   // stopService ``hack''

                if (obj == null) {
                    if (session > sessions && getActiveSessions() > sessions)
                        break; // we are an extra session, exit
                    else
                        continue;
                }
                if (session < sessions && // only initial sessions create extra sessions
                    maxSessions > sessions &&
                    getActiveSessions() < maxSessions &&
                    id % sessions == 0 &&
                    getOutstandingTransactions() > threshold)
                {
                        new Thread(this).start();
                }
                if (!(obj instanceof Serializable)) {
                    getLog().error (
                        "non serializable '" + obj.getClass().getName()
                       + "' on queue '" + queue + "'"
                    );
                    continue;
                }
                context = (Serializable) obj;
                if (obj instanceof Pausable) {
                    Pausable pausable = (Pausable) obj;
                    pt = pausable.getPausedTransaction();
                    if (pt != null) {
                        pt.cancelExpirationMonitor();
                        id      = pt.id();
                        members = pt.members();
                        iter    = pt.iterator();
                        abort   = pt.isAborting();
                        prof    = pt.getProfiler();
                        if (prof != null)
                            prof.reenable();
                    }
                } else 
                    pt = null;

                if (pt == null) {
                    int running = getRunningSessions();
                    if (maxActiveSessions > 0 && running >= maxActiveSessions) {
                        evt = getLog().createLogEvent ("warn",
                            Thread.currentThread().getName() 
                            + ": emergency retry, running-sessions=" + running 
                            + ", max-active-sessions=" + maxActiveSessions
                        );
                        evt.addMessage (obj);
                        psp.out (RETRY_QUEUE, obj, retryTimeout);
                        checkRetryTask();
                        continue;
                    }
                    abort = false;
                    id = nextId ();
                    members = new ArrayList ();
                    iter = getParticipants (DEFAULT_GROUP).iterator();
                }
                if (debug) {
                    evt = getLog().createLogEvent ("debug",
                        Thread.currentThread().getName() 
                        + ":" + Long.toString(id) +
                        (pt != null ? " [resuming]" : "")
                    );
                    if (prof == null)
                        prof = new Profiler();
                    startTime = System.currentTimeMillis();
                }
                snapshot (id, context, PREPARING);
                setThreadLocal(id, context);
                int action = prepare (session, id, context, members, iter, abort, evt, prof);
                removeThreadLocal();
                switch (action) {
                    case PAUSE:
                        paused = true;
                        if (id % TIMER_PURGE_INTERVAL == 0)
                            timer.purge();
                        break;
                    case PREPARED:
                        setState (id, COMMITTING);
                        setThreadLocal(id, context);
                        commit (session, id, context, members, false, evt, prof);
                        removeThreadLocal();
                        break;
                    case ABORTED:
                        setThreadLocal(id, context);
                        abort (session, id, context, members, false, evt, prof);
                        removeThreadLocal();
                        break;
                    case RETRY:
                        psp.out (RETRY_QUEUE, context);
                        checkRetryTask();
                        break;
                    case NO_JOIN:
                        break;
                }
                if ((action & PAUSE) == 0) {
                    snapshot (id, null, DONE);
                    if (id == tail) {
                        checkTail ();
                    }
                    tps.tick();
                }
            } catch (Throwable t) {
                if (evt == null)
                    getLog().fatal (t); // should never happen
                else
                    evt.addMessage (t);
            } finally {
                removeThreadLocal();
                if (hasStatusListeners) {
                    notifyStatusListeners (
                        session,
                        paused ? TransactionStatusEvent.State.PAUSED : TransactionStatusEvent.State.DONE, 
                        id, "", context);
                }

                if (evt != null) {
                    evt.addMessage (
                        String.format ("head=%d, tail=%d, outstanding=%d, active-sessions=%d/%d, %s, elapsed=%dms",
                            head, tail, getOutstandingTransactions(),
                            getActiveSessions(), maxSessions,
                            tps.toString(),
                                System.currentTimeMillis() - startTime
                        )
                    );
                    if (prof != null)
                        evt.addMessage (prof);
                    Logger.log (evt);
                    evt = null;
                }
            }
        }
        threads.remove(thread);
        int currentActiveSessions = activeSessions.decrementAndGet();
        getLog().info ("stop " + Thread.currentThread() + ", active sessions=" + currentActiveSessions);
    }

    @Override
    public long getTail () {
        return tail;
    }

    @Override
    public long getHead () {
        return head;
    }

    public long getInTransit () {
        return head - tail;
    }

    @Override
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException 
    {
        super.setConfiguration (cfg);
        debug = cfg.getBoolean ("debug");
        profiler = cfg.getBoolean ("profiler", debug); 
        if (profiler)
            debug = true; // profiler needs debug
        doRecover = cfg.getBoolean ("recover", true);
        retryInterval = cfg.getLong ("retry-interval", retryInterval);
        retryTimeout  = cfg.getLong ("retry-timeout", retryTimeout);
        pauseTimeout  = cfg.getLong ("pause-timeout", pauseTimeout);
        maxActiveSessions  = cfg.getInt  ("max-active-sessions", 0);
        sessions = cfg.getInt ("sessions", 1);
        threshold = cfg.getInt ("threshold", sessions / 2);
        maxSessions = cfg.getInt ("max-sessions", sessions);
        if (maxSessions < sessions)
            throw new ConfigurationException("max-sessions < sessions");
        if (maxActiveSessions > 0) {
            if (maxActiveSessions < sessions)
                throw new ConfigurationException("max-active-sessions < sessions");
            if (maxActiveSessions < maxSessions)
                throw new ConfigurationException("max-active-sessions < max-sessions");
        }
        callSelectorOnAbort = cfg.getBoolean("call-selector-on-abort", true);
    }
    public void addListener (TransactionStatusListener l) {
        synchronized (statusListeners) {
            statusListeners.add (l);
            hasStatusListeners = true;
        }
    }
    public void removeListener (TransactionStatusListener l) {
        synchronized (statusListeners) {
            statusListeners.remove (l);
            hasStatusListeners = !statusListeners.isEmpty();
        }
    }
    public TPS getTPS() {
        return tps;
    }

    @Override
    public String getTPSAsString() {
        return tps.toString();
    }

    @Override
    public float getTPSAvg() {
        return tps.getAvg();
    }

    @Override
    public int getTPSPeak() {
        return tps.getPeak();
    }

    @Override
    public Date getTPSPeakWhen() {
        return new Date(tps.getPeakWhen());
    }

    @Override
    public long getTPSElapsed() {
        return tps.getElapsed();
    }

    @Override
    public void resetTPS() {
        tps.reset();
    }

    protected void commit 
        (int session, long id, Serializable context, List<TransactionParticipant> members, boolean recover, LogEvent evt, Profiler prof)
    {
        for (TransactionParticipant p :members) {
            if (recover && p instanceof ContextRecovery) {
                context = ((ContextRecovery) p).recover (id, context, true);
                if (evt != null)
                    evt.addMessage (" commit-recover: " + p.getClass().getName());
            }
            if (hasStatusListeners)
                notifyStatusListeners (
                    session, TransactionStatusEvent.State.COMMITING, id, p.getClass().getName(), context
                );
            commit (p, id, context);
            if (evt != null) {
                evt.addMessage ("         commit: " + p.getClass().getName());
                if (prof != null)
                    prof.checkPoint (" commit: " + p.getClass().getName());
            }
        }
    }
    protected void abort 
        (int session, long id, Serializable context, List<TransactionParticipant> members, boolean recover, LogEvent evt, Profiler prof)
    {
        for (TransactionParticipant p :members) {
            if (recover && p instanceof ContextRecovery) {
                context = ((ContextRecovery) p).recover (id, context, false);
                if (evt != null)
                    evt.addMessage ("  abort-recover: " + p.getClass().getName());
            }
            if (hasStatusListeners)
                notifyStatusListeners (
                    session, TransactionStatusEvent.State.ABORTING, id, p.getClass().getName(), context
                );

            abort (p, id, context);
            if (evt != null) {
                evt.addMessage ("          abort: " + p.getClass().getName());
                if (prof != null)
                    prof.checkPoint ("  abort: " + p.getClass().getName());
            }
        }
    }
    protected int prepareForAbort
        (TransactionParticipant p, long id, Serializable context) 
    {
        try {
            if (p instanceof AbortParticipant) {
                setThreadName(id, "prepareForAbort", p);
                return ((AbortParticipant)p).prepareForAbort (id, context);
            }
        } catch (Throwable t) {
            getLog().warn ("PREPARE-FOR-ABORT: " + Long.toString (id), t);
        }
        return ABORTED | NO_JOIN;
    }
    protected int prepare 
        (TransactionParticipant p, long id, Serializable context) 
    {
        try {
            setThreadName(id, "prepare", p);
            return p.prepare (id, context);
        } catch (Throwable t) {
            getLog().warn ("PREPARE: " + Long.toString (id), t);
        }
        return ABORTED;
    }
    protected void commit 
        (TransactionParticipant p, long id, Serializable context) 
    {
        try {
            setThreadName(id, "commit", p);
            p.commit (id, context);
        } catch (Throwable t) {
            getLog().warn ("COMMIT: " + Long.toString (id), t);
        }
    }
    protected void abort 
        (TransactionParticipant p, long id, Serializable context) 
    {
        try {
            setThreadName(id, "abort", p);
            p.abort (id, context);
        } catch (Throwable t) {
            getLog().warn ("ABORT: " + Long.toString (id), t);
        }
    }
    protected int prepare
        (int session, long id, Serializable context, List<TransactionParticipant> members, Iterator<TransactionParticipant> iter, boolean abort, LogEvent evt, Profiler prof)
    {
        boolean retry = false;
        boolean pause = false;
        for (int i=0; iter.hasNext (); i++) {
            int action;
            if (i > MAX_PARTICIPANTS) {
                getLog().warn (
                    "loop detected - transaction " +id + " aborted."
                );
                return ABORTED;
            }
            TransactionParticipant p = iter.next();
            if (abort) {
                if (hasStatusListeners)
                    notifyStatusListeners (
                        session, TransactionStatusEvent.State.PREPARING_FOR_ABORT, id, p.getClass().getName(), context
                    );
                action = prepareForAbort (p, id, context);
                if (evt != null && p instanceof AbortParticipant)
                    evt.addMessage ("prepareForAbort: " + p.getClass().getName());
            } else {
                if (hasStatusListeners)
                    notifyStatusListeners (
                        session, TransactionStatusEvent.State.PREPARING, id, p.getClass().getName(), context
                    );
                action = prepare (p, id, context);
                abort  = (action & PREPARED) == ABORTED;
                retry  = (action & RETRY) == RETRY;
                pause  = (action & PAUSE) == PAUSE;
                if (evt != null) {
                    evt.addMessage ("        prepare: "
                            + p.getClass().getName() 
                            + (abort ? " ABORTED" : "")
                            + (retry ? " RETRY" : "")
                            + (pause ? " PAUSE" : "")
                            + ((action & READONLY) == READONLY ? " READONLY" : "")
                            + ((action & NO_JOIN) == NO_JOIN ? " NO_JOIN" : ""));
                    if (prof != null)
                        prof.checkPoint ("prepare: " + p.getClass().getName());
                }
            }
            if ((action & READONLY) == 0) {
                snapshot (id, context);
            }
            if ((action & NO_JOIN) == 0) {
                members.add (p);
            }
            if (p instanceof GroupSelector && ((action & PREPARED) == PREPARED || callSelectorOnAbort)) {
                String groupName = null;
                try {
                    groupName = ((GroupSelector)p).select (id, context);
                } catch (Exception e) {
                    if (evt != null) 
                        evt.addMessage ("       selector: " + p.getClass().getName() + " " + e.getMessage());
                    else 
                        getLog().error ("       selector: " + p.getClass().getName() + " " + e.getMessage());
                }
                if (evt != null) {
                    evt.addMessage ("       selector: " + groupName);
                }
                if (groupName != null) {
                    StringTokenizer st = new StringTokenizer (groupName, " ,");
                    List participants = new ArrayList();
                    while (st.hasMoreTokens ()) {
                        String grp = st.nextToken();
                        addGroup (id, grp);
                        participants.addAll (getParticipants (grp));
                    }
                    while (iter.hasNext())
                        participants.add (iter.next());

                    iter = participants.iterator();
                    continue;
                }
            }
            if (pause) {
                if (context instanceof Pausable) {
                    Pausable pausable = (Pausable) context;
                    long t = pausable.getTimeout();
                    if (t == 0) 
                        t = pauseTimeout;
                    TimerTask expirationMonitor = null;
                    if (t > 0)
                        expirationMonitor = new PausedMonitor (pausable);
                    PausedTransaction pt = new PausedTransaction (
                        this, id, members, iter, abort, expirationMonitor, prof
                    );
                    pausable.setPausedTransaction (pt);
                    if (expirationMonitor != null) {
                        synchronized (context) {
                            if (!pt.isResumed()) {
                                timer.schedule (
                                    expirationMonitor, t
                                );
                            }
                        }
                    }
                } else {
                    throw new RuntimeException ("Unable to PAUSE transaction - Context is not Pausable");
                }
                return PAUSE;
            }
        }
        return members.isEmpty() ? NO_JOIN :
                abort ? retry ? RETRY : ABORTED : PREPARED;
    }
    protected List<TransactionParticipant> getParticipants (String groupName) {
        List<TransactionParticipant> participants = groups.get (groupName);
        if (participants == null)
            participants = new ArrayList();
        return participants;
    }
    protected List<TransactionParticipant> getParticipants (long id) {
    	// Use a local copy of participant to avoid adding the 
        // GROUP participant to the DEFAULT_GROUP
    	List<TransactionParticipant> participantsChain = new ArrayList();
        List<TransactionParticipant> participants = getParticipants (DEFAULT_GROUP);
        // Add DEFAULT_GROUP participants 
        participantsChain.addAll(participants);
        String key = getKey(GROUPS, id);
        String grp;
        // now add participants of Group 
        while ( (grp = (String) psp.inp (key)) != null) {
            participantsChain.addAll (getParticipants (grp));
        }
        return participantsChain;
    }

    protected void initStatusListeners (Element config)  throws ConfigurationException{
        final Iterator iter = config.getChildren ("status-listener").iterator();
        while (iter.hasNext()) {
            final Element e = (Element) iter.next();
            final QFactory factory = getFactory();
            final TransactionStatusListener listener = (TransactionStatusListener) factory.newInstance (e.getAttributeValue ("class"));
            factory.setConfiguration (listener, config);
            addListener(listener);
        }
    }

    protected void initParticipants (Element config) 
        throws ConfigurationException
    {
        groups.put (DEFAULT_GROUP,  initGroup (config));
        for (Element e :(List<Element>)config.getChildren("group")) {
            String name = e.getAttributeValue ("name");
            if (name == null) 
                throw new ConfigurationException ("missing group name");
            if (groups.containsKey(name)) {
                throw new ConfigurationException (
                    "Group '" + name + "' already defined"
                );
            }
            groups.put (name, initGroup (e));
        }
    }
    protected List<TransactionParticipant> initGroup (Element e) 
        throws ConfigurationException
    {
        List group = new ArrayList ();
        for (Element el :(List<Element>)e.getChildren ("participant")) {
            group.add(createParticipant(el));
        }
        return group;
    }
    public TransactionParticipant createParticipant (Element e) 
        throws ConfigurationException
    {
        QFactory factory = getFactory();
        TransactionParticipant participant = (TransactionParticipant) 
            factory.newInstance (e.getAttributeValue ("class")
        );
        factory.setLogger (participant, e);
        QFactory.invoke (participant, "setTransactionManager", this, TransactionManager.class);
        factory.setConfiguration (participant, e);
        return participant;
    }

    @Override
    public int getOutstandingTransactions() {
        if (isp instanceof LocalSpace)
            return ((LocalSpace)isp).size(queue);
        return -1;
    }
    protected String getKey (String prefix, long id) {
        StringBuilder sb = new StringBuilder (getName());
        sb.append ('.');
        sb.append (prefix);
        sb.append (Long.toString (id));
        return sb.toString ();
    }
    protected long initCounter (String name, long defValue) {
        Long L = (Long) psp.rdp (name);
        if (L == null) {
            L = defValue;
            psp.out (name, L);
        }
        return L;
    }
    protected void commitOff (Space sp) {
        if (sp instanceof JDBMSpace) {
            ((JDBMSpace) sp).setAutoCommit (false);
        }
    }
    protected void commitOn (Space sp) {
        if (sp instanceof JDBMSpace) {
            JDBMSpace jsp = (JDBMSpace) sp;
            jsp.commit ();
            jsp.setAutoCommit (true);
        }
    }
    protected void syncTail () {
        synchronized (psp) {
            commitOff (psp);
            psp.inp (TAIL);
            psp.out (TAIL, tail);
            commitOn (psp);
        }
    }
    protected void initTailLock () {
        tailLock = TAILLOCK + "." + Integer.toString (this.hashCode());
        sp.put (tailLock, TAILLOCK);
    }
    protected void checkTail () {
        Object lock = sp.in (tailLock);
        while (tailDone()) {
            // if (debug) {
            //    getLog().debug ("tailDone " + tail);
            // }
            tail++;
        }
        syncTail ();
        sp.out (tailLock, lock);
    }
    protected boolean tailDone () {
        String stateKey = getKey (STATE, tail);
        if (DONE.equals (psp.rdp (stateKey))) {
            purge (tail);
            return true;
        }
        return false;
    }
    protected long nextId () {
        long h;
        synchronized (psp) {
            commitOff (psp);
            psp.in  (HEAD);
            h = head;
            psp.out (HEAD, ++head);
            commitOn (psp);
        }
        return h;
    }
    protected void snapshot (long id, Serializable context) {
        snapshot (id, context, null);
    }
    protected void snapshot (long id, Serializable context, Integer status) {
        String contextKey = getKey (CONTEXT, id);
        synchronized (psp) {
            commitOff (psp);
            SpaceUtil.wipe(psp, contextKey);
            if (context != null)
                psp.out (contextKey, context);

            if (status != null) {
                String stateKey  = getKey (STATE, id);
                psp.put (stateKey, status);
            }
            commitOn (psp);
        }
    }
    protected void setState (long id, Integer state) {
        String stateKey  = getKey (STATE, id);
        synchronized (psp) {
            commitOff (psp);
            SpaceUtil.wipe(psp, stateKey);
            if (state!= null)
                psp.out (stateKey, state);
            commitOn (psp);
        }
    }
    protected void addGroup (long id, String groupName) {
        if (groupName != null)
            psp.out (getKey (GROUPS, id), groupName);
    }
    protected void purge (long id) {
        String stateKey   = getKey (STATE, id);
        String contextKey = getKey (CONTEXT, id);
        String groupsKey  = getKey (GROUPS, id);
        synchronized (psp) {
            commitOff (psp);
            SpaceUtil.wipe(psp, stateKey);
            SpaceUtil.wipe(psp, contextKey);
            SpaceUtil.wipe(psp, groupsKey);
            commitOn (psp);
        }
    }
    protected void recover () {
        if (doRecover) {
            if (tail < head) {
                getLog().info ("recover - tail=" +tail+", head="+head);
            }
            while (tail < head) {
                recover (0, tail++);
            }
        } else
            tail = head;
        syncTail ();
    }
    protected void recover (int session, long id) {
        LogEvent evt = getLog().createLogEvent ("recover");
        Profiler prof = new Profiler();
        evt.addMessage ("<id>" + id + "</id>");
        try {
            String stateKey   = getKey (STATE, id);
            String contextKey = getKey (CONTEXT, id);
            Integer state = (Integer) psp.rdp (stateKey);
            if (state == null) {
                evt.addMessage ("unknown stateKey " + stateKey);
                SpaceUtil.wipe (psp, contextKey);   // just in case ...
                return;
            }
            Serializable context = (Serializable) psp.rdp (contextKey);
            if (context != null)
                evt.addMessage (context);

            if (DONE.equals (state)) {
                evt.addMessage ("<done/>");
            } else if (COMMITTING.equals (state)) {
                commit (session, id, context, getParticipants (id), true, evt, prof);
            } else if (PREPARING.equals (state)) {
                abort (session, id, context, getParticipants (id), true, evt, prof);
            }
            purge (id);
        } finally {
            evt.addMessage (prof);
            Logger.log (evt);
        }
    }
    protected synchronized void checkRetryTask () {
        if (retryTask == null) {
            retryTask = new RetryTask();
            new Thread(retryTask).start();
        }
    }

    public static class PausedMonitor extends TimerTask {
        Pausable context;
        public PausedMonitor (Pausable context) {
            super();
            this.context = context;
        }
        @Override
        public void run() {
            cancel();
            context.getPausedTransaction().forceAbort();
            context.resume();
        }
    }

    public class RetryTask implements Runnable {
        @Override
        public void run() {
            Thread.currentThread().setName (getName()+"-retry-task");
            while (running()) {
                for (Serializable context; (context = (Serializable)psp.rdp (RETRY_QUEUE)) != null;) 
                {
                    isp.out (queue, context, retryTimeout);
                    psp.inp (RETRY_QUEUE);
                }
                ISOUtil.sleep(retryInterval);
            }
        }
    }

    @Override
    public void setDebug (boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean getDebug() {
        return debug;
    }

    @Override
    public int getActiveSessions() {
        return activeSessions.intValue();
    }
    public int getRunningSessions() {
        return (int) (head - tail);
    }

    public static Serializable getSerializable() {
        return tlContext.get();
    }
    public static Context getContext() {
        return (Context) tlContext.get();
    }
    public static Long getId() {
        return tlId.get();
    }
    private void notifyStatusListeners
            (int session, TransactionStatusEvent.State state, long id, String info, Serializable context)
    {
        TransactionStatusEvent e = new TransactionStatusEvent(session, state, id, info, context);
        synchronized (statusListeners) {
            for (TransactionStatusListener l : statusListeners) {
                l.update (e);
            }
        }
    }
    private void setThreadName (long id, String method, TransactionParticipant p) {
        Thread.currentThread().setName(
            String.format("%s:%d %s %s", getName(), id, method, p.getClass().getName())
        );
    }
    private void setThreadLocal (long id, Serializable context) {
        tlId.set(id);
        tlContext.set(context);
    }
    private void removeThreadLocal() {
        tlId.remove();
        tlContext.remove();
    }
}
