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

package  org.jpos.security.jceadapter;

import org.jpos.security.SMException;

/**
 * Signals that a JCE Handler exception of some sort has occurred.
 * @author Hani Samuel Kirollos
 * @version $Revision$ $Date$
 */
public class JCEHandlerException extends SMException {

    /** Default constructor. */
    public JCEHandlerException () {
        super();
    }

    /** Constructs a JCEHandlerException with the given message.
     * @param s the detail message
     */
    public JCEHandlerException (String s) {
        super(s);
    }

    /** Constructs a JCEHandlerException wrapping the given cause.
     * @param e the underlying exception
     */
    public JCEHandlerException (Exception e) {
        super(e);
    }

    /** Constructs a JCEHandlerException with the given message and cause.
     * @param s the detail message
     * @param e the underlying exception
     */
    public JCEHandlerException (String s, Exception e) {
        super(s, e);
    }
}
