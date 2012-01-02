/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import org.jdom.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.space.LocalSpace;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceListener;
import org.jpos.util.DefaultTimer;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.util.NameRegistrar;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @jmx:mbean description="QMUX" extends="org.jpos.q2.QBeanSupportMBean"
 */
public class QMUX 
    extends QBeanSupport
    implements SpaceListener, MUX, QMUXMBean, Loggeable, ISOSource
{
    final String nomap = "0123456789";
    final String DEFAULT_KEY = "41, 11";
    private boolean headerIsKey;
    protected LocalSpace sp;
    protected String in, out, unhandled;
    protected String[] ready;
    protected String spaceName;
    protected String[] key;
    protected String ignorerc;
    protected String[] mtiMapping;
    List listeners;
    int rx, tx, rxExpired, txExpired, rxPending, rxUnhandled, rxForwarded;
    long lastTxn = 0L;
    boolean listenerRegistered;
    public QMUX () {
        super ();
        listeners = new ArrayList ();
    }
    public void initService () throws ConfigurationException {
        Element e = getPersist ();
        sp        = grabSpace (e.getChild ("space")); 
        in        = e.getChildTextTrim ("in");
        out       = e.getChildTextTrim ("out");
        ignorerc  = e.getChildTextTrim ("ignore-rc");
        key = toStringArray(e.getChildTextTrim("key"), ", ", DEFAULT_KEY);
        ready     = toStringArray(e.getChildTextTrim ("ready"));
        mtiMapping = toStringArray(e.getChildTextTrim ("mtimapping"));
        if (mtiMapping == null || mtiMapping.length != 3) 
            mtiMapping = new String[] { nomap, nomap, "0022446789" };
        addListeners ();
        unhandled = e.getChildTextTrim ("unhandled");
        NameRegistrar.register ("mux."+getName (), this);
    }
    public void startService () {
        if (!listenerRegistered) {
            listenerRegistered = true;
            sp.addListener (in, this);
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
        if (sp.rdp (req) != null)
            throw new ISOException ("Duplicate key '" + req + "' detected");
        sp.out (req, m);
        m.setDirection(0);
        if (timeout > 0)
            sp.out (out, m, timeout);
        else
            sp.out (out, m);

        ISOMsg resp = null;
        try {
            synchronized (this) { tx++; rxPending++; }

            for (;;) {
                resp = (ISOMsg) sp.rd (key, timeout);
                if (shouldIgnore (resp)) 
                    continue;
                sp.inp (key);
                break;
            } 
            if (resp == null && sp.inp (req) == null) {
                // possible race condition, retry for a few extra seconds
                resp = (ISOMsg) sp.in (key, 10000);
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
        return resp;
    }
    public void notify (Object k, Object value) {
        Object obj = sp.inp (k);
        if (obj instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) obj;
            try {
                String key = getKey (m);
                String req = key + ".req";
                Object r = sp.inp (req);
                if (r != null) {
                    if (r instanceof AsyncRequest) {
                        ((AsyncRequest) r).responseReceived (m);
                    } else {
                        sp.out (key, m);
                    }
                    return;
                }
            } catch (ISOException e) { 
                getLog().warn ("notify", e);
            }
            processUnhandled (m);
        }
    }

    public String getKey (ISOMsg m) throws ISOException {
        StringBuffer sb = new StringBuffer (out);
        sb.append ('.');
        sb.append (mapMTI(m.getMTI()));
        if (headerIsKey && m.getHeader()!=null) {
            sb.append ('.');
            sb.append(ISOUtil.hexString(m.getHeader()));
            sb.append ('.');
        }
        boolean hasFields = false;
        for (int i = 0; i < key.length; i++) {
            String f = key[i];
            String v = m.getString(f);
            if (v != null) {
                if ("11".equals(f)) {
                    String vt = v.trim();
                    int l = m.getMTI().charAt(0) == '2' ? 12 : 6;
                    if (vt.length() < l)
                        v = ISOUtil.zeropad(vt, l);
                }
                if ("41".equals(f)) {
                    v = ISOUtil.zeropad(v.trim(),16); // BIC ANSI to ISO hack
                }
                hasFields = true;
                sb.append(v);
            }
        }
        if (!hasFields)
            throw new ISOException ("Key fields not found - not sending " + sb.toString());
        return sb.toString();
    }
    private String mapMTI (String mti) {
        StringBuffer sb = new StringBuffer();
        if (mti != null && mti.length() == 4) {
            for (int i=0; i<mtiMapping.length; i++) {
                int c = mti.charAt (i) - '0';
                if (c >= 0 && c < 10) 
                    sb.append (mtiMapping[i].charAt(c));
            }
        }
        return sb.toString();
    }
    /**
     * @jmx:managed-attribute description="input queue"
     */
    public synchronized void setInQueue (String in) {
        this.in = in;
        getPersist().getChild("in").setText (in);
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="input queue"
     */
    public String getInQueue () {
        return in;
    }

    /**
     * @jmx:managed-attribute description="output queue"
     */
    public synchronized void setOutQueue (String out) {
        this.out = out; 
        getPersist().getChild("out").setText (out);
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="output queue"
     */
    public String getOutQueue () {
        return out;
    }
    public Space getSpace() {
        return sp;
    }
    /**
     * @jmx:managed-attribute description="unhandled queue"
     */
    public synchronized void setUnhandledQueue (String unhandled) {
        this.unhandled = unhandled;
        getPersist().getChild("unhandled").setText (unhandled);
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="unhandled queue"
     */
    public String getUnhandledQueue () {
        return unhandled;
    }
    public void request (ISOMsg m, long timeout, ISOResponseListener rl, Object handBack)
        throws ISOException 
    {
        String key = getKey (m);
        String req = key + ".req";
        if (sp.rdp (req) != null)
            throw new ISOException ("Duplicate key '" + req + "' detected.");
        m.setDirection(0);
        AsyncRequest ar = new AsyncRequest (rl, handBack);
        synchronized (ar) {
            if (timeout > 0)
                DefaultTimer.getTimer().schedule (ar, timeout);
        }
        sp.out (req, ar, timeout);
        sp.out (out, m, timeout);
    }
    @SuppressWarnings("unused")
    public String[] getReadyIndicatorNames() {
        return ready;
    }
    private void addListeners () 
        throws ConfigurationException
    {
        QFactory factory = getFactory ();
        Iterator iter = getPersist().getChildren (
            "request-listener"
        ).iterator();
        while (iter.hasNext()) {
            Element l = (Element) iter.next();
            ISORequestListener listener = (ISORequestListener) 
                factory.newInstance (l.getAttributeValue ("class"));
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
        append (sb, ", tx_expired=", txExpired);
        append (sb, ", tx_pending=", sp.size(out));
        append (sb, ", rx_expired=", rxExpired);
        append (sb, ", rx_pending=", rxPending);
        append (sb, ", rx_unhandled=", rxUnhandled);
        append (sb, ", rx_forwarded=", rxForwarded);
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

    public long getLastTxnTimestampInMillis() {
        return lastTxn;
    }
    public long getIdleTimeInMillis() {
        return lastTxn > 0L ? System.currentTimeMillis() - lastTxn : -1L;
    }
    
    protected void processUnhandled (ISOMsg m) {
        ISOSource source = m.getSource () != null ? m.getSource() : this;
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
     *
     */
    public void send(ISOMsg m) throws IOException, ISOException, ISOFilter.VetoException {
        sp.out (out, m);
    }

    public boolean isConnected() {
        if (ready != null && ready.length > 0) {
            for (int i=0; i<ready.length; i++)
                if (sp.rdp (ready[i]) != null)
                    return true;
            return false;
        }
        else
            return true;
    }
    public void dump (PrintStream p, String indent) {
        p.println (indent + getCountersAsString());
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
            return ignorerc.indexOf(m.getString(39)) >= 0;
        }
        return false;
    }
    private void append (StringBuffer sb, String name, int value) {
        sb.append (name);
        sb.append (value);
    }
    public class AsyncRequest extends TimerTask {
        ISOResponseListener rl;
        Object handBack;
        public AsyncRequest (ISOResponseListener rl, Object handBack) {
            super();
            this.rl = rl;
            this.handBack = handBack;
        }
        public void responseReceived (ISOMsg response) {
            cancel();
            ISOResponseListener _rl;
            synchronized (this) {
                _rl = rl;
                rl = null;
            }
            if (_rl != null)
                _rl.responseReceived (response, handBack);
        }
        public void run() {
            cancel();
            ISOResponseListener _rl;
            synchronized (this) {
                _rl = rl;
                rl = null;
            }
            if (_rl != null)
                _rl.expired(handBack);
        }
    }
}
