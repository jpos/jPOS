/*
 * Copyright (c) 2007 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.q2.iso;

import java.util.StringTokenizer;
import org.jdom.Element;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOException;
import org.jpos.iso.MUX;
import org.jpos.iso.ISOUtil;
import org.jpos.util.NameRegistrar;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;

/**
 * @author apr
 */
public class MUXPool extends QBeanSupport implements MUX {
    int strategy = 0;
    String[] muxName;
    MUX[] mux;
    int msgno = 0;
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
        int mnumber = 0;
        long maxWait = System.currentTimeMillis() + timeout;
        synchronized (this) {
            mnumber = msgno++;
        }
        MUX mux = strategy == ROUND_ROBIN ? 
            nextAvailableMUX (mnumber, maxWait) :
            firstAvailableMUX (maxWait);

        if (mux != null) {
            timeout = maxWait - System.currentTimeMillis();
            if (timeout >= 0)
                return mux.request (m, timeout);
        }
        return null;
    }
    public boolean isConnected() {
        for (int i=0; i<mux.length; i++)
            if (mux[i].isConnected())
                return true;
        return false;
    }
    private MUX firstAvailableMUX (long maxWait) {
        do {
            for (int i=0; i<mux.length; i++)
                if (mux[i].isConnected())
                    return mux[i];
            ISOUtil.sleep (1000);
        } while (System.currentTimeMillis() < maxWait);
        return null;
    }
    private MUX nextAvailableMUX (int mnumber, long maxWait) {
        do {
            for (int i=0; i<mux.length; i++) {
                int j = (mnumber+i) % mux.length;
                if (mux[j].isConnected())
                    return mux[j];
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
}

