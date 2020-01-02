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

package org.jpos.transaction;

import java.io.Serializable;

/**
 * GroupSelector can be implemented by a TransactionParticipant in
 * order to switch the transaction to a new group of participants.
 *
 * @author apr
 * @since 1.4.7
 * @see TransactionParticipant
 */
public interface GroupSelector extends TransactionParticipant {
    /**
     * @param id transaction id
     * @param context transaction context 
     * @return group name or null for no-action
     */
    String select(long id, Serializable context);
}

