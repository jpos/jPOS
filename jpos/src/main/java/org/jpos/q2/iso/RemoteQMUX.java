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

package org.jpos.q2.iso;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOResponseListener;

/**
 * RMI based QMUX proxy
 *
 * @author Mark Salter
 * @version $Revision: 2854 $ $Date: 2010-01-02 10:34:31 +0000 (Sat, 02 Jan 2010) $
 * @since 1.6
 * @see QMUX
 */

public interface RemoteQMUX extends Remote  {

    /**
     * Sends {@code m} and blocks for up to {@code timeout} ms waiting for a matching response.
     *
     * @param m message to send
     * @param timeout time to wait for a message
     * @return received message or null
     * @throws ISOException on pack/unpack error
     * @throws RemoteException if the RMI call fails
     */
    ISOMsg request(ISOMsg m, long timeout) throws ISOException, RemoteException;

    /**
     * Sends {@code m} asynchronously, dispatching the response (or expiration) to {@code r}.
     *
     * @param m message to send
     * @param timeout time to wait for a response
     * @param r response listener notified on success or expiration
     * @param handBack opaque token relayed back to {@code r}
     * @throws ISOException on pack/unpack error
     * @throws RemoteException if the RMI call fails
     */
    void request(ISOMsg m, long timeout, ISOResponseListener r, Object handBack)
        throws ISOException, RemoteException;

    /**
     * Indicates whether the underlying MUX is currently connected.
     *
     * @return true if connected
     * @throws RemoteException if the RMI call fails
     */
    boolean isConnected() throws RemoteException;
}

