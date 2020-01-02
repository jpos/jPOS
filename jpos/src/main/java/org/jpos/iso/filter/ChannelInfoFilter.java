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

package org.jpos.iso.filter;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;
import org.jpos.util.LogEvent;

import java.net.InetSocketAddress;
import java.net.Socket;


/**
 * ChannelInfoFilter sets information about the channel
 * in the outgoing/incoming ISOMsg
 */
@SuppressWarnings("unused")
public class ChannelInfoFilter implements ISOFilter, Configurable {
    String channelNameField;
    String socketInfoField;
    public ChannelInfoFilter() {
        super();
    }
   /**
    * @param cfg
    * <ul>
    *  <li>channel-name: put the channel name in the given field</li>
    *  <li>socket-info: put socket information in the given field 
    *  (if the channel is an instance of BaseChannel)
    *  </li>
    * </ul>
    */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        channelNameField = cfg.get("channel-name", null);
        socketInfoField  = cfg.get("socket-info", null);
    }

    @Override
    public ISOMsg filter (ISOChannel channel, ISOMsg m, LogEvent evt) {
        if (channelNameField != null)
            m.set(channelNameField, channel.getName());
        if (socketInfoField != null && channel instanceof BaseChannel) {
            Socket socket = ((BaseChannel) channel).getSocket();
            InetSocketAddress remoteAddr =
                (InetSocketAddress) socket.getRemoteSocketAddress();
            InetSocketAddress localAddr =
                (InetSocketAddress) socket.getLocalSocketAddress();

            StringBuilder sb = new StringBuilder();
            if (socketInfoField.equals(channelNameField)) {
                sb.append(channel.getName());
                sb.append(' ');
            }
            sb.append(localAddr.getAddress().getHostAddress());
            sb.append(':');
            sb.append(Integer.toString (localAddr.getPort()));
            sb.append(' ');
            sb.append(remoteAddr.getAddress().getHostAddress());
            sb.append(':');
            sb.append(Integer.toString (remoteAddr.getPort()));
            m.set (socketInfoField, sb.toString());
        }
        return m;
    }
}
