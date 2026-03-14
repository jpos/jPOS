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
 * JMX management interface for {@link BaseChannel}.
 */
public interface BaseChannelMBean {
    /** @return the remote host name or address */
    String getHost();
    /** @param host the remote host name or address */
    void setHost(String host);
    /** @return the remote port number */
    int    getPort();
    /** @param port the remote port number */
    void setPort(int port);
    /** @return true if the channel is currently connected */
    boolean isConnected();
    /** Establishes a connection. @throws IOException on connection failure */
    void connect() throws IOException;
    /** Closes the connection. @throws IOException on close failure */
    void disconnect() throws IOException;
    /** Reconnects the channel. @throws IOException on connection failure */
    void reconnect() throws IOException;
}

