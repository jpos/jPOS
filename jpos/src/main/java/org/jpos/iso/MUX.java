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

package org.jpos.iso;

/**
 * MUX interface
 * @author Alejandro Revilla
 */
public interface MUX extends ISOSource {
    /**
     * Sends a message to remote host and wait for response
     * @param m message to send
     * @param timeout time to wait for a message
     * @return received message or null
     * @throws ISOException
     */
    ISOMsg request(ISOMsg m, long timeout) throws ISOException;

    /**
     * Sends a message to remote host in async way
     * @param m message to send
     * @param timeout time to wait for the response
     * @param r reference to response listener
     * @param handBack optional handback to be given to reponse listener
     * @throws ISOException
     */
    void request(ISOMsg m, long timeout, ISOResponseListener r, Object handBack)
        throws ISOException;

    /**
     * If the mux is connected, this returns true right away. Otherwise, it waits the specified timeout for connection. 
     *
     * @param timeout the time to wait for a connection, in ms
     * @return If the mux was able to connect during the specified timeout
     */
    default boolean isConnected(long timeout) {
        if (isConnected()) return true;
        long end = System.nanoTime() + timeout * 1_000_000L;
        long sleep = Math.min(500, timeout); 
        while (sleep > 0) {
            ISOUtil.sleep(sleep);
            if (isConnected()) return true;
            sleep = Math.min(500, (end - System.nanoTime())/1_000_000L);
        }
        return false;
    }
}
