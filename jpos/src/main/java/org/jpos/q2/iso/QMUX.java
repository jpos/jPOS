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

package org.jpos.q2.iso;

import org.HdrHistogram.AtomicHistogram;
import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.core.Environment;
import org.jpos.iso.*;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.space.*;
import org.jpos.util.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Alejandro Revilla
 */
@SuppressWarnings("unchecked")
public class QMUX
    extends QBeanSupport
    implements SpaceListener, MUX, QMUXMBean, Loggeable, MetricsProvider
{
    static final String nomap = "0123456789";
    static final String DEFAULT_KEY = "41, 11";
    protected LocalSpace sp;
    protected String in, out, unhandled;
    protected String[] ready;
    protected String[] key;
    protected String ignorerc;
    protected String[] mtiMapping;
    private boolean headerIsKey;
    private boolean returnRejects;
    private LocalSpace isp; // internal space
    private Map<String,String[]> mtiKey = new HashMap<>();
    private Metrics metrics = new Metrics(new AtomicHistogram(60000, 2));

    List<ISORequestListener> listeners;
    private volatile int rx, tx, rxExpired, txExpired, rxPending, rxUnhandled, rxForwarded;
    private volatile long lastTxn = 0L;
    private boolean listenerRegistered;
    public QMUX () {
        super ();
        listeners = new ArrayList<>();
    }
    public void initService () throws ConfigurationException {
        Element e = getPersist ();
        sp        = grabSpace (e.getChild ("space"));
        isp       = cfg.getBoolean("reuse-space", false) ? sp : new TSpace();
        in        = Environment.get(e.getChildTextTrim ("in"));
        out       = Environment.get(e.getChildTextTrim ("out"));

        if (in == null || out == null) {
            throw new ConfigurationException ("Misconfigured QMUX. Please verify in/out queues");
        }
        ignorerc  = Environment.get(e.getChildTextTrim ("ignore-rc"));
        key = toStringArray(DEFAULT_KEY, ", ", null);
        returnRejects = cfg.getBoolean("return-rejects", false);
        for (Element keyElement : e.getChildren("key")) {
            String mtiOverride = QFactory.getAttributeValue(keyElement, "mti");
            if (mtiOverride != null && mtiOverride.length() >= 2) {
                mtiKey.put (mtiOverride.substring(0,2), toStringArray(keyElement.getTextTrim(), ", ", null));
            } else {
                key = toStringArray(e.getChildTextTrim("key"), ", ", DEFAULT_KEY);
            }
        }
        ready     = toStringArray(Environment.get(e.getChildTextTrim ("ready")));
        mtiMapping = toStringArray(Environment.get(e.getChildTextTrim ("mtimapping")));
        if (mtiMapping == null || mtiMapping.length != 3) 
            mtiMapping = new String[] { nomap, nomap, "0022446689" };
        addListeners ();
        unhandled = Environment.get(e.getChildTextTrim ("unhandled"));
        NameRegistrar.register ("mux."+getName (), this);
    }
    public void startService () {
        if (!listenerRegistered) {
            listenerRegistered = true;
            // Handle messages that could be in the in queue at start time
            synchronized (sp) {
                Object[] pending = SpaceUtil.inpAll(sp, in);
                sp.addListener (in, this);
                for (Object o : pending)
                    sp.out(in, o);
            }
        }
    }
    public void stopService () {
        listenerRegistered = false;
        sp.removeListener (in, this);
    }
    public void destroyService () {
        NameRegistrar.unregister ("mux."+getName ());
    }

    /**
     * @return MUX with name using NameRegistrar
     * @throws NameRegistrar.NotFoundException
     * @see NameRegistrar
     */
    public static MUX getMUX (String name)
        throws NameRegistrar.NotFoundException 
    {
        return (MUX) NameRegistrar.get ("mux."+name);
    }

    /**
     * @param m message to send
     * @param timeout amount of time in millis to wait for a response
     * @return response or null
     */
    public ISOMsg request (ISOMsg m, long timeout) throws ISOException {
        String key = getKey (m);
        String req = key + ".req";
        synchronized (isp) {
            if (isp.rdp (req) != null)
                throw new ISOException ("Duplicate key '" + req + "' detected");
            isp.out (req, m);
        }
        m.setDirection(0);
        Chronometer c = new Chronometer();
        if (timeout > 0)
            sp.out (out, m, timeout);
        else
            sp.out (out, m);

        ISOMsg resp;
        try {
            synchronized (this) { tx++; rxPending++; }

            for (;;) {
                resp = (ISOMsg) isp.in (key, timeout);
                if (!shouldIgnore (resp))
                    break;
            }
            if (resp == null && isp.inp (req) == null) {
                // possible race condition, retry for a few extra seconds
                resp = (ISOMsg) isp.in (key, 10000);
            }
            synchronized (this) {
                if (resp != null) 
                {
                    rx++;
                    lastTxn = System.currentTimeMillis();
                }else {
                    rxExpired++;
                    if (m.getDirection() != ISOMsg.OUTGOING)
                        txExpired++;
                }
            }
        } finally {
            synchronized (this) { rxPending--; }
        }
        long elapsed = c.elapsed();
        metrics.record("all", elapsed);
        if (resp != null)
            metrics.record("ok", elapsed);
        return resp;
    }
    public void request (ISOMsg m, long timeout, ISOResponseListener rl, Object handBack)
      throws ISOException
    {
        String key = getKey (m);
        String req = key + ".req";
        synchronized (isp) {
            if (isp.rdp (req) != null)
                throw new ISOException ("Duplicate key '" + req + "' detected.");
            m.setDirection(0);
            AsyncRequest ar = new AsyncRequest (rl, handBack);
            synchronized (ar) {
                if (timeout > 0)
                    ar.setFuture(getScheduledThreadPoolExecutor().schedule(ar, timeout, TimeUnit.MILLISECONDS));
            }
            isp.out (req, ar, timeout);
        }
        if (timeout > 0)
            sp.out (out, m, timeout);
        else
            sp.out (out, m);
        synchronized (this) { tx++; rxPending++; }
    }

    protected boolean isNotifyEligible(ISOMsg msg) {
        if (returnRejects)
            return true;

        try {
            return msg.isResponse();
        } catch (RuntimeException | ISOException ex) {
            // * ArrayIndexOutOfBoundsException - It may occur for messages where
            // MTI is not standard 4 characters (eg. FSDISOMsg), then notification is expected.
            // * ISOException: When there is no field 0, the error should be logged
            return true;
        }
    }

    @Override
    public void notify (Object k, Object value) {
        Object obj = sp.inp (k);
        if (obj instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) obj;
            try {
                if (isNotifyEligible(m)) {
                    String key = getKey (m);
                    String req = key + ".req";
                    Object r = isp.inp (req);
                    if (r != null) {
                        if (r instanceof AsyncRequest) {
                            ((AsyncRequest) r).responseReceived (m);
                        } else {
                            isp.out (key, m);
                        }
                        return;
                    }
                }
            } catch (ISOException e) {
                LogEvent evt = getLog().createLogEvent("notify");
                evt.addMessage(e);
                evt.addMessage(obj);
                Logger.log(evt);
            }
            processUnhandled (m);
        }
    }

    public String getKey (ISOMsg m) throws ISOException {
        if (out == null)
            throw new NullPointerException ("Misconfigured QMUX. Please verify out queue is not null.");
        StringBuilder sb = new StringBuilder (out);
        sb.append ('.');
        sb.append (mapMTI(m.getMTI()));
        if (headerIsKey && m.getHeader()!=null) {
            sb.append ('.');
            sb.append(ISOUtil.hexString(m.getHeader()));
            sb.append ('.');
        }
        boolean hasFields = false;
        String[] k = mtiKey.getOrDefault(m.getMTI().substring(0,2), key);
        for (String f : k) {
            String v = m.getString(f);
            if (v != null) {
                if ("11".equals(f)) {
                    String vt = v.trim();
                    int l = m.getMTI().charAt(0) == '2' ? 12 : 6;
                    if (vt.length() < l)
                        v = ISOUtil.zeropad(vt, l);
                }
                if ("41".equals(f)) {
                    v = ISOUtil.zeropad(v.trim(), 16); // BIC ANSI to ISO hack
                }
                hasFields = true;
                sb.append(v);
            }
        }
        if (!hasFields)
            throw new ISOException ("Key fields not found - not sending " + sb.toString());
        return sb.toString();
    }

    @Override
    public Metrics getMetrics() {
        return metrics;
    }

    private String mapMTI (String mti) throws ISOException {
        StringBuilder sb = new StringBuilder();
        if (mti != null) {
            if (mti.length() < 4)
                mti = ISOUtil.zeropad(mti, 4); // #jPOS-55
            if (mti.length() == 4) {
                for (int i=0; i<mtiMapping.length; i++) {
                    int c = mti.charAt (i) - '0';
                    if (c >= 0 && c < 10)
                        sb.append (mtiMapping[i].charAt(c));
                }
            }
        }
        return sb.toString();
    }
    public synchronized void setInQueue (String in) {
        this.in = in;
        getPersist().getChild("in").setText (in);
        setModified (true);
    }
    public String getInQueue () {
        return in;
    }
    public synchronized void setOutQueue (String out) {
        this.out = out; 
        getPersist().getChild("out").setText (out);
        setModified (true);
    }
    public String getOutQueue () {
        return out;
    }
    public Space getSpace() {
        return sp;
    }
    public synchronized void setUnhandledQueue (String unhandled) {
        this.unhandled = unhandled;
        getPersist().getChild("unhandled").setText (unhandled);
        setModified (true);
    }
    public String getUnhandledQueue () {
        return unhandled;
    }
    @SuppressWarnings("unused")
    public String[] getReadyIndicatorNames() {
        return ready;
    }

    private void addListeners() throws ConfigurationException {
        List<Element> rlisten = getPersist().getChildren("request-listener");
        if (rlisten.isEmpty())
            return;

        QFactory factory = getFactory ();
        for (Element l : rlisten) {
            ISORequestListener listener = (ISORequestListener) 
                factory.newInstance (QFactory.getAttributeValue (l, "class"));
            factory.setLogger        (listener, l);
            factory.setConfiguration (listener, l);
            addISORequestListener (listener);
        }
    }
    public void addISORequestListener(ISORequestListener l) {
        listeners.add (l);
    }
    public boolean removeISORequestListener(ISORequestListener l) {
    	return listeners.remove(l);
    }
    public synchronized void resetCounters() {
        rx = tx = rxExpired = txExpired = rxPending = rxUnhandled = rxForwarded = 0;
        lastTxn = 0l;
    }
    public String getCountersAsString () {
        StringBuffer sb = new StringBuffer();
        append (sb, "tx=", tx);
        append (sb, ", rx=", rx);
        append (sb, ", tx_expired=", getTXExpired());
        append (sb, ", tx_pending=", getTXPending());
        append (sb, ", rx_expired=", getRXExpired());
        append (sb, ", rx_pending=", getRXPending());
        append (sb, ", rx_unhandled=", getRXUnhandled());
        append (sb, ", rx_forwarded=", getRXForwarded());
        sb.append (", connected=");
        sb.append (Boolean.toString(isConnected()));
        sb.append (", last=");
        sb.append (lastTxn);
        if (lastTxn > 0) {
            sb.append (", idle=");
            sb.append(System.currentTimeMillis() - lastTxn);
            sb.append ("ms");
        }
        return sb.toString();
    }
    
    public int getTXCounter() {
        return tx;
    }
    public int getRXCounter() {
        return rx;
    }

    @Override
    public int getTXExpired() {
        return txExpired;
    }

    @Override
    public int getTXPending() {
        return sp.size(out);
    }

    @Override
    public int getRXExpired() {
        return rxExpired;
    }

    @Override
    public int getRXPending() {
        return rxPending;
    }

    @Override
    public int getRXUnhandled() {
        return rxUnhandled;
    }

    @Override
    public int getRXForwarded() {
        return rxForwarded;
    }

    public long getLastTxnTimestampInMillis() {
        return lastTxn;
    }
    public long getIdleTimeInMillis() {
        return lastTxn > 0L ? System.currentTimeMillis() - lastTxn : -1L;
    }

    protected void processUnhandled (ISOMsg m) {
        ISOSource source = m.getSource();
        source = source != null ? source : this;
        Iterator iter = listeners.iterator();
        if (iter.hasNext())
            synchronized (this) { rxForwarded++; }
        while (iter.hasNext())
            if (((ISORequestListener)iter.next()).process (source, m))
                return;
        if (unhandled != null) {
            synchronized (this) { rxUnhandled++; }
            sp.out (unhandled, m, 120000);
        }
    }
    private LocalSpace grabSpace (Element e) 
        throws ConfigurationException
    {
        String uri = e != null ? e.getText() : "";
        Space sp = SpaceFactory.getSpace (uri);
        if (sp instanceof LocalSpace) {
            return (LocalSpace) sp;
        }
        throw new ConfigurationException ("Invalid space " + uri);
    }

    /**
     * sends (or hands back) an ISOMsg
     *
     * @param m the Message to be sent
     * @throws java.io.IOException
     * @throws org.jpos.iso.ISOException
     * @throws org.jpos.iso.ISOFilter.VetoException;
     */
    public void send(ISOMsg m) throws IOException, ISOException {
        if (!isConnected())
            throw new ISOException ("MUX is not connected");
        sp.out (out, m);
    }

    public boolean isConnected() {
        if (running() && ready != null && ready.length > 0) {
            for (String aReady : ready)
                if (sp.rdp(aReady) != null)
                    return true;
            return false;
        }
        return running();
    }
    public void dump (PrintStream p, String indent) {
        p.println (indent + getCountersAsString());
        metrics.dump (p, indent);
    }
    private String[] toStringArray(String s, String delimiter, String def) {
        if (s == null)
            s = def;
        String[] arr = null;
        if (s != null && s.length() > 0) {
            StringTokenizer st;
            if (delimiter != null)
                st = new StringTokenizer(s, delimiter);
            else
                st = new StringTokenizer(s);

            List<String> l = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                if ("header".equalsIgnoreCase(t)) {
                    headerIsKey = true;
                } else {
                    l.add (t);
                }
            }
            arr = l.toArray(new String[l.size()]);
        }
        return arr;
    }
    private String[] toStringArray(String s) {
        return toStringArray(s, null,null);
    }
    private boolean shouldIgnore (ISOMsg m) {
        if (m != null && ignorerc != null 
            && ignorerc.length() > 0 && m.hasField(39))
        {
            return ignorerc.contains(m.getString(39));
        }
        return false;
    }
    private void append (StringBuffer sb, String name, int value) {
        sb.append (name);
        sb.append (value);
    }
    public class AsyncRequest implements Runnable {
        ISOResponseListener rl;
        Object handBack;
        ScheduledFuture future;
        Chronometer chrono;
        public AsyncRequest (ISOResponseListener rl, Object handBack) {
            super();
            this.rl = rl;
            this.handBack = handBack;
            this.chrono = new Chronometer();
        }
        public void setFuture(ScheduledFuture future) {
            this.future = future;
        }
        public void responseReceived (ISOMsg response) {
            if (future == null || future.cancel(false)) {
                synchronized (QMUX.this) {
                    rx++;
                    rxPending--;
                    lastTxn = System.currentTimeMillis();
                }
                long elapsed = chrono.elapsed();
                metrics.record("all", elapsed);
                metrics.record("ok", elapsed);
                rl.responseReceived(response, handBack);
            }
        }
        public void run() {
            synchronized(QMUX.this) {
                rxPending--;
            }
            metrics.record("all", chrono.elapsed());
            rl.expired(handBack);
        }
    }
}
