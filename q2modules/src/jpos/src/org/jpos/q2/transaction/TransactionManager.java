/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.q2.transaction;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.jdom.Element;
import org.jpos.space.Space;
import org.jpos.space.JDBMSpace;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceUtil;
import org.jpos.q2.QFactory;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.Q2ConfigurationException;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.transaction.TransactionConstants;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.ContextRecovery;

public class TransactionManager 
    extends QBeanSupport 
    implements Runnable, TransactionConstants, TransactionManagerMBean
{
    Space sp;
    Space psp;
    String queue;
    String tailLock;
    List participants;
    long head, tail;
    public static final String  HEAD       = "$HEAD";
    public static final String  TAIL       = "$TAIL";
    public static final String  CONTEXT    = "$CONTEXT.";
    public static final String  STATE      = "$STATE.";
    public static final String  TAILLOCK   = "$TAILLOCK";
    public static final Integer PREPARING  = new Integer (0);
    public static final Integer COMMITTING = new Integer (1);
    public static final Integer DONE       = new Integer (2);

    public void initService () throws Q2ConfigurationException {
        queue = cfg.get ("queue", null);
        if (queue == null)
            throw new Q2ConfigurationException ("queue property not specified");
        sp   = SpaceFactory.getSpace (cfg.get ("space"));
        psp  = SpaceFactory.getSpace (cfg.get ("persistent-space"));
        tail = initCounter (TAIL, cfg.getLong ("initial-tail", 0));
        head = Math.max (initCounter (HEAD, tail), tail);
        tailLock = TAILLOCK + "." + Integer.toString (this.hashCode());

        initTailLock ();
        initParticipants (getPersist());
    }
    public void startService () {
        recover ();
        long sessions = cfg.getLong ("sessions", 1);
        for (int i=0; i<sessions; i++) {
            Thread t = new Thread (this);
            t.setName (getName() + "-" + i);
            t.start ();
        }
    }
    public void stopService () {
        long sessions = cfg.getLong ("sessions", 1);
        for (int i=0; i<sessions; i++)
            sp.out (queue, this);
    }
    public void run () {
        long id;
        String threadName = Thread.currentThread().getName();
        getLog().info (threadName + " start");
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
                id = nextId ();
                List members = new ArrayList ();
                Serializable context = (Serializable) obj;
                snapshot (id, context, PREPARING);
                int action = prepare (id, context, members);
                switch (action) {
                    case PREPARED:
                        setState (id, COMMITTING);
                        commit (id, context, members, false);
                        break;
                    case ABORTED:
                        abort (id, context, members, false);
                        break;
                    case NO_JOIN:
                        break;
                }
                snapshot (id, null, DONE);
                if (id == tail) {
                    checkTail ();
                }
            } catch (Throwable t) {
                getLog().fatal (t); // should never happen
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
    private void commit 
        (long id, Serializable context, List members, boolean recover) 
    {
        Iterator iter = members.iterator();
        while (iter.hasNext ()) {
            TransactionParticipant p = (TransactionParticipant) iter.next();
            if (recover && p instanceof ContextRecovery)
                context = ((ContextRecovery) p).recover (id, context, true);
            commit (p, id, context);
        }
    }
    private void abort 
        (long id, Serializable context, List members, boolean recover) 
    {
        Iterator iter = members.iterator();
        while (iter.hasNext ()) {
            TransactionParticipant p = (TransactionParticipant) iter.next();
            if (recover && p instanceof ContextRecovery)
                context = ((ContextRecovery) p).recover (id, context, false);
            abort (p, id, context);
        }
    }
    private int prepareForAbort
        (TransactionParticipant p, long id, Serializable context) 
    {
        try {
            if (p instanceof AbortParticipant)
                return ((AbortParticipant)p).prepareForAbort (id, context);
        } catch (Throwable t) {
            getLog().warn ("PREPARE-FOR-ABORT: " + Long.toString (id), t);
        }
        return ABORTED;
    }
    private int prepare 
        (TransactionParticipant p, long id, Serializable context) 
    {
        try {
            return p.prepare (id, context);
        } catch (Throwable t) {
            getLog().warn ("PREPARE: " + Long.toString (id), t);
        }
        return ABORTED;
    }
    private void commit 
        (TransactionParticipant p, long id, Serializable context) 
    {
        try {
            p.commit (id, context);
        } catch (Throwable t) {
            getLog().warn ("COMMIT: " + Long.toString (id), t);
        }
    }
    private void abort 
        (TransactionParticipant p, long id, Serializable context) 
    {
        try {
            p.abort (id, context);
        } catch (Throwable t) {
            getLog().warn ("ABORT: " + Long.toString (id), t);
        }
    }
    private int prepare (long id, Serializable context, List members) {
        boolean abort = false;
        Iterator iter = participants.iterator();
        while (iter.hasNext ()) {
            int action = 0;
            TransactionParticipant p = (TransactionParticipant) iter.next();
            if (abort) {
                action = prepareForAbort (p, id, context);
            } else {
                action = prepare (p, id, context);
                abort  = (action & PREPARED) == ABORTED;
            }
            if ((action & READONLY) == 0) {
                snapshot (id, context);
            }
            if ((action & NO_JOIN) == 0) 
                members.add (p);
        }
        return members.size() == 0 ? NO_JOIN : (abort ? ABORTED : PREPARED);
    }
    private void initParticipants (Element config) 
        throws Q2ConfigurationException
    {
        participants = new ArrayList ();
        Iterator iter = config.getChildren ("participant").iterator();
        while (iter.hasNext()) {
            participants.add (createParticipant ((Element) iter.next()));
        }
    }
    private TransactionParticipant createParticipant (Element e) 
        throws Q2ConfigurationException
    {
        QFactory factory = getFactory();
        TransactionParticipant participant = (TransactionParticipant)
            factory.newInstance (e.getAttributeValue ("class"));

        factory.setLogger (participant, e);
        factory.setConfiguration (participant, e);
        return participant;
    }
    private String getKey (String prefix, long id) {
        StringBuffer sb = new StringBuffer (prefix);
        sb.append (Long.toString (id));
        return sb.toString ();
    }
    private long initCounter (String name, long defValue) {
        Long L = (Long) psp.rdp (name);
        if (L == null) {
            L = new Long (defValue);
            psp.out (name, L);
        }
        return L.longValue();
    }
    private void commitOff (Space sp) {
        if (sp instanceof JDBMSpace) {
            ((JDBMSpace) sp).setAutoCommit (false);
        }
    }
    private void commitOn (Space sp) {
        if (sp instanceof JDBMSpace) {
            JDBMSpace jsp = (JDBMSpace) sp;
            jsp.commit ();
            jsp.setAutoCommit (true);
        }
    }
    private void syncTail () {
        synchronized (psp) {
            commitOff (psp);
            psp.inp (TAIL);
            psp.out (TAIL, new Long (tail));
            commitOn (psp);
        }
    }
    private void initTailLock () {
        SpaceUtil.wipe (sp, tailLock);
        sp.out (tailLock, TAILLOCK);
    }
    private void checkTail () {
        Object lock = sp.inp (tailLock);
        if (lock == null)   // another thread is checking tail
            return;

        while (tailDone()) {
            tail++;
        }
        syncTail ();
        sp.out (tailLock, lock);
    }
    private boolean tailDone () {
        String stateKey = getKey (STATE, tail);
        Integer state = (Integer) psp.rdp (stateKey);
        if (DONE.equals (psp.rdp (stateKey))) {
            purge (tail);
            return true;
        }
        return false;
    }
    private long nextId () {
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
    private void snapshot (long id, Serializable context) {
        snapshot (id, context, null);
    }
    private void snapshot (long id, Serializable context, Integer status) {
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
    private void setState (long id, Integer state) {
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
    private void purge (long id) {
        String stateKey   = getKey (STATE, id);
        String contextKey = getKey (CONTEXT, id);
        synchronized (psp) {
            commitOff (psp);
            while (psp.inp (stateKey) != null)
                ;
            while (psp.inp (contextKey) != null)
                ;
            commitOn (psp);
        }
    }
    private void recover () {
        if (tail < head) {
            getLog().info ("recover - tail=" +tail+", head="+head);
        }
        while (tail < head) {
            recover (tail++);
        }
        syncTail ();
    }
    private void recover (long id) {
        LogEvent evt = getLog().createLogEvent ("recover");
        evt.addMessage ("<id>" + id + "</id>");
        try {
            String stateKey   = getKey (STATE, id);
            String contextKey = getKey (CONTEXT, id);
            Integer state = (Integer) psp.rdp (stateKey);
            if (state == null) {
                evt.addMessage ("<unknown/>");
                SpaceUtil.wipe (psp, contextKey);   // just in case ...
                return;
            }
            Serializable context = (Serializable) psp.rdp (contextKey);
            if (context != null)
                evt.addMessage (context);

            if (DONE.equals (state)) {
                evt.addMessage ("<done/>");
                purge (id);
                return; 
            } else if (COMMITTING.equals (state)) {
                evt.addMessage ("<commit/>");
                commit (id, context, participants, true);
            } else if (PREPARING.equals (state)) {
                evt.addMessage ("<abort/>");
                abort (id, context, participants, true);
            }
        } finally {
            Logger.log (evt);
        }
    }
}

