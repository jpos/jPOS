/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2009 Alejandro P. Revilla
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

import java.net.Socket;
import java.net.InetSocketAddress;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOException;
import org.jpos.util.LogEvent;


/**
 * ChannelInfoFilter sets information about the channel
 * in the outgoing/incoming ISOMsg
 */
public class ChannelInfoFilter implements ISOFilter, Configurable {
    int channelNameField;
    int socketInfoField;
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
        channelNameField = cfg.getInt ("channel-name", 0);
        socketInfoField  = cfg.getInt ("socket-info", 0);
    }

    public ISOMsg filter (ISOChannel channel, ISOMsg m, LogEvent evt) {
        try {
            if (channelNameField > 0)
                m.set (channelNameField, channel.getName());
            if (socketInfoField > 0 && channel instanceof BaseChannel) {
                Socket socket = ((BaseChannel)channel).getSocket();
                InetSocketAddress remoteAddr = 
                    (InetSocketAddress) socket.getRemoteSocketAddress();
                InetSocketAddress localAddr = 
                    (InetSocketAddress) socket.getLocalSocketAddress();

                StringBuilder sb = new StringBuilder();
                if (channelNameField == socketInfoField) {
                    sb.append (channel.getName());
                    sb.append (' ');
                }
                sb.append (localAddr.getAddress().getHostAddress());
                sb.append (':');
                sb.append (Integer.toString (localAddr.getPort()));
                sb.append (' ');
                sb.append (remoteAddr.getAddress().getHostAddress());
                sb.append (':');
                sb.append (Integer.toString (remoteAddr.getPort()));
                m.set (socketInfoField, sb.toString());
            }
        } catch (ISOException e) {
            evt.addMessage (evt);
        }
        return m;
    }
}

