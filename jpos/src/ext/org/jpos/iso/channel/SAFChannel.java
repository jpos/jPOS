/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.iso.channel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.iso.FilteredChannel;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMUX;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISORequest;
import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.util.BlockingQueue;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import com.sun.jini.reliableLog.LogException;
import com.sun.jini.reliableLog.LogHandler;
import com.sun.jini.reliableLog.ReliableLog;

/**
 * Store & Forward channel
 * Incoming filters are processed at 'store' time while Outgoing
 * filters are processed at 'forward' time.
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @since 1.3.9
 * @see FilteredChannel
 */
public class SAFChannel extends LogHandler
    implements ISOChannel, LogSource, ReConfigurable, 
               Runnable, SAFChannelMBean, FilteredChannel
{
    boolean usable = false;
    private int[] cnt;
    String name;
    BlockingQueue queue;
    Logger logger;
    String realm;
    Configuration cfg;
    ReliableLog log;
    ISOMUX mux;
    boolean debug = false;
    Vector outgoingFilters, incomingFilters;

    public SAFChannel () {
        super();
        cnt = new int[SIZEOF_CNT];
        queue = new BlockingQueue();
        outgoingFilters = new Vector();
        incomingFilters = new Vector();
        new Thread (this).start ();
    }

    public void setPackager(ISOPackager packager) { }

    public void connect () { }

    public void disconnect () { }

    public void reconnect ()  { }

    public boolean isConnected() {
        return usable;
    }

    public synchronized void send (ISOMsg m)
        throws IOException,ISOException, VetoException
    {
        if (!isConnected())
            throw new ISOException ("unconnected ISOChannel retry later");
        m.setDirection(ISOMsg.INCOMING);
        LogEvent evt = new LogEvent (this, "saf-send", m);
        m = applyFilters (outgoingFilters, m, evt);
        m.setDirection(ISOMsg.INCOMING);
        logUpdate (new LogEntry (LogEntry.QUEUE, m));
        queue.enqueue (m);
        cnt[TX]++;
        Logger.log (evt);
    }

    /* ---------------------------------------------------------------
    
    Future expansion, we may want to store messages in an external
    file, should our in-memory queue grows beyond a high water mark 

    private void addMessage (String f, ISOMsg m) throws IOException {
        FileOutputStream fos = new FileOutputStream (f, true);
        ObjectOutputStream out = new ObjectOutputStream (fos);
        out.writeObject (m);
        out.flush();
        fos.getFD().sync();
        out.close();
    }

    ---------------------------------------------------------------- */

    public ISOMsg receive() throws ISOException {
        throw new ISOException ("can not receive from SAFChannel");
    }

    public void setUsable (boolean usable) {
        this.usable = usable;
    }

    public void setName (String name) {
        this.name = name;
        NameRegistrar.register ("channel."+name, this);
    }

    public String getName() {
        return name;
    }

    public ISOPackager getPackager() {
        return null;
    }
    public void resetCounters() {
        cnt = new int[SIZEOF_CNT];
    }
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    public String getRealm () {
        return realm;
    }
    public Logger getLogger() {
        return logger;
    }
    /**
     * @param cfg 
     * <ul>
     *  <li>logdir - reliable log directory
     *  <li>flag-retransmissions - ditto (i.e. sends 0221 instead of 0220)
     *  <li>destination-mux - ditto
     *  <li>timeout - time in millis to wait for a response
     *  <li>delay   - inter-message delay
     *  <li>debug   - true/false
     * </ul>
     */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        debug = cfg.getBoolean ("debug") && (getLogger() != null);
        if (this.cfg == null) {
            try {
                ReliableLog log = 
                    new ReliableLog (cfg.get("logdir",null), this);
                try {
                    log.recover();
                } catch (LogException le) {
                    Logger.log (new LogEvent (this, "recover", le));
                }
                log.snapshot();
                setReliableLog (log);
                if (debug)
                    dumpList ();
                setUsable (true);
            } catch (IOException e) {
                throw new ConfigurationException (e);
            } 
        }
        this.cfg = cfg;
    }

    public void dumpList () {
        LogEvent evt = new LogEvent (this, "saf-dump-list");
        Iterator iter = queue.getQueue().iterator();
        while (iter.hasNext()) 
            evt.addMessage (iter.next());
        Logger.log (evt);
    }

    public void exportQueue (String fileName) throws IOException {
        synchronized (queue) {
            FileOutputStream fos = new FileOutputStream (fileName, true);
            PrintStream p = new PrintStream (fos);
                
            Iterator iter = queue.getQueue().iterator();
            while (iter.hasNext()) 
                ((ISOMsg)iter.next()).dump (p, "");
            fos.flush ();
        }
    }

    private void logUpdate (LogEntry entry) throws IOException {
        if (debug) 
            Logger.log (new LogEvent (this, "saf-update", entry));
        log.update (entry);
    }

    /**
     * @param log an already recovered - ready to use ReliableLog
     */
    public void setReliableLog (ReliableLog log) {
        this.log = log;
    }
    public void snapshot(OutputStream out) throws Exception {
        ObjectOutputStream stream = new ObjectOutputStream(out);
        stream.writeUTF(this.getClass().getName());
        stream.writeObject(queue.getQueue());
        stream.writeObject(null);
        stream.flush();
    }
    public void recover(InputStream in) throws Exception
    {
        ObjectInputStream stream = new ObjectInputStream(in);
        if (!this.getClass().getName().equals(stream.readUTF()))
            throw new IOException("log from wrong implementation");
        queue.setQueue ((LinkedList) stream.readObject());
    }
    public void applyUpdate(Object update) throws Exception {
        if (!(update instanceof LogEntry))
            throw new Exception ("not a LogEntry");

        LinkedList list = queue.getQueue();
        LogEntry entry = (LogEntry) update;
        switch (entry.op) {
            case LogEntry.QUEUE:
                list.addLast (entry.value);
                if (debug)
                    ((ISOMsg)entry.value).dump (System.out, "  QUEUE>");
                break;
            case LogEntry.REQUEUE:
                list.removeFirst();
                list.addFirst (entry.value);
                if (debug)
                    ((ISOMsg)entry.value).dump (System.out, "REQUEUE>");
                break;
            case LogEntry.DEQUEUE:
                if (debug)
                    System.out.println ("DEQUEUE>");
                list.removeFirst ();
                break;
        }
    }

    /**
     * @param filter incoming filter
     */
    public void addIncomingFilter (ISOFilter filter) {
        incomingFilters.add (filter);
    }
    /**
     * @param filter outgoing filter to add
     */
    public void addOutgoingFilter (ISOFilter filter) {
        outgoingFilters.add (filter);
    }
    public void addFilter (ISOFilter filter) {
        incomingFilters.add (filter);
        outgoingFilters.add (filter);
    }
    public void removeFilter (ISOFilter filter) {
        incomingFilters.remove (filter);
        outgoingFilters.remove (filter);
    }
    public void removeIncomingFilter (ISOFilter filter) {
        incomingFilters.remove (filter);
    }
    public void removeOutgoingFilter (ISOFilter filter) {
        outgoingFilters.remove (filter);
    }
    public Collection getIncomingFilters() {
        return incomingFilters;
    }
    public Collection getOutgoingFilters() {
        return outgoingFilters;
    }
    public void setIncomingFilters (Collection filters) {
        incomingFilters = new Vector (filters);
    }
    public void setOutgoingFilters (Collection filters) {
        outgoingFilters = new Vector (filters);
    }
    protected ISOMsg applyFilters (Collection filters, ISOMsg m, LogEvent evt) 
        throws VetoException
    {
        Iterator iter  = outgoingFilters.iterator();
        while (iter.hasNext()) {
            m.setDirection(ISOMsg.OUTGOING);
            m = ((ISOFilter) iter.next()).filter (this, m, evt);
        }
        m.setDirection(ISOMsg.OUTGOING);
        return m;
    }

    public void run () {
        for (;;) {
            try {
                if (!usable) {
                    Thread.sleep (1000);
                    continue;
                }
                mux = ISOMUX.getMUX (cfg.get ("destination-mux"));
                if (mux != null && mux.isConnected ()) {
                    ISOMsg msg = (ISOMsg) queue.dequeue();
                    if (isExpired (msg)) {
                        logUpdate (new LogEntry (LogEntry.DEQUEUE, null));
                        Thread.yield(); // easy baby ...
                        continue;
                    }
                    ISOMsg m   = (ISOMsg) msg.clone();
                    m.setDirection(ISOMsg.OUTGOING);
                    m = applyFilters (outgoingFilters, m, null);
                    m.setDirection(ISOMsg.OUTGOING);
                    if (cfg.getBoolean ("flag-retransmissions", false) && 
                        !msg.isRetransmission())
                    {                        
                        msg.setRetransmissionMTI();
                        logUpdate (new LogEntry (LogEntry.REQUEUE, msg));
                    }
                    int timeout = cfg.getInt ("timeout", 60000);
                    if (timeout > 0) {
                        ISORequest req = new ISORequest (m);
                        mux.queue (req);
                        ISOMsg resp = req.getResponse (timeout);
                        if (isValidResponse (resp, m))
                            logUpdate (new LogEntry (LogEntry.DEQUEUE, null));
                        else
                            queue.requeue (msg);
                    } else {
                        mux.send (m);
                        logUpdate (new LogEntry (LogEntry.DEQUEUE, null));
                    }

                    long delay = cfg.getLong ("delay");
                    if (delay > 0)
                        Thread.sleep (delay);
                }
                else
                    relax ();
            } catch (NameRegistrar.NotFoundException e) {
                relax ();
            } catch (Exception e) { 
                Logger.log (new LogEvent (this, "run", e));
                relax ();
            }
        }
    }
    private void relax () {
        try {
            Thread.sleep (1000);
        } catch (InterruptedException ie) { }
    }
    /**
     * @param resp response
     * @param formerReq former request message
     * @return true if response is valid and message can be dequeued
     */
    protected boolean isValidResponse (ISOMsg resp, ISOMsg formerReq) {
        return resp != null;
    }
    /**
     * @param request
     * @return true if message is expired and have to be discarded
     */
    protected boolean isExpired (ISOMsg m) {
        return false;
    }
    public static class LogEntry implements Serializable, Loggeable {
        public static final int QUEUE   = 0;
        public static final int REQUEUE = 1;
        public static final int DEQUEUE = 2;
        private static final long serialVersionUID=1;

        public int op;
        public Object value;
        public LogEntry (int op, Object value) {
            super();
            this.op = op;
            this.value = value;
        }
        public void dump (PrintStream p, String indent) {
            String inner = indent + "  ";
            String tag = null;
            switch (op) {
                case LogEntry.QUEUE:
                    tag = "queue";
                    break;
                case LogEntry.REQUEUE:
                    tag = "requeue";
                    break;
                case LogEntry.DEQUEUE:
                    tag = "dequeue";
                    break;
            }
            if (tag != null) {
                if (value instanceof ISOMsg) {
                    p.println (indent + "<" + tag + ">");
                    ((ISOMsg)value).dump (p, inner);
                    p.println (indent + "</" + tag + ">");
                } else {
                    p.println (indent + "<" + tag + "/>");
                }
            }
        }
    }
    public int getPendingCount() {
        return queue.pending ();
    }
    public int getMessageCount() {
        return cnt[TX];
    }
}

