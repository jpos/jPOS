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

package org.jpos.space;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.q2.Q2;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("unused unchecked")
public class SpaceSource implements ISOSource, SpaceListener<String,ISOMsg>, Serializable {
    private static final long serialVersionUID = -2629671264411649185L;

    private transient Space isp = SpaceFactory.getSpace();
    private transient LocalSpace sp;
    private String key;
    private long timeout;
    private boolean connected;

    public SpaceSource(LocalSpace sp, ISOSource source, long timeout) {
        this.key = "SS." + UUID.randomUUID().toString();
        this.connected = source.isConnected();
        this.sp = sp;
        sp.addListener(key, this, timeout + 10000L);
        isp.out (key, source, timeout);
    }

    public void init (LocalSpace sp, long timeout) {
        this.sp = sp;
        this.timeout = timeout;
    }

    @Override
    public void send(ISOMsg m) throws IOException, ISOException {
        if (sp == null)
            throw new IOException ("Space not configured");
        sp.out(key, m, timeout);
    }

    @Override
    public boolean isConnected() {
        return connected; // should be called _was_ connected
    }

    @Override
    public void notify(String key, ISOMsg m) {
        sp.removeListener(this.key, this);
        ISOSource source = (ISOSource) isp.inp (key);
        if (m != null && source != null && source.isConnected()) {
            try {
                source.send((ISOMsg) m.clone());
                sp.inp(key); // actually pick it
            } catch (Exception e) {
                Q2.getQ2().getLog().warn(e);
            }
        }
    }
}
