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

import java.io.IOException;
import java.io.Serializable;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISORequest;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOException;
import org.jpos.iso.FilteredBase;
import org.jpos.iso.ISOMUX;
import org.jpos.core.Sequencer;
import org.jpos.core.VolatileSequencer;
import org.jpos.util.NameRegistrar;
import org.jpos.util.BlockingQueue;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.iso.ISOFilter.VetoException;
import com.sun.jini.reliableLog.ReliableLog;
import com.sun.jini.reliableLog.LogException;
import com.sun.jini.reliableLog.LogHandler;

/**
 * Store & Forward channel
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @since 1.3.9
 */
public class SAFChannel extends LogHandler
    implements ISOChannel, LogSource, ReConfigurable, 
               Runnable, SAFChannelMBean
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
    Sequencer seq;

    public SAFChannel () {
	super();
        cnt = new int[SIZEOF_CNT];
	queue = new BlockingQueue();
        new Thread (this).start ();
    }

   /**
    * setPackager is optional on LoopbackChannel, it is
    * used for debugging/formating purposes only
    */
    public void setPackager(ISOPackager packager) {
	// N/A
    }

    public void connect () { }

    public void disconnect () { }

    public void reconnect ()  { }

    public boolean isConnected() {
	return usable;
    }

    public void send (ISOMsg m)
	throws IOException,ISOException, VetoException
    {
	if (!isConnected())
	    throw new ISOException ("unconnected ISOChannel retry later");
	LogEvent evt = new LogEvent (this, "saf-send", m);
        log.update (new LogEntry (LogEntry.QUEUE, m));
	queue.enqueue (m);
	cnt[TX]++;
	Logger.log (evt);
    }

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
     * @param cfg containing <code>logdir, flag-retransmissions, timeout, destination-mux, delay, sequencer</code> properties
     * 
     */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        if (this.cfg == null) {
	    try {
                ReliableLog log = new ReliableLog (cfg.get("logdir"), this);
                log.recover();
                log.snapshot();
                setReliableLog (log);
                // dumpList ();
                String seqName  = cfg.get ("sequencer", null);
                if (seqName != null) {
                    seq = (Sequencer) NameRegistrar.get (
                        "sequencer."+cfg.get("sequencer")
                    );
                } else if (seq == null) {
                    seq = new VolatileSequencer();
                }
                setUsable (true);
	    } catch (IOException e) {
	        throw new ConfigurationException (e);
	    } catch (NameRegistrar.NotFoundException e) {
	        throw new ConfigurationException (e);
            }
        }
        this.cfg = cfg;
    }

    public void dumpList () {
        Iterator iter = queue.getQueue().iterator();
        while (iter.hasNext()) 
            ((ISOMsg)iter.next()).dump (System.out, "   LIST>");
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
                // ((ISOMsg)entry.value).dump (System.out, "  QUEUE>");
                break;
            case LogEntry.REQUEUE:
                list.removeFirst();
                list.addFirst (entry.value);
                // ((ISOMsg)entry.value).dump (System.out, "REQUEUE>");
                break;
            case LogEntry.DEQUEUE:
                // System.out.println ("DEQUEUE>");
                list.removeFirst ();
                break;
        }
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
                    ISOMsg msg = (ISOMsg) queue.dequeue ();
                    ISORequest req = new ISORequest (applyProps (msg));
                    if (cfg.getBoolean ("flag-retransmissions", false) && 
                        !msg.isRetransmission())
                    {                        
                        ISOMsg m = (ISOMsg) msg.clone ();
                        m.setRetransmissionMTI();
                        log.update (new LogEntry (LogEntry.REQUEUE, m));
                    }
                    mux.queue (req);
                    ISOMsg resp = req.getResponse (
                        cfg.getInt ("timeout", 60000)
                    );
                    if (resp != null) 
                        log.update (new LogEntry (LogEntry.DEQUEUE, null));
                    else
                        queue.requeue (msg);
                    long delay = cfg.getLong ("delay");
                    if (delay > 0)
                        Thread.sleep (delay);
                }
            } catch (NameRegistrar.NotFoundException e) {
                try {
                    Thread.sleep (1000);
                } catch (InterruptedException ie) { }
            } catch (Exception e) { }
        }
    }
    public static class LogEntry implements Serializable {
        public static final int QUEUE   = 0;
        public static final int REQUEUE = 1;
        public static final int DEQUEUE = 2;
        private static final long serialVersionUID=956797214006808278L;

        public int op;
	public Object value;
	public LogEntry (int op, Object value) {
            super();
            this.op = op;
	    this.value = value;
            // dump ();
        }
        public void dump () {
            switch (op) {
                case LogEntry.QUEUE:
                    ((ISOMsg)value).dump (System.out, "  queue>");
                    break;
                case LogEntry.REQUEUE:
                    ((ISOMsg)value).dump (System.out, "requeue>");
                    break;
                case LogEntry.DEQUEUE:
                    System.out.println ("dequeue>");
                    break;
            }
	}
    }
    private ISOMsg applyProps (ISOMsg m) throws ISOException {
        ISOMsg msg=null;
        for (int i=0; i<128; i++) {
            if (m.hasField(i)) {
                String value = (String) m.getValue(i);
                if (msg == null && 
                    (value.charAt(0) == '$' || value.charAt(0) == '='))
                    msg = (ISOMsg) m.clone();

                if (value.equals ("$date") )
                    msg.set (new ISOField (i, ISODate.getDateTime(new Date())));
                else if (value.charAt (0) == '$')
                    msg.set (new ISOField (i,
                      ISOUtil.zeropad (
                        Integer.toString(seq.get (value.substring(1))),6)
                      )
                    );
                else if (value.charAt (0) == '=') {
                    String p = cfg.get (value.substring(1), null);
                    if (p != null)
                        msg.set (new ISOField (i, p));
                }
            }
        }
        return msg == null ? m : msg;
    }
    public int getPendingCount() {
        return queue.pending ();
    }
    public int getMessageCount() {
        return cnt[TX];
    }
}

