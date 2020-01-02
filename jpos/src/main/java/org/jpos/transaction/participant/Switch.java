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

package org.jpos.transaction.participant;

import java.io.Serializable;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.transaction.Context;
import org.jpos.transaction.GroupSelector;
import static org.jpos.transaction.ContextConstants.TXNNAME;

@SuppressWarnings("unused")
public class Switch implements Configurable, GroupSelector {
    private Configuration cfg;
    private String txnNameEntry;
    public String select (long id, Serializable ser) {
        Context ctx = (Context) ser;
        String type   = ctx.getString (txnNameEntry);
        String groups = null;
        if (type != null)
            groups = cfg.get (type, null);
        if (groups == null)
            groups = cfg.get ("unknown", "");
        ctx.log ("SWITCH " + type + " (" + groups + ")");

        return groups;
    }
    public int prepare (long id, Serializable o) {
        return PREPARED | READONLY | NO_JOIN;
    }
    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
        txnNameEntry = cfg.get("txnname", TXNNAME.toString());
    }
}
