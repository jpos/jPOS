/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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

import java.io.PrintStream;

import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jpos.q2.QFactory;
import org.jpos.q2.QBeanSupport;
import org.jpos.core.ConfigurationException;
import org.jdom.Element;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.LocalSpace;
import org.jpos.space.SpaceListener;

import org.jpos.iso.MUX;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOResponseListener;
import org.jpos.iso.ISOException;
import org.jpos.util.Loggeable;
import org.jpos.util.DefaultTimer;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

/**
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @jmx:mbean description="QMUX" extends="org.jpos.q2.QBeanSupportMBean"
 */
public class QMUX 
    extends QBeanSupport
    implements SpaceListener, MUX, QMUXMBean, Loggeable
{
    final String nomap = "0123456789";
    protected LocalSpace sp;
    protected String in, out, unhandled;
    protected String[] ready;
    protected String spaceName;
    protected int[] key;
    protected String ignorerc;
    protected String[] mtiMapping = new String[] { nomap, nomap, "0022456789" };
    List listeners;
    int rx, tx, rxExpired, txExpired, rxPending, rxUnhandled, rxForwarded;
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
        key       = toIntArray(e.getChildTextTrim ("key"));
        ready     = toStringArray(e.getChildTextTrim ("ready"));
        addListeners ();
        unhandled = e.getChildTextTrim ("unhandled");
        sp.addListener (in, this);
        NameRegistrar.register ("mux."+getName (), this);
    }
    public void startService () {
        if (getState() == STOPPED) {
            sp.addListener (in, this);
            NameRegistrar.register ("mux."+getName (), this);
        }
    }
    public void stopService () {
        NameRegistrar.unregister ("mux."+getName ());
        sp.removeListener (in, this);
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
                    rx++;
                else {
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
        for (int i=0; i<key.length; i++) {
            int f = key[i];
            String v = m.getString(f);
            if (v != null) {
                if (f == 41) 
                    v = ISOUtil.zeropad (v.trim(), 16); // BIC ANSI to ISO hack
                else if (f == 11) 
                    v = ISOUtil.zeropad (v.trim(), m.getMTI().charAt(0)=='2' ? 12 : 6); 
                sb.append (v);
            }
        }
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
        m.setDirection(0);
        AsyncRequest ar = new AsyncRequest (rl, handBack);
        synchronized (ar) {
            if (timeout > 0)
                DefaultTimer.getTimer().schedule (ar, timeout);
        }
        sp.out (req, ar, timeout);
        sp.out (out, m, timeout);
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
        return sb.toString();
    }
    protected void processUnhandled (ISOMsg m) {
        ISOSource source = m.getSource ();
        if (source != null) {
            Iterator iter = listeners.iterator();
            if (iter.hasNext())
                synchronized (this) { rxForwarded++; }
            while (iter.hasNext())
                if (((ISORequestListener)iter.next()).process (source, m))
                    return;
        }
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
    private String[] toStringArray (String s) {
        String[] ready = null;
        if (s != null && s.length() > 0) {
            StringTokenizer st = new StringTokenizer (s);
            ready = new String[st.countTokens()];
            for (int i=0; st.hasMoreTokens(); i++)
                ready[i] = st.nextToken();
        }
        return ready;
    }
    private int[] toIntArray (String s) 
        throws ConfigurationException
    {
        if (s == null || s.length() == 0)
            s = "41, 11";
        try {
            int[] k = null;
            StringTokenizer st = new StringTokenizer (s, ", ");
            k = new int[st.countTokens()];
            for (int i=0; st.hasMoreTokens(); i++)
                k[i] = Integer.parseInt(st.nextToken());
            return k;
        } catch (NumberFormatException e) {
            throw new ConfigurationException (e);
        }
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

