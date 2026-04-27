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

import org.jpos.util.LogEvent;

/**
 * An ISOFilter has the oportunity to modify an incoming or
 * outgoing ISOMsg that is about to go thru an ISOChannel.
 * It also has the chance to Veto by throwing an Exception
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public interface ISOFilter {
    /** Thrown by a filter to veto (suppress) an ISO message. */
    class VetoException extends ISOException {

        private static final long serialVersionUID = -4640160572663583113L;
        /** Default constructor. */
    public VetoException () {
            super();
        }
        /** Constructs a VetoException with the given detail message.
         * @param detail the detail message
         */
    public VetoException (String detail) {
            super(detail);
        }
        /** Constructs a VetoException wrapping the given exception.
         * @param nested the nested exception
         */
    public VetoException (Exception nested) {
            super(nested);
        }
        /** Constructs a VetoException with detail and nested exception.
         * @param detail the detail message
         * @param nested the nested exception
         */
    public VetoException (String detail, Exception nested) {
            super(detail, nested);
        }
    }
    /**
     * Filters an ISO message before sending or after receiving.
     * @param channel current ISOChannel instance
     * @param m ISOMsg to filter
     * @param evt LogEvent
     * @return an ISOMsg (possibly parameter m)
     * @throws VetoException if the message should be suppressed
     */
    ISOMsg filter(ISOChannel channel, ISOMsg m, LogEvent evt)
        throws VetoException;
}
