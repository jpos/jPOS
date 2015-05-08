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

package org.jpos.q2.iso;

import org.jdom.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.NameRegistrar;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author apr
 */
public class MUXPool extends QBeanSupport implements MUX, MUXPoolMBean {
    int strategy = 0;
    String[] muxName;
    MUX[] mux;
    AtomicInteger msgno = new AtomicInteger();
    public static final int ROUND_ROBIN = 1;
    public static final int PRIMARY_SECONDARY = 0;
       
    public void initService () throws ConfigurationException {
        Element e = getPersist ();
        muxName = toStringArray(e.getChildTextTrim ("muxes"));
        String s = e.getChildTextTrim ("strategy");
        strategy = "round-robin".equals (s) ? ROUND_ROBIN : PRIMARY_SECONDARY;

        mux = new MUX[muxName.length];
        try {
            for (int i=0; i<mux.length; i++)
                mux[i] = QMUX.getMUX (muxName[i]);
        } catch (NameRegistrar.NotFoundException ex) {
            throw new ConfigurationException (ex);
        }
        NameRegistrar.register ("mux."+getName (), this);
    }
    public void stopService () {
        NameRegistrar.unregister ("mux."+getName ());
    }
    public ISOMsg request (ISOMsg m, long timeout) throws ISOException {
        long maxWait = System.currentTimeMillis() + timeout;
        MUX mux = strategy == ROUND_ROBIN ?
            nextAvailableMUX (msgno.incrementAndGet(), maxWait) :
            firstAvailableMUX (maxWait);

        if (mux != null) {
            timeout = maxWait - System.currentTimeMillis();
            if (timeout >= 0)
                return mux.request (m, timeout);
        }
        return null;
    }
    public void send (ISOMsg m) throws ISOException, IOException {
        long maxWait = 1000L; // reasonable default
        MUX mux = strategy == ROUND_ROBIN ?
            nextAvailableMUX (msgno.incrementAndGet(), maxWait) :
            firstAvailableMUX (maxWait);

        if (mux == null)
            throw new ISOException ("No available MUX");

        mux.send(m);
    }
    public boolean isConnected() {
        for (MUX aMux : mux)
            if (aMux.isConnected())
                return true;
        return false;
    }
    protected MUX firstAvailableMUX (long maxWait) {
        do {
            for (MUX aMux : mux)
                if (aMux.isConnected())
                    return aMux;
            ISOUtil.sleep (1000);
        } while (System.currentTimeMillis() < maxWait);
        return null;
    }
    protected MUX nextAvailableMUX (int mnumber, long maxWait) {
        do {
            for (int i=0; i<mux.length; i++) {
                int j = (mnumber+i) % mux.length;
                if (mux[j].isConnected())
                    return mux[j];
                msgno.incrementAndGet();
            }
            ISOUtil.sleep (1000);
        } while (System.currentTimeMillis() < maxWait);
        return null;
    }
    private String[] toStringArray (String s) {
        String[] ss = null;
        if (s != null && s.length() > 0) {
            StringTokenizer st = new StringTokenizer (s);
            ss = new String[st.countTokens()];
            for (int i=0; st.hasMoreTokens(); i++)
                ss[i] = st.nextToken();
        }
        return ss;
    }
    public void request (ISOMsg m, long timeout, final ISOResponseListener r, final Object handBack) 
        throws ISOException 
    {
        int mnumber;
        long maxWait = System.currentTimeMillis() + timeout;
        mnumber = msgno.incrementAndGet();
        MUX mux = strategy == ROUND_ROBIN ?
        nextAvailableMUX (mnumber, maxWait) :
        firstAvailableMUX (maxWait);

        if (mux != null) {
            timeout = maxWait - System.currentTimeMillis();
            if (timeout >= 0)
                mux.request(m, timeout,r, handBack);
            else {
                new Thread() {
                    public void run() {
                        r.expired (handBack);
                    }
                }.start();
            }
        } else 
            throw new ISOException ("No MUX available");
    }

    @Override
    public String[] getMuxNames() {
        return muxName;
    }

    @Override
    public int getStrategy() {
        return strategy;
    }
}
