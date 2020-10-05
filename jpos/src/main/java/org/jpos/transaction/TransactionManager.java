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

package org.jpos.transaction;

import org.HdrHistogram.AtomicHistogram;
import org.jdom2.Element;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.space.*;
import org.jpos.util.*;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.jpos.iso.ISOUtil;

@SuppressWarnings("unchecked")
public class TransactionManager 
    extends QBeanSupport 
    implements Runnable, TransactionConstants, TransactionManagerMBean, Loggeable, MetricsProvider
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
    private Set<Destroyable> destroyables = new HashSet<>();
    private static final ThreadLocal<Serializable> tlContext = new ThreadLocal<>();
    private static final ThreadLocal<Long> tlId = new ThreadLocal<>();
    private Metrics metrics;
    private static ScheduledThreadPoolExecutor loadMonitorExecutor;
    private static Map<TransactionParticipant,String> names = new HashMap<>();


    Space sp;
    Space psp;
    Space isp;  // real input space
    Space iisp; // internal input space
    String queue;
    String tailLock;
    List<Thread> threads;
    final List<TransactionStatusListener> statusListeners = new ArrayList<>();
    boolean hasStatusListeners;
    boolean debug;
    boolean debugContext;
    boolean profiler;
    boolean doRecover;
    boolean callSelectorOnAbort;
    int sessions;
    int maxSessions;
    int threshold;
    int maxActiveSessions;
    private AtomicInteger activeSessions = new AtomicInteger();
    private AtomicInteger pausedCounter = new AtomicInteger();
    private AtomicInteger activeTransactions = new AtomicInteger(0);

    volatile long head, tail;
    long retryInterval = 5000L;
    long retryTimeout  = 60000L;
    long pauseTimeout  = 0L;
    boolean abortOnPauseTimeout = true;
    Runnable retryTask = null;
    TPS tps;
    final Timer timer = DefaultTimer.getTimer();

    @Override
    public void initService () throws ConfigurationException {
        queue = cfg.get ("queue", null);
        if (queue == null)
            throw new ConfigurationException ("queue property not specified");
        sp  = SpaceFactory.getSpace (cfg.get ("space"));
        isp = SpaceFactory.getSpace (cfg.get ("input-space", cfg.get ("space")));
        if (isp == sp)
            iisp = isp;
        else {
            iisp = sp;
        }
        psp  = SpaceFactory.getSpace (cfg.get ("persistent-space", this.toString()));
        tail = initCounter (TAIL, cfg.getLong ("initial-tail", 1));
        head = Math.max (initCounter (HEAD, tail), tail);
        initTailLock ();

        groups = new HashMap<>();
        initParticipants (getPersist());
        initStatusListeners (getPersist());
    }

    @Override
    public void startService () throws Exception {
        NameRegistrar.register(getName(), this);
        recover();
        threads = Collections.synchronizedList(new ArrayList(maxSessions));
        if (tps != null)
            tps.stop();
        tps = new TPS (cfg.getBoolean ("auto-update-tps", true));
        for (int i=0; i<sessions; i++) {
            new Thread(this).start();
        }
        if (psp.rdp (RETRY_QUEUE) != null)
            checkRetryTask();

        if (maxSessions > sessions) {
            loadMonitorExecutor = ConcurrentUtil.newScheduledThreadPoolExecutor();
            loadMonitorExecutor.scheduleAtFixedRate(
              new Thread(() -> {
                  int outstandingTransactions = getOutstandingTransactions();
                  int activeSessions = getActiveSessions();
                  if (activeSessions < maxSessions && outstandingTransactions > threshold) {
                      int count = Math.min(outstandingTransactions, maxSessions - activeSessions);
                      for (int i=0; i<count; i++)
                          new Thread(this).start();
                      getLog().info("Created " + count + " additional sessions");
                  }
              }), 5, 1, TimeUnit.SECONDS)
            ;
        }
        if (iisp != isp) {
            new Thread(new InputQueueMonitor()).start();
        }
    }

    @Override
    public void stopService () {
        NameRegistrar.unregister(getName());
        if (loadMonitorExecutor != null)
            loadMonitorExecutor.shutdown();
        Thread[] tt = threads.toArray(new Thread[threads.size()]);
        if (iisp != isp)
            for (Object o=iisp.inp(queue); o != null; o=iisp.inp(queue))
                isp.out(queue, o); // push back to replicated space
        for (Thread t : tt)
            iisp.out(queue, Boolean.FALSE, 60 * 1000);
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
        for (Destroyable destroyable : destroyables) {
            try {
                destroyable.destroy();
            } catch (Throwable t) {
                getLog().warn (t);
            }
        }
    }
    public void queue (Serializable context) {
        iisp.out(queue, context);
    }
    public void push (Serializable context) {
        iisp.push(queue, context);
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
        Profiler prof;
        boolean paused;
        boolean transactionActive;
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
            evt = null;
            paused = false;
            transactionActive = false;
            thread.setName (getName() + "-" + session + ":idle");
            int action = -1;
            try {
                if (hasStatusListeners)
                    notifyStatusListeners (session, TransactionStatusEvent.State.READY, id, "", null);

                Object obj = iisp.in (queue, MAX_WAIT);
                if (obj == Boolean.FALSE)
                    continue;   // stopService ``hack''

                if (obj == null) {
                    if (session+1 > sessions && getActiveSessions() > sessions)
                        break; // we are an extra session, exit
                    else {
                        continue;
                    }
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
                        evt     = pt.getLogEvent();
                        prof    = pt.getProfiler();
                        if (metrics != null && prof != null)
                            metrics.record(getName(pt.getParticipant()) + "-resume", prof.getPartialInMillis());
                        if (prof != null)
                            prof.reenable();
                        pausedCounter.decrementAndGet();
                    }
                } else 
                    pt = null;

                if (pt == null) {
                    int running = getActiveTransactions();
                    if (maxActiveSessions > 0 && running >= maxActiveSessions) {
                        getLog().warn (
                            Thread.currentThread().getName() 
                            + ": emergency retry, running-sessions=" + running 
                            + ", max-active-sessions=" + maxActiveSessions
                        );
                        psp.out (RETRY_QUEUE, obj, retryTimeout);
                        checkRetryTask();
                        continue;
                    }
                    abort = false;
                    id = nextId ();
                    members = new ArrayList ();
                    iter = getParticipants (DEFAULT_GROUP).iterator();
                    activeTransactions.incrementAndGet();
                }
                transactionActive = true;
                if (debug) {
                    if (evt == null) {
                        evt = getLog().createLogEvent("debug",
                          Thread.currentThread().getName()
                            + ":" + Long.toString(id) +
                            (pt != null ? " [resuming]" : "")
                        );
                        if (debugContext) {
                            evt.addMessage (context);
                        }
                    }
                    if (prof == null)
                        prof = new Profiler();
                    else
                        prof.checkPoint("resume");
                }
                snapshot (id, context, PREPARING);
                setThreadLocal(id, context);
                action = prepare (session, id, context, members, iter, abort, evt, prof);
                removeThreadLocal();
                switch (action) {
                    case PAUSE:
                        paused = true;
                        if (id % TIMER_PURGE_INTERVAL == 0)
                            timer.purge();
                        pausedCounter.incrementAndGet();
                        break;
                    case PREPARED:
                        if (members.size() > 0) {
                            setState(id, COMMITTING);
                            setThreadLocal(id, context);
                            commit(session, id, context, members, false, evt, prof);
                            removeThreadLocal();
                        }
                        break;
                    case ABORTED:
                        if (members.size() > 0) {
                            setThreadLocal(id, context);
                            abort(session, id, context, members, false, evt, prof);
                            removeThreadLocal();
                        }
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
                    } else {
                        purge (id, false);
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
                if (transactionActive && !paused)
                    activeTransactions.decrementAndGet();
                if (hasStatusListeners) {
                    notifyStatusListeners (
                        session,
                        paused ? TransactionStatusEvent.State.PAUSED : TransactionStatusEvent.State.DONE, 
                        id, "", context);
                }
                if (evt != null && (action == PREPARED || action == ABORTED || (action == -1 && prof != null))) {
                    switch (action) {
                        case PREPARED :
                            evt.setTag("commit");
                            break;
                        case ABORTED :
                            evt.setTag ("abort");
                            break;
                        case -1:
                            evt.setTag ("undefined");
                            break;
                    }
                    if (getInTransit() > Math.max(maxActiveSessions, activeSessions.get()) * 100) {
                        evt.addMessage("WARNING: IN-TRANSIT TOO HIGH");
                    }
                    evt.addMessage (
                        String.format (" %s, elapsed=%dms",
                            tmInfo(),
                            prof != null ? prof.getElapsedInMillis() : -1
                        )
                    );
                    if (prof != null)
                        evt.addMessage (prof);
                    try {
                        Logger.log(freeze(context, evt, prof));
                    } catch (Throwable t) {
                        getLog().error(t);
                    }
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
        return activeTransactions.get();
    }

    @Override
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException 
    {
        super.setConfiguration (cfg);
        debug = cfg.getBoolean ("debug", true);
        debugContext = cfg.getBoolean ("debug-context", debug);
        profiler = cfg.getBoolean ("profiler", debug); 
        if (profiler || debugContext)
            debug = true; // profiler and/or debugContext needs debug
        doRecover = cfg.getBoolean ("recover", true);
        retryInterval = cfg.getLong ("retry-interval", retryInterval);
        retryTimeout  = cfg.getLong ("retry-timeout", retryTimeout);
        pauseTimeout  = cfg.getLong ("pause-timeout", pauseTimeout);
        abortOnPauseTimeout = cfg.getBoolean("abort-on-pause-timeout", true);
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
        if (profiler)
            metrics = new Metrics(new AtomicHistogram(cfg.getLong("metrics-highest-trackable-value", 60000), 2));
    }
    public void addListener (TransactionStatusListener l) {
        synchronized (statusListeners) {
            statusListeners.add (l);
            hasStatusListeners = true;
        }
    }
    public void removeListener (TransactionStatusListener l) {
        synchronized (statusListeners) {
            statusListeners.remove(l);
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

    @Override
    public Metrics getMetrics() {
        return metrics;
    }

    @Override
    public void dump (PrintStream ps, String indent) {
        ps.printf ("%s%s%n", indent, tmInfo());
        if (metrics != null) {
            metrics.dump(ps, indent);
        }
    }

    protected void commit
        (int session, long id, Serializable context, List<TransactionParticipant> members, boolean recover, LogEvent evt, Profiler prof)
    {
        for (TransactionParticipant p :members) {
            if (recover && p instanceof ContextRecovery) {
                context = ((ContextRecovery) p).recover (id, context, true);
                if (evt != null)
                    evt.addMessage (" commit-recover: " + getName(p));
            }
            if (hasStatusListeners)
                notifyStatusListeners (
                    session, TransactionStatusEvent.State.COMMITING, id, getName(p), context
                );
            commit (p, id, context);
            if (evt != null) {
                evt.addMessage ("         commit: " + getName(p));
                if (prof != null)
                    prof.checkPoint (" commit: " + getName(p));
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
                    evt.addMessage ("  abort-recover: " + getName(p));
            }
            if (hasStatusListeners)
                notifyStatusListeners (
                    session, TransactionStatusEvent.State.ABORTING, id, getName(p), context
                );

            abort(p, id, context);
            if (evt != null) {
                evt.addMessage ("          abort: " + getName(p));
                if (prof != null)
                    prof.checkPoint ("  abort: " + getName(p));
            }
        }
    }
    protected int prepareForAbort
        (TransactionParticipant p, long id, Serializable context) 
    {
        Chronometer c = new Chronometer();
        try {
            if (p instanceof AbortParticipant) {
                setThreadName(id, "prepareForAbort", p);
                return ((AbortParticipant)p).prepareForAbort (id, context);
            }
        } catch (Throwable t) {
            getLog().warn ("PREPARE-FOR-ABORT: " + Long.toString (id), t);
        } finally {
            if (metrics != null)
                metrics.record(getName(p) + "-prepare-for-abort", c.elapsed());
        }
        return ABORTED | NO_JOIN;
    }
    protected int prepare 
        (TransactionParticipant p, long id, Serializable context) 
    {
        Chronometer c = new Chronometer();
        try {
            setThreadName(id, "prepare", p);
            return p.prepare (id, context);
        } catch (Throwable t) {
            getLog().warn ("PREPARE: " + Long.toString (id), t);
        } finally {
            if (metrics != null)
                metrics.record(getName(p) + "-prepare", c.elapsed());
        }
        return ABORTED;
    }
    protected void commit 
        (TransactionParticipant p, long id, Serializable context) 
    {
        Chronometer c = new Chronometer();
        try {
            setThreadName(id, "commit", p);
            p.commit(id, context);
        } catch (Throwable t) {
            getLog().warn ("COMMIT: " + Long.toString (id), t);
        }
        if (metrics != null)
            metrics.record(getName(p) + "-commit", c.elapsed());
    }
    protected void abort 
        (TransactionParticipant p, long id, Serializable context) 
    {
        Chronometer c = new Chronometer();
        try {
            setThreadName(id, "abort", p);
            p.abort(id, context);
        } catch (Throwable t) {
            getLog().warn ("ABORT: " + Long.toString (id), t);
        }
        if (metrics != null)
            metrics.record(getName(p) + "-abort", c.elapsed());
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
                        session, TransactionStatusEvent.State.PREPARING_FOR_ABORT, id, getName(p), context
                    );
                action = prepareForAbort (p, id, context);

                if (evt != null && p instanceof AbortParticipant) {
                    evt.addMessage("prepareForAbort: " + getName(p));
                    if (prof != null)
                        prof.checkPoint ("prepareForAbort: " + getName(p));
                }
            } else {
                if (hasStatusListeners)
                    notifyStatusListeners (
                        session, TransactionStatusEvent.State.PREPARING, id, getName(p), context
                    );
                action = prepare (p, id, context);

                abort  = (action & PREPARED) == ABORTED;
                retry  = (action & RETRY) == RETRY;
                pause  = (action & PAUSE) == PAUSE;
                if (evt != null) {
                    evt.addMessage ("        prepare: "
                            + getName(p)
                            + (abort ? " ABORTED" : " PREPARED")
                            + (retry ? " RETRY" : "")
                            + (pause ? " PAUSE" : "")
                            + ((action & READONLY) == READONLY ? " READONLY" : "")
                            + ((action & NO_JOIN) == NO_JOIN ? " NO_JOIN" : ""));
                    if (prof != null)
                        prof.checkPoint ("prepare: " + getName(p));
                }
            }
            if ((action & READONLY) == 0) {
                Chronometer c = new Chronometer();
                snapshot (id, context);
                if (metrics != null)
                    metrics.record(getName(p) + "-snapshot", c.elapsed());
            }
            if ((action & NO_JOIN) == 0) {
                members.add (p);
            }
            if (p instanceof GroupSelector && ((action & PREPARED) == PREPARED || callSelectorOnAbort)) {
                String groupName = null;
                Chronometer c = new Chronometer();
                try {
                    groupName = ((GroupSelector)p).select (id, context);
                } catch (Exception e) {
                    if (evt != null) 
                        evt.addMessage ("       selector: " + getName(p) + " " + e.getMessage());
                    else 
                        getLog().error ("       selector: " + getName(p) + " " + e.getMessage());
                } finally {
                    if (metrics != null)
                        metrics.record(getName(p) + "-selector", c.lap());
                }
                if (evt != null) {
                    evt.addMessage ("       selector: '" + groupName +"'");
                }
                if (groupName != null) {
                    StringTokenizer st = new StringTokenizer (groupName, " ,");
                    List participants = new ArrayList();
                    while (st.hasMoreTokens ()) {
                        String grp = st.nextToken();
                        addGroup (id, grp);
                        if (evt != null && groups.get(grp) == null)
                            evt.addMessage ("                 WARNING: group '" + grp + "' not configured");
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
                        this, id, p, members, iter, abort, expirationMonitor, prof, evt
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
        return abort ? retry ? RETRY : ABORTED : PREPARED;
    }
    protected List<TransactionParticipant> getParticipants (String groupName) {
        List<TransactionParticipant> participants = groups.get (groupName);
        if (participants == null) {
            participants = new ArrayList();
        }
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
            final TransactionStatusListener listener = (TransactionStatusListener) factory.newInstance (QFactory.getAttributeValue (e, "class"));
            factory.setConfiguration (listener, config);
            addListener(listener);
        }
    }

    protected void initParticipants (Element config) 
        throws ConfigurationException
    {
        groups.put (DEFAULT_GROUP,  initGroup (config));
        for (Element e : config.getChildren("group")) {
            String name = QFactory.getAttributeValue (e, "name");
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
        List<TransactionParticipant> group = new ArrayList<>();
        for (Element el : e.getChildren ("participant")) {
            if (QFactory.isEnabled(el)) {
                group.add(createParticipant(el));
            } else {
                getLog().warn ("participant ignored (enabled='" + QFactory.getEnabledAttribute(el) + "'): " + el.getAttributeValue("class") + "/" + el.getAttributeValue("realm"));
            }
        }
        return group;
    }
    public TransactionParticipant createParticipant (Element e) 
        throws ConfigurationException
    {
        QFactory factory = getFactory();
        TransactionParticipant participant =
            factory.newInstance (QFactory.getAttributeValue (e, "class")
        );
        factory.setLogger (participant, e);
        QFactory.invoke (participant, "setTransactionManager", this, TransactionManager.class);
        factory.setConfiguration (participant, e);
        String realm = QFactory.getAttributeValue(e, "realm");
        if (realm != null && realm.trim().length() > 0)
            realm = ":" + realm;
        else
            realm = "";
        names.put(participant, Caller.shortClassName(participant.getClass().getName())+realm);
        if (participant instanceof Destroyable) {
            destroyables.add((Destroyable) participant);
        }
        return participant;
    }

    @Override
    public int getOutstandingTransactions() {
        if (iisp instanceof LocalSpace)
            return ((LocalSpace) iisp).size(queue);
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
            ((JDBMSpace) sp).setAutoCommit(false);
        }
    }
    protected void commitOn (Space sp) {
        if (sp instanceof JDBMSpace) {
            JDBMSpace jsp = (JDBMSpace) sp;
            jsp.commit ();
            jsp.setAutoCommit(true);
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
        sp.out(tailLock, lock);
    }
    protected boolean tailDone () {
        String stateKey = getKey(STATE, tail);
        if (DONE.equals (psp.rdp (stateKey))) {
            purge (tail, true);
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
    protected void purge (long id, boolean full) {
        String stateKey   = getKey (STATE, id);
        String contextKey = getKey (CONTEXT, id);
        String groupsKey  = getKey (GROUPS, id);
        synchronized (psp) {
            commitOff (psp);
            if (full)
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
            purge (id, true);
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

    /**
     * This method gives the opportunity to decorate a LogEvent right before
     * it gets logged. When overriding it, unless you know what you're doing,
     * you should return a FrozenLogEvent in order to prevent concurrency issues.
     *
     * @param context current Context
     * @param evt current LogEvent
     * @param prof profiler (may be null)
     * @return FrozenLogEvent
     */
    protected FrozenLogEvent freeze(Serializable context, LogEvent evt, Profiler prof) {
        return new FrozenLogEvent(evt);
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
            PausedTransaction paused = context.getPausedTransaction();
            if (paused != null && paused.getTransactionManager().abortOnPauseTimeout)
                paused.forceAbort();
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
                    iisp.out (queue, context, retryTimeout);
                    psp.inp (RETRY_QUEUE);
                }
                ISOUtil.sleep(retryInterval);
            }
        }
    }

    public class InputQueueMonitor implements Runnable {
        @Override
        public void run() {
            Thread.currentThread().setName (getName()+"-input-queue-monitor");
            while (running()) {
                while (getOutstandingTransactions() > getActiveSessions() + threshold && running()) {
                    ISOUtil.sleep(100L);
                }
                if (!running())
                    break;
                try {
                    Object context = isp.in(queue, 1000L);
                    if (context != null) {
                        if (!running()) {
                            isp.out(queue, context); // place it back
                            break;
                        }
                        iisp.out(queue, context);
                    }
                } catch (SpaceError e) {
                    getLog().error(e);
                    ISOUtil.sleep(1000L); // relax on error
                }
            }
        }
    }

    @Override
    public void setDebug (boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean getDebugContext() {
        return debugContext;
    }

    @Override
    public void setDebugContext (boolean debugContext) {
        this.debugContext = debugContext;
    }

    @Override
    public boolean getDebug() {
        return debug;
    }


    @Override
    public int getActiveSessions() {
        return activeSessions.intValue();
    }
    public int getPausedCounter() {
        return pausedCounter.intValue();
    }
    public int getActiveTransactions() {
        return activeTransactions.intValue();
    }
    public int getMaxSessions() {
        return maxSessions;
    }
    public static Serializable getSerializable() {
        return tlContext.get();
    }
    public static <T extends Serializable> T getContext() {
        return (T) tlContext.get();
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
            String.format("%s:%d %s %s %s", getName(), id, method, p.getClass().getName(),
                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()))
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

    private String getName(TransactionParticipant p) {
        String name;
        return ((name = names.get(p)) != null) ? name : p.getClass().getName();
    }

    private String tmInfo() {
        return String.format ("in-transit=%d/%d, head=%d, tail=%d, paused=%d, outstanding=%d, active-sessions=%d/%d%s",
          getActiveTransactions(), getInTransit(), head, tail, pausedCounter.get(), getOutstandingTransactions(),
          getActiveSessions(), maxSessions,
          (tps != null ? ", " + tps.toString() : "")
        );
    }
}
