/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.util.NameRegistrar;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author apr
 */
public class MUXPool extends QBeanSupport implements MUX, MUXPoolMBean {
    public static final int PRIMARY_SECONDARY = 0;
    public static final int ROUND_ROBIN = 1;
    public static final int ROUND_ROBIN_WITH_OVERRIDE = 2;
    public static final int SPLIT_BY_DIVISOR = 3;

    int strategy = 0;
    String[] muxName;
    MUX[] mux;
    AtomicInteger msgno = new AtomicInteger();
    String[] overrideMTIs;
    String originalChannelField = "";
    String splitField = "";
    boolean checkEnabled;
    Space sp;
    MUXPoolStrategyHandler strategyHandler;

    public void initService () throws ConfigurationException {
        Element e = getPersist ();
        muxName = toStringArray(e.getChildTextTrim ("muxes"));
        strategy = getStrategy(e.getChildTextTrim("strategy"));
        overrideMTIs = toStringArray(e.getChildTextTrim("follower-override"));
        originalChannelField = e.getChildTextTrim("original-channel-field");
        splitField = e.getChildTextTrim("split-field");
        checkEnabled = cfg.getBoolean("check-enabled");
        sp = grabSpace (e.getChild ("space"));

        if (muxName != null)
            mux = new MUX[muxName.length];
        else
            throw new ConfigurationException(
                String.format("<muxes> element not configured for %s '%s'",
                    getClass().getName(), getName()));

        try {
            for (int i=0; i<mux.length; i++)
                mux[i] = QMUX.getMUX (muxName[i]);
        } catch (NameRegistrar.NotFoundException ex) {
            throw new ConfigurationException (ex);
        }

        initHandler(e.getChild("strategy-handler"));
        NameRegistrar.register ("mux."+getName (), this);
    }
    public void stopService () {
        NameRegistrar.unregister ("mux."+getName ());
    }

    protected void initHandler(Element e) throws ConfigurationException {
        if (e == null)
            return;

        QFactory factory = getFactory();
        strategyHandler = factory.newInstance(QFactory.getAttributeValue (e, "class"));
        factory.setLogger(strategyHandler, e);
        factory.setConfiguration(strategyHandler, e);
    }

    public ISOMsg request (ISOMsg m, long timeout) throws ISOException {
        if (timeout == 0) {
            // a zero timeout intent is to fire-and-forget,
            // you should use 'send' instead of 'request'
            try {
                send(m);
            } catch (IOException e) {
                throw new ISOException(e.getMessage(), e);
            }
        }

        long maxWait = System.currentTimeMillis() + timeout;
        MUX mux = getMUX(m,maxWait);
        if (mux != null) {
            long remainingTimeout = maxWait - System.currentTimeMillis();
            if (remainingTimeout >= 0)
                return mux.request(m, remainingTimeout);
        }
        return null;
    }

    public void request(ISOMsg m, long timeout, final ISOResponseListener r, final Object handBack)
        throws ISOException
    {
        if (timeout == 0) {
            // a zero timeout intent is to fire-and-forget,
            // you should use 'send' instead of 'request'
            try {
                send(m);
                new Thread(() -> r.expired(handBack)).start();
            } catch (IOException e) {
                throw new ISOException(e.getMessage(), e);
            }
        }

        long maxWait = System.currentTimeMillis() + timeout;
        MUX mux = getMUX(m,maxWait);
        if (mux != null) {
            long remainingTimeout = maxWait - System.currentTimeMillis();
            if (remainingTimeout >= 0)
                mux.request(m, remainingTimeout, r, handBack);
            else {
                new Thread(()->r.expired(handBack)).start();
            }
        } else
            throw new ISOException ("No MUX available");
    }

    public void send (ISOMsg m) throws ISOException, IOException {
        long maxWait = System.currentTimeMillis() + 1000L; // reasonable default
        MUX mux = getMUX(m,maxWait);

        if (mux == null)
            throw new ISOException ("No available MUX");

        mux.send(m);
    }

    protected MUX firstAvailableMUX (long maxWait) {
        do {
            for (MUX m : mux)
                if (isUsable(m))
                    return m;
            ISOUtil.sleep (1000);
        } while (System.currentTimeMillis() < maxWait);
        return null;
    }

