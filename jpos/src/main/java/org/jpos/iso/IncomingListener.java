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

package org.jpos.iso;

import org.jpos.transaction.ContextConstants;
import org.jpos.util.Log;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.transaction.Context;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class IncomingListener extends Log implements ISORequestListener, Configurable {
    long timeout;
    private Space<String,Context> sp;
    private String queue;
    private String source;
    private String request;
    private String timestamp;
    private Map<String,String> additionalContextEntries = null;


    @SuppressWarnings("unchecked")
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        timeout  = cfg.getLong ("timeout", 15000L);
        sp = (Space<String,Context>) SpaceFactory.getSpace (cfg.get ("space"));
        queue = cfg.get ("queue", null);
        if (queue == null)
            throw new ConfigurationException ("queue property not specified");
        source = cfg.get ("source", ContextConstants.SOURCE.toString());
        request = cfg.get ("request", ContextConstants.REQUEST.toString());
        timestamp = cfg.get ("timestamp", ContextConstants.TIMESTAMP.toString());
        Map<String,String> m = new HashMap<>();
        cfg.keySet()
           .stream()
           .filter (s -> s.startsWith("ctx."))
           .forEach(s -> m.put(s.substring(4), cfg.get(s)));
        if (m.size() > 0)
            additionalContextEntries = m;
    }
    public boolean process (ISOSource src, ISOMsg m) {
        final Context ctx  = new Context ();
        ctx.put (timestamp, new Date());
        ctx.getProfiler();
        ctx.put (source, src);
        ctx.put (request, m);
        if (additionalContextEntries != null) {
            additionalContextEntries.entrySet().forEach(
                e -> ctx.put(e.getKey(), e.getValue())
            );
        }
        sp.out(queue, ctx, timeout);
        return true;
    }
}
