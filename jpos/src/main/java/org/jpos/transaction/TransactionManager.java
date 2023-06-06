/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2022 jPOS Software SRL
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
import org.jpos.function.TriConsumer;
import org.jpos.function.TriFunction;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.space.*;
import org.jpos.util.*;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.jpos.iso.ISOUtil;

import static org.jpos.transaction.ContextConstants.TIMESTAMP;

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
    protected Map<String,List<TransactionParticipant>> groups;
    private Set<Destroyable> destroyables = new HashSet<>();
    private static final ThreadLocal<Serializable> tlContext = new ThreadLocal<>();
    private static final ThreadLocal<Long> tlId = new ThreadLocal<>();
    private Metrics metrics;
    private static Map<TransactionParticipant,ParticipantParams> params = new HashMap<>();
    private long globalMaxTime;

    private Space<String,Object> sp;
    private Space<String,Object> psp;
    private Space<String,Object> isp;  // real input space
    private Space<String,Object> iisp; // internal input space
    private String queue;
    private String tailLock;
    private final List<TransactionStatusListener> statusListeners = new ArrayList<>();
    private boolean hasStatusListeners;
    private boolean debug;
    private boolean debugContext;
    private boolean profiler;
    private boolean doRecover;
    private boolean callSelectorOnAbort;
    private boolean abortOnMisconfiguredGroups;
    private int sessions;
    private int maxSessions;
    private int threshold;
    private int maxActiveTransactions;
    private final AtomicInteger activeSessions = new AtomicInteger();
    private final AtomicInteger pausedSessions = new AtomicInteger();

    private volatile long head, tail;
    private long retryInterval = 5000L;
    private long retryTimeout  = 60000L;
    private long pauseTimeout  = 60000L;
    private boolean abortOnPauseTimeout = true;
    private Runnable retryTask = null;
    private TPS tps;
    private ExecutorService executor;

    @Override
    public void initService () throws ConfigurationException {
        queue = cfg.get ("queue", null);
        if (queue == null)
            throw new ConfigurationException ("queue property not specified");
        sp  = SpaceFactory.getSpace (cfg.get ("space"));
        isp = iisp = SpaceFactory.getSpace (cfg.get ("input-space", cfg.get ("space")));
        psp  = SpaceFactory.getSpace (cfg.get ("persistent-space", this.toString()));
        tail = initCounter (TAIL, cfg.getLong ("initial-tail", 1));
        head = Math.max (initCounter (HEAD, tail), tail);
        initTailLock ();

        groups = new HashMap<>();
        initParticipants (getPersist());
        initStatusListeners (getPersist());
        executor = Executors.newThreadPerTaskExecutor(
          Thread.ofVirtual()
          .allowSetThreadLocals(true)
          .inheritInheritableThreadLocals(false)
          .name(getName())
          .factory());
    }

    @Override
    public void startService () throws Exception {
        recover();
        if (tps != null)
            tps.stop();
        tps = new TPS (cfg.getBoolean ("auto-update-tps", true));
        Thread.ofPlatform().start(this);
        if (psp.rdp (RETRY_QUEUE) != null)
            checkRetryTask();

        if (iisp != isp) {
            Thread.ofVirtual().unstarted(
              new InputQueueMonitor()
            ).start();
        }
        NameRegistrar.register(getName(), this);
    }

    @Override
    public void stopService () {
        NameRegistrar.unregister(getName());
        if (iisp != isp)
            for (Object o=iisp.inp(queue); o != null; o=iisp.inp(queue))
                isp.out(queue, o); // push back to replicated space


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
        while (running()) {
            if (heavyLoaded()) {
                ISOUtil.sleep (100L);
                getLog().info ("HeavyLoaded - active sessions: " + getActiveSessions());
                continue;
            }
            Object obj = iisp.in (queue, MAX_WAIT);
            if (obj instanceof Serializable context) {
                if (getActiveSessions() <= maxSessions) {
                    if (context instanceof Context ctx)
                        ctx.log ("active=%d, maxSessions=%d".formatted(getActiveSessions(), maxSessions));
                    int session = activeSessions.incrementAndGet();
                    executor.execute(() -> {
                        runTransaction(context, session);
                        activeSessions.decrementAndGet();
                    });
                }
                else {
                    ISOUtil.sleep(100L);
                    iisp.push(queue, context);  // push it back
                }
            }
        }
    }

    private void runTransaction (Serializable context, int session) {
        long id = 0;
        List<TransactionParticipant> members = null;
        Iterator<TransactionParticipant> iter = null;
        boolean abort;
        LogEvent evt;
        Profiler prof;
        Thread thread = Thread.currentThread();

        prof = null;
        evt = null;
        thread.setName (getName() + "-" + session + ":idle");
        int action = -1;
        try {
            setThreadLocal(id, context);
            if (hasStatusListeners)
                notifyStatusListeners (session, TransactionStatusEvent.State.READY, id, "", null);

            Chronometer chronometer = new Chronometer(getStart(context));

            abort = false;
            id = nextId ();
            members = new ArrayList<> ();
            iter = getParticipants (DEFAULT_GROUP).iterator();
            if (debug) {
                evt = getLog().createLogEvent(
                  "debug",
                  "%s:%d".formatted(Thread.currentThread().getName(), id)
                );
                if (debugContext) {
                    evt.addMessage (context);
                }
                prof = new Profiler();
            }
            snapshot (id, context, PREPARING);
            action = prepare (session, id, context, members, iter, abort, evt, prof, chronometer);
            switch (action) {
                case PREPARED:
                    if (members.size() > 0) {
                        setState(id, COMMITTING);
                        commit(session, id, context, members, false, evt, prof);
                    }
                    break;
                case ABORTED:
                    if (members.size() > 0) {
                        abort(session, id, context, members, false, evt, prof);
                    }
                    break;
                case RETRY:
                    psp.out (RETRY_QUEUE, context);
                    checkRetryTask();
                    break;
                case NO_JOIN:
                    break;
            }
            snapshot (id, null, DONE);
            if (id == tail) {
                checkTail ();
            } else {
                purge (id, false);
            }
            tps.tick();
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
                  TransactionStatusEvent.State.DONE,
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
                if (getInTransit() > Math.max(maxActiveTransactions, activeSessions.get()) * 100L) {
                    evt.addMessage("WARNING: IN-TRANSIT TOO HIGH");
                }
                evt.addMessage (
                  String.format (" %s, elapsed=%dms",
                    tmInfo(),
                    prof.getElapsedInMillis()
                  )
                );
                evt.addMessage (prof);
                try {
                    Logger.log(freeze(context, evt, prof));
                } catch (Throwable t) {
                    getLog().error(t);
                }
            }
        }
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
        maxActiveTransactions = cfg.getInt  ("max-active-sessions", 0);
        sessions = cfg.getInt ("sessions", 1);
        threshold = cfg.getInt ("threshold", sessions / 2);
        maxSessions = cfg.getInt ("max-sessions", sessions);
        globalMaxTime = cfg.getLong("max-time", 0L);
        if (maxSessions < sessions)
            throw new ConfigurationException("max-sessions < sessions");
        if (maxActiveTransactions > 0) {
            if (maxActiveTransactions < sessions)
                throw new ConfigurationException("max-active-sessions < sessions");
            if (maxActiveTransactions < maxSessions)
                throw new ConfigurationException("max-active-sessions < max-sessions");
        }
        callSelectorOnAbort = cfg.getBoolean("call-selector-on-abort", true);
        if (profiler)
            metrics = new Metrics(new AtomicHistogram(cfg.getLong("metrics-highest-trackable-value", 60000), 2));
        abortOnMisconfiguredGroups = cfg.getBoolean("abort-on-misconfigured-groups");
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
            ParticipantParams pp = getParams(p);
            if (recover && p instanceof ContextRecovery cr) {
                context = recover (cr, id, context, pp, true);
                if (evt != null)
                    evt.addMessage (" commit-recover: " + getName(p));
            }
            if (hasStatusListeners)
                notifyStatusListeners (
                    session, TransactionStatusEvent.State.COMMITING, id, getName(p), context
                );
            commitOrAbort (p, id, context, pp, this::commit);
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
            ParticipantParams pp = getParams(p);
            if (recover && p instanceof ContextRecovery cr) {
                context = recover (cr, id, context, pp, true);
                if (evt != null)
                    evt.addMessage ("  abort-recover: " + getName(p));
            }
            if (hasStatusListeners)
                notifyStatusListeners (
                    session, TransactionStatusEvent.State.ABORTING, id, getName(p), context
                );

            commitOrAbort (p, id, context, pp, this::abort);
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
            getLog().warn ("ABORT: " + id, t);
        }
        if (metrics != null)
            metrics.record(getName(p) + "-abort", c.elapsed());
    }
    protected int prepare
        (int session, long id, Serializable context, List<TransactionParticipant> members, Iterator<TransactionParticipant> iter, boolean abort, LogEvent evt, Profiler prof, Chronometer chronometer)
    {
        boolean retry = false;
        for (int i=0; iter.hasNext (); i++) {
            int action;
            if (i > MAX_PARTICIPANTS) {
                getLog().warn (
                    "loop detected - transaction " +id + " aborted."
                );
                return ABORTED;
            }
            TransactionParticipant p = iter.next();
            ParticipantParams pp = getParams(p);
            if (!abort && pp.maxTime > 0 && chronometer.elapsed() > pp.maxTime) {
                abort = true;
                if (evt != null)
                    evt.addMessage("    forcedAbort: " + getName(p) + " elapsed=" + chronometer.elapsed());
            }

            if (abort) {
                if (hasStatusListeners)
                    notifyStatusListeners (
                        session, TransactionStatusEvent.State.PREPARING_FOR_ABORT, id, getName(p), context
                    );

                action = prepareOrAbort (p, id, context, pp, this::prepareForAbort);

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


                chronometer.lap();
                action = prepareOrAbort (p, id, context, pp, this::prepare);
                boolean timeout = pp.timeout > 0 && chronometer.partial() > pp.timeout;
                boolean maxTime = pp.maxTime > 0 && chronometer.elapsed() > pp.maxTime;
                if (timeout || maxTime)
                    action &= (PREPARED ^ 0xFFFF);

                abort  = (action & PREPARED) == ABORTED;
                retry  = (action & RETRY) == RETRY;

                if (evt != null) {
                    evt.addMessage ("        prepare: "
                            + getName(p)
                            + (abort ? " ABORTED" : " PREPARED")
                            + (timeout ? " TIMEOUT" : "")
                            + (maxTime ? " MAX_TIMEOUT" : "")
                            + (retry ? " RETRY" : "")
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
                    List<TransactionParticipant> participants = new ArrayList();
                    while (st.hasMoreTokens ()) {
                        String grp = st.nextToken();
                        addGroup (id, grp);
                        if (evt != null && groups.get(grp) == null) {
                            evt.addMessage ("                 WARNING: group '" + grp + "' not configured");
                            if (abortOnMisconfiguredGroups)
                                abort = true;
                        }
                        participants.addAll (getParticipants (grp));
                    }
                    while (iter.hasNext())
                        participants.add (iter.next());

                    iter = participants.iterator();
                }
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

        params.put(participant, new ParticipantParams(
            Caller.shortClassName(participant.getClass().getName())+realm,
            getLong (e, "timeout", 0L),
            getLong (e, "max-time", globalMaxTime),
            getSet(e.getChild("requires")),
            getSet(e.getChild("provides")),
            getSet(e.getChild("optional"))
          )
        );
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
        sb.append (id);
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
        if (sp instanceof JDBMSpace jsp) {
            jsp.setAutoCommit(false);
        }
    }
    protected void commitOn (Space sp) {
        if (sp instanceof JDBMSpace jsp) {
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
            tail++;
            Thread.yield();
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
            Thread.ofVirtual().start(retryTask).setDaemon(true);
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

    /**
     * This method returns the number of sessions that can be started at this point in time
     * @return number of sessions
     */
    protected int getSessionsToStandUp() {
        int outstandingTransactions = getOutstandingTransactions();
        int activeSessions = getActiveSessions();
        int count = 0;
        if (activeSessions < maxSessions && outstandingTransactions > threshold) {
            count = Math.min(outstandingTransactions, maxSessions - activeSessions);
        }
        return Math.min(1000, count); // reasonable value for virtual thread creation within one second
    }
  
    /** 
     * This method returns true if current session should stop working on more messages
     * @return
     */
    protected boolean isSessionToStandDown() {
        return false;
    }

    @Override
    public int getActiveSessions() {
        return activeSessions.intValue();
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
        return getParams(p).name();
    }

    private ParticipantParams getParams (TransactionParticipant p) {
        return Optional.ofNullable(params.get(p)).orElse(
          new ParticipantParams(p.getClass().getName(), 0L, 0L, Collections.emptySet(), Collections.emptySet(), Collections.emptySet())
        );
    }

    private String tmInfo() {
        return String.format ("in-transit=%d, head=%d, tail=%d, paused=%d, outstanding=%d, active-sessions=%d/%d%s",
          getInTransit(), head, tail, pausedSessions.get(), getOutstandingTransactions(),
          getActiveSessions(), maxSessions,
          (tps != null ? ", " + tps : "")
        );
    }

    private long getLong (Element e, String attributeName, long defValue) {
        String s = QFactory.getAttributeValue (e, attributeName);
        if (s != null) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {}
        }
        return defValue;
    }

    private Instant getStart (Serializable context) {
        if (context instanceof Context) {
            Object o = ((Context) context).get(TIMESTAMP);
            if (o instanceof Instant)
                return (Instant) o;
        }
        return Instant.now();
    }

    private boolean heavyLoaded() {
        return getActiveSessions() >= maxSessions;
    }

    private int pauseAndWait(Serializable context, int action) {
        if (context instanceof Pausable pausable) try {
            pausedSessions.incrementAndGet();
            Future<Integer> paused = pausable.pause();
            long timeout = pausable.getTimeout();
            timeout = timeout > 0 ? Math.min (timeout, pauseTimeout) : pauseTimeout;
            try {
                action = paused.get(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException e) {
                if (context instanceof Context ctx)
                    ctx.log(e);
            } catch (TimeoutException e) {
                action &= (PREPARED ^ 0xFFFF); // turn off 'PREPARED' - we need to abort
            }
        } finally {
            pausedSessions.decrementAndGet();
        }
        return action;
    }

    private record ParticipantParams (
      String name,
      long timeout,
      long maxTime,
      Set<String> requires,
      Set<String> provides,
      Set<String> optional) {
        public boolean isConstrained() {
            return !requires.isEmpty() || !optional.isEmpty();
        }
    }

    private Set<String> getSet (Element e) {
        return e != null ? new HashSet<>(Arrays.asList(ISOUtil.commaDecode(e.getTextTrim()))) : Collections.emptySet();
    }

    private int prepareOrAbort (TransactionParticipant p, long id, Serializable context, ParticipantParams pp, TriFunction<TransactionParticipant, Long, Serializable, Integer> preparationFunction) {
        int action;
        if (context instanceof Context ctx && pp.isConstrained()) {
            if (!ctx.hasKeys(pp.requires.toArray())) {
                ctx.log ("missing.requires: '%s'".formatted(ctx.keysNotPresent(pp.requires.toArray())));
                action = ABORTED;
            } else {
                Context c = ctx.clone(pp.requires.toArray(), pp.optional.toArray());
                action = preparationFunction.apply(p, id, c);
                ctx.merge(c.clone(pp.provides.toArray()));
            }
        } else {
            action = preparationFunction.apply(p, id, context);
        }
        if ((action & PAUSE) == PAUSE) {
            action = pauseAndWait(context, action);
        }
        return action;
    }

    private void commitOrAbort (TransactionParticipant p, long id, Serializable context, ParticipantParams pp, TriConsumer<TransactionParticipant, Long, Serializable> preparationFunction) {
        if (context instanceof Context ctx && pp.isConstrained()) {
            Context c = ctx.clone(pp.requires.toArray(), pp.optional.toArray());
            preparationFunction.accept(p, id, c);
            ctx.merge(c.clone(pp.provides.toArray()));
        } else {
            preparationFunction.accept(p, id, context);
        }
    }

    private Serializable recover (ContextRecovery p, long id, Serializable context, ParticipantParams pp, boolean commit) {
        if (context instanceof Context ctx && pp.isConstrained()) {
            Context c = ctx.clone(pp.requires.toArray(), pp.optional.toArray());
            Serializable s = p.recover (id, c, commit);
            return (s instanceof Context rc) ?
                rc.clone (pp.provides.toArray()) : s;
        } else {
            return p.recover (id, context, commit);
        }
    }
}
