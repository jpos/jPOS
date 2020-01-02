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

package org.jpos.iso;

/**
 * Tag this channel as a client one (from a Socket point of view)
 *
 * Please note that ISOChannel implementations may choose to
 * implement ClientChannel as well as ServerChannel, being a
 * client does not mean it can not be a server too.
 * 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see ISOChannel
 * @see ServerChannel
 */
public interface ClientChannel extends ISOChannel {
    /**
     * initialize an ISOChannel
     * @param host  server TCP Address
     * @param port  server port number
     */
    void setHost(String host, int port);

    /**
     * @return hostname (may be null)
     */
    String getHost();

    /**
     * @return port number (may be 0)
     */
    int getPort();
}

