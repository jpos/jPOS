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

import org.jpos.q2.QBeanSupport;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.RemoteException;

/**
 * Remote Space Proxy Adaptor.
 *
 * @author Mark Salter
 * @author Alwyn Schoeman
 * @version $Revision: 2854 $ $Date: 2010-01-02 10:34:31 +0000 (Sat, 02 Jan 2010) $
 */

public class QMUXProxyAdaptor 
    extends QBeanSupport
    implements QMUXProxyAdaptorMBean
{
    private QMUXProxy qmuxproxy = null;
    private String qmuxName;
    private QMUX qmux;

    public QMUXProxyAdaptor () {
        super ();
    }

    protected void startService () throws RemoteException, NamingException, NotFoundException {
        qmux = (QMUX) NameRegistrar.get(qmuxName);
        qmuxproxy = new QMUXProxy(qmux);
        InitialContext ctx = new InitialContext ();
        ctx.rebind (getName (), qmuxproxy);
    }

    protected void stopService () throws NamingException {
        InitialContext ctx = new InitialContext ();
        ctx.unbind(getName());
    }
    
    public synchronized void setQmuxName (String muxName) {
        this.qmuxName = muxName;
        setAttr (getAttrs (), "qmuxName", muxName);
        setModified (true);
    }

    public String getQmuxName () {
        return qmuxName;
    }

}

   
