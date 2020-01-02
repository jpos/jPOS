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

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI based Space proxy
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @since 2.0
 * @see Space
 */

public interface RemoteSpace extends Remote {

    /**
     * Write a new entry into the Space
     * @param key Entry's key
     * @param value Object value
     * @throws RemoteException
     */
    void out(Serializable key, Serializable value)
        throws RemoteException;


    /**
     * Write a new leased entry into the Space. Entry will remain valid
     * for a limited amount of time.
     * @param key Entry's key
     * @param value Object value
     * @param timeout entry valid time
     * @throws RemoteException
     */
    void out(Serializable key, Serializable value, long timeout)
        throws RemoteException;

    /**
     * Take an entry from the space, waiting forever until one exists.
     * @param key Entry's key
     * @return value
     * @throws RemoteException
     */
    Serializable in(Serializable key)
        throws RemoteException;

    /**
     * Read an entry from the space, waiting forever until one exists.
     * @param key Entry's key
     * @return value
     * @throws RemoteException
     */
    Serializable rd(Serializable key)
        throws RemoteException;

    /**
     * Take an entry from the space, waiting a limited amount of time
     * until one exists.
     * @param key Entry's key
     * @param timeout millis to wait
     * @return value or null
     * @throws RemoteException
     */
    Serializable in(Serializable key, long timeout)
        throws RemoteException;


    /**
     * Read an entry from the space, waiting a limited amount of time
     * until one exists.
     * @param key Entry's key
     * @param timeout millis to wait
     * @return value or null
     * @throws RemoteException
     */
    Serializable rd(Serializable key, long timeout)
        throws RemoteException;

    /**
     * In probe takes an entry from the space if one exists, 
     * return null otherwise.
     * @param key Entry's key
     * @return value or null
     * @throws RemoteException
     */
    Serializable inp(Serializable key)
        throws RemoteException;

    /**
     * Read probe reads an entry from the space if one exists, 
     * return null otherwise.
     * @param key Entry's key
     * @return value or null
     * @throws RemoteException
     */
    Serializable rdp(Serializable key)
        throws RemoteException;

}

