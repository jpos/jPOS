/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.transaction;

import java.io.Serializable;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.jpos.util.NameRegistrar;
import org.jdom.Element;
import org.jpos.space.Space;
import org.jpos.space.LocalSpace;
import org.jpos.space.JDBMSpace;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceUtil;
import org.jpos.q2.QFactory;
import org.jpos.q2.QBeanSupport;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;

public class TransactionManager 
    extends QBeanSupport 
    implements Runnable, TransactionConstants, TransactionManagerMBean
{
    Space sp;
    Space psp;
    String queue;
    String tailLock;
    Map groups;
    Thread[] threads;
    boolean debug;
    long head, tail, lastGC;
    long retryInterval = 5000L;
    long pauseTimeout  = 300*60*1000L;  // five minutes
    RetryTask retryTask = null;
    public static final String  HEAD       = "$HEAD";
    public static final String  TAIL       = "$TAIL";
    public static final String  CONTEXT    = "$CONTEXT.";
    public static final String  STATE      = "$STATE.";
    public static final String  GROUPS     = "$GROUPS.";
    public static final String  TAILLOCK   = "$TAILLOCK";
    public static final String  RETRY_QUEUE = "$RETRY_QUEUE";
    public static final String  LAST_RETRY = "$LAST_RETRY";
    public static final Integer PREPARING  = new Integer (0);
    public static final Integer COMMITTING = new Integer (1);
    public static final Integer DONE       = new Integer (2);
    public static final String  DEFAULT_GROUP = "";
    public static final long    MAX_PARTICIPANTS = 1000;  // loop prevention

    public void initService () throws ConfigurationException {
        queue = cfg.get ("queue", null);
        if (queue == null)
            throw new ConfigurationException ("queue property not specified");
        sp   = SpaceFactory.getSpace (cfg.get ("space"));
        psp  = SpaceFactory.getSpace (cfg.get ("persistent-space", this.toString()));
        tail = initCounter (TAIL, cfg.getLong ("initial-tail", 1));
        head = Math.max (initCounter (HEAD, tail), tail);
        initTailLock ();

        groups = new HashMap();
        initParticipants (getPersist());
    }
    public void startService () {
        NameRegistrar.register (getName (), this);
        recover ();
        int sessions = cfg.getInt ("sessions", 1);
        threads = new Thread[sessions];
        for (int i=0; i<sessions; i++) {
            Thread t = new Thread (this);
            t.setName (getName() + "-" + i);
            t.setDaemon (false);
            t.start ();
            threads[i] = t;
        }
    }
    public void stopService () {
        NameRegistrar.unregister (getName ());
        long sessions = cfg.getLong ("sessions", 1);
        for (int i=0; i<sessions; i++)
            sp.out (queue, this, 60*1000);
        for (int i=0; i<sessions; i++) {
            try {
                threads[i].join (300*1000);
            } catch (InterruptedException e) {
                getLog().warn ("Session " +i +" does not response - attempting to interrupt");
                threads[i].interrupt();
            }
            threads[i] = null;
        }
    }
    public void queue (Serializable context) {
        sp.out (queue, context);
    }
    public void run () {
        long id = 0;
        List members = null;
        Iterator iter = null;
        PausedTransaction pt;
        boolean abort = false;
        LogEvent evt = null;
        String threadName = Thread.currentThread().getName();
        getLog().info (threadName + " start");
        long startTime = 0L;
        while (running()) {
            try {
                Object obj = sp.in (queue);
                if (obj == this)
                    continue;   // stopService ``hack''
                if (!(obj instanceof Serializable)) {
                    getLog().error (
                        "non serializable '" + obj.getClass().getName()
                       + "' on queue '" + queue + "'"
                    );
                    continue;
                }
                if (obj instanceof Pausable) {
                    pt = ((Pausable)obj).getPausedTransaction();
                    if (pt != null) {
                        id      = pt.id();
                        members = pt.members();
                        iter    = pt.iterator();
                        abort   = pt.isAborting();
                    }
                } else 
                    pt = null;

                if (pt == null) {
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
                    startTime = System.currentTimeMillis();
                }
                Serializable context = (Serializable) obj;
                snapshot (id, context, PREPARING);
                int action = prepare (id, context, members, iter, abort, evt);
                switch (action) {
                    case PAUSE:
                        break;
                    case PREPARED:
                        setState (id, COMMITTING);
                        commit (id, context, members, false, evt);
                        break;
                    case ABORTED:
                        abort (id, context, members, false, evt);
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
                }
            } catch (Throwable t) {
                if (evt == null)
                    getLog().fatal (t); // should never happen
                else
                    evt.addMessage (t);
            } finally {
                if (evt != null) {
                    evt.addMessage ("elapsed time: " 
                        + (System.currentTimeMillis() - startTime) + "ms"
                    );
                    Logger.log (evt);
                    evt = null;
                }
            }
        }
        getLog().info (threadName + " stop");
    }
    public long getTail () {
        return tail;
    }
    public long getHead () {
        return head;
    }
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException 
    {
        super.setConfiguration (cfg);
        debug = cfg.getBoolean ("debug");
        retryInterval = cfg.getLong ("retry-interval", retryInterval);
        pauseTimeout  = cfg.getLong ("pause-timeout", pauseTimeout);
    }
    protected void commit 
        (long id, Serializable context, List members, boolean recover, LogEvent evt) 
    {
        Iterator iter = members.iterator();
        while (iter.hasNext ()) {
            TransactionParticipant p = (TransactionParticipant) iter.next();
            if (recover && p instanceof ContextRecovery) {
                context = ((ContextRecovery) p).recover (id, context, true);
                if (evt != null)
                    evt.addMessage (" commit-recover: " + p.getClass().getName());
            }
            commit (p, id, context);
            if (evt != null)
                evt.addMessage ("         commit: " + p.getClass().getName());
        }
    }
    protected void abort 
        (long id, Serializable context, List members, boolean recover, LogEvent evt) 
    {
        Iterator iter = members.iterator();
        while (iter.hasNext ()) {
            TransactionParticipant p = (TransactionParticipant) iter.next();
            if (recover && p instanceof ContextRecovery) {
                context = ((ContextRecovery) p).recover (id, context, false);
                if (evt != null)
                    evt.addMessage ("  abort-recover: " + p.getClass().getName());
            }
            abort (p, id, context);
            if (evt != null)
                evt.addMessage ("          abort: " + p.getClass().getName());
        }
    }
    protected int prepareForAbort
        (TransactionParticipant p, long id, Serializable context) 
    {
        try {
            if (p instanceof AbortParticipant)
                return ((AbortParticipant)p).prepareForAbort (id, context);
        } catch (Throwable t) {
            getLog().warn ("PREPARE-FOR-ABORT: " + Long.toString (id), t);
        }
        return ABORTED | NO_JOIN;
    }
    protected int prepare 
        (TransactionParticipant p, long id, Serializable context) 
    {
        try {
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
            p.commit (id, context);
        } catch (Throwable t) {
            getLog().warn ("COMMIT: " + Long.toString (id), t);
        }
    }
    protected void abort 
        (TransactionParticipant p, long id, Serializable context) 
    {
        try {
            p.abort (id, context);
        } catch (Throwable t) {
            getLog().warn ("ABORT: " + Long.toString (id), t);
        }
    }
    protected int prepare (long id, Serializable context, List members, Iterator iter, boolean abort, LogEvent evt) {
        boolean retry = false;
        boolean pause = false;
        for (int i=0; iter.hasNext (); i++) {
            int action = 0;
            if (i > MAX_PARTICIPANTS) {
                getLog().warn (
                    "loop detected - transaction " +id + " aborted."
                );
                return ABORTED;
            }
            TransactionParticipant p = (TransactionParticipant) iter.next();
            if (abort) {
                action = prepareForAbort (p, id, context);
                if (evt != null && (p instanceof AbortParticipant))
                    evt.addMessage ("prepareForAbort: " + p.getClass().getName());
            } else {
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
                }
            }
            if ((action & READONLY) == 0) {
                snapshot (id, context);
            }
            if ((action & NO_JOIN) == 0) {
                members.add (p);
            }
            if (p instanceof GroupSelector) {
                String groupName = null;
                try {
                    groupName = ((GroupSelector)p).select (id, context);
                } catch (Exception e) {
                    if (evt != null) 
                        evt.addMessage ("  groupSelector " + p + " - " + e.getMessage());
                    else 
                        getLog().error (" groupSelector: " + p + " - " + e.getMessage());
                }
                if (evt != null) 
                    evt.addMessage ("  groupSelector: " + groupName);
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
                    ((Pausable)context).setPausedTransaction (
                        new PausedTransaction (id, members, iter, abort)
                    );
                } else {
                    throw new RuntimeException ("Unable to PAUSE transaction - Context is not Pausable");
                }
                return PAUSE;
            }
        }
        return members.size() == 0 ? NO_JOIN : 
            (abort ? (retry ? RETRY : ABORTED) : PREPARED);
    }
    protected List getParticipants (String groupName) {
        List participants = (List) groups.get (groupName);
        if (participants == null)
            participants = new ArrayList();
        return participants;
    }
    protected List getParticipants (long id) {
        List participants = getParticipants (DEFAULT_GROUP);
        String key = getKey(GROUPS, id);
        String grp = null;
        while ( (grp = (String) psp.inp (key)) != null) {
            participants.addAll (getParticipants (grp));
        }
        return participants;
    }
    protected void initParticipants (Element config) 
        throws ConfigurationException
    {
        groups.put (DEFAULT_GROUP,  initGroup (config));
        Iterator iter = config.getChildren ("group").iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            String name = e.getAttributeValue ("name");
            if (name == null) 
                throw new ConfigurationException ("missing group name");
            if (groups.get (name) != null) {
                throw new ConfigurationException (
                    "Group '" + name + "' already defined"
                );
            }
            groups.put (name, initGroup (e));
        }
    }
    protected ArrayList initGroup (Element e) 
        throws ConfigurationException
    {
        ArrayList group = new ArrayList ();
        Iterator iter = e.getChildren ("participant").iterator();
        while (iter.hasNext()) {
            group.add (createParticipant ((Element) iter.next()));
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
        factory.invoke (participant, "setTransactionManager", this);
        factory.setConfiguration (participant, e);
        return participant;
    }
    public int getOutstandingTransactions() {
        if (sp instanceof LocalSpace)
            return ((LocalSpace)sp).size("queue");
        return -1;
    }
    protected String getKey (String prefix, long id) {
        StringBuffer sb = new StringBuffer (prefix);
        sb.append (Long.toString (id));
        return sb.toString ();
    }
    protected long initCounter (String name, long defValue) {
        Long L = (Long) psp.rdp (name);
        if (L == null) {
            L = new Long (defValue);
            psp.out (name, L);
        }
        return L.longValue();
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
            psp.out (TAIL, new Long (tail));
            commitOn (psp);
        }
    }
    protected void initTailLock () {
        tailLock = TAILLOCK + "." + Integer.toString (this.hashCode());
        SpaceUtil.wipe (sp, tailLock);
        sp.out (tailLock, TAILLOCK);
    }
    protected void checkTail () {
        Object lock = sp.in (tailLock);
        while (tailDone()) {
            tail++;
        }
        syncTail ();
        sp.out (tailLock, lock);
    }
    protected boolean tailDone () {
        String stateKey = getKey (STATE, tail);
        Integer state = (Integer) psp.rdp (stateKey);
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
            psp.out (HEAD, new Long (++head));
            commitOn (psp);
        }
        return h;
    }
    protected void snapshot (long id, Serializable context) {
        snapshot (id, context, null);
    }
    protected void snapshot (long id, Serializable context, Integer status) {
        if (!(psp instanceof JDBMSpace)) {
            // no sense taking a snapshot if space is not persistent
            return;
        }
        String contextKey = getKey (CONTEXT, id);
        synchronized (psp) {
            commitOff (psp);
            while (psp.inp (contextKey) != null)
                ;
            if (context != null)
                psp.out (contextKey, context);

            if (status != null) {
                String stateKey  = getKey (STATE, id);
                while (psp.inp (stateKey) != null)
                    ;
                psp.out (stateKey, status);
            }
            commitOn (psp);
        }
    }
    protected void setState (long id, Integer state) {
        String stateKey  = getKey (STATE, id);
        synchronized (psp) {
            commitOff (psp);
            while (psp.inp (stateKey) != null)
                ;
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
            while (psp.inp (stateKey) != null)
                ;
            while (psp.inp (contextKey) != null)
                ;
            while (psp.inp (groupsKey) != null)
                ;
            commitOn (psp);
        }
    }
    protected void recover () {
        if (tail < head) {
            getLog().info ("recover - tail=" +tail+", head="+head);
        }
        while (tail < head) {
            recover (tail++);
        }
        syncTail ();
    }
    protected void recover (long id) {
        LogEvent evt = getLog().createLogEvent ("recover");
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
                commit (id, context, getParticipants (id), true, evt);
            } else if (PREPARING.equals (state)) {
                abort (id, context, getParticipants (id), true, evt);
            }
            purge (id);
        } finally {
            Logger.log (evt);
        }
    }
    protected synchronized void checkRetryTask () {
        if (retryTask == null) {
            retryTask = new RetryTask();
            new Thread(retryTask).start();
        }
    }
    public class RetryTask implements Runnable {
        public void run() {
            Thread.currentThread().setName (getName()+"retry-task");
            while (running()) {
                for (Object context; (context = psp.inp (RETRY_QUEUE)) != null;) 
                    sp.out (queue, context);
                try {
                    Thread.sleep (retryInterval);
                } catch (InterruptedException e) { } 
            }
        }
    }
}

