/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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

package org.jpos.jfr;

import jdk.jfr.*;

@Category("jPOS")
@Name("jpos.Channel")
@StackTrace
public class ChannelEvent extends Event {
    @Name("detail")
    protected String detail;

    public ChannelEvent() {}
    public ChannelEvent(String detail) {
        this.detail = detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }

    public ChannelEvent append (String additionalDetail) {
        detail = detail != null ?
          "%s, %s".formatted (detail, additionalDetail) : additionalDetail;
        return this;
    }

    @Name("jpos.Channel.Send")
    public static class Send extends ChannelEvent { }

    @Name("jpos.Channel.Receive")
    public static class Receive extends ChannelEvent { }

    @Name("jpos.Channel.Connect")
    public static class Connect extends ChannelEvent { }

    @Name("jpos.Channel.Accept")
    public static class Accept extends ChannelEvent { }

    @Name("jpos.Channel.Disconnect")
    public static class Disconnect extends ChannelEvent { }

    @Name("jpos.Channel.ConnectionException")
    public static class ConnectionException extends ChannelEvent {
        public ConnectionException(String detail) {
            super(detail);
        }
    }

    @Name("jpos.Channel.AcceptException")
    public static class AcceptException extends ChannelEvent {
        public AcceptException(String detail) {
            super(detail);
        }
    }

    @Name("jpos.Channel.SendException")
    public static class SendException extends ChannelEvent {
        public SendException(String detail) {
            super(detail);
        }
    }

}
