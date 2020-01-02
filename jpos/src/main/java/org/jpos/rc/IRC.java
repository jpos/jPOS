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

package org.jpos.rc;

/**
 * Internal Result Code
 *
 *  0xxx: authorization/financial presentment approved.
 *  1xxx: authorization/financial presentment denied.
 *  2xxx: authorization/financial presentment denied, pick up card.
 *  3xxx: file actions.
 *  4xxx: reversal or chargeback actions.
 *  5xxx: reconciliation actions.
 *  6xxx: administrative actions.
 *  7xxx: fee collection actions.
 *  8xxx: network management actions.
 *  9xxx: error/response actions.
 * 10xxx: jPOS specific internal result codes.
 * 11xxx: User specific internal result codes.
 */
public interface IRC {
    /**
     * @return internal result code
     */
    int irc();

    /**
     * Returns the {@code int irc()} as a left-zero-padded String
     * This is the default implementation which can be overridden for performance or other reasons.
     * @return a left-zero-padded String with the integer value of this IRC
     */
    default String ircString() {
        return String.format("%04d" , irc());
    }

    /**
     * @return true if this IRC can be considered a 'success' (not necessarily an approval, could be a partial approval, advice accepted, etc.)
     */
    boolean success();

    /**
     * @return true if error type should inhibit response back to client
     */
    boolean inhibit();
}
