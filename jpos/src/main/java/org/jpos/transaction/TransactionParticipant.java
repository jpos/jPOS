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
 * 2 phase commit participant
 * @author apr
 * @since 1.4.7
 */
public interface TransactionParticipant extends TransactionConstants {
    /**
     * Called by TransactionManager in preparation for a transaction
     * @param id the Transaction identifier
     * @param context transaction context
     * @return PREPARED or ABORTED (| NO_JOIN | READONLY)
     */
    int  prepare(long id, Serializable context);

    /**
     * Called by TransactionManager upon transaction commit.
     * Warning: implementation should be able to handle multiple calls
     * with the same transaction id (rare crash recovery)
     *
     * @param id the Transaction identifier
     * @param context transaction context
     */
    default void commit(long id, Serializable context) { }

    /**
     * Called by TransactionManager upon transaction commit.
     * Warning: implementation should be able to handle multiple calls
     * with the same transaction id (rare crash recovery)
     *
     * @param id the Transaction identifier
     * @param context transaction context
     */
    default void abort(long id, Serializable context) { }
}
