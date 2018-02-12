/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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

package org.jpos.transaction.participant;

import java.io.Serializable;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;
import org.jpos.rc.CMF;
import org.jpos.rc.Result;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Caller;
import org.jpos.util.Chronometer;
import org.jpos.util.NameRegistrar;
import org.jpos.transaction.Context;

@SuppressWarnings("unused")
public class QueryHost implements TransactionParticipant, ISOResponseListener, Configurable {
    private static final long DEFAULT_TIMEOUT = 30000L;
    private static final long DEFAULT_WAIT_TIMEOUT = 12000L;

    private long timeout;
    private long waitTimeout;
    private String requestName;
    private String responseName;
    private String destination;
    private boolean continuations;
    private Configuration cfg;
    private String request;

    public QueryHost () {
        super();
    }
    public int prepare (long id, Serializable ser)  {
        Context ctx = (Context) ser;

        Result result = ctx.getResult();
        String ds = ctx.getString(destination);
        if (ds == null) {
            return result.fail(
              CMF.MISCONFIGURED_ENDPOINT, Caller.info(), "'%s' not present in Context", destination
            ).FAIL();
        }
        String muxName = cfg.get ("mux." + ds , "mux." + ds);
        MUX mux =  (MUX) NameRegistrar.getIfExists (muxName);
        if (mux == null)
            return result.fail(CMF.MISCONFIGURED_ENDPOINT, Caller.info(), "MUX '%s' not found", muxName).FAIL();

        ISOMsg m = (ISOMsg) ctx.get (requestName);
        if (m == null)
            return result.fail(CMF.INVALID_REQUEST, Caller.info(), "'%s' is null", requestName).FAIL();

        Chronometer chronometer = new Chronometer();
        if (isConnected(mux)) {
            long t = Math.max(timeout - chronometer.elapsed(), 1000L); // give at least a second to catch a response
            try {
                if (continuations) {
                    mux.request(m, t, this, ctx);
                    return PREPARED | READONLY | PAUSE | NO_JOIN;
                } else {
                    ISOMsg resp = mux.request(m, t);
                    if (resp != null) {
                        ctx.put(responseName, resp);
                        return PREPARED | READONLY | NO_JOIN;
                    } else {
                        return result.fail(CMF.HOST_UNREACHABLE, Caller.info(), "'%s' does not respond", muxName).FAIL();
                    }
                }
            } catch (ISOException e) {
                return result.fail(CMF.SYSTEM_ERROR, Caller.info(), e.getMessage()).FAIL();
            }
        } else {
            return result.fail(CMF.HOST_UNREACHABLE, Caller.info(), "'%s' is not connected", muxName).FAIL();
        }

    }

    public void responseReceived (ISOMsg resp, Object handBack) {
        Context ctx = (Context) handBack;
        ctx.put (responseName, resp);
        ctx.resume();
    }
    public void expired (Object handBack) {
        Context ctx = (Context) handBack;
        ctx.resume();
    }
    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        timeout = cfg.getLong ("timeout", DEFAULT_TIMEOUT);
        waitTimeout = cfg.getLong ("wait-timeout", DEFAULT_WAIT_TIMEOUT);
        requestName = cfg.get ("request", ContextConstants.REQUEST.toString());
        responseName = cfg.get ("response", ContextConstants.RESPONSE.toString());
        destination = cfg.get ("destination", ContextConstants.DESTINATION.toString());
        continuations = cfg.getBoolean("continuations", true);
    }

    protected boolean isConnected (MUX mux) {
        if (mux.isConnected())
            return true;
        long timeout = System.currentTimeMillis() + waitTimeout;
        while (System.currentTimeMillis() < timeout) {
            if (mux.isConnected())
                return true;
            ISOUtil.sleep (500);
        }
        return false;
    }
}
