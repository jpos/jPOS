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

import java.io.IOException;
import java.net.ServerSocket;

/**
 * <code>ISOServerSocketFactory</code> is used by BaseChannel and ISOServer
 * in order to provide hooks for SSL implementations.
 *
 * @version $Revision$ $Date$
 * @author  Alejandro P. Revilla
 * @since   1.3.3
 */
public interface ISOServerSocketFactory {
    /**
    * Create a server socket on the specified port (port 0 indicates
    * an anonymous port).
    * @param  port the port number
    * @return the server socket on the specified port
    * @exception IOException should an I/O error occur
    * @exception ISOException on any other error
    * creation
    */
    ServerSocket createServerSocket(int port)
        throws IOException, ISOException;
}
