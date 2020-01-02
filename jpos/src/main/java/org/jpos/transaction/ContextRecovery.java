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
 * ContextRecovery can be implemented by a TransactionParticipant in
 * order to customize the activation of a persisted context.
 *
 * @author apr
 * @since 1.4.7
 * @see TransactionParticipant
 */
public interface ContextRecovery {
    /**
     * Give participant the chance to "activate" a previously 
     * persisted context.
     *
     * @param id the Transaction identifier
     * @param context transaction context (as persisted by TransactionManager)
     * @param commit true if transaction is committing
     * @return activated context
     */
    Serializable recover(long id, Serializable context, boolean commit);
}

