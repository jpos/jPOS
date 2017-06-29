/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2017 jPOS Software SRL
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
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.transaction.Context;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.ContextConstants;

import static org.jpos.transaction.ContextConstants.SOURCE;
import static org.jpos.transaction.ContextConstants.RESPONSE;
import static org.jpos.transaction.ContextConstants.TX;

@SuppressWarnings("unused")
public class SendResponse implements AbortParticipant, Configurable {
    private String source;
    private String response;

    public int prepare (long id, Serializable context) {
        Context ctx = (Context) context;
        ISOSource source = (ISOSource) ctx.get (this.source);
        if (source == null || !source.isConnected())
            return ABORTED | READONLY | NO_JOIN;

        return PREPARED | READONLY;
    }
    public void commit (long id, Serializable context) {
        sendResponse(id, (Context) context);
    }
    public void abort (long id, Serializable context) {
        sendResponse(id, (Context) context);
    }
    private void sendResponse (long id, Context ctx) {
        ISOSource src = (ISOSource) ctx.get (source);
        ISOMsg resp = (ISOMsg) ctx.get (response);
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
            else
                src.send (resp);
        } catch (Throwable t) {
            ctx.log(t);
        }
    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        source   = cfg.get ("source",   SOURCE.toString());
        response = cfg.get ("response", RESPONSE.toString());
    }
}
