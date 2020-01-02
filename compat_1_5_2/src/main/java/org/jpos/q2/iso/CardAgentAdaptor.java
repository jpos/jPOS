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

package org.jpos.q2.iso;

import org.jdom2.Element;
import org.jpos.core.CardAgent;
import org.jpos.core.CardAgentLookup;
import org.jpos.core.Configurable;
import org.jpos.q2.QFactory;

/**
 * Task Adaptor
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
public class CardAgentAdaptor extends TaskAdaptor {
    public CardAgentAdaptor () {
        super ();
    }
    protected void startService () throws Exception {
        Object obj = getObject();
        if (obj instanceof Configurable) {
            QFactory factory = getServer().getFactory();
            Element e = getPersist ();
            ((Configurable)obj).setConfiguration (
                factory.getConfiguration (e)
            );
        }
        CardAgentLookup.add ((CardAgent) getObject ());
    }
    protected void stopService () throws Exception {
        CardAgentLookup.remove ((CardAgent) getObject ());
    }
}

