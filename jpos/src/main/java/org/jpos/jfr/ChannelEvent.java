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

package org.jpos.jfr;

import jdk.jfr.*;

/**
 * JFR event base class for jPOS channel operations.
 */
@Category("jPOS")
@Name("jpos.Channel")
@StackTrace
public class ChannelEvent extends Event {
    @Name("detail")
    /** Channel event detail string. */
    protected String detail;

    /** Creates a ChannelEvent with no detail. */
    public ChannelEvent() {}
    /** Creates a ChannelEvent with the given detail.
     * @param detail event detail string
     */
    public ChannelEvent(String detail) {
        this.detail = detail;
    }

    /** @param detail new detail string */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /** @return the event detail string */
    public String getDetail() {
        return detail;
    }

    /** Appends additional detail to this event.
     * @param additionalDetail text to append
     * @return this event
     */
    public ChannelEvent append (String additionalDetail) {
        detail = detail != null ?
          "%s, %s".formatted (detail, additionalDetail) : additionalDetail;
        return this;
    }

    /** JFR event for a channel send operation. */
    @Name("jpos.Channel.Send")
    public static class Send extends ChannelEvent {
        /** Default constructor. */
        public Send() { }
    }

    /** JFR event for a channel receive operation. */
    @Name("jpos.Channel.Receive")
    public static class Receive extends ChannelEvent {
        /** Default constructor. */
        public Receive() { }
    }

    /** JFR event for a channel connect operation. */
    @Name("jpos.Channel.Connect")
    public static class Connect extends ChannelEvent {
        /** Default constructor. */
        public Connect() { }
    }

    /** JFR event for a channel accept (inbound connection) operation. */
    @Name("jpos.Channel.Accept")
    public static class Accept extends ChannelEvent {
        /** Default constructor. */
        public Accept() { }
    }

    /** JFR event for a channel disconnect operation. */
    @Name("jpos.Channel.Disconnect")
    public static class Disconnect extends ChannelEvent {
        /** Default constructor. */
        public Disconnect() { }
    }

    /** JFR event for a channel connection exception. */
    @Name("jpos.Channel.ConnectionException")
    public static class ConnectionException extends ChannelEvent {
        /** @param detail exception detail string */
        public ConnectionException(String detail) {
            super(detail);
        }
    }

    /** JFR event for a channel accept exception. */
    @Name("jpos.Channel.AcceptException")
    public static class AcceptException extends ChannelEvent {
        /** @param detail exception detail string */
        public AcceptException(String detail) {
            super(detail);
        }
    }

    /** JFR event for a channel send exception. */
    @Name("jpos.Channel.SendException")
    public static class SendException extends ChannelEvent {
        /** @param detail exception detail string */
        public SendException(String detail) {
            super(detail);
        }
    }

}
