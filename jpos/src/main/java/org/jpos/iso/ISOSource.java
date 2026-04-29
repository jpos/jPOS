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

import java.io.IOException;

/**
 * Source for an ISORequest (where to send a reply)
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 * @see ISORequestListener
 */
public interface ISOSource {
    /**
     * sends (or hands back) an ISOMsg
     * @param m the Message to be sent
     * @exception IOException
     * @exception ISOException
     * @exception ISOFilter.VetoException;
     */
    void send(ISOMsg m)
        throws IOException, ISOException;

    /**
     * @return true if source is connected and usable
     */
    boolean isConnected();

    /**
     * If this ISOSource is connected, this returns true right away. Otherwise, it waits the specified timeout for connection. 
     *
     * @param timeout the time to wait for a connection, in ms
     * @return If the ISOSource connected during the specified timeout
     */
    default boolean isConnected(long timeout) {
        if (isConnected()) return true;
        long end = System.nanoTime() + timeout * 1_000_000L;
        long sleep = Math.min(500, timeout);
        while (sleep > 0 && !Thread.currentThread().isInterrupted()) { // Honor interruptions.
            ISOUtil.sleep(sleep);
            if (isConnected()) return true;
            sleep = Math.min(500, (end - System.nanoTime())/1_000_000L);
        }
        return false;
    }

}
