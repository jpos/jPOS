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
 * Base JFR event emitted from the jPOS channel layer.
 *
 * <p>Concrete subclasses (declared as static inner classes) tag specific
 * channel lifecycle moments — connect, accept, send, receive, disconnect,
 * and their exceptional variants.
 */
@Category("jPOS")
@Name("jpos.Channel")
@StackTrace
public class ChannelEvent extends Event {
    /** Free-form event detail, recorded as the JFR field {@code detail}. */
    @Name("detail")
    protected String detail;

    /** Constructs an empty event with no detail. */
    public ChannelEvent() {}
    /**
     * Constructs an event with the given detail string.
     *
     * @param detail event detail text
     */
    public ChannelEvent(String detail) {
        this.detail = detail;
    }

    /**
     * Replaces the event detail.
     *
     * @param detail event detail text
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /**
     * Returns the current event detail.
     *
     * @return event detail text, or {@code null} if not set
     */
    public String getDetail() {
        return detail;
    }

    /**
     * Appends additional information to the existing detail string,
     * separated by a comma.
     *
     * @param additionalDetail text to append
     * @return this event for chaining
     */
    public ChannelEvent append (String additionalDetail) {
        detail = detail != null ?
          "%s, %s".formatted (detail, additionalDetail) : additionalDetail;
        return this;
    }

    /** JFR event recorded for a successful channel send. */
    @Name("jpos.Channel.Send")
    public static class Send extends ChannelEvent {
        /** Creates an empty Send event with no detail. */
        public Send() {}
    }

    /** JFR event recorded for a successful channel receive. */
    @Name("jpos.Channel.Receive")
    public static class Receive extends ChannelEvent {
        /** Creates an empty Receive event with no detail. */
        public Receive() {}
    }

    /** JFR event recorded when an outbound channel completes its connect handshake. */
    @Name("jpos.Channel.Connect")
    public static class Connect extends ChannelEvent {
        /** Creates an empty Connect event with no detail. */
        public Connect() {}
    }

    /** JFR event recorded when a server channel accepts a new client. */
    @Name("jpos.Channel.Accept")
    public static class Accept extends ChannelEvent {
        /** Creates an empty Accept event with no detail. */
        public Accept() {}
    }

    /** JFR event recorded when a channel disconnects. */
    @Name("jpos.Channel.Disconnect")
    public static class Disconnect extends ChannelEvent {
        /** Creates an empty Disconnect event with no detail. */
        public Disconnect() {}
    }

    /** JFR event recorded when an outbound connect attempt fails. */
    @Name("jpos.Channel.ConnectionException")
    public static class ConnectionException extends ChannelEvent {
        /**
         * Constructs the event with a description of the failure.
         *
         * @param detail failure description
         */
        public ConnectionException(String detail) {
            super(detail);
        }
    }

    /** JFR event recorded when accepting an inbound connection fails. */
    @Name("jpos.Channel.AcceptException")
    public static class AcceptException extends ChannelEvent {
        /**
         * Constructs the event with a description of the failure.
         *
         * @param detail failure description
         */
        public AcceptException(String detail) {
            super(detail);
        }
    }

    /** JFR event recorded when sending a message on a channel fails. */
    @Name("jpos.Channel.SendException")
    public static class SendException extends ChannelEvent {
        /**
         * Constructs the event with a description of the failure.
         *
         * @param detail failure description
         */
        public SendException(String detail) {
            super(detail);
        }
    }

}
