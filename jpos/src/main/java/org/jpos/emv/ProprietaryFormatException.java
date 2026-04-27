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

package org.jpos.emv;

/**
 * Thrown when an EMV field uses a proprietary format that cannot be processed.
 * @author Vishnu Pillai
 */
public class ProprietaryFormatException extends Exception {
    /** Default constructor. */
    public ProprietaryFormatException() {
        super();
    }

    /**
     * Constructs a new exception with the given detail message.
     *
     * @param message failure description
     */
    public ProprietaryFormatException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given detail message and cause.
     *
     * @param message failure description
     * @param cause underlying cause
     */
    public ProprietaryFormatException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception wrapping the given cause.
     *
     * @param cause underlying cause
     */
    public ProprietaryFormatException(final Throwable cause) {
        super(cause);
    }
}
