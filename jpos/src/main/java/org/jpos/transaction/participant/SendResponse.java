/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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
import org.jpos.core.annotation.Config;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.space.LocalSpace;
import org.jpos.space.SpaceSource;
import org.jpos.transaction.Context;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.TransactionManager;

import static org.jpos.transaction.ContextConstants.*;

/**
 * Transaction participant that sends the response message stored in the
 * {@link Context} back over the originating {@link ISOSource}, with optional
 * header handling driven by {@link HeaderStrategy}.
 */
@SuppressWarnings("unused")
public class SendResponse implements AbortParticipant, Configurable {
    /** Default constructor; no instance state to initialise. */
    public SendResponse() {}
    private String source;
    private String request;
    private String response;
    private LocalSpace isp;
    private long timeout = 70000L;
    private HeaderStrategy headerStrategy;

    @Config("abort-on-closed")
    private boolean abortOnClosed = true;

    public int prepare (long id, Serializable context) {
        Context ctx = (Context) context;
        ISOSource source = ctx.get (this.source);
        if (abortOnClosed && (source == null || !source.isConnected())) {
            ctx.log (this.source + " not present or not longer connected");
            return ABORTED | READONLY | NO_JOIN;
        }

        return PREPARED | READONLY;
    }

    public void commit (long id, Serializable context) {
        sendResponse(id, (Context) context);
    }
    public void abort (long id, Serializable context) {
        sendResponse(id, (Context) context);
    }

    private void sendResponse (long id, Context ctx) {
        ISOSource src = ctx.get (source);
        ISOMsg m = ctx.get(request);
        ISOMsg resp = ctx.get (response);
        try {
            if (ctx.getResult().hasInhibit()) {
                ctx.log("*** RESPONSE INHIBITED ***");
            } else if (ctx.get (TX.toString()) != null) {
                ctx.log("*** PANIC - TX not null - RESPONSE OMITTED ***");
            } else if (resp == null) {
                ctx.log (response + " not present");
            } else if (src == null) {
                ctx.log (source + " not present");
            } else if (!src.isConnected())
                ctx.log (source + " is no longer connected");
            else {
                if (src instanceof SpaceSource)
                    ((SpaceSource)src).init(isp, timeout);
                if (src.isConnected() && resp != null) {
                    headerStrategy.handleHeader(m, resp);
                    src.send(resp);
                }
            }
        } catch (Throwable t) {
            ctx.log(t);
        }
    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        source   = cfg.get ("source",   SOURCE.toString());
        request =  cfg.get ("request",  REQUEST.toString());
        response = cfg.get ("response", RESPONSE.toString());
        timeout  = cfg.getLong ("timeout", timeout);
        try {
            headerStrategy = HeaderStrategy.valueOf(
              cfg.get("header-strategy", "PRESERVE_RESPONSE").toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException (e.getMessage());
        }
    }
    /**
     * Captures the input space from the hosting transaction manager so the
     * participant can re-arm a {@link SpaceSource} during commit.
     *
     * @param tm the hosting transaction manager
     */
    public void setTransactionManager(TransactionManager tm) {
        isp = (LocalSpace) tm.getInputSpace();
    }

    /** Strategy contract for populating the response message's ISO header. */
    private interface HeaderHandler {
        /**
         * Adjusts {@code r}'s header based on the request {@code m}.
         *
         * @param m original request message
         * @param r response message about to be sent
         */
        void handleHeader (ISOMsg m, ISOMsg r);
    }

    /** Selects how the response message's ISO header is populated before sending. */
    @SuppressWarnings("unused")
    public enum HeaderStrategy implements HeaderHandler {
        /** Copies the request header onto the response. */
        PRESERVE_ORIGINAL() {
            @Override
            public void handleHeader(ISOMsg m, ISOMsg r) {
                r.setHeader(m.getHeader());
            }
        },
        /** Leaves whatever header the response already carries (default). */
        PRESERVE_RESPONSE() {
            @Override
            public void handleHeader(ISOMsg m, ISOMsg r) { }
        },
        /** Clears the response header so the channel emits no header bytes. */
        SET_TO_NULL() {
            @Override
            public void handleHeader(ISOMsg m, ISOMsg r) {
                r.setHeader((byte[]) null);
            }
        }
    }
}