    protected MUX nextAvailableMUX (int mnumber, long maxWait) {
        do {
            for (int i=0; i<mux.length; i++) {
                int j = (mnumber+i) % mux.length;
                if (isUsable(mux[j]))
                    return mux[j];
                msgno.incrementAndGet();
            }
            ISOUtil.sleep (1000);
        } while (System.currentTimeMillis() < maxWait);
        return null;
    }

    private boolean overrideMTI(String mtiReq) {
        if(overrideMTIs != null){
            for (String mti : overrideMTIs) {
                if(mti.equals(mtiReq))
                    return true;
            }
        }
        return false;
    }

    private MUX nextAvailableWithOverrideMUX(ISOMsg m, long maxWait) {
        try{
            if(originalChannelField != null && !"".equals(originalChannelField)) {
                String channelName = m.getString(originalChannelField);
                if(channelName != null && !"".equals(channelName) && overrideMTI(m.getMTI())){
                    ChannelAdaptor channel = NameRegistrar.get (channelName);
                    for (MUX mx : mux) {
                        if(channel != null && ((QMUX)mx).getInQueue().equals(channel.getOutQueue())){
                            if(isUsable(mx))
                                return mx;
                        }
                    }
                }
            }
            return nextAvailableMUX(msgno.incrementAndGet(), maxWait);
        }catch(Exception e){
            getLog().warn(e);
        }
        return null;
    }

    private MUX splitByDivisorMUX(ISOMsg m, long maxWait) {
        try{
            if(splitField != null && !"".equals(splitField)){
                if(m.hasField(splitField) && ISOUtil.isNumeric(m.getString(splitField),10)){
                    MUX mx = mux[(int)(Long.valueOf(m.getString(splitField))%mux.length)];
                    if(isUsable(mx))
                        return mx;
                }
            }
            return nextAvailableMUX(msgno.incrementAndGet(), maxWait);
        }catch(Exception e){
            getLog().warn(e);
        }
        return null;
    }

    private int getStrategy(String stg) {
        if (stg == null)
            return PRIMARY_SECONDARY;

        stg = stg.trim();
        switch (stg) {
            case "round-robin":
                return ROUND_ROBIN;
            case "round-robin-with-override":
                return ROUND_ROBIN_WITH_OVERRIDE;
            case "split-by-divisor":
                return SPLIT_BY_DIVISOR;
            default:
                return PRIMARY_SECONDARY;
        }
    }

    private MUX getMUX(ISOMsg m, long maxWait) {
        MUX mux = null;
        if (strategyHandler != null) {
             mux = strategyHandler.getMUX(m, maxWait, this);
        }

        if (mux != null)
            return mux;

        switch (strategy) {
            case ROUND_ROBIN: return nextAvailableMUX(msgno.incrementAndGet(), maxWait);
            case ROUND_ROBIN_WITH_OVERRIDE: return nextAvailableWithOverrideMUX(m, maxWait);
            case SPLIT_BY_DIVISOR: return splitByDivisorMUX(m, maxWait);
            default: return firstAvailableMUX(maxWait);
        }
    }

    @Override
    public String[] getMuxNames() {
        return muxName;
    }

    @Override
    public int getStrategy() {
        return strategy;
    }

    private Space grabSpace (Element e) throws ConfigurationException
    {
        String uri = e != null ? e.getText() : "";
        return SpaceFactory.getSpace (uri);
    }

    public boolean isConnected() {
        for (MUX m : mux)
            if (isUsable(m))
                return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean isUsable (MUX mux) {
        if (!checkEnabled || !(mux instanceof QMUX))
            return mux.isConnected();

        QMUX qmux = (QMUX) mux;
        String enabledKey = qmux.getName() + ".enabled";
        String[] readyNames = qmux.getReadyIndicatorNames();
        if (readyNames != null && readyNames.length == 1) {
            // check that 'mux.enabled' entry has the same content as 'ready'
            return mux.isConnected() && sp.rdp (enabledKey) == sp.rdp (readyNames[0]);
        }
        return mux.isConnected() && sp.rdp (enabledKey) != null;
    }

    private String[] toStringArray (String s) {
        return (s != null && s.length() > 0) ? ISOUtil.toStringArray(s) : null;
    }
}
